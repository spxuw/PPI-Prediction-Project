import os
import networkx as nx
import csv

import csv
import json 
import sys


class BiologicalFeatureExtractor():

	def __init__(self, sequence_file_path, network_dir_path,protein_similarity_file_path,output_dir_path,shingle_length = 20):
		self.sequence_file_path = sequence_file_path
		self.output_dir_path = output_dir_path

		self.network_dir_path = network_dir_path
		self.network_files = [file for file in os.listdir(self.network_dir_path) if file[0] != "." and "TRAINING" in file]
		
		if len(self.network_files) == 0:
			self.network_files = [file for file in os.listdir(self.network_dir_path) if file[0] != "." and "TEST" in file]



		self.validation_files = [file for file in os.listdir(self.network_dir_path) if file[0] != "." and "VALIDATION" in file]

		self.shingle_length = shingle_length

		self.protein_similarity_file_path = protein_similarity_file_path 



	def __load_map__ensembl_id__sequence__(self):
		
		map__ensembl_id__sequence = {}

		with open(self.sequence_file_path, 'r') as fp:
			map__ensembl_id__sequence = json.load(fp)

		return map__ensembl_id__sequence

	def __load_network__(self,network_file_path):
		
		G = nx.Graph()
		
		with open(network_file_path,'r') as fp:
			csv_reader = csv.reader(fp, delimiter = "\t")
			for index,row in enumerate(csv_reader):
				if index == 0:
					continue

				u = row[1]
				v = row[2]

				cl = int(row[0])

				if cl == 0:
					G.add_edge(u,v)
		
		V = set(G.nodes())

		return G,V

	def __load_similarity_db__(self):
		
		map__ensembl_id__similar_ensembls_ids = {}
		
		if os.path.exists(self.protein_similarity_file_path):
			with open(self.protein_similarity_file_path,'r') as fp:
				map__ensembl_id__similar_ensembls_ids = json.load(fp)

		else:
			map__ensembl_id__sequence = self.__load_map__ensembl_id__sequence__()
			map__ensembl_id__shingle_sequence = self.__create_shingles_for_each_protein__(map__ensembl_id__sequence)
			map__ensembl_id__similar_ensembls_ids = self.__compute_similarity_database__(map__ensembl_id__shingle_sequence)

		for ensembl_id, similar_ensembl_ids in map__ensembl_id__similar_ensembls_ids.items():
			map__ensembl_id__similar_ensembls_ids[ensembl_id] = set(similar_ensembl_ids)

		return map__ensembl_id__similar_ensembls_ids

	def __create_shingles_for_each_protein__(self,map__ensembl_id__sequence):

		map__ensembl_id__shingle_sequence = {}

		for ensembl_id, sequence in map__ensembl_id__sequence.items():
			
			shingle_sequence = [sequence[i : i + self.shingle_length ] for i in range(0,len(sequence)- self.shingle_length+1)]

			map__ensembl_id__shingle_sequence[ensembl_id] = set(shingle_sequence)


		return map__ensembl_id__shingle_sequence


	def __compute_protein_similarity__(self, u, v, map__ensembl_id__shingle_sequence):

		shingle_sequence_u = map__ensembl_id__shingle_sequence[u]
		shingle_sequence_v = map__ensembl_id__shingle_sequence[v]
		return len(shingle_sequence_u & shingle_sequence_v) > 0

	# Print iterations progress
	def __print_progress_bar__(self,iteration, total, prefix = '', suffix = '', decimals = 1, length = 100, fill = '█', printEnd = "\r"):
		"""
		Call in a loop to create terminal progress bar
		@params:
			iteration   - Required  : current iteration (Int)
			total       - Required  : total iterations (Int)
			prefix      - Optional  : prefix string (Str)
			suffix      - Optional  : suffix string (Str)
			decimals    - Optional  : positive number of decimals in percent complete (Int)
			length      - Optional  : character length of bar (Int)
			fill        - Optional  : bar fill character (Str)
			printEnd    - Optional  : end character (e.g. "\r", "\r\n") (Str)
		"""
		percent = ("{0:." + str(decimals) + "f}").format(100 * (iteration / float(total)))
		filledLength = int(length * iteration // total)
		bar = fill * filledLength + '-' * (length - filledLength)
		print('\r%s |%s| %s%% %s' % (prefix, bar, percent, suffix), end = printEnd)
		# Print New Line on Complete
		if iteration == total: 
			print()



	def __compute_similarity_database__(self,map__ensembl_id__shingle_sequence):
		
		ensembl_ids = map__ensembl_id__shingle_sequence.keys()
		map__ensembl_id__similar_ensembls_ids = {}

		number_of_total_iterations = len(ensembl_ids)**2
		current_iteration = 0

		already_seen_ensembl_ids_pair = set()
		
		for u in ensembl_ids:
			for v in ensembl_ids:
				
				self.__print_progress_bar__(iteration = current_iteration, total = number_of_total_iterations)
				current_iteration += 1

				current_pair = tuple(sorted([u,v]))

				if current_pair in already_seen_ensembl_ids_pair:
					continue

				already_seen_ensembl_ids_pair.add(current_pair)

				if self.__compute_protein_similarity__(u,v, map__ensembl_id__shingle_sequence):

					if u not in map__ensembl_id__similar_ensembls_ids:
						map__ensembl_id__similar_ensembls_ids[u] = set()

					if v not in map__ensembl_id__similar_ensembls_ids:
						map__ensembl_id__similar_ensembls_ids[v] = set()

					map__ensembl_id__similar_ensembls_ids[u].add(v)
					map__ensembl_id__similar_ensembls_ids[v].add(u)


		for ensembl_id, similar_proteins in map__ensembl_id__similar_ensembls_ids.items():
			map__ensembl_id__similar_ensembls_ids[ensembl_id] = list(similar_proteins)

		with open(self.protein_similarity_file_path, "w") as fp:
					
			json.dump(map__ensembl_id__similar_ensembls_ids, fp)

		return map__ensembl_id__similar_ensembls_ids

	def __retrieve_neighborurs_similar_to_simalar_proteins__(self, map__ensembl_id__ensembl_ids,G):

		map__protein__neighbours_of_similar_proteins = {}
		
		for protein_u in map__ensembl_id__ensembl_ids:

			total_neighbours_of_protein_similar_to_u = set()					
			proteins_similar_to_protein_u = map__ensembl_id__ensembl_ids[protein_u]

			for ensembl_id in proteins_similar_to_protein_u:
				if ensembl_id not in G:
					continue
						
				neighbours_of_protein_similar_to_u = G[ensembl_id]
			
				total_neighbours_of_protein_similar_to_u.update(neighbours_of_protein_similar_to_u)

			map__protein__neighbours_of_similar_proteins[protein_u] = total_neighbours_of_protein_similar_to_u			
					
		return map__protein__neighbours_of_similar_proteins


	def __compute_PIPE_similarity_score__(self, map__ensembl_id__similar_ensembls_ids):

		for network_file_path in self.network_files:
			
			print()
			print(network_file_path)
			print()

			print("Loading Network...")
			print()
			G,V = self.__load_network__(self.network_dir_path + "/" + network_file_path)
			map__u_v__score = {}			
			
			number_of_total_iterations = len(map__ensembl_id__similar_ensembls_ids.keys())
			current_iteration = 0

			already_seen_pairs = set()
			map__protein__neighbours_of_similar_proteins = self.__retrieve_neighborurs_similar_to_simalar_proteins__(map__ensembl_id__similar_ensembls_ids,G)

			print("Computing Protein Interaction Score...")
			print()

			for u in V:
				self.__print_progress_bar__(iteration = current_iteration, total = number_of_total_iterations)
				current_iteration += 1
				
				if u in map__ensembl_id__similar_ensembls_ids:
					
					total_neighbours_of_protein_similar_to_u = map__protein__neighbours_of_similar_proteins[u]
					protein_similar_to_u = map__ensembl_id__similar_ensembls_ids[u]
					
					for v in V:

						if v in map__ensembl_id__similar_ensembls_ids:
							
							
							
							protein_similar_to_v = set()
							current_pair = tuple(sorted([u,v]))

							if current_pair in already_seen_pairs:
								continue

							already_seen_pairs.add(current_pair)

							protein_similar_to_v = map__ensembl_id__similar_ensembls_ids[v]
							total_neighbours_of_protein_similar_to_v = map__protein__neighbours_of_similar_proteins[v]

							intersection_size_u_to_v = len(total_neighbours_of_protein_similar_to_u & protein_similar_to_v)
							intersection_size_v_to_u = len(total_neighbours_of_protein_similar_to_v & protein_similar_to_u)

							max_intersection = max(intersection_size_v_to_u,intersection_size_u_to_v)
							min_intersection = min(intersection_size_v_to_u,intersection_size_u_to_v)

							map__u_v__score[current_pair] = (max_intersection,min_intersection)

			print()
			print("Saving Final Data Frame...")
			print()
			self.__save_data_frame__(self.network_dir_path + "/" + network_file_path,self.output_dir_path + "/"+ network_file_path,map__u_v__score)


	
	def __save_validation_files__(self,):

		for validation_file in self.validation_files:

			validation_file_path = self.network_dir_path + "/" + validation_file
			output_validation_file_path = self.output_dir_path + "/" + validation_file
			validation_pairs = []
			
			with open(validation_file_path,'r') as fp:
				csv_reader = csv.reader(fp,delimiter = "\t")

				for row in csv_reader:
					validation_pairs.append(row)

			with open(output_validation_file_path, "w") as fw:
				csv_writer = csv.writer(fw, delimiter = "\t")
				csv_writer.writerows(validation_pairs)


							

	def __save_data_frame__(self,network_file_path, output_file_path,map__u_v__score):

		with open(output_file_path,'w') as fw:

			csv_writer = csv.writer(fw, delimiter="\t")

			with open(network_file_path,'r') as fp:
				
				csv_reader = csv.reader(fp, delimiter = "\t")
				header_length = 0
				record_length = 0
				for index,row in enumerate(csv_reader):
					
					if index == 0:
						record = row
						header_length = len(record)
						
						record.append("shingle_sim_max")
						record.append("shingle_sim_min")


						csv_writer.writerow(record)
						continue

					u = row[1]
					v = row[2]

					sorted_tuple = tuple(sorted([u,v]))
					
					record = row[:-1]
					record_length = len(record)

					assert header_length == record_length
					
					if sorted_tuple in map__u_v__score:
						


						PIPE_max = map__u_v__score[sorted_tuple][0]
						PIPE_min = map__u_v__score[sorted_tuple][1]

						record.append(PIPE_max)
						record.append(PIPE_min)

						csv_writer.writerow(record)

					else:
						
						record.append(0)
						record.append(0)

						csv_writer.writerow(record)



	def run(self,):
		
		map__ensembl_id__similar_ensembls_ids = self.__load_similarity_db__()
		self.__compute_PIPE_similarity_score__(map__ensembl_id__similar_ensembls_ids)
		self.__save_validation_files__()



if __name__ == '__main__':

	if len(sys.argv) != 3:
		
		print('Wrong Usage')
		print()
		print("Correct Usage:")
		print("$ python3 biological_feature_extractor.py <directory_that_contains_all_the_10_TRAINING_and_VALIDATION_sets> <output_EXISTING_directory_that_will_contain_all_the_10_TRAINING_and_VALIDATION_sets_INTEGRATED_WITH_BIOLOGICAL_FEATURES>")
		print()
		print("Program is exit")
		exit(1)

	network_dir_path = sys.argv[1]
	output_dir_path = sys.argv[2]
	
	bfe = BiologicalFeatureExtractor( 
				
				sequence_file_path = "../../datasets/ensembl_id_to_protein_sequence.json",
				network_dir_path = network_dir_path,
				protein_similarity_file_path ="../../datasets/similar_proteins.json",
				output_dir_path = output_dir_path
		)

	bfe.run()


