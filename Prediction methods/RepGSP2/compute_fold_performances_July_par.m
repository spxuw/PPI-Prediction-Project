function [Performance_Table, cumTP] = compute_fold_performances_July_par(cmp_time, ATrue, Values, pos, n_missingedges, TopP)

%% Compute Performance Metrics
ind = sub2ind(size(ATrue),TopP(:,1),TopP(:,2));

SelectedValues = Values;
SelectedATrue = ATrue(ind);
[X,Y,T,AUROCmatlab] = perfcurve(SelectedATrue,1-SelectedValues,1);
[X_reca,Y_prec,T,AUPRCmatlab] = perfcurve(SelectedATrue,1-SelectedValues,1,'XCrit','reca','YCrit','prec');

figure('Name','TP vs FP matlab')
plot(X,Y,'LineWidth',1)
xlabel('False positive rate') 
ylabel('True positive rate')

figure('Name','prec vs rec')
plot(X_reca,Y_prec,'LineWidth',1);
ylabel('precision')
xlabel('recall')

cumTP = cumsum(ATrue(ind))';% check
Pperc = cumTP(pos)/pos;
PpercME = cumTP(pos)/n_missingedges;
Performance_Table = table(AUROCmatlab, AUPRCmatlab, Pperc, PpercME, cmp_time);

end
