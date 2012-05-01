package cl.buildersoft.web.servlet;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cl.buildersoft.framework.util.BSDataUtils;

/**
 * Servlet implementation class MarkFly
 */
@WebServlet("/servlet/MarkFly")
public class MarkFly extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public MarkFly() {
		super();
	}

	protected void service(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		/**
		 * <code>
		System.out.println("Mat: " + request.getParameter("Mat"));
		System.out.println("Vuelo: " + request.getParameter("Vuelo"));
</code>
		 */

		String mat = request.getParameter("Mat");
		String vuelo = request.getParameter("Vuelo");

		BSDataUtils du = new BSDataUtils();
		Connection conn = du.getConnection(request.getServletContext());

		String sql = "INSERT INTO tMarkFly(MAT, VUELO) VALUES(?, ?);";
		List<Object> prms = new ArrayList<Object>();
		prms.add(mat);
		prms.add(vuelo);

		du.update(conn, sql, prms);
		du.closeSQL();

	}
}
