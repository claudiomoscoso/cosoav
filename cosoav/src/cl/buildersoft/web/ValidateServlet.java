package cl.buildersoft.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cl.buildersoft.beans.User;
import cl.buildersoft.util.DataAccessUtil;

/**
 * Servlet implementation class ValidateServlet
 */

@WebServlet(urlPatterns="/login/ValidateServlet")
public class ValidateServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4481703270849068766L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ValidateServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		DataAccessUtil dau = new DataAccessUtil();
		Connection conn = null;
		try {
			conn = dau.getConnection(getServletContext(), "bsframework");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		// BSDataUtils dau = new BSDataUtils();
		// Connection conn = dau.getConnection();

		Integer id = Integer.parseInt(request.getParameter("user"));

		User user = new User();
		user.setConnection(conn);
		user.setId(id);

		Boolean found = null;
		try {
			found = user.search();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String page = null;

		if (found) {
			page = "/login/ok.jsp";
		} else {
			page = "/login/not-found.jsp";

		}

		request.getRequestDispatcher(page).forward(request, response);
	}

}
