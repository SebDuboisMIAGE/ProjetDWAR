package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.AdresseTAN;
import beans.Donnees;

public class ItineraireServlet extends HttpServlet{

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		@SuppressWarnings("unused")
		PrintWriter out = resp.getWriter();	
		
		//Lecture des choix d'adresse
		System.out.println(req.getParameter("listedepart"));
		System.out.println(req.getParameter("listearrivee"));
		
		/*
		// Récupération des trajets (Google et Tan)
		try {
			TrajetGoogle trajetGoogleDriving = setTrajetGoogle(getTrajetGoogle(departure, arrivee, "driving"));
			req.setAttribute("TrajetGoogleDriving", trajetGoogleDriving);
			TrajetGoogle trajetGoogleBiking = setTrajetGoogle(getTrajetGoogle(departure, arrivee, "bicycling"));
			req.setAttribute("TrajetGoogleBicycling", trajetGoogleBiking);
			TrajetGoogle trajetGoogleWalking = setTrajetGoogle(getTrajetGoogle(departure, arrivee, "walking"));
			req.setAttribute("TrajetGoogleWalking", trajetGoogleWalking);
		} catch (JSONException e1) {
			// TODO
			e1.printStackTrace();
		}		
		*/
		// Renvoit des données dans la page JSP
		//this.getServletContext().getRequestDispatcher("/index.jsp").forward(req,resp);

	};
	
}
