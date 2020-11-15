package termes;

public class Application extends LambdaTerm {

	public Application(LambdaTerm fpos, LambdaTerm apos) {
		this.fpos = fpos;
		this.apos = apos;
	}

	public static Application Add(LambdaTerm x, LambdaTerm y) {
		return new Application(new Application(new Operator("add"), x), y);
	}

	public static Application Sub(LambdaTerm x, LambdaTerm y) {
		return new Application(new Application(new Operator("sub"), x), y);
	}

	public static Application Cons(LambdaTerm x, LambdaTerm xs) {
		return new Application(new Application(new Operator("cons"), x), xs);
	}

	public static Application Assign(LambdaTerm l1, LambdaTerm l2) {
		return new Application(new Application(new Operator("assign"), l1), l2);
	}

	public static Application Ref(LambdaTerm l1) {
		return new Application(new Operator("ref"), l1);
	}

	public static Application Deref(LambdaTerm l1) {
		return new Application(new Operator("deref"), l1);
	}

	public static Application Fix(String vari, LambdaTerm l) {
		return new Application(new Operator("fix"), new Abstraction(vari, l));
	}

	@Override
	public String toString() {
		if (this.fpos instanceof Application) {
			if (this.fpos.fpos instanceof Operator) {
				switch (this.fpos.fpos.oper) {
				case "add":
					return this.fpos.apos.toString() + " + " + this.apos.toString();
				case "sub":
					return this.fpos.apos.toString() + " - " + this.apos.toString();
				case "cons":
					return this.fpos.apos.toString() + "::" + this.apos.toString();
				case "assign":
					return this.fpos.apos.toString() + ":=" + this.apos.toString();
				}
			}
		} else {
			if (this.fpos instanceof Operator) {
				if (this.fpos.oper.equals("deref")) {
					return "!" + this.apos.toString();
				}
			}
		}
		return "(" + this.fpos.toString() + " " + this.apos.toString() + ")";
	}

	@Override
	public Application clone() {
		return new Application(this.fpos.clone(), this.apos.clone());
	}
	
}
