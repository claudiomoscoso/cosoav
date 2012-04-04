package cosoav.process;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import cosoav.utils.AbstractProcess;

public class ProcessHtml extends AbstractProcess {

	public void process(Connection conn) throws ParseException, SQLException,
			DocumentException {
		List<Long> ids = getIds(conn, "NEW");
		closeSQL();

		boolean success = false;
		if (ids != null) {

			for (Long id : ids) {
				changeStatus(conn, id, "PROC");

				try {
					processContent(conn, id);
					changeStatus(conn, id, "AS_XML");
				} catch (SQLException e) {
					changeStatus(conn, id, "ERROR");
					throw e;
				}

			}
		}

	}

	private void processContent(Connection conn, Long id)
			throws ParseException, SQLException, DocumentException {
		String sql = "SELECT cHTML FROM tDataBrute WHERE cId=?";

		// PreparedStatement preparedStatement;
		ResultSet rs = null;
		String html = null;
		String xml = null;

		rs = this.queryResultSet(conn, sql, id);

		// preparedStatement = conn.prepareStatement(sql);
		// preparedStatement.setLong(1, id);
		// rs = preparedStatement.executeQuery();

		if (rs.next()) {
			html = rs.getString(1);
		}

		this.closeSQL(rs);

		xml = processContent(html);
		Calendar date = getCalendar(html);

		// preparedStatement.close();

		sql = "UPDATE tDataBrute SET cXML=?, cDate=? WHERE cId=?";

		if (conn != null) {

			List<Object> params = new ArrayList<Object>();
			params.add(xml);
			params.add(date);
			params.add(id);

			this.update(conn, sql, params);
			/**
			 * <code>						
						preparedStatement = conn.prepareStatement(sql);

						preparedStatement.setString(1, xml);
						preparedStatement.setLong(2, id);

						preparedStatement.execute();
						preparedStatement.close();
						</code>
			 */

			// preparedStatement = null;
		}

	}

	private String processContent(String content) throws ParseException,
			DocumentException {
		int llegadasPosition = content.indexOf("LLEGADAS");
		int tbodyBeginPosition = content.indexOf("TBODY", llegadasPosition) - 1;
		int tbodyEndPosition = content.indexOf("/TBODY", tbodyBeginPosition) + 7;

		String infoContent = content.substring(tbodyBeginPosition,
				tbodyEndPosition);

		// System.out.println(infoContent.substring(0, 15000));
		infoContent = fixContent(infoContent);

		DocumentHelper.parseText(infoContent);

		return infoContent;
	}

	private Calendar getCalendar(String infoContent) throws ParseException {
		int fechDesdeIndex = infoContent.indexOf("fechDesde");

		int valueIndexBegin = infoContent.indexOf("value", fechDesdeIndex)
				+ "value".length() + 2;
		int valueIndexEnd = infoContent.indexOf("\" read", valueIndexBegin);

		String fechDesdeValue = infoContent.substring(valueIndexBegin,
				valueIndexEnd);

		return string2Calendar(fechDesdeValue, "dd/MM/yyyy");
	}

	private static String fixContent(String infoContent) {
		String out = infoContent.replaceAll("'", "\"");
		out = out.replaceAll("\"style=", "\" style=");
		out = out.replaceAll("</td>", "</TD>");
		out = out.replaceAll("<td", "<TD");

		out = fixImgTag(out);
		return out;
	}

	private static String fixImgTag(String info) {
		List<Integer> positions = new ArrayList<Integer>();

		int pos = info.indexOf("<img", 0);
		while (pos > -1) {
			positions.add(pos);
			pos = info.indexOf("<img", pos + 1);
		}

		int endIndex = 0;
		// String imgTag = null;
		char source[];
		char target[];

		for (int i = positions.size() - 1; i >= 0; i--) {
			source = info.toCharArray();
			target = new char[source.length + 1];
			endIndex = info.indexOf(">", positions.get(i));

			System.arraycopy(source, 0, target, 0, endIndex);
			target[endIndex] = '/';
			System.arraycopy(source, endIndex + 0, target, endIndex + 1,
					source.length - endIndex);

			info = new String(target);
		}

		// System.out.println(positions);
		return info;
	}

	private static void showArray(char[] source) {
		// System.out.println(new String(source));

	}

	private void parseAsXML(String infoContent) {

	}

