#!/bin/bash

output_dir=$1

for json_path in $output_dir/json/*.json;
do
	start_time=`date +%s`
	json_filename=$(basename $json_path)
	json_filename_wext=${json_filename%.*}
	python3 -u gan_90.py $json_filename
	end_time=`date +%s`
	runtime=$((end_time - start_time))
	echo $runtime > "$output_dir/gan/$json_filename_wext.log" 
done


