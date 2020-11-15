package evaluateur;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import termes.Abstraction;
import termes.Address;
import termes.Application;
import termes.BranchEmpty;
import termes.BranchZero;
import termes.Entier;
import termes.LambdaTerm;
import termes.Let;
import termes.Operator;
import termes.Ref;
import termes.Sequence;
import termes.Unit;
import termes.Variable;
import test.Main;
import types.Type;
import typeur.Status;

public class Evaluateur {

	public static LambdaTerm barendregt_rec(LambdaTerm l, Map<String, String> remp) {
		String nvari;
		Map<String, String> remp1 = new HashMap<>();
		Map<String, String> remp2 = new HashMap<>();
		switch (l.getClass().getSimpleName()) {
		case "Variable":
			if (!remp.containsKey(l.vari))
				nvari = l.vari;
			else
				nvari = remp.get(l.vari);
			return new Variable(nvari);
		case "Abstraction":
			nvari = Type.var_gen();
			remp.put(l.vari, nvari);
			return new Abstraction(nvari, Evaluateur.barendregt_rec(l.corps, remp));
		case "Application":
			for (Entry<String, String> e : remp.entrySet()) {
				remp1.put(e.getKey(), e.getValue());
				remp2.put(e.getKey(), e.getValue());
			}
			return new Application(Evaluateur.barendregt_rec(l.fpos, remp1), Evaluateur.barendregt_rec(l.apos, remp2));
		case "Sequence":
			List<LambdaTerm> res = new ArrayList<>();
			for (LambdaTerm el : l.elements) {
				Map<String, String> remps = new HashMap<>();
				for (Entry<String, String> e : remp.entrySet())
					remps.put(e.getKey(), e.getValue());
				res.add(Evaluateur.barendregt_rec(el, remps));
			}
			return new Sequence(res);
		case "BranchZero":
			for (Entry<String, String> e : remp.entrySet()) {
				remp1.put(e.getKey(), e.getValue());
				remp2.put(e.getKey(), e.getValue());
			}
			return new BranchZero(Evaluateur.barendregt_rec(l.corps, remp1), Evaluateur.barendregt_rec(l.fpos, remp2),
					Evaluateur.barendregt_rec(l.apos, remp));
		case "BranchEmpty":
			for (Entry<String, String> e : remp.entrySet()) {
				remp1.put(e.getKey(), e.getValue());
				remp2.put(e.getKey(), e.getValue());
			}
			return new BranchEmpty(Evaluateur.barendregt_rec(l.corps, remp1), Evaluateur.barendregt_rec(l.fpos, remp2),
					Evaluateur.barendregt_rec(l.apos, remp));
		case "Let":
			nvari = Type.var_gen();
			for (Entry<String, String> e : remp.entrySet()) {
				remp1.put(e.getKey(), e.getValue());
				remp2.put(e.getKey(), e.getValue());
			}
			remp2.put(l.vari, nvari);
			return new Let(nvari, Evaluateur.barendregt_rec(l.fpos, remp1), Evaluateur.barendregt_rec(l.apos, remp2));
		default:
			return l;
		}
	}

	public static LambdaTerm barendregt(LambdaTerm l) {
		return Evaluateur.barendregt_rec(l, new HashMap<>());
	}

	public static LambdaTerm instancie(LambdaTerm l, String x, LambdaTerm a) {
		switch (l.getClass().getSimpleName()) {
		case "Variable":
			if (!l.vari.equals(x))
				return new Variable(l.vari);
			return a;
		case "Abstraction":
			return new Abstraction(l.vari, Evaluateur.instancie(l.corps, x, a));
		case "Application":
			return new Application(Evaluateur.instancie(l.fpos, x, a), Evaluateur.instancie(l.apos, x, a));
		case "BranchZero":
			return new BranchZero(Evaluateur.instancie(l.corps, x, a), Evaluateur.instancie(l.fpos, x, a),
					Evaluateur.instancie(l.apos, x, a));
		case "BranchEmpty":
			return new BranchEmpty(Evaluateur.instancie(l.corps, x, a), Evaluateur.instancie(l.fpos, x, a),
					Evaluateur.instancie(l.apos, x, a));
		case "Let":
			return new Let(l.vari, Evaluateur.instancie(l.fpos, x, a), Evaluateur.instancie(l.apos, x, a));
		case "Sequence":
			List<LambdaTerm> res = new ArrayList<>();
			for (LambdaTerm el : l.elements)
				res.add(Evaluateur.instancie(el, x, a));
			return new Sequence(res);
		case "Operator":
		case "Entier":
		case "Ref":
		case "Unit":
			return l;
		default: // ERROR constructeur de terme inconnu pdt l'instanciation
			return null;
		}
	}

