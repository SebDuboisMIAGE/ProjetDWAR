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
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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

import com.google.appengine.api.datastore.Text;
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
		
		// Requete TAN
		try {
			getTaN(departure);
		} catch (Exception e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
		}	    
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

public void getTaN(String departure) throws Exception
{
	String urlParameters =
	        "nom=" + URLEncoder.encode("nantes", "UTF-8");
	URL url;
    HttpURLConnection connection = null;  
    try {
      //Create connection
      url = new URL("https://www.tan.fr/ewp/mhv.php/itineraire/address.json");
      connection = (HttpURLConnection)url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			
      connection.setRequestProperty("Content-Length", "" + 
               Integer.toString(urlParameters.getBytes().length));
      connection.setRequestProperty("Content-Language", "en-US");  
			
      connection.setUseCaches (false);
      connection.setDoInput(true);
      connection.setDoOutput(true);

      //Send request
      DataOutputStream wr = new DataOutputStream (
                  connection.getOutputStream ());
      wr.writeBytes (urlParameters);
      wr.flush ();
      wr.close ();

      //Get Response	
      InputStream is = connection.getInputStream();
      BufferedReader rd = new BufferedReader(new InputStreamReader(is));
      String line;
      StringBuffer response = new StringBuffer(); 
      while((line = rd.readLine()) != null) {
        response.append(line);
        response.append('\r');
      }
      rd.close();
      System.out.println(response.toString());

    } catch (Exception e) {

      e.printStackTrace();
    } finally {

      if(connection != null) {
        connection.disconnect(); 
      }
    }	
	
	/*
	try{
		URL url = new URL("https://www.tan.fr/ewp/mhv.php/itineraire/address.json?nom=nantes");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("POST"); 
		String charset = "UTF-8"; 
		String qry = URLEncoder.encode("&nom=nantes",charset); 
		connection.setRequestProperty("Accept-Charset", charset); 
		connection.setRequestProperty("Content-Type", "application/x-ww-form-urlencoded"); 
		connection.setRequestProperty("Content-Length", ""+Integer.toString(qry.getBytes().length)); 
		connection.setDoInput(true); 
		connection.setDoOutput(true); 
		
		InputStream _is;  
		if (connection.getResponseCode() >= 400) {  
		    _is = connection.getInputStream();  
		} else {  
		     
		    _is = connection.getErrorStream();  
		}
		
		
	}
	catch (Exception e)
	{
		throw e;
	}*/
}
}
