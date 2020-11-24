# Code description
Internal_validation.py is an example code to calculate the AUROC, AUPRC, P(5000) and NDCG.
External_validation.py is an example code to output the scores of top-500 links.
Note that in internal validation, we used 10 different randomly splitings. However, the participants should report the metrics with 10-fold cross validation.

# Computing environment
All the source codes are tested with python 3.8.3 on macOS 11.01.

# External packages/libraries 
The following python dependencies are required:
  - python==3.8.3
  - networkx==2.5
  - pandas==1.1.4
  - numpy==1.19.2 
  - sklearn==0.23.2
  
# Additional dataset used in the method
N/A

# Method description
The method used in this example code is the heuristic score: Common Neighbors for node pair. The common neighbors algorithm quantifies the overlap or similarity of two nodes as follows[1]:

![equation](https://render.githubusercontent.com/render/math?math=s_%7Bij%7D%20%3D%20%7C%5CGamma(i)%5Ccup%5CGamma(j)%7C)  


# Reference
[1]. Lü, L., & Zhou, T. (2011). Link prediction in complex networks: A survey. Physica A: statistical mechanics and its applications, 390(6), 1150-1170.




