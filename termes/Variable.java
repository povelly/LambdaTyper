package termes;

public class Variable extends LambdaTerm {
	
	public Variable(String vari) {
		this.vari = vari;
	}
	
	@Override
	public String toString() {
		return this.vari;
	}
	
	@Override
	public Variable clone() {
		return new Variable(this.vari);
	}


}
