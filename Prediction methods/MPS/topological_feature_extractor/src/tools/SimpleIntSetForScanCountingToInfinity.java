package tools;

import java.util.Arrays;

public class SimpleIntSetForScanCountingToInfinity extends SimpleIntSetCountingToInfinity {
	public static void main(String[] args) {

		SimpleIntSetForScanCountingToInfinity set = new SimpleIntSetForScanCountingToInfinity(10);
		set.add(0);
		System.out.println(set.contains(0));
		System.out.println(set.contains(3));
		System.out.println(Arrays.toString(set.map_key_presence));
		System.out.println(Arrays.toString(set.get_list_of_keys()));
		System.out.println(set.size);
		set.add(5);
		System.out.println(set.contains(5));
		System.out.println(Arrays.toString(set.map_key_presence));
		System.out.println(Arrays.toString(set.get_list_of_keys()));
		System.out.println(set.size);
		set.add(5);
		System.out.println(Arrays.toString(set.map_key_presence));
		System.out.println(Arrays.toString(set.get_list_of_keys()));
		System.out.println(set.size);
		set.clear(); 
		System.out.println(Arrays.toString(set.map_key_presence));
		System.out.println(Arrays.toString(set.get_list_of_keys()));
		System.out.println(set.size);
		System.out.println(set.contains(3));
		System.out.println(set.contains(5));
		System.out.println(set.contains(1));
		return;
	}

	protected int[] list_of_keys;

	public SimpleIntSetForScanCountingToInfinity(int max_key_value) {
		super(max_key_value);
		//
		this.list_of_keys = new int[max_key_value + 1];
		//
		return;
	}

	public void add(int key) {
		if (!this.contains(key)) {
			this.map_key_presence[key] = this.threshold;
			this.list_of_keys[this.size] = key;
			this.size++;
		}
		return;
	}

	public boolean informativeAdd(int key) {
		if (!this.contains(key)) {
			this.map_key_presence[key] = this.threshold;
			this.list_of_keys[this.size] = key;
			this.size++;
			return true;
		}
		return false;
	}

	public int[] get_list_of_keys() {
		return this.list_of_keys;
	}

}
