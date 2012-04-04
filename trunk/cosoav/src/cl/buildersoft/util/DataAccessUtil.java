package cl.buildersoft.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;

import cosoav.utils.AbstractProcess;

public class DataAccessUtil extends AbstractProcess {

	public Connection getConnection(ServletContext context)
			throws ClassNotFoundException, SQLException {
		return getConnection(context, "cosoav");
	}

	public Connection getConnection(ServletContext context, String prefix)
			throws ClassNotFoundException, SQLException {

		String driverName = context.getInitParameter(prefix
				+ ".database.driver");
		String serverName = context.getInitParameter(prefix
				+ ".database.server");
		String database = context.getInitParameter(prefix
				+ ".database.database");
		String username = context.getInitParameter(prefix
				+ ".database.username");
		String password = context.getInitParameter(prefix
				+ ".database.password");

		return getConnection(driverName, serverName, database, password,
				username);
	}

	public Connection getConnection(String driverName, String serverName,
			String database, String password, String username)
			throws ClassNotFoundException, SQLException {
		return super.getConnection(driverName, serverName, database, password,
				username);
	}

	public void closeSQL() throws SQLException {
		super.closeSQL();
	}

	public void closeSQL(ResultSet rs) throws SQLException {
		super.closeSQL(rs);
	}

	public String queryField(Connection conn, String sql, Object parameter)
			throws SQLException {
		return super.queryField(conn, sql, parameter);
	}

	public String queryField(Connection conn, String sql, List<Object> parameter)
			throws SQLException {
		return super.queryField(conn, sql, parameter);
	}

	public ResultSet queryResultSet(Connection conn, String sql,
			List<Object> parameters) throws SQLException {
		return super.queryResultSet(conn, sql, parameters);
	}

	public ResultSet queryResultSet(Connection conn, String sql,
			Object parameter) throws SQLException {
		return super.queryResultSet(conn, sql, parameter);
	}

	public boolean update(Connection conn, String sql, Object parameter)
			throws SQLException {
		return super.update(conn, sql, parameter);
	}

	public boolean update(Connection conn, String sql, List<Object> parameter)
			throws SQLException {
		return super.update(conn, sql, parameter);
	}

	public String calendar2String(Calendar calendar) {
		String out = calendar.get(Calendar.DAY_OF_MONTH) + "/"
				+ (calendar.get(Calendar.MONTH) + 1) + "/"
				+ calendar.get(Calendar.YEAR) + " "
				+ calendar.get(Calendar.HOUR_OF_DAY) + ":"
				+ calendar.get(Calendar.MINUTE);
		return out;
	}

	public Calendar date2Calendar(Date date) {
		return super.date2Calendar(date);
	}
}
