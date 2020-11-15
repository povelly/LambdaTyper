package typeur;

import types.Type;

public class TypeEquation {

	public TypeEquation(Type tg, Type td) {
		this.tg = tg;
		this.td = td;
	}

	public Type tg;
	public Type td;

	@Override
	public TypeEquation clone() {
		return new TypeEquation(this.tg, this.td);
	}

	@Override
	public String toString() {
		return this.tg.toString() + " = " + this.td.toString();
	}
}
