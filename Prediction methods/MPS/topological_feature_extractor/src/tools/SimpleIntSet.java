package tools;

/**
 * 
 * @author ikki
 *
 */
public class SimpleIntSet {

	protected boolean[] map_key_presence;
	protected int size;

	protected int iterator_index;

	public SimpleIntSet(int max_key_value) {
		this.map_key_presence = new boolean[max_key_value + 1];
		this.size = 0;
		this.iterator_index = 0;
		return;
	}

	public int size() {
		return this.size;
	}

	public void add(int key) {
		if (!this.map_key_presence[key]) {
			this.map_key_presence[key] = true;
			this.size++;
		}
		return;
	}

	public boolean contains(int key) {
		return this.map_key_presence[key];
	}

	public void reset() {
		for (int index = 0, len = this.map_key_presence.length; index < len; index++) {
			this.map_key_presence[index] = false;
		}
		this.size = 0;
		return;
	}

	public int[] getSetContentAsSortedArray() {
		int[] arr = new int[this.size];
		int index = 0;
		for (int key = 0; key < this.map_key_presence.length; key++) {
			if (this.map_key_presence[key]) {
				arr[index++] = key;
			}
		}
		return arr;
	}

	public String toString() {
		if (this.size == 0) {
			return "{}";
		}
		//
		String set_content_as_string = "{";
		int num_elements_printed_so_far = 0;
		for (int index = 0; index < this.map_key_presence.length; index++) {
			if (this.map_key_presence[index]) {
				set_content_as_string += index;
				num_elements_printed_so_far++;
				if (num_elements_printed_so_far < this.size) {
					set_content_as_string += ", ";
				}
			}
		}
		set_content_as_string += "}";
		return set_content_as_string;
	}

	public void startIteration() {
		this.iterator_index = 0;
		return;
	}

	public boolean nextKey(int[] returned_current_single_key) {
		//
		while (this.iterator_index < this.map_key_presence.length && !this.map_key_presence[this.iterator_index]) {
			this.iterator_index++;
		}
		if (this.iterator_index >= this.map_key_presence.length) {
			this.iterator_index = 0;
			return false;
		}
		//
		returned_current_single_key[0] = this.iterator_index;
		this.iterator_index++;
		//
		return true;
	}
}
