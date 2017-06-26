package compilador;


import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Interpretador {

	private No raiz;
	private List<Simbolo> simbolos;
	private List<String> erros = new ArrayList<String>();
	private Scanner sc;

	public Interpretador(No raiz, List<Simbolo> simbolos) {
		this.setRaiz(raiz);
		this.setSimbolos(simbolos);
	}

	public void interpretar() {
		try {
			interpretar(raiz);

			System.out.println("\nTabela de Simbolos depois de interpretar 9:");
			System.out.format("%10s%10s%10s%10s\n", "Indice", "Imagem", "Tipo", "Valor");
			for (int i = 0; i < simbolos.size(); i++) {
				System.out.format("%10s", i);
				System.out.format("%10s", simbolos.get(i).getImagem());
				System.out.format("%10s", simbolos.get(i).getTipo());
				System.out.format("%10s\n", simbolos.get(i).getValor());
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

	private Object interpretar(No no) {
		if (no.getTipo().equals("NO_TOKEN")) {
			return null;
		} else if (no.getTipo().equals("NO_LIST_CMD")) {
			return listCmd(no);
		} else if (no.getTipo().equals("NO_CMD")) {
			return cmd(no);
		} else if (no.getTipo().equals("NO_CMD_INTER")) {
			return cmdInter(no);
		} else if (no.getTipo().equals("NO_DECL")) {
			return decl(no);
		} else if (no.getTipo().equals("NO_TIPO")) {
			return tipo(no);
		} else if (no.getTipo().equals("NO_LIST_ID")) {
			return listId(no);
		} else if (no.getTipo().equals("NO_LIST_ID_2")) {
			return listId2(no);
		} else if (no.getTipo().equals("NO_ATRIB")) {
			return atrib(no);
		} else if (no.getTipo().equals("NO_EXP_ARIT")) {
			return expArit(no);
		} else if (no.getTipo().equals("NO_EXP_ARIT")) {
			return expArit(no);
		} else if (no.getTipo().equals("NO_OP_ARIT")) {
			return opArit(no);
		} else if (no.getTipo().equals("NO_OPERAN")) {
			return operan(no);
		} else if (no.getTipo().equals("NO_LACO")) {
			return laco(no);
		} else if (no.getTipo().equals("NO_EXP_LOG")) {
			return expLog(no);
		} else if (no.getTipo().equals("NO_EXP_REL")) {
			return expRel(no);
		} else if (no.getTipo().equals("NO_OP_REL")) {
			return opRel(no);
		} else if (no.getTipo().equals("NO_OP_LOG")) {
			return opLog(no);
		} else {
			addErro("Erro: NO desconhecido = " + no.getTipo());
			return null;
		}
	}

	// <op_log> ::= '&&' | '||'
	private Object opLog(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <op_rel> ::= '>' | '<' | '>=' | '<=' | '==' | '!='
	private Object opRel(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <exp_rel> ::= <op_rel> <operan> <operan> | <op_log> '{' <exp_rel> '}' '{'
	// <exp_rel> '}'
	private Object expRel(No no) {
		if (no.getFilhos().get(0).getTipo().equals("NO_OP_REL")) {

			Object sinal = interpretar(no.getFilhos().get(0));

			Object operan1 = (Object) interpretar(no.getFilhos().get(1));
			Object operan2 = (Object) interpretar(no.getFilhos().get(2));

			if (sinal.equals(">")) {
				if (operan1 instanceof Integer && operan2 instanceof Integer) {
					if (((Integer) operan1).intValue() > ((Integer) operan2).intValue()) {
						return true;
					} else {
						return false;
					}
				} else if (operan1 instanceof Double && operan2 instanceof Double) {
					if (((Double) operan1).doubleValue() > ((Double) operan2).doubleValue()) {
						return true;
					} else {
						return false;
					}
				}

			} else if (sinal.equals("<")) {
				if (operan1 instanceof Integer && operan2 instanceof Integer) {
					if (((Integer) operan1).intValue() < ((Integer) operan2).intValue()) {
						return true;
					} else {
						return false;
					}
				} else if (operan1 instanceof Double && operan2 instanceof Double) {
					if (((Double) operan1).doubleValue() < ((Double) operan2).doubleValue()) {
						return true;
					} else {
						return false;
					}
				}
			} else if (sinal.equals(">=")) {
				if (operan1 instanceof Integer && operan2 instanceof Integer) {
					if (((Integer) operan1).intValue() >= ((Integer) operan2).intValue()) {
						return true;
					} else {
						return false;
					}
				} else if (operan1 instanceof Double && operan2 instanceof Double) {
					if (((Double) operan1).doubleValue() >= ((Double) operan2).doubleValue()) {
						return true;
					} else {
						return false;
					}
				}
			} else if (sinal.equals("<=")) {
				if (operan1 instanceof Integer && operan2 instanceof Integer) {
					if (((Integer) operan1).intValue() <= ((Integer) operan2).intValue()) {
						return true;
					} else {
						return false;
					}
				} else if (operan1 instanceof Double && operan2 instanceof Double) {
					if (((Double) operan1).doubleValue() <= ((Double) operan2).doubleValue()) {
						return true;
					} else {
						return false;
					}
				}
			} else if (sinal.equals("==")) {
				if (operan1 instanceof Integer && operan2 instanceof Integer) {
					if (((Integer) operan1).intValue() == ((Integer) operan2).intValue()) {
						return true;
					} else {
						return false;
					}
				} else if (operan1 instanceof Double && operan2 instanceof Double) {
					if (((Double) operan1).doubleValue() == ((Double) operan2).doubleValue()) {
						return true;
					} else {
						return false;
					}
				}
			} else if (sinal.equals("!=")) {
				if (operan1 instanceof Integer && operan2 instanceof Integer) {
					if (((Integer) operan1).intValue() != ((Integer) operan2).intValue()) {
						return true;
					} else {
						return false;
					}
				} else if (operan1 instanceof Double && operan2 instanceof Double) {
					if (((Double) operan1).doubleValue() != ((Double) operan2).doubleValue()) {
						return true;
					} else {
						return false;
					}
				}
			}

		} else if (no.getFilhos().get(0).getTipo().equals("NO_OP_LOG")) {
			interpretar(no.getFilhos().get(2));
			interpretar(no.getFilhos().get(5));
		}

		return null;
	}

	// <exp_log> ::= '{' <exp_rel> '}'
	private Object expLog(No no) {
		return interpretar(no.getFilhos().get(1));
	}

	// <laco> ::= 'enquanto' <exp_log> <list_cmd>
	private Object laco(No no) {
		boolean retorno = (boolean) interpretar(no.getFilhos().get(1));

		while (retorno == true) {
			interpretar(no.getFilhos().get(2));
			retorno = (boolean) interpretar(no.getFilhos().get(1));
		}

		return null;
	}

	// <operan> ::= id | cli | clr | cls
	private Object operan(No no) {
		Token token = (Token) no.getFilhos().get(0).getToken();
		// System.out.println(token);
		if (token.getClasse().equals("ID")) {
			return simbolos.get(token.getIndice()).getValor();
		} else if (token.getClasse().equals("CLI")) {
			return Integer.parseInt(token.getImagem());
		} else if (token.getClasse().equals("CLR")) {
			return Double.parseDouble(token.getImagem());
		} else if (token.getClasse().equals("CLS")) {
			return token.getImagem();
		}
		return null;
	}

	// <op_arit> ::= '+' | '-' | '*' | '/' | '.'
	private Object opArit(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <exp_arit> ::= <operan> | '{' <op_arit> <exp_arit> <exp_arit> '}'
	private Object expArit(No no) {
		if (no.getFilhos().size() == 1) {
			return interpretar(no.getFilhos().get(0));
		} else if (no.getFilhos().size() == 5) {
			Object operador = interpretar(no.getFilhos().get(1));

			Object valor1 = (Object) interpretar(no.getFilhos().get(2));
			Object valor2 = (Object) interpretar(no.getFilhos().get(3));

			if (operador.equals("+")) {
				if (valor1 instanceof Integer && valor2 instanceof Integer) {
					return ((Integer) valor1).intValue() + ((Integer) valor2).intValue();
				} else if (valor1 instanceof Double && valor2 instanceof Double) {
					return ((Double) valor1).doubleValue() + ((Double) valor2).doubleValue();
				}
			} else if (operador.equals("-")) {
				if (valor1 instanceof Integer && valor2 instanceof Integer) {
					return ((Integer) valor1).intValue() - ((Integer) valor2).intValue();
				} else if (valor1 instanceof Double && valor2 instanceof Double) {
					return ((Double) valor1).doubleValue() - ((Double) valor2).doubleValue();
				}
			} else if (operador.equals("*")) {
				if (valor1 instanceof Integer && valor2 instanceof Integer) {
					return ((Integer) valor1).intValue() * ((Integer) valor2).intValue();
				} else if (valor1 instanceof Double && valor2 instanceof Double) {
					return ((Double) valor1).doubleValue() * ((Double) valor2).doubleValue();
				}
			} else if (operador.equals("/")) {
				if (valor1 instanceof Integer && valor2 instanceof Integer) {
					return ((Integer) valor1).intValue() / ((Integer) valor2).intValue();
				} else if (valor1 instanceof Double && valor2 instanceof Double) {
					return ((Double) valor1).doubleValue() / ((Double) valor2).doubleValue();
				}
			} else if (operador.equals(".")) {
				if (valor1 instanceof String || valor2 instanceof String) {
					valor1 = valor1.toString();
					valor2 = valor2.toString();
					return ((String) valor1) + ((String) valor2);
				}
			}
		}
		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = (Token) no.getFilhos().get(1).getToken();
		Object valor = interpretar(no.getFilhos().get(2));

		simbolos.get(id.getIndice()).setValor(valor);
		return null;
	}

	// <list_id2> ::= <list_id> | &
	private Object listId2(No no) {
		if (no.getFilhos().isEmpty()) {
			return new ArrayList<Token>();
		} else {
			return interpretar(no.getFilhos().get(0));
		}
	}

	// <list_id> ::= id <list_id2>
	@SuppressWarnings("unchecked")
	private Object listId(No no) {
		Token id = no.getFilhos().get(0).getToken();
		List<Token> listId2 = (List<Token>) interpretar(no.getFilhos().get(1));
		listId2.add(0, id);

		return listId2;
	}

	// FIM DAS REGRAS

	// <tipo> ::= 'int' | 'real' | 'texto' | 'logico'
	private Object tipo(No no) {
		return no.getFilhos().get(0).getToken();
	}

	// <decl> ::= <tipo> <list_id>
	@SuppressWarnings("unchecked")
	private Object decl(No no) {

		Token tipo = (Token) interpretar(no.getFilhos().get(0));
		List<Token> listaIds = (List<Token>) interpretar(no.getFilhos().get(1));

		for (Token listaId : listaIds) {
			if (tipo.getImagem().equals("int")) {
				simbolos.get(listaId.getIndice()).setValor("0");
			} else if (tipo.getImagem().equals("real")) {
				simbolos.get(listaId.getIndice()).setValor("0.0");
			} else if (tipo.getImagem().equals("texto")) {
				simbolos.get(listaId.getIndice()).setValor("\"\"");
			}
		}

		return null;
	}

	// <cmd_inter> ::= <decl> | <atrib> | <laco> | <cond> | <escrita> |
	// <leitura>
	private Object cmdInter(No no) {
		interpretar(no.getFilhos().get(0));
		return null;
	}

	// <cmd> ::= '{' <cmd_inter> '}'
	private Object cmd(No no) {
		interpretar(no.getFilhos().get(1));
		return null;
	}

	// <list_cmd> ::= <cmd> <list_cmd> | &
	private Object listCmd(No no) {
		if (!no.getFilhos().isEmpty()) {
			interpretar(no.getFilhos().get(0));
			interpretar(no.getFilhos().get(1));
		}
		return null;
	}

	public void addErro(String erro) {
		erros.add("Erro de interpretação: " + erro + ". ");
	}

	public boolean temErros() {
		return !erros.isEmpty();
	}

	public void mostraErros() {
		for (String erro : erros) {
			System.out.println(erro);
		}
	}

	public No getRaiz() {
		return raiz;
	}

	public void setRaiz(No raiz) {
		this.raiz = raiz;
	}

	public List<Simbolo> getSimbolos() {
		return simbolos;
	}

	public void setSimbolos(List<Simbolo> simbolos) {
		this.simbolos = simbolos;
	}

	public List<String> getErros() {
		return erros;
	}

	public void setErros(List<String> erros) {
		this.erros = erros;
	}

	public Scanner getSc() {
		return sc;
	}

	public void setSc(Scanner sc) {
		this.sc = sc;
	}

}
