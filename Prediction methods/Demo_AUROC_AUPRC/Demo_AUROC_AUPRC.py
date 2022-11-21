import numpy as np
import matplotlib.pyplot as plt

from sklearn import svm
from sklearn.metrics import auc
from sklearn.metrics import roc_curve
from sklearn.metrics import roc_auc_score,precision_recall_curve
import pdb

pos = np.ones((50,1))
neg = np.zeros((50,1))
testy = np.concatenate((pos,neg))


#ns_probs = np.random.normal(0, 0.1, 100050) # imbalanced 
ns_probs = np.random.normal(0, 0.1, 100) # balanced
ns_probs[0:50] = np.random.normal(0.15, 0.1, 50)

plt.figure(0,figsize=(3,3))
plt.hist([ns_probs[0:50],ns_probs[50:100050]], 30, alpha=0.5, label=['Positive','Negative'])
#plt.yscale('log', nonposy='clip')
plt.legend(loc='upper right')
plt.savefig('line_plot_1.pdf')  
print(roc_auc_score(testy, ns_probs))

plt.figure(1,figsize=(3,3))
ns_fpr, ns_tpr, _ = roc_curve(testy, ns_probs)
plt.plot(ns_fpr, ns_tpr, linestyle='--')
# axis labels
plt.xlabel('False Positive Rate')
plt.ylabel('True Positive Rate')
# show the legend
plt.savefig('AUROC_1.pdf')


lr_precision, lr_recall, _ = precision_recall_curve(testy, ns_probs)
plt.figure(2,figsize=(3,3))
plt.plot(lr_recall, lr_precision, linestyle='--')
# axis labels
plt.xlabel('Recall')
plt.ylabel('Precision')
# show the legend
plt.savefig('AUPRC_1.pdf')
print(auc(lr_recall, lr_precision))
