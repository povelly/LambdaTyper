package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import evaluateur.Evaluateur;
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
		 * System.out.println("-----------"); Type ti = Type.Entier();
		 * System.out.println(ti.toString()); System.out.println("-----------"); Type tv
		 * = Type.Variable("ℕ"); System.out.println(tv.toString());
		 * System.out.println("\n\n-----------");
		 * System.out.println(">>"+ti.equals(Type.Entier()));
		 */

		// DEFINITION DES TERMES

		Entier i = new Entier(5);
		LambdaTerm ex_k = new Abstraction("x", new Abstraction("y", new Variable("x")));
		LambdaTerm ex_s = new Abstraction("x",
				new Abstraction("y",
						new Abstraction("z", new Application(new Application(new Variable("x"), new Variable("z")),
								new Application(new Variable("y"), new Variable("z"))))));
		LambdaTerm ex_skk = new Application(new Application(ex_s, ex_k), ex_k);

		LambdaTerm ex_seq123 = new Sequence(new ArrayList<LambdaTerm>() {
			private static final long serialVersionUID = 1L;

			{
				add(new Entier(1));
				add(new Entier(2));
				add(new Entier(3));
			}
		});

		// TEST GENERATION EQUATION
		
		System.out.println("---");
		System.out.println(Typeur.equa_gen(Collections.emptyMap(), i, Type.Unit()).toString());
		
		System.out.println("---");
		System.out.println(Typeur.equa_gen(new HashMap<String, Type>(), ex_seq123, Typeur.guess));

		System.out.println("---");
		System.out.println(
				Typeur.equa_gen(new HashMap<String, Type>(), new Abstraction("x", new Variable("x")), Typeur.guess));

		System.out.println("---");
		System.out.println(Typeur.equa_gen(new HashMap<String, Type>() {
			private static final long serialVersionUID = 1L;

			{
				put("a", Type.Entier());
			}
		}, new Variable("a"), Typeur.guess).toString());

		System.out.println("---");
		System.out.println(Typeur.equa_gen(new HashMap<String, Type>() {
			private static final long serialVersionUID = 1L;

			{
				put("z", Type.Entier());
			}
		}, new Abstraction("y", new Variable("z")), Typeur.guess));

		// TEST TYPEUR (Unification: OK)

		System.out.println("---");
		System.out.println(Typeur.type_env(new HashMap<>(), ex_s));

		System.out.println("---");
		System.out.println(Typeur.type_env(new HashMap<>(), ex_k));

		System.out.println("---");
		System.out.println(Typeur.type_env(new HashMap<>(), new Abstraction("x", new Variable("x"))));

		System.out.println("---");
		System.out.println(Typeur.typeur(ex_seq123));

		// TEST DE MEMOIRE
		System.out.println("---");
		Evaluateur.ltrcbv(ex_skk);
	}

}
