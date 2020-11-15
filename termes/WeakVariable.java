package termes;

public class WeakVariable extends LambdaTerm {

	@Override
	public LambdaTerm clone() {
		return new WeakVariable();
	}
	
}
