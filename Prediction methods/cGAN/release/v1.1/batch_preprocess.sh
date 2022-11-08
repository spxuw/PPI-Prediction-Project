#!/bin/bash

# command line arguments
input_dir=$1
output_dir=$2
node2vec_path=$3

# 5 interactomes n90
declare -a network_list=("Yeast" "HuRI" "SyntheticPPI" "Arabidopsis" "Celegans")

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

    runtime_filename="$network_output_dir/$network_name/runtimes.txt"

    for edgelist_path in $network_output_dir/$network_name/*.edgelist;
    do
        start_time=`date +%s`
        whole_input_path=$(realpath $edgelist_path)
        whole_output_path="${whole_input_path%.*}.emb"
        output_filemame=$(basename $whole_output_path)
    	$node2vec_path -i:$whole_input_path -o:$whole_output_path -l:80 -d:32 -p:0.25 -q:0.25 -r:10 -v -k:10 -e:5
        sed '1d' ${whole_input_path%.*}.emb > ${whole_input_path%.*}_truncated.emb
        end_time=`date +%s`
        runtime=$((end_time - start_time))
        echo -e "$output_filemame\t$runtime" >> $runtime_filename

    	edgelist_filename=$(basename $edgelist_path)
    	if [[ "$edgelist_filename" == *"n100"* ]];
        then
    		species="$( cut -d '_' -f 1 <<< "$edgelist_filename" )";

    		for ((j=1; j<=10; j++)); do
                json_path="$output_dir/json/$species$j.json"
       			cp template_input.json $json_path
    			sed -i 's,|1|,'$network_output_dir'/'$network_name'/'$species'_n100.edgelist,g' $json_path
    			sed -i 's,|2|,'$network_output_dir'/'$network_name'/'$species'_n90_train_kf_'$j'.edgelist,g' $json_path
    			sed -i 's,|4|,'$network_output_dir'/'$network_name'/'$species'_n90_modules_kf_'$j'.csv,g' $json_path
    			sed -i 's,|5|,'$network_output_dir'/'$network_name'/'$species'_n81_train_kf_'$j'.edgelist,g' $json_path
                sed -i 's,|3|,'$network_output_dir'/'$network_name'/'$species'_n90_train_kf_'$j'_truncated.emb,g' $json_path
    			sed -i 's,|6|,'$network_output_dir'/'$network_name'/'$species'_n81_train_kf_'$j'_truncated.emb,g' $json_path
    			sed -i 's,|7|,'$network_output_dir'/'$network_name'/'$species'_n81_modules_kf_'$j'.csv,g' $json_path
    			sed -i 's,|8|,'$output_dir'/gan/,g' $json_path
    			sed -i 's,|9|,'$species'_kf_'$j',g' $json_path
    		done
    	fi
    done
done
