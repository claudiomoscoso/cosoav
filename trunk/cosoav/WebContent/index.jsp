<%@page import="java.sql.Timestamp"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page import="cl.buildersoft.util.DataAccessUtil"%>
<%@page import="java.sql.Connection"%>
<%@page import="java.sql.ResultSet"%>
<%@page import="java.util.Calendar"%>
<%@page import="java.util.Date"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	response.setDateHeader("Date", new java.util.Date().getTime());
	response.setDateHeader("Expires", 0);
	response.setHeader("Cache-Control",
			"no-cache, must-revalidate, s-maxage=0, proxy-revalidate, private");
	response.setHeader("Pragma", "no-cache");
%>
<%
	ServletContext servletContext = getServletContext();
	String refreshTime = servletContext
			.getInitParameter("cosoav.web.refresh");

	DataAccessUtil dau = new DataAccessUtil();
	Connection conn = dau.getConnection(servletContext);

	String sql = "SELECT MAX(r.cDataBrute) AS cMax, db.cProcessTime AS cProcessTime ";
	sql += "FROM tReport r ";
	sql += "LEFT JOIN tDataBrute db ON r.cDataBrute = db.cId ";
	sql += "WHERE r.cStatus = 'DONE';";

	ResultSet rs = dau.queryResultSet(conn, sql, null);
	rs.next();
	String max = rs.getString("cMax");
	String processTime = dau.calendar2String(dau.date2Calendar(rs
			.getTimestamp("cProcessTime")));
	dau.closeSQL(rs);

	sql = "SELECT r.MAT, t.cTypePlain, r.ST, r.VUELO, cLlegadaIti, cSalidaIti, ";
	sql += "CONCAT(TIME_FORMAT(r.cLlegadaConf, '%H:%i'), ' (', DATE_FORMAT(r.cLlegadaConf, '%d-%c'), ')' ) AS cLlegadaConf, ";
	sql += "CONCAT(TIME_FORMAT(r.cSalidaConf, '%H:%i'), ' (', DATE_FORMAT(r.cSalidaConf, '%d-%c'), ')' ) AS cSalidaConf, ";
	//	sql += "r.cSalida, ";
	sql += "TIME_FORMAT(SEC_TO_TIME(r.cTransito*60), '%H:%i') AS cTransito, a.cName AS cAtencion, ";
	sql += "TIMESTAMPDIFF(MINUTE, now(), cLlegadaConf) as cMinutes ";
	sql += "FROM tReport AS r ";
	sql += "LEFT JOIN tPlainType AS pt ON r.MAT = pt.MAT ";
	sql += "LEFT JOIN tTimeConfig AS t ON pt.cTypePlain = t.cTypePlain ";
	sql += "LEFT JOIN tAtention AS a ON a.cId =  ";
	sql += "CASE ";
	sql += "WHEN r.cTransito > t.cMins THEN \"1\" ";
	sql += "ELSE \"2\" ";
	sql += "END ";
	sql += "WHERE r.cDataBrute = ? AND cSalidaConf IS NOT NULL AND DATE_SUB(NOW(), INTERVAL 15 MINUTE) < cLlegadaConf ";
	sql += "ORDER BY cMinutes ASC;";

	rs = dau.queryResultSet(conn, sql, max);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="refresh" content="<%=refreshTime%>;">
<title>Contingencia de Soporte Andes (<%=max%>)</title>
<script>
var counter = <%=refreshTime%>;
function onLoad(){
	setInterval("refreshCounter()", 1000);
}

function refreshCounter(){
	counter--;
	document.getElementById("updateLabel").innerHTML = counter;
}
</script>
</head>
<body onload="onLoad();">

	<h1>Soporte Andes</h1>
	
<!-- 
<textarea rows="10" cols="10"><%=sql%></textarea>
 -->

	<table border="0">
		<tr>
			<td>Itinerario:</td>
			<td><%=processTime%></td>
		</tr>
		<tr>
			<td>Actualización:</td>
			<td id="updateLabel"><%=refreshTime%></td>
			<td><a href="http://code.google.com/p/cosoav/issues/list" target="_blank">Soporte a aplicación</a>
			</td>
		</tr>
	</table>

	<table border="1" width="80%">
		<thead>
			<tr>
				<td colspan="4"></td>
				<td colspan="2" align="center">Llegada</td>
				<td colspan="2" align="center">Salida</td>
				<td colspan="3"></td>
			</tr>
			<tr>
				<td>MAT</td>
				<td>Tipo Avn</td>
				<td>ST</td>
				<td>Vuelo</td>
				<td>Confirmado</td>
				<td>Itinirario</td>
				<td>Confirmado</td>
				<td>Itinirario</td>
				<td>Tiempo llegada</td>
				<td>Transito</td>
				<td>Atención</td>
			</tr>
		</thead>
		<tbody>

			<%
				String backColor = null;
				String fontColor = null;
				String mat = null;
				String typePlain = null;
				String st = null;
				String vuelo = null;
				String llegadaConf = null;
				Timestamp llegadaIti = null;
				String salidaConf = null;
				Timestamp salidaIti = null;
				String transito = null;
				String atencion = null;
				Integer minutes = null;
				while (rs.next()) {
					mat = rs.getString("MAT");
					typePlain = rs.getString("cTypePlain");
					st = rs.getString("ST");
					vuelo = rs.getString("VUELO");
					llegadaConf = rs.getString("cLlegadaConf");
					llegadaIti = rs.getTimestamp("cLlegadaIti");

					salidaConf = rs.getString("cSalidaConf");
					salidaIti = rs.getTimestamp("cSalidaIti");

					transito = rs.getString("cTransito");
					atencion = rs.getString("cAtencion");
					minutes = rs.getInt("cMinutes");

					if (minutes < 0) {
						backColor = "#B8B8B8";
						fontColor = "#505050";
					} else if (minutes >= 0 && minutes <= 15) {
						backColor = "red";
						fontColor = "#FFFF00";
					} else if (minutes > 15 && minutes <= 30) {
						backColor = "orange";
						fontColor = "";
					} else if (minutes > 30){
						backColor = "geen";
						fontColor = "";
					}
			%>

			<tr bgcolor="<%=backColor%>" style="color:<%=fontColor%>">
				<td><%=mat%></td>
				<td><%=typePlain%></td>
				<td><%=st%></td>
				<td><%=vuelo%></td>
				<td><%=llegadaConf%></td>
				<td><%=formatDate(llegadaIti, "HH:mm (dd-MM)")%></td>
				<td><%=salidaConf%></td>
				<td><%=formatDate(salidaIti, "HH:mm (dd-MM)")%></td>
				<td><%=minutes%></td>
				<td><%=transito%></td>
				<td><%=atencion%></td>
			</tr>

			<%
				}
				dau.closeSQL(rs);
			%>
			
		</tbody>
	</table>

</body>
</html>
<%!private String formatDate(Timestamp date, String format) {
		String out = "";
		if (date != null) {
			DateFormat formatter = new SimpleDateFormat(format);
			out = formatter.format((Timestamp) date);
		}
		return out;

	}%>