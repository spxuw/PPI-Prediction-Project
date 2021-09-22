package algos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import tools.SimpleIntDoubleMapCountingToInfinity;
import tools.SimpleIntSetCountingToInfinity;
import tools.SimpleSetUnorderedIntIntCouples;
import tools.SimpleIntSetForScanCountingToInfinity;
import tools.SimpleSetUnorderedIntIntCouplesCountingToInfinity;
import tools.SimpleUnorderedIntIntCouplesDoubleMapCountingToInfinity;

public class TopologicalFeaturesExtractor {

	public static void main(String[] args) throws Exception {
		//
		if ((args.length != 4)
				|| ((args.length >= 1) && (!args[0].equalsIgnoreCase("i") && !args[0].equalsIgnoreCase("e")))) {
			System.out.println();
			System.out.println("You must pass exactly one parameter.");
			System.out.println();
			System.out.println("Correct usage:");
			System.out.println(
					" $ java -Xms12g -Xmx12g algos.TopologicalFeaturesExtractor <i|e> <directory_containing_the_input_file> <input_PPI_file_name> <output_directory_THAT_MUST_EXIST>");
			System.out.println();
			System.out.println("Example for internal validation:");
			System.out.println(
					" $ java -Xms12g -Xmx12g algos.TopologicalFeaturesExtractor i /Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets HuRI.csv /Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets/HuRI__TRAINIG_and_VALIDATION");
			System.out.println();
			System.out.println("Example for external validation:");
			System.out.println(
					" $ java -Xms12g -Xmx12g algos.TopologicalFeaturesExtractor e /Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets HuRI.csv /Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets/HuRI__TEST");
			System.out.println();
			return;
		}
		//
		System.out.println(new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime()));
		//
		String external_internal_validation_flag = args[0];
		System.out.println(external_internal_validation_flag);
		String input_directory = args[1];
		System.out.println(input_directory);
		String input_file_name = args[2];
		System.out.println(input_file_name);
		String output_directory = args[3];
		System.out.println(output_directory);
		System.out.println();
		System.out.println(Arrays.toString(args));
		System.out.println();
		//
		int k = 10;
		if (external_internal_validation_flag.equalsIgnoreCase("i")) {
			k = 10;
		} else if (external_internal_validation_flag.equalsIgnoreCase("e")) {
			k = 1;
		} else {
			return;
		}
		int random_seed = 0;
		TopologicalFeaturesExtractor.split_k_folds__Training_SetS_and_Validation_SetS(input_directory, input_file_name,
				output_directory, k, random_seed);
		System.out.println();
		System.out.println(new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime()));
		System.out.println();
		return;
	}

	public static void inner_main(String[] args) throws Exception {
		//
		System.out.println(new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime()));

		String input_file_complete_path = args[0];

		String _graph_input_file_name_COLI = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/COLI/out_graph_file.tsv";
		String _graph_input_file_name_HuRI_05 = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/HuRI/HuRI_gene_name_conversion/DIAMOnD_conversion/H-I-05__FINAL.tsv";
		String _graph_input_file_name_HuRI_14 = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/HuRI/HuRI_gene_name_conversion/DIAMOnD_conversion/HI-II-14__FINAL.tsv";
		String _graph_input_file_name_HuRI_19 = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/HuRI/HuRI_gene_name_conversion/DIAMOnD_conversion/HuRI- HI-III-19__FINAL.tsv";
		String __graph_input_file_name_BARABASI = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/BARABASI/FINAL_barabasi_ppi_p_1__p_2__num_interactions.tsv";
		//
		String _graph_input_file_name_HuRI_UNION = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/HuRI/HI-union.tsv";
		//
		String _graph_HuRI_14_INT_19 = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/HuRI/HuRI_gene_name_conversion/HuRI_14_intersection_19/HuRI__14_intersection_19_tr.tsv";
		String _POSITIVE_INSTANCES_for_TEST__HuRI_14_INT_19 = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/HuRI/HuRI_gene_name_conversion/HuRI_14_intersection_19/HuRI__complement_14_to_19_POSITIVE_INSTANCES_ONLY.tsv";
		//
//		String graph_input_file_name = graph_input_file_name_COLI;// ______ 1min
//		String graph_input_file_name = graph_input_file_name_HuRI_05;// __ 1min
//		String graph_input_file_name = graph_input_file_name_HuRI_14;// __ 2min
//		String graph_input_file_name = graph_input_file_name_HuRI_19;// _ 21min 18min
//		String graph_input_file_name = graph_input_file_name_BARABASI;// _ >
		//
//		String graph_input_file_name = graph_HuRI_14_INT_19;// ___________ 4min
//		String POSITIVE_instances_input_file_name = POSITIVE_INSTANCES_for_TEST__HuRI_14_INT_19;

		//

		String graph_input_file_name_SyntheticPPI_OFFICIAL_PPI_CHALLENGE = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets/SyntheticPPI.csv";
		String graph_input_file_name_Arabidopsis_OFFICIAL_PPI_CHALLENGE = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets/Arabidopsis.csv";
		String graph_input_file_name_CElegance_OFFICIAL_PPI_CHALLENGE = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets/C. elegans.csv";
		String graph_input_file_name_Yeast_OFFICIAL_PPI_CHALLENGE = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets/Yeast.csv";
		String graph_input_file_name_HuRI_OFFICIAL_PPI_CHALLENGE = "/Users/ikki/Dropbox/PPI/PPIChallenge/datasets/PPI_challenge_official_datasets/HuRI.csv";
		//
		//
//		String graph_input_file_name = graph_input_file_name_SyntheticPPI_OFFICIAL_PPI_CHALLENGE;
//		String graph_name = "SyntheticPPI";
		//
//		String graph_input_file_name = graph_input_file_name_Arabidopsis_OFFICIAL_PPI_CHALLENGE;
//		String graph_name = "Arabidopsis";
		//
//		String graph_input_file_name = graph_input_file_name_CElegance_OFFICIAL_PPI_CHALLENGE;
//		String graph_name = "CElegance";
		//
//		String graph_input_file_name = graph_input_file_name_Yeast_OFFICIAL_PPI_CHALLENGE;
//		String graph_name = "Yeast";
		//
		String graph_input_file_name = graph_input_file_name_HuRI_OFFICIAL_PPI_CHALLENGE;
		String graph_name = "HuRI_OFFICIAL_PPI_CHALLENGE";// 24min for 10fold
		//
		//

		int k = 10;
//		GraphUndirectedUnweighted.random_k_folds__Training_SetS_and_Validation_SetS(graph_input_file_name, graph_name,
//				k);
		int random_seed = 0;
//		GraphUndirectedUnweighted.split_k_folds__Training_SetS_and_Validation_SetS(input_file_complete_path, k,
//				random_seed);
//		GraphUndirectedUnweighted.split_k_folds__Training_SetS_and_Validation_SetS(graph_input_file_name, graph_name, k,
//				random_seed);
		if (args.length >= 0) {
			return;
		}
		// #####################################################################################################
		// #####################################################################################################
		// #####################################################################################################
		// #####################################################################################################
		// #####################################################################################################

		TopologicalFeaturesExtractor g = new TopologicalFeaturesExtractor();
		double beta = 0.9;
		g.create_splitted_graph__V0(graph_input_file_name, beta);
		//
//		g.create_graph(graph_input_file_name, POSITIVE_instances_input_file_name);
		//
		//
//		System.out.println();
//		int c_node_id = 0;
//		for (int[] adj_list : g.adjacency_list) {
//			System.out.println(c_node_id + " -- " + Arrays.toString(adj_list));
//			c_node_id++;
//		}
		System.out.println("num_nodes           :" + "  " + g.get_num_nodes());
		System.out.println("num_edges           :" + " " + g.get_num_edges());
		System.out.println("num_POSITIVE_couples:" + "  " + g.get_num_POSITIVE_couples());
		System.out.println("this.max_node_id    :" + "  " + g.max_node_id);
		//

//		if (args.length >= 0) {
//			return;
//		}
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//
//		g.compute_feature_vectors_for_Training_Set_and_Validation_Set(graph_name);//////////////////////////////////////////////////////
		//
		//
		//
		//
		//
		//
		//
		//
		//
		//

