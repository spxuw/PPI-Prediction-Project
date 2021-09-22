from UnsupervisedRanker import UnsupervisedRanker
import csv
import pdb


class TwoFeaturesUnsupervisedRanker(UnsupervisedRanker):

    def __init__(self, ranker_name, file_name_of_all_node_id_couples_to_rank_represented_as_vectors, name_feature_1,
                 name_feature_2, is_bio_top):
        UnsupervisedRanker.__init__(self, ranker_name, file_name_of_all_node_id_couples_to_rank_represented_as_vectors)
        #
        self.name_feature_1 = name_feature_1
        self.name_feature_2 = name_feature_2
        self.is_bio_top = is_bio_top
        #
        return

    def rank(self):
        #
        input_file = open(self.file_name_of_all_node_id_couples_to_rank_represented_as_vectors, 'r', 10 ** 6)
        csv_reader = csv.reader(input_file, delimiter='\t', quotechar='"', quoting=csv.QUOTE_NONE)
        #
        header = next(csv_reader)
        index_feature_1 = -1
        index_feature_2 = -1


        for index, field_name in enumerate(header):
            if field_name == self.name_feature_1:
                index_feature_1 = index
            if field_name == self.name_feature_2:
                index_feature_2 = index

        #
        # replace with exceptions...
        if index_feature_1 < 0 or index_feature_2 < 0:

            return None
        #
        list__u__v__feature_1__feature_2 = []

        max_value_feature_1 = 0.0
        max_value_feature_2 = 0.0

        for record in csv_reader:
            #
            # is the couple already edged in the training graph?
            if record[0] == "0":
                # print("the couple already edged in the training graph!!!")
                continue
            #
            u = record[1]
            v = record[2]

            value_feature_1 = float(record[index_feature_1])
            value_feature_2 = float(record[index_feature_2])

            if max_value_feature_1 < value_feature_1:
                max_value_feature_1 = value_feature_1

            if max_value_feature_2 < value_feature_2:
                max_value_feature_2 = value_feature_2

            if self.is_bio_top:

                value_feature_3 = float(record[4])
                list__u__v__feature_1__feature_2.append((u, v, value_feature_1, value_feature_2,value_feature_3))

            else:

                list__u__v__feature_1__feature_2.append((u, v, value_feature_1, value_feature_2))

        input_file.close()

        # far ricontrollare ad Adriano
        if self.is_bio_top:
            list__u__v__feature_1__feature_2.sort(key= lambda x: (x[-3]/max_value_feature_1 + x[-2]/max_value_feature_2, x[-1]), reverse=True)
        else:
            list__u__v__feature_1__feature_2.sort(key=lambda x: (x[-2], x[-1]), reverse=True)
        #
        return list__u__v__feature_1__feature_2


    def predict(self,size = 500):
        return self.rank()[:size]
