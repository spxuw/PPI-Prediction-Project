# Test datasets

Test datasets in `./STRING` folder provide input to the current version of the link prediction software.

## Data source
STRING (Search Tool for the Retrieval of Interacting Genes/Proteins) database: https://string-db.org

*Sensen LJ, Kuhn M, Stark M, Chaffron S, Creevey C, Muller J, et al.* **STRING 8 - A global view on proteins and their functional interactions in 630 organisms.** Nucleic Acids Res. 2009;37 SUPPL. 1:D412. doi:10.1093/nar/gkn760.

Original STRING datasets were made available under the [*Creative Commons Attribution 4.0 International (CC BY 4.0)*](https://creativecommons.org/licenses/by/4.0/) license.


### Species - files

*Homo sapiens* - 9606.protein.links.detailed.v11.0  
*Saccharomyces cerevisiae* - 4932.protein.links.detailed.v11.0  
*Mus musculus* - 10090.protein.physical.links.v11.0  
*Rattus norvegicus* - 10116.protein.physical.links.v11.0  
*Sus scrofa* - 9823.protein.physical.links.v11.0  

## Post-processing

The following amendments were performed on the original files from STRING:
- \>=0.95 combined score links were used
- header was amended
- source and target proteins of the links with Ensembl protein ID (e.g. ENSP00000000412,ENSP00000221957) were exported to CSV files

## List of post-processed files
`./STRING/human.csv`  
`./STRING/yeast.csv`  
`./STRING/mouse.csv`  
`./STRING/rat.csv`  
`./STRING/pig.csv`  
