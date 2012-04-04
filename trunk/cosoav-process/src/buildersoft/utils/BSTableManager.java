package buildersoft.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class BSTableManager extends BSDataUtils {
	private Connection connection = null;
	private String tableName = null;
	Class<? extends BSTableManager> theClass = null;

	public BSTableManager() {
		this.theClass = this.getClass();
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public Integer insert() throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException, SQLException {
		this.theClass = this.getClass();

		String[] tableFields = getTableFields(theClass);
		String[] objectFields = getObjectFields(theClass);

		tableFields = deleteId(tableFields);
		objectFields = deleteId(objectFields);

		String sql = buildInsertSQLString(theClass, tableFields);
		Object[] params = getValues4Insert(theClass, objectFields);

		Integer newId = insert(this.connection, sql, array2List(params));

		String idField = getIdField(getObjectFields(theClass));
		fillField(theClass, idField, newId);

		return newId;
	}

	public Integer update() throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException, SQLException {

		String[] tableFields = getTableFields(this.theClass);
		String[] objectFields = getObjectFields(this.theClass);

		String sql = buildUpdateSQLString(this.theClass, tableFields);

		Object[] params = getValues4Update(this.theClass, objectFields);

		return update(this.connection, sql, array2List(params));
	}

	public void save() throws SecurityException, IllegalArgumentException,
			NoSuchFieldException, IllegalAccessException,
			NoSuchMethodException, InvocationTargetException, SQLException {
		if (update() == 0) {
			insert();
		}
	}

	public void delete() throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException, NoSuchFieldException, SQLException {
		Class<? extends BSTableManager> c = this.getClass();

		String[] objectFields = getObjectFields(c);
		Integer idValue = (Integer) getMethodValue(c, "get"
				+ getIdField(objectFields));

		String[] tableFields = getTableFields(c);
		String sql = buildDeleteSQLString(c, tableFields);

		update(this.connection, sql, array2List(idValue));

		Object[] prms = new Object[objectFields.length];

		fillObject(c, objectFields, prms);
	}

	public Boolean search() throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException,
			NoSuchFieldException, SQLException {
		Boolean out = Boolean.FALSE;

		String[] tableFields = getTableFields(this.theClass);
		String[] objectFields = getObjectFields(this.theClass);
		String[] tableFieldsWithOutId = deleteId(tableFields);

		// tableFields = deleteId(tableFields);
		// objectFields = deleteId(objectFields);

		String sql = buildSelectSQLString(tableFields, tableFieldsWithOutId);

//		System.out.println(sql);

		Integer idValue = getIdValue(this.theClass, "get"
				+ getIdField(objectFields));
		ResultSet rs = this.queryResultSet(this.connection, sql, idValue);

		Object value = null;
		if (rs.next()) {

			for (String f : tableFieldsWithOutId) {
				value = rs.getObject(f);
				fillField(this.theClass,  f.substring(1, f.length()),
						value);
			}
			out = Boolean.TRUE;
		}

		return out;
	}

	private void fillObject(Class<? extends BSTableManager> c,
			String[] objectFields, Object[] params) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		Class objectClass = null;
		String fieldName = null;
		Object value = null;
		for (int i = 0; i < objectFields.length; i++) {
			objectClass = params[i] == null ? null : params[i].getClass();
			fieldName = objectFields[i];
			value = params[i];

			fillField(c, fieldName, value);

		}

	}

	private void fillField(Class<? extends BSTableManager> c, String fieldName,
			Object value) throws NoSuchMethodException, IllegalAccessException,
			InvocationTargetException {
		Class<?> type = getTypeMethod(c, fieldName);
		Class[] paramTypes = new Class[] { type };
		Method method = c.getMethod("set" + fieldName, paramTypes);
		method.invoke(this, value);
	}

	private Class<?> getTypeMethod(Class<? extends BSTableManager> c,
			String methodName) throws SecurityException, NoSuchMethodException {

		Method m = c.getMethod("get" + methodName, null);
		Type type = m.getGenericReturnType();

		Class<?> out = null;

		if (type.toString().indexOf("String") > -1) {
			out = "".getClass();
		} else if (type.toString().indexOf("Integer") > -1) {
			out = Integer.class;
		} else if (type.toString().indexOf("Double") > -1) {
			out = Double.class;
		} else if (type.toString().indexOf("Long") > -1) {
			out = Long.class;
		} else if (type.toString().indexOf("Boolean") > -1) {
			out = Boolean.class;
		} else if (type.toString().indexOf("Calendar") > -1) {
			out = Calendar.class;
		} else if (type.toString().equals("java.util.Date")) {
			out = Date.class;
		} else {
			throw new RuntimeException("Type mismatch");
		}

		return out;
	}

	private String buildInsertSQLString(Class<? extends BSTableManager> c,
			String[] tableFields) throws NoSuchFieldException,
			IllegalAccessException {
		String sql = "INSERT INTO " + getTableName(c) + "(";

		sql += unSplit(tableFields, ",") + ") VALUES(";
		sql += getCommas(tableFields) + ");";
		return sql;
	}

	private String buildUpdateSQLString(Class<? extends BSTableManager> c,
			String[] tableFields) throws NoSuchFieldException,
			IllegalAccessException {

		String id = getIdField(tableFields);
		tableFields = deleteId(tableFields);

		String sql = "UPDATE " + getTableName(c) + " SET ";
		sql += unSplit(tableFields, "=?,");
		sql += " WHERE " + id + "=?";
		return sql;
	}

	private String buildDeleteSQLString(Class<? extends BSTableManager> c,
			String[] tableFields) throws NoSuchFieldException,
			IllegalAccessException {

		String sql = "DELETE FROM " + getTableName(c) + " WHERE "
				+ getIdField(tableFields) + "=?";

		return sql;
	}

	private String buildSelectSQLString(String[] tableFields,
			String[] tableFieldsWithOutId) throws NoSuchFieldException,
			IllegalAccessException {

		String sql = "SELECT " + unSplit(tableFieldsWithOutId, ",") + " FROM "
				+ getTableName(this.theClass) + " WHERE "
				+ getIdField(tableFields) + "=?";

		// sql += unSplit(tableFields, ",") + ") VALUES(";
		// sql += getCommas(tableFields) + ");";
		return sql;
	}

	private String getIdField(String[] tableFields) {
		String out = "";
		for (String s : tableFields) {
			if (isId(s)) {
				out = s;
				break;
			}
		}
		return out;
	}

	private Integer getIdValue(Class<? extends BSTableManager> c,
			String idFieldName) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		return (Integer) getMethodValue(c, idFieldName);

	}

	private Object[] getValues4Insert(Class<? extends BSTableManager> c,
			String[] tableFields) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		Object[] out = new Object[tableFields.length];
		Method method = null;
		int i = 0;
		for (String name : tableFields) {
			name = "get" + name;
			method = c.getMethod(name, null);
			out[i++] = method.invoke(this, null);
		}
		return out;
	}

	private Object[] getValues4Update(Class<? extends BSTableManager> c,
			String[] objectFields) throws SecurityException,
			NoSuchMethodException, IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {

		Object[] out = new Object[objectFields.length];
		Object aux = null;
		Object value = null;
		String methodName = "";
		// Method method = null;
		int i = 0;
		for (String name : objectFields) {
			methodName = "get" + name;

			value = getMethodValue(c, methodName);
			if (isId(name)) {
				aux = value;
			} else {
				out[i++] = value;
			}
		}
		out[i] = aux;

		return out;
	}

	private Object getMethodValue(Class<? extends BSTableManager> c,
			String methodName) throws NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {

		Method method = c.getMethod(methodName, null);
		Object value = method.invoke(this, null);
		return value;
	}

	private String getCommas(String[] tableFields) {
		String out = "";
		for (int i = 0; i < tableFields.length; i++) {
			out += "?,";
		}
		out = out.substring(0, out.length() - 1);
		return out;
	}

	private String[] deleteId(String[] tableFields) {
		String[] out = new String[tableFields.length - 1];
		int i = 0;
		for (String s : tableFields) {
			if (!isId(s)) {
				out[i++] = s;
			}
		}
		return out;
	}

	private boolean isId(String s) {
		return "id".equalsIgnoreCase(s) || "cid".equalsIgnoreCase(s);
	}

	private String unSplit(String[] tableFields, String s) {
		String out = "";
		for (String f : tableFields) {
			out += f + s;
		}
		out = out.substring(0, out.length() - 1);
		return out;
	}

	private String[] getTableFields(Class<? extends BSTableManager> c) {
		return getFields(c, true);
	}

	private String[] getObjectFields(Class<? extends BSTableManager> c) {
		return getFields(c, false);
	}

	private String[] getFields(Class<? extends BSTableManager> c,
			boolean asFields) {
		String[] out = null;
		List<String> list = new ArrayList<String>();

		String pre = asFields ? "c" : "";

		String name = null;
		Method[] methods = c.getDeclaredMethods();
		for (Method method : methods) {
			name = method.getName();
			if (name.startsWith("get")) {
				name = name.substring(3);
				list.add(pre + name);
			}
		}

		out = new String[list.size()];
		int i = 0;
		for (String o : list) {
			out[i++] = o;

		}

		return out;
	}

	private String getTableName(Class<? extends BSTableManager> c)
			throws SecurityException, NoSuchFieldException,
			IllegalArgumentException, IllegalAccessException {
		String out = null;
		if (this.tableName == null) {
			Field privateStringField = c.getDeclaredField("TABLE");
			privateStringField.setAccessible(true);
			out = (String) privateStringField.get(this);

			this.tableName = out;
		}

		return out;
	}

}
