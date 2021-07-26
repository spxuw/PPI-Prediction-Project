package tools;

public class SimpleUnorderedIntIntCouplesDoubleMapCountingToInfinity extends SimpleIntDoubleMapCountingToInfinity {

	/**
	 * 
	 * @param key_1
	 * @param key_2
	 * @return
	 */
	protected int computeAggregatedKey(int key_1, int key_2) {
		return key_2 * this.base + key_1;
	}

	/**
	 * 
	 */
	protected int base;

	/**
	 * 
	 * @param max_key_value
	 */
	public SimpleUnorderedIntIntCouplesDoubleMapCountingToInfinity(int max_key_value) {
		super(max_key_value * (max_key_value + 1) + max_key_value);
		//
//		System.out.println("max_key_value * (max_key_value + 1) + max_key_value "
//				+ (max_key_value * (max_key_value + 1) + max_key_value));
		this.base = max_key_value + 1;
		//
	}

	/**
	 * 
	 * @param key_1
	 * @param key_2
	 * @param value
	 */
	public void put(int key_1, int key_2, double value) {
		if (key_1 < key_2) {
			super.put(this.computeAggregatedKey(key_1, key_2), value);
		} else {
			super.put(this.computeAggregatedKey(key_2, key_1), value);
		}
		return;
	}

	/**
	 * 
	 * @param key_1
	 * @param key_2
	 * @param value
	 */
	public void addValue(int key_1, int key_2, double value) {
		if (key_1 < key_2) {
			super.addValue(this.computeAggregatedKey(key_1, key_2), value);
		} else {
			super.addValue(this.computeAggregatedKey(key_2, key_1), value);
		}
		return;
	}

	/**
	 * 
	 */
	public double get(int key_1, int key_2, double default_value) {
		if (key_1 < key_2) {
			return super.get(this.computeAggregatedKey(key_1, key_2), default_value);
		}
		return super.get(this.computeAggregatedKey(key_2, key_1), default_value);
	}

	/**
	 * 
	 */
	public boolean containsKey(int key_1, int key_2) {
		if (key_1 < key_2) {
			return super.containsKey(this.computeAggregatedKey(key_1, key_2));
		}
		return super.containsKey(this.computeAggregatedKey(key_2, key_1));
	}

}
