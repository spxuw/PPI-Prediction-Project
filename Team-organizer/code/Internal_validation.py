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
import time
import warnings
warnings.filterwarnings('ignore')

# import the edge list
Edgelist = pd.read_csv('../data/HuRI/HuRI.csv')

# performance metric
AUROC = np.zeros(10)
AUPRC = np.zeros(10)
P_5000 = np.zeros(10)
NDCG = np.zeros(10)
Computing_time = np.zeros(10)

for rea in range(10):
    start_time = time.time()
    # construct the graph
    G = nx.from_pandas_edgelist(Edgelist, source='source', target='target')
    Unobserved_links = list(nx.non_edges(G))
    train,test=train_test_split(list(G.edges()),test_size=0.1,random_state=rea)
    scores = np.zeros(len(test) + len(Unobserved_links))
    G.remove_edges_from(test)

    for i in range(len(test)):
        CN = sorted(nx.common_neighbors(G, (test[i][0]),(test[i][1])))
        scores[i] = len(CN)

    #sources = []
    #targets = []
    for i in range(len(Unobserved_links)):
        CN = sorted(nx.common_neighbors(G, (Unobserved_links[i][0]),(Unobserved_links[i][1])))
        scores[i+len(test)]=len(CN)
        #sources.append(Unobserved_links[i][0])
        #targets.append(Unobserved_links[i][1])

    true_label = np.concatenate((np.ones(len(test)), np.zeros(len(Unobserved_links))))
    # AUC
    AUROC[rea] = roc_auc_score(true_label, scores)

    # AUPRC
    precision, recall, thresholds = precision_recall_curve(true_label, scores)
    AUPRC[rea] = metrics.auc(recall, precision)

    # Precision of top 5000
    sorted_indices = scores.argsort()[-5000:][::-1]
    P_5000[rea] =  len(np.where(sorted_indices<5000)[0])*1.0/5000

    # NDCG
    NDCG[rea] = ndcg_score([true_label], [scores])

    # Time complexity
    Computing_time[rea] = time.time() - start_time
Metric = pd.DataFrame({'AUROC': AUROC, 'AUPRC': AUPRC, 'P_5000': P_5000, 'NDCG': NDCG, 'Computing_time': Computing_time})
#Merged = pd.DataFrame({'source': sources, 'target': targets, 'score': scores[len(test):len(test)+len(Unobserved_links)]})
#Merged = Merged.sort_values(by=['score'], ascending=False)
#Merged = Merged.head(int(np.round(len(Unobserved_links)*0.01)))
#Merged.to_csv('../results/HuRI_scores.csv', index=False)
Metric.to_csv('../results/HuRI_Metrics.csv', index=False)