	public static String readFileContent(String filePath) throws IOException {
		String out = "";
		BufferedReader in = new BufferedReader(new FileReader(filePath));
		String str;
		while ((str = in.readLine()) != null) {
			out += str + "\n";
		}
		in.close();

		/**
		 * <code>
		out = "<TBODY>"
				+ "<TR><TD title='' style=''>CC-CZN</TD><TD title=''style=''>P28</TD><TD title=''style=''>CAM</TD><TD>284</TD><TD>PUQ</TD><TD id=\"itin\">00:10</TD><TD title=''style=''>23:54</TD><TD></TD><TD id=\"ta\">09:25</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAH</TD><TD title=''style=''>P21</TD><TD title=''style=''>21</TD><TD title=''style=''>MOR</TD><TD title=''style=''>C2</TD><TD>97</TD><TD>PUQ</TD><TD>01:20</TD><TD title=''style=';'>01:20</TD><TD title=''style=''>00/131</TD><TD></TD><TD></TD></TR>\n"
				+ "<TR><TD title='' style=''>CC-BAG</TD><TD title=''style=''>P11</TD><TD title=''style=''>CRC</TD><TD>480</TD><TD>AEP</TD><TD id=\"itin\">00:15</TD><TD title=''style=''>00:09</TD><TD></TD><TD id=\"ta\">06:55</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAN</TD><TD title=''style=''>P25</TD><TD title=''style=''>25</TD><TD title=''style=''>CRC</TD><TD title=''style=''>C2</TD><TD>380</TD><TD>ARI</TD><TD>01:30</TD><TD title='1.)2012-01-25 01:40:25 => 01:30->01:32 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>01:32</TD><TD title=''style=''>00/172</TD><TD></TD><TD></TD></TR>\n"
				+ "<TR><TD title='' style=''>CC-BJB</TD><TD title=''style=''>R37</TD><TD title=''style=''>SMU</TD><TD>137</TD><TD>ANF</TD><TD id=\"itin\">00:30</TD><TD title=''style=''>00:17</TD><TD></TD><TD id=\"ta\">01:52</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BJB</TD><TD title=''style=''>R37</TD><TD></TD><TD title=''style=''>SMU</TD><TD></TD><TD>1398</TD><TD>ANF</TD><TD>02:00</TD><TD title='1.)2012-01-25 02:16:29 => 02:00->02:09 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>02:09</TD><TD></TD><TD></TD><TD></TD></TR>\n"
				+ "<TR><TD title='' style=''>CC-BAR</TD><TD title=''style=''>P24</TD><TD title=''style=''>CAM</TD><TD>159</TD><TD>CJC</TD><TD id=\"itin\">01:25</TD><TD title='1.)2012-01-25 00:53:28 => 01:24->01:05 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>01:05</TD><TD align=\"center\" title='0109/12/12/15' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">05:24</TD> <TD style=\"border: none\"></TD><TD title='1.)2012-01-25 00:00:51 => CC-BJC->CC-COM &#13;&#10;' style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>CC-COM</TD><TD title=''style=''>P26</TD><TD title=''style=''>26</TD><TD title=''style=''>SRW</TD><TD title=''style=''>C2</TD><TD>366</TD><TD>IQQ</TD><TD>02:30</TD><TD title=''style=';'>02:30</TD><TD title=''style=''>00/133</TD><TD></TD><TD></TD></TR>\n"
				+ "<TR><TD title='' style=''>CC-CYI</TD><TD title=''style=''>P16</TD><TD title=''style=''>MOR</TD><TD>2635</TD><TD>LIM</TD><TD id=\"itin\">02:00</TD><TD title=''style=''>01:34</TD><TD></TD><TD id=\"ta\">04:39</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAU</TD><TD title=''style=''>P28</TD><TD title=''style=''>28</TD><TD title=''style=''>SMU</TD><TD title=''style=''>C2</TD><TD>140</TD><TD>CJC</TD><TD>05:00</TD><TD title='1.)2012-01-25 05:08:36 => 05:00->04:58 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>04:58</TD><TD title=''style=''>00/150</TD><TD></TD><td id=\"obs\" align=\"center\" title='03:18' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-CZQ</TD><TD title=''style=''>P25</TD><TD title=''style=''>CRC</TD><TD>167</TD><TD>IQQ</TD><TD id=\"itin\">02:00</TD><TD title=''style=''>01:49</TD><TD></TD><TD id=\"ta\">06:29</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-CZS</TD><TD title=''style=''>P22</TD><TD title=''style=''>22</TD><TD title=''style=''>CRC</TD><TD title=''style=''>C2</TD><TD>324</TD><TD>ANF</TD><TD>05:35</TD><TD title='1.)2012-01-25 05:40:25 => 05:35->05:34 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>05:34</TD><TD title=''style=''>00/125</TD><TD></TD><td id=\"obs\" align=\"center\" title='04:26' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-BJC</TD><TD title=''style=''>P27</TD><TD title=''style=''>SMU</TD><TD>339</TD><TD>ANF</TD><TD id=\"itin\">01:50</TD><TD title='1.)2012-01-25 01:54:12 => 02:45->02:28 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>02:28</TD><TD></TD><TD id=\"ta\">06:31</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-CVP</TD><TD title=''style=''>P24</TD><TD title=''style=''>24</TD><TD title=''style=''>SSO</TD><TD title=''style=''>C2</TD><TD>160</TD><TD>IQQ</TD><TD>05:55</TD><TD title='1.)2012-01-25 06:00:29 => 05:55->05:54 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>05:54</TD><TD title=''style=''>00/105</TD><TD></TD><td id=\"obs\" align=\"center\" title='04:32' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-BAQ</TD><TD title=''style=''>P26</TD><TD title=''style=''>SMU</TD><TD>92</TD><TD>PUQ</TD><TD id=\"itin\">03:50</TD><TD title=''style=''>03:25</TD><TD align=\"center\" title='0325/28/28/30' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">03:32</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAL</TD><TD title=''style=''>P25</TD><TD title=''style=''>25</TD><TD title=''style=''>LOS</TD><TD title=''style=''>C2</TD><TD>81</TD><TD>PUQ</TD><TD>06:00</TD><TD title=''style=';'>06:00</TD><TD title=''style=''>00/152</TD><TD></TD><td id=\"obs\" align=\"center\" title='04:08' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-CPJ</TD><TD title='1.)2012-01-25 04:44:02 => P18->P20 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>P20</TD><TD title=''style=''>LRS</TD><TD>2643</TD><TD>LIM</TD><TD id=\"itin\">05:10</TD><TD title='1.)2012-01-25 02:15:38 => 05:17->04:51 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>04:51</TD><TD align=\"center\" title='04:50/55/55/00 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">02:12</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-CYI</TD><TD title=''style=''>P16</TD><TD title=''style=''>16</TD><TD title=''style=''>LRS</TD><TD title=''style=''>C3</TD><TD>2644</TD><TD>LIM</TD><TD>06:10</TD><TD title='1.)2012-01-25 06:24:28 => 06:10->06:13 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>06:13</TD><TD title=''style=''>00/133</TD><TD></TD><td id=\"obs\" align=\"center\" title='01:44' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-CPQ</TD><TD title=''style=''>P17</TD><TD title=''style=''>LAG</TD><TD>2641</TD><TD>LIM</TD><TD id=\"itin\">05:20</TD><TD title='1.)2012-01-25 05:02:11 => 05:32->05:00 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>05:00</TD><TD align=\"center\" title='05:00/11/06/29 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">03:59</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAR</TD><TD title=''style=''>P27</TD><TD title=''style=''>27</TD><TD title=''style=''>GPP</TD><TD title=''style=''>C4</TD><TD>142</TD><TD>CJC</TD><TD>06:30</TD><TD title='1.)2012-01-25 06:36:27 => 06:30->06:29 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>06:29</TD><TD title=''style=''>00/190</TD><TD></TD><td id=\"obs\" align=\"center\" title='04:02' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-CML</TD><TD title=''style=''>P18</TD><TD title=''style=''>MQM</TD><TD>601</TD><TD>LAX</TD><TD id=\"itin\">05:50</TD><TD title='1.)2012-01-25 04:07:08 => 05:45->05:28 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>05:28</TD><TD align=\"center\" title='05:30/35/35/37 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">16:57</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAI</TD><TD title=''style=''>P22</TD><TD title=''style=''>22</TD><TD title=''style=''>GRE</TD><TD title=''style=''>C5</TD><TD>162</TD><TD>IQQ</TD><TD>06:50</TD><TD title='1.)2012-01-25 07:00:35 => 06:50->06:51 &#13;&#10;2.)2012-01-25 08:36:25 => 06:51->06:50 &#13;&#10;'style='background-color : #FFFF99;color : #000000;font-weight :bold;;'>06:50</TD><TD title=''style=''>00/166</TD><TD></TD><td id=\"obs\" align=\"center\" title='5:41' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>HC-CLB</TD><TD title=''style=''>P10</TD><TD title=''style=''>ARN</TD><TD>1447</TD><TD>GYE</TD><TD id=\"itin\">05:50</TD><TD title=''style=''>05:37</TD><TD align=\"center\" title='05:40/45/45/46 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">05:08</TD> <TD style=\"border: none\"></TD><TD title='' style=''>LV-BFY</TD><TD title=''style=''>R37</TD><TD title=''style=''>19A</TD><TD title=''style=''>PCS</TD><TD title=''style=''>C1</TD><TD>4645</TD><TD>EZE</TD><TD>06:50</TD><TD title='1.)2012-01-25 07:04:34 => 06:50->06:53 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>06:53</TD><TD title=''style=''>12/132</TD><TD></TD><td id=\"obs\" align=\"center\" title='4:45' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-CZU</TD><TD title=''style=''>P10</TD><TD title=''style=''>SSO</TD><TD>501</TD><TD>MIA</TD><TD id=\"itin\">06:45</TD><TD title=''style=''>06:21</TD><TD align=\"center\" title='06:30/35/35/38 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">01:54</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAQ</TD><TD title=''style=''>P26</TD><TD title=''style=''>26</TD><TD title=''style=''>RBD</TD><TD title=''style=''>C2</TD><TD>126</TD><TD>ANF</TD><TD>07:00</TD><TD title='1.)2012-01-25 07:12:27 => 07:00->06:57 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>06:57</TD><TD title=''style=''>00/149</TD><TD></TD><td id=\"obs\" align=\"center\" title='3:30' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-BAT</TD><TD title=''style=''>P28</TD><TD title=''style=''>LRS</TD><TD>290</TD><TD>PUQ</TD><TD id=\"itin\">07:00</TD><TD title=''style=''>06:49</TD><TD align=\"center\" title='06:45/00/00/03 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">01:21</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-CPJ</TD><TD title=''style=''>R09</TD><TD title=''style=''>10</TD><TD title=''style=''>RGM</TD><TD title=''style=''>C4</TD><TD>2634</TD><TD>LIM</TD><TD>07:05</TD><TD title='1.)2012-01-25 07:20:27 => 07:05->07:03 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>07:03</TD><TD title=''style=''>00/117</TD><TD></TD><td id=\"obs\" align=\"center\" title='5:30' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-BJB</TD><TD title=''style=''>P25</TD><TD title=''style=''>LAG</TD><TD>323</TD><TD>ANF</TD><TD id=\"itin\">07:45</TD><TD title=''style=''>07:20</TD><TD align=\"center\" title='07:19/25/25/30 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">01:49</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAG</TD><TD title=''style=''>P20</TD><TD title=''style=''>20A</TD><TD title=''style=''>LKC</TD><TD title=''style=''>C3</TD><TD>439</TD><TD>AEP</TD><TD>07:00</TD><TD title='1.)2012-01-25 07:20:27 => 07:00->07:04 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>07:04</TD><TD title=''style=''>09/148</TD><TD></TD><td id=\"obs\" align=\"center\" title='5:52' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-BJA</TD><TD title=''style=''>P17</TD><TD title='1.)2012-01-25 05:34:59 => POC->GRE &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>GRE</TD><TD>503</TD><TD>MIA</TD><TD id=\"itin\">07:55</TD><TD title=''style=''>07:36</TD><TD align=\"center\" title='07:48/52/52/56 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">16:09</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAJ</TD><TD title=''style=''>R38</TD><TD title=''style=''>31</TD><TD title=''style=''>JSM</TD><TD title=''style=''>C1</TD><TD>203</TD><TD>CCP</TD><TD>07:10</TD><TD title='1.)2012-01-25 07:20:27 => 07:10->07:11 &#13;&#10;2.)2012-01-25 10:56:31 => 07:11->07:10 &#13;&#10;'style='background-color : #FFFF99;color : #000000;font-weight :bold;;'>07:10</TD><TD title=''style=''>00/102</TD><TD></TD><td id=\"obs\" align=\"center\" title='6:00' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "<TR><TD title='' style=''>CC-BDB</TD><TD title=''style=''>P14</TD><TD title='1.)2012-01-25 05:39:57 => GRE->X &#13;&#10;2.)2012-01-25 05:53:44 => X->NRR &#13;&#10;'style='background-color : #FFFF99;color : #000000;font-weight :bold;'>NRR</TD><TD>621</TD><TD>MEX</TD><TD id=\"itin\">06:50</TD><TD title='1.)2012-01-25 07:16:44 => 07:47->07:43 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>07:43</TD><TD align=\"center\" title='07:45/50/50/52 (1)' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td><TD id=\"ta\">13:07</TD> <TD style=\"border: none\"></TD><TD title='' style=''>CC-BAK</TD><TD title='1.)2012-01-25 03:33:45 => R32->R33 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>R33</TD><TD title=''style=''>30</TD><TD title='1.)2012-01-25 06:11:58 => MQM->LAG &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;'>LAG</TD><TD title=''style=''>C5</TD><TD>316</TD><TD>CPO</TD><TD>07:15</TD><TD title='1.)2012-01-25 07:28:31 => 07:15->07:13 &#13;&#10;'style='background-color : #CCFFCC; color : #000000; font-weight :bold;;'>07:13</TD><TD title=''style=''>00/122</TD><TD></TD><td id=\"obs\" align=\"center\" title='0:28' style=''><img src=\"images/obs-escrito.png\" height=\"100%\"></td></TR>\n"
				+ "</TBODY>";
</code>
		 */
		return out;
	}
}
