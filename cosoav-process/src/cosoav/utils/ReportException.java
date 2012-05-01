package cosoav.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.List;

public class ReportException extends AbstractProcess {
	public void process(Exception e) {
		process(e, null, null);
	}

	public void process(Exception e, String html, List<Object> prms) {
		String body = prepareMessage(html, e, prms);

		toConsole(body);
		toFile(body);
		toMail(e.getMessage(), body);

	}

	private void toMail(String subject, String body) {
		SendMessage sm = new SendMessage();
		sm.process(subject, body);

	}

	private void toFile(String body) {
		String path = System.getProperty("cosoav.log.path");
		if (!path.endsWith("\\")) {
			path += "\\";
		}

		FileOutputStream file = null;
		String fileName = path + "log.txt";
		try {
			file = new FileOutputStream(fileName, true);
			PrintStream stream = new PrintStream(file);
			stream.print(body);
			stream.close();
		} catch (FileNotFoundException e) {
			System.err.println("\n(X)Can't find file \"" + fileName + "\"\n");
		} finally {
			file = null;
		}

	}

	private void toConsole(String s) {
		System.out.println(s);
	}

	private String prepareMessage(String html, Exception e, List<Object> prms) {
		String out = getHeaderMessage();

		out += getParams(prms);
		out += exception2String(e);
		out += prepareHtml(html);

		return out;
	}

	private String prepareHtml(String html) {
		String out = "";
		if (html != null) {
			out += "\nBEGIN HTML\n";
			out += html;
			out += "\nEND HTML\n";
		}
		return out;
	}

	private String getParams(List<Object> prms) {
		String out = "";
		if (prms != null) {
			out = "Parameters:\n";
			for (Object o : prms) {
				out += "[" + o + "]\n";
			}
			out += "\n\n";
		}
		return out;
	}

	private String getHeaderMessage() {
		return "-------------------------------\n"
				+ calendar2String(Calendar.getInstance()) + "\n";
	}

	private String exception2String(Exception e) {
		String out = e.getMessage() + "\n";
		if (e != null) {
			StackTraceElement[] stackTrace = e.getStackTrace();
			StackTraceElement stackTraceElement = null;
			int stackTraceLength = stackTrace.length;
			StringBuffer stringBuffer = new StringBuffer(1024);

			for (int i = 0; i < stackTraceLength; i++) {
				stackTraceElement = stackTrace[i];
				stringBuffer.append(stackTraceElement.getClassName() + " ("
						+ stackTraceElement.getFileName() + ") line "
						+ stackTraceElement.getLineNumber() + " method "
						+ stackTraceElement.getMethodName() + "\n");
			}
			out = stringBuffer.toString();
		}
		return out;
	}

}
