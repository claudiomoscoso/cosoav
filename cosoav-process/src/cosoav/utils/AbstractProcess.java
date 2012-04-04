package cosoav.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.dom4j.Element;

public class AbstractProcess {
	PreparedStatement preparedStatement = null;

	protected List<Object> array2List(Object... prms) {
		List<Object> out = new ArrayList<Object>();

		for (Object o : prms) {
			out.add(o);
		}

		return out;
	}

	protected void reportException(Exception e, String html, List<Object> prms) {
		ReportException re = new ReportException();
		re.process(e, html, prms);

	}

	protected void reportException(Exception e, String html) {
		reportException(e, html, null);
	}

	protected void reportException(Exception e) {
		reportException(e, null, null);
	}

	protected Calendar string2Calendar(String dateString, String format)
			throws ParseException {
		DateFormat formatter = new SimpleDateFormat(format);
		java.util.Date date = (java.util.Date) formatter.parse(dateString);

		Calendar out = date2Calendar(date);

		// System.out.println(calendar2String(out));
		date = null;
		return out;
	}

	protected Calendar date2Calendar(java.util.Date date) {
		Calendar out = Calendar.getInstance();
		out.setTimeInMillis(date.getTime());
		return out;
	}

	protected String calendar2String(Calendar calendar) {
		String out = calendar.get(Calendar.YEAR) + "-"
				+ (calendar.get(Calendar.MONTH) + 1) + "-"
				+ calendar.get(Calendar.DAY_OF_MONTH) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendar.get(Calendar.MINUTE) + " - "
				+ (new Date(calendar.getTimeInMillis()).toString());
		return out;
	}

	protected Integer time2Integer(String s) {
		int pointPointIndex = s.indexOf(":");

		int hour = Integer.parseInt(s.substring(0, pointPointIndex));
		int mins = Integer.parseInt(s.substring(pointPointIndex + 1));

		Integer out = (hour * 60) + mins;
		return out;
	}

	protected Integer getHour(String s) {
		int pointPointIndex = s.indexOf(":");
		return Integer.parseInt(s.substring(0, pointPointIndex));
	}

	protected Integer getMins(String s) {
		int pointPointIndex = s.indexOf(":");

		return Integer.parseInt(s.substring(pointPointIndex + 1));
	}

	protected void changeStatus(Connection conn, Long id, String status)
			throws SQLException {
		String sql = "UPDATE tDataBrute SET cStatus=? WHERE cId=?";
		// String sql =
		// "INSERT tDataBrute SET cHTML=?, cProcesed=?, cDatetime=?;";

		List<Object> prms = new ArrayList<Object>();
		prms.add(status);
		prms.add(id);
		this.update(conn, sql, prms);
		closeSQL();

		/**
		 * <code>
			PreparedStatement preparedStatement;
			try {
				preparedStatement = conn.prepareStatement(sql);

				preparedStatement.setString(1, status);
				preparedStatement.setLong(2, id);

				preparedStatement.execute();
				preparedStatement.close();

			} catch (SQLException e) {
				success = false;
				e.printStackTrace();
				SendMessage sm = new SendMessage();
				sm.process(e, "Error SQL");
			}
			preparedStatement = null;
</code>
		 */

	}

	protected List<Long> getIds(Connection conn, String status)
			throws SQLException {
		String sql = "SELECT cId FROM tDataBrute WHERE cStatus=?";
		List<Long> out = new ArrayList<Long>();

		ResultSet rs = this.queryResultSet(conn, sql, status);

		while (rs.next()) {
			out.add(rs.getLong("cId"));
		}
		rs.close();
		closeSQL();

		return out;
	}

	protected void closeSQL(ResultSet rs) throws SQLException {
		if (rs != null) {
			rs.close();
		}
		closeSQL();
	}

	protected void closeSQL() throws SQLException {
		this.preparedStatement.close();
	}

	protected boolean update(Connection conn, String sql, Object parameter)
			throws SQLException {
		List<Object> prms = new ArrayList<Object>();
		prms.add(parameter);

		return update(conn, sql, prms);
	}

