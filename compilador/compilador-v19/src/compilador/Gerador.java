package compilador;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class Gerador {

	private No raiz;
	private List<Simbolo> simbolos;
	private List<String> erros = new ArrayList<String>();
	private FileOutputStream printWriter;
	private File arquivo;
	private File arqAsm;
	private PrintWriter printAsm;
	private List<Instrucao> instrucoes = new ArrayList<>();
	private int indiceVar = 0;
	private List<Dado> dados = new ArrayList<>();

	public Gerador(No raiz, List<Simbolo> simbolos) {
		this.setRaiz(raiz);
		this.setSimbolos(simbolos);
	}

	public void gerar() throws IOException {
		arquivo = new File("saida.lvm");
		printWriter = new FileOutputStream(arquivo);

		arqAsm = new File("saida.lasm");
		printAsm = new PrintWriter(arqAsm);

		magicWorld();

		gerar(raiz);

		imprimeTamInstrucoes();
		imprimeTamDados();
		imprimeInstrucoes();
		imprimeDados();

		printWriter.write(0xff);

		printAsm.flush();
		printAsm.close();

		printWriter.flush();
		printWriter.close();

		// System.out.println("\nTabela de Simbolos depois do gerador:");
		// for (int i = 0; i < simbolos.size(); i++) {
		// System.out.print(" " + i);
		// System.out.print(" " + simbolos.get(i).getImagem());
		// System.out.print(" " + simbolos.get(i).getEscopo());
		// System.out.print(" " + simbolos.get(i).getTipo());
		// System.out.print(" " + simbolos.get(i).getIndiceVar());
		// System.out.print(" " + simbolos.get(i).getValor());
		// System.out.print(" " + simbolos.get(i).getString());
		// System.out.println(" " + simbolos.get(i).getTamVar());
		// }
		//
		// System.out.println("\nTabela de dados:");
		// for (int i = 0; i < dados.size(); i++) {
		// System.out.print(" " + i);
		// System.out.print(" " + dados.get(i).getDado());
		// System.out.print(" " + dados.get(i).getIndiceDado());
		// System.out.println(" " + dados.get(i).getTamDado());
		// }
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
		} else if (no.getTipo().equals("NO_ESCRITA")) {
			return escrita(no);
		} else if (no.getTipo().equals("NO_EXP_ARIT")) {
			return expArit(no);
		} else if (no.getTipo().equals("NO_OPERAN")) {
			return operan(no);
		} else if (no.getTipo().equals("NO_OP_ARIT")) {
			return opArit(no);
		} else {
			addErro("Erro: NO desconhecido = " + no.getTipo());
			return null;
		}
	}

	// <op_arit> ::= '+' | '-' | '*' | '/' | '.'
	private Object opArit(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <operan> ::= id | cli | clr | cls
	private Object operan(No no) {
		return no.getFilhos().get(0).getToken();
	}

	// <exp_arit> ::= <operan> | '{' <op_arit> <exp_arit> <exp_arit> '}'
	private Object expArit(No no) {

		if (no.getFilhos().size() == 1) {
			return gerar(no.getFilhos().get(0));
		} else if (no.getFilhos().size() == 5) {

			Object operador = gerar(no.getFilhos().get(1));

			Token valor2 = (Token) gerar(no.getFilhos().get(3));

			if (valor2 != null) {
				if (valor2.getClasse().equals("CLI")) {

				} else if (valor2.getClasse().equals("ID")) {

				} else if (valor2.getClasse().equals("CLS")) {
					// System.out.println("valor2 = " + valor2);
					String string = valor2.getImagem();
					int tamDado = string.length() + 1 - contaBarras(string);

					Dado dd = new Dado();
					dd.setDado(string);
					dd.setIndiceDado(indiceVar);
					dd.setTamDado(tamDado);
					dd.setTipo("texto");
					dados.add(dd);

					printAsm.println("mov strControl, [" + retornaStringHex(indiceVar) + "h]");
					insereInstrucao(1, 1, 1, 1, indiceVar, 4);
					indiceVar += tamDado;
				}
			}

			Token valor1 = (Token) gerar(no.getFilhos().get(2));
			if (operador.equals("+")) {
				if (valor1.getClasse().equals("CLI")) {

				} else if (valor1.getClasse().equals("ID")) {

				}
			} else if (operador.equals("-")) {

			} else if (operador.equals("*")) {
				if (valor1.getClasse().equals("CLI")) {

				} else if (valor1.getClasse().equals("ID")) {

				}

			} else if (operador.equals("/")) {

			} else if (operador.equals(".")) {
				if (valor1.getClasse().equals("CLS")) {
					// System.out.println("valor1 = " + valor1);
					String string = valor1.getImagem();
					int tamDado = string.length() + 1 - contaBarras(string);

					Dado dd = new Dado();
					dd.setDado(string);
					dd.setIndiceDado(indiceVar);
					dd.setTamDado(tamDado);
					dd.setTipo("texto");
					dados.add(dd);

					printAsm.println("concat strControl, [" + retornaStringHex(indiceVar) + "h]");
					insereInstrucao(12, 1, 1, 1, indiceVar, 4);
					indiceVar += tamDado;
				}
			}

			return null;
		}

		printAsm.println();
		return null;
	}

	// <escrita> ::= 'mostra' <exp_arit>
	private Object escrita(No no) {

		Token token = (Token) gerar(no.getFilhos().get(1));

		if (token != null) {

			// System.out.println("Token = " + token);

			if (token.getClasse().equals("CLS")) {
				// System.out.println("token = " + token);
				String string = token.getImagem();
				int tamDado = string.length() + 1 - contaBarras(string);

				Dado dd = new Dado();
				dd.setDado(string);
				dd.setIndiceDado(indiceVar);
				dd.setTamDado(tamDado);
				dd.setTipo("texto");
				dados.add(dd);

				printAsm.println("mov strControl, [" + retornaStringHex(indiceVar) + "h]");
				insereInstrucao(1, 1, 1, 1, indiceVar, 4);
				indiceVar += tamDado;
			}
		}

		printAsm.println("mov [00000000h], strControl");
		insereInstrucao(2, 1, 1, 1, 0, 4);

		printAsm.println();
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
		erros.add("Erro no gerador binário: " + erro + ". ");
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

	// INICIO METODOS

	private void magicWorld() throws IOException {
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xbe);
	}

	private void imprimeTamInstrucoes() throws IOException {
		int contaBytes = 0;
		for (int i = 0; i < instrucoes.size(); i++) {
			contaBytes += instrucoes.get(i).getTamByte1() + instrucoes.get(i).getTamByte2()
					+ instrucoes.get(i).getTamByte3();
		}
		escreveHex(contaBytes);
	}

	private void escreveHex(int numero) throws IOException {
		int result1 = ((numero >> 24) & 0xFF);
		int result2 = ((numero >> 16) & 0xFF);
		int result3 = ((numero >> 8) & 0xFF);
		int result4 = ((numero) & 0xFF);
		printWriter.write(result1);
		printWriter.write(result2);
		printWriter.write(result3);
		printWriter.write(result4);

	}

	private void imprimeTamDados() throws IOException {
		int contaBytes = 0;
		for (int i = 0; i < dados.size(); i++) {
			contaBytes += dados.get(i).getTamDado();
			// System.out.println("dados = " + simbolos.get(i).getImagem());
		}
		escreveHex(contaBytes);
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

	private int contaBarras(String string) {
		int contBarra = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '\\') {
				i++;
				contBarra++;
			}
		}
		return contBarra;
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

	private void imprimeInstrucoes() throws IOException {
		for (int i = 0; i < instrucoes.size(); i++) {
			escreveInstrucao(instrucoes.get(i).getValor1(), instrucoes.get(i).getTamByte1());
			escreveInstrucao(instrucoes.get(i).getValor2(), instrucoes.get(i).getTamByte2());
			escreveInstrucao(instrucoes.get(i).getValor3(), instrucoes.get(i).getTamByte3());
		}
	}

	private void escreveInstrucao(int numero, int tamByte) throws IOException {
		if (tamByte == 1) {
			printWriter.write(numero);
		} else if (tamByte == 4) {
			escreveHex(numero);
		}
	}

	private void imprimeDados() throws IOException {
		for (int i = 0; i < dados.size(); i++) {
			String tipo = dados.get(i).getTipo();

			if (tipo.equals("int")) {

			} else if (tipo.equals("texto")) {

				String string = dados.get(i).getDado();

				for (int j = 0; j < string.length(); j++) {
					if (string.charAt(j) == '\\') {
						j++;
						if (string.charAt(j) == '\\') {
							printWriter.write(0x5c);
						} else if (string.charAt(j) == 'n') {
							printWriter.write(0x0a);
						} else if (string.charAt(j) == 't') {
							printWriter.write(0x09);
						}
					} else {
						printWriter.write(string.charAt(j));
					}
				}
				printWriter.write(0x0);
			}
		}
	}

}
