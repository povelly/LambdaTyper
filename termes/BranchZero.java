package termes;

public class BranchZero extends LambdaTerm {

	public BranchZero(LambdaTerm fpos, LambdaTerm apos, LambdaTerm corps) {
		this.fpos = fpos;
		this.apos = apos;
		this.corps = corps;
	}

	@Override
	public String toString() {
		return "(if0 " + this.corps.toString() + " then " + this.fpos.toString() + ")";
	}

	@Override
	public BranchZero clone() {
		return new BranchZero(this.fpos.clone(), this.apos.clone(), this.corps.clone());
	}

}
