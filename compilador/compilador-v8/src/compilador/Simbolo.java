package compilador;

public class Simbolo {
	private String imagem;
	private String tipo;
	private Object valor;
	private int inicio;
	private int tamMemoria;

	public int getTamMemoria() {
		return tamMemoria;
	}

	public void setTamMemoria(int tamMemoria) {
		this.tamMemoria = tamMemoria;
	}

	public int getInicio() {
		return inicio;
	}

	public void setInicio(int inicio) {
		this.inicio = inicio;
	}

	public String getImagem() {
		return imagem;
	}

	public void setImagem(String imagem) {
		this.imagem = imagem;
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
