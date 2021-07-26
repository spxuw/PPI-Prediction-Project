package algos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import tools.SimpleIntSetCountingToInfinity;

public class PrimSeq {

	public static void main(String[] args) throws Exception {
		//
		String ppi_file_name = "/Users/ikki/DATASETS/PPIs/adri_PIPE/HuRI.csv";
		String ppi_validation_set_file_name = "/Users/ikki/DATASETS/PPIs/adri_PIPE/HuRI_OFFICIAL_PPI_CHALLENGE__splitted_fold_0____VALIDATION_SET__nodes_couples__1610783787680.tsv";
		String primary_structure_file_name = "/Users/ikki/DATASETS/PPIs/adri_PIPE/ensembl_id_to_protein_sequence.tsv";
		//
		PrimSeq ps = new PrimSeq();
		int shingle_width = 20;
		//
		ps.aaa(ppi_file_name, ppi_validation_set_file_name, primary_structure_file_name, shingle_width);
		//
		//
		return;
	}

	/**
	 * 
	 * @param ppi_file_name
	 * @param ppi_validation_set_file_name
	 * @param primary_structure_file_name
	 * @param shingle_width
	 * @throws Exception
	 */
	protected void aaa(String ppi_file_name, String ppi_validation_set_file_name, String primary_structure_file_name,
			int shingle_width) throws Exception {
		//
		Set<UnorderedTupleOfStrings> validation_set = new HashSet<UnorderedTupleOfStrings>();
		BufferedReader br = new BufferedReader(new FileReader(ppi_validation_set_file_name), 10000000);
		br.readLine();// skip the first line
		String line;
		String[] record;
		String prot_u_id, prot_v_id;
		while ((line = br.readLine()) != null) {
			record = line.split("\t");
			prot_u_id = record[1].toUpperCase();
			prot_v_id = record[2].toUpperCase();
			//
			if (prot_u_id.equals(prot_v_id)) {
				continue;
			}
			//
			validation_set.add(new UnorderedTupleOfStrings(prot_u_id, prot_v_id));
			//
		}
		br.close();
		//
		System.out.println();
		System.out.println("validation_set.size() " + validation_set.size());
		System.out.println();
		//
		//
		//
		Map<String, HashSet<String>> ppi_as_map__protein_id__set_proteins_ids = new HashMap<String, HashSet<String>>();
		br = new BufferedReader(new FileReader(ppi_file_name), 100000000);
		br.readLine();// skip the first line
		while ((line = br.readLine()) != null) {
			record = line.split(",");
			prot_u_id = record[0];
			prot_v_id = record[1];
			//
			if (prot_u_id.equals(prot_v_id)) {
				continue;
			}
			if (validation_set.contains(new UnorderedTupleOfStrings(prot_u_id, prot_v_id))) {
				continue;
			}
			//
			this.addToCoupleToTheMap(ppi_as_map__protein_id__set_proteins_ids, prot_u_id, prot_v_id);
			//
		}
		br.close();
		//
		System.out.println();
		System.out.println(
				"ppi_as_map__protein_id__set_proteins_ids.size() " + ppi_as_map__protein_id__set_proteins_ids.size());
		System.out.println();
//		System.out.println();
//		System.out.println("map__node_id__list_node_ids.size() " + map__node_id__list_node_ids.size());
//		for (Map.Entry<String, HashSet<String>> pair : map__node_id__list_node_ids.entrySet()) {
//			System.out.println(" " + pair.getKey() + " -- " + pair.getValue());
//		}
//		System.out.println();
		//
		//
		//
		//
		// Map<String, char[]> map__protein_id__primary_structure = new HashMap<String,
		// char[]>();
		Map<String, String> map__protein_id__primary_structure = new HashMap<String, String>();
		br = new BufferedReader(new FileReader(primary_structure_file_name), 100000000);
		br.readLine();// skip the first line
		String prot_id, prot_primary_sequence_as_string;
//		char[] prot_primary_sequence;
		while ((line = br.readLine()) != null) {
			record = line.split("\t");
			prot_id = record[0];
			//
			if (!ppi_as_map__protein_id__set_proteins_ids.containsKey(prot_id)) {
				continue;
			}
			//
			prot_primary_sequence_as_string = record[1];
			if (prot_primary_sequence_as_string.length() <= 1) {
				continue;
			}
			//
			//
//			prot_primary_sequence = prot_primary_sequence_as_string.toCharArray();
			//
			if (!map__protein_id__primary_structure.containsKey(prot_id)) {
				map__protein_id__primary_structure.put(prot_id, prot_primary_sequence_as_string);
			}
			//
		}
		br.close();
		//
//		System.out.println();
//		System.out.println("map__protein_id__primary_structure.size() " + map__protein_id__primary_structure.size());
//		for (Map.Entry<String, String> pair : map__protein_id__primary_structure.entrySet()) {
//			System.out.println(" " + pair.getKey() + " -- " + pair.getValue());
//		}
//		System.out.println();
		//
		//
		//
		Map<String, HashSet<String>> map__shingle__set_of_proteins_ids = new HashMap<String, HashSet<String>>();
		Map<String, HashMap<String, Integer>> map__shingle__MULTISET_of_proteins_ids = new HashMap<String, HashMap<String, Integer>>();
////	Map<String, ArrayList<String>> map__protein_id__collection_of_shingles = new HashMap<String, ArrayList<String>>();
		int i;
		String c_protein_id, c_primary_structure, c_shingle;
		for (Map.Entry<String, String> protein_id__protein_primary_structure : map__protein_id__primary_structure
				.entrySet()) {
			c_protein_id = protein_id__protein_primary_structure.getKey();
			c_primary_structure = protein_id__protein_primary_structure.getValue();
//			System.out.println(" " + c_protein_id + " -- " + c_primary_structure);
			//
			if (c_primary_structure.length() <= shingle_width) {
				c_shingle = c_primary_structure;

			}
			//
			// Scan of all shingles
			for (i = 0; i < c_primary_structure.length() - shingle_width; i++) {
				c_shingle = c_primary_structure.substring(i, i + shingle_width);
				this.addProteinToShingle(map__shingle__set_of_proteins_ids, c_shingle, c_protein_id);
				this.addProteinToShingleMultiset(map__shingle__MULTISET_of_proteins_ids, c_shingle, c_protein_id);
			}
			//
		}
		//
		System.out.println();
		System.out.println("map__shingle__set_of_proteins_ids.size()      " + map__shingle__set_of_proteins_ids.size());
		System.out.println(
				"map__shingle__MULTISET_of_proteins_ids.size() " + map__shingle__MULTISET_of_proteins_ids.size());
		System.out.println();
		System.out.println();
		//
		//
		//
		Map<String, HashSet<String>> map__shingle__set_of_NEIGHBOUR_proteins_ids = new HashMap<String, HashSet<String>>();
		HashSet<String> c_set_of_proteins = null;
		for (String shingle : map__shingle__set_of_proteins_ids.keySet()) {
			c_set_of_proteins = new HashSet<String>();
			for (String protein : map__shingle__set_of_proteins_ids.get(shingle)) {
				c_set_of_proteins.addAll(ppi_as_map__protein_id__set_proteins_ids.get(protein));
			}
			map__shingle__set_of_NEIGHBOUR_proteins_ids.put(shingle, c_set_of_proteins);
		}
		//
		System.out.println();
		System.out.println("map__shingle__set_of_NEIGHBOUR_proteins_ids.size() "
				+ map__shingle__set_of_NEIGHBOUR_proteins_ids.size());
		System.out.println();
		System.out.println();
		//
		//
		//
//		for (String key : map__shingle__set_of_proteins_ids.keySet()) {
//			map__shingle__set_of_proteins_ids.get(key)
//		}
		//
		//
		//
		// For all proteins A:
		//
//		String[] ALL_proteins_id = new String[map__protein_id__primary_structure.size()];
//		i = 0;
//		for (String protein_id : map__protein_id__primary_structure.keySet()) {
//			ALL_proteins_id[i] = protein_id;
//			i++;
//		}
//		Arrays.sort(ALL_proteins_id);
		//
//		this.computeData_0(shingle_width, ALL_proteins_id, map__protein_id__primary_structure,
//				map__shingle__set_of_NEIGHBOUR_proteins_ids, map__shingle__set_of_proteins_ids,
//				ppi_as_map__protein_id__set_proteins_ids);
		//
		//
		//
		//
		//
		String[] ALL_proteins_id = new String[ppi_as_map__protein_id__set_proteins_ids.size()];
		i = 0;
		for (String protein_id : ppi_as_map__protein_id__set_proteins_ids.keySet()) {
			ALL_proteins_id[i] = protein_id;
			i++;
		}
		Arrays.sort(ALL_proteins_id);
		map__protein_id__primary_structure = null;
		ppi_as_map__protein_id__set_proteins_ids = null;
		validation_set = null;
		System.gc();
//		this.computeData_2(map__shingle__MULTISET_of_proteins_ids, map__shingle__set_of_NEIGHBOUR_proteins_ids,
//				map__shingle__set_of_proteins_ids, ALL_proteins_id);
		//
		this.computeData_5(map__shingle__MULTISET_of_proteins_ids, map__shingle__set_of_NEIGHBOUR_proteins_ids,
				map__shingle__set_of_proteins_ids, ALL_proteins_id);
		//

		System.out.println();
		return;
	}

