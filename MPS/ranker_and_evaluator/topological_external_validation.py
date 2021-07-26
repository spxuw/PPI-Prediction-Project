import pprint as pp
import csv
import os
import sys

from TwoFeaturesUnsupervisedRanker import TwoFeaturesUnsupervisedRanker

def __choose_classifier__(classifier_name, training_file_path, considered_features):


	if classifier_name == "TwoFeaturesUnsupervisedRanker":


		two_features_unsupervised_ranker = TwoFeaturesUnsupervisedRanker(
			classifier_name,
			training_file_path,
			considered_features[0],
			considered_features[1],
			False
			)
		
		return two_features_unsupervised_ranker


def __compute_compute_edge_to_discover__(classifier_name,considered_features,directory_path,output_dir_path):

	ppi_files = [file for file in os.listdir(directory_path) if file[0] != "."]

	for file in ppi_files:

		ranker = __choose_classifier__(classifier_name, directory_path +"/"+ file , considered_features)
		unlabeled_pairs = ranker.predict()

		csv_writer = csv.writer(open(output_dir_path + "/" +  file,'w'), delimiter = ",",quotechar='"', quoting=csv.QUOTE_NONE)
		
		csv_writer.writerow(["u","v","Score"])
		for item in unlabeled_pairs:
			csv_writer.writerow(item[:3])

def external_validation(ranker, features,PPI_directory_path,output_dir_path):
	

	__compute_compute_edge_to_discover__(
		classifier_name = ranker["name"],
		considered_features = features,
		directory_path =PPI_directory_path,
		output_dir_path = output_dir_path)

##################################################################################################
if __name__ == '__main__':

	if len(sys.argv) != 3:
		
		print("Wrong Usage")
		print()
		print("Correct Usage:")
		print("python3 topological_external_validation.py <directory_that_contains_the_ENTIRE_TEST_set> <output_EXISTING_directory_that_will_contain_the_FIRST_500_PREDICTED_NON_INTERACTING_PROTEIN_PAIRS>")
		print()
		print("Program is Closing....")
		exit(1)

	ppi_dir_path = sys.argv[1]
	output_dir_path = sys.argv[2]

	features = ['sim__jac__MAX__sum','Preferential_Attachment']


	ranker = {
		'name': 'TwoFeaturesUnsupervisedRanker',
		'parameters' : {}
		}

	external_validation(
		ranker = ranker,
		features = features,
		PPI_directory_path = ppi_dir_path,
		output_dir_path = output_dir_path
	)
	