import sys
import os
import pandas as pd
import numpy as np
import networkx as nx
import random
from sklearn.model_selection import KFold
import time
from itertools import chain
from sklearn.model_selection import train_test_split
import warnings
warnings.filterwarnings('ignore')

# set random seed for random.sample in BFS
random.seed(42)

computing_time = []
computing_time_n90 = np.zeros(10)
computing_time_ncustom = np.zeros(10)

network_name = sys.argv[1]
network_file = sys.argv[2]
output_dir = sys.argv[3]

# save global id dictionary
occurred_dict = {}


def id_converter(edgelist):
    index = 0
    # convert first column into integer
    for i in range(len(edgelist)):
        key = edgelist.iloc[i, 0]
        if key in occurred_dict:
            edgelist.iloc[i, 0] = occurred_dict[key]
        else:
            index = index + 1
            edgelist.iloc[i, 0] = index
            occurred_dict[key] = index
    # convert second column into integer
    for j in range(len(edgelist)):
        key = edgelist.iloc[j, 1]
        if key in occurred_dict:
            edgelist.iloc[j, 1] = occurred_dict[key]
        else:
            index = index + 1
            edgelist.iloc[j, 1] = i + index
            occurred_dict[key] = i + index

    return edgelist


def bfs(size, graph):
    # save network nodes list in a variable
    nodes = list(graph.nodes(data=True))
    # initialize a list which will be filled with appropriate sized modules
    module_list = []
    # loop through all nodes
    for i in nodes:
        tree_level = 0
        # initialize variable to save nodes found at the current tree level
        # until it reaches the appropriate size
        current_list = []
        # loop while the current module has the given amount of nodes
        while len(current_list) < size:
            # nodes found at the current tree level
            current_nodes = list(nx.descendants_at_distance(graph, source=i[0], distance=tree_level))
            tree_level += 1
            # to avoid repeat of nodes at the current tree level compare nodes
            # at the current level and the nodes in current sized module
            overlap = set(current_list) & set(current_nodes)
            if len(overlap) == len(current_nodes):
                break
            else:
                if len(current_list)+len(current_nodes) < size:
                    current_list = current_list+current_nodes
                else:
                    n = size-len(current_list)
                    sub_nodes = random.sample(current_nodes, n)
                    current_list = current_list+sub_nodes
                    # sort current module to be able to remove duplicated ones
                    # from  whole modules list later
                    current_list.sort()
                    module_list.append(current_list)

    # remove duplicated modules
    dup_free = []
    dup_free_set = set()

    for x in module_list:
        if tuple(x) not in dup_free_set:
            dup_free.append(x)
            dup_free_set.add(tuple(x))

    # network node coverage percentage
    coverage = (len(list(set(chain(*dup_free))))/len(nodes))*100
    # add module id column
    module_id = range(1, len(dup_free)+1)
    modules = pd.DataFrame(dup_free)
    modules.insert(0, "", module_id)

    return [modules, coverage]


def analyse(train, test, kf_count, current_size, graph):
    test_df = pd.DataFrame(test, columns=['source', 'target'])
    # test_df.to_csv(output_dir+str(network_name)+str(current_size)+'test_kf_'+ \
    #     str(kf_count)+'_'+'.edgelist', index=False, sep=" ", header=False)

    train_df = pd.DataFrame(train, columns=['source', 'target'])
    train_df.to_csv(output_dir+str(network_name)+str(current_size)+ \
        'train_kf_'+str(kf_count)+'.edgelist', index=False, sep=" ", header=False)

    if (current_size == '_n81_'):
        # remove test edges to avoid lagging nodes in a module
        graph.remove_edges_from(test)

    bfs_output = bfs(36, graph)
    modules = bfs_output[0]
    modules.to_csv(output_dir+str(network_name)+str(current_size)+ \
        'modules_kf_'+str(kf_count)+'.csv', index=False, sep='\t', header=False)

    filenames.append(os.path.basename(output_dir+str(network_name)+ \
        str(current_size)+'train_kf_'+str(kf_count)+ \
        '.edgelist'))

    if (current_size == '_n81_'):
        net_coverage_ncustom.append(bfs_output[1])
    else:
        net_coverage_n90.append(bfs_output[1])

    # time complexity
    computing_time.append(time.time() - start_time)

    return [net_coverage_ncustom, net_coverage_n90]


