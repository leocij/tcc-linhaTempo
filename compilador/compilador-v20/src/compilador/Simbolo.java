package compilador;

public class Simbolo {
	private String imagem;
	private String escopo;
	private String tipo;
	private Object valor;
	private int tamVar;
	private int indiceVar;
	private String string;

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public int getIndiceVar() {
		return indiceVar;
	}

	public void setIndiceVar(int indiceVar) {
		this.indiceVar = indiceVar;
	}

	public int getTamVar() {
		return tamVar;
	}

	public void setTamVar(int tamVar) {
		this.tamVar = tamVar;
	}

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
	}

	public String getEscopo() {
		return escopo;
	}

	public void setEscopo(String escopo) {
		this.escopo = escopo;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public Object getValor() {
		return valor;
	}

	public void setValor(Object valor) {
		this.valor = valor;
	}

}
