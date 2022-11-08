#!/bin/bash

# command line arguments
input_dir=$1
output_dir=$2

# 5 interactomes n90
declare -a network_list=("yeast" "human" "pig" "rat" "mouse")

network_output_dir="$output_dir/network"

mkdir -p "$network_output_dir"
mkdir -p "$output_dir/json"
mkdir -p "$output_dir/gan"

# run the link prediction of internal validation for 5 interactomes
for ((i=0; i<5; i++));
do
    network_name="${network_list[$i]}"
    mkdir -p $network_output_dir/$network_name
    python3 -u preprocess.py $network_name $input_dir/$network_name.csv $network_output_dir/$network_name/

    for edgelist_path in $network_output_dir/$network_name/*.edgelist;
    do
    	edgelist_filename=$(basename $edgelist_path)
    	if [[ "$edgelist_filename" == *"n100"* ]];
        then
    		species="$( cut -d '_' -f 1 <<< "$edgelist_filename" )";

    		for ((j=1; j<=10; j++)); do
                json_path="$output_dir/json/$species$j.json"
       			cp template_input_adj_only.json $json_path
    			sed -i 's,|1|,'$network_output_dir'/'$network_name'/'$species'_n100.edgelist,g' $json_path
    			sed -i 's,|2|,'$network_output_dir'/'$network_name'/'$species'_n90_train_kf_'$j'.edgelist,g' $json_path
    			sed -i 's,|3|,'$network_output_dir'/'$network_name'/'$species'_n90_modules_kf_'$j'.csv,g' $json_path
    			sed -i 's,|4|,'$network_output_dir'/'$network_name'/'$species'_n81_train_kf_'$j'.edgelist,g' $json_path
    			sed -i 's,|5|,'$network_output_dir'/'$network_name'/'$species'_n81_modules_kf_'$j'.csv,g' $json_path
    			sed -i 's,|6|,'$output_dir'/gan/,g' $json_path
    			sed -i 's,|7|,'$species'_kf_'$j',g' $json_path
    		done
    	fi
    done
done
