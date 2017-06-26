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
	// private List<Memoria> enderecos = new ArrayList<>();
	private int contaMem = 0;
	private int tamTexto = 5;

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
		tamInstrucoes(instrucoes);
		tamDados(simbolos);
		imprimeInstrucoes(instrucoes);
		imprimeDados(simbolos);

		printWriter.write(0xff);
		printWriter.flush();
		printWriter.close();

		System.out.println("\nTabela de Simbolos depois de Gerar:");
		for (int i = 0; i < simbolos.size(); i++) {
			System.out.print(" " + i);
			System.out.print(" " + simbolos.get(i).getImagem());
			System.out.print(" " + simbolos.get(i).getTipo());
			System.out.print(" " + simbolos.get(i).getValor());
			System.out.print(" " + simbolos.get(i).getIniMemoria());
			System.out.println(" " + simbolos.get(i).getTamMemoria());
		}
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
		} else if (no.getTipo().equals("NO_ESCRITA")) {
			return escrita(no);
		} else if (no.getTipo().equals("NO_LEITURA")) {
			return leitura(no);
		} else {
			addErro("No desconhecido = " + no.getTipo());
			return null;
		}

	}

	// <leitura> ::= 'le' id
	private Object leitura(No no) {
		Token token = no.getFilhos().get(1).getToken();

		String tipo = simbolos.get(token.getIndice()).getTipo();

		if (tipo.equals("int")) {
			int iniString = simbolos.get(token.getIndice()).getIniMemoria();
			System.out.println("MOV [" + retornaStringHex(iniString) + "], ENTRINT");
			insereInstrucao(3, 1, iniString, 4, 2, 1);
		} else if (tipo.equals("real")) {

		} else if (tipo.equals("texto")) {

		}

		return null;
	}

	// <escrita> ::= 'mostra' <exp_arit>
	private Object escrita(No no) {
		Token token = (Token) gerar(no.getFilhos().get(1));
		System.out.println("Token = " + token);

		if (token.getClasse().equals("CLS")) {

			int contBarra = 0;
			String str = token.getImagem();

			for (int i = 0; i < str.length(); i++) {
				if (str.charAt(i) == '\\') {
					if (str.charAt(i + 1) == '\\') {
						i++;
					} else {
						contBarra++;
					}
				}
			}

			Simbolo simb = new Simbolo();
			simb.setImagem(str);
			System.out.println("contaMem = " + contaMem);
			simb.setIniMemoria(contaMem);
			contaMem += tamTexto - contBarra;
			simb.setTamMemoria(tamTexto);
			simb.setTipo("texto");
			simb.setValor(str);
			simbolos.add(simb);

			int iniString = simb.getIniMemoria();
			System.out.println("MOV SAIDA, [" + retornaStringHex(iniString) + "]");
			insereInstrucao(1, 1, 1, 1, iniString, 4);

		} else if (token.getClasse().equals("ID")) {
			String tipo = simbolos.get(token.getIndice()).getTipo();

			if (tipo.equals("int")) {
				int iniString = simbolos.get(token.getIndice()).getIniMemoria();
				System.out.println("MOV SAIDA, [" + retornaStringHex(iniString) + "]");
				insereInstrucao(2, 1, 1, 1, iniString, 4);
			} else if (tipo.equals("texto")) {
				// String string =
				// simbolos.get(token.getIndice()).getValor().toString();
				int iniString = simbolos.get(token.getIndice()).getIniMemoria();
				System.out.println("MOV SAIDA, [" + retornaStringHex(iniString) + "]");
				insereInstrucao(1, 1, 1, 1, iniString, 4);
			}
		}

		return null;
	}

	// <op_arit> ::= '+' | '-' | '*' | '/' | '.'
	private Object opArit(No no) {
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
					// System.out.println("0101" + String.format("%08X",
					// simbolos.get(valor2.getIndice()).getMemoria()));
					// System.out.println("ID - MOV EAX, " +
					// valor2.getImagem());
					// insertInstruction(1, 1, 1, 1,
					// simbolos.get(valor2.getIndice()).getMemoria(), 4);
				} else if (valor2.getClasse().equals("CLI")) {
					// System.out.println("0401" + String.format("%08x",
					// Integer.parseInt(valor2.getImagem())));
					// System.out.println("CLI - MOV EAX, " +
					// valor2.getImagem());
					// insertInstruction(4, 1, 1, 1,
					// Integer.parseInt(valor2.getImagem()), 4);
				}
			}

			Object operador = gerar(no.getFilhos().get(1));

			Token valor1 = (Token) gerar(no.getFilhos().get(2));
			if (operador.equals("+")) {
				if (valor1.getClasse().equals("ID")) {
					// System.out.println("0201" + String.format("%08X",
					// simbolos.get(valor1.getIndice()).getMemoria()));
					// System.out.println("ID - ADD EAX, " +
					// valor1.getImagem());
					// insertInstruction(2, 1, 1, 1,
					// simbolos.get(valor1.getIndice()).getMemoria(), 4);

				} else if (valor1.getClasse().equals("CLI")) {
					// System.out.println("0701" + String.format("%08x",
					// Integer.parseInt(valor1.getImagem())));
					// System.out.println("CLI - ADD EAX, " +
					// valor1.getImagem());
					// insertInstruction(7, 1, 1, 1,
					// Integer.parseInt(valor1.getImagem()), 4);
				}

			}

		}

		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = no.getFilhos().get(1).getToken();
		Token operando = (Token) gerar(no.getFilhos().get(2));

		if (operando != null) {
			if (operando.getClasse().equals("CLI")) {

				simbolos.get(id.getIndice()).setValor(operando.getImagem());

				// int numOper = Integer.parseInt(operando.getImagem());
				// System.out.println("MOV LINT, [" + retornaStringHex(numOper)
				// + "]");
				// simbolos.get(id.getIndice()).setValor(operando.getImagem());
				// insereInstrucao(1, 1, 1, 1, numOper, 4);
				//
				// int numId = simbolos.get(id.getIndice()).getIniMemoria();
				// System.out.println("MOV [" + retornaStringHex(numId) + "],
				// LINT");
				// insereInstrucao(2, 1, numId, 4, 1, 1);

			} else if (operando.getClasse().equals("ID")) {

				// System.out.println("OPER - MOV EAX, " +
				// operando.getImagem());
				// insertInstruction(1, 1, 1, 1,
				// simbolos.get(operando.getIndice()).getMemoria(), 4);
				//
				// System.out.println("ID - MOV " + id.getImagem() + ", EAX");
				// insertInstruction(3, 1,
				// simbolos.get(id.getIndice()).getMemoria(), 4, 1, 1);
			} else if (operando.getClasse().equals("CLS")) {
				simbolos.get(id.getIndice()).setValor(operando.getImagem());
			}
		} else {
			// System.out.println("MOV " + id.getImagem() + ", EAX");
			// insertInstruction(3, 1,
			// simbolos.get(id.getIndice()).getMemoria(), 4, 1, 1);
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

		// Memoria endereco = new Memoria();

		Token id = (Token) no.getFilhos().get(0).getToken();
		String tipo = (String) simbolos.get(id.getIndice()).getTipo();

		if (tipo.equals("int")) {
			simbolos.get(id.getIndice()).setIniMemoria(contaMem);
			contaMem += 4;
			simbolos.get(id.getIndice()).setTamMemoria(4);
			simbolos.get(id.getIndice()).setValor(0);
		} else if (tipo.equals("real")) {
			// printWriter.write(" " + id.getImagem() + " = 0.0");
		} else if (tipo.equals("texto")) {
			simbolos.get(id.getIndice()).setIniMemoria(contaMem);
			contaMem += tamTexto;
			simbolos.get(id.getIndice()).setTamMemoria(tamTexto);
			simbolos.get(id.getIndice()).setValor("\0");
		}
		// enderecos.add(endereco);
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

	public List<String> getErros() {
		return erros;
	}

	public void setErros(List<String> erros) {
		this.erros = erros;
	}

	public PrintWriter getPrintWriter() {
		return printWriter;
	}

	public void setPrintWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}

	public File getArquivo() {
		return arquivo;
	}

	public void setArquivo(File arquivo) {
		this.arquivo = arquivo;
	}

	// INICIO DOS METODOS

	private void tamInstrucoes(List<Instrucao> inst) {
		int contaBytes = 0;
		for (int i = 0; i < inst.size(); i++) {
			contaBytes += inst.get(i).getTamByte1() + inst.get(i).getTamByte2() + inst.get(i).getTamByte3();
		}
		escreveHex(contaBytes);
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

	private void tamDados(List<Simbolo> simbolos2) {
		int contaBytes = 0;
		for (int i = 0; i < simbolos2.size(); i++) {
			contaBytes += simbolos2.get(i).getTamMemoria();
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

	private void imprimeInstrucoes(List<Instrucao> instrucoes2) {
		for (int i = 0; i < instrucoes2.size(); i++) {
			escreveInstrucao(instrucoes2.get(i).getValor1(), instrucoes2.get(i).getTamByte1());
			escreveInstrucao(instrucoes2.get(i).getValor2(), instrucoes2.get(i).getTamByte2());
			escreveInstrucao(instrucoes2.get(i).getValor3(), instrucoes2.get(i).getTamByte3());
		}
	}

	private void escreveInstrucao(int numero, int tamByte) {
		if (tamByte == 1) {
			printWriter.write(numero);
		} else if (tamByte == 4) {
			escreveHex(numero);
		}
	}

	private void imprimeDados(List<Simbolo> simbolos2) {
		int numero = 0;

		for (int i = 0; i < simbolos2.size(); i++) {

			if (simbolos2.get(i).getTipo().equals("int")) {
				numero = Integer.parseInt(simbolos2.get(i).getValor().toString());
				escreveHex(numero);
			} else if (simbolos2.get(i).getTipo().equals("texto")) {

				int tamString = simbolos2.get(i).getTamMemoria();

				// escreveHex(tamString);

				String string = simbolos2.get(i).getValor().toString();

				System.out.println("String = " + string);
				int j = 0;
				int k = 0;
				for (; j < string.length(); j++) {
					if (string.charAt(j) == '\\') {
						j++;
						k--;
						if (string.charAt(j) == 'n') {
							printWriter.write(0x0a);
						} else if (string.charAt(j) == 't') {
							printWriter.write(0x09);
						} /*
							 * else if (string.charAt(j) == '0') {
							 * printWriter.write(0x00); }
							 */
					} else {
						printWriter.write(string.charAt(j));
					}

				}

				printWriter.write(0x00);
				j++;

				for (k += j; k < tamString; k++) {
					printWriter.write(0x00);
				}
			}
		}
	}

}
