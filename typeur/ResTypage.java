package typeur;

import types.Type;

public class ResTypage {
	
	public Status status;
	Type res;
	String cause;
	
	@Override
	public String toString() {
		if (this.status == Status.GECHEC || this.status == Status.ECHEC)
			return this.cause;
		return res.toString();
	}
	
}