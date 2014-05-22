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

import beans.AdresseTAN;
import beans.Donnees;
import beans.EtapeTAN;
import beans.GPSCoordonate;
import beans.TrajetGoogle;
import beans.EtapeGoogle;
import beans.TrajetTAN;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class ProjetServlet extends HttpServlet {

	public static String prefix_url_geocode = "https://maps.google.com/maps/api/geocode/json";
	public static String prefix_url_direction = "https://maps.googleapis.com/maps/api/directions/json";
	public static String key ="AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA";

	

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		this.getServletContext().getRequestDispatcher("/index.jsp").forward(req,resp);	 
	}
		
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		@SuppressWarnings("unused")
		PrintWriter out = resp.getWriter();
		
		// Récupération des informations saisies par l'utilisateur
		String departure = req.getParameter("from");
		String arrivee = req.getParameter("to");
		String cost = req.getParameter("cost");
		if(cost == null){cost = "";}
		String time = req.getParameter("time");
		if(time == null){time = "";}
		String ecolo = req.getParameter("ecolo");
		if(ecolo == null){ecolo = "";}
		String calorie = req.getParameter("calorie");
		if(calorie == null){calorie = "";}
		
		Donnees data = new Donnees();
		data.setArrival(arrivee); 
		data.setDeparture(departure);
		data.setCost(cost.equals("") ? -1 : Integer.parseInt(cost));
		data.setTime(time.equals("") ? -1 : Integer.parseInt(time));
		data.setEcological(ecolo.equals("") ? -1 : Integer.parseInt(ecolo));
		data.setCalorie(calorie.equals("") ? -1 : Integer.parseInt(calorie));
		
		req.setAttribute("donnees", data);
		req.setAttribute("carte", "carte");
				
		// Requete TAN
		try {
			//Récupération des listes d'adresses de départ et d'arrivee via la TAN
			List<AdresseTAN> listeChoixDepart = getChoixAdresseTAN(getAdresseTaN(departure));
			req.setAttribute("ListeDepart", listeChoixDepart);
			List<AdresseTAN> listeChoixArrivee = getChoixAdresseTAN(getAdresseTaN(arrivee));
			req.setAttribute("ListeArrivee", listeChoixArrivee);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		//Lecture des choix d'adresse
		System.out.println(req.getParameter("listedepart"));
		
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
		this.getServletContext().getRequestDispatcher("/index.jsp").forward(req,resp);

	};

	/*
	 * Fonction permettant de renvoyer les coordonnées GPS d'une adresse -
	 * Latitude - Longitude
	 */
	public GPSCoordonate getJSONwithAdress(String adress) {
		try {
			String url_build = prefix_url_geocode
					+ "?address="
					+ URLEncoder.encode(adress, "UTF-8")
					+ "&sensor=false&key="
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

			// get the first result
			JSONObject res = obj.getJSONArray("results").getJSONObject(0);
			JSONObject loc = res.getJSONObject("geometry").getJSONObject(
					"location");

			GPSCoordonate coord = new GPSCoordonate();
			coord.setLat(loc.getDouble("lat"));
			coord.setLng(loc.getDouble("lng"));

			return coord;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

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
* 		--  TAN  --
*/
	
	/*
	 * Récupère les adresses similaires à l'adresse en paramètre 
	 * Renvoit un fichier JSON
	 */
	public JSONObject getAdresseTaN(String addresse) throws Exception {
		
		String urlParameters = "nom=" + URLEncoder.encode(addresse, "UTF-8")
				+ "\"";
		URL url;
		HttpURLConnection connection = null;
		
			// Create connection
			url = new URL(
					"https://www.tan.fr/ewp/mhv.php/itineraire/address.json");

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
			JSONObject adresse = new JSONArray(response.toString()).getJSONObject(0);
			return adresse;
	}	
	
	/*
	 * Créé une liste d'adresse TAN à partir d'un fichier JSON
	 */
	public List<AdresseTAN> getChoixAdresseTAN(JSONObject obj){
		List<AdresseTAN> liste = new ArrayList<AdresseTAN>();
		// get the first result
		JSONArray list;
		try {
			list = obj.getJSONArray("lieux");
			// Lecture de toutes les adresses trouvees
			for (int i = 0; i < list.length(); ++i) {
				AdresseTAN adresse = new AdresseTAN();
				JSONObject item = list.getJSONObject(i);
				adresse.setAdresse(item.getString("nom")+' '+item.getString("cp")+' '+item.getString("ville"));
				adresse.setIdTAN(item.getString("id"));
				liste.add(adresse);
			}
		} catch (JSONException e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
		}
		
		return liste;
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
