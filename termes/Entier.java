package termes;

public class Entier extends LambdaTerm {

	public Entier(int val) {
		this.val = val;
	}
	
	@Override
	public String toString() {
		return this.val + "";
	}
	
	@Override
	public Entier clone() {
		return new Entier(this.val);
	}

}
