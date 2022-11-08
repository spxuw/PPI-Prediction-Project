% GSP based PPI prediction
%  
%
%   Date: July 2021
%
%   References: 
%      S. Colonnese, P. Di Lorenzo, T. Cattai, G. Scarano, and F. D. V. Fallani, 
%      A joint markov model for communities, connectivity and signals dened over graphs,
%      IEEE Signal Processing Letters 27, 1160 (2020).
%   

clear all, close all,  clc
addpath(genpath(['.\MSCD_Wav_BETA']))%Folder containing the external library, see the acknwoledgment file for details

%% Read data
if not(exist('prot_interact'))
    load('HuRI_StringArray.mat');
    load('SyntheticPPI_StringArray.mat');
    load('Arabidopsis_StringArray.mat');
    load('C_elegans_StringArray.mat');
    load('Yeast_StringArray.mat');
end
%% PPI Settings (database, no of connected components and top degree nodes to analyze
prot_interact=C_elegans;%HuRI;%database selection: HuRI, Arabidopsis, C_elegans, Yeast, SyntheticPPI
selected_db_name='C_elegans';
thresh_ConnComp_tokeep=0.8;%keeping connected components up to selected % of nodes
%SyntheticPPI: thresh_ConnComp_tokeep=0.999; HuRI: thresh_ConnComp_tokeep=0.8;
thresh_TopDegree_tokeep=0.9;%keeping connected components up to selected % of nodes
%% View original graph, no self loop, to-do: use multiple edge to define weight
prot_list=unique(prot_interact);
prot_num=numel(prot_list);
disp('Creating ground truth graph...');
tic
G=graph(prot_interact(:,1),prot_interact(:,2));
G=simplify(G);
toc
[G_bin,G_binsize] = conncomp(G);

%% 10-fold evaluation
n_fold = 10;
fold_range=2:5;

AUROC = zeros(n_fold,1);     % Area under the receiver operating characteristic (ROC) curve
% TP/(TP+FN) versus FP/(TN+FP) (Pd vs Pfa)
AUPRC = zeros(n_fold,1);     % Area under the Precision-Recall Curve (PRC)
% TP/(TP+FP) versus TP/(TP+FN) (Precision vs Pd)
P_500 = zeros(n_fold,1);     % Precision of the top-500 predicted PPIs
% List of top 500 proteins: TP/(TP+FP) sul
% sottoinsieme
NDCG = zeros(n_fold,1);      % Normalized Discounted Cumulative Gain [12]
% caricata funzione ndcg.m, verificare
% definizione
cmp_time = zeros(n_fold,1);  % (in seconds)
cmp_time_bar = zeros(n_fold,1);  % (in seconds)


rng(1); %initializes the Mersenne Twister generator using a seed of 1.
rand_edge_order = randperm(numedges(G));
start_edges=1;
nedges_fold = floor(numedges(G)/n_fold);


for i=1:n_fold
    disp(['fold no.' num2str(i)]);
    close all
    
    % network wo 10% arc
    GTrain = rmedge(G, rand_edge_order(start_edges:(start_edges+nedges_fold-1)));
    start_edges=start_edges+nedges_fold;%ready for next fold
    
    
    % Link prediction algorithm
    GSP_PPI_data.AInput=adjacency(GTrain);
    GSP_PPI_data.P=min(500, numnodes(G));
    GSP_PPI_data.G_bin=G_bin;
    GSP_PPI_data.GInput=GTrain;
    
    
    if any(fold_range(:) == i)
        GSP_PPI_data = MultiCC_GSP_based_PPIlearning_challenge_2021_July_ok_par(GSP_PPI_data);
        cmp_time(i,1) = toc;
        
        % Performance computation
        [Performance_Table(i,:), cumTP] = compute_fold_performances_July_par(cmp_time(i), adjacency(G), ...
            GSP_PPI_data.TopP_Vc,...
            500, nedges_fold, GSP_PPI_data.TopP...
            );
        % saving results and variables
        if ~exist('results_test', 'dir')
            mkdir('results_test')
        end
        writetable(Performance_Table, ['.\results_test\' selected_db_name '_perf_6_3_075rep_'  num2str(i) '_table.txt']);
        save(['.\results_test\' selected_db_name '_perfrevised_6_3_075rep_'  num2str(i) 'GSP_fold_' num2str(i) '.mat']);
    end
end



