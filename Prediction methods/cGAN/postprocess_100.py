import os
import sys
import pandas as pd
import numpy as np
import csv
import networkx as nx
import random
import pdb
from sklearn.model_selection import train_test_split
from sklearn.metrics import average_precision_score
from sklearn import metrics
from sklearn.metrics import precision_recall_curve
from sklearn.metrics import roc_auc_score
from sklearn.metrics import ndcg_score
import glob
import statistics
import warnings
warnings.filterwarnings('ignore')

# import the edge list
output_dir = sys.argv[1]
network_name = sys.argv[2]
gan_filename=sys.argv[3]


id_data = glob.glob(output_dir+'network/'+str(network_name)+'/'+str(network_name)+'_edgelist_conv_id.csv')
id_data = pd.read_csv(id_data[0], sep = '\t')
# get int:str ID dictionary
id_dict = dict(zip(id_data.int_node_id, id_data.str_node_id))

gan_file = open(gan_filename, 'r')
gan_lines = gan_file.readlines()

edge_map = {}
# create edge id dictionary from gan output
for line in gan_lines:
    edge_id_end_pos = line.index('\'', 2)
    node_indices = line[2:edge_id_end_pos].split('_')
	
	# Ignore self loops
    if (node_indices[0] == node_indices[1]):
	    continue
	
    edge_id = node_indices[0] + "_" + node_indices[1] if node_indices[0] < node_indices[1] else node_indices[1] + "_" + node_indices[0]

    confidence_start_pos = line.rfind('[') + 1
    confidence = float(line[confidence_start_pos:-2])

    if edge_id in edge_map:
        edge_map[edge_id]["conf"].append(confidence)
    else:
        edge_map[edge_id] = {
            "id": edge_id,
            "conf": [ confidence ],
            "source": [ node_indices[0] ],
            "target": [ node_indices[1] ]
        }


scores_list = []
source_list = []
target_list = []

for edge_id in edge_map:
    node_indices = edge_id.split('_')
    source = id_dict[int(node_indices[0])]
    target = id_dict[int(node_indices[1])]

    source_list.append(source)
    target_list.append(target)
    scores_list.append(max(edge_map[edge_id]["conf"]))

whole_dict = {'Sources':source_list, 'Targets':target_list, 'Score':scores_list}
whole_df = pd.DataFrame(whole_dict)
sorted_df = whole_df.sort_values(by='Score', ascending=False)
top500 = sorted_df.head(500)
top500.to_csv(output_dir+'metrics/'+network_name+'_top500_scores.csv', index=False)
