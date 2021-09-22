"""RW2 Machine."""
from gensim.models import Word2Vec
import ray
import numpy as np
import random
import copy
import pandas as pd


class ParallelRW2:
    """
    RW2 model class.
    """
    def __init__(self, graph):
        """
        RW2 machine constructor.
        """
        self.graph = graph
        self.embedding = None
        self.walks = None
        self.sampler = None
        self.model = None
        self.attr_name = 'attributes'
        self.edge_weight_name ='weight'

    def do_walks(self, p, q, walk_number, walk_length, workers, is_directed = False, feat_weights_map = None, a2n = None):
        """
        Doing attributed second order random walks.
        """

        self.preprocess_transition_probs(p, q, feat_weights_map, is_directed)

        labels = list(self.graph.nodes()) if a2n is None else list(a2n.keys())
        #splits = np.array_split(np.array(labels), workers) splits[i].tolist()
        splits = self._assign_walk(walk_number, workers)

        rgraph, ral_node, ral_edges = ray.put(self.graph), ray.put(self.alias_nodes), ray.put(self.alias_edges)
        ral_f, ra2n = ray.put(self.alias_features), ray.put(a2n)

        print(splits)
        rwalks = ray.get([simulate_walks.remote(rgraph,  ral_node, ral_edges, ral_f, self.attr_name, ra2n, splits[i], walk_length, labels) for i in range(workers)])

        self.walks = []
        for rwalk_split in rwalks:
            for wk in rwalk_split:
                self.walks.append(list(map(str, wk)))

        print(len(self.walks))

    def _assign_walk(self, N, workers):
        array = np.zeros(workers, dtype = int)
        for i in range(workers):
            array[i] = N // workers  # integer division

        # divide up the remainder
        for i in range(N % workers):
            array[i] += 1
        return array

    def preprocess_transition_probs(self, p,q, feat_vector, is_directed):
        """
        Preprocessing of transition probabilities for guiding the random walks.
        """

        G = self.graph#!! check

        alias_nodes = {}
        alias_features = {}
        for node in G.nodes():
            alias_nodes[node] = get_alias(
                [G[node][nbr][self.edge_weight_name] for nbr in sorted(G.neighbors(node))])  # sorted(G.neighbors(node))
            if feat_vector is not None:
                alias_features[node] = get_alias(
                    [feat_vector[node][feat] for feat in sorted(G.nodes[node][self.attr_name])])

        alias_edges = {}
        if is_directed:
            for edge in G.edges(): alias_edges[edge] = get_alias_edge(G, p, q, self.edge_weight_name,edge[0], edge[1])
        else:
            for edge in G.edges():
                alias_edges[edge] = get_alias_edge(G, p, q, self.edge_weight_name, edge[0], edge[1])
                alias_edges[(edge[1], edge[0])] = get_alias_edge(G, p, q, self.edge_weight_name, edge[1], edge[0])

        self.alias_nodes = alias_nodes
        self.alias_edges = alias_edges
        self.alias_features = alias_features


    def learn_embedding(self, dimension, window, workers, epochs, words_mapping = None, min_count = 0, sg = 1):  # compute_loss = True):
        """
        Fitting an embedding.
        """
        self.model = Word2Vec(self.walks, size = dimension, window = window, min_count = min_count, sg = sg, workers = workers,
                         iter = epochs)  # , compute_loss = compute_loss)

        maps = None if words_mapping is None else to_reverse(words_mapping)
        func_map = lambda x: x if words_mapping is None else maps[int(x)]
        words = list(self.model.wv.vocab.keys())
        embedding = {func_map(word): self.model.wv[word] for word in words}   # notice that the labels are encoded in integers
        self.embedding = embedding

    def save_embedding(self, output: str):
        """
        Function to save the embedding.
        """
        w2vsave = True
        if w2vsave:
            self.save_model(self.model, output)
        else:
            save_embedding_utils(self.embedding, output+ ".csv")

    def save_model(self, model, output: str):
        pat = output +'.w2v'
        model.wv.save_word2vec_format(pat)

@ray.remote
def simulate_walks(G,  alias_nodes, alias_edges, alias_features, attrname, a2n, num_walks, walk_length, split):
    walks = []
    splitcopy = copy.deepcopy(split)
    print(True if a2n is None else False)
    for walk_iter in range(num_walks):
        random.shuffle(splitcopy)

        for i, label in enumerate(splitcopy):  # to parellize
            start_node = np.random.choice(list(a2n[label])) if a2n is not None else label
            start_label = label if a2n is not None else None
            walks.append(node2vec_walk(G, alias_nodes, alias_edges, alias_features, walk_length, attrname, start_node = start_node, start_label = start_label))

    return walks


