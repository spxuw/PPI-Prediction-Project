package algos;

public class UnorderedTupleOfStrings {

	public String a = null;
	public String b = null;

	protected UnorderedTupleOfStrings() {
		return;
	}

	public UnorderedTupleOfStrings(String a, String b) {
		this.a = a;
		this.b = b;
		if (b.compareToIgnoreCase(a) == -1) {
			this.a = b;
			this.b = a;
		}
		return;
	}

	public boolean equals(Object o) {
//		return (o != null && ((o == this) || . . . . );
		return ((((UnorderedTupleOfStrings) o).a.equals(this.a)) && (((UnorderedTupleOfStrings) o).b.equals(this.b)));
	}

	public int hashCode() {
		return a.hashCode() * 4096 + b.hashCode();
	}
}
