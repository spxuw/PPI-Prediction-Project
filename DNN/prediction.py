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
DATASET_FILE = "/scratch-cbe/users/loan.vulliard/edge-prediction/huri.csv"
SCORE_FILE_NAME = (
    "./scores/total_preds_"
    + str(round(time.time()))
    + "_approach8_NN_N2V_NodeAndEdge.huri.csv"
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


def train(trainset_edges, trainset_non_edges):
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


metrics = ["source", "target", "score"]
with open(SCORE_FILE_NAME, "w") as f:
    f.write(",".join(metrics))
    f.write("\n")


print(time.asctime(), "- Start novel edge prediction")

trainset_edges = dataset
trainset_non_edges = get_non_edges(trainset_edges)

# --- TRAINING ---
model = train(trainset_edges, trainset_non_edges)

# --- PREDICTION ---

# Predict all edges and store high values
guess_dict = dict()
edge_iter = nx.non_edges(full_graph)
# Only used for printing progress
counter = 0
nb_candidates = (
    (full_graph.number_of_nodes() * (full_graph.number_of_nodes() - 1)) / 2
) - full_graph.number_of_edges()
while 1:
    edge_set = list(islice(edge_iter, 500000))
    counter += 500000
    print(time.asctime(), "- Processing candidate edges", counter, "/", nb_candidates)
    if not edge_set:
        # We processed all edges
        break
    edge_set_edge = np.vstack(
        [model["edge embedding"][tuple(str(x) for x in e)] for e in edge_set]
    )
    edge_set_nodeA = np.vstack([model["node embedding"][str(e[0])] for e in edge_set])
    edge_set_nodeB = np.vstack([model["node embedding"][str(e[0])] for e in edge_set])
    edge_preds = model["nn"].predict([edge_set_nodeA, edge_set_nodeB, edge_set_edge])
    for (e, v) in zip(edge_set, edge_preds):
        if v >= 1:  # Only keep track of the strongest hits
            guess_dict[e] = v[0]
    gc.collect()
print(time.asctime(), "- Prediction complete")

# No self-edge was considered
assert not any([a == b for (a, b) in guess_dict.keys()])

# Ensure there is no duplicate
assert all([(b, a) not in guess_dict.keys() for (a, b) in guess_dict.keys()])

# Get 500 of top predictions
ind_top_500 = np.random.choice(len(guess_dict.keys()), 500, replace = False)
edges_top_500 = np.array(list(guess_dict.keys()))[ind_top_500]

# Output top predictions
with open(SCORE_FILE_NAME, "a") as f:
    for i in range(500):
        a, b = edges_top_500[i, :]
        c = guess_dict[(a, b)]
        f.write(",".join([a, b, str(c)]))
        f.write("\n")
