#!/bin/bash

# command line arguments
output_dir=$1

# 5 interactomes n90
declare -a network_list=("Yeast" "HuRI" "SyntheticPPI" "Arabidopsis" "Celegans")

metrics_dir="$output_dir/metrics/"

mkdir -p "$metrics_dir"

# run the postprocess for 5 interactomes
for ((i=0; i<5; i++));
do
    network_name="${network_list[$i]}"
    network_output_dir="$output_dir/network/$network_name/"
    nohup python3 -u postprocess_90.py $network_name $output_dir $network_output_dir $metrics_dir > $network_name"_postprocess.log" 2>&1 &
done
