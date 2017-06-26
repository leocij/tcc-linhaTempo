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
	private int indiceVar;
	private File arqAsm;
	private PrintWriter printAsm;
	private List<Instrucao> instrucoes = new ArrayList<>();
	private int conta = 0;

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

		printWriter.flush();
		printWriter.close();

		printAsm.flush();
		printAsm.close();

		// imprimeSimbolos();
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
		} else if (no.getTipo().equals("NO_COND")) {
			return cond(no);
		} else if (no.getTipo().equals("NO_OP_REL")) {
			return opRel(no);
		} else if (no.getTipo().equals("NO_SENAO")) {
			return senao(no);
		} else if (no.getTipo().equals("NO_LEITURA")) {
			return leitura(no);
		} else if (no.getTipo().equals("NO_ESCRITA")) {
			return escrita(no);
		} else {
			addErro("NO desconhecido = " + no.getTipo());
			return null;
		}
	}

	// <escrita> ::= 'mostra' <exp_arit>
	@SuppressWarnings("unchecked")
	private Object escrita(No no) {

		List<Token> operandos = (List<Token>) gerar(no.getFilhos().get(1));

		gerar(no.getFilhos().get(1));

		for (Token operando : operandos) {
			// printWriter.write(" printf(");
			if (operando.getClasse().equals("ID")) {
				if (simbolos.get(operando.getIndice()).getTipo().equals("int")) {
					// printWriter.write(" printf(\"%d\", " +
					// operando.getImagem() + ");\n");
				} else if (simbolos.get(operando.getIndice()).getTipo().equals("real")) {
					// printWriter.write(" printf(\"%lf\", " +
					// operando.getImagem() + ");\n");
				} else if (simbolos.get(operando.getIndice()).getTipo().equals("texto")) {
					// printWriter.write(" printf(\"%s\", " +
					// operando.getImagem() + ");\n");
				}

			} else if (operando.getClasse().equals("CLI")) {
				// printWriter.write(" printf(\"%d\", " + operando.getImagem() +
				// ");\n");
			} else if (operando.getClasse().equals("CLR")) {
				// printWriter.write(" printf(\"%lf\", " + operando.getImagem()
				// + ");\n");
			} else if (operando.getClasse().equals("CLS")) {
				// printWriter.write(" printf(\"" + operando.getImagem() +
				// "\");\n");
			}
		}
		// printWriter.write("\n");

		return null;

	}

	// <leitura> ::= 'le' id
	private Object leitura(No no) {
		Token id = no.getFilhos().get(1).getToken();

		if (simbolos.get(id.getIndice()).getTipo().equals("int")) {
			// printWriter.write(" scanf(\"%d\", &" + id.getImagem());
		} else if (simbolos.get(id.getIndice()).getTipo().equals("real")) {
			// printWriter.write(" scanf(\"%lf\", &" + id.getImagem());
		} else if (simbolos.get(id.getIndice()).getTipo().equals("texto")) {
			// printWriter.write(" fgets(" + id.getImagem() + ", sizeof(" +
			// id.getImagem() + "), stdin");
			// printWriter.write(" scanf(\"%s\", " + id.getImagem());
		}

		return null;
	}

	// <senao> ::= '{' <list_cmd> '}' | &
	private Object senao(No no) {
		if (!no.getFilhos().isEmpty()) {
			gerar(no.getFilhos().get(1));
		}
		return null;
	}

	// <cond> ::= 'se' <exp_log> '{' <list_cmd> '}' <senao>
	@SuppressWarnings("unchecked")
	private Object cond(No no) {
		printAsm.write("  if(");
		List<Token> operandos = (List<Token>) gerar(no.getFilhos().get(1));

		for (Token operando : operandos) {
			printAsm.write((" " + operando.getImagem()));
		}
		
		printAsm.write(" ){\n  ");
		conta+=6;
		System.out.println(" Conta = " + conta);
		gerar(no.getFilhos().get(3));
		
		printAsm.write("  }else{\n  ");
		gerar(no.getFilhos().get(5));
		printAsm.write("  }\n\n");
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

	// <laco> ::= 'enquanto' <exp_log> <list_cmd>
	private Object laco(No no) {
		return null;
	}

	// <operan> ::= id | cli | clr | cls
	private Object operan(No no) {
		return no.getFilhos().get(0).getToken();
	}

	// <op_arit> ::= '+' | '-' | '*' | '/' | '.'
	private Object opArit(No no) {
		return no.getFilhos().get(0).getToken();
	}

	// <exp_arit> ::= <operan> | '{' <op_arit> <exp_arit> <exp_arit> '}'
	private Object expArit(No no) {
		if (no.getFilhos().size() == 1) {
			return gerar(no.getFilhos().get(0));
		} else if (no.getFilhos().size() == 5) {

			Token valor2 = (Token) gerar(no.getFilhos().get(3));

			if (valor2 != null) {
				// System.out.println("Valor2 = " + valor2);

				if (valor2.getClasse().equals("CLI")) {
					int numero = Integer.parseInt(valor2.getImagem().toString());
					// System.out.println("mov eax, " +
					// retornaStringHex(numero));
					printAsm.println("mov eax, " + retornaStringHex(numero) + "h");
					insereInstrucao(1, 1, 1, 1, numero, 4);
				} else if (valor2.getClasse().equals("ID")) {
					int numero = simbolos.get(valor2.getIndice()).getIndiceVariavel();
					// System.out.println("mov eax, [" +
					// retornaStringHex(numero) +
					// "h]");
					printAsm.println("mov eax, [" + retornaStringHex(numero) + "h]");
					insereInstrucao(3, 1, 1, 1, numero, 4);
				}
			} else {
				// System.out.println("NULL - Valor2 = " + valor2);
			}

			Token sinal = (Token) gerar(no.getFilhos().get(1));
			// System.out.println("sinal = " + sinal);

			Token valor1 = (Token) gerar(no.getFilhos().get(2));
			// System.out.println("valor1 = " + valor1);

			if (sinal.getImagem().equals("+")) {
				if (valor1.getClasse().equals("CLI")) {
					int numero = Integer.parseInt(valor1.getImagem().toString());
					printAsm.println("add eax, " + retornaStringHex(numero) + "h");
					insereInstrucao(8, 1, 1, 1, numero, 4);
				} else if (valor1.getClasse().equals("ID")) {
					int numero = simbolos.get(valor1.getIndice()).getIndiceVariavel();
					// System.out.println("add eax, [" +
					// retornaStringHex(numero) + "h]");
					printAsm.println("add eax, [" + retornaStringHex(numero) + "h]");
					insereInstrucao(4, 1, 1, 1, numero, 4);
				}
			}

		}
		return null;
	}

	// <atrib> ::= '=' id <exp_arit>
	private Object atrib(No no) {
		Token id = no.getFilhos().get(1).getToken();
		Token op = (Token) gerar(no.getFilhos().get(2));

		if (op != null) {
			if (op.getClasse().equals("CLI")) {
				int numero = Integer.parseInt(op.getImagem().toString());
				printAsm.println("mov eax, " + retornaStringHex(numero) + "h");
				insereInstrucao(1, 1, 1, 1, numero, 4);

				int numero2 = simbolos.get(id.getIndice()).getIndiceVariavel();
				printAsm.println("mov [" + retornaStringHex(numero2) + "h], eax");
				insereInstrucao(2, 1, 1, 1, numero2, 4);

			} else if (op.getClasse().equals("ID")) {
				int numero = simbolos.get(op.getIndice()).getIndiceVariavel();
				printAsm.println("mov eax, [" + retornaStringHex(numero) + "h]");
				insereInstrucao(3, 1, 1, 1, numero, 4);

				int numero2 = simbolos.get(id.getIndice()).getIndiceVariavel();
				printAsm.println("mov [" + retornaStringHex(numero2) + "h], eax");
				insereInstrucao(2, 1, 1, 1, numero2, 4);
			}
		} else {
			int numero = simbolos.get(id.getIndice()).getIndiceVariavel();
			// System.out.println("mov [" + retornaStringHex(numero) + "h],
			// eax");
			printAsm.println("mov [" + retornaStringHex(numero) + "h], eax");
			insereInstrucao(2, 1, 1, 1, numero, 4);
		}

		printAsm.println();
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
			// System.out.println("id = " + id);
			// System.out.println("tipo = " + tipo);

			simbolos.get(id.getIndice()).setIndiceVariavel(indiceVar);
			printAsm.println("[" + retornaStringHex(indiceVar) + "h] ");
			indiceVar += 4;
			simbolos.get(id.getIndice()).setTamVariavel(4);

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
		printAsm.println();
		// System.out.println();
		// System.out.println();
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

	public FileOutputStream getPrintWriter() {
		return printWriter;
	}

	public void setPrintWriter(FileOutputStream printWriter) {
		this.printWriter = printWriter;
	}

	public File getArquivo() {
		return arquivo;
	}

	public void setArquivo(File arquivo) {
		this.arquivo = arquivo;
	}

	// Inicio Metodos

	private void magicWorld() throws IOException {
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xba);
		printWriter.write(0xbe);
	}

	// private void imprimeSimbolos() {
	// System.out.println("TABELA DE SIMBOLOS");
	// for (int i = 0; i < simbolos.size(); i++) {
	// System.out.print("Imagem = " + simbolos.get(i).getImagem());
	// System.out.print(", Tipo = " + simbolos.get(i).getTipo());
	// System.out.print(", Valor = " + simbolos.get(i).getValor());
	// System.out.println(", IndiceMem = " +
	// simbolos.get(i).getIndiceVariavel());
	// }
	// }

	private void imprimeTamDados() throws IOException {
		int contaBytes = 0;
		for (int i = 0; i < simbolos.size(); i++) {
			contaBytes += simbolos.get(i).getTamVariavel();
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

		// System.out.println("r1 = " + result1 + ", r2 = " + result2 + ", r3 =
		// " + result3 + ", r4 = " + result4);
	}

	private void imprimeDados() throws IOException {
		for (int i = 0; i < simbolos.size(); i++) {
			if (simbolos.get(i).getTamVariavel() == 4) {
				escreveHex(0);
			}

		}
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

	private void imprimeTamInstrucoes() throws IOException {
		int contaBytes = 0;
		for (int i = 0; i < instrucoes.size(); i++) {
			contaBytes += instrucoes.get(i).getTamByte1() + instrucoes.get(i).getTamByte2()
					+ instrucoes.get(i).getTamByte3();
		}
		escreveHex(contaBytes);
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

}
