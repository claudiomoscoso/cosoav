package cosoav.utils;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SendMessage {

	public void process(String subjet, String body) {
		if (subjet.length() > 100) {
			subjet = subjet.substring(0, 100) + "...";
		}

		Properties props = new Properties();

		String host = System.getProperty("cosoav.mail.server");

		props.put("mail.smtp.host", host);
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "465");

		Session session = Session.getDefaultInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						String user = System.getProperty("cosoav.mail.user");
						String password = System
								.getProperty("cosoav.mail.password");
						return new PasswordAuthentication(user, password);
					}
				});

		try {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("no-reply@no-spam.com"));

			setDestiny(message);

			message.setSubject("COSOAV : " + subjet);
			message.setText(body);

//			Transport.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}

	}

	private void setDestiny(Message message) throws MessagingException,
			AddressException {

		String[] destinations = System.getProperty("cosoav.mail.destiny")
				.split(",");
		Address[] addresses = new InternetAddress[destinations.length];
		int i = 0;
		for (String destiny : destinations) {
			addresses[i] = new InternetAddress(destiny.trim()); // InternetAddress.parse(destiny);
			i++;
		}
		message.setRecipients(Message.RecipientType.TO, addresses);

	}
	/**
	 * <code>
	public void process1(String msg) {
		String host = "smtp.gmail.com";
		int port = 587;
		String username = "claudio.moscoso";
		String password = "teamomuchoverito";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");

		Session session = Session.getInstance(props);

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("claudio.moscoso@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse("claudio.moscoso@gmail.com"));
			message.setSubject(msg);
			message.setText("Dear Mail Crawler,"
					+ "\n\n No spam to my email, please!");

			Transport transport = session.getTransport("smtp");
			transport.connect(host, port, username, password);

			Transport.send(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}</code>
	 */
}
