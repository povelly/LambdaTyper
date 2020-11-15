package termes;

public class BranchEmpty extends LambdaTerm {

	public BranchEmpty(LambdaTerm fpos, LambdaTerm apos, LambdaTerm corps) {
		this.fpos = fpos;
		this.apos = apos;
		this.corps = corps;
	}

	@Override
	public String toString() {
		return "(ifempty " + this.corps.toString() + " then " + this.fpos.toString() + ")";
	}

	@Override
	public BranchEmpty clone() {
		return new BranchEmpty(this.fpos.clone(), this.apos.clone(), this.corps.clone());
	}

}