	/**
	 * 
	 * @param map__shingle__MULTISET_of_proteins_ids
	 * @param map__shingle__set_of_NEIGHBOUR_proteins_ids
	 * @param map__shingle__set_of_proteins_ids
	 */
	protected void computeData_3(Map<String, HashMap<String, Integer>> map__shingle__MULTISET_of_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_NEIGHBOUR_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_proteins_ids, String[] ALL_proteins_id) {
		//
//		new OrderedTupleOfStrings();
		HashMap<String, HashSet<String>> map__NEIGHBOUR_protein_id__set_of_shingles = new HashMap<String, HashSet<String>>();
		for (String c_shingle : map__shingle__set_of_NEIGHBOUR_proteins_ids.keySet()) {
			//
			for (String c_prot : map__shingle__set_of_NEIGHBOUR_proteins_ids.get(c_shingle)) {
				if (!map__NEIGHBOUR_protein_id__set_of_shingles.containsKey(c_prot)) {
					map__NEIGHBOUR_protein_id__set_of_shingles.put(c_prot, new HashSet<String>());
				}
				map__NEIGHBOUR_protein_id__set_of_shingles.get(c_prot).add(c_shingle);
			}
		}
		System.out.println();
		System.out.println("map__NEIGHBOUR_protein_id__set_of_shingles.size() "
				+ map__NEIGHBOUR_protein_id__set_of_shingles.size());
		System.out.println();
		//
		HashMap<String, HashSet<String>> map__protein_id__set_of_shingles = new HashMap<String, HashSet<String>>();
		for (String c_shingle : map__shingle__set_of_proteins_ids.keySet()) {
			//
			for (String c_prot : map__shingle__set_of_proteins_ids.get(c_shingle)) {
				if (!map__protein_id__set_of_shingles.containsKey(c_prot)) {
					map__protein_id__set_of_shingles.put(c_prot, new HashSet<String>());
				}
				map__protein_id__set_of_shingles.get(c_prot).add(c_shingle);
			}
		}
		System.out.println();
		System.out.println("map__protein_id__set_of_shingles.size() " + map__protein_id__set_of_shingles.size());
		System.out.println();
		//
		//
		// HashSet<OrderedTupleOfStrings> set_of_S_1_S_2 = new
		// HashSet<OrderedTupleOfStrings>();
		HashSet<String> set_of_S_1_S_2 = new HashSet<String>(1000000);
		OrderedTupleOfStrings c_couple_of_shingles = null;
		HashSet<String> set_of_NEIGHBOUR_proteins_ids = null;
		HashSet<String> set_of_proteins_ids = null;
		int int_value, freq_S_1_in_A, freq_S_2_in_B, int_value_with_frequencies;
		String prot_A, prot_B;
		Map<String, Integer> map__A__freq;
		Map<String, Integer> map__B__freq;
		int num_int_zero = 0;
		int counter = 0;
		for (String c_prot : ALL_proteins_id) {
			counter++;
			System.out.println(((float) counter) / ALL_proteins_id.length);
			System.out.println("set_of_S_1_S_2.size() " + set_of_S_1_S_2.size());
			System.out.println();
			if (!map__NEIGHBOUR_protein_id__set_of_shingles.containsKey(c_prot)
					|| !map__protein_id__set_of_shingles.containsKey(c_prot)) {
				continue;
			}
			//
			//
			for (String S_1 : map__NEIGHBOUR_protein_id__set_of_shingles.get(c_prot)) {
				//
				set_of_NEIGHBOUR_proteins_ids = map__shingle__set_of_NEIGHBOUR_proteins_ids.get(S_1);
				map__A__freq = map__shingle__MULTISET_of_proteins_ids.get(S_1);
				//
				for (String S_2 : map__protein_id__set_of_shingles.get(c_prot)) {
					// c_couple_of_shingles = new OrderedTupleOfStrings(S_1, S_2);
					if (!set_of_S_1_S_2.add(S_1 + S_2)) {
						continue;
					}
					//
					set_of_proteins_ids = map__shingle__set_of_proteins_ids.get(S_2);
					int_value = this.computeIntersectionSize(set_of_proteins_ids, set_of_NEIGHBOUR_proteins_ids);
					//
					if (int_value == 0) {
						num_int_zero++;
						System.out.println("znum_int_zero " + num_int_zero);
						continue;
					}
					//
//					System.out.println(S_1 + " " + S_2 + " " + int_value + " " + num_int_zero);
					//
					map__B__freq = map__shingle__MULTISET_of_proteins_ids.get(S_2);
					for (String A : map__A__freq.keySet()) {
						freq_S_1_in_A = map__A__freq.get(A);
						for (String B : map__B__freq.keySet()) {
							freq_S_2_in_B = map__B__freq.get(B);
							//
							int_value_with_frequencies = int_value * freq_S_1_in_A * freq_S_2_in_B;
							//
//							System.out.println(A + " " + B + " " + S_1 + " " + S_2 + "" + int_value + " "
//									+ int_value_with_frequencies);
//							System.out.println("num_int_zero " + num_int_zero);
//							System.out.println(((float) counter) / ALL_proteins_id.length);
						}
					}
				}
			}
			//
		}
		//
		return;
	}

