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
	private int indiceVar = 0;
	private File arqAsm;
	private PrintWriter printAsm;
	private List<Instrucao> instrucoes = new ArrayList<>();
	private int y = 0;
	private int[] verdadeAntes = new int[1000];
	private int pulaSenao = 0;
	private int[] falsoAntes = new int[1000];
	private int m = 0;
	private int pulaFimse = 0;

	public Gerador(No raiz, List<Simbolo> simbolos) {
		this.setRaiz(raiz);
		this.setSimbolos(simbolos);
	}

	public void gerar() throws IOException {
		arquivo = new File("saida.lvm");
		printWriter = new FileOutputStream(arquivo);

		arqAsm = new File("saida.lasm");
		printAsm = new PrintWriter(arqAsm);

		contaInstrucoes();

		magicWorld();

		gerar(raiz);

		imprimeTamInstrucoes();
		imprimeTamDados();
		imprimeInstrucoes();
		imprimeDados();

		// System.out.println("contInstrucoes = " + contaInstrucoes());

		printWriter.write(0xff);

		printAsm.flush();
		printAsm.close();

		printWriter.flush();
		printWriter.close();

//		System.out.println("TABELA DE SIMBOLOS");
//		for (int i = 0; i < simbolos.size(); i++) {
//			System.out.print("Imagem = " + simbolos.get(i).getImagem());
//			System.out.print(", Tipo = " + simbolos.get(i).getTipo());
//			System.out.print(", Valor = " + simbolos.get(i).getValor());
//			System.out.print(", TamVar = " + simbolos.get(i).getTamVar());
//			System.out.println(", IndiceVar = " + simbolos.get(i).getIndiceVar());
//		}

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
		} else if (no.getTipo().equals("NO_OP_LOG")) {
			return opLog(no);
		} else if (no.getTipo().equals("NO_SENAO")) {
			return senao(no);
		} else {
			addErro("Erro: NO desconhecido = " + no.getTipo());
			return null;
		}
	}

	// <senao> ::= '{' <list_cmd> '}' |
	private Object senao(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(1));
		}
		return null;
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

			Object sinal = gerar(no.getFilhos().get(0));

			//System.out.println("sinal = " + sinal);

			Token operan1 = (Token) gerar(no.getFilhos().get(1));
			//System.out.println("operan1 = " + operan1);

			Token operan2 = (Token) gerar(no.getFilhos().get(2));
			//System.out.println("operan2 = " + operan2);

			if (operan2.getClasse().equals("CLI")) {
				int numero = Integer.parseInt(operan2.getImagem().toString());
				printAsm.println("mov eax, " + retornaStringHex(numero) + "h");
				insereInstrucao(1, 1, 1, 1, numero, 4);
			}

			int numero = simbolos.get(operan1.getIndice()).getIndiceVar();
			printAsm.println("cmp eax, " + retornaStringHex(numero) + "h");
			insereInstrucao(5, 1, 1, 1, numero, 4);

			if (sinal.equals(">")) {
				printAsm.println("jump jle, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(6, 1, 4, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("<")) {
				printAsm.println("jump jge, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(6, 1, 3, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals(">=")) {
				printAsm.println("jump jl, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(6, 1, 2, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("<=")) {
				printAsm.println("jump jg, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(6, 1, 1, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("==")) {
				printAsm.println("jump jne, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(6, 1, 6, 1, indiceVar, 4);
				indiceVar += 4;
			} else if (sinal.equals("!=")) {
				printAsm.println("jump je, " + retornaStringHex(indiceVar) + "h");
				insereInstrucao(6, 1, 5, 1, indiceVar, 4);
				indiceVar += 4;
			}

		} else if (no.getFilhos().get(0).getTipo().equals("NO_OP_LOG")) {
			gerar(no.getFilhos().get(2));
			gerar(no.getFilhos().get(5));
		}

		return null;
	}

	// <exp_log> ::= '{' <exp_rel> '}'
	private Object expLog(No no) {
		return gerar(no.getFilhos().get(1));
	}

	// <cond> ::= 'se' <exp_log> '{' <list_cmd> '}' <senao>
	private Object cond(No no) {
		gerar(no.getFilhos().get(1));
		
		verdadeAntes[y] = contaInstrucoes();
		//System.out.println("verdadeAntes = " + verdadeAntes[y]);
		y++;
		
		Simbolo s = new Simbolo();
		s.setImagem(null);
		indiceVar -= 4;
		gerar(no.getFilhos().get(3));
		y--;
		//System.out.println("contaInstrucoes1 = " + contaInstrucoes());
		//System.out.println("verdadeAntes1 = " + verdadeAntes[y]);
		pulaSenao = contaInstrucoes() - verdadeAntes[y];
		//System.out.println("pulaSenao = " + pulaSenao);
		s.setIndiceVar(indiceVar);
		s.setTamVar(4);
		s.setTipo("int");
		s.setValor(pulaSenao+6);
		simbolos.add(s);
		
		indiceVar += 4;
		printAsm.println("jump fimse, " + retornaStringHex(indiceVar) + "h");
		insereInstrucao(6, 1, 7, 1, indiceVar, 4);
		
		//System.out.println("contaInstrucoes1 = " + contaInstrucoes());
		falsoAntes[m] = contaInstrucoes();
		m++;
		
		Simbolo s2 = new Simbolo();
		gerar(no.getFilhos().get(5));
		m--;
		pulaFimse = contaInstrucoes() - falsoAntes[m];
		//System.out.println("pulaFimse = " + pulaFimse);
		s2.setIndiceVar(indiceVar);
		s2.setTamVar(4);
		s2.setValor(pulaFimse);
		simbolos.add(s2);
		indiceVar += 4;

		return null;
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
					int numero = Integer.parseInt(valor2.getImagem().toString());
					printAsm.println("mov eax, " + retornaStringHex(numero) + "h");
					insereInstrucao(1, 1, 1, 1, numero, 4);
				} else if (valor2.getClasse().equals("ID")) {
					int numero = simbolos.get(valor2.getIndice()).getIndiceVar();
					printAsm.println("mov eax, [" + retornaStringHex(numero) + "h]");
					insereInstrucao(3, 1, 1, 1, numero, 4);
				}
			}

			Token valor1 = (Token) gerar(no.getFilhos().get(2));
			if (operador.equals("+")) {
				if (valor1.getClasse().equals("CLI")) {
					int numero = Integer.parseInt(valor1.getImagem().toString());
					printAsm.println("add eax, " + retornaStringHex(numero) + "h");
					insereInstrucao(8, 1, 1, 1, numero, 4);
				} else if (valor1.getClasse().equals("ID")) {
					int numero = simbolos.get(valor1.getIndice()).getIndiceVar();
					printAsm.println("add eax, [" + retornaStringHex(numero) + "h]");
					insereInstrucao(4, 1, 1, 1, numero, 4);
				}
			} else if (operador.equals("-")) {

			} else if (operador.equals("*")) {
				if (valor1.getClasse().equals("CLI")) {
					int numero = Integer.parseInt(valor1.getImagem().toString());
					printAsm.println("mult eax, " + retornaStringHex(numero) + "h");
					insereInstrucao(9, 1, 1, 1, numero, 4);
				} else if (valor1.getClasse().equals("ID")) {
					int numero = simbolos.get(valor1.getIndice()).getIndiceVar();
					printAsm.println("mult eax, [" + retornaStringHex(numero) + "h]");
					insereInstrucao(7, 1, 1, 1, numero, 4);
				}

			} else if (operador.equals("/")) {

			} else if (operador.equals(".")) {

			}

			return null;
		}

		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = no.getFilhos().get(1).getToken();
		Token valorExpArit = (Token) gerar(no.getFilhos().get(2));

		if (valorExpArit != null) {
			if (valorExpArit.getClasse().equals("CLI")) {
				int numero = Integer.parseInt(valorExpArit.getImagem().toString());
				printAsm.println("mov eax, " + retornaStringHex(numero) + "h");
				insereInstrucao(1, 1, 1, 1, numero, 4);
			} else if (valorExpArit.getClasse().equals("ID")) {
				int numero = simbolos.get(valorExpArit.getIndice()).getIndiceVar();
				printAsm.println("mov eax, [" + retornaStringHex(numero) + "h]");
				insereInstrucao(3, 1, 1, 1, numero, 4);
			}
		}

		int numero = simbolos.get(id.getIndice()).getIndiceVar();
		printAsm.println("mov [" + retornaStringHex(numero) + "h], eax");
		insereInstrucao(2, 1, 1, 1, numero, 4);

		return null;
	}

	// <list_id2> ::= <list_id> |
	private Object listId2(No no) {
		if (no.getFilhos().isEmpty()) {
			return new ArrayList<Token>();
		} else {
			return gerar(no.getFilhos().get(0));
		}
	}

	// <list_id> ::= id <list_id2>
	@SuppressWarnings("unchecked")
	private Object listId(No no) {
		Token id = no.getFilhos().get(0).getToken();
		List<Token> listId2 = (List<Token>) gerar(no.getFilhos().get(1));
		listId2.add(0, id);

		return listId2;
	}

	// <tipo> ::= 'int' | 'real' | 'texto' | 'logico'
	private Object tipo(No no) {
		return no.getFilhos().get(0).getToken().getImagem();
	}

	// <decl> ::= <tipo> <list_id>
	@SuppressWarnings("unchecked")
	private Object decl(No no) {
		List<Token> listId = (List<Token>) gerar(no.getFilhos().get(1));

		for (Token id : listId) {
			simbolos.get(id.getIndice()).setIndiceVar(indiceVar);
			printAsm.println("[" + retornaStringHex(indiceVar) + "h] ");
			indiceVar += 4;
			simbolos.get(id.getIndice()).setTamVar(4);
			
			String tipo = simbolos.get(id.getIndice()).getTipo();
			
			if(tipo.equals("int")){
				simbolos.get(id.getIndice()).setValor(0);
			}
		}

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

	private int contaInstrucoes() {
		int contaBytes = 0;
		for (int i = 0; i < instrucoes.size(); i++) {
			contaBytes += instrucoes.get(i).getTamByte1() + instrucoes.get(i).getTamByte2()
					+ instrucoes.get(i).getTamByte3();
		}
		return contaBytes;
	}

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
		for (int i = 0; i < simbolos.size(); i++) {
			contaBytes += simbolos.get(i).getTamVar();
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
		for (int i = 0; i < simbolos.size(); i++) {
			if (simbolos.get(i).getTamVar() == 4) {
				int valor = (int) simbolos.get(i).getValor();
				escreveHex(valor);
			}

		}
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
