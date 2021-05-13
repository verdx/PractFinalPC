package parte2;

public class Pair<T1, T2> {

	T1 obj1;
	T2 obj2;
	
	public Pair(T1 obj1, T2 obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}
	
	public T1 first() {
		return obj1;
	}
	
	public T2 second() {
		return obj2;
	}
}
