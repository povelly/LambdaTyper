package termes;

public class Let extends LambdaTerm {

	public Let(String vari, LambdaTerm fpos, LambdaTerm apos) {
		this.vari = vari;
		this.fpos = fpos;
		this.apos = apos;
	}

	@Override
	public String toString() {
		return "let " + this.vari + " = " + this.fpos.toString() + " in " + this.apos.toString() + ")";
	}

	@Override
	public Let clone() {
		return new Let(this.vari, this.fpos.clone(), this.apos.clone());
	}

}
