package termes;

import java.util.ArrayList;
import java.util.List;

public abstract class LambdaTerm {

	public String vari;
	public String oper;
	public int val;
	public LambdaTerm fpos;
	public LambdaTerm apos;
	public LambdaTerm corps;
	public List<LambdaTerm> elements = new ArrayList<>();

	public boolean est_non_expansif() {
		switch (this.getClass().getSimpleName()) {
		case "Entier":
		case "Abstraction":
		case "Unit":
			return true;
		case "Let":
			return this.apos.est_non_expansif() && this.fpos.est_non_expansif();
		case "Application":
			if (this.fpos instanceof Operator) {
				if (this.fpos.oper.equals("hd") || this.fpos.oper.equals("hd") || this.fpos.oper.equals("hd"))
					return this.apos.est_non_expansif();
			} else if (this.fpos instanceof Application) {
				if (this.fpos.oper.equals("izte") || this.fpos.oper.equals("iete"))
					return this.apos.est_non_expansif() && this.fpos.est_non_expansif();
			}
			return false;
		default:
			return false;
		}
	}
	
	@Override
	public abstract LambdaTerm clone();
	
	public static List<LambdaTerm> cons(LambdaTerm x, List<LambdaTerm> s) {
		List<LambdaTerm> res = new ArrayList<>();
		res.add(x);
		for (LambdaTerm el : s)
			res.add(el);
		return res;
	}

}