//		System.out.println(Arrays.toString(g.map__node_inner_id__node_outer_id));
		//
		//
		//
		System.out.println();
		System.out.println("num_nodes                    :" + "  " + g.get_num_nodes());
		System.out.println("num_edges                    :" + " " + g.get_num_edges());
		System.out.println("num_POSITIVE_couples         :" + "  " + g.get_num_POSITIVE_couples());
		System.out.println("this.max_node_id             :" + "  " + g.max_node_id);
		double num_UNedged_couples_of_nodes = (((double) (g.get_num_nodes())) * (g.get_num_nodes() - 1) / 2.)
				- g.get_num_edges();
		System.out.println("num_UNedged_couples_of_nodes :" + "  " + num_UNedged_couples_of_nodes);
		System.out.println();
		System.out.println(new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime()));

		return;
	}

	// ==============================================================================================
	// ==============================================================================================
	// ==============================================================================================

	public static void random_k_folds__Training_SetS_and_Validation_SetS(String graph_input_file_name,
			String graph_name, int k) throws Exception {
		//
		double beta = 1. - (1. / k);
		//
		TopologicalFeaturesExtractor g = null;
		for (int i = 0; i < k; i++) {
			//
			g = new TopologicalFeaturesExtractor();
			//
			g.create_splitted_graph__V0(graph_input_file_name, beta);
			//
			System.out.println();
			System.out.println();
			System.out.println("  " + (i + 1) + " folds processed so far over a total of " + k + ".");
			System.out.println();
			System.out.println("num_nodes           :" + "  " + g.get_num_nodes());
			System.out.println("num_edges           :" + " " + g.get_num_edges());
			System.out.println("num_POSITIVE_couples:" + "  " + g.get_num_POSITIVE_couples());
			System.out.println("this.max_node_id    :" + "  " + g.max_node_id);
			//
			g.compute_feature_vectors_for_Training_Set_and_Validation_Set(graph_name + "__random_fold_" + i + "__");
			//
		}
		//
		return;
	}

	public static void split_k_folds__Training_SetS_and_Validation_SetS(String input_directory, String input_file_name,
			String output_directory, int k, int random_seed) throws Exception {
		//
		String graph_input_file_name = input_directory + "/" + input_file_name;
		String graph_output_file_name = output_directory + "/" + input_file_name;
		//
		Random random_generator = new Random(random_seed);
		//
		TopologicalFeaturesExtractor g = null;
		for (int i = 0; i < k; i++) {
			//
			g = new TopologicalFeaturesExtractor();
			//
			g.create_splitted_graph(graph_input_file_name, random_generator, k, i);
			//
			System.out.println();
			System.out.println();
			System.out.println("  " + (i + 1) + " folds processed so far over a total of " + k + ".");
			System.out.println();
			System.out.println("num_nodes           :" + "  " + g.get_num_nodes());
			System.out.println("num_edges           :" + " " + g.get_num_edges());
			System.out.println("num_POSITIVE_couples:" + "  " + g.get_num_POSITIVE_couples());
			System.out.println("this.max_node_id    :" + "  " + g.max_node_id);
			//
			//
			if (k == 1) {
				g.compute_feature_vectors_for_Training_Set_and_Validation_Set(graph_output_file_name + "__");
			} else {
				g.compute_feature_vectors_for_Training_Set_and_Validation_Set(
						graph_output_file_name + "__splitted_fold_" + i + "__");
			}
			//
		}
		//
		return;
	}
	// ==============================================================================================
	// ==============================================================================================
	// ==============================================================================================

	Set<UnorderedTuple> set_edges = null;
	Set<UnorderedTuple> set_POSITIVE_couples = null;

	// ------------------------------------------------------------------
	SimpleSetUnorderedIntIntCouples fast_set_edges = null;
	SimpleSetUnorderedIntIntCouples fast_set_POSITIVE_couples = null;
	//
	Map<String, Integer> map__node_outer_id__node_inner_id = null;
	String[] map__node_inner_id__node_outer_id = null;
	int[][] adjacency_list = null;
	int max_node_id = -1;
	int num_nodes = 0;
	// ------------------------------------------------------------------

	public TopologicalFeaturesExtractor() {
		return;
	}

	public int get_num_nodes() {
		return this.num_nodes;
	}

	public int get_num_edges() {
		return this.set_edges.size();
	}

	public int get_num_POSITIVE_couples() {
		return this.fast_set_POSITIVE_couples.size();
//		return this.set_POSITIVE_couples.size();
	}

	public int get_degree(int u) {
		return this.adjacency_list[u].length;
	}

	public void create_splitted_graph(String graph_input_file_name, Random random_generator, int k, int fold_number)
			throws Exception {
		//
		// Collect all the outer node id set.
		// compute the max outer node id.
		// set the max_node_id.
		Set<String> set__all_node_outer_id = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(graph_input_file_name), 100000000);
		String line = null;
		// int max_node_outer_id = -1;
		String outer_node_id_u = "";
		String outer_node_id_v = "";
		int first_separator_index = 0;
		br.readLine();// skip the first line
		while ((line = br.readLine()) != null) {
			//
			first_separator_index = line.indexOf(",");
			outer_node_id_u = line.substring(0, first_separator_index);
			outer_node_id_v = line.substring(first_separator_index + 1);
			//
			set__all_node_outer_id.add(outer_node_id_u);
			set__all_node_outer_id.add(outer_node_id_v);
			//
		}
		this.max_node_id = set__all_node_outer_id.size() - 1;
		this.num_nodes = set__all_node_outer_id.size();
		br.close();
		//
		// create bijective mapping between inner and outer node ids.
		//
		// create the mapping from outer to inner node ids.
		List<String> sorted_list__all_node_outer_id = new ArrayList<String>(set__all_node_outer_id);
		set__all_node_outer_id = null;
		Collections.sort(sorted_list__all_node_outer_id);
		this.map__node_outer_id__node_inner_id = new HashMap<String, Integer>(2 * this.num_nodes);
		int node_inner_id = 0;
		for (String node_outer_id : sorted_list__all_node_outer_id) {
			this.map__node_outer_id__node_inner_id.put(node_outer_id, node_inner_id);
			node_inner_id++;
		}
		sorted_list__all_node_outer_id = null;
		//
		// create the mapping from inner to outer node ids.
		this.map__node_inner_id__node_outer_id = new String[this.max_node_id + 1];
		String node_outer_id = "";
		node_inner_id = 0;
		for (Map.Entry<String, Integer> outer_inner_node_id_pair : this.map__node_outer_id__node_inner_id.entrySet()) {
			node_outer_id = outer_inner_node_id_pair.getKey();
			node_inner_id = outer_inner_node_id_pair.getValue();
			this.map__node_inner_id__node_outer_id[node_inner_id] = node_outer_id;
		}
		//
		// extract the set of ALL edges.
		Set<UnorderedTuple> set_ALL_edges = new HashSet<UnorderedTuple>();
		//
		br = new BufferedReader(new FileReader(graph_input_file_name), 100000000);
		String node_u_outer_id;
		String node_v_outer_id;
		int node_u_inner_id;
		int node_v_inner_id;
		br.readLine();// skip the first line
		while ((line = br.readLine()) != null) {
			//
			first_separator_index = line.indexOf(",");
			node_u_outer_id = line.substring(0, first_separator_index);
			node_v_outer_id = line.substring(first_separator_index + 1);
			//
			if (node_u_outer_id.equals(node_v_outer_id)) {
				continue;
			}
			//
			node_u_inner_id = this.map__node_outer_id__node_inner_id.get(node_u_outer_id);
			node_v_inner_id = this.map__node_outer_id__node_inner_id.get(node_v_outer_id);
			//
			set_ALL_edges.add(new UnorderedTuple(node_u_inner_id, node_v_inner_id));
			//
		}
		br.close();
		//
		// split the set of edges
		ArrayList<UnorderedTuple> shuffled_list_of_ALL_edges = new ArrayList<UnorderedTuple>(set_ALL_edges);
		set_ALL_edges = null;
		Collections.shuffle(shuffled_list_of_ALL_edges, random_generator);
		//
		// Compute the partition indexes.
		double k_as_double = k;
		int initial_partition_index_to_consider = (int) Math
				.floor(shuffled_list_of_ALL_edges.size() * (fold_number / k_as_double));
		int final_partition_index_to_exclude = (int) Math
				.floor(shuffled_list_of_ALL_edges.size() * ((fold_number + 1) / k_as_double));
		if (k == 1) {
			initial_partition_index_to_consider = -1;
			final_partition_index_to_exclude = initial_partition_index_to_consider;
		}
		//
		//
		this.set_POSITIVE_couples = new HashSet<UnorderedTuple>();// TO REMOVE?
		this.set_edges = new HashSet<UnorderedTuple>();// TO REMOVE?
		this.fast_set_edges = new SimpleSetUnorderedIntIntCouples(this.max_node_id);
		this.fast_set_POSITIVE_couples = new SimpleSetUnorderedIntIntCouples(this.max_node_id);
		UnorderedTuple c_couple_of_inner_ids_of_u_and_v = null;
		for (int i = 0; i < shuffled_list_of_ALL_edges.size(); i++) {
			//
			c_couple_of_inner_ids_of_u_and_v = shuffled_list_of_ALL_edges.get(i);
			//
			// add only (1-beta) edges inside the set_POSITIVE_couples.
			if ((i >= initial_partition_index_to_consider) && (i < final_partition_index_to_exclude)) {
				// add edge inside the set_POSITIVE_couples.
				this.set_POSITIVE_couples.add(c_couple_of_inner_ids_of_u_and_v);
				this.fast_set_POSITIVE_couples.add(c_couple_of_inner_ids_of_u_and_v.a,
						c_couple_of_inner_ids_of_u_and_v.b);
			} else {
				// add the edge.
				this.set_edges.add(c_couple_of_inner_ids_of_u_and_v);
				this.fast_set_edges.add(c_couple_of_inner_ids_of_u_and_v.a, c_couple_of_inner_ids_of_u_and_v.b);
			}
		}
		shuffled_list_of_ALL_edges = null;
		//
		// create the test graph considering the max node id
		Map<Integer, ArrayList<Integer>> inner_adjacency_list = new HashMap<Integer, ArrayList<Integer>>(
				2 * this.max_node_id);
		int u, v;
		for (UnorderedTuple c_edge : this.set_edges) {
			//
			u = c_edge.a;
			v = c_edge.b;
			if (!(inner_adjacency_list.containsKey(u))) {
				inner_adjacency_list.put(u, new ArrayList<Integer>());
			}
			if (!(inner_adjacency_list.containsKey(v))) {
				inner_adjacency_list.put(v, new ArrayList<Integer>());
			}
			inner_adjacency_list.get(u).add(v);
			inner_adjacency_list.get(v).add(u);
		}
		//
		// graph creation considering the ORIGINAL and GENERAL max_node_id.
		this.adjacency_list = new int[this.max_node_id + 1][];
		ArrayList<Integer> c_adj_list = null;
		int i = 0;
		for (int node_id = 0; node_id <= this.max_node_id; node_id++) {
			//
			if (inner_adjacency_list.containsKey(node_id)) {
				c_adj_list = inner_adjacency_list.get(node_id);
				Collections.sort(c_adj_list);
				//
				this.adjacency_list[node_id] = new int[c_adj_list.size()];
				i = 0;
				for (int c_neig : c_adj_list) {
					this.adjacency_list[node_id][i] = c_neig;
					i++;
				}
			} else {
				this.adjacency_list[node_id] = new int[0];
			}
		}
		return;
	}

	/**
	 * qqq
	 * 
	 * @return
	 * @throws Exception
	 */
	public String compute_feature_vectors_for_Training_Set_and_Validation_Set(String input_graph_complete_path)
			throws Exception {
		long TimeStamp = System.currentTimeMillis();
		//
		int u = 0;
		int v = 0;
		int c_class = 0;
		//
		String header = "";
		//
		// Write the validation file.
		if (this.fast_set_POSITIVE_couples.size() > 0) {
			String val_set_output_file_name = input_graph_complete_path + "__VALIDATION_SET__nodes_couples__"
					+ TimeStamp + ".tsv";
			BufferedWriter bw_val = new BufferedWriter(new FileWriter(val_set_output_file_name), 100000000);
			header = "class	u	v";
			bw_val.write(header + "\n");
			// generate all possible
			// unordered couples of node inner identifiers
			// (where u < v)
			// checking if the couple should be added in the
			// validation set.
			//
			for (u = 0; u < this.max_node_id; u++) {
				for (v = u + 1; v <= this.max_node_id; v++) {
					if (this.fast_set_POSITIVE_couples.contains(u, v)) {
						bw_val.write("0" + "\t" + this.map__node_inner_id__node_outer_id[u] + "\t"
								+ this.map__node_inner_id__node_outer_id[v] + "\n");
					}
				}

			}
			bw_val.close();
		}
		//
		// Compute JACCARD for all couples!
		SimpleUnorderedIntIntCouplesDoubleMapCountingToInfinity map__u__v__intersection = new SimpleUnorderedIntIntCouplesDoubleMapCountingToInfinity(
				this.max_node_id);
		int[] adjacency_list_of_u = null;
		int degree_of_u = 0;
		double c_nummber_of_common_elements = 0;
		double c_jaccard = 0.;
		for (u = 0; u < this.max_node_id; u++) {
			//
//			System.out.println("map__u__v__jaccard  u= " + u);
//			System.out.println("map__u__v__jaccard  map__u__v__jaccard.size()= " + map__u__v__jaccard.size());
			//
			adjacency_list_of_u = this.adjacency_list[u];
			degree_of_u = adjacency_list_of_u.length;
			//
			for (v = u + 1; v <= this.max_node_id; v++) {
				c_nummber_of_common_elements = compute_nummber_of_common_elements(adjacency_list_of_u,
						this.adjacency_list[v]);
//				c_jaccard = c_nummber_of_common_elements
//						/ (degree_of_u + this.adjacency_list[v].length - c_nummber_of_common_elements);
				//
				map__u__v__intersection.put(u, v, c_nummber_of_common_elements);
				//
			}
		}
//		if (map__u__v__jaccard.size() >= 0) { 
//			return null;
//		}

		//
		// Creation of the TRAINING set
		String output_file_name = input_graph_complete_path + "__TRAINING_SET__nodes_couples_in_the_fetures_space__"
				+ TimeStamp + ".tsv";
		if (this.fast_set_POSITIVE_couples.size() <= 0) {
			output_file_name = input_graph_complete_path + "__TEST_SET__nodes_couples_in_the_fetures_space__"
					+ TimeStamp + ".tsv";
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file_name), 100000000);
		// header = "class u v Shortest_Path_length Preferential_Attachment Adamic_Addar
		// Resource_Allocation Jaccard_Coefficient CH1_L2 CH2_L2 A3 L3 CH1_L3 CH2_L3 sim
		// L3E";
		header = "class	u	v	Shortest_Path_length	Preferential_Attachment	Adamic_Addar	Resource_Allocation	Jaccard_Coefficient	CH1_L2	CH2_L2	A3	L3	CH1_L3	CH2_L3	sim	L3E	INT_SIZE	UNION_SIZE	deg_u	deg_v	MAX_deg	min_deg	MAX_deg/min_deg	min_deg/MAX_deg";
		header += "\t" + "sim__jac__avg__sum";
		header += "\t" + "sim__jac__MAX__sum";// o
		header += "\t" + "sim__jac__sum__MAX";
		header += "\t" + "sim__jac__avg__MAX";
		header += "\t" + "sim__jac__MAX__MAX";// o
		header += "\t" + "sim__jac__sum__wavg";
		//
		header += "	" + "sim__jac_minus_one__sum__sum";
		header += "	" + "sim__jac_minus_one__avg__sum";
		header += "	" + "sim__jac_minus_one__MAX__sum";
		header += "	" + "sim__jac_minus_one__sum__MAX";
		header += "	" + "sim__jac_minus_one__avg__MAX";
		header += "	" + "sim__jac_minus_one__MAX__MAX";
		header += "	" + "sim_minus_one__jac__sum__wavg";
		//
		header += "	" + "sim__con__sum__sum";
		header += "	" + "sim__con__avg__sum";
		header += "	" + "sim__con__MAX__sum";// o
		header += "	" + "sim__con__sum__MAX";
		header += "	" + "sim__con__avg__MAX";
		header += "	" + "sim__con__MAX__MAX";// o
		header += "	" + "sim__con__sum__wavg";
		//
		header += "	" + "sim__con_minus_one__sum__sum";
		header += "	" + "sim__con_minus_one__avg__sum";
		header += "	" + "sim__con_minus_one__MAX__sum";
		header += "	" + "sim__con_minus_one__sum__MAX";
		header += "	" + "sim__con_minus_one__avg__MAX";
		header += "	" + "sim__con_minus_one__MAX__MAX";
		header += "	" + "sim_minus_con__sum__wavg";
		//
		//
		//
		header = "class	u	v	Shortest_Path_length	Preferential_Attachment";
		header += "\t" + "sim";// o
		header += "\t" + "sim__jac__MAX__sum";// o
		header += "\t" + "sim__jac__MAX__MAX";// o
		header += "\t" + "sim__con__MAX__sum";// o
		header += "\t" + "sim__con__MAX__MAX";// o
		bw.write(header + "\n");
		//
		SimpleIntSetForScanCountingToInfinity set_of_common_neighbours = new SimpleIntSetForScanCountingToInfinity(
				this.max_node_id);
		SimpleIntDoubleMapCountingToInfinity map__node__INTERNAL_DEGREE = new SimpleIntDoubleMapCountingToInfinity(
				this.max_node_id);
		SimpleIntSetCountingToInfinity set__a_nodes = new SimpleIntSetCountingToInfinity(this.max_node_id);
		SimpleIntSetCountingToInfinity set__b_nodes = new SimpleIntSetCountingToInfinity(this.max_node_id);
		//
		//
		// set of ordered couples of node inner identifiers
		// of internal nodes for simple-paths of length three.
		SimpleSetUnorderedIntIntCouplesCountingToInfinity set__node_a__node_b = new SimpleSetUnorderedIntIntCouplesCountingToInfinity(
				this.max_node_id);
		//
		// paired couples of node ids,
		// where at the same index is stored the id of the two
		// internal nodes of simple-paths of length three.
		// The first array elements stores the index of the first free location.
		int[] list_node_a_with_size = new int[1 + this.get_num_edges()];
		int[] list_node_b_with_size = new int[1 + this.get_num_edges()];
		//
		// distance_independent_metrics stores at
		// index 0 the Preferential-Attachment metric,
		// index 1 the Shortest-Path: 0 means a Shortest-Path > 3.
		double[] distance_independent_metrics = new double[8];// PA, Shortest_Path, deg_u, deg_v, Max_deg, min_deg,
																// MAX_deg/min_deg, min_deg/MAX_deg
		double[] L2_metrics = new double[7];// All metrics with distance 2
		double[] L3_metrics = new double[33];// All metrics with distance 3
		double[] all_zeros = new double[L3_metrics.length];// useful array of all zeros.
		Arrays.fill(all_zeros, 0.);
		//
		// flag that indicates that the nodes u and v
		// are connected by an edge in the Training-GRAPH.
		boolean u_and_v_are_connected = false;
		//
		// Set of nodes internal identifiers of nodes at distance of 1, 2 or 3 hops from
		// node u.
		SimpleIntSetForScanCountingToInfinity set_nodes_distance_1_or_2_or_3 = new SimpleIntSetForScanCountingToInfinity(
				this.max_node_id);
		int size_set_nodes_distance_1_or_2_or_3;
		//
		int ii, jj, kk, neigh_hop_1, neigh_hop_2, neigh_hop_3;
		// representation of set_nodes_distance_1_or_2_or_3 as an array always bigger
		// than what is needed.
		int[] set_nodes_distance_1_or_2_or_3__AS_ARRAY;
		//
		//
		long num_couples_processed_so_far = 0;
		long total_num_couples = (this.num_nodes * (this.num_nodes - 1)) / 2;
		//
		//
		// Fill the set of nodes internal identifiers of nodes at distance of 1, 2 or 3
		// hops from node u.
		for (u = 0; u < this.max_node_id; u++) {
			//
			set_nodes_distance_1_or_2_or_3.clear();
			//
			for (ii = 0; ii < this.adjacency_list[u].length; ii++) {
				neigh_hop_1 = this.adjacency_list[u][ii];
				if (neigh_hop_1 == u) {
					continue;
				}
				//
				for (jj = 0; jj < this.adjacency_list[neigh_hop_1].length; jj++) {
					neigh_hop_2 = this.adjacency_list[neigh_hop_1][jj];
					//
					if (neigh_hop_2 == neigh_hop_1 || neigh_hop_2 == u) {
						continue;
					}
					//
					if (u < neigh_hop_2) {
						set_nodes_distance_1_or_2_or_3.add(neigh_hop_2);
					}
					//
					for (kk = 0; kk < this.adjacency_list[neigh_hop_2].length; kk++) {
						neigh_hop_3 = this.adjacency_list[neigh_hop_2][kk];
						//
//						if (neigh_dist_3 != neigh_dist_1) { // ErROr!!!
//							continue;
//						}
						if (neigh_hop_3 == neigh_hop_2 || neigh_hop_3 == neigh_hop_1 || neigh_hop_3 == u) {
							continue;
						}
						if (u < neigh_hop_3) {
							set_nodes_distance_1_or_2_or_3.add(neigh_hop_3);
						}
						//
					}
				}
			}
			//
			// represent the set_nodes_distance_1_or_2_or_3 as an array for fast scanning.
			set_nodes_distance_1_or_2_or_3__AS_ARRAY = set_nodes_distance_1_or_2_or_3.get_list_of_keys();
			size_set_nodes_distance_1_or_2_or_3 = set_nodes_distance_1_or_2_or_3.size();
			System.out.println("size_set_nodes_distance_1_or_2_or_3= " + size_set_nodes_distance_1_or_2_or_3);
			for (ii = 0; ii < size_set_nodes_distance_1_or_2_or_3; ii++) {
				//
				v = set_nodes_distance_1_or_2_or_3__AS_ARRAY[ii];
				if (u >= v) {
					continue;
				}
				//
				if (this.fast_set_edges.contains(u, v)) {
					c_class = 0;
					u_and_v_are_connected = true;
				} else {
					c_class = 1;
					u_and_v_are_connected = false;
				}
				//
				this.compute_distance_independent_metrics(u, v, u_and_v_are_connected, distance_independent_metrics);
//				System.out.println("--------------------------------------");
//				System.out.println("u= " + u + " " + this.map__node_inner_id__node_outer_id[u]);
//				System.out.println("v= " + v + " " + this.map__node_inner_id__node_outer_id[v]);
//				System.out.println("c_class= " + c_class);// ofd
//				System.out.println(Arrays.toString(distance_independent_metrics));// ofd
				this.compute_L2_metrics(u, v, set_of_common_neighbours, u_and_v_are_connected, L2_metrics);
				this.compute_L3_metrics(u, v, map__u__v__intersection, map__node__INTERNAL_DEGREE,
						list_node_a_with_size, list_node_b_with_size, set__node_a__node_b, set__a_nodes, set__b_nodes,
						L3_metrics);
//				System.out.println("--------------------------------------");
				//
				//
				// Deducting the Shortest-Path length from the metrics at distance 2 and 3.
				if (L3_metrics[0] > 0) {
					distance_independent_metrics[1] = 3; // Shortest-Path.
				}
				if (L2_metrics[0] > 0) {
					distance_independent_metrics[1] = 2; // Shortest-Path.
				}
				//
				// write the u-v vector on disk.
				this.create_and_write_output_row(u, v, bw, c_class, distance_independent_metrics, L2_metrics,
						L3_metrics);
				//
			}
			//
			System.arraycopy(all_zeros, 0, distance_independent_metrics, 0, distance_independent_metrics.length);
			System.arraycopy(all_zeros, 0, L2_metrics, 0, L2_metrics.length);
			System.arraycopy(all_zeros, 0, L3_metrics, 0, L3_metrics.length);
			//
			// compute the metrics for all node v at distance grater than 3.
//			if (all_zeros.length >= 0) {  // -------------------------------------------------------------------
//				continue;
//			}
			for (v = u + 1; v <= this.max_node_id; v++) {
				num_couples_processed_so_far++;
				//
				//
				if (v == this.max_node_id) {
					System.out.println();
					System.out.println("c_inner_node_id: " + u + "  max_inner_node_id: " + this.max_node_id);
					System.out.println("c_outer_node_id: " + this.map__node_inner_id__node_outer_id[u]);
					System.out.println(
							"couples done/total= " + ((float) num_couples_processed_so_far / total_num_couples));
					System.out.println("  nodes done/total= " + ((float) (u + 1) / this.max_node_id));
				}
				//
				//
				if (set_nodes_distance_1_or_2_or_3.contains(v)) {
					continue;
				}
				//
				if (this.fast_set_edges.contains(u, v)) {
					c_class = 0;
					u_and_v_are_connected = true;
				} else {
					c_class = 1;
					u_and_v_are_connected = false;
				}
				//
				//
				this.compute_distance_independent_metrics(u, v, u_and_v_are_connected, distance_independent_metrics);
				//
				this.create_and_write_output_row(u, v, bw, c_class, distance_independent_metrics, L2_metrics,
						L3_metrics);
			}
			//
//			bw.flush();// ofd
			//
		}
		bw.close();
		//
		//
		return output_file_name;

	}

	protected void create_and_write_output_row(int u, int v, BufferedWriter bw, int c_class,
			double[] distance_independent_metrics, double[] L2_metrics, double[] L3_metrics) throws Exception {
		String c_row = "";
		c_row += c_class + "\t";
		c_row += this.map__node_inner_id__node_outer_id[u] + "\t";
		c_row += this.map__node_inner_id__node_outer_id[v] + "\t";
		//
		c_row += distance_independent_metrics[1] + "\t"; // Shortest_Path
		c_row += distance_independent_metrics[0] + "\t"; // PA
		//
		c_row += L3_metrics[0] + "\t";// sim__jac__sum__sum
		c_row += L3_metrics[1] + "\t";// sim__jac__MAX__sum
		c_row += L3_metrics[2] + "\t";// sim__jac__MAX__MAX
		c_row += L3_metrics[3] + "\t";// sim__con__MAX__sum
		c_row += L3_metrics[4] + "\t";// sim__con__MAX__MAX
		//
		bw.write(c_row + "\n");
		//
		return;
	}

	protected void create_and_write_output_row__ORIGINAL(int u, int v, BufferedWriter bw, int c_class,
			double[] distance_independent_metrics, double[] L2_metrics, double[] L3_metrics) throws Exception {
		String c_row = "";
		c_row += c_class + "\t";
		c_row += this.map__node_inner_id__node_outer_id[u] + "\t";
		c_row += this.map__node_inner_id__node_outer_id[v] + "\t";
		//
		c_row += distance_independent_metrics[1] + "\t"; // Shortest_Path
		c_row += distance_independent_metrics[0] + "\t"; // PA
		//
		c_row += L2_metrics[0] + "\t";// ok AA
		c_row += L2_metrics[1] + "\t";// ok RA
		c_row += L2_metrics[2] + "\t";// ok JA
		c_row += L2_metrics[3] + "\t";// ok CH1_L2
		c_row += L2_metrics[4] + "\t";// ok CH2_L2
		//
		c_row += L3_metrics[0] + "\t";// A3
		c_row += L3_metrics[1] + "\t";// L3
		c_row += L3_metrics[2] + "\t";// CH1L3
		c_row += L3_metrics[3] + "\t";// CH2L3
		c_row += L3_metrics[4] + "\t";// sim
		c_row += L3_metrics[5] + "\t";// L3E
		//
		c_row += L2_metrics[5] + "\t";// intersection size, a.k.a number of common neighbours.
		c_row += L2_metrics[6] + "\t";// union size of the two set of neighbours.
		c_row += distance_independent_metrics[2] + "\t"; //
		c_row += distance_independent_metrics[3] + "\t"; //
		c_row += distance_independent_metrics[4] + "\t"; //
		c_row += distance_independent_metrics[5] + "\t"; //
		c_row += distance_independent_metrics[6] + "\t"; //
		c_row += distance_independent_metrics[7] + "\t"; //
		//
		c_row += L3_metrics[6] + "\t"; //
		c_row += L3_metrics[7] + "\t"; //
		c_row += L3_metrics[8] + "\t"; //
		c_row += L3_metrics[9] + "\t"; //
		c_row += L3_metrics[10] + "\t"; //
		c_row += L3_metrics[11] + "\t"; //
		//
		c_row += L3_metrics[12] + "\t"; //
		c_row += L3_metrics[13] + "\t"; //
		c_row += L3_metrics[14] + "\t"; //
		c_row += L3_metrics[15] + "\t"; //
		c_row += L3_metrics[16] + "\t"; //
		c_row += L3_metrics[17] + "\t"; //
		//
		c_row += L3_metrics[18] + "\t"; //
		c_row += L3_metrics[19] + "\t"; //
		c_row += L3_metrics[20] + "\t"; //
		c_row += L3_metrics[21] + "\t"; //
		c_row += L3_metrics[22] + "\t"; //
		c_row += L3_metrics[23] + "\t"; //
		//
		c_row += L3_metrics[24] + "\t"; //
		c_row += L3_metrics[25] + "\t"; //
		c_row += L3_metrics[26] + "\t"; //
		c_row += L3_metrics[27] + "\t"; //
		c_row += L3_metrics[28] + "\t"; //
		c_row += L3_metrics[29] + "\t"; //
		//
		c_row += L3_metrics[30] + "\t"; //
		c_row += L3_metrics[31] + "\t"; //
		c_row += L3_metrics[32] + "\t"; //
		//
		bw.write(c_row + "\n");
		//
		return;
	}

	/**
	 * 
	 * @param u
	 * @param v
	 * @param output_results
	 */
	protected void compute_distance_independent_metrics(int u, int v, boolean u_and_v_are_connected,
			double[] output_results) {
		//
		int max_degree, min_degree;
		int subtraction_factor = (u_and_v_are_connected ? 1 : 0);
		int deg_u = this.adjacency_list[u].length - subtraction_factor;
		int deg_v = this.adjacency_list[v].length - subtraction_factor;
		output_results[0] = deg_u * deg_v; // PA
		output_results[2] = deg_u; // deg_u
		output_results[3] = deg_v; // deg_v
		max_degree = (deg_u > deg_v ? deg_u : deg_v);
		min_degree = (deg_u < deg_v ? deg_u : deg_v);
		output_results[4] = max_degree;// Max_deg
		output_results[5] = min_degree;// min_deg
		output_results[6] = ((double) max_degree) / min_degree; // MAX_deg/min_deg
		output_results[7] = ((double) min_degree) / max_degree; // min_deg/MAX_deg
		//
		assert output_results[0] >= 0;
		//
		return;
	}

	protected void compute_L3_metrics(int u, int v,
			SimpleUnorderedIntIntCouplesDoubleMapCountingToInfinity map__u__v__intersection,
			SimpleIntDoubleMapCountingToInfinity map__node__INTERNAL_DEGREE, int[] list_node_a_with_size,
			int[] list_node_b_with_size, SimpleSetUnorderedIntIntCouplesCountingToInfinity set__node_a__node_b,
			SimpleIntSetCountingToInfinity set__a_nodes, SimpleIntSetCountingToInfinity set__b_nodes,
			double[] output_results) {
		// fill the set_of_induced_nodes_from_L3
		Arrays.fill(output_results, 0);
		//
		int[] adj_list_u = this.adjacency_list[u];
		int[] adj_list_a = null;
		int[] adj_list_v = this.adjacency_list[v];
		//
		set__node_a__node_b.clear();
		map__node__INTERNAL_DEGREE.clear();
		list_node_a_with_size[0] = 1;
		list_node_b_with_size[0] = 1;
		//
		int a = 0;
		int index_adj_list_u;
		for (index_adj_list_u = 0; index_adj_list_u < adj_list_u.length; index_adj_list_u++) {
			//
			a = adj_list_u[index_adj_list_u];
			if (a == u || a == v) {
				continue;
			}
			//
			//
			adj_list_a = this.adjacency_list[a];
			//
			this.update_inner_degree_and_add_a_b_internal_node_couples_list(u, v, a, adj_list_a, adj_list_v,
					map__node__INTERNAL_DEGREE, list_node_a_with_size, list_node_b_with_size, set__node_a__node_b);
			//
		}
		//
		int index = 0;
		int max_index_excluded = list_node_a_with_size[0];
		//
		assert list_node_a_with_size[0] == list_node_b_with_size[0];
		//
		int b, degree_a, degree_b;
		double A3 = 0;
		double L3 = 0;
		double CH1L3 = 0;
		double CH2L3 = 0;
		double L3E = 0;
		double INTERNAL_DEGREE_a, INTERNAL_DEGREE_b, one_plus_INTERNAL_DEGREE_a, one_plus_INTERNAL_DEGREE_b;
		double EXTERNAL_DEGREE_a, EXTERNAL_DEGREE_b, one_plus_EXTERNAL_DEGREE_a, one_plus_EXTERNAL_DEGREE_b;
		set__a_nodes.clear();
		set__b_nodes.clear();
		double intersection_size = 0;
		double union_size = 0;
		double jacc__u_b, jacc__a_v;
		for (index = 1; index < max_index_excluded; index++) {
			a = list_node_a_with_size[index];
			b = list_node_b_with_size[index];
			//
			degree_a = this.adjacency_list[a].length;
			degree_b = this.adjacency_list[b].length;
			INTERNAL_DEGREE_a = map__node__INTERNAL_DEGREE.get(a, 0.);
			INTERNAL_DEGREE_b = map__node__INTERNAL_DEGREE.get(b, 0.);
			EXTERNAL_DEGREE_a = degree_a - INTERNAL_DEGREE_a;
			EXTERNAL_DEGREE_b = degree_b - INTERNAL_DEGREE_b;
			one_plus_INTERNAL_DEGREE_a = INTERNAL_DEGREE_a + 1;
			one_plus_INTERNAL_DEGREE_b = INTERNAL_DEGREE_b + 1;
			one_plus_EXTERNAL_DEGREE_a = EXTERNAL_DEGREE_a + 1;
			one_plus_EXTERNAL_DEGREE_b = EXTERNAL_DEGREE_b + 1;
			//
//			System.out.println("a= " + a + " " + this.map__node_inner_id__node_outer_id[a]);
//			System.out.println("degree_a                  = " + degree_a);
//			System.out.println("INTERNAL_DEGREE_a         = " + INTERNAL_DEGREE_a);
//			System.out.println("one_plus_INTERNAL_DEGREE_a= " + one_plus_INTERNAL_DEGREE_a);
//			System.out.println("EXTERNAL_DEGREE_a         = " + EXTERNAL_DEGREE_a);
//			System.out.println("one_plus_EXTERNAL_DEGREE_a= " + one_plus_EXTERNAL_DEGREE_a);
//			System.out.println("b= " + b + " " + this.map__node_inner_id__node_outer_id[b]);
//			System.out.println("degree_b                  = " + degree_b);
//			System.out.println("INTERNAL_DEGREE_b         = " + INTERNAL_DEGREE_b);
//			System.out.println("one_plus_INTERNAL_DEGREE_b= " + one_plus_INTERNAL_DEGREE_b);
//			System.out.println("EXTERNAL_DEGREE_b         = " + EXTERNAL_DEGREE_b);
//			System.out.println("one_plus_EXTERNAL_DEGREE_b= " + one_plus_EXTERNAL_DEGREE_b);
			//
			A3 += 1;
			L3 += 1. / Math.sqrt(degree_a * degree_b);
			//
			CH1L3 += Math.sqrt((INTERNAL_DEGREE_a * INTERNAL_DEGREE_b) / (degree_a * degree_b));
			CH2L3 += Math.sqrt((one_plus_INTERNAL_DEGREE_a * one_plus_INTERNAL_DEGREE_b)
					/ (one_plus_EXTERNAL_DEGREE_a * one_plus_EXTERNAL_DEGREE_b));
			//
			// L3E
			set__a_nodes.add(a);
			set__b_nodes.add(b);
			intersection_size = map__u__v__intersection.get(u, b, 0.);
			union_size = this.adjacency_list[u].length + this.adjacency_list[b].length - intersection_size;
			jacc__u_b = intersection_size / union_size;
			//
			intersection_size = map__u__v__intersection.get(a, v, 0.);
			union_size = this.adjacency_list[a].length + this.adjacency_list[v].length - intersection_size;
			jacc__a_v = intersection_size / union_size;
			//
			// L3E += map__u__v__jaccard.get(u, b, 0.) * map__u__v__jaccard.get(a, v, 0.);
			L3E += jacc__u_b * jacc__a_v;
		}
		// L3E... continue
		L3E *= ((double) set__a_nodes.size()) / this.adjacency_list[u].length;
		L3E *= ((double) set__b_nodes.size()) / this.adjacency_list[v].length;
		//
		// --- SIM ---
		b = u;
		a = v;
		int[] neigs_b = this.adjacency_list[b];
		int alpha, beta;
		int[] neigs_a = this.adjacency_list[a];
		double jac__sum_1 = 0.;
		double jac__sum_2 = 0.;
		double jac__avg_1 = 0.;
		double jac__avg_2 = 0.;
		double jac__MAX_1 = 0.;
		double jac__MAX_2 = 0.;
		//
		double con__sum_1 = 0.;
		double con__sum_2 = 0.;
		double con__avg_1 = 0.;
		double con__avg_2 = 0.;
		double con__MAX_1 = 0.;
		double con__MAX_2 = 0.;
		//
		double con_minus_one__sum_1 = 0.;
		double con_minus_one__sum_2 = 0.;
		double con_minus_one__avg_1 = 0.;
		double con_minus_one__avg_2 = 0.;
		double con_minus_one__MAX_1 = 0.;
		double con_minus_one__MAX_2 = 0.;
		//
		double jac_minus_one__sum_1 = 0.;
		double jac_minus_one__sum_2 = 0.;
		double jac_minus_one__avg_1 = 0.;
		double jac_minus_one__avg_2 = 0.;
		double jac_minus_one__MAX_1 = 0.;
		double jac_minus_one__MAX_2 = 0.;
		double c_jac, c_jac_minus_one, c_con, c_con_minus_one;
		int deg_b = this.adjacency_list[b].length;
		int deg_a = this.adjacency_list[a].length;
		for (index = 0; index < neigs_a.length; index++) {
			beta = neigs_a[index];
			intersection_size = map__u__v__intersection.get(b, beta, 0.);
			c_jac = intersection_size / (deg_b + this.adjacency_list[beta].length - intersection_size);
			c_jac_minus_one = intersection_size / (deg_b + this.adjacency_list[beta].length - intersection_size - 1.);
			c_con = (intersection_size > 0 ? intersection_size / (this.adjacency_list[beta].length) : 0);
			c_con_minus_one = 0;
			if ((this.adjacency_list[beta].length - 1.) > 0) {
				c_con_minus_one = (intersection_size > 0 ? intersection_size / (this.adjacency_list[beta].length - 1.)
						: 0);
			}
			// c_jacc = map__u__v__jaccard.get(b, beta, 0.);
			jac__sum_1 += c_jac;
			jac__MAX_1 = (c_jac > jac__MAX_1 ? c_jac : jac__MAX_1);
			//
			jac_minus_one__sum_1 += c_jac_minus_one;
			jac_minus_one__MAX_1 = (c_jac_minus_one > jac_minus_one__MAX_1 ? c_jac_minus_one : jac_minus_one__MAX_1);
			//
			con__sum_1 += c_con;
			con__MAX_1 = (c_con > con__MAX_1 ? c_con : con__MAX_1);
			//
			con_minus_one__sum_1 += c_con_minus_one;
			con_minus_one__MAX_1 = (c_con_minus_one > con_minus_one__MAX_1 ? c_con_minus_one : con_minus_one__MAX_1);
		}
		for (index = 0; index < neigs_b.length; index++) {
			alpha = neigs_b[index];
			intersection_size = map__u__v__intersection.get(alpha, a, 0.);
			c_jac = intersection_size / (deg_a + this.adjacency_list[alpha].length - intersection_size);
			c_jac_minus_one = intersection_size / (deg_a + this.adjacency_list[alpha].length - intersection_size - 1.);
			c_con = (intersection_size > 0 ? intersection_size / (this.adjacency_list[alpha].length) : 0);
			c_con_minus_one = 0;
			if ((this.adjacency_list[alpha].length - 1.) > 0) {
				c_con_minus_one = (intersection_size > 0 ? intersection_size / (this.adjacency_list[alpha].length - 1.)
						: 0);
			}
			// c_jacc = map__u__v__jaccard.get(alpha, a, 0.);
			jac__sum_2 += c_jac;
			jac__MAX_2 = (c_jac > jac__MAX_2 ? c_jac : jac__MAX_2);
			//
			jac_minus_one__sum_2 += c_jac_minus_one;
			jac_minus_one__MAX_2 = (c_jac_minus_one > jac_minus_one__MAX_2 ? c_jac_minus_one : jac_minus_one__MAX_2);
			//
			con__sum_2 += c_con;
			con__MAX_2 = (c_con > con__MAX_2 ? c_con : con__MAX_2);
			//
			con_minus_one__sum_2 += c_con_minus_one;
			con_minus_one__MAX_2 = (c_con_minus_one > con_minus_one__MAX_2 ? c_con_minus_one : con_minus_one__MAX_2);
		}
		//
		// SUM
		double sim__jac__sum__sum = jac__sum_1 + jac__sum_2;
		double sim__jac_minus_one__sum__sum = jac_minus_one__sum_1 + jac_minus_one__sum_2;
		double sim__con__sum__sum = con__sum_1 + con__sum_2;
		double sim__con_minus_one__sum__sum = con_minus_one__sum_1 + con_minus_one__sum_2;
		//
		jac__avg_1 = jac__sum_1 / neigs_a.length;
		jac__avg_2 = jac__sum_2 / neigs_b.length;
		jac_minus_one__avg_1 = jac_minus_one__sum_1 / neigs_a.length;
		jac_minus_one__avg_2 = jac_minus_one__sum_2 / neigs_b.length;
		con__avg_1 = con__sum_1 / neigs_a.length;
		con__avg_2 = con__sum_2 / neigs_b.length;
		con_minus_one__avg_1 = con_minus_one__sum_1 / neigs_a.length;
		con_minus_one__avg_2 = con_minus_one__sum_2 / neigs_b.length;
		//
		double sim__jac__avg__sum = jac__avg_1 + jac__avg_2;
		double sim__con__avg__sum = con__avg_1 + con__avg_2;
		double sim__jac_minus_one__avg__sum = jac_minus_one__avg_1 + jac_minus_one__avg_2;
		double sim__con_minus_one__avg__sum = con_minus_one__avg_1 + con_minus_one__avg_2;
		//
		double sim__jac__MAX__sum = jac__MAX_1 + jac__MAX_2;
		double sim__con__MAX__sum = con__MAX_1 + con__MAX_2;
		double sim__jac_minus_one__MAX__sum = jac_minus_one__MAX_1 + jac_minus_one__MAX_2;
		double sim__con_minus_one__MAX__sum = con_minus_one__MAX_1 + con_minus_one__MAX_2;
		//
		// AVG
//		double sim__jac__sum__avg = sim__jac__sum__sum / 2.;
//		double sim__jac__avg__avg = sim__jac__avg__sum / 2.;
//		double sim__jac__MAX__avg = sim__jac__MAX__sum / 2.;
		//
		// MAX
		double sim__jac__sum__MAX = (jac__sum_1 >= jac__sum_2 ? jac__sum_1 : jac__sum_2);
		double sim__con__sum__MAX = (con__sum_1 >= con__sum_2 ? con__sum_1 : con__sum_2);
		double sim__jac_minus_one__sum__MAX = (jac_minus_one__sum_1 >= jac_minus_one__sum_2 ? jac_minus_one__sum_1
				: jac_minus_one__sum_2);
		double sim__con_minus_one__sum__MAX = (con_minus_one__sum_1 >= con_minus_one__sum_2 ? con_minus_one__sum_1
				: con_minus_one__sum_2);
		//
		double sim__jac__avg__MAX = (jac__avg_1 >= jac__avg_2 ? jac__avg_1 : jac__avg_2);
		double sim__con__avg__MAX = (con__avg_1 >= con__avg_2 ? con__avg_1 : con__avg_2);
		double sim__jac_minus_one__avg__MAX = (jac_minus_one__avg_1 >= jac_minus_one__avg_2 ? jac_minus_one__avg_1
				: jac_minus_one__avg_2);
		double sim__con_minus_one__avg__MAX = (con_minus_one__avg_1 >= con_minus_one__avg_2 ? con_minus_one__avg_1
				: con_minus_one__avg_2);
		//
		double sim__jac__MAX__MAX = (jac__MAX_1 >= jac__MAX_2 ? jac__MAX_1 : jac__MAX_2);
		double sim__con__MAX__MAX = (con__MAX_1 >= con__MAX_2 ? con__MAX_1 : con__MAX_2);
		double sim__jac_minus_one__MAX__MAX = (jac_minus_one__MAX_1 >= jac_minus_one__MAX_2 ? jac_minus_one__MAX_1
				: jac_minus_one__MAX_2);
		double sim__con_minus_one__MAX__MAX = (con_minus_one__MAX_1 >= con_minus_one__MAX_2 ? con_minus_one__MAX_1
				: con_minus_one__MAX_2);
		//
		// weighted-AVG
		double sim__jac__sum__wavg = sim__jac__sum__sum / (neigs_a.length + neigs_b.length);
		double sim__con__sum__wavg = sim__con__sum__sum / (neigs_a.length + neigs_b.length);
		double sim_minus_one__jac__sum__wavg = sim__jac_minus_one__sum__sum / (neigs_a.length + neigs_b.length);
		double sim_minus_con__sum__wavg = sim__con_minus_one__sum__sum / (neigs_a.length + neigs_b.length);
//		double sim__jac__avg__wavg = 0.;
//		double sim__jac__MAX__wavg = 0.;
		//
		//
		//
		//
		int cursor = 0;
//		output_results[cursor++] = A3;
//		output_results[cursor++] = L3;
//		output_results[cursor++] = CH1L3;
//		output_results[cursor++] = CH2L3;
		output_results[cursor++] = sim__jac__sum__sum;
//		output_results[cursor++] = L3E;
		// ---
//		output_results[cursor++] = sim__jac__avg__sum;
		output_results[cursor++] = sim__jac__MAX__sum;
//		output_results[cursor++] = sim__jac__sum__MAX;
//		output_results[cursor++] = sim__jac__avg__MAX;
		output_results[cursor++] = sim__jac__MAX__MAX;
//		output_results[cursor++] = sim__jac__sum__wavg;
		//
//		output_results[cursor++] = sim__jac_minus_one__sum__sum;
//		output_results[cursor++] = sim__jac_minus_one__avg__sum;
//		output_results[cursor++] = sim__jac_minus_one__MAX__sum;
//		output_results[cursor++] = sim__jac_minus_one__sum__MAX;
//		output_results[cursor++] = sim__jac_minus_one__avg__MAX;
//		output_results[cursor++] = sim__jac_minus_one__MAX__MAX;
//		output_results[cursor++] = sim_minus_one__jac__sum__wavg;
		//
		//
		//
		//
//		output_results[cursor++] = sim__con__sum__sum;
//		output_results[cursor++] = sim__con__avg__sum;
		output_results[cursor++] = sim__con__MAX__sum;
//		output_results[cursor++] = sim__con__sum__MAX;
//		output_results[cursor++] = sim__con__avg__MAX;
		output_results[cursor++] = sim__con__MAX__MAX;
//		output_results[cursor++] = sim__con__sum__wavg;
		//
//		output_results[cursor++] = sim__con_minus_one__sum__sum;
//		output_results[cursor++] = sim__con_minus_one__avg__sum;
//		output_results[cursor++] = sim__con_minus_one__MAX__sum;
//		output_results[cursor++] = sim__con_minus_one__sum__MAX;
//		output_results[cursor++] = sim__con_minus_one__avg__MAX;
//		output_results[cursor++] = sim__con_minus_one__MAX__MAX;
//		output_results[cursor++] = sim_minus_con__sum__wavg;
		//
		//
		//

		// ofd
//		System.out.println();
//		System.out.println(Arrays.toString(output_results));
//		System.out.println();
		//
		assert A3 < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(A3));
		assert L3 < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(L3));
		assert CH1L3 < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(CH1L3));
		assert CH2L3 < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(CH2L3));
		assert sim__jac__sum__sum < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(sim__jac__sum__sum));
		assert L3E < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(L3E));
		//
		return;
	}

	/**
	 * 
	 * @param sorted_array_1
	 * @param sorted_array_2
	 * @return
	 */
	protected int update_inner_degree_and_add_a_b_internal_node_couples_list(int node_u, int node_v, int node_a,
			int[] adj_list_a, int[] adj_list_v, SimpleIntDoubleMapCountingToInfinity map__node__INTERNAL_DEGREE,
			int[] list_node_a_with_size, int[] list_node_b_with_size,
			SimpleSetUnorderedIntIntCouplesCountingToInfinity set__node_a__node_b) {
		int num_common_elements = 0;
		int i = 0;
		int j = 0;
		while (true) {
			if (i >= adj_list_a.length) {
				break;
			}
			if (j >= adj_list_v.length) {
				break;
			}
			//
			if (adj_list_a[i] == node_u || adj_list_a[i] == node_a || adj_list_a[i] == node_v) {
				i++;
				continue;
			}
			if (adj_list_v[j] == node_u || adj_list_v[j] == node_a || adj_list_v[j] == node_v) {
				j++;
				continue;
			}
			//
			//
			if (adj_list_a[i] == adj_list_v[j]) {
				//
				num_common_elements++;
				//
				// update list of a-b edges.
				list_node_a_with_size[list_node_a_with_size[0]] = node_a;
				list_node_b_with_size[list_node_b_with_size[0]] = adj_list_v[j];
				list_node_a_with_size[0]++;
				list_node_b_with_size[0]++;
				//
				//
				j++;
				i++;
				continue;
				//
			}
			if (adj_list_a[i] > adj_list_v[j]) {
				j++;
				continue;
			}
			if (adj_list_a[i] < adj_list_v[j]) {
				i++;
			}
		}
		//
		// Compute the INTERNAL_DEGREE of each a-b nodes.
		int node_b;
		for (i = 1; i < list_node_a_with_size[0]; i++) {
			node_a = list_node_a_with_size[i];
			node_b = list_node_b_with_size[i];
			if (set__node_a__node_b.informativeAdd(node_a, node_b)) {
				map__node__INTERNAL_DEGREE.addValue(node_a, 1);
				map__node__INTERNAL_DEGREE.addValue(node_b, 1);
			}
		}
		//
		return num_common_elements;
	}

	/**
	 * 
	 * @param u
	 * @param v
	 * @param set_of_common_neighbours
	 * @param output_results
	 * @return
	 */
	protected void compute_L2_metrics(int u, int v, SimpleIntSetForScanCountingToInfinity set_of_common_neighbours,
			boolean u_and_v_are_connected, double[] output_results) {
		//
		Arrays.fill(output_results, 0.);
		//
		int degree_u = this.adjacency_list[u].length;
		int degree_v = this.adjacency_list[v].length;
		//
		// ---------------------------------------------
		// Fill the set of common neighbours.
		int num_common_neighbours = 0;
		set_of_common_neighbours.clear();
		int[] adj_list_of_u = this.adjacency_list[u];
		int[] adj_list_of_v = this.adjacency_list[v];
		num_common_neighbours = this.update_set_with_common_elements(adj_list_of_u, adj_list_of_v,
				set_of_common_neighbours);
		if (num_common_neighbours == 0) {
			return;
		}
		//
		assert num_common_neighbours == set_of_common_neighbours.size();
		//
		//
		// RA index
		double AA_index = 0.;
		double resources_allocation_index = 0.;
		int cardinality_union_of_neighbours = (degree_u + degree_v - num_common_neighbours);
		double JA_index = ((double) num_common_neighbours) / cardinality_union_of_neighbours;
		if (u_and_v_are_connected) {
			cardinality_union_of_neighbours = ((degree_u - 1) + (degree_v - 1) - num_common_neighbours);
			JA_index = ((double) num_common_neighbours) / cardinality_union_of_neighbours;
		}
		//
		double CH1L2_index = 0.;
		double CH2L2_index = 0.;
		//
		int c_common_neighbour = 0;
		int c_common_neighbour_degree = 0;
		int c_common_neighbour_INTERNAL_degree = 0;
		int c_common_neighbour_EXTERNAL_degree = 0;
		int c_neig_neig = 0;
		int[] set_of_common_neighbours_as_array = set_of_common_neighbours.get_list_of_keys();
		for (int i = 0; i < num_common_neighbours; i++) {
			c_common_neighbour = set_of_common_neighbours_as_array[i];
			c_common_neighbour_degree = this.get_degree(c_common_neighbour);
			//
			// RA
			resources_allocation_index += 1. / c_common_neighbour_degree;
			AA_index += 1. / Math.log(c_common_neighbour_degree);
			//
			// CH1L2 and CH2L2
			c_common_neighbour_INTERNAL_degree = 0;
			for (int j = 0; j < this.adjacency_list[c_common_neighbour].length; j++) {
				c_neig_neig = this.adjacency_list[c_common_neighbour][j];
				if (set_of_common_neighbours.contains(c_neig_neig)) {
					c_common_neighbour_INTERNAL_degree++;
				}
			}
			c_common_neighbour_EXTERNAL_degree = c_common_neighbour_degree - c_common_neighbour_INTERNAL_degree - 2;
			//
			CH1L2_index += ((double) c_common_neighbour_INTERNAL_degree) / c_common_neighbour_degree;
			CH2L2_index += (c_common_neighbour_INTERNAL_degree + 1.) / (c_common_neighbour_EXTERNAL_degree + 1.);
			//
		}
		output_results[0] = AA_index;
		output_results[1] = resources_allocation_index;
		output_results[2] = JA_index;
		output_results[3] = CH1L2_index;
		output_results[4] = CH2L2_index;
		output_results[5] = num_common_neighbours;
		output_results[6] = cardinality_union_of_neighbours;
		//
		// ofd
//		if (CH2L2_index >= Double.POSITIVE_INFINITY) {
//			System.out.println();
//			System.out.println("--- OFD ---");
//			// System.out.println("" + );
//			System.out.println(Arrays.toString(output_results));
//			System.out.println();
//		}
//		System.out.println();
//		System.out.println(Arrays.toString(output_results));
//		System.out.println();
		//
		assert AA_index < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(AA_index));
		assert resources_allocation_index < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(resources_allocation_index));
		assert JA_index < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(JA_index));
		assert CH1L2_index < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(CH1L2_index));
		assert CH2L2_index < Double.POSITIVE_INFINITY;
		assert !(Double.isNaN(CH2L2_index));
		//
		return;
	}

	protected int update_set_with_common_elements(int[] sorted_array_1, int[] sorted_array_2,
			SimpleIntSetForScanCountingToInfinity set_of_elements) {
		int num_common_elements = 0;
		int i = 0;
		int j = 0;
		while (true) {
			if (i >= sorted_array_1.length) {
				break;
			}
			if (j >= sorted_array_2.length) {
				break;
			}
			if (sorted_array_1[i] == sorted_array_2[j]) {
				set_of_elements.add(sorted_array_1[i]);
				num_common_elements++;
				j++;
				i++;
				continue;
			}
			if (sorted_array_1[i] > sorted_array_2[j]) {
				j++;
				continue;
			}
			i++;
		}
		return num_common_elements;
	}

	protected double compute_JACCARD_between_two_adjacency_lists(int[] sorted_array_1, int[] sorted_array_2) {
		double num_of_common_elements = this.compute_nummber_of_common_elements(sorted_array_1, sorted_array_2);
		return (num_of_common_elements / (sorted_array_1.length + sorted_array_2.length - num_of_common_elements));
	}

	/**
	 * 
	 * @param sorted_array_1
	 * @param sorted_array_2
	 * @return
	 */
	protected int compute_nummber_of_common_elements(int[] sorted_array_1, int[] sorted_array_2) {
		int num_common_elements = 0;
		int i = 0;
		int j = 0;
		while (true) {
			if (i >= sorted_array_1.length) {
				break;
			}
			if (j >= sorted_array_2.length) {
				break;
			}
			if (sorted_array_1[i] == sorted_array_2[j]) {
				num_common_elements++;
				j++;
				i++;
				continue;
			}
			if (sorted_array_1[i] > sorted_array_2[j]) {
				j++;
				continue;
			}
			i++;
		}
		return num_common_elements;
	}

	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// -------- O L D M E T H O D S --------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------
	// ---------------------------------------

	public void create_graph____V0(String graph_input_file_name, String POSITIVE_couples_input_file_name)
			throws Exception {
		//
		this.create_splitted_graph__V0(graph_input_file_name, 1.1);
		//
		this.set_POSITIVE_couples = new HashSet<UnorderedTuple>();
		this.fast_set_POSITIVE_couples = new SimpleSetUnorderedIntIntCouples(this.max_node_id);
		BufferedReader br = new BufferedReader(new FileReader(POSITIVE_couples_input_file_name), 100000000);
		int node_u_outer_id = -1;
		int node_v_outer_id = -1;
		int node_u_inner_id = -1;
		int node_v_inner_id = -1;
		String line = null;
		while ((line = br.readLine()) != null) {
			node_u_outer_id = Integer.parseInt(line.substring(0, line.indexOf("\t")));
			node_v_outer_id = Integer.parseInt(line.substring(line.indexOf("\t") + 1));
			//
//			System.out.println();
//			System.out.println(node_u_outer_id + " " + node_v_outer_id);
			//
			node_u_inner_id = this.map__node_outer_id__node_inner_id.get(node_u_outer_id);
			node_v_inner_id = this.map__node_outer_id__node_inner_id.get(node_v_outer_id);
			//
			this.set_POSITIVE_couples.add(new UnorderedTuple(node_u_inner_id, node_v_inner_id));
			if (node_u_inner_id < node_v_inner_id) {
				fast_set_POSITIVE_couples.add(node_u_inner_id, node_v_inner_id);
//				System.out.println(node_u_inner_id + " " + node_v_inner_id);
			} else {
				fast_set_POSITIVE_couples.add(node_v_inner_id, node_u_inner_id);
//				System.out.println(node_v_inner_id + " " + node_u_inner_id);
			}
			//
		}
		br.close();
		//
		return;
	}

	// --------------------------------------
	public String compute_feature_vectors_for_all_unedged_node_couples__V_0() throws Exception {
		long TimeStamp = System.currentTimeMillis();
		String output_file_name = "/Users/ikki/Dropbox/PPI/PPIChallenge/sw/sw/TR_and_TEST/HuRI_UNSUP/g_te__nodes_couples_in_the_fetures_space__"
				+ TimeStamp + ".tsv";
		BufferedWriter bw = new BufferedWriter(new FileWriter(output_file_name), 100000000);
		String header = "";
		header = "class	u	v	num_paths_length_3	degree_normalized_num_paths_length_3	f__Adamic_Addar	f__Resource_Allocation	f__Jaccard_Coefficient	f__Shortest_Path_length	f__Preferential_Attachment	num_paths_length_3_SIMPLE_degree_normalization	num_paths_length_3_ln_degree_normalization	num_paths_length_3_lg_degree_normalization	num_paths_length_3_lnp1_degree_normalization	num_paths_length_3_LOG_degree_normalization	map__u_v__CH1_L2	map__u_v__CH2_L2	map__u_v__CH1_L3	map__u_v__CH2_L3	map__u_v__CH2_L2_adr	map__u_v__CH2_L3_adr";
		bw.write(header + "\n");
		//
		SimpleIntSetForScanCountingToInfinity set_of_common_neighbours = new SimpleIntSetForScanCountingToInfinity(
				this.max_node_id);
		SimpleIntSetForScanCountingToInfinity set_of_induced_nodes_from_L3 = new SimpleIntSetForScanCountingToInfinity(
				this.max_node_id);
		SimpleIntDoubleMapCountingToInfinity map__node__INTERNAL_DEGREE = new SimpleIntDoubleMapCountingToInfinity(
				this.max_node_id);
		//
		int[] list_node_a_with_size = new int[2 + this.get_num_edges()];
		int[] list_node_b_with_size = new int[2 + this.get_num_edges()];
		Arrays.fill(list_node_a_with_size, 0);
		Arrays.fill(list_node_b_with_size, 0);
		//
		double[] distance_independent_metrics = new double[1];// PA
		double[] L2_metrics = new double[8];
		double[] L3_metrics = new double[40];
		double[] all_zeros = new double[100];
		Arrays.fill(all_zeros, 0.);
		//
		int u = 0;
		int v = 0;
		int c_class = 0;
		String c_row = "";
		//
		SimpleIntSetCountingToInfinity set_nodes_distance_1_or_2_or_3 = new SimpleIntSetCountingToInfinity(
				this.max_node_id);
		int ii, jj, kk, neigh_dist_1, neigh_dist_2, neigh_dist_3, set_nodes_distance_1_or_2_or_3__SIZE;
		int[] set_nodes_distance_1_or_2_or_3__AS_ARRAY;
		int previous_u = -1;
		long num_couples_processed_so_far = 0;
		long total_num_couples = (this.num_nodes * (this.num_nodes - 1)) / 2;
		for (u = 0; u < this.max_node_id; u++) {
			//
			// collect all nodes at distance <=3 from node u.
			set_nodes_distance_1_or_2_or_3.clear();
			for (ii = 0; ii < this.adjacency_list[u].length; ii++) {
				neigh_dist_1 = this.adjacency_list[u][ii];
				for (jj = 0; jj < this.adjacency_list[neigh_dist_1].length; jj++) {
					neigh_dist_2 = this.adjacency_list[neigh_dist_1][jj];
					if (neigh_dist_2 == u) {
						continue;
					}
					for (kk = 0; kk < this.adjacency_list[neigh_dist_2].length; kk++) {
						neigh_dist_3 = this.adjacency_list[neigh_dist_2][kk];
						if (neigh_dist_3 == neigh_dist_2) {
							continue;
						}
						if (u < neigh_dist_3) {
							//
							if (set_nodes_distance_1_or_2_or_3.informativeAdd(neigh_dist_3)) {
								//
								v = neigh_dist_3;
								if (this.fast_set_edges.contains(u, v)) {
									continue;
								}
								//
								if (this.fast_set_POSITIVE_couples.contains(u, v)) {
									c_class = 0;
								} else {
									c_class = 1;
								}
								//
								//
								this.compute_distance_independent_metrics(u, v, false, distance_independent_metrics);
								this.compute_L2_metrics(u, v, set_of_common_neighbours, false, L2_metrics);
//								this.compute_L3_metrics(u, v, set_of_induced_nodes_from_L3, map__node__INTERNAL_DEGREE,
//										L3_metrics);
								//
								if (L3_metrics[0] > 0) {
									L2_metrics[3] = 3; // Shortest-Path.
								}
								if (L2_metrics[0] > 0) {
									L2_metrics[3] = 2; // Shortest-Path.
								}
								//
//								this.create_and_write_output_row(u, v, bw, c_class, distance_independent_metrics,
//										L2_metrics, L3_metrics);
								//
							}
						}
					}
				}
			}
			//
//			set_nodes_distance_1_or_2_or_3__SIZE = set_nodes_distance_1_or_2_or_3.size();
//			set_nodes_distance_1_or_2_or_3__AS_ARRAY = set_nodes_distance_1_or_2_or_3.get_list_of_keys();
//			for (ii = 0; ii < set_nodes_distance_1_or_2_or_3__SIZE; ii++) {
//				num_couples_processed_so_far++;
//				//
//				v = set_nodes_distance_1_or_2_or_3__AS_ARRAY[ii];
//				//
//				
//			}
			//
			System.arraycopy(all_zeros, 0, L2_metrics, 0, L2_metrics.length);
			System.arraycopy(all_zeros, 0, L3_metrics, 0, L3_metrics.length);
			//
			for (v = u + 1; v <= this.max_node_id; v++) {
				num_couples_processed_so_far++;
				//
				if (set_nodes_distance_1_or_2_or_3.contains(v)) {
					continue;
				}
				//
				if (this.fast_set_edges.contains(u, v)) {
					continue;
				}
				//
				//
				//
				// if (this.set_POSITIVE_couples.contains(c_tuple)) {
				if (this.fast_set_POSITIVE_couples.contains(u, v)) {
					c_class = 0;
				} else {
					c_class = 1;
				}
				//
				//
				this.compute_distance_independent_metrics(u, v, false, distance_independent_metrics);
//				//
//				if (set_nodes_distance_1_or_2_or_3.contains(v)) {
//					this.compute_L2_metrics(u, v, set_of_common_neighbours, L2_metrics);
//					this.compute_L3_metrics(u, v, set_of_induced_nodes_from_L3, map__node__INTERNAL_DEGREE, L3_metrics);
//				} else {
//					if (L2_metrics[0] > 0) {
//						System.arraycopy(all_zeros, 0, L2_metrics, 0, L2_metrics.length);
//					}
//					if (L3_metrics[0] > 0) {
//						System.arraycopy(all_zeros, 0, L3_metrics, 0, L3_metrics.length);
//					}
//				}
				//
				// Do not store the vectorized version of the current couple
				// if is a NotPositive couple and both L2 and L3 are 0.
//				if ((c_class == 1) && (L2_metrics[0] <= 0) && (L3_metrics[0] <= 0)) {
//					continue;
//				}
//				if (L3_metrics[0] > 0) {
//					L2_metrics[3] = 3; // Shortest-Path.
//				}
//				if (L2_metrics[0] > 0) {
//					L2_metrics[3] = 2; // Shortest-Path.
//				}
				//
				if (previous_u != u) {
					System.out.println();
					System.out.println("c_inner_node_id: " + u + "  max_inner_node_id: " + this.max_node_id);
					System.out.println(this.map__node_inner_id__node_outer_id[u]);
					System.out.println(
							"couples done/total= " + ((float) num_couples_processed_so_far / total_num_couples));
					System.out.println("  nodes done/total= " + ((float) u / this.num_nodes));
				}
				previous_u = u;
				//
//				this.create_and_write_output_row(u, v, bw, c_class, distance_independent_metrics, L2_metrics,
//						L3_metrics);
			}
		}
		bw.close();
		//
		return output_file_name;

	}

	public void create_splitted_graph__V0(String graph_input_file_name, double beta) throws Exception {
		//
		// Collect all the outer node id set.
		// compute the max outer node id.
		// set the max_node_id.
		Set<String> set__all_node_outer_id = new HashSet<String>();
		BufferedReader br = new BufferedReader(new FileReader(graph_input_file_name), 100000000);
		String line = null;
		// int max_node_outer_id = -1;
		String outer_node_id_u = "";
		String outer_node_id_v = "";
		int first_separator_index = 0;
		br.readLine();// skip the first line
		while ((line = br.readLine()) != null) {
			//
			first_separator_index = line.indexOf(",");
			outer_node_id_u = line.substring(0, first_separator_index);
			outer_node_id_v = line.substring(first_separator_index + 1);
			//
			set__all_node_outer_id.add(outer_node_id_u);
			set__all_node_outer_id.add(outer_node_id_v);
			//
		}
		this.max_node_id = set__all_node_outer_id.size() - 1;
		this.num_nodes = set__all_node_outer_id.size();
		br.close();
		//
		// create bijective mapping between inner and outer node ids.
		//
		// create the mapping from outer to inner node ids.
		List<String> sorted_list__all_node_outer_id = new ArrayList<String>(set__all_node_outer_id);
		set__all_node_outer_id = null;
		Collections.sort(sorted_list__all_node_outer_id);
		this.map__node_outer_id__node_inner_id = new HashMap<String, Integer>(2 * this.num_nodes);
		int node_inner_id = 0;
		for (String node_outer_id : sorted_list__all_node_outer_id) {
			this.map__node_outer_id__node_inner_id.put(node_outer_id, node_inner_id);
			node_inner_id++;
		}
		sorted_list__all_node_outer_id = null;
		//
		// create the mapping from inner to outer node ids.
		this.map__node_inner_id__node_outer_id = new String[this.max_node_id + 1];
		String node_outer_id = "";
		node_inner_id = 0;
		for (Map.Entry<String, Integer> outer_inner_node_id_pair : this.map__node_outer_id__node_inner_id.entrySet()) {
			node_outer_id = outer_inner_node_id_pair.getKey();
			node_inner_id = outer_inner_node_id_pair.getValue();
			this.map__node_inner_id__node_outer_id[node_inner_id] = node_outer_id;
		}
		//
		// extract the set of ALL edges.
		Set<UnorderedTuple> set_ALL_edges = new HashSet<UnorderedTuple>();
		//
		br = new BufferedReader(new FileReader(graph_input_file_name), 100000000);
		String node_u_outer_id;
		String node_v_outer_id;
		int node_u_inner_id;
		int node_v_inner_id;
		br.readLine();// skip the first line
		while ((line = br.readLine()) != null) {
			//
			first_separator_index = line.indexOf(",");
			node_u_outer_id = line.substring(0, first_separator_index);
			node_v_outer_id = line.substring(first_separator_index + 1);
			//
			if (node_u_outer_id.equals(node_v_outer_id)) {
				continue;
			}
			//
			node_u_inner_id = this.map__node_outer_id__node_inner_id.get(node_u_outer_id);
			node_v_inner_id = this.map__node_outer_id__node_inner_id.get(node_v_outer_id);
			//
			set_ALL_edges.add(new UnorderedTuple(node_u_inner_id, node_v_inner_id));
			//
		}
		br.close();
		//
		// split the set of edges
		ArrayList<UnorderedTuple> shuffled_list_of_ALL_edges = new ArrayList<UnorderedTuple>(set_ALL_edges);
		set_ALL_edges = null;
		Collections.shuffle(shuffled_list_of_ALL_edges);
		//
		this.set_POSITIVE_couples = new HashSet<UnorderedTuple>();// TO REMOVE?
		this.set_edges = new HashSet<UnorderedTuple>();// TO REMOVE?
		this.fast_set_edges = new SimpleSetUnorderedIntIntCouples(this.max_node_id);
		this.fast_set_POSITIVE_couples = new SimpleSetUnorderedIntIntCouples(this.max_node_id);
		UnorderedTuple c_couple_of_inner_ids_of_u_and_v = null;
		for (int i = 0; i < shuffled_list_of_ALL_edges.size(); i++) {
			//
			c_couple_of_inner_ids_of_u_and_v = shuffled_list_of_ALL_edges.get(i);
			//
			// add only (1-beta) edges inside the set_POSITIVE_couples.
			if ((i + 1.) / shuffled_list_of_ALL_edges.size() <= beta) {
				// add the edge.
				this.set_edges.add(c_couple_of_inner_ids_of_u_and_v);
//				System.out.println(this.max_node_id);
//				System.out.println(c_couple_of_inner_ids_of_u_and_v.a + "__" + c_couple_of_inner_ids_of_u_and_v.b);
				this.fast_set_edges.add(c_couple_of_inner_ids_of_u_and_v.a, c_couple_of_inner_ids_of_u_and_v.b);
			} else {
				// add edge inside the set_POSITIVE_couples.
				this.set_POSITIVE_couples.add(c_couple_of_inner_ids_of_u_and_v);
				this.fast_set_POSITIVE_couples.add(c_couple_of_inner_ids_of_u_and_v.a,
						c_couple_of_inner_ids_of_u_and_v.b);
			}
		}
		shuffled_list_of_ALL_edges = null;
		//
		// create the test graph considering the max node id
		Map<Integer, ArrayList<Integer>> inner_adjacency_list = new HashMap<Integer, ArrayList<Integer>>(
				2 * this.max_node_id);
		int u, v;
		for (UnorderedTuple c_edge : this.set_edges) {
			//
			u = c_edge.a;
			v = c_edge.b;
			if (!(inner_adjacency_list.containsKey(u))) {
				inner_adjacency_list.put(u, new ArrayList<Integer>());
			}
			if (!(inner_adjacency_list.containsKey(v))) {
				inner_adjacency_list.put(v, new ArrayList<Integer>());
			}
			inner_adjacency_list.get(u).add(v);
			inner_adjacency_list.get(v).add(u);
		}
		//
		// graph creation considering the ORIGINAL and GENERAL max_node_id.
		this.adjacency_list = new int[this.max_node_id + 1][];
		ArrayList<Integer> c_adj_list = null;
		int i = 0;
		for (int node_id = 0; node_id <= this.max_node_id; node_id++) {
			//
			if (inner_adjacency_list.containsKey(node_id)) {
				c_adj_list = inner_adjacency_list.get(node_id);
				Collections.sort(c_adj_list);
				//
				this.adjacency_list[node_id] = new int[c_adj_list.size()];
				i = 0;
				for (int c_neig : c_adj_list) {
					this.adjacency_list[node_id][i] = c_neig;
					i++;
				}
			} else {
				this.adjacency_list[node_id] = new int[0];
			}
		}
		return;
	}

}
