package termes;

public class Abstraction extends LambdaTerm {
	
	public Abstraction(String vari, LambdaTerm corps) {
		this.vari = vari;
		this.corps = corps;
	}
	
	@Override
	public String toString() {
		return "λ" + this.vari + "." + this.corps.toString();
	}
	
	@Override
	public Abstraction clone() {
		return new Abstraction(this.vari, this.corps);
	}
	
}
