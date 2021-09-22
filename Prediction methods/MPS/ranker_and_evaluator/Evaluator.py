import math
import csv
import pprint as pp
from sklearn import metrics


class Evaluator():

    def __init__(self, file_name_of_all_node_id_couples_belonging_to_the_validation_set):
        self.file_name_of_all_node_id_couples_belonging_to_the_validation_set = file_name_of_all_node_id_couples_belonging_to_the_validation_set
        return

    def __is_a_TRUE_POSITIVE__(self, node_couple, validation_set_of_node_id_couples):
        # print("node_couple", node_couple)
        return (node_couple in validation_set_of_node_id_couples) or (
                (node_couple[1], node_couple[0]) in validation_set_of_node_id_couples)

    def __compute_precision_at_k__(self, predicted_rank_of_node_id_couples, validation_set_of_node_id_couples, k=500):
        """
        ###
        """
        num_TRUE_POSITIVES_at_k = 0
        #
        for i in range(min(k, len(predicted_rank_of_node_id_couples))):
            #
            current_predicted_node_couple = predicted_rank_of_node_id_couples[i][:2]
            #
            if self.__is_a_TRUE_POSITIVE__(current_predicted_node_couple, validation_set_of_node_id_couples):
                num_TRUE_POSITIVES_at_k += 1
            #
        num_TRUE_POSITIVES_at_k = float(num_TRUE_POSITIVES_at_k)
        return num_TRUE_POSITIVES_at_k / min(len(validation_set_of_node_id_couples), k)

    def __compute_IDCG__(self, size_of_validation_set, base=2):
        """
        ###
        """
        IDCG = 0.
        #
        for i in range(size_of_validation_set):
            IDCG += 1. / math.log(i + base, base)
        #
        return IDCG

    def __compute_nDCG__(self, predicted_rank_of_node_id_couples, validation_set_of_node_id_couples, base=2):
        """
        ###
        """
        DCG = 0.
        #
        IDCG = self.__compute_IDCG__(len(validation_set_of_node_id_couples))
        #
        for index, current_predicted_node_id_couple in enumerate(predicted_rank_of_node_id_couples):
            #
            current_predicted_node_id_couple = current_predicted_node_id_couple[:2]
            #
            if self.__is_a_TRUE_POSITIVE__(current_predicted_node_id_couple, validation_set_of_node_id_couples):
                DCG += 1. / math.log(index + base, base)
        #
        return DCG / IDCG

    def __compute_ROC_and_PR_AUCs__(self, predicted_rank_of_node_id_couples, validation_set_of_node_id_couples):
        """
        ###
        """
        roc_auc = 0.
        pr_auc = 0.
        #
        num_TP_so_far = 0
        num_FP_so_far = 0
        n = 0
        #
        num_POSITIVES = len(validation_set_of_node_id_couples)
        num_NEGATIVES = len(predicted_rank_of_node_id_couples) - len(validation_set_of_node_id_couples)
        #
        # set__FP_rate__TP_rate = set()
        # set__RECALL__PRECISION = set()
        list__FP_rate__TP_rate = []
        list__RECALL__PRECISION = []
        for index, current_predicted_node_id_couple in enumerate(predicted_rank_of_node_id_couples):
            #
            n = index + 1
            #
            current_predicted_node_id_couple = current_predicted_node_id_couple[:2]
            #
            if self.__is_a_TRUE_POSITIVE__(current_predicted_node_id_couple, validation_set_of_node_id_couples):
                num_TP_so_far += 1
            else:
                num_FP_so_far += 1
            #
            # set__FP_rate__TP_rate.add((num_FP_so_far / num_NEGATIVES, num_TP_so_far / num_POSITIVES))
            # set__RECALL__PRECISION.add((num_TP_so_far / num_POSITIVES, num_TP_so_far / n))
            list__FP_rate__TP_rate.append((num_FP_so_far / num_NEGATIVES, num_TP_so_far / num_POSITIVES))
            list__RECALL__PRECISION.append((num_TP_so_far / num_POSITIVES, num_TP_so_far / n))
            #
        # print()
        # print("num_TP_so_far ", num_TP_so_far)
        # print("num_FP_so_far ", num_FP_so_far)
        # print("num_NEGATIVES ", num_NEGATIVES)
        # print("num_POSITIVES ", num_POSITIVES)
        # print("n             ", n)
        # print()
        # Sorting...
        # list__FP_rate__TP_rate = list(set__FP_rate__TP_rate)
        list__FP_rate__TP_rate.sort(key=lambda x: (x[0], x[1]))
        # list__RECALL__PRECISION = list(set__RECALL__PRECISION)
        list__RECALL__PRECISION.sort(key=lambda x: (x[0], x[1]))
        # print()
        # print("dfresgf")
        # pp.pprint(set__RECALL__PRECISION)
        print()
        # pp.pprint(set__FP_rate__TP_rate)
        print()
        #
        list__fpr = [fpr for fpr, tpr in list__FP_rate__TP_rate]
        list__tpr = [tpr for fpr, tpr in list__FP_rate__TP_rate]
        roc_auc = metrics.auc(list__fpr, list__tpr)
        #
        list__recall = [recall for recall, precision in list__RECALL__PRECISION]
        list__precision = [precision for recall, precision in list__RECALL__PRECISION]
        pr_auc = metrics.auc(list__recall, list__precision)
        #
        return roc_auc, pr_auc

    def __extract_the_validation_set_from_file__(self):
        #
        validation_set__u_v = set()
        #
        input_file = open(self.file_name_of_all_node_id_couples_belonging_to_the_validation_set, 'r', 10 ** 6)
        csv_reader = csv.reader(input_file, delimiter='\t', quotechar='"', quoting=csv.QUOTE_NONE)
        #
        header = next(csv_reader)
        for record in csv_reader:
            validation_set__u_v.add((record[-2], record[-1]))
            ###self.validation_set__u_v__v_u.add((record[-1], record[-2]))
        input_file.close()
        #
        return validation_set__u_v

    def compute_metrics(self, predicted_rank_of_node_id_couples):
        """
        The methods computes all the evaluation metrics requested by the challenge.
        :param predicted_rank_of_node_id_couples: predicted rank represented as a LIST of (u,v) node id pairs (with u<v): LIST of TUPLES of size >= 2 where u and v are in the first two positions.
        :return: dictionary containing all the evaluation metrics requested by the challenge.
        """
        #
        validation_set_of_node_id_couples = self.__extract_the_validation_set_from_file__()
        #
        precision = self.__compute_precision_at_k__(predicted_rank_of_node_id_couples,
                                                    validation_set_of_node_id_couples, k=500)
        NDCG = self.__compute_nDCG__(predicted_rank_of_node_id_couples, validation_set_of_node_id_couples)
        #
        roc_auc, pr_auc = self.__compute_ROC_and_PR_AUCs__(predicted_rank_of_node_id_couples,
                                                           validation_set_of_node_id_couples)
        #
        map__metric__score = {'Precision@500': precision, 'nDCG': NDCG, 'AUROC': roc_auc, 'AUPRC': pr_auc}
        #
        return map__metric__score

    def compute_only_metrics(self, predicted_rank_of_node_id_couples):
        """
        The methods computes all the evaluation metrics requested by the challenge.
        :param predicted_rank_of_node_id_couples: predicted rank represented as a LIST of (u,v) node id pairs (with u<v): LIST of TUPLES of size >= 2 where u and v are in the first two positions.
        :return: dictionary containing all the evaluation metrics requested by the challenge.
        """
        #
        validation_set_of_node_id_couples = self.__extract_the_validation_set_from_file__()
        #
        precision = self.__compute_precision_at_k__(predicted_rank_of_node_id_couples,
                                                    validation_set_of_node_id_couples, k=500)
        NDCG = self.__compute_nDCG__(predicted_rank_of_node_id_couples, validation_set_of_node_id_couples)
        #
        #
        map__metric__score = {'Precision@500': precision, 'nDCG': NDCG, 'AUROC': 0.0, 'AUPRC': 0.0}
        #
        return map__metric__score