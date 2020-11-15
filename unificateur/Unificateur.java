package unificateur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import termes.Application;
import termes.Entier;
import termes.For;
import termes.Ref;
import termes.Sequence;
import termes.Unit;
import termes.Variable;
import termes.WeakFor;
import termes.WeakVariable;
import test.Main;
import types.Type;
import typeur.Status;
import typeur.TypeEquation;
import typeur.Typeur;

public class Unificateur {

	public static Type recupGuess(List<TypeEquation> eq) {
		for (TypeEquation res : eq) {
			if (res.tg.equals(Typeur.guess))
				return res.td;
			else if (res.td.equals(Typeur.guess))
				return res.tg;
		}
		return null; // ERROR Pas trouvé dans les équations
	}

	public static boolean checkOccur(String v, Type t) {
		switch (t.type.getSimpleName()) {
		case "Variable":
			return v.equals(t.tvari);
		case "WeakVariable":
			if (t.used)
				return Unificateur.checkOccur(v, t.tres);
			return v.equals(t.tvari);
		case "Application":
			return Unificateur.checkOccur(v, t.targ) || Unificateur.checkOccur(v, t.tres);
		case "Entier":
			return false;
		case "Sequence":
		case "Ref":
		case "For":
		case "WeakFor":
			return Unificateur.checkOccur(v, t.tres);
		default: // ERROR Type non supporté
			return false;
		}
	}

	public static Type substitue(String tvari, Type ts, Type t) {
		switch (t.type.getSimpleName()) {
		case "Variable":
			if (tvari.equals(t.tvari))
				return ts;
			return t;
		case "WeakVariable":
			if (t.used) {
				t.targ = Unificateur.substitue(tvari, ts, t.tres);
				Type res = new Type();
				res.type = WeakVariable.class;
				res.tvari = t.tvari;
				res.used = t.used;
				res.tres = t.targ;
				return res;
			}
			return t;
		case "Application":
			return Type.Application(Unificateur.substitue(tvari, ts, t.targ), Unificateur.substitue(tvari, ts, t.tres));
		case "Entier":
		case "Unit":
			return t;
		case "Sequence":
			return Type.Sequence(Unificateur.substitue(tvari, ts, t.tres));
		case "Ref":
			return Type.Ref(Unificateur.substitue(tvari, ts, t.tres));
		case "For":
			return Type.For(t.tvari, Unificateur.substitue(tvari, ts, t.tres));
		case "WeakFor":
			Type res = new Type();
			res.type = WeakFor.class;
			res.tvari = t.tvari;
			res.tres = Unificateur.substitue(tvari, ts, t.tres);
			res.used = t.used;
			return res;
		default: // error
			return null;
		}
	}

	public static Type tbarendregt_rec(Type t, Map<String, String> remp) {
		String nvari;
		Type nt;
		Type res;
		switch (t.type.getSimpleName()) {
		case "Entier":
		case "Unit":
			return t;
		case "Variable":
			if (!remp.containsKey(t.tvari))
				nvari = t.tvari;
			else
				nvari = remp.get(t.tvari);
			return Type.Variable(nvari);
		case "WeakVariable":
			if (t.used) {
				nt = Unificateur.tbarendregt_rec(t.targ, remp);
				t.tres = nt;
				res = new Type();
				res.type = WeakVariable.class;
				res.tvari = t.tvari;
				res.used = t.used;
				res.targ = t.targ;
				return res;
			}
			if (!remp.containsKey(t.tvari))
				nvari = t.tvari;
			else
				nvari = remp.get(t.tvari);
			res = new Type();
			res.type = WeakVariable.class;
			res.tvari = nvari;
			res.used = t.used;
			res.targ = t.targ;
			return res;
		case "Application":
			Map<String, String> remp1 = new HashMap<>();
			Map<String, String> remp2 = new HashMap<>();
			for (Entry<String, String> e : remp.entrySet()) {
				remp1.put(e.getKey(), e.getValue());
				remp2.put(e.getKey(), e.getValue());
			}
			return Type.Application(Unificateur.tbarendregt_rec(t.targ, remp1),
					Unificateur.tbarendregt_rec(t.tres, remp2));
		case "Sequence":
			return Type.Sequence(Unificateur.tbarendregt_rec(t.tres, remp));
		case "Ref":
			return Type.Ref(Unificateur.tbarendregt_rec(t.tres, remp));
		case "For":
			nvari = Type.var_gen();
			remp.put(t.tvari, nvari);
			return Type.For(nvari, Unificateur.tbarendregt_rec(t.tres, remp));
		case "WeakFor":
			if (t.used) {
				res = new Type();
				res.type = WeakFor.class;
				res.used = t.used;
				res.tres = Unificateur.tbarendregt_rec(t.tres, remp);
				res.tvari = t.tvari;
				res.targ = t.targ;
				return res;
			}
			nvari = "_" + Type.var_gen();
			remp.put(t.tvari, nvari);
			res = new Type();
			res.type = WeakFor.class;
			res.used = t.used;
			res.tres = Unificateur.tbarendregt_rec(t.tres, remp);
			res.tvari = t.tvari;
			res.targ = t.targ;
			return res;
		default: // ERROR Problème d'alpha conversion
			return null;
		}
	}

