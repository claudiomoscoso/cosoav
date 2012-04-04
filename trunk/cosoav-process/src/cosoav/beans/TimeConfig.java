package cosoav.beans;

import buildersoft.utils.BSTableManager;

public class TimeConfig extends BSTableManager {
	private Integer id = null;
	private String TypePlain = null;
	private Integer mins = null;

	private String TABLE = "tTimeConfig";

	public TimeConfig(){}
	
	public TimeConfig(Integer id, String typePlain, Integer mins)
			throws SecurityException, IllegalArgumentException,
			NoSuchFieldException, IllegalAccessException {
		super();
		this.id = id;
		TypePlain = typePlain;
		this.mins = mins;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTypePlain() {
		return TypePlain;
	}

	public void setTypePlain(String typePlain) {
		TypePlain = typePlain;
	}

	public Integer getMins() {
		return mins;
	}

	public void setMins(Integer mins) {
		this.mins = mins;
	}

	@Override
	public String toString() {
		return "TimeConfig [id=" + id + ", TypePlain=" + TypePlain + ", mins="
				+ mins + "]";
	}

}
