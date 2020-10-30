
(*
type lterme2 = V of string
             | L of string * lterme2
             | A of lterme2 * lterme2
*)

type lterme = V of string
             | L of {vari: string; corps: lterme}
             | A of {fpos: lterme; apos: lterme}

let rec print_lterme t =
  match t with
  | V(str) -> str
  | L(lmb) -> lmb.vari ^ print_lterme(lmb.corps)
  | A(abs) -> print_lterme(abs.fpos) ^ print_lterme(abs.apos);;

let t = V("a") in
print_string(print_lterme(t) ^ "\n")
;;
