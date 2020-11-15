package test;

import java.util.ArrayList;
import java.util.HashMap;

import termes.Abstraction;
import termes.Application;
import termes.Entier;
import termes.LambdaTerm;
import termes.Sequence;
import termes.Variable;
import types.Type;
import typeur.Typeur;

public class Main {

	public static final int MAX_UNIF = 1000;
	public static final int MAX_EVAL = 1000;

	public static void main(String[] args) {
		/*
		 * Entier i = new Entier(5); System.out.println(i.toString());
		 * System.out.println("-----------"); Type ti = Type.Entier();
		 * System.out.println(ti.toString()); System.out.println("-----------"); Type tv
		 * = Type.Variable("ℕ"); System.out.println(tv.toString());
		 * //System.out.println("\n\n-----------");
		 * 
		 * //System.out.println(">>"+ti.equals(Type.Entier()));
		 * System.out.println("-----------"); ResUnif resu =
		 * Typeur.equa_gen(Collections.emptyMap(), i, Type.Unit()); for (TypeEquation e
		 * : resu.res) { System.out.println(e.tg.toString());
		 * System.out.println(e.td.toString()); } System.out.println(resu.status);
		 */

		LambdaTerm ex_k = new Abstraction("x", new Abstraction("y", new Variable("z")));
		LambdaTerm ex_s = new Abstraction("x",
				new Abstraction("y",
						new Abstraction("z", new Application(new Application(new Variable("x"), new Variable("z")),
								new Application(new Variable("y"), new Variable("z"))))));
		LambdaTerm ex_skk = new Application(new Application(ex_s, ex_k), ex_k);
		// System.out.println(ex_skk.toString());
		// Evaluateur.ltrcbv(ex_skk);

		LambdaTerm ex_seq123 = new Sequence(new ArrayList<LambdaTerm>() {
			{
				add(new Entier(1));
				add(new Entier(2));
				add(new Entier(3));
			}
		});
		// System.out.println(Typeur.equa_gen(new HashMap<String, Type>(), ex_seq123,
		// Typeur.guess));

		// System.out.println(Typeur.equa_gen(new HashMap<String, Type>(), new
		// Abstraction("x", new Variable("x")), Typeur.guess));

		// System.out.println(Typeur.equa_gen(new HashMap<String, Type>() {
		// private static final long serialVersionUID = 1L;
		//
		// {
		// put("a", Type.Entier());
		// }
		// }, new Variable("a"), Typeur.guess).toString());

		System.out.println(Typeur.equa_gen(new HashMap<String, Type>() {
			private static final long serialVersionUID = 1L;

			{
				put("z", Type.Entier());
			}
		}, new Abstraction("y", new Variable("z")), Typeur.guess));

		System.out.println(Typeur.type_env(new HashMap<String, Type>(), ex_skk));
		System.out.println(Typeur.typeur(ex_seq123));

	}

}