	public static Type tbarendregt(Type t) {
		return Unificateur.tbarendregt_rec(t, new HashMap<String, String>());
	}

	public static List<TypeEquation> substitue_partout(String v, Type ts, List<TypeEquation> eqs) {
		List<TypeEquation> res = new ArrayList<>();
		for (TypeEquation eq : eqs)
			res.add(new TypeEquation(Unificateur.substitue(v, ts, eq.tg), Unificateur.substitue(v, ts, eq.td)));
		return res;
	}

	public static String print_gen_res(ResUnif u) {
		if (u.status == Status.GSUCCES)
			return u.status + "\n" + Typeur.print_tequas(u.res);
		return u.status + " : " + u.cause;
	}

	public static ResUnif unification_etape(List<TypeEquation> eqs, int i) {
		ResUnif res = new ResUnif();
		List<TypeEquation> nt;
		if (i >= eqs.size()) {
			res.status = Status.FINI;
			res.res = eqs;
		} else if (eqs.get(i).tg.type.equals(Variable.class) && eqs.get(i).tg.equals(Typeur.guess)) {
			res.status = Status.CONTINUE;
			res.res = eqs;
		} else if (eqs.get(i).td.type.equals(Variable.class) && eqs.get(i).td.equals(Typeur.guess)) {
			res.status = Status.CONTINUE;
			res.res = eqs;
		} else if (eqs.get(i).tg.equals(eqs.get(i).td)) {
			res.status = Status.CONTINUE;
			nt = Unificateur.cloneEquas(eqs);
			nt.set(i, nt.get(nt.size() - 1));
			nt.subList(0, eqs.size() - 1);
			res.res = nt;
		} else if (eqs.get(i).tg.type.equals(For.class)) {
			res.status = Status.RECOMMENCE;
			Type typ1 = Unificateur.tbarendregt(eqs.get(i).tg).tres;
			TypeEquation eq1 = new TypeEquation(typ1, eqs.get(i).td);
			nt = Unificateur.cloneEquas(eqs);
			nt.set(i, nt.get(nt.size() - 1));
			nt.subList(0, nt.size() - 1);
			nt.add(eq1);
			res.res = nt;
		} else if (eqs.get(i).td.type.equals(For.class)) {
			res.status = Status.RECOMMENCE;
			Type typ1 = Unificateur.tbarendregt(eqs.get(i).td).tres;
			TypeEquation eq1 = new TypeEquation(typ1, eqs.get(i).tg);
			nt = Unificateur.cloneEquas(eqs);
			nt.set(i, nt.get(nt.size() - 1));
			nt.subList(0, nt.size() - 1);
			nt.add(eq1);
			res.res = nt;
		} else if (eqs.get(i).tg.type.equals(WeakFor.class)) {
			if (eqs.get(i).tg.used) {
				res.status = Status.RECOMMENCE;
				TypeEquation eq1 = new TypeEquation(eqs.get(i).tg.tres, eqs.get(i).td);
				nt = Unificateur.cloneEquas(eqs);
				nt.set(i, nt.get(nt.size() - 1));
				nt.subList(0, nt.size() - 1);
				nt.add(eq1);
				res.res = nt;
			} else {
				res.status = Status.RECOMMENCE;
				Type typ1 = eqs.get(i).tg.tres;
				TypeEquation eq1 = new TypeEquation(typ1, eqs.get(i).td);
				nt = Unificateur.cloneEquas(eqs);
				nt.set(i, nt.get(nt.size() - 1));
				nt.subList(0, nt.size() - 1);
				nt.add(eq1);
				res.res = nt;
			}
		} else if (eqs.get(i).tg.type.equals(Variable.class)) {
			if (!(eqs.get(i).td.type.equals(Variable.class))
					&& Unificateur.checkOccur(eqs.get(i).tg.tvari, eqs.get(i).td)) {
				res.status = Status.ECHEC;
				res.cause = "Variable " + eqs.get(i).tg.tvari + " présente dans " + eqs.get(i).td.toString();
			} else {
				res.status = Status.RECOMMENCE;
				nt = Unificateur.cloneEquas(eqs);
				nt.set(i, nt.get(nt.size() - 1));
				nt.subList(0, eqs.size() - 1);
				res.res = Unificateur.substitue_partout(eqs.get(i).tg.tvari, eqs.get(i).td, nt);
			}
		} else if (eqs.get(i).td.type.equals(Variable.class)) {
			if (Unificateur.checkOccur(eqs.get(i).td.tvari, eqs.get(i).tg)) {
				res.status = Status.ECHEC;
				res.cause = "Variable " + eqs.get(i).td.tvari + " présente dans " + eqs.get(i).tg.toString();
			} else {
				res.status = Status.RECOMMENCE;
				nt = Unificateur.cloneEquas(eqs);
				nt.set(i, nt.get(nt.size() - 1));
				nt.subList(0, nt.size() - 1);
				res.res = Unificateur.substitue_partout(eqs.get(i).td.tvari, eqs.get(i).tg, nt);
			}
		} else if (eqs.get(i).tg.type.equals(WeakVariable.class)) {
			if (!(eqs.get(i).td.type.equals(WeakVariable.class))
					&& Unificateur.checkOccur(eqs.get(i).tg.tvari, eqs.get(i).td)) {
				res.status = Status.ECHEC;
				res.cause = "Variable " + eqs.get(i).tg.tvari + " présente dans " + eqs.get(i).td.toString();
			} else {
				if (eqs.get(i).tg.used) {
					res.status = Status.RECOMMENCE;
					nt = Unificateur.cloneEquas(eqs);
					nt.set(i, nt.get(nt.size() - 1));
					nt.subList(0, nt.size() - 1);
					nt.add(new TypeEquation(eqs.get(i).tg.tres, eqs.get(i).td));
					res.res = nt;
				} else {
					res.status = Status.RECOMMENCE;
					eqs.get(i).tg.used = true;
					eqs.get(i).tg.tres = eqs.get(i).td;
					nt = Unificateur.cloneEquas(eqs);
					nt.set(i, nt.get(nt.size() - 1));
					nt.subList(0, nt.size() - 1);
					res.res = nt;
				}
			}
		} else if (eqs.get(i).td.type.equals(WeakVariable.class)) {
			if (!(eqs.get(i).td.type.equals(WeakVariable.class))
					&& Unificateur.checkOccur(eqs.get(i).td.tvari, eqs.get(i).tg)) {
				res.status = Status.ECHEC;
				res.cause = "Variable " + eqs.get(i).td.tvari + " présente dans " + eqs.get(i).tg.toString();
			} else {
				if (eqs.get(i).td.used) {
					res.status = Status.RECOMMENCE;
					nt = Unificateur.cloneEquas(eqs);
					nt.set(i, nt.get(nt.size() - 1));
					nt.subList(0, nt.size() - 1);
					nt.add(new TypeEquation(eqs.get(i).td.tres, eqs.get(i).tg));
					res.res = nt;
				} else {
					res.status = Status.RECOMMENCE;
					eqs.get(i).td.used = true;
					eqs.get(i).td.tres = eqs.get(i).tg;
					nt = Unificateur.cloneEquas(eqs);
					nt.set(i, nt.get(nt.size() - 1));
					nt.subList(0, nt.size() - 1);
					res.res = nt;
				}
			}
		} else if (eqs.get(i).tg.type.equals(Application.class)) {
			if (eqs.get(i).td.type.equals(Application.class)) {
				res.status = Status.RECOMMENCE;
				nt = Unificateur.cloneEquas(eqs);
				nt.set(i, nt.get(nt.size() - 1));
				nt.subList(0, nt.size() - 1);
				nt.add(new TypeEquation(eqs.get(i).tg.targ, eqs.get(i).td.targ));
				nt.add(new TypeEquation(eqs.get(i).tg.tres, eqs.get(i).td.tres));
				res.res = nt;
			} else {
				res.status = Status.ECHEC;
				res.cause = "Variable " + eqs.get(i).tg.toString() + " présente dans " + eqs.get(i).td.toString();
			}
		} else if (eqs.get(i).tg.type.equals(Entier.class)) {
			res.status = Status.ECHEC;
			res.cause = "Type entier incompatible avec " + eqs.get(i).td.toString();
		} else if (eqs.get(i).tg.type.equals(Unit.class)) {
			res.status = Status.ECHEC;
			res.cause = "Type unit incompatible avec " + eqs.get(i).td.toString();
		} else if (eqs.get(i).tg.type.equals(Sequence.class)) {
			if (eqs.get(i).td.type.equals(Sequence.class)) {
				res.status = Status.RECOMMENCE;
				nt = Unificateur.cloneEquas(eqs);
				nt.set(i, nt.get(nt.size() - 1));
				nt.subList(0, nt.size() - 1);
				nt.add(new TypeEquation(eqs.get(i).tg.tres, eqs.get(i).td.tres));
				res.res = nt;
			} else {
				res.status = Status.ECHEC;
				res.cause = "Variable " + eqs.get(i).tg.toString() + " présente dans " + eqs.get(i).td.toString();
			}
		} else if (eqs.get(i).tg.type.equals(Ref.class)) {
			if (eqs.get(i).td.type.equals(Ref.class)) {
				res.status = Status.RECOMMENCE;
				nt = Unificateur.cloneEquas(eqs);
				nt.set(i, nt.get(nt.size() - 1));
				nt.subList(0, nt.size() - 1);
				nt.add(new TypeEquation(eqs.get(i).tg.tres, eqs.get(i).td.tres));
				res.res = nt;
			} else {
				res.status = Status.ECHEC;
				res.cause = "Type ref " + eqs.get(i).tg.toString() + " incompatible avec " + eqs.get(i).td.toString();
			}
		} else { // ERROR
			res.status = Status.ECHEC;
			res.cause = "Cas non pris en charge";
		}
		return res;
	}

	public static ResUnif unification(List<TypeEquation> e) {
		int i = 0; // eq courantes dans eqs
		int c = 0; // nb etapes
		List<TypeEquation> eqs = Unificateur.cloneEquas(e);
		ResUnif resu = new ResUnif();
		while (c < Main.MAX_UNIF) {
			resu = Unificateur.unification_etape(eqs, i);
			switch (resu.status) {
			case CONTINUE:
				eqs = resu.res;
				i++;
				break;
			case RECOMMENCE:
				eqs = resu.res;
				i = 0;
				break;
			case ECHEC:
				return resu;
			case FINI:
				return resu;
			default:
				break;
			}
			c++;
		}
		ResUnif res = new ResUnif();
		res.status = Status.EXPIRE;
		return res;
	}

	public static List<TypeEquation> cloneEquas(List<TypeEquation> l) {
		List<TypeEquation> res = new ArrayList<>();
		for (TypeEquation e : l)
			res.add(e.clone());
		return res;
	}

}
