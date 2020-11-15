package termes;

public class Operator extends LambdaTerm {
	
	public Operator(String oper) {
		this.oper = oper;
	}
	
	@Override
	public String toString() {
		return this.oper;
	}
	
	@Override
	public Operator clone() {
		return new Operator(this.oper);
	}


}
