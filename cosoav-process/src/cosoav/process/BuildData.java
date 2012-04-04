package cosoav.process;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import cosoav.utils.AbstractProcess;
import cosoav.utils.FieldsEnum;

public class BuildData extends AbstractProcess {
	public void process(Connection conn) throws SQLException, DocumentException {
		List<Long> ids = getIds(conn, "AS_XML");
		closeSQL();

		for (Long id : ids) {
			changeStatus(conn, id, "PROC_XML");
			try {
				processContent(conn, id);
				changeStatus(conn, id, "DONE");
			} catch (SQLException e) {
				changeStatus(conn, id, "ERROR_XML");
				throw e;
			}
		}

	}

	private void processContent(Connection conn, Long id) throws SQLException,
			DocumentException {
		String sql = "SELECT cXML, cDate FROM tDataBrute WHERE cId = ?";

		ResultSet rs = this.queryResultSet(conn, sql, id);

		String xml = null;
		Calendar date = null;
		if (rs.next()) {
			xml = rs.getString("cXML");
			date = date2Calendar(rs.getDate("cDate"));
		}
		closeSQL(rs);

		Document doc = string2Document(xml);
		xml2db(doc, conn, id, date);

	}

	private void xml2db(Document doc, Connection conn, Long index, Calendar date)
			throws SQLException {
		Element root = doc.getRootElement();
		List<Element> rows = root.elements("TR");
		Element row = null;
		Element oldRow = null;
		boolean onlyOut = false;
		Element td = null;
		for (Object rowObj : rows) {
			row = (Element) rowObj;
			td = getElement(row, 0);
			onlyOut = td.attribute("colspan") != null;
			date = validDate(date, row, oldRow, onlyOut);

			saveRow(conn, index, row, onlyOut, date);
			oldRow = row;

		}
	}

	private Calendar validDate(Calendar date, Element row, Element oldRow,
			boolean onlyOut) {
		String oldItiString = null;
		String itiString = null;
		Integer itiInteger = 0;
		Integer itiOldInteger = 0;

		int field = !onlyOut ? FieldsEnum.LL_ITI : FieldsEnum.SA_ITI;
		int fiveHours = time2Integer("05:00");

		if (oldRow != null) {
			oldItiString = getElementTextTrim(oldRow, field,
					getElement(oldRow, 0).attribute("colspan") != null);
			itiString = getElementTextTrim(row, field, getElement(row, 0)
					.attribute("colspan") != null);

			itiInteger = time2Integer(itiString);
			itiOldInteger = time2Integer(oldItiString);

			if ((itiOldInteger - itiInteger) > fiveHours) {
				date.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		/**
		 * <code>
		System.out.print(onlyOut + "    - ");
		System.out.print("itiInteger " + itiString + " " + itiInteger);
		System.out
				.print(" itiOldInteger " + oldItiString + " " + itiOldInteger);
		System.out.println(" " + ((itiOldInteger - itiInteger) > 300));
</code>
		 */
		return date;
	}

	private void saveRow(Connection conn, Long id, Element row,
			boolean onlyOut, Calendar dateConf) throws SQLException {
		List params = new ArrayList();
		String cnf = null;
		String iti = null;
		String time = null;
		String mat = null;
		Calendar dateIti = (Calendar) dateConf.clone();

		String sql = null;
		if (!onlyOut) {
			sql = "INSERT INTO tIn";
			sql += "(cDataBrute, MAT, ST, CRD1, VUELO, ORI, ITI, RECNF, OBS, TA, cDateConf, cDateIti) ";
			sql += "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

			cnf = getElementNullIfTrim(row, FieldsEnum.LL_RECNF, onlyOut);
			iti = getElementTextTrim(row, FieldsEnum.LL_ITI, onlyOut);

			time = cnf == null ? iti : cnf;
			dateConf.set(Calendar.HOUR_OF_DAY, this.getHour(time));
			dateConf.set(Calendar.MINUTE, this.getMins(time));
			
			dateIti.set(Calendar.HOUR_OF_DAY, this.getHour(iti));
			dateIti.set(Calendar.MINUTE, this.getMins(iti));

			mat = getElementTextTrim(row, FieldsEnum.LL_MAT, onlyOut);
			params.add(id);
			params.add(mat);
			params.add(getElementNullIfTrim(row, FieldsEnum.LL_ST, onlyOut));
			params.add(getElementNullIfTrim(row, FieldsEnum.LL_CRD1, onlyOut));
			params.add(getElementTextTrim(row, FieldsEnum.LL_VUELO, onlyOut));
			params.add(getElementTextTrim(row, FieldsEnum.LL_ORI, onlyOut));
			params.add(iti);
			params.add(cnf);
			params.add(getElementTextTrim(row, FieldsEnum.LL_OBS, onlyOut));
			params.add(getElementTextTrim(row, FieldsEnum.LL_TA, onlyOut));
			params.add(dateConf);
			params.add(dateIti);

			this.update(conn, sql, params);
			this.closeSQL();

			params.clear();
		}

		sql = "INSERT INTO tOut";
		sql += "(cDataBrute, MAT, ST, PTA, CRD1, CRD2, VUELO, DES, ITI, CNF, ROC, ATR, OBS, cDateConf, cDateIti) ";
		sql += "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";

		mat = getElementTextTrim(row, FieldsEnum.SA_MAT, onlyOut);

		cnf = getElementNullIfTrim(row, FieldsEnum.SA_CNF, onlyOut);
		iti = getElementTextTrim(row, FieldsEnum.SA_ITI, onlyOut);

		time = cnf == null ? iti : cnf;

		dateConf.set(Calendar.HOUR_OF_DAY, this.getHour(time));
		dateConf.set(Calendar.MINUTE, this.getMins(time));
		dateIti.set(Calendar.HOUR_OF_DAY, this.getHour(iti));
		dateIti.set(Calendar.MINUTE, this.getMins(iti));

		params.add(id);
		params.add(mat);
		params.add(getElementTextTrim(row, FieldsEnum.SA_ST, onlyOut));
		params.add(getElementTextTrim(row, FieldsEnum.SA_PTA, onlyOut));
		params.add(getElementTextTrim(row, FieldsEnum.SA_CRD1, onlyOut));
		params.add(getElementTextTrim(row, FieldsEnum.SA_CRD2, onlyOut));
		params.add(getElementTextTrim(row, FieldsEnum.SA_VUELO, onlyOut));
		params.add(getElementTextTrim(row, FieldsEnum.SA_DES, onlyOut));
		params.add(iti);
		params.add(cnf);
		params.add(getElementTextTrim(row, FieldsEnum.SA_ROC, onlyOut));
		params.add(getElementTextTrim(row, FieldsEnum.SA_ATR, onlyOut));
		params.add(getElementTextTrim(row, FieldsEnum.SA_OBS, onlyOut));
		params.add(dateConf);
		params.add(dateIti);

		this.update(conn, sql, params);
		this.closeSQL();

	}

	private Document string2Document(String xml) throws DocumentException {
		Document doc = null;

		doc = DocumentHelper.parseText(xml);

		return doc;

	}

}
