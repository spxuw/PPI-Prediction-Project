import os
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
import warnings
warnings.filterwarnings('ignore')

# import the edge list
Edgelist = pd.read_csv('../data/HuRI/HuRI.csv')

# performance metric
AUROC = np.zeros(10)
AUPRC = np.zeros(10)
P_500 = np.zeros(10)
NDCG = np.zeros(10)

for rea in range(1):
    # construct the graph
    G = nx.from_pandas_edgelist(Edgelist, source='source', target='target')
    Unobserved_links = list(nx.non_edges(G))
    scores = np.zeros(len(Unobserved_links))

    sources = []
    targets = []
    for i in range(len(Unobserved_links)):
        CN = sorted(nx.common_neighbors(G, (Unobserved_links[i][0]),(Unobserved_links[i][1])))
        scores[i]=len(CN)
        sources.append(Unobserved_links[i][0])
        targets.append(Unobserved_links[i][1])

Merged = pd.DataFrame({'source': sources, 'target': targets, 'score': scores})
Merged = Merged.sort_values(by=['score'], ascending=False)
Merged = Merged.head(500)
Merged.to_csv('../results/HuRI_scores.csv', index=False)