	protected void computeData_4(Map<String, HashMap<String, Integer>> map__shingle__MULTISET_of_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_NEIGHBOUR_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_proteins_ids, String[] ALL_proteins_id) {
		//
//		new OrderedTupleOfStrings();
		HashMap<String, HashSet<String>> map__NEIGHBOUR_protein_id__set_of_shingles = new HashMap<String, HashSet<String>>();
		for (String c_shingle : map__shingle__set_of_NEIGHBOUR_proteins_ids.keySet()) {
			//
			for (String c_prot : map__shingle__set_of_NEIGHBOUR_proteins_ids.get(c_shingle)) {
				if (!map__NEIGHBOUR_protein_id__set_of_shingles.containsKey(c_prot)) {
					map__NEIGHBOUR_protein_id__set_of_shingles.put(c_prot, new HashSet<String>());
				}
				map__NEIGHBOUR_protein_id__set_of_shingles.get(c_prot).add(c_shingle);
			}
		}
		System.out.println();
		System.out.println("map__NEIGHBOUR_protein_id__set_of_shingles.size() "
				+ map__NEIGHBOUR_protein_id__set_of_shingles.size());
		System.out.println();
		//
		HashMap<String, HashSet<String>> map__protein_id__set_of_shingles = new HashMap<String, HashSet<String>>();
		for (String c_shingle : map__shingle__set_of_proteins_ids.keySet()) {
			//
			for (String c_prot : map__shingle__set_of_proteins_ids.get(c_shingle)) {
				if (!map__protein_id__set_of_shingles.containsKey(c_prot)) {
					map__protein_id__set_of_shingles.put(c_prot, new HashSet<String>());
				}
				map__protein_id__set_of_shingles.get(c_prot).add(c_shingle);
			}
		}
		System.out.println();
		System.out.println("map__protein_id__set_of_shingles.size() " + map__protein_id__set_of_shingles.size());
		System.out.println();
		//
		//
		// HashSet<OrderedTupleOfStrings> set_of_S_1_S_2 = new
		// HashSet<OrderedTupleOfStrings>();
		HashSet<String> set_of_S_1_S_2 = new HashSet<String>(1000000);
		OrderedTupleOfStrings c_couple_of_shingles = null;
		HashSet<String> set_of_NEIGHBOUR_proteins_ids = null;
		HashSet<String> set_of_proteins_ids = null;
		int int_value, freq_S_1_in_A, freq_S_2_in_B, int_value_with_frequencies;
		String prot_A, prot_B;
		Map<String, Integer> map__A__freq;
		Map<String, Integer> map__B__freq;
		int num_int_zero = 0;
		int counter = 0;
		for (String c_prot : ALL_proteins_id) {
			counter++;
			System.out.println(((float) counter) / ALL_proteins_id.length);
			System.out.println("set_of_S_1_S_2.size() " + set_of_S_1_S_2.size());
			System.out.println();
			if (!map__NEIGHBOUR_protein_id__set_of_shingles.containsKey(c_prot)
					|| !map__protein_id__set_of_shingles.containsKey(c_prot)) {
				continue;
			}
			//
			//
			for (String S_1 : map__NEIGHBOUR_protein_id__set_of_shingles.get(c_prot)) {
				//
				set_of_NEIGHBOUR_proteins_ids = map__shingle__set_of_NEIGHBOUR_proteins_ids.get(S_1);
				map__A__freq = map__shingle__MULTISET_of_proteins_ids.get(S_1);
				//
				for (String S_2 : map__protein_id__set_of_shingles.get(c_prot)) {
					// c_couple_of_shingles = new OrderedTupleOfStrings(S_1, S_2);
					if (!set_of_S_1_S_2.add(S_1 + S_2)) {
						continue;
					}
					//
					set_of_proteins_ids = map__shingle__set_of_proteins_ids.get(S_2);
					int_value = this.computeIntersectionSize(set_of_proteins_ids, set_of_NEIGHBOUR_proteins_ids);
					//
					if (int_value == 0) {
						num_int_zero++;
						System.out.println("znum_int_zero " + num_int_zero);
						continue;
					}
					//
//					System.out.println(S_1 + " " + S_2 + " " + int_value + " " + num_int_zero);
					//
					map__B__freq = map__shingle__MULTISET_of_proteins_ids.get(S_2);
					for (String A : map__A__freq.keySet()) {
						freq_S_1_in_A = map__A__freq.get(A);
						for (String B : map__B__freq.keySet()) {
							freq_S_2_in_B = map__B__freq.get(B);
							//
							int_value_with_frequencies = int_value * freq_S_1_in_A * freq_S_2_in_B;
							//
//							System.out.println(A + " " + B + " " + S_1 + " " + S_2 + "" + int_value + " "
//									+ int_value_with_frequencies);
//							System.out.println("num_int_zero " + num_int_zero);
//							System.out.println(((float) counter) / ALL_proteins_id.length);
						}
					}
				}
			}
			//
		}
		//
		return;
	}

