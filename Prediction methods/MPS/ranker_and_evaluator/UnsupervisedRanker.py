from Ranker import Ranker

class UnsupervisedRanker(Ranker):

    def __init__(self, ranker_name, file_name_of_all_node_id_couples_to_rank_represented_as_vectors):
        Ranker.__init__(self, ranker_name, file_name_of_all_node_id_couples_to_rank_represented_as_vectors)
        return
