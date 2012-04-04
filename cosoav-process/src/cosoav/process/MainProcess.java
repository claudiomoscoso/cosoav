package cosoav.process;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import cosoav.utils.AbstractProcess;

public class MainProcess extends AbstractProcess {
	public static void main(String[] args) {

		MainProcess mp = new MainProcess();
		mp.process(args);
		mp = null;

	}

	public void process(String[] args) {
		long ini = System.currentTimeMillis();
		String fileProperties = validParameter(args);

		try {
			readProperties(fileProperties);
		} catch (Exception e) {
			reportException(e);
			System.exit(-1);
		}

		try {
			validMailConnect();
		} catch (MessagingException e) {
			reportException(
					e,
					null,
					array2List(
							"cosoav.mail.server="
									+ System.getProperty("cosoav.mail.server"),
							"cosoav.mail.user="
									+ System.getProperty("cosoav.mail.user"),
							"cosoav.mail.password="
									+ System.getProperty("cosoav.mail.password")));
			System.exit(-2);
		}

		Connection conn = null;

		try {
			conn = dataBaseConnection();
		} catch (Exception e) {
			reportException(
					e,
					null,
					array2List(
							"cosoav.database.driver="
									+ System.getProperty("cosoav.database.driver"),
							"cosoav.database.server="
									+ System.getProperty("cosoav.database.server"),
							"cosoav.database.database="
									+ System.getProperty("cosoav.database.database"),
							"cosoav.database.username="
									+ System.getProperty("cosoav.database.username"),
							"cosoav.database.password="
									+ System.getProperty("cosoav.database.password")));
			System.exit(-3);
		}

		ReadUrl readUrl = new ReadUrl();
		ProcessHtml processHtml = new ProcessHtml();
		BuildData buildData = new BuildData();
		BuildReport buildReport = new BuildReport();

		String url = getUrl();
		String html = null;
		try {
			html = readUrl.process(conn, url);
		} catch (Exception e) {
			reportException(e, url);
		}

		try {
			processHtml.process(conn);
			buildData.process(conn);
			buildReport.process(conn);
			saveTimeReport(conn, System.currentTimeMillis() - ini);
			conn.close();
		} catch (Exception e) {
			reportException(e, html);
		}

	}

	private Connection dataBaseConnection() throws ClassNotFoundException,
			SQLException {
		String driverName = System.getProperty("cosoav.database.driver");
		String serverName = System.getProperty("cosoav.database.server");
		String database = System.getProperty("cosoav.database.database");
		String username = System.getProperty("cosoav.database.username");
		String password = System.getProperty("cosoav.database.password");

		return getConnection(driverName, serverName, database, password,
				username);
	}

	private String getUrl() {
		return System.getProperty("cosoav.url");
	}

	private void validMailConnect() throws MessagingException {
		String enable = System.getProperty("cosoav.mail.enable");
		if (Boolean.parseBoolean(enable)) {
//			int port = 587;
//			String host = System.getProperty("cosoav.mail.server");
//			String user = System.getProperty("cosoav.mail.user");
//			String pwd = System.getProperty("cosoav.mail.password");

			Properties props = new Properties();
			// required for gmail
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.auth", "true");
			// or use getDefaultInstance instance if desired...
			Session session = Session.getInstance(props, null);
			Transport transport = session.getTransport("smtp");
			// transport.connect(host, port, user, pwd);
			transport.close();
		}
	}

	private void validMailConnect2() {
		Properties props = new Properties();
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						String user = "claudio.moscoso";
						String password = "mascota123";

						user = System.getProperty("cosoav.mail.user");
						password = System.getProperty("cosoav.mail.password");

						return new PasswordAuthentication(user, password);
					}
				});

		Message message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress("no-reply@no-spam.com"));

			// setDestiny(message);

			message.setSubject("COSOAV");
			message.setText("");

			// Transport.send(message);
		} catch (AddressException e) {

			e.printStackTrace();
		} catch (MessagingException e) {

			e.printStackTrace();
		}
	}

	private void saveTimeReport(Connection conn, long time) throws SQLException {
		String sql = "INSERT INTO tReportStatus(cDate, cProcessTime) VALUES(?,?)";
		List<Object> params = new ArrayList<Object>();

		params.add(new Date());
		params.add(time);

		this.update(conn, sql, params);
		this.closeSQL();
	}

	private void readProperties(String file) throws FileNotFoundException,
			IOException {
		Properties prps = new Properties();

		prps.load(new FileInputStream(file));

		Enumeration names = prps.propertyNames();

		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			String value = prps.getProperty(name);
			System.setProperty(name, value);
			System.out.println(name + "=" + value);
		}
	}

	private String validParameter(String[] args) {
		String out = null;
		if (args.length >= 1) {
			out = args[0];
		} else {
			String msg = "Debe ejecutar indicando como parametro el archivo de propiedades\n"
					+ "por ejemplo: \njava -cp ./cosoav-process-v0.1.jar;./mysql-connector-java-5.1.18-bin.jar;./dom4j-1.6.1.jar;. ReadUrl D:\\temp\\BS\\airport\\cosoav.properties";
			System.out.println(msg);
			throw new RuntimeException(msg);
		}

		return out;
	}

	/**
	 * <code>
	private void listProperties() {
		Properties props = System.getProperties();
		Enumeration<?> enume = props.propertyNames();
		for (; enume.hasMoreElements();) {
			String propName = (String) enume.nextElement();

			String propValue = (String) props.get(propName);
		}

	}
</code>
	 * 
	 * @param driverName
	 * @param serverName
	 * @param database
	 * @param password
	 * @param username
	 * 
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */

}
