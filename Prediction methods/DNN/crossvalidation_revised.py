#!/usr/bin/env python3
# coding: utf-8

import os, time, sys
import pandas as pd
import numpy as np
import networkx as nx
from sklearn.metrics import roc_auc_score
from sklearn.metrics import ndcg_score
from sklearn.metrics import average_precision_score
from collections import defaultdict
from itertools import islice
from node2vec import Node2Vec
import node2vec.edges
import gc
from tensorflow import keras
from tensorflow.keras.layers import *
from tensorflow.keras.models import Model
from tensorflow.keras import backend as K

# Define constants
if len(sys.argv) > 1:
    DATASET_FILE = (
        "/scratch-cbe/users/loan.vulliard/edge-prediction/" + sys.argv[1] + ".csv"
    )
    SCORE_FILE_NAME = (
        "./scores/"
        + str(round(time.time()))
        + "_approach8_NN_N2V_NodeAndEdge."
        + sys.argv[1]
        + ".csv"
    )
else:
    DATASET_FILE = "HuRI_list.csv"
    SCORE_FILE_NAME = (
        "./scores/"
        + str(round(time.time()))
        + "_approach8_NN_N2V_NodeAndEdge.HuRI.csv"
    )
np.random.seed(0)

# Other choices that can be tweaked include:
# * Operation for edge embedding (e.g. Hadamard)
# * Operation for edge prediction aggregation (e.g. max)
# * Length of the node/edge embedding (e.g. 20)

# Load edge list
dataset = pd.read_csv(DATASET_FILE)
# Keep track of all nodes to consider
nodes = np.unique(np.hstack([dataset.source, dataset.target]))


full_graph = nx.from_pandas_edgelist(dataset)
npd = np.array(dataset)


cv_order = np.array(range(dataset.shape[0]))
np.random.shuffle(cv_order)
# Last fold length might differ
fold_length = np.ceil(dataset.shape[0] / 10).astype("int")


def get_non_edges(edges: pd.DataFrame, n: int = 0):
    """This method returns a balanced set of non-edges for a given
    edge list. This means all links are selected to connect the subset
    of nodes present in the original edge list.
    If `n` is provided, only `n` non-edges would be returned.
    """
    # The following graph contains all actual edges and not only the
    # selected training/testing edges and should NOT be used outside
    # of negative edge selection.
    subgraph = full_graph.subgraph(np.unique(edges.values))
    non_edges = pd.DataFrame(list(nx.non_edges(subgraph)), columns=["source", "target"])
    if n:
        # Randomly select n non-edges
        non_edges = non_edges.iloc[
            np.random.choice(non_edges.shape[0], n, replace=False)
        ]
    else:
        # Randomly select as many non-edges as input edges
        non_edges = non_edges.iloc[
            np.random.choice(non_edges.shape[0], edges.shape[0], replace=False)
        ]
    # The edges does not include known links
    assert not any([(x == npd).all(1).any() for x in np.array(non_edges)])
    return non_edges


def p500(guess_dict, positive_list):
    if len(guess_dict) <= 500:
        correct_guess = [
            (positive_list == (a, b)).all(1).any()
            or (positive_list == (b, a)).all(1).any()
            for (a, b) in guess_dict.keys()
        ]
        return sum(correct_guess) / 500
    else:
        ind_top_500 = np.argsort(list(guess_dict.values()))[::-1][:500]
        edges_top_500 = np.array(list(guess_dict.keys()))[ind_top_500]
        correct_guess = [
            (positive_list == (a, b)).all(1).any()
            or (positive_list == (b, a)).all(1).any()
            for (a, b) in edges_top_500
        ]
        return sum(correct_guess) / 500


# Best model so far:
def edgeModel():
    inp = Input(shape=(500,))
    x = Dense(200, kernel_initializer="lecun_normal", activation="selu")(inp)
    x = AlphaDropout(0.1)(x)
    out = Dense(40)(x)
    model = Model(inp, out)
    return model


def nodesModel():
    inp = Input(shape=(1000,))
    x = Dense(200, kernel_initializer="lecun_normal", activation="selu")(inp)
    x = AlphaDropout(0.1)(x)
    out = Dense(40)(x)
    model = Model(inp, out)
    return model


