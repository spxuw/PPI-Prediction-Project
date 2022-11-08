# Release - 1.3

This version of the software combines two previous versions (v1.0 and v1.2) under one common launcher, which enables better parametrization and easier runs on arbitrary networks. In addition, P@500 calculation was fixed in the postprocessing step.

## Usage

Execute the following command after specifying the proper arguments:

```
./launcher.sh version type inputDir outputDir node2vecPath
```

| Arguments | |
| ------ | ------ |
| version | The version of the cGAN model to be used. Possible values:<br /><ul><li>cgan1: Initial version developed and submitted for the Protein-Protein Interaction Prediction Challenge organized by the International Network Medicine Consortium in 2020-2021 (https://www.network-medicine.org/). Uses topology-based and node embedding information for the link prediction, tends to predict self-loops with high confidence.</li><li>cgan2: Improved and refined version, published in BMC Bioinformatics, 2022 (https://doi.org/10.1186/s12859-022-04598-x). Uses topology-based information only.</li></ul> |
| type | The type of query to be executed. Possible values:<br /><ul><li>cross_val: Trains the given model and evaluates its performance 10 separate times in a k-fold cross validation manner. Training data consists of N81-N90 network pairs, while evaluation is conducted on N90-N100 pairs. At the end of the execution, a _metrics.csv_ is generated, containing AUROC, AUPRC, P@500, NDCG and computation time values for each fold.</li><li>exp_val: Trains the given model on only a single pair of N90-N100 network pair, then generates new predictions based on the N100 network. The single resulting CSV file might serve as input for later experimental evaluations.</li></ul> |
| inputDir | The full path of the directory containing any number of network files. These files are expected to be adjacency lists in CSV format and structured the same as the sample networks. The software automatically executes on each valid CSV file in the input directory, using the same launch arguments.</li></ul> |
| outputDir | The full path of the the desired output directory. Under this path, subdirectories will be created that are named after the corresponding input networks.</li></ul> |
| node2vecPath | The full path of the node2vec executable, compiled from the SNAP library (https://github.com/snap-stanford/snap). Requiried only when ```version=cgan1 ```.</li></ul> |