	protected boolean update(Connection conn, String sql, List<Object> parameter)
			throws SQLException {
		int rowsAffected = 0;

		preparedStatement = conn.prepareStatement(sql);
		parametersToStatement(parameter, preparedStatement);
		rowsAffected = preparedStatement.executeUpdate();

		return rowsAffected > 0;
	}

	protected String queryField(Connection conn, String sql, Object parameter)
			throws SQLException {
		List<Object> prms = new ArrayList<Object>();
		prms.add(parameter);

		return queryField(conn, sql, prms);
	}

	protected String queryField(Connection conn, String sql,
			List<Object> parameter) throws SQLException {
		String out = null;
		ResultSet rs = queryResultSet(conn, sql, parameter);

		if (rs.next()) {
			out = rs.getString(1);
		}
		rs.close();

		return out;
	}

	protected ResultSet queryResultSet(Connection conn, String sql,
			Object parameter) throws SQLException {
		List<Object> prms = new ArrayList<Object>();
		prms.add(parameter);

		return queryResultSet(conn, sql, prms);
	}

	protected ResultSet queryResultSet(Connection conn, String sql,
			List<Object> parameters) throws SQLException {
		ResultSet out = null;

		preparedStatement = conn.prepareStatement(sql);
		parametersToStatement(parameters, preparedStatement);

		out = preparedStatement.executeQuery();

		return out;
	}

	private void parametersToStatement(List<Object> parameters,
			PreparedStatement preparedStatement) throws SQLException {
		// this.callableStatement = conn.prepareCall(sqlStatement);
		if (parameters != null) {
			// int len = parameters.size();
			int i = 0;
			// Object param = null;
			// for (int i = 0; i < len; i++) {
			// param = parameters[i];
			for (Object param : parameters) {

				if (param instanceof String) {
					preparedStatement.setString(i + 1, (String) param);
				} else if (param instanceof Integer) {
					preparedStatement.setInt(i + 1,
							((Integer) param).intValue());
				} else if (param instanceof Double) {
					preparedStatement.setDouble(i + 1,
							((Double) param).doubleValue());
				} else if (param instanceof Long) {
					preparedStatement
							.setLong(i + 1, ((Long) param).longValue());
				} else if (param instanceof Boolean) {
					preparedStatement.setBoolean(i + 1,
							((Boolean) param).booleanValue());
				} else if (param instanceof java.util.Calendar
						|| param instanceof java.util.GregorianCalendar) {
					java.sql.Timestamp time = new java.sql.Timestamp(
							((java.util.Calendar) param).getTimeInMillis());
					preparedStatement.setTimestamp(i + 1, time);
				} else if (param instanceof java.util.Date) {
					java.sql.Timestamp time = new java.sql.Timestamp(
							((java.util.Date) param).getTime());
					preparedStatement.setTimestamp(i + 1, time);
				} else if (param == null) {
					preparedStatement.setNull(i + 1, java.sql.Types.NULL);
				} else {
					String message = "Object type not cataloged, please insert code in \"AbstractProcess\" for class \""
							+ param.getClass().getName() + "\"";
					System.out.println(message);
					SendMessage sm = new SendMessage();
					sm.process(message, "UNKNOW data type");

					throw new RuntimeException();
				}

				i++;
			}
		}
	}

	protected Element getElement(Element row, int index) {
		return getElement(row, index, false);
	}

	protected Element getElement(Element row, int index, boolean onlyOut) {
		Object e = row.elements("TD").get(index - (onlyOut ? 8 : 0));
		return (Element) e;
	}

	protected String getElementTextTrim(Element row, int index) {
		return getElementTextTrim(row, index, false);
	}

	protected String getElementTextTrim(Element row, int index, boolean onlyOut) {
		Element e = getElement(row, index, onlyOut);
		return e.getTextTrim();
	}

	protected String getElementNullIfTrim(Element row, int index,
			boolean onlyOut) {
		Element e = getElement(row, index, onlyOut);
		String trim = e.getTextTrim();
		return trim.length() == 0 ? null : trim;
	}

	protected Connection getConnection(String driverName, String serverName,
			String database, String password, String username)
			throws ClassNotFoundException, SQLException {
		Connection connection = null;
		Class.forName(driverName);

		String url = "jdbc:mysql://" + serverName + "/" + database;

		connection = DriverManager.getConnection(url, username, password);
		return connection;
	}
}
