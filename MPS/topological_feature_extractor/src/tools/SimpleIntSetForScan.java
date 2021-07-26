package tools;

public class SimpleIntSetForScan extends SimpleIntSet {

	protected int[] list_of_keys;

	public SimpleIntSetForScan(int max_key_value) {
		super(max_key_value);
		//
		this.list_of_keys = new int[max_key_value + 1];
		//
		return;
	}

	public void add(int key) {
		if (!this.map_key_presence[key]) {
			this.map_key_presence[key] = true;
			this.list_of_keys[this.size] = key;
			this.size++;
		}
		return;
	}

	public boolean nextKey(int[] returned_current_single_key) {
		//
		if (this.iterator_index >= this.size) {
			this.iterator_index = 0;
			return false;
		}
		//
		returned_current_single_key[0] = this.list_of_keys[this.iterator_index];
		this.iterator_index++;
		//
		return true;
	}

	public int[] get_list_of_keys() {
		return this.list_of_keys;
	}

}
