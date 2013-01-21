

public class HandRank implements Comparable<HandRank> {
	int a, b, c, d, e, val;
	HandRank(int a, int b, int c, int d, int e) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.e = e;
		val = 160000 * a + 8000 * b + 400 * c + 20 * d + e;
	}
	
	public void print() {
		System.out.println(a + " " + b + " " + c + " " + d + " " + e);
	}
	public int compareTo(HandRank other) {
		return this.val - other.val;
	}
}
