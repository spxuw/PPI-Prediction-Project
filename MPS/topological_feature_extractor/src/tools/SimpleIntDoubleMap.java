package tools;

public class SimpleIntDoubleMap {

	protected double[] map_key_value;
	protected int size;
	protected static double NaN = Double.NaN;

	protected int iterator_index;

	public SimpleIntDoubleMap(int max_key_value) {
		this.map_key_value = new double[max_key_value + 1];
		for (int i = 0; i < this.map_key_value.length; i++) {
			this.map_key_value[i] = SimpleIntDoubleMap.NaN;
		}
		this.size = 0;
		this.iterator_index = 0;
		return;
	}

	public void put(int key, double value) {
		// Check if this.map_key_value[key] is Not-a-Number
		// Exploiting the property of NaN value: (NaN != NaN) IS ALWAYS TRUE,
		// (NaN == NaN) IS ALWAYS FALSE
		if (this.map_key_value[key] != this.map_key_value[key]) {
			this.size++;
		}
		this.map_key_value[key] = value;
		return;
	}

	public double get(int key) {
		return this.map_key_value[key];
	}

	public double get(int key, double default_value) {
		if (this.containsKey(key)) {
			return this.map_key_value[key];
		}
		return default_value;
	}

	public boolean containsKey(int key) {
		// Exploiting the property of NaN value: (NaN != NaN) IS ALWAYS TRUE,
		// (NaN == NaN) IS ALWAYS FALSE
		return (this.map_key_value[key] == this.map_key_value[key]);
	}

	public void reset() {
		for (int i = 0; i < this.map_key_value.length; i++) {
			this.map_key_value[i] = SimpleIntDoubleMap.NaN;
		}
		this.size = 0;
		return;
	}

//	public boolean nextKey(int[] returned_current_single_key) {
//		//
//		while (this.iterator_index < this.map_key_presence.length && !this.map_key_presence[this.iterator_index]) {
//			this.iterator_index++;
//		}
//		if (this.iterator_index >= this.map_key_presence.length) {
//			this.iterator_index = 0;
//			return false;
//		}
//		//
//		returned_current_single_key[0] = this.iterator_index;
//		this.iterator_index++;
//		//
//		return true;
//	}

	public void startIteration() {
		this.iterator_index = 0;
		return;
	}

	public boolean nextKey(int[] returned_current_single_key) {
		//
		while (this.iterator_index < this.map_key_value.length
				&& !(this.map_key_value[this.iterator_index] == this.map_key_value[this.iterator_index])) {
			this.iterator_index++;
		}
		if (this.iterator_index >= this.map_key_value.length) {
			this.iterator_index = 0;
			return false;
		}
		//
		returned_current_single_key[0] = this.iterator_index;
		this.iterator_index++;
		//
		return true;
	}

	public boolean nextValue(double[] returned_current_single_value) {
		//
		while (this.iterator_index < this.map_key_value.length
				&& !(this.map_key_value[this.iterator_index] == this.map_key_value[this.iterator_index])) {
			this.iterator_index++;
		}
		if (this.iterator_index >= this.map_key_value.length) {
			this.iterator_index = 0;
			return false;
		}
		//
		returned_current_single_value[0] = this.map_key_value[this.iterator_index];
		this.iterator_index++;
		//
		return true;
	}

	public boolean nextKeyValueCouple(int[] returned_current_single_key, double[] returned_current_single_value) {
		//
		while (this.iterator_index < this.map_key_value.length
				&& !(this.map_key_value[this.iterator_index] == this.map_key_value[this.iterator_index])) {
			this.iterator_index++;
		}
		if (this.iterator_index >= this.map_key_value.length) {
			this.iterator_index = 0;
			return false;
		}
		//
		returned_current_single_key[0] = this.iterator_index;
		returned_current_single_value[0] = this.map_key_value[this.iterator_index];
		this.iterator_index++;
		//
		return true;
	}

}
