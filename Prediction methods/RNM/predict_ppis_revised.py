import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import networkx as nx
import time,subprocess,argparse
from sklearn.model_selection import KFold
from functools import partial
from sklearn.metrics import roc_auc_score,precision_recall_curve,auc,ndcg_score
import pdb

parser = argparse.ArgumentParser(description='Predict PPIs from interaction data using the Weiner Filter')

parser.add_argument('-s','--spectral_scale',type=float,nargs=1,
                   help='Fraction of largest eigenvalue for use in spectral noise model',default=0.8)
parser.add_argument('-C','--noise_offset',type=float,nargs=1,
                   help='Offset of the nth root noise',default=0.)
parser.add_argument('-a','--nth_root',type=float,nargs=1,
                   help='Fractional power of the nth root noise',
                    default=3.)
parser.add_argument('-K','--root_scaling',type=float,nargs=1,
                   help='Scaling of nth root noise', default=3.)

args = parser.parse_args()
sc = args.spectral_scale if isinstance(args.spectral_scale,float) else args.spectral_scale[0]
K = args.root_scaling if isinstance(args.root_scaling,float) else args.root_scaling[0]
C = args.noise_offset if isinstance(args.noise_offset,float) else args.noise_offset[0]
a = args.nth_root if isinstance(args.nth_root,float) else args.nth_root[0]

def calc_stats(t,T):
    auprc = auc(*precision_recall_curve(t,np.linspace(0,1,t.shape[0])[::-1])[:2][::-1])
    auroc = roc_auc_score(t,np.linspace(0,1,t.shape[0])[::-1])
    TP = np.cumsum(t)
    TPR = TP/np.arange(1,t.shape[0]+1)
    ndcg = ndcg_score(t.reshape(1,-1),np.linspace(0,1,t.shape[0])[::-1].reshape(1,-1))
#     TN = np.zeros(t.shape[0])
#     TN[0] = (t.shape[0]-T)
#     TN[1:] = (t.shape[0]-T) - (np.arange(1,t.shape[0])-TP[:-1])
#     FP = np.arange(1,t.shape[0]+1)-TP
#     FPR = (FP)/(TN+FP)
#     #FPR = 1 - np.cumsum(t)/np.arange(1,t.shape[0]+1)
#     DCG = np.sum([1/np.log(1+ii+1) for ii,TT in enumerate(t) if TT])
#     IDCG = np.sum(1/np.log(1+np.arange(1,T+1)))
    return {'AUROC':auroc,'AUPRC':auprc,'P_500':TPR[500],'NDCG':ndcg}

def run_l3(trH,teH,srt_ids,org):
    #srt_ids = np.sort(np.union1d(data.source.values, data.target.values))
    idx_gid = pd.Series(dict(enumerate(srt_ids)))
    gid_idx = pd.Series(dict([(elt,ii) for ii,elt in enumerate(srt_ids)]))
    my_l = []
    for u,v in trH.edges():
        my_l.append('\t'.join(['%d' % elt for elt in [gid_idx.loc[u],gid_idx.loc[v],1]]))
        my_l.append('\t'.join(['%d' % elt for elt in [gid_idx.loc[v],gid_idx.loc[u],1]]))
    with open('temp_edges.txt','w') as fh:
        fh.write('\n'.join(my_l))
    TA = time.process_time()
    subprocess.run(['./L3.out','temp_edges.txt'])
    TB = time.process_time()
    L3pred = pd.read_table('L3_predictions_temp_edges.txt.dat',header=None)
    L3pred.iloc[:,0] = idx_gid.loc[L3pred.iloc[:,0]].values
    L3pred.iloc[:,1] = idx_gid.loc[L3pred.iloc[:,1]].values
    L3pred.columns=['source','target','score_l3']
    ## order indices lexicographically
    r2r = np.where(L3pred.source>L3pred.target)[0]
    L3pred.iloc[r2r,:2] = L3pred.iloc[r2r,:2].iloc[:,::-1].copy().values
    Z3 = list(zip(L3pred.iloc[:,0].values,L3pred.iloc[:,1].values))
    tf_l = np.array([elt in teH.edges() for elt in Z3 if elt not in trH.edges()])
    return L3pred.set_index(['source','target']).rank(pct=True),tf_l,TB-TA

org_fn_d = {'worm':'C. elegans.csv',
            'human':'HuRI.csv',
            'plant':'Arabidopsis.csv',
            'synthetic':'SyntheticPPI.csv',
            'yeast':'Yeast.csv'}
