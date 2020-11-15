package termes;

public class Ref extends LambdaTerm {

	@Override
	public LambdaTerm clone() {
		return new Ref();
	}

}
