package app;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TraitementServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		String id = req.getParameter("from");
		resp.setContentType("text/plain");
		resp.getWriter().println(id);
		resp.getWriter().println("test");
	}
}
