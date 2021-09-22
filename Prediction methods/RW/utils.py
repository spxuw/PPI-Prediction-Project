import time
import numpy as np
from sklearn.metrics import roc_auc_score, average_precision_score, ndcg_score
import networkx as nx
import csv
from rw2 import ParallelRW2 as RW2
import pickle
import itertools


def load_graph(path):
    H = nx.read_edgelist(path)
    temp = sorted(H.nodes())
    mapping = {k: i for i, k in enumerate(temp)}
    return nx.relabel_nodes(H, mapping), mapping


def get_nodes(train_pos_edges):
    subset_nodes = set()
    for v1, v2 in train_pos_edges:
        subset_nodes.add(v1)
        subset_nodes.add(v2)
    return list(subset_nodes)

def read_graph_rw2(edgelist, node2attr_path):
    words_mapping = None
    encode = False
    attributed = True
    nodes = get_nodes(edgelist)

    with open(node2attr_path, 'rb') as handle:
        node_labels = pickle.load(handle)

    g, attributes2nodes, feat_transitions, node2int, attrs2int = load_graph_rw2(edgelist, nodes = nodes, node2attributes = node_labels, encode2int = encode, directed = False, placeholder = None, fill_empty = False)
    if encode:
        words_mapping = attrs2int if attributed else node2int

    return g, words_mapping, feat_transitions, attributes2nodes

def get_mask(matrix, indices, value):
    for v1, v2 in indices:
         matrix[v1, v2] = value
         matrix[v2, v1] = value

def save(no_folds, avg_values, name):
    with open(name, 'w', newline = '') as file:
        writer = csv.writer(file)
        writer.writerow(["AUROC", "AUPRC", "P_500", "NDCG", "Computing_time"])
        for i in range(no_folds):
            writer.writerow([avg_values[i]["auroc"], avg_values[i]["auprc"], avg_values[i]["pr_at_500"], avg_values[i]["ndcg"], avg_values[i]["comp_time"]])

def save_top(tops, path = 'top500.csv'):
    with open(path, 'w', newline = '') as file:
        writer = csv.writer(file)
        writer.writerow(["source", "target", "score"])
        for t, s, score in tops:
            writer.writerow([t, s, score])


def precision_at_k(y_true, y_score, k=10):
    """Precision at rank k
    Parameters
    ----------
    y_true : array-like, shape = [n_samples]
        Ground truth (true relevance labels).
    y_score : array-like, shape = [n_samples]
        Predicted scores.
    k : int
        Rank.
    Returns
    -------
    precision @k : float
    """
    unique_y = np.unique(y_true)

    if len(unique_y) > 2:
        raise ValueError("Only supported for two relevance levels.")

    pos_label = unique_y[1]
    n_pos = np.sum(y_true == pos_label)

    order = np.argsort(y_score)[::-1]
    y_true = np.take(y_true, order[:k])
    n_relevant = np.sum(y_true == pos_label)

    # Divide by min(n_pos, k) such that the best achievable score is always 1.0.
    return float(n_relevant) / min(n_pos, k)

def evaluate_model(test_pos_edges, pred_matrix, N, time_start):
    true_matrix = np.zeros((N, N))
    get_mask(true_matrix, test_pos_edges, 1.)
    true_scores, pred_scores = true_matrix[np.triu_indices(N)], pred_matrix[np.triu_indices(N)]

    auroc = roc_auc_score(true_scores, pred_scores)
    auprc = average_precision_score(true_scores, pred_scores)
    ndcg = ndcg_score(np.expand_dims(true_scores, axis=0), np.expand_dims(pred_scores, axis=0))
    pr_at_5000 = precision_at_k(true_scores, pred_scores, k = 500)
    comp_time = time.time() - time_start
    return {'auroc': auroc, 'auprc': auprc, 'ndcg': ndcg, 'pr_at_500': pr_at_5000, 'comp_time': comp_time}


def retrieve_domine_interaction(file):
    interactions_set = set()
    with open(file, "r", encoding="utf-8") as domain_interactions:
        for line in domain_interactions.readlines():
            splitted_line = line.split("|")
            interactions_set.add((splitted_line[0], splitted_line[1]))
    return interactions_set


def retrieve_3did_interaction(file):
    interactions_set = set()
    with open(file, "r", encoding="utf-8") as did_interactions:
        for line in did_interactions.readlines():
            if line.startswith("#=ID"):
                splitted_line = line.split("\t")
                first_pfam = splitted_line[3].replace(" (", "")
                first_pfam = first_pfam[0:first_pfam.index(".")]
                second_pfam = splitted_line[4].replace("\n", "")
                second_pfam = second_pfam[0:second_pfam.index(".")]
                print(first_pfam, second_pfam)
                interactions_set.add((first_pfam, second_pfam))
    return interactions_set


