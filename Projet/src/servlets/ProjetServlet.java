package servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.servlet.ServletException;
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

import beans.Donnees;
import beans.GPSCoordonate;
import beans.TrajetGoogle;
import beans.EtapeGoogle;
import beans.TrajetTAN;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class ProjetServlet extends HttpServlet {

	public static String prefix_url_geocode = "https://maps.google.com/maps/api/geocode/json";
	public static String prefix_url_direction = "https://maps.googleapis.com/maps/api/directions/json";
	public static String key ="AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA";

	/*
	 * public void doGet(HttpServletRequest req, HttpServletResponse resp)
	 * throws ServletException, IOException {
	 * this.getServletContext().getRequestDispatcher("/projet").forward(req,
	 * resp);
	 * 
	 * }
	 */

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();

		// Récupération des informations saisies par l'utilisateur
		String departure = req.getParameter("from");
		String arrivee = req.getParameter("to");
		
		Donnees data = new Donnees();
		data.setArrival(arrivee);
		data.setDeparture(departure);
		
		System.out.println(data.getArrival());
		System.out.println(data.getDeparture());
		
		req.setAttribute("donnees", data);
		req.setAttribute("carte", "carte");

		// Récupération du trajet Google
		try {
			TrajetGoogle trajetGoogle = setTrajetGoogle(getTrajetGoogle(departure, arrivee));
			System.out.println("Google : \n  Départ : "+trajetGoogle.getDeparture()+"\n  Arrivée : "+trajetGoogle.getArrival());
			req.setAttribute("TrajetGoogle", trajetGoogle);
		} catch (JSONException e1) {
			// TODO
			e1.printStackTrace();
		}

		// Vérification de la disponibilité des adresses de la TAN
		// System.out.println(itineraire.getArrivee());

		

		this.getServletContext().getRequestDispatcher("/index.jsp").forward(req,resp);

		// Requete TAN

		System.out.println("TAN : ");
		try {
//			getTaN(departure);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
	public JSONObject getTrajetGoogle(String departure, String arrivee)
			throws IOException, JSONException {
		String url_build = prefix_url_direction + "?origin="
				+ URLEncoder.encode(departure, "UTF-8") + "&destination="
				+ URLEncoder.encode(arrivee, "UTF-8")
				+ "&sensor=false&key="
				+ key;
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
		return obj;
	}

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
		EtapeGoogle step = new EtapeGoogle();
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

/*
* 		--  TAN  --
*/
	public JSONObject getAdresseTaN(String departure) throws Exception {
		
		String urlParameters = "nom=" + URLEncoder.encode(departure, "UTF-8")
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
			JSONObject adresse = new JSONObject(response.toString());
			return adresse;

		/*
		 * try{ URL url = new
		 * URL("https://www.tan.fr/ewp/mhv.php/itineraire/address.json?nom=nantes"
		 * ); HttpURLConnection connection =
		 * (HttpURLConnection)url.openConnection();
		 * connection.setRequestMethod("POST"); String charset = "UTF-8"; String
		 * qry = URLEncoder.encode("&nom=nantes",charset);
		 * connection.setRequestProperty("Accept-Charset", charset);
		 * connection.setRequestProperty("Content-Type",
		 * "application/x-ww-form-urlencoded");
		 * connection.setRequestProperty("Content-Length",
		 * ""+Integer.toString(qry.getBytes().length));
		 * connection.setDoInput(true); connection.setDoOutput(true);
		 * 
		 * InputStream _is; if (connection.getResponseCode() >= 400) { _is =
		 * connection.getInputStream(); } else {
		 * 
		 * _is = connection.getErrorStream(); }
		 * 
		 * 
		 * } catch (Exception e) { throw e; }
		 */
	}
	
	
	public TrajetTAN setAdresseTAN(JSONObject obj){
	//TODO	
		return null;
	}

	
}
