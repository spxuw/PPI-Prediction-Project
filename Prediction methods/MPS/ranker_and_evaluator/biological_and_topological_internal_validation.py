import pprint as pp
import csv
import os
import time
import sys

from TwoFeaturesUnsupervisedRanker import TwoFeaturesUnsupervisedRanker
from Evaluator import Evaluator


def __get_training_validation_files__(dir_path):
	
	training_files = [file for file in os.listdir(dir_path) if file[0] != "." and "TRAINING" in file]
	validation_files = [file for file in os.listdir(dir_path) if file[0] != "." and "VALIDATION" in file]

	training_files.sort()
	validation_files.sort()

	return training_files, validation_files


def __choose_classifier__(classifier_name, classifier_parameters, training_file_path, considered_features):


	if classifier_name == "TwoFeaturesUnsupervisedRanker":


		two_features_unsupervised_ranker = TwoFeaturesUnsupervisedRanker(
			classifier_name,
			training_file_path,
			considered_features[0],
			considered_features[1],
			True
			)

		return two_features_unsupervised_ranker


def __compute_cross_validation__(
	
	classifier_name,
	classifier_parameters,
	considered_features,
	directory_path,
	number_of_fold = 10):

	training_files, validation_files = __get_training_validation_files__(directory_path)

	cross_validation_metric_value = []
	iteration_number_of_fold = 0

	for training, validation in zip(training_files,validation_files):
		iteration_number_of_fold += 1
		print()
		print("Current Iteration:", iteration_number_of_fold)
		print()
		print(training,validation)
		print()

		t0 = time.time()

		ranker = __choose_classifier__(classifier_name, classifier_parameters, directory_path + "/" +training , considered_features)

		eval = Evaluator(directory_path + "/" + validation)
		print("Computing ranking....")
		print()
		predicted_rank_of_node_id_couples__c_ranker = ranker.rank()
		t1 = time.time()

		computational_time = t1 - t0

		print("Computing metrics (AUROC, AUPRC, Precision@500, nDCG)....")
		print()
		map__metric__score = eval.compute_metrics(predicted_rank_of_node_id_couples__c_ranker)


		cross_validation_metric_value.append([map__metric__score['AUROC'],map__metric__score['AUPRC'],map__metric__score['Precision@500'],map__metric__score['nDCG'],computational_time])
	


		if iteration_number_of_fold == number_of_fold:
			break

	return cross_validation_metric_value



def internal_validation(ranker, features,ppi_training_validation_dir_path,output_dir_path,ppi_name):

	cross_validation_results = [] 

	cross_validation_results = __compute_cross_validation__(
					
		classifier_name = ranker["name"],
		classifier_parameters = ranker["parameters"], 
		considered_features = features,
		directory_path = ppi_training_validation_dir_path
					
	)

	csv_writer = csv.writer(open(output_dir_path + "/" + ppi_name + ".csv",'w'), delimiter = ",",quotechar='"', quoting=csv.QUOTE_NONE)
			
	csv_writer.writerow(["AUROC","AUPRC","P_500","NDCG","Computing_time"])
	csv_writer.writerows(cross_validation_results)


##################################################################################################
if __name__ == '__main__':

	if len(sys.argv) != 4:
		
		print("Wrong Usage")
		print()
		print("Correct Usage:")
		print("python3 topological_internal_validation.py <directory_that_contains_all_the_10_TRAINING_and_VALIDATION_sets> <input_PPI_file_name> <output_EXISTING_directory_that_will_contain_the_CROSS_VALIDATION_RESULTS_FILE>")
		print()
		print("Program is Closing....")
		exit(1)

	ppi_training_validation_dir_path = sys.argv[1]
	ppi_name = sys.argv[2]
	output_dir_path = sys.argv[3]


	features = ['shingle_sim_max','sim__jac__MAX__sum']

	ranker = {
			'name': 'TwoFeaturesUnsupervisedRanker',
			'parameters' : {}
		}



	internal_validation(
		features = features, 
		ranker = ranker,
		ppi_training_validation_dir_path = ppi_training_validation_dir_path,
		output_dir_path = output_dir_path,
		ppi_name = ppi_name
		)




