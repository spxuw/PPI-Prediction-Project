import sys
import pandas as pd
import numpy as np
from sklearn import metrics
from sklearn.metrics import precision_recall_curve
from sklearn.metrics import roc_auc_score
from sklearn.metrics import ndcg_score
from sklearn.metrics import accuracy_score
import glob
import warnings
# import statistics
warnings.filterwarnings('ignore')

# read in file paths from cmd arguments
network_name = sys.argv[1]
preproc_dir = sys.argv[2]
network_dir = sys.argv[3]
output_dir = sys.argv[4]

# subset gan files for current network_name
gan_files = glob.glob(preproc_dir+'gan/'+str(network_name)+'*results.csv')
gan_files.sort()

# subset runtime files for current network_name
preproc_runtime = glob.glob(network_dir+'*.txt')
preproc_runtime = pd.read_csv(preproc_runtime[0], sep='\t')
preproc_runtime = preproc_runtime[preproc_runtime.file != str(network_name)+'_n100.emb']
preproc_runtime['file'] = preproc_runtime['file'].str.replace('.edgelist', '').str.replace('.emb','').str.replace(str(network_name), '')
preproc_runtime['file'] = preproc_runtime.file.str.slice(start=11)
new_row = []
occurred = []

for current_file in preproc_runtime['file']:
    if current_file in occurred:
        continue
    else:
        occurred.append(current_file)
        sub_data = preproc_runtime.loc[preproc_runtime['file'] == current_file]
        new_row.append({'file':current_file, 'computing_time':sub_data.sum(0)[1]})

preproc_runtime = pd.DataFrame(new_row)


gan_runtimes = glob.glob(preproc_dir+'gan/'+str(network_name)+'*.log')
gan_runtimes.sort()

# performance metric
AUROC = np.zeros(10)
AUPRC = np.zeros(10)
# P_500 = np.zeros(10)
Accuracy = np.zeros(10)
NDCG = np.zeros(10)
Computing_time = np.zeros(10)


rea = 0
for kf in range(0, 10):
    print(kf)

    gan_file = open(gan_files[kf], 'r')
    gan_lines = gan_file.readlines()

    edge_map = {}

    count = 0
    # Strips the newline character
    for line in gan_lines:
        count += 1
        edge_id_end_pos = line.index('\'', 2)
        node_indices = line[2:edge_id_end_pos].split('_')
        confidence_start_pos = line.rfind('[') + 1
        binary_class = float(line[edge_id_end_pos+4:confidence_start_pos-3])
        edge_id = node_indices[0] + "_" + node_indices[1] if node_indices[0] < node_indices[1] else node_indices[1] + "_" + node_indices[0]

        confidence_start_pos = line.rfind('[') + 1
        confidence = float(line[confidence_start_pos:-2])

        if edge_id in edge_map:
            edge_map[edge_id]["conf"].append(confidence)
            edge_map[edge_id]["binary"].append(binary_class)
        else:
            edge_map[edge_id] = {
                "id": edge_id,
                "conf": [ confidence ],
                "binary": [ binary_class ]
            }
    scores = []
    true_label = []
    for edge_id in edge_map:
        # scores.append(statistics.mean(edge_map[edge_id]["conf"]))
        scores.append(max(edge_map[edge_id]["conf"]))
        true_label.append(int(edge_map[edge_id]["binary"][0]))

    scores = np.array(scores)
    true_label = np.array(true_label)
    # AUC
    AUROC[rea] = roc_auc_score(true_label, scores)

    # AUPRC
    precision, recall, thresholds = precision_recall_curve(true_label, scores)
    AUPRC[rea] = metrics.auc(recall, precision)

    # # Precision of top 5000
    # sorted_indices = scores.argsort()[-500:][::-1]
    # P_500[rea] = len(np.where(sorted_indices<500)[0])*1.0/500

    # Accuracy
    binary_scores = np.where(scores > 0.5, 1, 0)
    Accuracy[rea] = accuracy_score(true_label, binary_scores)

    # NDCG
    NDCG[rea] = ndcg_score([true_label], [scores])

    # Time complexity
    current_gan_runtime = open(gan_runtimes[kf], 'r')
    current_gan_runtime = current_gan_runtime.read()

    Computing_time[rea] = float(preproc_runtime.iloc[rea, 1]) + float(current_gan_runtime)

    rea = rea + 1

Metric = pd.DataFrame({'AUROC': AUROC, 'AUPRC': AUPRC, 'Accuracy': Accuracy, \
'NDCG': NDCG, 'Computing_time': Computing_time})
Metric.to_csv(output_dir+str(network_name)+'_metrics.csv', index=False)
