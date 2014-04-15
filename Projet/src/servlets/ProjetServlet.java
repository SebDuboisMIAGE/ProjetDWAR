package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import beans.GPSCoordonate;
import beans.Itineraire;
import beans.PointItineraire;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class ProjetServlet extends HttpServlet {
	
	public static String prefix_url_geocode = "https://maps.google.com/maps/api/geocode/json";
	public static String prefix_url_direction = "https://maps.googleapis.com/maps/api/directions/json";
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
	
		// Récupération des saisies dans le HTML
		String departure = req.getParameter("from");
		String arrivee = req.getParameter("to");
		
		// Récupération de l'itinéraire
		Itineraire itineraire = getItineraire(departure, arrivee);

		// Vérification de la disponibilité des adresses de la TAN
		System.out.println(itineraire.getArrivee());
	    
	};

/*
 * Fonction permettant de renvoyer les coordonnées GPS d'une adresse
 * - Latitude
 * - Longitude
 */
public GPSCoordonate getJSONwithAdress(String adress){
	try{
		String url_build = prefix_url_geocode + "?address=" + URLEncoder.encode(adress, "UTF-8") + "&sensor=false&key=AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA";
		URL url = new URL(url_build);
		// read from the URL
	    Scanner scan = new Scanner(url.openStream());
	    String str = new String();
	    while (scan.hasNext())
	        str += scan.nextLine();
	    scan.close();
	 
	    // build a JSON object
	    JSONObject obj = new JSONObject(str);
	 
	    // get the first result
	    JSONObject res = obj.getJSONArray("results").getJSONObject(0);
	    JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");
	    
	    GPSCoordonate coord = new GPSCoordonate();
	    coord.setLat(loc.getDouble("lat"));
	    coord.setLng(loc.getDouble("lng"));
	    	    
	    return coord;
	}
	catch(Exception e){
		e.printStackTrace();
		return null;
	}	
}

/*
 * Fonction permettant de récupérer toutes les informations d'un itinéraire
 * - Départ
 * - Arrivée
 * - Durée
 * - Distance
 * - Steps (PointItineraire) :	- Départ
 * 								- Arrivée
 * 								- Durée
 * 								- Distance
 * 								- Consigne
 */
public Itineraire getItineraire(String departure, String arrivee){
	try{
		String url_build = prefix_url_direction + "?origin=" + URLEncoder.encode(departure, "UTF-8") + "&destination=" + URLEncoder.encode(arrivee, "UTF-8") + "&sensor=false&key=AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA";
		URL url = new URL(url_build);
		// read from the URL
		System.out.println(url_build);
	    Scanner scan = new Scanner(url.openStream());
	    String str = new String();
	    while (scan.hasNext())
	        str += scan.nextLine();
	    scan.close();
	 
	    // build a JSON object
	    JSONObject obj = new JSONObject(str);
	 
	    // get the first result
	    JSONObject res = obj.getJSONArray("routes").getJSONObject(0);
	    JSONObject info = res.getJSONArray("legs").getJSONObject(0);	    
	    
	    Itineraire itineraire = new Itineraire();
	    itineraire.setDistance(info.getJSONObject("distance").getDouble("value"));
	    itineraire.setDistance(info.getJSONObject("duration").getDouble("value"));
	    itineraire.setDeparture(info.getString("start_address"));
	    itineraire.setArrivee(info.getString("end_location"));
	    JSONArray list =info.getJSONArray("steps");
	    PointItineraire step = new PointItineraire();
	    for (int i = 0; i < list.length(); ++i) {
	        JSONObject item = list.getJSONObject(i);
	        step.setDistance(item.getJSONObject("distance").getDouble("value"));
	        step.setDuree(item.getJSONObject("duration").getDouble("value"));
	        GPSCoordonate depart = new GPSCoordonate();
	        depart.setLat(item.getJSONObject("start_location").getDouble("lat"));
	        depart.setLng(item.getJSONObject("start_location").getDouble("lng"));
	        GPSCoordonate arrive = new GPSCoordonate();
	        arrive.setLat(item.getJSONObject("end_location").getDouble("lat"));
	        arrive.setLng(item.getJSONObject("end_location").getDouble("lng"));
	        step.setDepart(depart);
	        step.setArrivee(arrive);
	        step.setConsigne(item.getString("html_instructions"));
	        itineraire.setSteps(step);
	    }	    
	    return itineraire;
	}
	catch(Exception e){
		e.printStackTrace();
		return null;
	}	
}
}