TOPN = 10000
def run_l3_ext(H):
    #srt_ids = np.sort(np.union1d(data.source.values, data.target.values))
    srt_ids = sorted(list(H.nodes()))
    idx_gid = pd.Series(dict(enumerate(srt_ids)))
    gid_idx = pd.Series(dict([(elt,ii) for ii,elt in enumerate(srt_ids)]))
    my_l = []
    for u,v in H.edges():
        my_l.append('\t'.join(['%d' % elt for elt in [gid_idx.loc[u],gid_idx.loc[v],1]]))
        my_l.append('\t'.join(['%d' % elt for elt in [gid_idx.loc[v],gid_idx.loc[u],1]]))
    with open('temp_edges.txt','w') as fh:
        fh.write('\n'.join(my_l))
    subprocess.run(['./L3.out','temp_edges.txt'])
    L3pred = pd.read_table('L3_predictions_temp_edges.txt.dat',header=None)
    L3pred2 = pd.DataFrame({'source':idx_gid.loc[L3pred.iloc[:TOPN,0]].values,
              'target':idx_gid.loc[L3pred.iloc[:TOPN,1]].values,
              'score':L3pred.iloc[:TOPN,2].values})
    ## order indices lexicographically
    r2r = np.where(L3pred2.source>L3pred2.target)[0]
    L3pred2.iloc[r2r,:2] = L3pred2.iloc[r2r,:2].iloc[:,::-1].copy().values
    return pd.DataFrame(L3pred2).set_index(['source','target'])

np.random.seed(123456789)
kf = KFold(10,shuffle=True)
#K = 3; C = 0; a=3; sc=0.95
def internal_validation():
    for org,fn in org_fn_d.items():
        interaction_data = pd.read_csv('_'.join([org,fn]))
        interaction_data['weight']=1
        interaction_data = interaction_data.set_index(['source','target'])
        all_edges = interaction_data.index.tolist()
        H = nx.Graph()
        H.add_edges_from(all_edges)
        np.random.seed(123456789)
        avg_res = {}
        for jj,(tr,te) in enumerate(kf.split(all_edges)):
            trH = nx.Graph()
            teH = nx.Graph()
            traea = np.asarray(all_edges)[tr,:]
            teaea = np.asarray(all_edges)[te,:]
            trH.add_edges_from(traea)
            idx_ensg_tr = pd.Series(dict(enumerate(trH.nodes())))
            teH.add_edges_from(teaea)
            T0 = time.process_time()
            ## run L3
            l3_p,l3_tf,l3_dt =run_l3(trH,teH,H.nodes(),org)
            ## find eigenvalues and eigenvectors for the adjacency matrix -- to be used in the later methods
            trAdj = nx.adjacency_matrix(trH)
            x_inds,y_inds = np.triu_indices(trAdj.shape[0])
            evls,evcs = np.linalg.eigh(trAdj.toarray()) ## decompose adjacency matrix
            sel_evls = np.where(np.abs(evls)>1e-12)[0]
            nz_evls = np.abs(evls[sel_evls]) ## abs to get |A|
            nz_evcs = evcs[:,sel_evls]
            S = np.matrix(np.dot(nz_evcs*nz_evls,nz_evcs.T))
            ## diagonal noise model
            N = np.diagflat(K*(np.sum(trAdj.toarray(),axis=0))**(1/a)+C) ## model 1
            SM_inv = np.linalg.inv(S+N)
            diag_pred = S*SM_inv*trAdj*S*SM_inv
            vals1 = np.asarray(diag_pred[(x_inds,y_inds)]).squeeze()
            vals1_AS = np.argsort(vals1)[::-1]
            Z1_df = pd.DataFrame({'source':idx_ensg_tr.loc[x_inds[vals1_AS]].values,
                                  'target':idx_ensg_tr.loc[y_inds[vals1_AS]].values,'score_noise':vals1[vals1_AS]})
            ## order indices lexicographically
            r2r = np.where(Z1_df.source>Z1_df.target)[0]

            Z1_df.iloc[r2r,:2] = Z1_df.iloc[r2r,:2].iloc[:,::-1].copy().values
            Z1_df = Z1_df.set_index(['source','target'])
            ## eliminate any edges from the training set
            Z1_df = Z1_df.loc[Z1_df.index.difference(trH.edges())]
            ## convert scores to ranks
            pdb.set_trace()
            Z1_df = Z1_df.rank(pct=True)
            ## spectral noise model
            RR=sc*np.amax(nz_evls)
            spec_pred = np.matrix(np.dot(nz_evcs*(nz_evls**2*evls[sel_evls]/(nz_evls**2+RR**2)),nz_evcs.T))
            vals2 = np.asarray(spec_pred[(x_inds,y_inds)]).squeeze()
            vals2_AS = np.argsort(vals2)[::-1]
            Z2_df = pd.DataFrame({'source':idx_ensg_tr.loc[x_inds[vals2_AS]].values,
                      'target':idx_ensg_tr.loc[y_inds[vals2_AS]].values,'score_spec':vals2[vals2_AS]})
            ## order indices lexicographically
            r2r = np.where(Z2_df.source>Z2_df.target)[0]
            Z2_df.iloc[r2r,:2] = Z2_df.iloc[r2r,:2].iloc[:,::-1].copy().values
            Z2_df = Z2_df.set_index(['source','target'])
            ## eliminate any edges from the training set
            Z2_df = Z2_df.loc[Z2_df.index.difference(trH.edges())]
            ## convert scores to ranks
            Z2_df = Z2_df.rank(pct=True)
            ## combine the 3 prediction methods by averaging over the ranks

            avg_df = pd.concat([Z1_df,Z2_df,l3_p],axis=1).fillna(0).mean(axis=1).sort_values(ascending=False)

            ## test whether predictions are in the test network
            tf_arr_avg = np.asarray([elt in teH.edges() for elt in avg_df.index])
            ## calculate statistics
            opavg = calc_stats(tf_arr_avg,len(te))
            T1 = time.process_time()
            opavg['computing_time']=T1-T0
            avg_res[jj]=opavg
        pd.DataFrame(avg_res).T.to_csv('%s_avg.csv' % org)

