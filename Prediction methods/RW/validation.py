from sklearn.linear_model import LogisticRegression
import numpy as np
from node2vec.edges import HadamardEmbedder
import time
from utils import evaluate_model, get_mask, save, load_graph, read_graph_rw2, train_rw2, k_largest_index_argsort, save_top
from sklearn.model_selection import KFold
from random import shuffle
import networkx as nx
import random
import pickle
import ray
from params import parameter_parser
ray.init()

def run_supervised(no_folds, filename):
    args = parameter_parser()
    workers = args.workers
    graph, mapping = load_graph(args.edgelist)
    print(graph.number_of_edges())
    nodes = list(graph.nodes())
    mapping2 = {str(i): k for k, i in list(mapping.items())}

    pos_edges = [e for e in graph.edges]
    shuffle(pos_edges)

    with open(args.content, 'rb') as handle:
        node_labels = pickle.load(handle)

    kf = KFold(n_splits = no_folds)
    avg_values_vec = []
    n_run = 0

    for gtrain, test in kf.split(pos_edges):
        n_run += 1
        time_start = time.time()
        train_pos_edges = [pos_edges[i] for i in gtrain]
        model, n2w_model, train_edges = fit_model(args, train_pos_edges, n_run, mapping2)

        test_pos_edges = [pos_edges[i] for i in test]
        pred_matrix = supervised_test_by_matrix(n2w_model, model, train_edges, nodes, mapping, mapping2, node_labels, workers)
        values_vec = evaluate_model(test_pos_edges, pred_matrix, len(nodes), time_start)
        print(values_vec)
        avg_values_vec.append(values_vec)

    save(no_folds, avg_values_vec, filename)

    model, n2w_model, train_edges = fit_model(args, pos_edges, no_folds+1, mapping2)
    pred_matrix = supervised_test_by_matrix(n2w_model, model, train_edges, nodes, mapping, mapping2, node_labels, workers)
    topkpreds(pred_matrix, nodes, mapping2)



def fit_model(args, train_pos_edges, n_run, mapping2):
    n2w_model = get_embeddings(args, train_pos_edges, args.content, n_run, mapping2)
    train_neg_edges = generate__negative_random_balanced_interactions(train_pos_edges)
    x_train, y_train = preprocessing(n2w_model, train_pos_edges, train_neg_edges, mapping2)

    model = LogisticRegression().fit(x_train, y_train)
    train_edges = train_pos_edges + train_neg_edges
    return model, n2w_model, train_edges


@ray.remote
def par_preds(dimension, indices, model, edges_embs, mapping):
    to_preds = np.zeros((len(indices), dimension))

    for ids, x in enumerate(indices):
        i, j = x
        to_preds[ids, :] = edges_embs[(mapping[str(i)], mapping[str(j)])]  # check
    return model.predict_proba(to_preds)


def supervised_test_by_matrix(embeddings, model, train_edges, nodes, mapping, mapping2, node_labels, workers):
    induce_embeddings(embeddings, nodes, node_labels, mapping2)
    vocabulary = {str(mapping[k]) for k in list(set(embeddings.wv.vocab.keys())) if k in mapping}
    edges_embs = HadamardEmbedder(keyed_vectors = embeddings.wv)

    N = len(nodes)

    mask_train, pred_matrix = np.zeros((N, N)), np.zeros((N, N))
    get_mask(mask_train, train_edges, 1.)

    isolated = set()
    indexes_to_pred = list()
    nnn = 0
    for ind, i in enumerate(nodes):
        si = str(i)
        if si not in vocabulary:
            isolated.add(si)
            continue

        for j in nodes[ind+1:]:
            sj = str(j)
            if sj not in vocabulary:
                isolated.add(sj)
                continue
            if mask_train[i, j] == 1.:
                continue

            indexes_to_pred.append((i, j))
            nnn += 1

    for i in enumerate(nodes):
        si = str(i)
        if si not in vocabulary or mask_train[i, i] == 1.: continue
        indexes_to_pred.append((i, i))
        nnn += 1

    dimension = len(edges_embs[(mapping2[str(indexes_to_pred[0][0])], mapping2[str(indexes_to_pred[0][1])])])
    load = np.array_split(np.array(indexes_to_pred), workers)
    rwalks = ray.get([par_preds.remote(dimension, load[i], model, edges_embs, mapping2) for i in range(workers)])

    for i in range(workers):
        preds = rwalks[i]
        for ids, x in enumerate(load[i]):
            i, j = x
            pred_matrix[i, j] = preds[ids, 1]
            if i == j: continue
            pred_matrix[j, i] = preds[ids, 1]


    return pred_matrix


