package cosoav.process;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cosoav.beans.Report;
import cosoav.utils.AbstractProcess;

public class BuildReport extends AbstractProcess {

	public void process(Connection conn) throws SQLException, ParseException {
		List<Long> ids = getIds(conn, "DONE");
		closeSQL();

		for (Long idPack : ids) {
			changeStatus(conn, idPack, "BUILD_REPO");

			try {
				processContent(conn, idPack);
				changeStatus(conn, idPack, "OK");
			} catch (SQLException e) {
				changeStatus(conn, idPack, "ERROR_REPO");
				throw e;
			}

		}

	}

	private void processContent(Connection conn, Long idPack)
			throws SQLException, ParseException {
		// String date = this.queryField(conn,
		// "SELECT cDate FROM tIn WHERE cId=?", idPack);

		// System.out.println(date);
		List<Report> reports = buildReports(conn, idPack);

		buildOtherInfo(conn, reports, idPack);
		done(conn, idPack);

	}

	private void done(Connection conn, Long idPack) throws SQLException {
		String sql = "UPDATE tReport SET cStatus = 'DONE' WHERE cDataBrute = ?";

		this.update(conn, sql, idPack);
		this.closeSQL();

	}

	private List<Report> buildReports(Connection conn, Long idPack)
			throws SQLException {
		// , cLlegadaIti, cSalidaIti
		String sql = "";
		// sql = "INSERT INTO tReport(MAT, cDataBrute, ST, VUELO, cLlegada) ";
		// sql +=
		// "SELECT MAT, ?, ST, VUELO, cDate FROM tIn WHERE ST IS NOT NULL AND cDataBrute = ?";

		sql = "INSERT INTO tReport(MAT, cDataBrute, ST, VUELO, cLlegadaConf, cLlegadaIti) ";
		sql += "SELECT MAT, ?, ST, VUELO, cDateConf, cDateIti FROM tIn WHERE ST IS NOT NULL AND cDataBrute = ?";

		List<Object> prms = new ArrayList<Object>();
		prms.add(idPack);
		prms.add(idPack);

		this.update(conn, sql, prms);
		this.closeSQL();

		List<Report> out = new ArrayList<Report>();
		sql = "SELECT cId, cLlegadaConf, MAT FROM tReport WHERE cDataBrute=? ORDER BY cLlegadaConf";

		ResultSet rs = this.queryResultSet(conn, sql, idPack);

		Report report = null;
		while (rs.next()) {
			report = new Report();
			report.setId(rs.getLong("cId"));

			Timestamp d = rs.getTimestamp("cLlegadaConf");
			// System.out.println( calendar2String( date2Calendar(d) ));

			report.setLlegada(date2Calendar(d));
			report.setMat(rs.getString("MAT"));
			out.add(report);
		}

		this.closeSQL(rs);
		return out;
	}

	private void buildOtherInfo(Connection conn, List<Report> reports,
			Long idPack) throws SQLException, ParseException {
		// String mat = null;
		for (Report report : reports) {
			// mat = getMatricula(conn, report);
			Calendar[] dateOut = findDateOut(conn, report, idPack);

			updateReport(conn, report, dateOut[0], dateOut[1]);
		}
		transitoTime(conn, idPack);
		// attention(conn, idPack);
	}

	private void attention(Connection conn, Long idPack) throws SQLException {
		/**
		 * llena el campo atención en base al tiempo en tránsito y al modelo del
		 * avion
		 */
		String sql = "UPDATE tReport SET ";
		sql += "cTransito=EXTRACT(DAY_MINUTE FROM cSalida) - EXTRACT(DAY_MINUTE FROM cLlegada) ";
		sql += "WHERE cDataBrute=? AND cLlegada IS NOT NULL";

		sql = "";

		// this.update(conn, sql, idPack);
		// this.closeSQL();
	}

	private void transitoTime(Connection conn, Long idPack) throws SQLException {
		/**
		 * Calcula el tiempo en tránsito, que es la diferencia entre el campo
		 * llegada y salida.
		 */
		String sql = "UPDATE tReport SET ";
		sql += "cTransito=TIME_TO_SEC(TIMEDIFF(cSalidaConf, cLlegadaConf))/60 ";
		sql += "WHERE cDataBrute=? AND cLlegadaConf IS NOT NULL";

		this.update(conn, sql, idPack);
		this.closeSQL();
	}

	private void updateReport(Connection conn, Report report,
			Calendar dateConfOut, Calendar dateItiOut) throws SQLException {
		/**
		 * actualiza campo con la fecha-hora de salida, además de otros campos
		 * relativos a la salida
		 */
		String sql = "UPDATE tReport SET cSalidaConf=?, cSalidaIti=?  WHERE cId=?";

		List<Object> prms = new ArrayList<Object>();
		prms.add(dateConfOut);
		prms.add(dateItiOut);
		prms.add(report.getId());

		try {
			this.update(conn, sql, prms);
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.closeSQL();
	}

	private Calendar[] findDateOut(Connection conn, Report report, Long idPack)
			throws ParseException, SQLException {
		String sql = "SELECT cId, cDateConf, cDateIti, ITI ";
		sql += "FROM tOut ";
		sql += "WHERE MAT = ? AND cUsed = 0 AND cDataBrute = ? AND cDateConf > ? ORDER BY cDateConf";

		List<Object> prms = new ArrayList<Object>();
		prms.add(report.getMat());
		prms.add(idPack);
		prms.add(report.getLlegada());

		ResultSet rs = this.queryResultSet(conn, sql, prms);

		Long id = null;
		Calendar[] out = new Calendar[2];
		if (rs.next()) {
			id = rs.getLong("cId");

			out[0] = this.date2Calendar(rs.getTimestamp("cDateConf"));
			out[1] = this.date2Calendar(rs.getTimestamp("cDateIti"));

			this.closeSQL(rs);

			sql = "UPDATE tOut SET cUsed=true WHERE cId = ?";
			this.update(conn, sql, id);
			this.closeSQL();
		}
		return out;
	}

	private String getMatricula(Connection conn, Report report)
			throws SQLException {
		String sql = "SELECT MAT FROM tReport WHERE cId=?";
		String out = this.queryField(conn, sql, report.getId());
		this.closeSQL();
		return out;
	}
}
