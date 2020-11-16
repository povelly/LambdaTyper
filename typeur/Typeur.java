package typeur;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import termes.LambdaTerm;
import termes.Operator;
import termes.Sequence;
import types.Type;
import unificateur.ResUnif;
import unificateur.Unificateur;

public class Typeur {

	public static Type guess = Type.Variable("???");

	public static ResUnif equa_gen(Map<String, Type> env, LambdaTerm l, Type t) {
		ResUnif resu = new ResUnif();
		resu.status = Status.GSUCCES;
		Type ta;
		Map<String, Type> env1;
		Map<String, Type> env2;
		Map<String, Type> env3;
		ResUnif resu1;
		ResUnif resu2;
		ResUnif resu3;
		switch (l.getClass().getSimpleName()) {
		case "Variable":
			Type res = env.get(l.vari);
			if (res == null) {
				resu.status = Status.GECHEC;
				resu.cause = l.vari + " n'est pas dans l'env de typage";
			}
			resu.res = new ArrayList<TypeEquation>() {
				private static final long serialVersionUID = 1L;
				{
					add(new TypeEquation(res, t));
				}
			};
			break;
		case "Abstraction":
			ta = Type.Variable(Type.var_gen());
			Type tr = Type.Variable(Type.var_gen());
			env.put(l.vari, ta);
			resu1 = Typeur.equa_gen(env, l.corps, tr);
			if (resu1.status == Status.GECHEC)
				return resu1;
			resu1.res.add(new TypeEquation(t, Type.Application(ta, tr)));
			resu.res = resu1.res;
			break;
		case "Application":
			if (l.fpos instanceof Operator && l.fpos.oper.equals("fix") && l.apos instanceof Sequence) {
				Type tfix = Type.Variable(Type.var_gen());
				env.put(l.apos.vari, tfix);
				ResUnif resfix = Typeur.equa_gen(env, l.apos.corps, tfix);
				if (resfix.status == Status.GECHEC)
					return resfix;
				resfix.res.add(new TypeEquation(t, Type.Application(t, tfix)));
				resu.res = resfix.res;
			} else {
				ta = Type.Variable(Type.var_gen());
				env1 = new HashMap<>();
				env2 = new HashMap<>();
				for (Entry<String, Type> e : env.entrySet()) {
					env1.put(e.getKey(), e.getValue());
					env2.put(e.getKey(), e.getValue());
				}
				ResUnif resuf = Typeur.equa_gen(env1, l.fpos, Type.Application(ta, t));
				ResUnif resua = Typeur.equa_gen(env2, l.apos, ta);
				if (resuf.status == Status.GECHEC)
					return resuf;
				if (resua.status == Status.GECHEC)
					return resua;
				resuf.res.addAll(resua.res);
				resu.res = resuf.res;
			}
			break;
		case "Let":
			env1 = new HashMap<>();
			env2 = new HashMap<>();
			for (Entry<String, Type> e : env.entrySet()) {
				env1.put(e.getKey(), e.getValue());
				env2.put(e.getKey(), e.getValue());
			}
			ResTypage tres = Typeur.type_env(env1, l.fpos);
			if (tres.status == Status.ECHEC) {
				resu.status = Status.GECHEC;
				resu.cause = "Dans le typage de " + l.fpos.toString() + " dans un let, echec : " + tres.cause;
			} else {
				if (l.fpos.est_non_expansif())
					env2.put(l.vari, Typeur.generalize(env2, tres.res));
				else
					env2.put(l.vari, Typeur.weak_generalize(env2, tres.res));
				ResUnif equa2 = Typeur.equa_gen(env2, l.apos, t);
				if (equa2.status == Status.GECHEC)
					return equa2;
				resu.res = equa2.res;
			}
			break;
		case "Entier":
			resu.res = new ArrayList<TypeEquation>() {
				private static final long serialVersionUID = 1L;
				{
					add(new TypeEquation(t, Type.Entier()));
				}
			};
			break;
		case "Sequence":
			Type tel = Type.Variable(Type.var_gen());
			List<TypeEquation> equas = new ArrayList<TypeEquation>() {
				private static final long serialVersionUID = 1L;
				{
					add(new TypeEquation(t, Type.Sequence(tel)));
				}
			};
			for (LambdaTerm elt : l.elements) {
				ResUnif resuel = Typeur.equa_gen(env, elt, tel);
				if (resuel.status == Status.GECHEC)
					return resuel;
				equas.addAll(resuel.res);
			}
			resu.res = equas;
			break;
		case "Operator":
			String tvar;
			switch (l.oper) {
			case "add":
			case "sub":
				resu.res = new ArrayList<TypeEquation>() {
					private static final long serialVersionUID = 1L;
					{
						add(new TypeEquation(t,
								Type.Application(Type.Entier(), Type.Application(Type.Entier(), Type.Entier()))));
					}
				};
				break;
			case "hd":
				tvar = Type.var_gen();
				resu.res = new ArrayList<TypeEquation>() {
					private static final long serialVersionUID = 1L;
					{
						add(new TypeEquation(t, Type.For(tvar,
								Type.Application(Type.Sequence(Type.Variable(tvar)), Type.Variable(tvar)))));
					}
				};
				break;
			case "cons":
				tvar = Type.var_gen();
				resu.res = new ArrayList<TypeEquation>() {
					private static final long serialVersionUID = 1L;
					{
						add(new TypeEquation(t, Type.For(tvar, Type.Application(Type.Variable(tvar), Type.Application(
								Type.Sequence(Type.Variable(tvar)), Type.Sequence(Type.Variable(tvar)))))));
					}
				};
				break;
			case "ref":
				tvar = Type.var_gen();
				resu.res = new ArrayList<TypeEquation>() {
					private static final long serialVersionUID = 1L;
					{
						add(new TypeEquation(t,
								Type.For(tvar, Type.Application(Type.Variable(tvar), Type.Ref(Type.Variable(tvar))))));
					}
				};
				break;
			case "deref":
				tvar = Type.var_gen();
				resu.res = new ArrayList<TypeEquation>() {
					private static final long serialVersionUID = 1L;
					{
						add(new TypeEquation(t,
								Type.For(tvar, Type.Application(Type.Ref(Type.Variable(tvar)), Type.Variable(tvar)))));
					}
				};
				break;
			case "assign":
				tvar = Type.var_gen();
				resu.res = new ArrayList<TypeEquation>() {
					private static final long serialVersionUID = 1L;
					{
						add(new TypeEquation(t, Type.For(tvar, Type.Application(Type.Ref(Type.Variable(tvar)),
								Type.Application(Type.Variable(tvar), Type.Unit())))));
					}
				};
				break;
			default: // ERROR opérateur non reconnu
				return null;
			}
		case "BranchZero":
			Type nt = Type.Variable(Type.var_gen());
			env1 = new HashMap<>();
			env2 = new HashMap<>();
			env3 = new HashMap<>();
			for (Entry<String, Type> e : env.entrySet()) {
				env1.put(e.getKey(), e.getValue());
				env2.put(e.getKey(), e.getValue());
				env3.put(e.getKey(), e.getValue());
			}
			resu1 = Typeur.equa_gen(env1, l.corps, Type.Entier());
			if (resu1.status == Status.GECHEC)
				return resu1;
			resu2 = Typeur.equa_gen(env2, l.apos, nt);
			if (resu2.status == Status.GECHEC)
				return resu2;
			resu3 = Typeur.equa_gen(env3, l.fpos, nt);
			if (resu3.status == Status.GECHEC)
				return resu3;
			List<TypeEquation> eqx = new ArrayList<>();
			eqx.addAll(resu1.res);
			eqx.addAll(resu2.res);
			eqx.addAll(resu3.res);
			eqx.add(new TypeEquation(t, nt));
			resu.res = eqx;
			break;
		case "BranchEmpty":
			ta = nt = Type.Variable(Type.var_gen());
			nt = Type.Variable(Type.var_gen());
			env1 = new HashMap<>();
			env2 = new HashMap<>();
			env3 = new HashMap<>();
			for (Entry<String, Type> e : env.entrySet()) {
				env1.put(e.getKey(), e.getValue());
				env2.put(e.getKey(), e.getValue());
				env3.put(e.getKey(), e.getValue());
			}
			resu1 = Typeur.equa_gen(env1, l.corps, Type.Sequence(ta));
			if (resu1.status == Status.GECHEC)
				return resu1;
			resu2 = Typeur.equa_gen(env2, l.apos, nt);
			if (resu2.status == Status.GECHEC)
				return resu2;
			resu3 = Typeur.equa_gen(env3, l.fpos, nt);
			if (resu3.status == Status.GECHEC)
				return resu3;
			eqx = new ArrayList<>();
			eqx.addAll(resu1.res);
			eqx.addAll(resu2.res);
			eqx.addAll(resu3.res);
			eqx.add(new TypeEquation(t, nt));
			resu.res = eqx;
			break;
		}
		return resu;
	}

