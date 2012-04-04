package cl.buildersoft.beans;

import buildersoft.utils.BSTableManager;

public class User extends BSTableManager {
	private Integer id = null;
	private String mail = null;
	private String name = null;
	private String password = null;
	private String TABLE = "tUser";

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
