import sys
import pandas as pd
import glob
import warnings
warnings.filterwarnings('ignore')

# import the edge list
output_dir = sys.argv[1]
network_name = sys.argv[2]
gan_filename = sys.argv[3]
tax_id = sys.argv[4]
symbol_dir = sys.argv[5]

id_data = glob.glob(output_dir+'network/'+str(network_name)+'/'+str(network_name)+'_edgelist_conv_id.csv')
id_data = pd.read_csv(id_data[0], sep = '\t')
# get int:str ID dictionary
id_dict = dict(zip(id_data.int_node_id, id_data.str_node_id))

symbol_data = glob.glob(symbol_dir+str(tax_id)+'.protein.info.v11.5.txt')
symbol_data = pd.read_csv(symbol_data[0], sep = '\t')
symbol_data = symbol_data.rename(columns={'#string_protein_id': 'ensembl'})
to_replace = str('^'+tax_id+'.')
symbol_data['ensembl'] = symbol_data['ensembl'].str.replace(to_replace, '')
symbol_dict = dict(zip(symbol_data.ensembl, symbol_data.preferred_name))

# edgelist_n100 = glob.glob(output_dir+'network/'+str(network_name)+'/'+str(network_name)+'_n100.edgelist')
# edgelist_n100 = pd.read_csv(edgelist_n100[0], header=None, names=['Sources', 'Targets'], sep = " ")
#
# top_x_num = len(edgelist_n100)-(len(edgelist_n100)*0.9)

gan_file = open(gan_filename, 'r')
gan_lines = gan_file.readlines()

edge_map = {}
# create edge id dictionary from gan output
for line in gan_lines:
    edge_id_end_pos = line.index('\'', 2)
    node_indices = line[2:edge_id_end_pos].split('_')

	# # Ignore self loops
    # if (node_indices[0] == node_indices[1]):
	#     continue

    edge_id = node_indices[0] + "_" + node_indices[1] if node_indices[0] < node_indices[1] else node_indices[1] + "_" + node_indices[0]

    confidence_start_pos = line.rfind('[') + 1
    confidence = float(line[confidence_start_pos:-2])

    if edge_id in edge_map:
        edge_map[edge_id]["conf"].append(confidence)
    else:
        edge_map[edge_id] = {
            "id": edge_id,
            "conf": [ confidence ],
            "source": [ node_indices[0] ],
            "target": [ node_indices[1] ]
        }


scores_list = []
source_list = []
target_list = []

for edge_id in edge_map:
    node_indices = edge_id.split('_')
    source = id_dict[int(node_indices[0])]
    target = id_dict[int(node_indices[1])]

    source = symbol_dict[source]
    target = symbol_dict[target]

    source_list.append(source)
    target_list.append(target)
    scores_list.append(max(edge_map[edge_id]["conf"]))

whole_dict = {'Sources':source_list, 'Targets':target_list, 'Score':scores_list}
whole_df = pd.DataFrame(whole_dict)
sorted_df = whole_df.sort_values(by='Score', ascending=False)
sorted_df.to_csv(output_dir+'human_readable_gan/'+network_name+'_human_readable_scores_symbol.csv', index=False)

# topX = sorted_df.head(int(top_x_num))
# topX.to_csv(output_dir+'human_readable_gan/'+network_name+'_human_readable_scores_topx.csv', sep = "\t", index=False, header=['Sources', 'Targets', 'Scores'])
