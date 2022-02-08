package com.xbz.jdbc.model;

public class FieldFix {
	private String srcField;

	private String newField;

	private String newType;

	private String format;

	public String getSrcField() {
		return srcField;
	}

	public void setSrcField(String srcField) {
		this.srcField = srcField;
	}

	public String getNewField() {
		return newField;
	}

	public void setNewField(String newField) {
		this.newField = newField;
	}

	public String getNewType() {
		return newType;
	}

	public void setNewType(String newType) {
		this.newType = newType;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
}
