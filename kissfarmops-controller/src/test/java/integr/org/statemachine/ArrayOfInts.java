package integr.org.statemachine;

import java.util.ArrayList;
import java.util.Collection;

public class ArrayOfInts extends ArrayList<Integer> {
	private static final long serialVersionUID = 7984208327280614388L;

	public ArrayOfInts() {
	}

	public ArrayOfInts(Collection<Integer> collection) {
		super(collection);
	}
}