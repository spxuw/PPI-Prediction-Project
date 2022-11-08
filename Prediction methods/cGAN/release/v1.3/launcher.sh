#!/bin/bash

# command line arguments
version=$1
type=$2
input_dir=$3
output_dir=$4
node2vec_path=${5:-default value}

for network_path in $input_dir/*.csv;
do
  network_filename=$(basename $network_path)
  network_name=${network_filename%.*}
  mkdir -p "$output_dir/$network_name/network"
  mkdir -p "$output_dir/$network_name/json"
  mkdir -p "$output_dir/$network_name/gan"
  mkdir -p "$output_dir/$network_name/metrics"

  if [[ "$version" == cgan1 ]];
  then
    python3 -u ./v1.0/preprocess.py $network_name $network_path $output_dir/$network_name/network/
    runtime_filename="$output_dir/$network_name/network/runtimes.txt"
    for edgelist_path in $output_dir/$network_name/network/*.edgelist;
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
        for ((j=1; j<=10; j++));
        do
          json_path="$output_dir/$network_name/json/"$network_name"_$j.json"
          cp ./v1.0/template_input.json $json_path
          sed -i 's,|1|,'$output_dir'/'$network_name'/network/'$network_name'_n100.edgelist,g' $json_path
          sed -i 's,|10|,'$output_dir'/'$network_name'/network/'$network_name'_n100_truncated.emb,g' $json_path
          sed -i 's,|11|,'$output_dir'/'$network_name'/network/'$network_name'_n100_modules.csv,g' $json_path
          sed -i 's,|2|,'$output_dir'/'$network_name'/network/'$network_name'_n90_train_kf_'$j'.edgelist,g' $json_path
          sed -i 's,|3|,'$output_dir'/'$network_name'/network/'$network_name'_n90_train_kf_'$j'_truncated.emb,g' $json_path
          sed -i 's,|4|,'$output_dir'/'$network_name'/network/'$network_name'_n90_modules_kf_'$j'.csv,g' $json_path
          sed -i 's,|5|,'$output_dir'/'$network_name'/network/'$network_name'_n81_train_kf_'$j'.edgelist,g' $json_path
          sed -i 's,|6|,'$output_dir'/'$network_name'/network/'$network_name'_n81_train_kf_'$j'_truncated.emb,g' $json_path
          sed -i 's,|7|,'$output_dir'/'$network_name'/network/'$network_name'_n81_modules_kf_'$j'.csv,g' $json_path
          sed -i 's,|8|,'$output_dir'/'$network_name'/gan/,g' $json_path
          sed -i 's,|9|,'$network_name'_kf_'$j',g' $json_path
        done
      fi
    done
  elif [[ "$version" == cgan2 ]];
  then
    python3 -u ./v1.2/preprocess.py $network_name $network_path $output_dir/$network_name/network/
    for edgelist_path in $output_dir/$network_name/network/*.edgelist;
    do
      edgelist_filename=$(basename $edgelist_path)
      if [[ "$edgelist_filename" == *"n100"* ]];
      then
        for ((j=1; j<=10; j++));
        do
          json_path="$output_dir/$network_name/json/"$network_name"_$j.json"
          cp ./v1.2/template_input.json $json_path
          sed -i 's,|1|,'$output_dir'/'$network_name'/network/'$network_name'_n100.edgelist,g' $json_path
          sed -i 's,|8|,'$output_dir'/'$network_name'/network/'$network_name'_n100_modules.csv,g' $json_path
          sed -i 's,|2|,'$output_dir'/'$network_name'/network/'$network_name'_n90_train_kf_'$j'.edgelist,g' $json_path
          sed -i 's,|3|,'$output_dir'/'$network_name'/network/'$network_name'_n90_modules_kf_'$j'.csv,g' $json_path
          sed -i 's,|4|,'$output_dir'/'$network_name'/network/'$network_name'_n81_train_kf_'$j'.edgelist,g' $json_path
          sed -i 's,|5|,'$output_dir'/'$network_name'/network/'$network_name'_n81_modules_kf_'$j'.csv,g' $json_path
          sed -i 's,|6|,'$output_dir'/'$network_name'/gan/,g' $json_path
          sed -i 's,|7|,'$network_name'_kf_'$j',g' $json_path
        done
      fi
    done
  fi
  if [[ "$type" == cross_val ]];
  then
    for json_path in $output_dir/$network_name/json/*.json;
    do
      start_time=`date +%s`
      json_filename=$(basename $json_path)
      json_filename_wext=${json_filename%.*}
      if [[ "$version" == cgan1 ]];
      then
        python3 -u ./v1.0/gan_90.py $json_path
      elif [[ "$version" == cgan2 ]];
      then
        python3 -u ./v1.2/gan_90.py $json_path
      fi
      end_time=`date +%s`
      runtime=$((end_time - start_time))
      echo $runtime > "$output_dir/$network_name/gan/$json_filename_wext.log"
    done
  elif [[ "$type" == exp_val ]];
  then
    if [[ "$version" == cgan1 ]];
    then
      python3 -u ./v1.0/gan_100.py $output_dir/$network_name/json/"$network_name"_1.json
    elif [[ "$version" == cgan2 ]];
    then
      python3 -u ./v1.2/gan_100.py $output_dir/$network_name/json/"$network_name"_1.json
    fi
  fi
  if [[ "$type" == cross_val ]];
  then
    python3 -u ./postprocess_90.py $network_name $output_dir/$network_name/ $output_dir/$network_name/network/ $output_dir/$network_name/metrics/
  elif [[ "$type" == exp_val ]];
  then
    python3 -u ./postprocess_100.py $output_dir/ $network_name $output_dir/$network_name/gan/"$network_name"_kf_1_results.csv
  fi
done
