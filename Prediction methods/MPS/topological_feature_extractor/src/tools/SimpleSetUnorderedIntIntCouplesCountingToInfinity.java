package tools;

public class SimpleSetUnorderedIntIntCouplesCountingToInfinity extends SimpleIntSetCountingToInfinity {

	/**
	 * 
	 * @param key_1
	 * @param key_2
	 * @return
	 */
	protected int computeAggregatedKey(int key_1, int key_2) {
//		System.out.println("key_1      " + key_1);
//		System.out.println("key_2      " + key_2);
//		System.out.println("this.base  " + this.base);
//		System.out.println("key_2 * (this.base + 1) + key_1 " + (key_2 * (this.base + 1) + key_1));
		return key_2 * this.base + key_1;
	}

	protected int base;

	/**
	 * 
	 * @param max_key_1_value
	 * @param max_key_2_value
	 */
	public SimpleSetUnorderedIntIntCouplesCountingToInfinity(int max_key_value) {
		super(max_key_value * (max_key_value + 1) + max_key_value);
		//
//		System.out.println("max_key_value * (max_key_value + 1) + max_key_value "
//				+ (max_key_value * (max_key_value + 1) + max_key_value));
		this.base = max_key_value + 1;
		//
		return;
	}

	/**
	 * 
	 * @param key_1
	 * @param key_2
	 * @param value
	 */
	public void add(int key_1, int key_2) {
		if (key_1 < key_2) {
			super.add(this.computeAggregatedKey(key_1, key_2));
		} else {
			super.add(this.computeAggregatedKey(key_2, key_1));
		}
		return;
	}

	public boolean informativeAdd(int key_1, int key_2) {
		if (key_1 < key_2) {
			return super.informativeAdd(this.computeAggregatedKey(key_1, key_2));
		} else {
			return super.informativeAdd(this.computeAggregatedKey(key_2, key_1));
		}
	}

	/**
	 * 
	 * @param key_1
	 * @param key_2
	 * @return
	 */
	public boolean contains(int key_1, int key_2) {
		if (key_1 < key_2) {
			return super.contains(this.computeAggregatedKey(key_1, key_2));
		} else {
			return super.contains(this.computeAggregatedKey(key_2, key_1));
		}
	}

}
