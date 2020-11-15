package termes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sequence extends LambdaTerm {
	
	public Sequence(List<LambdaTerm> elements) {
		this.elements = elements;
	}
	
	public static Sequence Nil() {
		return new Sequence(Collections.emptyList());
	}
	
	@Override
	public String toString() {
		String res = "[";
		for (int i = 0; i < this.elements.size(); i++) {

			res += this.elements.get(i).toString();
			if (i != this.elements.size() - 1)
				res += ", ";
		}
		return res + "]";
	}
	
	@Override
	public Sequence clone() {
		List<LambdaTerm> res = new ArrayList<>();
		for (LambdaTerm e : this.elements)
			res.add(e.clone());
		return new Sequence(res);
	}


}