def external_validation():
    org='human'
    interaction_data = pd.read_table('HURI_DATA.txt')
    interaction_data['weight']=1
    interaction_data = interaction_data.set_index(['Ensembl_gene_id_a','Ensembl_gene_id_b'])
    assay_pred_l = []
    graph_l = []
    for assay in range(1,4):
        ## make predictions for each assay separately
        print(assay)
        H = nx.Graph()
        assay_edgs = interaction_data[interaction_data['in_assay_v%d'%assay]==1]
        all_edges = assay_edgs.index.tolist()
        H.add_edges_from(all_edges)
        graph_l.append(H)
        Adj = nx.adjacency_matrix(H)
        x_inds,y_inds = np.triu_indices(Adj.shape[0])
        idx_ensg = pd.Series(dict(enumerate(H.nodes())))
        Hevls,Hevcs = np.linalg.eigh(Adj.toarray()) ## decompose adjacency matrix
        Hsel_evls = np.where(np.abs(Hevls)>1e-12)[0]
        Hnz_evls = np.abs(Hevls[Hsel_evls]) ## abs to get |A|
        Hnz_evcs = Hevcs[:,Hsel_evls]
        HS = np.matrix(np.dot(Hnz_evcs*Hnz_evls,Hnz_evcs.T))
        HN = np.diagflat(K*(np.sum(Adj.toarray(),axis=0))**(1/a)+C) ## model 1
        HSM_inv = np.linalg.inv(HS+HN)
        Hdiag_pred = HS*HSM_inv*Adj*HS*HSM_inv
        Hvals1 = np.asarray(Hdiag_pred[(x_inds,y_inds)]).squeeze()
        Hvals1_AS = np.argsort(Hvals1)[::-1][:TOPN]
        diagpred = pd.DataFrame({'source':idx_ensg[x_inds[Hvals1_AS]].values,
              'target':idx_ensg[y_inds[Hvals1_AS]].values,
              'score_diag':Hvals1[Hvals1_AS]})
        r2r = np.where(diagpred.source>diagpred.target)[0]
        diagpred.iloc[r2r,:2] = diagpred.iloc[r2r,:2].iloc[:,::-1].copy().values
        diagpred = diagpred.set_index(['source','target'])

        RR=sc*np.amax(Hnz_evls)
        Hspec_pred = np.matrix(np.dot(Hnz_evcs*(Hnz_evls**2*Hevls[Hsel_evls]/(Hnz_evls**2+RR**2)),Hnz_evcs.T))
        Hvals2 = np.asarray(Hspec_pred[(x_inds,y_inds)]).squeeze()
        Hvals2_AS = np.argsort(Hvals2)[::-1][:TOPN]
        specpred = pd.DataFrame({'source':idx_ensg[x_inds[Hvals2_AS]].values,
              'target':idx_ensg[y_inds[Hvals2_AS]].values,
              'score_spec':Hvals2[Hvals2_AS]})
        r2r = np.where(specpred.source>specpred.target)[0]
        specpred.iloc[r2r,:2] = specpred.iloc[r2r,:2].iloc[:,::-1].copy().values
        specpred = specpred.set_index(['source','target'])
        L3pred = run_l3_ext(H)
        comb_pred = pd.concat([diagpred.rank(),specpred.rank(),L3pred.rank()],axis=1).fillna(0)
        #comb_pred = comb_pred.fillna(comb_pred.shape[0])
        rk_pred = comb_pred.mean(axis=1).sort_values(ascending=False)
        assay_pred_l.append(rk_pred)
    all_assay_preds = pd.concat(assay_pred_l,axis=1)
    all_assay_preds.columns = ['assay1_score','assay2_score','assay3_score']
    ## combine predictions, accounting for which pairs are covered in multiple assays
    minvals = all_assay_preds.min(axis=1)
    for assay,col in all_assay_preds.iteritems():
        IND = int(assay.split('_')[0][-1])-1
        selH = graph_l[IND]
        NA_INDS = np.where(col.isna())[0]
        AA,BB = zip(*col[col.isna()].index.tolist())
        PEN = np.logical_and([aa in selH.nodes() for aa in AA],
                             [bb in selH.nodes() for bb in BB])
        for na_ind,tf in zip(NA_INDS,PEN):
            all_assay_preds.loc[col.index[na_ind],assay] = 0 if tf else min(minvals.loc[col.index[na_ind]],2500)
    combined_predictions=all_assay_preds.mean(axis=1).sort_values(ascending=False).to_frame()
    combined_predictions.columns=['score']
    combined_predictions.index.names = ['source','target']
    combined_predictions.iloc[:50000].to_csv('human_top50000_predictions.csv')

if __name__ == '__main__':
    internal_validation()
    #external_validation()