def node2vec_walk(G, alias_nodes, alias_edges, alias_features, walk_length, attrname, start_node, start_label = None):
    walk = []
    graphwalk = [start_node]

    if start_label is None:
        walk.append(start_node)
    else:
        walk.append(start_label)

    while len(walk) < walk_length:
        node_cur = graphwalk[-1]

        cur_nbrs = sorted(G.neighbors(node_cur))
        if cur_nbrs:
            if len(walk) == 1:
                next_node = sample_next_node(cur_nbrs, alias_nodes[node_cur])
            else:
                node_prev = graphwalk.pop(-2)  # graphwalk[-2] #keeps graphwalk with length 2
                next_node = sample_next_node(cur_nbrs, alias_edges[(node_prev, node_cur)])

            if start_label is None:
                walk.append(next_node)
            else:
                walk.append(sample_next_element(attrname, G, next_node, alias_features))

            graphwalk.append(next_node)
        else:
            break

    return walk

def to_reverse(words_mapping):
    int2atrs = {}
    for k,v in words_mapping.items():
       int2atrs[v] = k
    return int2atrs

def get_alias_edge(G, p, q, edgeweigth, src, dst):
    """
    Get the alias edge setup lists for a given edge.
    P and Q usage in sampling.
    :param src: source node id
    :param dst: destination node id
    :return: alias edge
    """
    unnormalized_probs = []
    for dst_nbr in sorted(G.neighbors(dst)):
        if dst_nbr == src:
            unnormalized_probs.append(G[dst][dst_nbr][edgeweigth] / p)
        elif G.has_edge(dst_nbr, src):
            unnormalized_probs.append(G[dst][dst_nbr][edgeweigth])
        else:
            unnormalized_probs.append(G[dst][dst_nbr][edgeweigth] / q)

    return get_alias(unnormalized_probs)

def sample_next_node(elements, vector):
    """
    Sample a single item from a population of elements considering the vector of the pre-computed probabilities.
    Both the vectors need to be sorted, i guess.
    :param elements: elements to sample
    :type elements: list
    :param vector: probabilities assigned to the elements
    :return: the drawn element
    """
    return elements[alias_draw(vector[0], vector[1])]

def sample_next_element(attrname, graph, node, vector):
    """
    Sample a single item from a population of elements considering the vector of the pre-computed probabilities.
    Both the vectors have need to be sorted, i guess. This method is designed for the features sampling.
    If the sample vector is not set returns the node.
    :param graph: the graph
    :param node: a node id
    :param vector: probabilities assigned to the elements
    :type vector: list
    :return: the drawn element
    """
    if not vector:
        return node
    else:
        nodeattrs = sorted(graph.nodes[node][attrname])  # nodeattrs = sorted(graph.node[node][self.attrname])
        return sample_next_node(nodeattrs, vector[node])


def get_alias(unnormalized_probs):
    """
    Generate the alias of the probabilities.
    :param unnormalized_probs: vector of unnormalized probability
    :type unnormalized_probs: list
    :return: two utility lists for non-uniform sampling from discrete distributions
    """
    normalized_probs = get_normalized_probabilities(unnormalized_probs)
    return alias_setup(normalized_probs)

def get_normalized_probabilities(unnormalized_probs):
    """
    Normalize the probabilities in the vector.
    :param unnormalized_probs: vector of unnormalized probabilities
    :type unnormalized_probs: list
    :return: a vector of normalized probabilities
    """
    norm_const = sum(unnormalized_probs)
    normalized_probs = [float(u_prob) / norm_const for u_prob in unnormalized_probs]
    return normalized_probs


def alias_setup(probs):
    """
    Compute utility lists for non-uniform sampling from discrete distributions.
    Refer to https://hips.seas.harvard.edu/blog/2013/03/03/the-alias-method-efficient-sampling-with-many-discrete-outcomes/
    for details
    :param probs: vector of normalized probabilities
    :return: two utility lists for non-uniform sampling from discrete distributions
    """
    K = len(probs)
    q = np.zeros(K)
    J = np.zeros(K, dtype=np.int)

    smaller = []
    larger = []
    for kk, prob in enumerate(probs):
        q[kk] = K*prob

        if q[kk] < 1.0: smaller.append(kk)
        else: larger.append(kk)

    while len(smaller) > 0 and len(larger) > 0:
        small = smaller.pop()
        large = larger.pop()

        J[small] = large
        q[large] = q[large] + q[small] - 1.0

        if q[large] < 1.0: smaller.append(large)
        else: larger.append(large)

    return J, q


def alias_draw(J, q):
    """
    Draw sample from a non-uniform discrete distribution using alias sampling.
    :param J: utility list from non-uniform sampling from discrete distribution
    :type J: list
    :param q: utility list from non-uniform sampling from discrete distribution
    :type q: list
    :return: the drawn alias
    """
    K = len(J)
    kk = int(np.floor(np.random.rand()*K))

    if np.random.rand() < q[kk]: return kk
    else: return J[kk]

def save_embedding_utils(embeddings, output: str):
    """
    Function to save the embedding.
    """
    word_list = list(embeddings.keys())
    embeddings = np.array([embeddings[word] for word in word_list])

    ids = np.array([word for word in word_list]).reshape(-1, 1)
    columns = ["id"] + ["x_" + str(x) for x in range(embeddings.shape[1])]
    embedding = pd.DataFrame(np.concatenate([ids, embeddings], axis = 1), columns = columns)
    embedding = embedding.sort_values(by = ['id'])
    embedding.to_csv(output, index = None)