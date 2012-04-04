package cosoav.beans;

import java.util.Calendar;

public class Report {
	private Long id = null;
	private Long dataBrute = null;
	private String mat = null;
	private String st = null;
	private String vuelo = null;
	private Calendar llegada = null;
	private Calendar salida = null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDataBrute() {
		return dataBrute;
	}

	public void setDataBrute(Long dataBrute) {
		this.dataBrute = dataBrute;
	}

	public String getMat() {
		return mat;
	}

	public void setMat(String mat) {
		this.mat = mat;
	}

	public String getSt() {
		return st;
	}

	public void setSt(String st) {
		this.st = st;
	}

	public String getVuelo() {
		return vuelo;
	}

	public void setVuelo(String vuelo) {
		this.vuelo = vuelo;
	}

	public Calendar getLlegada() {
		return llegada;
	}

	public void setLlegada(Calendar llegada) {
		this.llegada = llegada;
	}

	public Calendar getSalida() {
		return salida;
	}

	public void setSalida(Calendar salida) {
		this.salida = salida;
	}
}