	public static ResEval ltrcbv_rec(LambdaTerm l, Map<String, LambdaTerm> mem) {
		ResEval resu = new ResEval();
		resu.status = Status.KO;
		resu.rmem = mem;
		ResEval resfun;
		switch (l.getClass().getSimpleName()) {
		case "Sequence":
			List<LambdaTerm> temp = Evaluateur.cloneTerms(l.elements);
			for (int i = 0; i < temp.size(); i++) {
				ResEval resel = Evaluateur.ltrcbv_rec(temp.get(i), mem);
				if (resel.status == Status.OK) {
					List<LambdaTerm> temp2 = Evaluateur.cloneTerms(temp);
					temp2.set(i, resel.res);
					resu.res = new Sequence(temp2);
					resu.status = Status.OK;
					break;
				}
			}
			break;
		case "Application":
			resfun = Evaluateur.ltrcbv_rec(l.fpos, mem);
			if (resfun.status == Status.OK) {
				resu.res = new Application(resfun.res, l.apos);
				resu.status = Status.OK;
			} else {
				ResEval resarg = Evaluateur.ltrcbv_rec(l.apos, mem);
				if (resarg.status == Status.OK) {
					resu.res = new Application(l.fpos, resarg.res);
					resu.status = Status.OK;
				} else if (l.fpos instanceof Abstraction) {
					resu.res = Evaluateur.instancie(l.fpos.corps, l.fpos.vari, l.apos);
					resu.status = Status.OK;
				} else if (l.fpos instanceof Operator) {
					switch (l.fpos.oper) {
					case "hd":
						if (l.apos instanceof Sequence && l.apos.elements.size() > 0) {
							resu.res = l.apos.elements.get(0);
							resu.status = Status.OK;
						}
						break;
					case "fix":
						if (l.apos instanceof Abstraction) {
							resu.res = Evaluateur.instancie(Evaluateur.barendregt(l.apos.corps), l.apos.vari, l);
							resu.status = Status.OK;
						}
						break;
					case "ref":
						String rho = Type.var_gen();
						resu.res = new Address(rho);
						mem.put(rho, l.apos);
						resu.rmem = mem;
						resu.status = Status.OK;
						break;
					case "deref":
						if (l.apos instanceof Ref) {
							resu.res = mem.get(l.apos.vari);
							resu.status = Status.OK;
						}
						break;
					}
				} else if (l.fpos instanceof Application) {
					if (l.fpos.fpos instanceof Operator) {
						switch (l.fpos.fpos.oper) {
						case "add":
							if (l.apos instanceof Entier && l.fpos.apos instanceof Entier) {
								resu.res = new Entier(l.apos.val + l.fpos.apos.val);
								resu.status = Status.OK;
							}
							break;
						case "sub":
							if (l.apos instanceof Entier && l.fpos.apos instanceof Entier
									&& l.apos.val - l.fpos.apos.val <= 0) {
								resu.res = new Entier(l.fpos.apos.val - l.fpos.apos.val);
								resu.status = Status.OK;
							}
							break;
						case "cons":
							if (l.apos instanceof Sequence) {
								resu.res = new Sequence(LambdaTerm.cons(l.fpos.apos, l.apos.elements));
								resu.status = Status.OK;
							}
							break;
						case "assign":
							if (l.fpos.apos instanceof Ref) {
								mem.put(l.fpos.apos.vari, l.apos);
								resu.rmem = mem;
								resu.res = new Unit();
								resu.status = Status.OK;
							}
							break;
						}
					}
				}
			}
			break;
		case "Let":
			resfun = Evaluateur.ltrcbv_rec(l.fpos, mem);
			if (resfun.status == Status.OK) {
				LambdaTerm res = new Let(l.vari, resfun.res, l.apos);
				resu.res = res;
				resu.status = Status.OK;
			} else {
				LambdaTerm res = Evaluateur.instancie(l.apos, l.vari, l.fpos);
				resu.res = res;
				resu.status = Status.OK;
			}
			break;
		case "BranchZero":
			resfun = Evaluateur.ltrcbv_rec(l.corps, mem);
			if (resfun.status == Status.OK) {
				LambdaTerm res = new BranchZero(resfun.res, l.fpos, l.apos);
				resu.res = res;
				resu.status = Status.OK;
			} else if (l.corps instanceof Entier) {
				if (l.corps.val == 0) {
					resu.res = l.fpos;
					resu.status = Status.OK;
				} else {
					resu.res = l.apos;
					resu.status = Status.OK;
				}
			}
			break;
		case "BranchEmpty":
			resfun = Evaluateur.ltrcbv_rec(l.corps, mem);
			if (resfun.status == Status.OK) {
				LambdaTerm res = new BranchEmpty(resfun.res, l.fpos, l.apos);
				resu.res = res;
				resu.status = Status.OK;
			} else if (l.corps instanceof Sequence) {
				if (l.corps.elements.size() == 0) {
					resu.res = l.fpos;
					resu.status = Status.OK;
				} else {
					resu.res = l.apos;
					resu.status = Status.OK;
				}
			}
			break;
		default:
			break;
		}
		return resu;
	}

	public static void ltrcbv(LambdaTerm l) {
		LambdaTerm courant = Evaluateur.barendregt(l);
		Map<String, LambdaTerm> mem = new HashMap<>();
		int c = 0;
		System.out.println(courant.toString());
		while (c < Main.MAX_EVAL) {
			ResEval nouveau = Evaluateur.ltrcbv_rec(courant, mem);
			if (nouveau.status == Status.KO)
				break;
			System.out.println("→" + nouveau.res.toString());
			courant = nouveau.res;
			mem = nouveau.rmem;
			c++;
		}
		if (c == Main.MAX_EVAL)
			System.out.println("Stop : trop de réductions");
	}

	public static List<LambdaTerm> cloneTerms(List<LambdaTerm> l) {
		List<LambdaTerm> res = new ArrayList<>();
		for (LambdaTerm e : l)
			res.add(e.clone());
		return res;
	}

}
