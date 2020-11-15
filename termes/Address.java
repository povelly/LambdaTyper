package termes;

public class Address extends LambdaTerm {

	public Address(String vari) {
		this.vari = vari;
	}
	
	@Override
	public String toString() {
		return this.vari;
	}
	
	@Override
	public Address clone() {
		return new Address(this.vari);
	}
	
	
}
