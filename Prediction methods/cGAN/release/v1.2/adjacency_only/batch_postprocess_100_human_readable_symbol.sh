#!/bin/bash

# command line arguments
output_dir=$1
symbol_dir=$2

# 5 interactomes n90
declare -a network_list=("yeast" "human" "pig" "rat" "mouse")
declare -a tax_list=("4932" "9606" "9823" "10116" "10090")

gan_readable_dir=$output_dir"human_readable_gan/"

mkdir -p "$gan_readable_dir"

# run the postprocess for 5 interactomes
for ((i=0; i<5; i++));
do
    network_name="${network_list[$i]}"
    tax_id="${tax_list[$i]}"
    for ((j=1; j<11; j++));
    do
        gan_file=$output_dir"gan500/"$network_name"_alt_results.csv"
        echo $gan_file
        nohup python3 -u postprocess_100_whole_symbol.py $output_dir $network_name $gan_file $tax_id $symbol_dir > $network_name"_postprocess_for_unique_gan.log" 2>&1 &
    done
done