	/**
	 * 
	 * @param map__shingle__MULTISET_of_proteins_ids
	 * @param map__shingle__set_of_NEIGHBOUR_proteins_ids
	 * @param map__shingle__set_of_proteins_ids
	 * @param ALL_proteins_outer_id
	 */
	protected void computeData_2(Map<String, HashMap<String, Integer>> map__shingle__MULTISET_of_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_NEIGHBOUR_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_proteins_ids, String[] ALL_proteins_outer_id) {
		//
		//
		// create the mapping from outer to inner protein ids.
		String[] map__protein_inner_id__protein_outer_id = Arrays.copyOf(ALL_proteins_outer_id,
				ALL_proteins_outer_id.length);
		Arrays.sort(map__protein_inner_id__protein_outer_id);
		//
		Map<String, Integer> map__outer_prot_id__inner_prot_id = new HashMap<String, Integer>(
				map__protein_inner_id__protein_outer_id.length * 2);

		String c_prot_outer_id;
		for (int c_prot_inner_id = 0; c_prot_inner_id < map__protein_inner_id__protein_outer_id.length; c_prot_inner_id++) {
			c_prot_outer_id = map__protein_inner_id__protein_outer_id[c_prot_inner_id];
			map__outer_prot_id__inner_prot_id.put(c_prot_outer_id, c_prot_inner_id);
		}
		//
		//
		//
		String[] set_ALL_shingles = new String[map__shingle__set_of_proteins_ids.size()];
		int i;
		i = 0;
		for (String shingle : map__shingle__set_of_proteins_ids.keySet()) {
			set_ALL_shingles[i] = shingle;
			i++;
		}
		Arrays.sort(set_ALL_shingles);
		//
		//
		System.out.println();
		System.out.println();
		System.out.println();
		HashMap<String, int[]> map__S_1__set_NEIGHBOUR_prots_ids_as_sorted_array = this
				.createMappingShingleIdSetProtsIdsAsSortedArray(set_ALL_shingles,
						map__shingle__set_of_NEIGHBOUR_proteins_ids, map__outer_prot_id__inner_prot_id);
		System.out.println();
		System.out.println("map__shingle__set_of_NEIGHBOUR_proteins_ids.size() "
				+ map__shingle__set_of_NEIGHBOUR_proteins_ids.size());
		System.out.println();
		HashMap<String, int[]> map__S_2__set_prots_ids_as_sorted_array = this
				.createMappingShingleIdSetProtsIdsAsSortedArray(set_ALL_shingles, map__shingle__set_of_proteins_ids,
						map__outer_prot_id__inner_prot_id);
		System.out.println();
		System.out.println(
				"map__S_2__set_prots_ids_as_sorted_array.size() " + map__S_2__set_prots_ids_as_sorted_array.size());
		System.out.println();
		//
		//
//		map__shingle__MULTISET_of_proteins_ids
		Map<String, int[]> map__shingle_id__paired_set_prots_ids = new HashMap<String, int[]>();
		Map<String, int[]> map__shingle_id__paired_set_prots_ids_freq = new HashMap<String, int[]>();
		int[] c_paired_set_prots_ids = null;
		int[] c_paired_set_prots_ids_freq = null;
		int c_inner_protein_id;
		for (String c_shingle_id : map__shingle__MULTISET_of_proteins_ids.keySet()) {
			c_paired_set_prots_ids = new int[map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id).size()];
			c_paired_set_prots_ids_freq = new int[map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id).size()];
			i = 0;
			for (String c_outer_protein_id : map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id).keySet()) {
				c_inner_protein_id = map__outer_prot_id__inner_prot_id.get(c_outer_protein_id);
				c_paired_set_prots_ids[i] = c_inner_protein_id;
				c_paired_set_prots_ids_freq[i] = map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id)
						.get(c_outer_protein_id);
				i++;
			}
			map__shingle_id__paired_set_prots_ids.put(c_shingle_id, c_paired_set_prots_ids);
			map__shingle_id__paired_set_prots_ids_freq.put(c_shingle_id, c_paired_set_prots_ids_freq);
		}
		System.out.println();
		System.out.println(
				"map__shingle_id__paired_set_prots_ids.size()      " + map__shingle_id__paired_set_prots_ids.size());
		System.out.println("map__shingle_id__paired_set_prots_ids_freq.size() "
				+ map__shingle_id__paired_set_prots_ids_freq.size());
		System.out.println();
		//
		//
		//
		map__shingle__MULTISET_of_proteins_ids = null;
		map__shingle__set_of_NEIGHBOUR_proteins_ids = null;
		map__shingle__set_of_proteins_ids = null;
		System.gc();
		//
		//
		int int_value, freq_S_1_in_A, freq_S_2_in_B, int_value_with_frequencies;
		String prot_A, prot_B;
		int num_int_zero = 0;
		int i_S_1, i_S_2;
		String S_1, S_2;
		int[] set_of_NEIGHBOUR_proteins_ids;
		int[] set_of_proteins_ids;
		int j;
		int[] S_1__paired_set_prots_ids;
		int[] S_1__paired_set_prots_ids_freq;
		int[] S_2__paired_set_prots_ids;
		int[] S_2__paired_set_prots_ids_freq;
		for (i_S_1 = 0; i_S_1 < set_ALL_shingles.length; i_S_1++) {
			S_1 = set_ALL_shingles[i_S_1];
			//
			System.out.println("i_S_1/(set_ALL_shingles.length= " + ((float) i_S_1) / (set_ALL_shingles.length));
			System.out.println("num_intersection_zero " + num_int_zero);
			System.out.println();
			//
			set_of_NEIGHBOUR_proteins_ids = map__S_1__set_NEIGHBOUR_prots_ids_as_sorted_array.get(S_1);
			S_1__paired_set_prots_ids = map__shingle_id__paired_set_prots_ids.get(S_1);
			S_1__paired_set_prots_ids_freq = map__shingle_id__paired_set_prots_ids_freq.get(S_1);
			//
			for (i_S_2 = 0; i_S_2 < set_ALL_shingles.length; i_S_2++) {
				S_2 = set_ALL_shingles[i_S_2];
				//
				set_of_proteins_ids = map__S_2__set_prots_ids_as_sorted_array.get(S_2);
				//
				int_value = this.compute_nummber_of_common_elements(set_of_proteins_ids, set_of_NEIGHBOUR_proteins_ids);
				//
				if (int_value == 0) {
					num_int_zero++;
					continue;
				}
				//
				//
				//
				// System.out.println(i_S_1 + " " + i_S_2 + " " + val);
				S_2__paired_set_prots_ids = map__shingle_id__paired_set_prots_ids.get(S_2);
				S_2__paired_set_prots_ids_freq = map__shingle_id__paired_set_prots_ids_freq.get(S_2);
				for (i = 0; i < S_1__paired_set_prots_ids.length; i++) {
					//
					freq_S_1_in_A = S_1__paired_set_prots_ids_freq[i];
					prot_A = map__protein_inner_id__protein_outer_id[S_1__paired_set_prots_ids[i]];
					//
					for (j = 0; j < S_2__paired_set_prots_ids.length; j++) {
						//
						freq_S_2_in_B = S_2__paired_set_prots_ids_freq[j];
						prot_B = map__protein_inner_id__protein_outer_id[S_2__paired_set_prots_ids[j]];
						//
						int_value_with_frequencies = int_value * freq_S_1_in_A * freq_S_2_in_B;
						//
//						System.out.println(i_S_1 + " " + i_S_2 + " " + prot_A + " " + prot_B + " " + int_value + " "
//								+ int_value_with_frequencies);
//						System.out.println("num_int_zero " + num_int_zero);
						//
					}
				}
			}
		}
		//
		return;
	}

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

	protected void computeData_5(Map<String, HashMap<String, Integer>> map__shingle__MULTISET_of_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_NEIGHBOUR_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_proteins_ids, String[] ALL_proteins_outer_id) {
		//
		// create the mapping from outer to inner protein ids.
		String[] map__protein_inner_id__protein_outer_id = Arrays.copyOf(ALL_proteins_outer_id,
				ALL_proteins_outer_id.length);
		Arrays.sort(map__protein_inner_id__protein_outer_id);
		//
		Map<String, Integer> map__outer_prot_id__inner_prot_id = new HashMap<String, Integer>(
				map__protein_inner_id__protein_outer_id.length * 2);
		String c_prot_outer_id;
		for (int c_prot_inner_id = 0; c_prot_inner_id < map__protein_inner_id__protein_outer_id.length; c_prot_inner_id++) {
			c_prot_outer_id = map__protein_inner_id__protein_outer_id[c_prot_inner_id];
			map__outer_prot_id__inner_prot_id.put(c_prot_outer_id, c_prot_inner_id);
		}
		//
		//
		// Create the set containing all shingles
		String[] set_ALL_shingles = new String[map__shingle__set_of_proteins_ids.size()];
		int i;
		i = 0;
		for (String shingle : map__shingle__set_of_proteins_ids.keySet()) {
			set_ALL_shingles[i] = shingle;
			i++;
		}
		Arrays.sort(set_ALL_shingles);
		//
		//
		System.out.println();
		System.out.println();
		System.out.println();
		//
		// creating the mapping shingles of type S_1
		// set of neighbours proteins to the ones
		// that have S_1 in their primary structure.
		HashMap<String, int[]> map__S_1__set_NEIGHBOUR_prots_ids_as_sorted_array = this
				.createMappingShingleIdSetProtsIdsAsSortedArray(set_ALL_shingles,
						map__shingle__set_of_NEIGHBOUR_proteins_ids, map__outer_prot_id__inner_prot_id);
		System.out.println();
		System.out.println("map__shingle__set_of_NEIGHBOUR_proteins_ids.size() "
				+ map__shingle__set_of_NEIGHBOUR_proteins_ids.size());
		System.out.println();
		//
		// creating the mapping shingles of type S_2
		// set of proteins
		// that have S_2 in their primary structure.
		HashMap<String, int[]> map__S_2__set_prots_ids_as_sorted_array = this
				.createMappingShingleIdSetProtsIdsAsSortedArray(set_ALL_shingles, map__shingle__set_of_proteins_ids,
						map__outer_prot_id__inner_prot_id);
		System.out.println();
		System.out.println(
				"map__S_2__set_prots_ids_as_sorted_array.size() " + map__S_2__set_prots_ids_as_sorted_array.size());
		System.out.println();
		//
		//
		//
		// creating the mapping protein INNER id
		// to the set of shingles of type S_2.
		String[][] map__prot_inner_id__set_of_S_2_as_array;
		int[][] map__prot_inner_id__set_of_S_2_INNER_id_as_array;
		HashMap<String, Integer> map_shingle__outer_id__inner_id = new HashMap<String, Integer>();
		HashMap<Integer, HashSet<String>> map__prot_inner_id__set_of_S_2 = new HashMap<Integer, HashSet<String>>();
		int[] c_set_prots_ids_as_sorted_array;
		int c_prot;
		int max_prot_INNER_id = 0;
		int max_inner_shingle_id = 0;
		for (String c_shingle : map__S_2__set_prots_ids_as_sorted_array.keySet()) {
			//
			if (!map_shingle__outer_id__inner_id.containsKey(c_shingle)) {
				map_shingle__outer_id__inner_id.put(c_shingle, max_inner_shingle_id);
				max_inner_shingle_id++;
			}
			//
			c_set_prots_ids_as_sorted_array = map__S_2__set_prots_ids_as_sorted_array.get(c_shingle);
			for (i = 0; i < c_set_prots_ids_as_sorted_array.length; i++) {
				c_prot = c_set_prots_ids_as_sorted_array[i];
				//
				if (c_prot > max_prot_INNER_id) {
					max_prot_INNER_id = c_prot;
				}
				//
				if (!map__prot_inner_id__set_of_S_2.containsKey(c_prot)) {
					map__prot_inner_id__set_of_S_2.put(c_prot, new HashSet<String>());
				}
				map__prot_inner_id__set_of_S_2.get(c_prot).add(c_shingle);
				//
			}
		}
		//
		System.out.println();
		System.out.println("map__prot_inner_id__set_of_S_2.size() " + map__prot_inner_id__set_of_S_2.size());
		System.out.println("max_prot_INNER_id                     " + max_prot_INNER_id);
		System.out.println();
		//
		map__prot_inner_id__set_of_S_2_as_array = new String[max_prot_INNER_id + 1][];
		map__prot_inner_id__set_of_S_2_INNER_id_as_array = new int[max_prot_INNER_id + 1][];
		for (i = 0; i < map__prot_inner_id__set_of_S_2_as_array.length; i++) {
			map__prot_inner_id__set_of_S_2_as_array[i] = new String[0];
			map__prot_inner_id__set_of_S_2_INNER_id_as_array[i] = new int[0];
		}
		//
		HashSet<String> temp_set_of_S_2_as_array;
		for (int c_prot_INNER_id : map__prot_inner_id__set_of_S_2.keySet()) {
			temp_set_of_S_2_as_array = map__prot_inner_id__set_of_S_2.get(c_prot_INNER_id);
			map__prot_inner_id__set_of_S_2_as_array[c_prot_INNER_id] = new String[temp_set_of_S_2_as_array.size()];
			map__prot_inner_id__set_of_S_2_INNER_id_as_array[c_prot_INNER_id] = new int[temp_set_of_S_2_as_array
					.size()];
			i = 0;
			for (String shingle_S_2 : temp_set_of_S_2_as_array) {
				map__prot_inner_id__set_of_S_2_as_array[c_prot_INNER_id][i] = shingle_S_2;
				map__prot_inner_id__set_of_S_2_INNER_id_as_array[c_prot_INNER_id][i] = map_shingle__outer_id__inner_id
						.get(shingle_S_2);
				i++;
			}
		}
		map__prot_inner_id__set_of_S_2 = null;// gc
		System.out.println();
		System.out.println(
				"map__prot_inner_id__set_of_S_2_as_array.length " + map__prot_inner_id__set_of_S_2_as_array.length);
		System.out.println();
		//
		//
		//
		// Creating the map {shingle_id: (protein_INNER_id,
		// shingle_frequency_inside_protein)}
		Map<String, int[]> map__shingle_id__paired_set_prots_ids = new HashMap<String, int[]>();
		Map<String, int[]> map__shingle_id__paired_set_prots_ids_freq = new HashMap<String, int[]>();
		int[] c_paired_set_prots_ids = null;
		int[] c_paired_set_prots_ids_freq = null;
		int c_inner_protein_id;
		for (String c_shingle_id : map__shingle__MULTISET_of_proteins_ids.keySet()) {
			c_paired_set_prots_ids = new int[map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id).size()];
			c_paired_set_prots_ids_freq = new int[map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id).size()];
			i = 0;
			for (String c_outer_protein_id : map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id).keySet()) {
				c_inner_protein_id = map__outer_prot_id__inner_prot_id.get(c_outer_protein_id);
				c_paired_set_prots_ids[i] = c_inner_protein_id;
				c_paired_set_prots_ids_freq[i] = map__shingle__MULTISET_of_proteins_ids.get(c_shingle_id)
						.get(c_outer_protein_id);
				i++;
			}
			map__shingle_id__paired_set_prots_ids.put(c_shingle_id, c_paired_set_prots_ids);
			map__shingle_id__paired_set_prots_ids_freq.put(c_shingle_id, c_paired_set_prots_ids_freq);
		}
		System.out.println();
		System.out.println(
				"map__shingle_id__paired_set_prots_ids.size()      " + map__shingle_id__paired_set_prots_ids.size());
		System.out.println("map__shingle_id__paired_set_prots_ids_freq.size() "
				+ map__shingle_id__paired_set_prots_ids_freq.size());
		System.out.println();
		//
		//
		//
		map__shingle__MULTISET_of_proteins_ids = null;
		map__shingle__set_of_NEIGHBOUR_proteins_ids = null;
		map__shingle__set_of_proteins_ids = null;
		System.gc();
		//
		//
		int int_value, freq_S_1_in_A, freq_S_2_in_B, int_value_with_frequencies;
		String prot_A, prot_B;
		int num_int_zero = 0;
		int i_S_1;
		String S_1, S_2;
		int S_2_inner_id;
		int[] set_of_NEIGHBOUR_proteins_ids;
		int[] set_of_proteins_ids;
		int j, k;
		int[] S_1__paired_set_prots_ids;
		int[] S_1__paired_set_prots_ids_freq;
		int[] S_2__paired_set_prots_ids;
		int[] S_2__paired_set_prots_ids_freq;
		// HashSet<String> all_S_2 = new HashSet<String>(4000000);
		// HashSet<Integer> all_S_2_inner_ids = new HashSet<Integer>(4000000);
		SimpleIntSetCountingToInfinity fast_all_S_2_inner_ids = new SimpleIntSetCountingToInfinity(
				max_inner_shingle_id);
		long num_records = 0;
		for (i_S_1 = 0; i_S_1 < set_ALL_shingles.length; i_S_1++) {
			S_1 = set_ALL_shingles[i_S_1];
			//
			System.out.println("i_S_1/(set_ALL_shingles.length= " + ((float) i_S_1) / (set_ALL_shingles.length));
			System.out.println("num_intersection_zero " + num_int_zero);
			System.out.println("num_records           " + num_records);
			System.out.println();
			//
			set_of_NEIGHBOUR_proteins_ids = map__S_1__set_NEIGHBOUR_prots_ids_as_sorted_array.get(S_1);
			S_1__paired_set_prots_ids = map__shingle_id__paired_set_prots_ids.get(S_1);
			S_1__paired_set_prots_ids_freq = map__shingle_id__paired_set_prots_ids_freq.get(S_1);
			//
			//
			//
			// all_S_2.clear();
			// all_S_2_inner_ids.clear();
			fast_all_S_2_inner_ids.clear();
			//
			for (int i_prot = 0; i_prot < set_of_NEIGHBOUR_proteins_ids.length; i_prot++) {
				c_prot = set_of_NEIGHBOUR_proteins_ids[i_prot];
				//
				for (k = 0; k < map__prot_inner_id__set_of_S_2_as_array[c_prot].length; k++) {
					S_2_inner_id = map__prot_inner_id__set_of_S_2_INNER_id_as_array[c_prot][k];
					if (!fast_all_S_2_inner_ids.informativeAdd(S_2_inner_id)) {
						continue;
					}
					S_2 = map__prot_inner_id__set_of_S_2_as_array[c_prot][k];
					//
					//
//					if (!all_S_2_inner_ids.add(S_2_inner_id)) {
//						continue;
//					}
//					if (!all_S_2.add(S_2)) {
//						continue;
//					}
					//
					//
					//
					set_of_proteins_ids = map__S_2__set_prots_ids_as_sorted_array.get(S_2);
					//
					int_value = this.compute_nummber_of_common_elements(set_of_proteins_ids,
							set_of_NEIGHBOUR_proteins_ids);
					//
					if (int_value == 0) {
						num_int_zero++;
						continue;
					}
					//
					//
					//
					// System.out.println(i_S_1 + " " + i_S_2 + " " + val);
					S_2__paired_set_prots_ids = map__shingle_id__paired_set_prots_ids.get(S_2);
					S_2__paired_set_prots_ids_freq = map__shingle_id__paired_set_prots_ids_freq.get(S_2);
					for (i = 0; i < S_1__paired_set_prots_ids.length; i++) {
						//
						freq_S_1_in_A = S_1__paired_set_prots_ids_freq[i];
						prot_A = map__protein_inner_id__protein_outer_id[S_1__paired_set_prots_ids[i]];
						//
						for (j = 0; j < S_2__paired_set_prots_ids.length; j++) {
							//
							freq_S_2_in_B = S_2__paired_set_prots_ids_freq[j];
							prot_B = map__protein_inner_id__protein_outer_id[S_2__paired_set_prots_ids[j]];
							//
							int_value_with_frequencies = int_value * freq_S_1_in_A * freq_S_2_in_B;
							//
//							System.out.println(S_1 + " " + S_2 + " " + prot_A + " " + prot_B + " " + int_value + " "
//									+ int_value_with_frequencies);
//							System.out.println("num_int_zero " + num_int_zero);
							//
							num_records++;
						}
					}
					//
				}
			}
			//
		}
		//
		return;
	}

	protected HashMap<String, int[]> createMappingShingleIdSetProtsIdsAsSortedArray(String[] set_ALL_shingles,
			Map<String, HashSet<String>> map__shingle__set_of_proteins_outer_ids,
			Map<String, Integer> map__outer_prot_id__inner_prot_id) {
		HashMap<String, int[]> map__shingle_id__set_prots_ids_as_sorted_array = new HashMap<String, int[]>();
		String c_shingle_id;
		int c_inner_prot_id;
		int[] c_set_prots_ids_as_sorted_array = null;
		int i;
		for (int index = 0; index < set_ALL_shingles.length; index++) {
			c_shingle_id = set_ALL_shingles[index];
			c_set_prots_ids_as_sorted_array = new int[map__shingle__set_of_proteins_outer_ids.get(c_shingle_id).size()];
			i = 0;
			for (String c_outer_prot_id : map__shingle__set_of_proteins_outer_ids.get(c_shingle_id)) {
				c_inner_prot_id = map__outer_prot_id__inner_prot_id.get(c_outer_prot_id);
				c_set_prots_ids_as_sorted_array[i] = c_inner_prot_id;
				i++;
			}
			Arrays.sort(c_set_prots_ids_as_sorted_array);
			map__shingle_id__set_prots_ids_as_sorted_array.put(c_shingle_id, c_set_prots_ids_as_sorted_array);
		}
		//
		return map__shingle_id__set_prots_ids_as_sorted_array;
	}

	/**
	 * 
	 * @param map__shingle__MULTISET_of_proteins_ids
	 * @param map__shingle__set_of_NEIGHBOUR_proteins_ids
	 * @param map__shingle__set_of_proteins_ids
	 * @param ALL_proteins_id
	 */
	protected void computeData_1(Map<String, HashMap<String, Integer>> map__shingle__MULTISET_of_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_NEIGHBOUR_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_proteins_ids, String[] ALL_proteins_id) {
		//
		//
		String[] set_ALL_shingles = new String[map__shingle__set_of_proteins_ids.size()];
		int i;
		i = 0;
		for (String shingle : map__shingle__set_of_proteins_ids.keySet()) {
			set_ALL_shingles[i] = shingle;
			i++;
		}
		Arrays.sort(set_ALL_shingles);
		//
		//
		//
		HashSet<String> set_of_S_1_S_2 = new HashSet<String>(1000000);
		OrderedTupleOfStrings c_couple_of_shingles = null;
		HashSet<String> set_of_NEIGHBOUR_proteins_ids = null;
		HashSet<String> set_of_proteins_ids = null;
		int int_value, freq_S_1_in_A, freq_S_2_in_B, int_value_with_frequencies;
		String prot_A, prot_B;
		Map<String, Integer> map__A__freq;
		Map<String, Integer> map__B__freq;
		int num_int_zero = 0;
		int counter = 0;
		int i_S_1, i_S_2;
		String S_1, S_2;
		for (i_S_1 = 0; i_S_1 < set_ALL_shingles.length; i_S_1++) {
			S_1 = set_ALL_shingles[i_S_1];
			//
			set_of_NEIGHBOUR_proteins_ids = map__shingle__set_of_NEIGHBOUR_proteins_ids.get(S_1);
			map__A__freq = map__shingle__MULTISET_of_proteins_ids.get(S_1);
			//
			for (i_S_2 = 0; i_S_2 < set_ALL_shingles.length; i_S_2++) {
				S_2 = set_ALL_shingles[i_S_2];
				//
				set_of_proteins_ids = map__shingle__set_of_proteins_ids.get(S_2);
				//
				int_value = this.computeIntersectionSize(set_of_proteins_ids, set_of_NEIGHBOUR_proteins_ids);
				//
				if (int_value == 0) {
					num_int_zero++;
					continue;
				}
				//
				//
				//
				// System.out.println(i_S_1 + " " + i_S_2 + " " + val);
				map__B__freq = map__shingle__MULTISET_of_proteins_ids.get(S_2);
				for (String A : map__A__freq.keySet()) {
					freq_S_1_in_A = map__A__freq.get(A);
					for (String B : map__B__freq.keySet()) {
						freq_S_2_in_B = map__B__freq.get(B);
						//
						int_value_with_frequencies = int_value * freq_S_1_in_A * freq_S_2_in_B;
						//
						System.out.println(i_S_1 + " " + i_S_2 + " " + A + " " + B + " " + int_value + " "
								+ int_value_with_frequencies);
						System.out.println("num_int_zero " + num_int_zero);
					}
				}
			}
			//
			//
		}
		//
		return;
	}

	protected void computeData_0(int shingle_width, String[] ALL_proteins_id,
			Map<String, String> map__protein_id__primary_structure,
			Map<String, HashSet<String>> map__shingle__set_of_NEIGHBOUR_proteins_ids,
			Map<String, HashSet<String>> map__shingle__set_of_proteins_ids,
			Map<String, HashSet<String>> ppi_as_map__protein_id__set_proteins_ids) {
		//
		HashSet<String> all_proteins_containing_S_2 = null;
		HashSet<String> all_proteins_close_to_the_ones_containing_S_1 = null;
		String shingle_1, shingle_2, c_protein_A_id, c_protein_B_id, c_primary_structure_A, c_primary_structure_B;
		int i_A, i_B, i_s_1, i_s_2;
		int value;
		// For all proteins A:
		for (i_A = 0; i_A < ALL_proteins_id.length; i_A++) {
			c_protein_A_id = ALL_proteins_id[i_A];
			c_primary_structure_A = map__protein_id__primary_structure.get(c_protein_A_id);
			//
			//
			// For all S_1 in A:
			for (i_s_1 = 0; i_s_1 < c_primary_structure_A.length() - shingle_width; i_s_1++) {
				shingle_1 = c_primary_structure_A.substring(i_s_1, i_s_1 + shingle_width);
				//
				// Take all proteins with S_1.
				map__shingle__set_of_proteins_ids.get(shingle_1);
				//
				// Take all proteins near to the ones that contain S_1
				all_proteins_close_to_the_ones_containing_S_1 = map__shingle__set_of_NEIGHBOUR_proteins_ids
						.get(shingle_1);
//				all_proteins_close_to_the_ones_containing_S_1.clear();
//				for (String c_prot_with_S_1 : map__shingle__set_of_proteins_ids.get(shingle_1)) {
//					all_proteins_close_to_the_ones_containing_S_1
//							.addAll(ppi_as_map__protein_id__set_proteins_ids.get(c_prot_with_S_1));
//				}
				//
				//
				//
				// For all proteins B:
				for (i_B = 0; i_B < ALL_proteins_id.length; i_B++) {
					c_protein_B_id = ALL_proteins_id[i_B];
					//
					System.out.println(i_A + " " + i_B);
					//
					if (ppi_as_map__protein_id__set_proteins_ids.containsKey(c_protein_A_id)) {
						if (ppi_as_map__protein_id__set_proteins_ids.get(c_protein_A_id).contains(c_protein_B_id)) {
							continue;
						}
					}
					//
					c_primary_structure_B = map__protein_id__primary_structure.get(c_protein_B_id);
					//
					// For all S_2 in B:
					for (i_s_2 = 0; i_s_2 < c_primary_structure_B.length() - shingle_width; i_s_2++) {
						shingle_2 = c_primary_structure_B.substring(i_s_2, i_s_2 + shingle_width);
						//
						// Take all proteins with S_2.
						all_proteins_containing_S_2 = map__shingle__set_of_proteins_ids.get(shingle_2);
						if (all_proteins_containing_S_2 == null) {
							continue;
						}
						//
						//
//						value = this.computeIntersectionSize(all_proteins_containing_S_2,
//								all_proteins_close_to_the_ones_containing_S_1);
//						//
//						if (value == 0) {
//							continue;
//						}
//						//
//						//
//						if (value >= 2) {
//						System.out.println(i_A + " " + i_B + " " + c_protein_A_id + " " + c_protein_B_id + " "
//								+ shingle_1 + " " + shingle_2 + " " + value);
//						}
					}
					//
					//

					//
				}
				//
				//
				//
			}
			//
		}
		return;
	}

	protected int computeIntersectionSize(HashSet<String> set_1, HashSet<String> set_2) {
		int int_size = 0;
		//
		if (set_2.size() >= set_1.size()) {
			for (String e_1 : set_1) {
				if (set_2.contains(e_1)) {
					int_size++;
				}
			}
		} else if (set_2.size() < set_1.size()) {
			for (String e_2 : set_2) {
				if (set_1.contains(e_2)) {
					int_size++;
				}
			}
		}
		//
		return int_size;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void addProteinToShingle(Map<String, HashSet<String>> map__shingle__set_of_proteins_ids, String shingle,
			String protein_id) {
		if (!map__shingle__set_of_proteins_ids.containsKey(shingle)) {
			map__shingle__set_of_proteins_ids.put(shingle, new HashSet<String>());
		}
		map__shingle__set_of_proteins_ids.get(shingle).add(protein_id);
		return;
	}

//	
	protected void addProteinToShingleMultiset(
			Map<String, HashMap<String, Integer>> map__shingle__MULTISET_of_proteins_ids, String shingle,
			String protein_id) {
		if (!map__shingle__MULTISET_of_proteins_ids.containsKey(shingle)) {
			map__shingle__MULTISET_of_proteins_ids.put(shingle, new HashMap<String, Integer>());
		}
		if (!map__shingle__MULTISET_of_proteins_ids.get(shingle).containsKey(protein_id)) {
			map__shingle__MULTISET_of_proteins_ids.get(shingle).put(protein_id, 0);
		}
		//
		int new_value = map__shingle__MULTISET_of_proteins_ids.get(shingle).get(protein_id) + 1;
		map__shingle__MULTISET_of_proteins_ids.get(shingle).put(protein_id, new_value);
		return;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	protected void addToCoupleToTheMap(Map<String, HashSet<String>> map__node_id__list_node_ids, String prot_u_id,
			String prot_v_id) {
		this.addToTheMap(map__node_id__list_node_ids, prot_u_id, prot_v_id);
		this.addToTheMap(map__node_id__list_node_ids, prot_v_id, prot_u_id);
		return;
	}

	protected void addToTheMap(Map<String, HashSet<String>> map__node_id__list_node_ids, String prot_a_id,
			String prot_b_id) {
		if (!map__node_id__list_node_ids.containsKey(prot_a_id)) {
			map__node_id__list_node_ids.put(prot_a_id, new HashSet<String>());
		}
		map__node_id__list_node_ids.get(prot_a_id).add(prot_b_id);
		return;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
