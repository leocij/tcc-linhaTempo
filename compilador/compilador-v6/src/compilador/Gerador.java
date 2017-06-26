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
	private int contaMem = 0;
	private int tamTexto = 10;

	public Gerador(No raiz, List<Simbolo> simbolos) {
		this.setRaiz(raiz);
		this.setSimbolos(simbolos);
	}

	public void gerar() throws FileNotFoundException {
		arquivo = new File("saida.lvm");
		printWriter = new PrintWriter(arquivo);

		escrevePalavraChave();

		gerar(raiz);

		tamInstrucoes();
		tamDados();
		imprimeInstrucoes();
		imprimeDados();

		printWriter.write(0xff);
		printWriter.flush();
		printWriter.close();

		imprimeTabelaSimbolos();
		System.out.println("\n");
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
		} else if (no.getTipo().equals("NO_ESCRITA")) {
			return escrita(no);
		} else {
			addErro("NO desconhecido = " + no.getTipo());
			return null;
		}
	}

	// <escrita> ::= 'mostra' <exp_arit>
	private Object escrita(No no) {
		Token token = (Token) gerar(no.getFilhos().get(1));
		System.out.println("Token Escrita = " + token);
		
		if (token.getClasse().equals("CLS")) {
			String string = token.getImagem();
			System.out.println("String Escrita = " + string);

			System.out.println("MOV SAIDA, [" + retornaStringHex(contaMem) + "]");
			insereInstrucao(1, 1, 1, 1, contaMem, 4);
			contaMem += tamTexto - contaBarra(string);
		}
		
		return null;
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

		}
		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = no.getFilhos().get(1).getToken();
		Token operando = (Token) gerar(no.getFilhos().get(2));

		System.out.println("ID = " + id);
		System.out.println("OPERANDO = " + operando);

		if (operando != null) {
			if (operando.getClasse().equals("CLI")) {
				int numero = Integer.parseInt(operando.getImagem().toString());
				simbolos.get(id.getIndice()).setValor(numero);
			} else if (operando.getClasse().equals("ID")) {

			} else if (operando.getClasse().equals("CLS")) {
				String string = operando.getImagem();
				simbolos.get(id.getIndice()).setValor(string);
			}
		} else {
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
		Token id = (Token) no.getFilhos().get(0).getToken();
		String tipo = (String) simbolos.get(id.getIndice()).getTipo();

		// System.out.println("ID = " + id);

		if (tipo.equals("int")) {
			simbolos.get(id.getIndice()).setIniMemoria(contaMem);
			contaMem += 4;
			simbolos.get(id.getIndice()).setTamMemoria(4);
			simbolos.get(id.getIndice()).setValor(0);
		} else if (tipo.equals("texto")) {
			simbolos.get(id.getIndice()).setIniMemoria(contaMem);
			contaMem += tamTexto;
			simbolos.get(id.getIndice()).setTamMemoria(tamTexto);
			simbolos.get(id.getIndice()).setValor("\0");
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

	private void imprimeDados() {
		for (int i = 0; i < simbolos.size(); i++) {
			if (simbolos.get(i).getConstante().equals("CLS")) {
				// String string = simbolos.get(i).getImagem();
				// imprimeString(string);
			} else if (simbolos.get(i).getConstante().equals("ID")) {
				if (simbolos.get(i).getTipo().equals("int")) {
					int numero = Integer.parseInt(simbolos.get(i).getValor().toString());
					escreveHex(numero);
				} else if (simbolos.get(i).getTipo().equals("texto")) {
					String string = simbolos.get(i).getValor().toString();

					imprimeString(string);
				}
			}

		}
	}

	private void imprimeString(String string) {

		System.out.println("String outro = " + string);

		int tamStr = string.length();
		System.out.println("tamStr = " + tamStr);

		int i = 0;
		while (i < tamTexto - 1) {
			if (i < tamStr) {
				if (string.charAt(i) == '\\') {
					if (string.charAt(i + 1) == 'n') {
						i++;
						tamStr--;
						printWriter.write(0x0a);
					} else if (string.charAt(i + 1) == 't') {
						i++;
						tamStr--;
						printWriter.write(0x09);
					}
				} else {
					printWriter.write(string.charAt(i));
				}
			} else {
				printWriter.write(0x00);
			}
			i++;
		}
		printWriter.write(0x00);
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

	private void tamDados() {
		int contaBytes = 0;
		for (int i = 0; i < simbolos.size(); i++) {
			contaBytes += simbolos.get(i).getTamMemoria();
		}
		escreveHex(contaBytes);
	}

	private void tamInstrucoes() {
		int contaBytes = 0;
		for (int i = 0; i < instrucoes.size(); i++) {
			contaBytes += instrucoes.get(i).getTamByte1() + instrucoes.get(i).getTamByte2()
					+ instrucoes.get(i).getTamByte3();
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

	private void imprimeTabelaSimbolos() {
		System.out.println("\nTabela de Simbolos depois de Gerar:");
		for (int i = 0; i < simbolos.size(); i++) {
			System.out.print(" " + i);
			System.out.print(" " + simbolos.get(i).getImagem());
			System.out.print(" " + simbolos.get(i).getTipo());
			System.out.print(" " + simbolos.get(i).getValor());
			System.out.print(" " + simbolos.get(i).getIniMemoria());
			System.out.print(" " + simbolos.get(i).getTamMemoria());
			System.out.println(" " + simbolos.get(i).getConstante());
		}
	}

	private void escrevePalavraChave() {
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xbe);
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
	
	private int contaBarra(String str) {
		int contBarra = 0;
		

		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == '\\') {
				if (str.charAt(i + 1) == '\\') {
					i++;
				} else {
					contBarra++;
				}
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

}