# import the edge list
Edgelist = pd.read_csv(network_file)

# edit network name for output file names
network_name = network_name.replace('. ', '_')

# save id conversion status
convert = False
# if edge list elements are integer skip the conversion
# else convert node names into integers
if Edgelist.dtypes[0] != 'int':
    # change conversion status
    convert = True
    Edgelist = id_converter(Edgelist)

n100_df = pd.DataFrame(Edgelist, columns=['source', 'target'])
n100_df.to_csv(output_dir+str(network_name)+'_n100'+'.edgelist', \
    index=False, sep=" ", header=False)

int_node_ids = (n100_df['source'].append(n100_df['target'])).unique().tolist()
# check whether current dataset is converted or the original was already int
if convert:
    # get original node ids from dictionary
    key_list = list(occurred_dict.keys())
    value_list = list(occurred_dict.values())
    str_id = []
    for node in int_node_ids:
        if node in value_list:
            node_pos = value_list.index(node)
            str_id.append(key_list[node_pos])
        else:
            str_id.append('-')
            print("Error: Name conversion is not appropriate")

    id_dict = {'int_node_id':int_node_ids, 'str_node_id':str_id}
    id_dict_df = pd.DataFrame(id_dict)
    id_dict_df.to_csv(output_dir+str(network_name)+'_edgelist_conv_id'+'.csv', \
        index=False, sep='\t', header=True)

# construct the graph
G = nx.from_pandas_edgelist(Edgelist, source='source', target='target')
kf = KFold(n_splits=10, random_state=42, shuffle=True)
X = list(G.edges())
kf.get_n_splits(X)

n100_bfs = bfs(36, G)
n100_modules = n100_bfs[0]
n100_modules.to_csv(output_dir+str(network_name)+'_n100_modules.csv', \
    index=False, sep='\t', header=False)
n100_coverage = n100_bfs[1]
with open(output_dir+str(network_name)+'_n100_coverage.csv', 'w') as n100_coverage_file:
    n100_coverage_file.write(str(n100_coverage))



net_coverage_n90 = []
net_coverage_ncustom = []
filenames = []

# trace kfold status
kf_count = 1
for train_index, test_index in kf.split(X):
    start_time = time.time()
    # construct the graph
    G = nx.from_pandas_edgelist(Edgelist, source='source', target='target')
    # split data into train and test sets
    train = [X[i] for i in train_index]
    test = [X[i] for i in test_index]
    # remove test edges to avoid lagging nodes in a module
    G.remove_edges_from(test)
    # split n90 train data into train and test sets for gan
    train_custom, test_custom = train_test_split(train, test_size=0.1, random_state=42, shuffle=True)

    n90 = analyse(train, test, kf_count, '_n90_', G)
    ncustom = analyse(train_custom, test_custom, kf_count, '_n81_', G)

    kf_count += 1

net_coverage_ncustom = ncustom[0]
net_coverage_n90 = n90[1]

net_coverage_n90_df = pd.DataFrame(net_coverage_n90, columns=['coverage'])
net_coverage_n90_df.to_csv(output_dir+str(network_name)+'_n90_coverage_'+'.csv', \
    index=False, sep='\t')

net_coverage_n81_df = pd.DataFrame(net_coverage_ncustom, columns=['coverage'])
net_coverage_n81_df.to_csv(output_dir+str(network_name)+'_n81_coverage_'+ \
    '.csv', index=False, sep='\t')

time_df = pd.DataFrame(list(zip(filenames, computing_time)), \
    columns = ['file', 'computing_time'])
time_df.to_csv(output_dir+'runtimes.txt', index=False, sep='\t')
