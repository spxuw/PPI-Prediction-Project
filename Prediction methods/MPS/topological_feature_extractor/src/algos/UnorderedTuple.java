package algos;

public class UnorderedTuple {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int a = -1;
	public int b = -1;

	public UnorderedTuple(int a, int b) {
		this.a = a;
		this.b = b;
		if (b < a) {
			this.a = b;
			this.b = a;
		}
		return;
	}

	public boolean equals(Object o) {
//		return (o != null && ((o == this) || ((o instanceof UnorderedTuple) && (((UnorderedTuple) o).a == this.a)
//				&& (((UnorderedTuple) o).b == this.b))));
		return ((((UnorderedTuple) o).a == this.a) && (((UnorderedTuple) o).b == this.b));
	}

	public int hashCode() {
		return a * 4096 + b;
	}
}