def generate__negative_random_balanced_interactions(positive_interactions):
    H = nx.Graph()
    H.add_edges_from(positive_interactions)
    set_of_proteins = set(H.nodes())
    protein_2_degree = dict(H.degree)
    balanced_negative_interactions = set()

    for protein in set_of_proteins:
        neg_interactions_retrieved = 0
        while neg_interactions_retrieved < protein_2_degree[protein]/2:
            second_protein = random.sample(set_of_proteins, 1)[0]
            ordered_interaction = (protein, second_protein) if protein < second_protein else (second_protein, protein)
            if ordered_interaction not in positive_interactions and ordered_interaction not in balanced_negative_interactions:
                balanced_negative_interactions.add(ordered_interaction)
                neg_interactions_retrieved += 1
    return list(balanced_negative_interactions)


def get_preds(dimension, indices, model, pred_matrix, edges_embs, mapping):
    to_preds = np.zeros((len(indices), dimension))
    print(to_preds.shape)

    for ids, x in enumerate(indices):
        i, j = x
        to_preds[ids, :] = edges_embs[(mapping[str(i)], mapping[str(j)])]#check
    preds = model.predict_proba(to_preds)

    for ids, x in enumerate(indices):
        i, j = x
        pred_matrix[i, j] = preds[ids, 1]
        if i == j: continue
        pred_matrix[j, i] = preds[ids, 1]


def get_embeddings(args, edgelist, node_labels, norun, mapping2):
    edgelist_clean = [(mapping2[str(x[0])], mapping2[str(x[1])]) for x in edgelist]
    g, words_mapping, feat_transitions, attributes2nodes = read_graph_rw2(edgelist_clean, node_labels)

    return train_rw2(args, g, words_mapping, feat_transitions, attributes2nodes)


def preprocessing(embeddings, pos_edges, neg_edges, mapping):
    edges_embs = HadamardEmbedder(keyed_vectors = embeddings.wv)
    pos_feat = [(edges_embs[(mapping[str(edge[0])], mapping[str(edge[1])])], 1.) for edge in pos_edges]
    neg_feat = [(edges_embs[(mapping[str(edge[0])], mapping[str(edge[1])])], 0.) for edge in neg_edges]
    pos_feat.extend(neg_feat)
    shuffle(pos_feat)
    x = np.array([edge[0] for edge in pos_feat])
    y = np.array([edge[1] for edge in pos_feat])
    print(x.shape, y.shape)
    return x, y


def induce_embeddings(model, nodes, node_labels, int2node):
    vocabulary = set(model.wv.vocab.keys())
    mpnodes = list(map(lambda x: int2node[str(x)], nodes))
    newnodes = list(set(mpnodes).difference(vocabulary))

    for node in newnodes:
        atrs = set(node_labels[node]).intersection(vocabulary)
        if len(atrs) == 0: continue
        model.wv[node] = np.mean(list(map(lambda x: model.wv[x], atrs)), axis=0)

def topkpreds(pred_matrix, nodes, mapping, k = 500):
    idx2node = {i:node for i, node in enumerate(nodes)}
    index = k_largest_index_argsort(pred_matrix, k)
    tops = [(mapping[str(idx2node[n1])], mapping[str(idx2node[n2])], pred_matrix[n1, n2]) for n1, n2 in index]
    save_top(tops)




run_supervised(10, 'RealPPI_Metrics_attr_induced.csv')