def train_and_predict(trainset_edges, trainset_non_edges):
    """
    For a list of tuples `trainset_edges` describing known edges
    in a network and a list of tuples `trainset_non_edges`
    describing known non-edges, predict confidence score based
    on edge embedding and neural network classifier.
    Returns a dictionary with the following keys:
    `guesses`:  a dictionary matching edge and confidence score
    for high confidence edges
    `edge embedding`: the Node2Vec edge embeddings
    `node embedding`: the Node2Vec node embeddings
    `nn`: the trained neural network (usable for predictions)
    """

    G = nx.from_pandas_edgelist(trainset_edges)

    # Compute edge embedding
    G_embedding = Node2Vec(G, dimensions=500, walk_length=16, num_walks=500, workers=15)
    model = G_embedding.fit(window=10, min_count=1)
    edges_embs = node2vec.edges.HadamardEmbedder(keyed_vectors=model.wv)
    print(time.asctime(), "- Embedding complete")

    # Define and train model

    # ## Test symmetrical model
    # Inspired from https://stackoverflow.com/questions/56877217/enforcing-symmetry-in-keras and https://github.com/josefondrej/symmetric-layers/blob/master/symnn.py and https://www.pyimagesearch.com/2019/02/04/keras-multiple-inputs-and-mixed-data/

    # Define sets of inputs
    input_edge = Input(shape=(500,))
    input_nodeA = Input(shape=(500,))
    input_nodeB = Input(shape=(500,))

    # Combine node information in both possible order
    inputs_nodeA_first = concatenate([input_nodeA, input_nodeB])
    inputs_nodeB_first = concatenate([input_nodeB, input_nodeA])

    nodes_model = nodesModel()
    model_nodeA_first = nodes_model(inputs_nodeA_first)
    model_nodeB_first = nodes_model(inputs_nodeB_first)

    node_info = Maximum()([model_nodeA_first, model_nodeB_first])
    edge_info = edgeModel()(input_edge)

    all_info = concatenate([node_info, edge_info])

    x = Dense(20, kernel_initializer="lecun_normal", activation="selu")(
        all_info
    )  # 16 also fine
    x = AlphaDropout(0.1)(x)

    outputs = Dense(1, activation="sigmoid")(x)

    nn_model_symm = Model(
        inputs=[input_nodeA, input_nodeB, input_edge], outputs=outputs
    )

    # Prepare feature vectors for training set
    x_train_edge = np.vstack(
        [
            [edges_embs[tuple(e)] for e in trainset_edges.values.astype(str)],
            [edges_embs[tuple(e)] for e in trainset_non_edges.values.astype(str)],
        ]
    )
    x_train_nodeA = np.vstack(
        [
            [model.wv[str(e[0])] for e in trainset_edges.values],
            [model.wv[str(e[0])] for e in trainset_non_edges.values],
        ]
    )
    x_train_nodeB = np.vstack(
        [
            [model.wv[str(e[1])] for e in trainset_edges.values],
            [model.wv[str(e[1])] for e in trainset_non_edges.values],
        ]
    )
    y_train = np.hstack(
        [[1 for e in trainset_edges.values], [0 for e in trainset_non_edges.values]]
    )

    nn_model_symm.compile(
        optimizer="adam", loss="binary_crossentropy", metrics=["binary_accuracy"]
    )

    # Train model on dataset
    nn_model_symm.fit(
        x=[x_train_nodeA, x_train_nodeB, x_train_edge],
        y=y_train,
        batch_size=32,
        epochs=100,
        validation_split=0.2,
        shuffle=True,
    )
    print(time.asctime(), "- Training complete")

    output = dict()
    output["edge embedding"] = edges_embs
    output["node embedding"] = model.wv
    output["nn"] = nn_model_symm

    return output


metrics = ["AUROC", "AUPRC", "P_500", "NDCG", "Computing_time"]
with open(SCORE_FILE_NAME, "w") as f:
    f.write(",".join(metrics))
    f.write("\n")

for i in range(10):
    # Allow to re-run single fold if needed
    np.random.seed(i)

    print(time.asctime(), "- Fold", i)

    score = dict()

    # --- CV partitioning ---

    fold_range = range(i * fold_length, min((i + 1) * fold_length, dataset.shape[0]))

    trainset_edges = dataset.loc[~dataset.index.isin(cv_order[fold_range])]
    trainset_non_edges = get_non_edges(trainset_edges)

    # --- TRAINING ---

    # Monitor time taken to predict edges
    start_time = time.time()

    model = train_and_predict(trainset_edges, trainset_non_edges)

    # --- TESTING ---
    # Iterator for test set:
    # All node pairs (existing or not, including self-edges) that are not part of the training set
    G = nx.from_pandas_edgelist(trainset_edges)
    nonG = nx.from_pandas_edgelist(trainset_non_edges)
    edge_iter = iter(set((x,y) for x in nx.nodes(full_graph) for y in nx.nodes(
        full_graph) if (x,y) not in nx.edges(nonG) and (x,y) not in nx.edges(G) and x <= y))

    # Predict all edges and store values
    guess_dict = dict()

    while 1:
        edge_set = list(islice(edge_iter, 500000))
        if not edge_set:
            # We processed all edges
            break
        training_nodes = np.unique(trainset_edges.values)
        x_test_edge = np.array(
            [
                model["edge embedding"][tuple(str(x) for x in e)]
                if e[0] in training_nodes and e[1] in training_nodes
                else np.zeros(500)
                for e in edge_set
            ]
        )
        x_test_nodeA = np.array(
            [
                model["node embedding"][str(e[0])]
                if e[0] in training_nodes
                else np.zeros(500)
                for e in edge_set
            ]
        )
        x_test_nodeB = np.array(
            [
                model["node embedding"][str(e[1])]
                if e[1] in training_nodes
                else np.zeros(500)
                for e in edge_set
            ]
        )
        edge_preds = model["nn"].predict(
            [x_test_nodeA, x_test_nodeB, x_test_edge]
        )
        for (e, v) in zip(edge_set, edge_preds):
            guess_dict[e] = v[0]
        gc.collect()

    print(time.asctime(), "- Prediction complete")

    y_pred_scores = list(guess_dict.values())

    y_label = [x in nx.edges(full_graph) for x in guess_dict.keys()]

    # Total runtime
    score["Computing_time"] = str(time.time() - start_time)

    # AUROC
    score["AUROC"] = str(roc_auc_score(y_label, y_pred_scores))

    # AUPRC
    score["AUPRC"] = str(average_precision_score(y_label, y_pred_scores))

    # P(500)
    score["P_500"] = str(p500(guess_dict, npd))

    # nDCG
    score["NDCG"] = str(
        ndcg_score(
            np.reshape(y_label, np.atleast_2d(y_pred_scores).shape),
            np.atleast_2d(y_pred_scores),
        )
    )

    with open(SCORE_FILE_NAME, "a") as f:
        f.write(",".join([score[x] for x in metrics]))
        f.write("\n")
