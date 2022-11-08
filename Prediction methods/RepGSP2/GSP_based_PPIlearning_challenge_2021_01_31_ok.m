function GSP_PPI_data=GSP_based_PPIlearning_challenge_2021_01_31_ok(GSP_PPI_data)
%global groundtruthA;%for debug only

%% input param
P=GSP_PPI_data.P;
AInput=GSP_PPI_data.AInput;
G_bin=GSP_PPI_data.G_bin;


%% output param
GSP_PPI_data.TopP=[];% This will contain the indexes of the P minimum distance node pairs, LeastP;
GSP_PPI_data.TopP_Vc=[];% This will contain the distances of the P minimum distance node pairs, LeastP_Vc;
GSP_PPI_data.VcPotMarkov=[];%% The coefficient of the matrix VcPotMarkov will be 0  for the node pairs which have edges in the input graph, and it will contain the distances  between nodes otherwise;

%%  MRF potential functions
nscales=6;%determines community mining computational complexity
idxscale=3;%idxscale<=nscales, increases with community size
alpha=0.75;  %Weight of SoG Vc versus Community labels Vc
com_potential='repulsive';%'actractive' or 'repulsive'

VcXAll=[];
VcCAll=[];
VcTOTAll=[];

N=size(AInput,1);
if  alpha~=1
    Q=pat_com_det_tremblay_challenge_2021_01_31_ok(AInput,nscales);
    SelectedCom=Q.COM(:,idxscale);
%Q.COM= NxNscales,  
%largest communities (least no. of  labels) are found  at Q.COM(:,end)
else
    SelectedCom=ones(N,1);
end


VcPotMarkov=zeros(N,N);
SumAin=sum(AInput(:));
LeastP=zeros(P,2);
LeastP_Vc=ones(P,1);
VcX=zeros(N,N);
delta=10;

tic
for i=1:N
    if ~mod(i,delta) disp(['accomplished percentage ' num2str(i/N)]); end
    for j=(i+1):N
    %if ~mod(j,40*delta) disp(['accomplished percentage ' num2str(i/N)]); end
        if (~AInput(i,j))&(G_bin(i)==G_bin(j)) % all the links for debug purposes
            VcX=diag(AInput(:,i))*AInput*diag(AInput(:,j));
            VcX=(1-sum(VcX(:))/SumAin)^3;% 
            switch com_potential
                case 'repulsive'
                VcC=double(SelectedCom(i)==SelectedCom(j));
               case 'actractive'
                VcC=double(SelectedCom(i)~=SelectedCom(j));
            end
            VcTOT=alpha*VcX+(1-alpha)*VcC;
            VcPotMarkov(i,j)=VcTOT; 
            if VcTOT< LeastP_Vc(P)
                LeastP(P,:)=[i,j];
                LeastP_Vc(P)=VcTOT;
                [LeastP_Vc,sort_index]=sort(LeastP_Vc,'ascend');
                LeastP(:,1)=LeastP(sort_index,1);
                LeastP(:,2)=LeastP(sort_index,2);
                VcXAll=[VcXAll VcX];
                VcCAll=[VcCAll VcC];
                VcTOTAll=[VcTOTAll VcTOT];
                
            end
        end
        
    end
end
toc
disp(['time in cycle'])
VcPotMarkov=VcPotMarkov+triu(VcPotMarkov,1)';
VcPotMarkov=VcPotMarkov-triu(tril(VcPotMarkov));






%% Output Par
GSP_PPI_data.TopP=LeastP;
GSP_PPI_data.TopP_Vc=LeastP_Vc;
GSP_PPI_data.VcPotMarkov=VcPotMarkov;


end