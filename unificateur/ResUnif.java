package unificateur;

import java.util.ArrayList;
import java.util.List;

import typeur.Status;
import typeur.TypeEquation;

public class ResUnif {

	public List<TypeEquation> res = new ArrayList<>();
	public Status status;
	public String cause;

	@Override
	public String toString() {
		if (this.status == Status.GECHEC || this.status == Status.ECHEC)
			return this.cause;
		String res = "(";
		for (TypeEquation e : this.res)
			res += e.toString() + ", ";
		return res.substring(0, res.length() - 2) + ")";
	}

}
