package compilador;


public class Simbolo {
	private String imagem;
	private String escopo;
	private String tipo;
	private Object valor;
	private int iniMemoria;
	private int tamMemoria;
	private String constante;
	
	
	
	public String getConstante() {
		return constante;
	}

	public void setConstante(String constante) {
		this.constante = constante;
	}

	public int getIniMemoria() {
		return iniMemoria;
	}

	public void setIniMemoria(int iniMemoria) {
		this.iniMemoria = iniMemoria;
	}

	public int getTamMemoria() {
		return tamMemoria;
	}

	public void setTamMemoria(int tamMemoria) {
		this.tamMemoria = tamMemoria;
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