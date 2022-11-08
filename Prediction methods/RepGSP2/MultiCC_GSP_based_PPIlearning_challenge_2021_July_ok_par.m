function GSP_PPI_data = MultiCC_GSP_based_PPIlearning_challenge_2021_July_ok_par(GSP_PPI_data)
%global groundtruthA;%for debug only

%% input param
P = GSP_PPI_data.P;
AInput = GSP_PPI_data.AInput;
G_bin = GSP_PPI_data.G_bin;
GInput = GSP_PPI_data.GInput;


%% output param
GSP_PPI_data.TopP = [];% This will contain the indexes of the P minimum distance node pairs, LeastP;
GSP_PPI_data.TopP_Vc = [];% This will contain the distances of the P minimum distance node pairs, LeastP_Vc;
GSP_PPI_data.VcPotMarkov = [];%% The coefficient of the matrix VcPotMarkov will be 0  for the node pairs which have edges in the input graph, and it will contain the distances  between nodes otherwise;

%%  MRF potential functions
nscales = 6;%determines community mining computational complexity
idxscale = 3;%idxscale<=nscales, increases with community size
alpha = 0.75;  %Weight of SoG Vc versus Community labels Vc
com_potential = 'repulsive';%'actractive' or 'repulsive'




VcXAll = [];
VcCAll = [];
VcTOTAll = [];

N = size(AInput,1);
if  alpha~=1
    [G_bin,G_binsize] = conncomp(GInput);
    SelectedCom = ones(N,1);
    
    switch com_potential
        case 'repulsive'
            computeVcC = @(i,j)(double(SelectedCom(i)==SelectedCom(j)));
        case 'actractive'
            computeVcC = @(i,j)(double(SelectedCom(i)~=SelectedCom(j)));
    end
    for i=1:numel( G_binsize)
        if i==1
            n_communities = 1;
        else
            n_communities = numel(unique(SelectedCom));
        end
        Idx_Sub_Connect_G{i} = (G_bin == i);
        if G_binsize(i)>50
            tmpG = subgraph(GInput, Idx_Sub_Connect_G{i});
            Q = pat_com_det_tremblay_challenge_2021_01_31_ok(adjacency(tmpG),nscales);
            SelectedCom(Idx_Sub_Connect_G{i}) = Q.COM(:,idxscale)+n_communities;
        else
            SelectedCom(Idx_Sub_Connect_G{i}) = n_communities+1;
        end
    end
end

VcPotMarkov = triu(ones(N,N));
SumAin = sum(AInput(:))/2;%%%%%%%% HO DIVISO A METà

% LeastP = [];
% LeastP_Vc = [];
% VcX = zeros(N,N);
% delta = 10;
% Num_me = 0; %number of missing edges (inizializzazione)

Mat = triu(ones(N),1);
[row, col] = find(Mat);
Alllinks = table(row,col); %all possible links 
clear Mat row col
[row, col] = find(triu(AInput,1));
Links1 = table(row,col); %links=1
LeastP = setdiff(Alllinks,Links1); %null links on which to calculate the potential function (Vc)
LeastPMat = [LeastP.row LeastP.col];
% LeastP = [Links0.];
LeastP_Vc = ones(size(LeastP,1),1);
delta = 10;
NE = length(LeastP_Vc);
LeastP = [];

tic
parfor nn=1:NE
%     if ~mod(nn,delta) disp(['accomplished percentage ' num2str(nn/NE)]); end
    i = LeastPMat(nn,1);
    j = LeastPMat(nn,2);
    if (G_bin(i)==G_bin(j)) % all the links for debug purposes
        %calcolo potenziali
        VcX = diag(AInput(:,i))*AInput*diag(AInput(:,j));
        VcX = (1-sum(VcX(:))/SumAin)^3;%
        
        % previous            switch com_potential
        %                 case 'repulsive'
        %                     VcC = double(SelectedCom(i)==SelectedCom(j));
        %                 case 'actractive'
        %                     VcC = double(SelectedCom(i)~=SelectedCom(j));
        %             end
        
        %new
        VcC = computeVcC(i,j);%new
        VcTOT = alpha*VcX+(1-alpha)*VcC;
    else
        VcTOT=1;
    end
    %assgnazione potenziali
    %         VcPotMarkov(i,j) = VcTOT;
    LeastP(nn,:) = [i,j];
    LeastP_Vc(nn) = VcTOT;
    
end


toc
disp(['time in cycle'])

[LeastP_Vc,sort_index] = sort(LeastP_Vc,'ascend');
LeastP = LeastP(sort_index,:);

VcPotMarkov=VcPotMarkov+(triu(VcPotMarkov,1))';
% VcPotMarkov=VcPotMarkov-triu(tril(VcPotMarkov));

%% Output Par
GSP_PPI_data.TopP=LeastP;
GSP_PPI_data.TopP_Vc=LeastP_Vc;
% GSP_PPI_data.VcPotMarkov=VcPotMarkov;


end