package evaluateur;

import java.util.HashMap;
import java.util.Map;

import termes.LambdaTerm;
import typeur.Status;

public class ResEval {
	
	public Status status;
	public LambdaTerm res;
	public Map<String, LambdaTerm> rmem = new HashMap<>();
	
	public ResEval() {
		
	}
	
	public ResEval(Status status, LambdaTerm res, Map<String, LambdaTerm> rmem) {
		this.status = status;
		this.res = res;
		this.rmem = rmem;
	}

}
