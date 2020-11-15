package termes;

public class Unit extends LambdaTerm {

	public Unit() {
	}

	@Override
	public String toString() {
		return "□";
	}
	
	@Override
	public Unit clone() {
		return new Unit();
	}


}
