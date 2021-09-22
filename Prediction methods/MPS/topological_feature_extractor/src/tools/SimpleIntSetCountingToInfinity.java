package tools;

import java.util.Arrays;

public class SimpleIntSetCountingToInfinity {
	public static void main(String[] args) {

		SimpleIntSetCountingToInfinity set = new SimpleIntSetCountingToInfinity(10); 
		set.add(0);
		System.out.println(set.contains(0));
		System.out.println(set.contains(3));
		System.out.println(Arrays.toString(set.map_key_presence));
		set.add(5);
		System.out.println(set.contains(5));
		System.out.println(Arrays.toString(set.map_key_presence));
		set.add(5);
		System.out.println(Arrays.toString(set.map_key_presence));
		set.clear();
		System.out.println(Arrays.toString(set.map_key_presence));
		System.out.println(set.contains(3));
		System.out.println(set.contains(5));
		System.out.println(set.contains(1));
		return;
	}

	protected int threshold;
	protected final int maximum_allowed_threshold;

	protected int[] map_key_presence;
	protected int size;

	protected int iterator_index;

	public SimpleIntSetCountingToInfinity(int max_key_value) {
		this.map_key_presence = new int[max_key_value + 1];
		this.size = 0;
		this.iterator_index = 0;
		this.threshold = 1;
		this.maximum_allowed_threshold = 1000000000;
		return;
	}

	public int size() {
		return this.size;
	}

	public void add(int key) {
		if (!this.contains(key)) {
			this.map_key_presence[key] = this.threshold;
			this.size++;
		}
		return;
	}

	public boolean informativeAdd(int key) {
		if (!this.contains(key)) {
			this.map_key_presence[key] = this.threshold;
			this.size++;
			return true;
		}
		return false;
	}

	public boolean contains(int key) {
		return (this.map_key_presence[key] >= this.threshold);
	}

	public void clear() {
		//
		this.threshold++;
		//
		if (this.threshold > this.maximum_allowed_threshold) {
			//
			this.threshold = 1;
			//
			Arrays.fill(this.map_key_presence, 0);
		}
		//
		this.size = 0;
		//
		return;
	}

}