def k_largest_index_argsort(a, k):
    idx = np.argsort(a.ravel())[:-k-1:-1]
    return np.column_stack(np.unravel_index(idx, a.shape))


def train_rw2(args, g, words_mapping, feat_transitions, attributes2nodes):
    """
    RW2 model fitting.
    :param args: Arguments object.
    """
    model = RW2(g)
    model.do_walks(p = args.P, q = args.Q, walk_number = args.walk_number, walk_length = args.walk_length, workers = args.workers, is_directed = args.directed, feat_weights_map = feat_transitions, a2n = attributes2nodes)

    model.learn_embedding(words_mapping = words_mapping, dimension = args.dimensions, window = args.window_size, workers = args.workers, epochs = args.epochs, min_count = args.min_count, sg = args.sg)  # , compute_loss = args.compute_loss)
    #model.save_embedding(args.output)
    return model.model


def reverse_it(mapping):
    maps = {}
    for k, values in mapping.items():
        for v in values:
            if v not in maps: maps[v] = []
            maps[v].append(k)
    return maps

def encode_it(edgelist, nodelist, node2attributes = None):
    """
    Encode nodes and attributes into integers.
    """
    assert(len(edgelist[0]) >= 2)
    enc_node2attributes, attrs2int = None, None

    #edgelist_no_weight = edgelist if len(edgelist[0]) == 2 else list(map(lambda x: [x[0], x[1]], edgelist))
    nodes = sorted(set(nodelist))#sorted(set(list(itertools.chain(*edgelist_no_weight))))
    #print(len(nodes),nodes)
    node2int = {node: i for i, node in enumerate(nodes)}
    enc_edgelist = list(map(lambda x: tuple([node2int[x[0]], node2int[x[1]]] + list(x[2:])), edgelist))

    if node2attributes is not None:
        attributes = sorted(set(list(itertools.chain(*node2attributes.values()))))
        attrs2int = {atrs: i for i, atrs in enumerate(attributes)}
        cast_atrs2int = lambda x: attrs2int[x]
        enc_node2attributes = dict(list(map(lambda x: (node2int[x[0]], list(map(cast_atrs2int, x[1]))), node2attributes.items())))

    return enc_edgelist, node2int, enc_node2attributes, attrs2int

def load_graph_rw2(edgelist, nodes, node2attributes = None, encode2int = True, weighted = False, directed = False, placeholder = None, fill_empty = False):
    """
    Graph loader.
    """
    node2int, attrs2int, attributes2nodes, feat_transitions = None, None, None, None
    edgel, node2attrs = edgelist, node2attributes



    if weighted:
        g = nx.DiGraph(edgel, data = (('weight', float),))
        if encode2int:

            g.add_nodes_from(list(itertools.chain(node2int.values())))
        else: g.add_nodes_from(nodes)
    else:
        g = nx.DiGraph(edgel)
        if encode2int:
            g.add_nodes_from(list(itertools.chain(list(node2int.values()))))
        else:
            g.add_nodes_from(nodes)
        for edge in g.edges(): g[edge[0]][edge[1]]['weight'] = 1

    if not directed:
        g = g.to_undirected()

    g.remove_edges_from(nx.selfloop_edges(g))

    if node2attrs is not None:
        #print(len(node2attrs), len(nodes))
        dif = set(g.nodes) - set(node2attrs.keys())
        #print(list(dif)[:10])
        if len(dif) > 0:
            if placeholder is not None:
                if encode2int:
                    tph = sorted(attrs2int.values())[-1] + 1
                    attrs2int[placeholder] = tph
                    placeholder = tph

                for node in dif: node2attrs[node] = []
                if fill_empty:
                    for node in dif:
                        node2attrs[node].append(placeholder)

                else:
                    for node in node2attrs.keys(): node2attrs[node].append(placeholder)

            else:
                print(len(dif))
                assert(False)

        dif = list(set(node2attrs.keys()) - set(g.nodes))
        for torem in dif: del node2attrs[torem]

        nx.set_node_attributes(g, node2attrs, 'attributes')


        feat_transitions = {node: {atr: 1 for atr in node2attrs[node]} for node in list(g.nodes)}
        attributes2nodes = reverse_it(node2attrs)

        print("# attributes", len(attributes2nodes.keys()))

    return g, attributes2nodes, feat_transitions, node2int, attrs2int