	public static ResTypage type_env(Map<String, Type> env, LambdaTerm l) {
		ResTypage resu = new ResTypage();
		ResUnif eqs = Typeur.equa_gen(env, l, Typeur.guess);
		if (eqs.status == Status.GECHEC) {
			resu.status = Status.ECHEC;
			resu.cause = eqs.cause;
		} else {
			ResUnif ures = Unificateur.unification(eqs.res);
			if (ures.status == Status.FINI) {
				resu.status = Status.SUCCES;
				resu.res = Unificateur.recupGuess(ures.res);
			} else {
				resu.status = Status.ECHEC;
				resu.cause = ures.cause;
			}
		}
		return resu;
	}

	public static List<String> removeVar(String v, List<String> l) {
		List<String> acc = new ArrayList<>(l);
		acc.remove(v);
		return acc;
	}

	public static List<String> freeVars(Type t) {
		switch (t.type.getSimpleName()) {
		case "Variable":
			return new ArrayList<String>() {
				private static final long serialVersionUID = 1L;
				{
					add(t.tvari);
				}
			};
		case "WeakVariable":
			if (t.used)
				return Typeur.freeVars(t.targ);
			else
				return new ArrayList<String>() {
					private static final long serialVersionUID = 1L;
					{
						add(t.tvari);
					}
				};
		case "Application":
			List<String> vs2 = Typeur.freeVars(t.tres);
			List<String> vs1 = Typeur.freeVars(t.targ);
			for (String v2 : vs2) {
				vs1 = Typeur.removeVar(v2, vs1);
				vs1.add(v2);
			}
			return vs1;
		case "Entier":
		case "Unit":
			return Collections.emptyList();
		case "Sequence":
			return Typeur.freeVars(t.tres);
		case "For":
			return Typeur.removeVar(t.tvari, Typeur.freeVars(t.tres));
		case "WeakFor":
			if (t.used)
				return Typeur.freeVars(t.tres);
			return Typeur.removeVar(t.tvari, Typeur.freeVars(t.tres));
		case "Ref":
			return Typeur.freeVars(t.tres);
		default: // ERROR
			return null;
		}
	}
	
	public static ResTypage typeur(LambdaTerm l) {
		return Typeur.type_env(new HashMap<String, Type>(), l);
	}

	public static Type generalize(Map<String, Type> env, Type t) {
		List<String> vls = Typeur.freeVars(t);
		Type acc = t;
		for (String v : vls)
			acc = Type.For(v, acc);
		return acc;
	}

	public static Type weak_generalize(Map<String, Type> env, Type t) {
		List<String> vls = Typeur.freeVars(t);
		Type acc = t;
		for (String v : vls)
			acc = Type.WeakFor(v, acc);
		return acc;
	}

	public static String print_tequa(TypeEquation e) {
		return e.tg.toString() + " = " + e.td.toString();
	}

	public static String print_tequas(List<TypeEquation> l) {
		String res = "";
		for (TypeEquation e : l)
			res += Typeur.print_tequa(e) + "\n";
		return res;
	}

}
