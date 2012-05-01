<%@page import="java.sql.Timestamp,java.text.SimpleDateFormat,java.text.DateFormat,cl.buildersoft.util.DataAccessUtil,java.sql.Connection,java.sql.ResultSet,java.util.Calendar,java.util.Date"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%
	response.setDateHeader("Date", new java.util.Date().getTime());
	response.setDateHeader("Expires", 0);
	response.setHeader("Cache-Control",
			"no-cache, must-revalidate, s-maxage=0, proxy-revalidate, private");
	response.setHeader("Pragma", "no-cache");

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
	String max = null;
	String processTime = null;
	if (rs.next()) {
		max = rs.getString("cMax");
		if (max != null) {
			processTime = dau.calendar2String(dau.date2Calendar(rs
					.getTimestamp("cProcessTime")));
		}
	}
	dau.closeSQL(rs);

	sql = "SELECT r.MAT, t.cTypePlain, r.ST, r.VUELO, cLlegadaIti, cSalidaIti, ";
	sql += "cLlegadaConf, ";
	sql += "cSalidaConf, ";
	sql += "TIME_FORMAT(SEC_TO_TIME(r.cTransito*60), '%H:%i') AS cTransito, ";
	sql += "a.cName AS cAtencion, ";
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
<LINK rel="stylesheet" type="text/css"
	href="${pageContext.request.contextPath}/css/default.css?<%=Math.random() %>>" />

<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<meta http-equiv="refresh" content="<%=refreshTime%>;">
<title>Contingencia de Soporte Andes (<%=max%>)</title>
<script src="${pageContext.request.contextPath}/js/jquery-1.7.1.js">
</script>

<script>
var counter = <%=refreshTime%>;

function onLoad(){
	setInterval("refreshCounter()", 1000);
}

function refreshCounter(){
	counter--;
	document.getElementById("updateLabel").innerHTML = counter;
}

function hideRow(row, mat, vuelo){
	var data = 'Mat=' + mat + '&Vuelo=' + vuelo;
	$.ajax({url:'${pageContext.request.contextPath}/servlet/MarkFly', 
		cache:false,
		type:'post',
		data: data});
	
	
	$(row).hide('slow');
	
}
</script>
</head>
<body onload="javascript:onLoad();">

	<h1 class="cTitle">Soporte Andes</h1>
	
<!-- 
<textarea rows="10" cols="10"><%=sql%></textarea>
 -->

	<table border="0">
		<tr>
			<td class="cLabel">Itinerario:</td>
			<td class="cData"><%=max != null ? processTime : ""%></td>
		</tr>
		<tr>
			<td class="cLabel">Actualización:</td>
			<td id="updateLabel" class="cData"><%=refreshTime%></td>
		</tr>
	</table>

	<table class="cList" border="1" width="80%" cellpadding="0"
		cellspacing="0">
		<thead>
			<tr>
				<td colspan="4" class="cHeadTD"></td>
				<td colspan="2" class="cHeadTD" align="center">Llegada</td>
				<td colspan="2" align="center" class="cHeadTD">Salida</td>
				<td colspan="4" class="cHeadTD"></td>
			</tr>
			<tr>
				<td class="cHeadTD">MAT</td>
				<td class="cHeadTD">Tipo Avn</td>
				<td class="cHeadTD">ST</td>
				<td class="cHeadTD">Vuelo</td>
				<td class="cHeadTD">Confirmado</td>
				<td class="cHeadTD">Itinirario</td>
				<td class="cHeadTD">Confirmado</td>
				<td class="cHeadTD">Itinirario</td>
				<td class="cHeadTD">Tiempo llegada</td>
				<td class="cHeadTD">Transito</td>
				<td class="cHeadTD">Atención</td>
				<td class="cHeadTD" align="center">Acc</td>
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
				Timestamp llegadaConf = null;
				Timestamp llegadaIti = null;
				Timestamp salidaConf = null;
				Timestamp salidaIti = null;
				String transito = null;
				String atencion = null;
				Integer minutes = null;
				if (max != null) {
					while (rs.next()) {
						mat = rs.getString("MAT");
						typePlain = rs.getString("cTypePlain");
						st = rs.getString("ST");
						vuelo = rs.getString("VUELO");
						llegadaConf = rs.getTimestamp("cLlegadaConf");
						llegadaIti = rs.getTimestamp("cLlegadaIti");

						salidaConf = rs.getTimestamp("cSalidaConf");
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
						} else if (minutes > 30) {
							backColor = "geen";
							fontColor = "";
						}
			%>

			<tr bgcolor="<%=backColor%>" style="color:<%=fontColor%>">
				<td class="cDataTD"><%=mat%></td>
				<td class="cDataTD"><%=typePlain%></td>
				<td class="cDataTD"><%=st%></td>
				<td class="cDataTD"><%=vuelo%></td>
				<td class="cDataTD"><%=formatDate(llegadaConf, "HH:mm (dd-MM)")%></td>
				<td class="cDataTD"><%=formatDate(llegadaIti, "HH:mm (dd-MM)")%></td>
				<td class="cDataTD"><%=formatDate(salidaConf, "HH:mm (dd-MM)")%></td>
				<td class="cDataTD"><%=formatDate(salidaIti, "HH:mm (dd-MM)")%></td>
				<td class="cDataTD"><%=minutes%></td>
				<td class="cDataTD"><%=transito%></td>
				<td class="cDataTD"><%=atencion%></td>
				<%
					if (minutes < 0) {
								String js = "javascript:hideRow(this.parentNode.parentNode, '" + mat + "', '"
										+ vuelo + "');";
				%>
				<td class="cDataTD" align="center" style="cursor: pointer;"><img
					height="15px" src="${pageContext.request.contextPath}/img/ok.jpg" onclick="<%=js%>"></td>
				<%
					} else {
				%>
				<td class="cDataTD">&nbsp;</td>
				<%
					}
				%>
			</tr>

			<%
				}
					dau.closeSQL(rs);
				} else {
			%>
					<tr>
				<td class="cDataTD" colspan="12">No se encontró información</td>
			</tr>
					<%
						}
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