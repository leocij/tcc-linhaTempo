package compilador;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Gerador {

	private No raiz;
	private List<Simbolo> simbolos;
	private List<String> erros = new ArrayList<String>();
	private PrintWriter printWriter;
	private File arquivo;
	private List<Instrucao> instrucoes = new ArrayList<>();
	private int inicioDado = 0;

	public Gerador(No raiz, List<Simbolo> simbolos) {
		this.setRaiz(raiz);
		this.setSimbolos(simbolos);
	}

	public void gerar() throws FileNotFoundException {
		arquivo = new File("saida.lvm");
		printWriter = new PrintWriter(arquivo);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xbe);
		gerar(raiz);
		tamInstrucoes();
		tamDados();
		imprimeInstrucoes();
		escreveDados();
		printWriter.write(0xff);
		printWriter.flush();
		printWriter.close();

		imprimeSimbolos();
	}

	private Object gerar(No no) {
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
		} else if (no.getTipo().equals("NO_OPERAN")) {
			return operan(no);
		} else if (no.getTipo().equals("NO_OP_ARIT")) {
			return opArit(no);
		} else if (no.getTipo().equals("NO_COND")) {
			return cond(no);
		} else if (no.getTipo().equals("NO_EXP_LOG")) {
			return expLog(no);
		} else if (no.getTipo().equals("NO_EXP_REL")) {
			return expRel(no);
		} else if (no.getTipo().equals("NO_OP_REL")) {
			return opRel(no);
		} else if (no.getTipo().equals("NO_SENAO")) {
			return senao(no);
		} else {
			addErro("No desconhecido = " + no.getTipo());
			return null;
		}
	}

	// <senao> ::= '{' <list_cmd> '}' | &
	private Object senao(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(1));
		}
		return null;
	}

	// <op_rel> ::= '>' | '<' | '>=' | '<=' | '==' | '!='
	private Object opRel(No no) {
		return no.getFilhos().get(0).getToken();
	}

	// <exp_rel> ::= <op_rel> <operan> <operan> | <op_log> '{' <exp_rel> '}' '{'
	// <exp_rel> '}'
	private Object expRel(No no) {

		if (no.getFilhos().size() == 3) {
			Token operan1 = (Token) gerar(no.getFilhos().get(1));
			Token operRel = (Token) gerar(no.getFilhos().get(0));
			Token operan2 = (Token) gerar(no.getFilhos().get(2));

			List<Token> listOperans = new ArrayList<>();

			listOperans.add(operan1);
			listOperans.add(operRel);
			listOperans.add(operan2);
			return listOperans;
		} else if (no.getFilhos().size() == 7) {

		}
		return null;
	}

	// <exp_log> ::= '{' <exp_rel> '}'
	@SuppressWarnings("unchecked")
	private Object expLog(No no) {
		List<Token> listExpLog = (List<Token>) gerar(no.getFilhos().get(1));
		return listExpLog;
	}

	// <cond> ::= 'se' <exp_log> '{' <list_cmd> '}' <senao>
	@SuppressWarnings("unchecked")
	private Object cond(No no) {
		List<Token> operandos = (List<Token>) gerar(no.getFilhos().get(1));

		for (Token operando : operandos) {
			System.out.println(operando);
		}

		gerar(no.getFilhos().get(3));

		gerar(no.getFilhos().get(5));

		return null;
	}

	// <op_arit> ::= '+' | '-' | '*' | '/' | '.'
	private Object opArit(No no) {
		// System.out.println("opArit = " +
		// no.getFilhos().get(0).getToken().getImagem());
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <operan> ::= id | cli | clr | cls
	private Object operan(No no) {
		return (Token) no.getFilhos().get(0).getToken();
	}

	// <exp_arit> ::= <operan> | '{' <op_arit> <exp_arit> <exp_arit> '}'
	private Object expArit(No no) {

		if (no.getFilhos().size() == 1) {
			return gerar(no.getFilhos().get(0));
		} else if (no.getFilhos().size() == 5) {

			Token valor2 = (Token) gerar(no.getFilhos().get(3));

			if (valor2 != null) {
				if (valor2.getClasse().equals("ID")) {
					int inicio = simbolos.get(valor2.getIndice()).getInicio();
					System.out.println("mov rInt, [" + retornaStringHex(inicio) + "]");
					insereInstrucao(1, 1, 1, 1, inicio, 4);
				} else if (valor2.getClasse().equals("CLI")) {

				}
			}

			Object operador = gerar(no.getFilhos().get(1));

			Token valor1 = (Token) gerar(no.getFilhos().get(2));
			if (operador.equals("+")) {
				if (valor1.getClasse().equals("ID")) {
					int inicio = simbolos.get(valor1.getIndice()).getInicio();
					System.out.println("add rInt, [" + retornaStringHex(inicio) + "]");
					insereInstrucao(2, 1, 1, 1, inicio, 4);
				} else if (valor1.getClasse().equals("CLI")) {

				}

			}

		}

		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = no.getFilhos().get(1).getToken();
		Token operando = (Token) gerar(no.getFilhos().get(2));

		// System.out.println("id = " + id);
		// System.out.println("operando = " + operando);

		if (operando != null) {
			if (operando.getClasse().equals("CLI")) {
				simbolos.get(id.getIndice()).setValor(operando.getImagem());
			} else if (operando.getClasse().equals("ID")) {

			}
		} else {
			int inicio = simbolos.get(id.getIndice()).getInicio();
			System.out.println("mov [" + retornaStringHex(inicio) + "], rInt");
			insereInstrucao(3, 1, inicio, 4, 1, 1);
		}

		return null;
	}

	// <list_id2> ::= <list_id> | &
	private Object listId2(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(0));
		}
		return null;
	}

	// <list_id> ::= id <list_id2>
	private Object listId(No no) {
		Token id = (Token) no.getFilhos().get(0).getToken();
		String tipo = (String) simbolos.get(id.getIndice()).getTipo();
		if (tipo.equals("int")) {
			simbolos.get(id.getIndice()).setValor("0");
			simbolos.get(id.getIndice()).setInicio(inicioDado);
			simbolos.get(id.getIndice()).setTamMemoria(4);
			inicioDado += 4;
		} else if (tipo.equals("real")) {
			// printWriter.write(" " + id.getImagem() + " = 0.0");
		} else if (tipo.equals("texto")) {
			// printWriter.write(" " + id.getImagem() + "[1000]");
		}
		gerar(no.getFilhos().get(1));
		return null;
	}

	// <tipo> ::= 'int' | 'real' | 'texto' | 'logico'
	private Object tipo(No no) {
		gerar(no.getFilhos().get(0));
		return null;
	}

	// <decl> ::= <tipo> <list_id>
	private Object decl(No no) {
		gerar(no.getFilhos().get(0));
		gerar(no.getFilhos().get(1));
		return null;
	}

	// <cmd_inter> ::= <decl> | <atrib> | <laco> | <cond> | <escrita> |
	// <leitura>
	private Object cmdInter(No no) {
		gerar(no.getFilhos().get(0));
		return null;
	}

	// <cmd> ::= '{' <cmd_inter> '}'
	private Object cmd(No no) {
		gerar(no.getFilhos().get(1));
		return null;
	}

	// <list_cmd> ::= <cmd> <list_cmd> | &
	private Object listCmd(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(0));
			gerar(no.getFilhos().get(1));
		}
		return null;
	}

	// FIM DAS REGRAS

	public void addErro(String erro) {
		erros.add("Erro de geração: " + erro + ". ");
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

	// INICIO METODOS

	private void imprimeSimbolos() {
		System.out.println("IMAGEM TIPO INICIO VALOR");
		for (int i = 0; i < simbolos.size(); i++) {
			System.out.print(" " + simbolos.get(i).getImagem());
			System.out.print(" " + simbolos.get(i).getTipo());
			System.out.print(" " + simbolos.get(i).getInicio());
			System.out.println(" " + simbolos.get(i).getValor());
		}
	}

	private void escreveDados() {
		for (int i = 0; i < simbolos.size(); i++) {
			int numero = Integer.parseInt(simbolos.get(i).getValor().toString());

			escreveHex(numero);
		}
	}

	private void escreveHex(int numero) {
		int result1 = ((numero >> 24) & 0xFF);
		int result2 = ((numero >> 16) & 0xFF);
		int result3 = ((numero >> 8) & 0xFF);
		int result4 = ((numero) & 0xFF);
		printWriter.write(result1);
		printWriter.write(result2);
		printWriter.write(result3);
		printWriter.write(result4);
	}

	private String retornaStringHex(int numero) {
		int result1 = ((numero >> 24) & 0xFF);
		int result2 = ((numero >> 16) & 0xFF);
		int result3 = ((numero >> 8) & 0xFF);
		int result4 = ((numero) & 0xFF);

		String string = "" + String.format("%02X", result1) + String.format("%02X", result2)
				+ String.format("%02X", result3) + String.format("%02X", result4);

		return string;
	}

	private void insereInstrucao(int valor1, int tamByte1, int valor2, int tamByte2, int valor3, int tamByte3) {
		Instrucao instrucao = new Instrucao();
		instrucao.setValor1(valor1);
		instrucao.setTamByte1(tamByte1);
		instrucao.setValor2(valor2);
		instrucao.setTamByte2(tamByte2);
		instrucao.setValor3(valor3);
		instrucao.setTamByte3(tamByte3);
		instrucoes.add(instrucao);
	}

	private void imprimeInstrucoes() {
		for (int i = 0; i < instrucoes.size(); i++) {
			escreveInstrucao(instrucoes.get(i).getValor1(), instrucoes.get(i).getTamByte1());
			escreveInstrucao(instrucoes.get(i).getValor2(), instrucoes.get(i).getTamByte2());
			escreveInstrucao(instrucoes.get(i).getValor3(), instrucoes.get(i).getTamByte3());
		}
	}

	private void escreveInstrucao(int numero, int tamByte) {
		if (tamByte == 1) {
			printWriter.write(numero);
		} else if (tamByte == 4) {
			escreveHex(numero);
		}
	}

	private void tamInstrucoes() {
		int contaBytes = 0;
		for (int i = 0; i < instrucoes.size(); i++) {
			contaBytes += instrucoes.get(i).getTamByte1() + instrucoes.get(i).getTamByte2()
					+ instrucoes.get(i).getTamByte3();
		}
		escreveHex(contaBytes);
	}

	private void tamDados() {
		int contaBytes = 0;
		for (int i = 0; i < simbolos.size(); i++) {
			contaBytes += simbolos.get(i).getTamMemoria();
		}
		escreveHex(contaBytes);
	}

}
