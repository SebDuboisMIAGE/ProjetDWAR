package servlets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

import beans.AdresseTAN;
import beans.Donnees;
import beans.EtapeGoogle;
import beans.EtapeTAN;
import beans.GPSCoordonate;
import beans.TrajetGoogle;
import beans.TrajetTAN;

public class ItineraireServlet extends HttpServlet{
	
	public static String prefix_url_geocode = "https://maps.google.com/maps/api/geocode/json";
	public static String prefix_url_direction = "https://maps.googleapis.com/maps/api/directions/json";
	public static String key ="AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA";

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		@SuppressWarnings("unused")
		PrintWriter out = resp.getWriter();	
	
		//Récupération des adresses de la liste déroulante
		String departure = req.getParameter("listedepart");
		String arrivee = req.getParameter("listearrivee");
		
		req.setAttribute("visibility_carte", "visible_carte");
		req.setAttribute("visibility_calculer", "visible_calculer");
		req.setAttribute("visibility_resultat", "visible_resultat");
		
		// Récupération des trajets (Google et Tan)
		try {
			TrajetGoogle trajetGoogleDriving = setTrajetGoogle(getTrajetGoogle(departure, arrivee, "driving"));
			req.setAttribute("TrajetGoogleDriving", trajetGoogleDriving);
			TrajetGoogle trajetGoogleBiking = setTrajetGoogle(getTrajetGoogle(departure, arrivee, "bicycling"));
			req.setAttribute("TrajetGoogleBicycling", trajetGoogleBiking);
			TrajetGoogle trajetGoogleWalking = setTrajetGoogle(getTrajetGoogle(departure, arrivee, "walking"));
			req.setAttribute("TrajetGoogleWalking", trajetGoogleWalking);
			//Faire pareil pour la TAN
		} catch (JSONException e1) {
			// TODO
			e1.printStackTrace();
		}		
		
