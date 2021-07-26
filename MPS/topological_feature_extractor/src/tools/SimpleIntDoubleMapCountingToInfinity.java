package tools;

public class SimpleIntDoubleMapCountingToInfinity {

	SimpleIntSetCountingToInfinity set_keys;

	protected double[] map_key_value;

	public SimpleIntDoubleMapCountingToInfinity(int max_key_value) {
		this.map_key_value = new double[max_key_value + 1];
		for (int i = 0; i < this.map_key_value.length; i++) {
			this.map_key_value[i] = SimpleIntDoubleMap.NaN;
		}
		//
		this.set_keys = new SimpleIntSetCountingToInfinity(max_key_value); 
		//
		return;
	}

	public int size() {
		return this.set_keys.size;
	}

	public void put(int key, double value) {
		if (!this.containsKey(key)) {
			this.set_keys.add(key);
		}
		this.map_key_value[key] = value;
		return;
	}

	public void addValue(int key, double value) {
		if (!this.containsKey(key)) {
			this.set_keys.add(key);
			this.map_key_value[key] = value;
		} else {
			this.map_key_value[key] += value;
		}

		return;
	}

	public double get(int key, double default_value) {
		if (this.containsKey(key)) {
			return this.map_key_value[key];
		}
		return default_value;
	}

	public boolean containsKey(int key) {
		return this.set_keys.contains(key);
	}

	public void clear() {
		//
		this.set_keys.clear();
		//
		return;
	}

}
