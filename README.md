# PPI Prediction Challenge Preparation
This is the repository for the "International Network Medicine Consortium Protein-Protein Interactions Prediction Challenge".


# Contents
* [Overview]
* [Repo Contents]
* [Software dependencies]
* [Reproduction instructions]
* [Baseline methods implementation]

# Overview

Comprehensive insight of the human protein-protein interaction (PPI) network or interactome can greatly help us understand the molecular mechanism of complex biological processes and diseases. Despite the remarkable experimental efforts to map the human interactome, a large number of PPIs are still unmapped. Computational approaches, especially network-based methods, can facilitate the identification of new unmapped PPIs. However, a systematic benchmark evaluation of existing network-based methods in predicting PPIs is still lacking. Here, we report the collective efforts of the International Network Medicine Consortium on benchmarking 24 representative network-based methods in predicting PPIs across five different interactomes, including a synthetic interactome generated by the duplication-mutation-complementation model, and the interactomes of four different organisms: Arabidopsis, C. elegans, S. cerevisiae, and Homo sapiens. We select the top seven methods through computational validation on the human interactome. Then we experimentally validate their top predicted PPIs through the yeast two-hybrid experiment. Our results indicate that the similarity-based methods leveraging the underlying characteristics of PPIs shows superior performances over other general link prediction methods. Through experimental validation, we confirmed that the top-ranking methods still show promising performance externally. These new predicted PPIs formalize into four functional modules, implying that the physical binding assembles proteins into large functional communities.

# Repo Contents of each team
* Codes: source code of top-3 methods.
* Edgelist: edge lsit of five interactomes.
* results: (i). Predicitons of top-500 PPIs by top-7 methods. (ii). Results of experimental valition of positive PPIs by top-7 methods.
# Software dependencies
The following dependencies are required:

# Reproduction instructions

### Baseline methods implementation
The baseline methods used to compared the peroformance in each network:
  - SEAL: https://github.com/muhanzhang/SEAL.
  - SkipGNN: https://github.com/kexinhuang12345/SkipGNN.
