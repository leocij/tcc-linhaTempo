package compilador;

public class Simbolo {
	private String imagem;
	private String tipo;
	private Object valor;
	private Integer indiceVariavel;
	private Integer tamVariavel;

	public Integer getTamVariavel() {
		return tamVariavel;
	}

	public void setTamVariavel(Integer tamVariavel) {
		this.tamVariavel = tamVariavel;
	}

	public Integer getIndiceVariavel() {
		return indiceVariavel;
	}

	public void setIndiceVariavel(Integer indiceVariavel) {
		this.indiceVariavel = indiceVariavel;
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
