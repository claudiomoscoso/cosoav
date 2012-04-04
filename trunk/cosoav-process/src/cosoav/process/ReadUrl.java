package cosoav.process;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cosoav.utils.AbstractProcess;

public class ReadUrl extends AbstractProcess {
	public String process(Connection conn, String urlString)
			throws SQLException, IOException {
		String urlContent = readFromUrl(urlString);
		if (urlContent != null) {
			saveToDataBase(urlContent, conn);
		}

		return urlContent;
	}

	private void saveToDataBase(String urlContent, Connection conn)
			throws SQLException {

		// Connection conn = getConnection();
		if (conn != null) {
			String sql = "INSERT tDataBrute SET cHTML=?, cProcessTime=?;";

			List<Object> parameter = new ArrayList<Object>();
			parameter.add(urlContent);
			parameter.add(new Date());

			this.update(conn, sql, parameter);

		}

	}

	public String readFromUrl(String urlString) throws IOException {
		String out = null;

		URL url = new URL(urlString);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String str;
		out = "";
		while ((str = in.readLine()) != null) {
			out += str + "\n";
		}
		in.close();

		return out;
	}
}
