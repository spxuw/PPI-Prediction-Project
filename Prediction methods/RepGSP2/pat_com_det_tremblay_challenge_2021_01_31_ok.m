function Q=pat_com_det_tremblay_challenge_2021_01_31_ok(A, nscales_input)
mydir='.';
addpath(genpath([mydir '\sgwt_com_det']))% community mining library
%if issym(A)==0
%    error('La matrice di adiacenza è direzionata')
%end 
%% Ciclo di controllo della effettiva connessione del grafo 
%(verifico non ci siano parti sconnesse)
N=length(A);
[CC] = conn_comp(A,1);
removenodes=[];
Acomplete=A;

if size(CC,1)~=1
    warning(['Il tuo grafo è disconnesso in ' num2str(size(CC,1)) ' componenti : i calcoli sono svolti per la componente più larga. Dimensioni delle componenti : ' char(10) '' num2str(sum(CC~=0,2)') ]);
    [~,ind]=max(sum(CC~=0,2));
    keepnodes=sort(CC(ind,CC(ind,:)~=0)); %ordina gli elementi di CC
    A=A(keepnodes,keepnodes);
    removenodes=setdiff([1:N],keepnodes);
end
%% Inserisci come vuoi calcolare la partizione del grafo  
interactive=0;
if(interactive)
prompt= 'Come vuoi calcolare il laplaciano? [normlap lap rwlap]\ndefault normlap\n'; %'normal', 'lap' or 'rwlap'
lap_flag=input(prompt,'s')
if isempty (lap_flag)%p2 col Sect IIA Tremblay
    lap_flag='normlap';
end
clear prompt
if size(A)>=1000 %se le dimensioni del grafo superano i 1000 nodi automaticamente usa la FWT
    filter_flag='scaling_function';%fast algorithm in Sect.V
else    
    prompt='\n\nCome vuoi calcolare il coefficiente di correlazione? [wavelet scaling_function=fast]\n default wavelet\n';% 
    filter_flag=input(prompt,'s')
    if isempty(filter_flag)
        filter_flag='wavelet';
    end
end    
clear prompt
switch filter_flag
    case 'scaling_function'    
    Nsignals=input('\n\nNumero di segnali random da analizzare:\ndefault 15\n');%per costruzione base fast SectV
    if isempty(Nsignals)
        Nsignals=15;
    end
    Jstab=input('\n\nNumero di vettori per calcolare la stabilità:\ndefault 10\n');%per costruzione base fast SectVI
    if isempty(Jstab)
       Jstab=10;
    end
end
Nscales=input('\n\nNumero di scales da analizzare:\ndefault 10\n');
if isempty(Nscales)
    Nscales=10;
end
else
    lap_flag='normlap';
    filter_flag='wavelet';
    Nsignals=15;
    Jstab=10;
    Nscales=nscales_input;


end

%% Elaborazione dati e partizione in comunità
switch filter_flag
    case 'wavelet'
    display('Eseguo algoritmo standard...diagonalizzazione del Laplaciano...');
    [V,D]=MSCD_compute_spectrum(A,lap_flag); 
    D=sort(D);D(1)=0;D(D<0)=0;
    display('calcolo delle graph wavelets...');
    [Fg,g,tg]=MSCD_compute_bases(Nscales,V,D,filter_flag);
    display('calcolo delle partizioni...');
    COM_complete=MSCD_partition_complete(Fg,V(:,1));
    for i=1:length(removenodes)
        COM_complete(removenodes(i)+1:end+1,:)=COM_complete(removenodes(i):end,:);
        COM_complete(removenodes(i),:)=0;
    end
    %save COM_complete;
    
    case 'scaling_function'
    display('Eseguo algoritmo fast...calcolo delle partizioni e della loro stabilità...');
    [tg,COM_RV,stab]=MSCD_partition_RV(Nscales,A,Nsignals,Jstab,lap_flag,filter_flag);
    for i=1:length(removenodes)
        COM_RV(removenodes(i)+1:end+1,:)=COM_RV(removenodes(i):end,:);
        COM_RV(removenodes(i),:)=0;
    end
    %save COM_RV;
    display('plottaggio instabilità...');
    f1=figure; hold on;
    plot(log10(tg),1-stab,'linewidth',2);
    set(gca,'fontsize',20);set(gca,'ylim',[0,1]);
    set(gca,'xlim',[min(log10(tg)),max(log10(tg))]);
    aux=get(gca,'xtick');set(gca,'xticklabel',{floor((10.^aux).*10)/10});
    xlabel('scale s');ylabel('1-\gamma_a');title('Instability');
    set(gcf,'name',strcat('Instability'));
end

%% one can compute and plot performance iff ground truth exists:
ground_truth_exist=0; %completare con features cliniche
if ground_truth_exist
switch option
    case 'sales_pardo'
        disp(' ground thruth is represented by COM1 COM2 and COM3');
%if size(Aold)~=size(Q.A)%stf controllare se si puo'' eliminare questo if
switch filter_flag
    case 'wavelet'
        for t=1:Nscales
            isCOM1_complete(t)=PartAgreeCoef_ARonly(COM_complete(:,t),COM1);
            isCOM2_complete(t)=PartAgreeCoef_ARonly(COM_complete(:,t),COM2);
            isCOM3_complete(t)=PartAgreeCoef_ARonly(COM_complete(:,t),COM3);
        end
    case 'scaling_function'
        for t=1:Nscales
            isCOM1_complete(t)=PartAgreeCoef_ARonly(COM_RV(:,t),COM1);
            isCOM2_complete(t)=PartAgreeCoef_ARonly(COM_RV(:,t),COM2);
            isCOM3_complete(t)=PartAgreeCoef_ARonly(COM_RV(:,t),COM3);
        end
end
f1=figure; hold on;
plot(log10(tg),isCOM1_complete,'rs','linewidth',2);
plot(log10(tg),isCOM2_complete,'b<','linewidth',2);
plot(log10(tg),isCOM3_complete,'ko','linewidth',2);
legend('Large Scale','Medium Scale','Small Scale');
set(gca,'fontsize',20);
set(gca,'ylim',[0,1]);
set(gca,'xlim',[min(log10(tg)),max(log10(tg))]);
aux=get(gca,'xtick');set(gca,'xticklabel',{floor((10.^aux).*10)/10});
xlabel('scale s');
set(gcf,'name',strcat('Multiscale Community Mining Results'));
ylabel('Adj. Rand index');
set(f1,'position',[444   450   560   322]);
set(gcf,'PaperPositionMode','auto');
    otherwise
        disp('no ground truth on real data or image');
end
else
            disp('no clinical ground truth ');
end %end if ground truth exist
%% Risultati
Q.Nscales=Nscales;
Q.removenodes=removenodes;
Q.keepnodes=setdiff([1:N],removenodes);

switch filter_flag
    case 'wavelet'
        Q.COM=COM_complete;
    case 'scaling_function'
        Q.COM=COM_RV;
end

%% Visualizzazione 
               G_Acomplete = graph(Acomplete,'OmitSelfLoops');
     LWidths = G_Acomplete.Edges.Weight;
 figure(315)
set(gcf,'Name', 'Comm tremblay');
for i=1:Nscales
   switch filter_flag
        case 'wavelet'
            subplot(1,Nscales,i);
    plot(G_Acomplete,'Layout','Circle','EdgeCData',LWidths, 'NodeCData',COM_complete(:,i)/max(COM_complete(:,i)),'NodeColor','flat','MarkerSize',6);%
     title(['sc=' num2str(i)]);% basato su somiglianza: ' selected_data ', Node color=' selected_feat]);
   caxis([0 1]);
    colormap(jet);
    %mycb=colorbar;
    %ylabel({'Patients stratification';[ ' Links: ' selected_data ] ;[ ', Node color: ' selected_feat]})
            % OLD STF gsp_plot_signal(G,Q.COM_complete(:,i));%G struttura grafo
        case 'scaling_function'
    plot(G_Acomplete,'Layout','Circle','EdgeCData',LWidths, 'NodeCData',COM_RV(:,i)/max(COM_RV(:,i)),'NodeColor','flat','MarkerSize',6);%
    caxis([0 1]);
    colormap(jet);
    %mycb=colorbar;
           % gsp_plot_signal(G,Q.COM_RV(:,i));%G struttura grafo
    end
    movie_scene(i)=getframe(gcf);
end
%save ('movie_scene','movie_scene');
%fig=figure;
%movie(fig,movie_scene,1,1);

visual3D=0;
if visual3D
    figure(320)
    set(gcf,'name',strcat('Multiscale Community Mining Results'));
    %visualization attempt
    for i=1:Nscales
        ax(i) = axes('Position',[0.1*i,0.1*i,0.3,0.3]);
        plot(ax(i),G_Acomplete,'Layout','Circle','EdgeCData',LWidths, 'NodeCData',Q.COM(:,i)/max(Q.COM(:,i)),'NodeColor','flat','MarkerSize',6);
        set(ax(i),'Color','none');
        set(ax(i),'Box','off');
    end
end
%movie2avi(movie_scene,'myavifile.avi','Compression','Cinepak','fps',1);
% AVI=VideoWriter(movie_scene,'Indexed AVI',
end