		// Renvoit des données dans la page JSP
		this.getServletContext().getRequestDispatcher("/index.jsp").forward(req,resp);

	};	
	
	 /*
		--	GOOGLE  --
	 * Fonction permettant de récupérer toutes les informations d'un itinéraire
	 * - Départ - Arrivée - Durée - Distance - Steps (PointItineraire) : -
	 * Départ - Arrivée - Durée - Distance - Consigne
	 */
	public JSONObject getTrajetGoogle(String departure, String arrivee, String transport)
			throws IOException, JSONException {
		String url_build = prefix_url_direction + "?origin="
				+ URLEncoder.encode(departure, "UTF-8") + "&destination="
				+ URLEncoder.encode(arrivee, "UTF-8") 
				+ "&mode=" + URLEncoder.encode(transport, "UTF-8")
				+ "&language=FR&sensor=false&key="
				+ key;
		URL url = new URL(url_build);
		// read from the URL
		Scanner scan = new Scanner(url.openStream());
		String str = new String();
		while (scan.hasNext())
			str += scan.nextLine();
		scan.close();
	
		// build a JSON object
		JSONObject obj = new JSONObject(str);
		return obj;
	}
	
	/*
	 * Fonction permettant de découper un fichier JSON et de créer un objet TrajetGoogle
	 */
	public TrajetGoogle setTrajetGoogle(JSONObject obj) throws JSONException {
		// get the first result
		JSONObject res = obj.getJSONArray("routes").getJSONObject(0);
		JSONObject info = res.getJSONArray("legs").getJSONObject(0);
	
		TrajetGoogle itineraire = new TrajetGoogle();
		itineraire.setDistance(info.getJSONObject("distance")
				.getString("text"));
		itineraire.setDuration(info.getJSONObject("duration")
				.getString("text"));
		itineraire.setDeparture(info.getString("start_address"));
		itineraire.setArrival(info.getString("end_address"));
		
		// set GPS departure and arrival
		GPSCoordonate departGPS = new GPSCoordonate();
		departGPS.setLat(info.getJSONObject("start_location").getDouble("lat"));
		departGPS.setLng(info.getJSONObject("start_location").getDouble("lng"));
		itineraire.setDepartureGPS(departGPS);
		GPSCoordonate arriveeGPS = new GPSCoordonate();
		arriveeGPS.setLat(info.getJSONObject("end_location").getDouble("lat"));
		arriveeGPS.setLng(info.getJSONObject("end_location").getDouble("lng"));
		itineraire.setArrivalGPS(arriveeGPS);
				
		// set Etapes
		JSONArray list = info.getJSONArray("steps");		
		for (int i = 0; i < list.length(); ++i) {
			EtapeGoogle step = new EtapeGoogle();
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
			step.setWay(decodePoly(item.getJSONObject("polyline").getString("points")));
			itineraire.setSteps(step);
		}
		return itineraire;
	}
	
	/*
	 * Fonction permettant de récupérer un fichier JSON représentant le trajet TAN 
	 * d'une adresse à une autre
	 * Renvoit un JSON
	 */
	public JSONObject getTrajetTAN(AdresseTAN origine, AdresseTAN destination) throws Exception
	{
		String urlParameters = "depart=" + URLEncoder.encode(origine.getIdTAN(), "UTF-8") + "&arrive=" + URLEncoder.encode(destination.getIdTAN(), "UTF-8") + "&type=0&accessible=0&temps=" + URLEncoder.encode("2014-05-13 17:00","UTF-8") + "&retour=0"
				+ "\"";
		URL url;
		HttpURLConnection connection = null;
		
			// Create connection
			url = new URL(
					"https://www.tan.fr/ewp/mhv.php/itineraire/resultat.json");

			// set connection properties
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");

			connection.setRequestProperty("Content-Length",
					"" + Integer.toString(urlParameters.getBytes().length));
			connection.setRequestProperty("Content-Language", "en-US");
			connection.setConnectTimeout(60000);// 60 s
			connection.setReadTimeout(60000);// 60s
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(
					connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			StringBuffer response = new StringBuffer();
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			JSONObject itineraire = new JSONArray(response.toString()).getJSONObject(0);
			return itineraire;
	}
	
	/*
	 * Fonction permettant de créer un objet itinéraire TAN en fonction du Fichier JSON
	 * passé en paramètre
	 */
	public TrajetTAN setTrajetTAN(JSONObject obj) throws JSONException {
		TrajetTAN itineraire = new TrajetTAN();
		// get the first result
		itineraire.setDeparture(obj.getString("adresseDepart"));
		itineraire.setArrival(obj.getString("adresseArrivee"));
		itineraire.setHeureDepart(obj.getString("heureDepart"));
		itineraire.setHeureArrivee(obj.getString("heureArrivee"));
		itineraire.setDuration(obj.getString("duree"));
		itineraire.setCorrespondance(obj.getString("correspondance"));
		
		JSONArray etapes = obj.getJSONArray("etapes");
		
		//Récupération des étapes du trajet TAN
		for (int i = 0; i < etapes.length(); ++i) {
			EtapeTAN step = new EtapeTAN();
			JSONObject etape = etapes.getJSONObject(i);
			if (etape.getString("marche") == "True"){
				step.setMarche(true);
			}
			else{
				step.setMarche(false);
			}
			JSONObject ligne = etape.getJSONObject("ligne");
			step.setNumligne(ligne.getString("numLigne"));
			JSONObject arretStop = etape.getJSONObject("arretStop");
			step.setLibelleArret(arretStop.getString("libelle"));
			step.setHeureDepart(etape.getString("heureDepart"));
			step.setHeureArrivee(etape.getString("heureArrivee"));
			step.setDuree(etape.getString("duree"));

			itineraire.setSteps(step);
		}
		return itineraire;
	}
	
	/*
	 * Permet de décoder les directions  Google
	 */
	public static ArrayList<GPSCoordonate> decodePoly(String encoded) {
		  ArrayList<GPSCoordonate> poly = new ArrayList<GPSCoordonate>();
		  int index = 0, len = encoded.length();
		  int lat = 0, lng = 0;
		  while (index < len) {
		   int b, shift = 0, result = 0;
		   do {
		    b = encoded.charAt(index++) - 63;
		    result |= (b & 0x1f) << shift;
		    shift += 5;
		   } while (b >= 0x20);
		   int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		   lat += dlat;
		   shift = 0;
		   result = 0;
		   do {
		    b = encoded.charAt(index++) - 63;
		    result |= (b & 0x1f) << shift;
		    shift += 5;
		   } while (b >= 0x20);
		   int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
		   lng += dlng;
		   GPSCoordonate p = new GPSCoordonate();
		   p.setLat(((double) lat / 1E5));
		   p.setLng(((double) lng / 1E5));
		   poly.add(p);
		  }
		  return poly;
	 }
	
}
