package types;

import termes.Application;
import termes.Entier;
import termes.For;
import termes.LambdaTerm;
import termes.Ref;
import termes.Sequence;
import termes.Unit;
import termes.Variable;
import termes.WeakFor;
import termes.WeakVariable;
import unificateur.Unificateur;

public class Type {

	public Class<? extends LambdaTerm> type;
	public String tvari;
	public Type targ;
	public Type tres;
	public boolean used;

	private static int cpt = 0;

	public static Type Variable(String tvari) {
		Type v = new Type();
		v.type = Variable.class;
		v.tvari = tvari;
		return v;
	}

	public static Type Application(Type targ, Type tres) {
		Type v = new Type();
		v.type = Application.class;
		v.targ = targ;
		v.tres = tres;
		return v;
	}

	public static Type Sequence(Type tres) {
		Type v = new Type();
		v.type = Sequence.class;
		v.tres = tres;
		return v;
	}

	public static Type Entier() {
		Type v = new Type();
		v.type = Entier.class;
		return v;
	}

	public static Type For(String tvari, Type tres) {
		Type v = new Type();
		v.type = For.class;
		v.tvari = tvari;
		v.tres = tres;
		return v;
	}

	public static Type WeakVariable(String tvari) {
		Type v = new Type();
		v.type = WeakVariable.class;
		v.tvari = tvari;
		return v;
	}

	public static Type WeakFor(String tvari, Type tres) {
		Type v = new Type();
		v.type = WeakFor.class;
		v.tvari = "_" + tvari;
		Type nvar = Type.WeakVariable(v.tvari);
		v.tres = Unificateur.substitue(tvari, nvar, tres);
		v.used = nvar.used;
		return v;
	}

	public static Type Ref(Type tres) {
		Type v = new Type();
		v.type = Ref.class;
		v.tres = tres;
		return v;
	}

	public static Type Unit() {
		Type v = new Type();
		v.type = Unit.class;
		return v;
	}

	public String toString() {
		switch (this.type.getSimpleName()) {
		case "Abstraction":
			return "(" + this.targ.toString() + ") → " + this.tres.toString();
		case "Application":
			return "(" + this.targ.toString() + ") → " + this.tres.toString();
		case "Variable":
			return this.tvari;
		case "Entier":
			return "ℕ";
		case "Unit":
			return "⬤";
		case "Sequence": // TODO
			return "[" + this.tres.toString() + "]";
		case "Ref":
			return "REF " + this.tres.toString();
		case "For":
			return "∀" + this.tvari + "." + this.tres.toString();
		case "WeakVariable":
			if (this.used)
				return this.tres.toString();
			else
				return this.tvari;
		case "WeakFor":
			if (this.used)
				return this.tres.toString();
			else
				return "∀" + this.tvari + "." + this.tres.toString();
		default:
			return "Error: " + this.type.getSimpleName();
		}
	}

	public boolean equals(Type t) {
		if (this.type != t.type)
			return false;
		switch (this.type.getSimpleName()) {
		case "Variable":
			if (this.tvari.equals(t.tvari))
				return true;
			break;
		case "WeakVariable":
			if (this.used == t.used) {
				if (this.used)
					return this.tvari.equals(t.tvari) && this.targ.equals(t.targ);
				else
					return this.tvari.equals(t.tvari);
			}
			break;
		case "WeakFor":
			if (this.used == t.used) {
				if (this.used)
					return this.tvari.equals(t.tvari);
				else
					return this.tvari.equals(t.tvari) && this.tres.equals(t.tres);
			}
			break;
		case "Application":
			return this.targ.equals(t.tres) && this.tres.equals(t.tres);
		case "Entier":
			return true;
		case "Let":
			return this.tres.equals(t.tres);
		case "Ref":
			return this.tres.equals(t.tres);
		case "For":
			String nv = Type.var_gen();
			return Unificateur.substitue(this.tvari, Type.Variable(nv), this.tres)
					.equals(Unificateur.substitue(t.tvari, Type.Variable(nv), t.tres));
		}
		return false;
	}

	public static String var_gen() {
		return "T" + ++cpt;
	}

}
