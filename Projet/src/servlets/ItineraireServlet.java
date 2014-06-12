package servlets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.lang.*;

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
	public static String filepath_Stops = "stops.txt";
	public static String filepath_Shapes = "shapes.txt";

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		resp.setContentType("text/html");
		@SuppressWarnings("unused")
		PrintWriter out = resp.getWriter();	

		String departure = req.getParameter("from");
		String arrivee = req.getParameter("to");
		Donnees data = new Donnees();
		data.setArrival(arrivee); 
		data.setDeparture(departure);
	
		//Récupération des adresses de la liste déroulante
		String departureChoose = req.getParameter("listedepart");
		String arriveeChoose = req.getParameter("listearrivee");
		
		req.setAttribute("donnees", data);
		req.setAttribute("visibility_carte", "visible_carte");
		req.setAttribute("visibility_calculer", "visible_calculer");
		req.setAttribute("visibility_resultat", "visible_resultat");
		
		// Récupération des trajets (Google et Tan)
		try {
			TrajetGoogle trajetGoogleDriving = setTrajetGoogle(getTrajetGoogle(departureChoose, arriveeChoose, "driving"));
			req.setAttribute("TrajetGoogleDriving", trajetGoogleDriving);
			TrajetGoogle trajetGoogleBiking = setTrajetGoogle(getTrajetGoogle(departureChoose, arriveeChoose, "bicycling"));
			req.setAttribute("TrajetGoogleBicycling", trajetGoogleBiking);
			TrajetGoogle trajetGoogleWalking = setTrajetGoogle(getTrajetGoogle(departureChoose, arriveeChoose, "walking"));
			req.setAttribute("TrajetGoogleWalking", trajetGoogleWalking);
			TrajetTAN trajetTAN = setTrajetTAN(getTrajetTAN(getAdresseTAN(departureChoose), getAdresseTAN(arriveeChoose)));
			req.setAttribute("TrajetTan", trajetTAN);
		} catch (Exception e1) {
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
		departGPS.setLat(info.getJSONObject("start_location").getString("lat"));
		departGPS.setLng(info.getJSONObject("start_location").getString("lng"));
		itineraire.setDepartureGPS(departGPS);
		GPSCoordonate arriveeGPS = new GPSCoordonate();
		arriveeGPS.setLat(info.getJSONObject("end_location").getString("lat"));
		arriveeGPS.setLng(info.getJSONObject("end_location").getString("lng"));
		itineraire.setArrivalGPS(arriveeGPS);
				
		// set Etapes
		JSONArray list = info.getJSONArray("steps");		
		for (int i = 0; i < list.length(); ++i) {
			EtapeGoogle step = new EtapeGoogle();
			JSONObject item = list.getJSONObject(i);
			step.setDistance(item.getJSONObject("distance").getDouble("value"));
			step.setDuree(item.getJSONObject("duration").getDouble("value"));
			GPSCoordonate depart = new GPSCoordonate();
			depart.setLat(item.getJSONObject("start_location").getString("lat"));
			depart.setLng(item.getJSONObject("start_location").getString("lng"));
			GPSCoordonate arrive = new GPSCoordonate();
			arrive.setLat(item.getJSONObject("end_location").getString("lat"));
			arrive.setLng(item.getJSONObject("end_location").getString("lng"));
			step.setDepart(depart);
			step.setArrivee(arrive);
			step.setConsigne(item.getString("html_instructions"));
			step.setWay(decodePoly(item.getJSONObject("polyline").getString("points")));
			itineraire.setSteps(step);
		}
		return itineraire;
	}
	
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
	public AdresseTAN getAdresseTAN(String adresse) throws Exception{
		
		AdresseTAN adresseT = new AdresseTAN();
		// get the first result
		JSONArray list;
		try {
			JSONObject obj = getAdresseTaN(adresse);
			list = obj.getJSONArray("lieux");
			// Lecture de toutes les adresses trouvees			
			JSONObject item = list.getJSONObject(0);
			adresseT.setAdresse(item.getString("nom")+' '+item.getString("cp")+' '+item.getString("ville"));
			adresseT.setIdTAN(item.getString("id"));
		} catch (JSONException e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
		}
		
		return adresseT;
	}	
	
	/*
	 * Fonction permettant de récupérer un fichier JSON représentant le trajet TAN 
	 * d'une adresse à une autre
	 * Renvoit un JSON
	 */
	public JSONObject getTrajetTAN(AdresseTAN origine, AdresseTAN destination) throws Exception
	{
		String urlParameters = "depart=" + URLEncoder.encode(origine.getIdTAN(), "UTF-8") + "&arrive=" + URLEncoder.encode(destination.getIdTAN(), "UTF-8") + "&type=0&accessible=0&temps=" + URLEncoder.encode("2014-06-05 17:00","UTF-8") + "&retour=0"
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
				System.out.println(line);
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
	public TrajetTAN setTrajetTAN(JSONObject obj) throws JSONException, IOException {
		TrajetTAN itineraire = new TrajetTAN();
		// get the first result
		itineraire.setDeparture(obj.getString("adresseDepart"));
		itineraire.setArrival(obj.getString("adresseArrivee"));
		//TODO Recuperer aussi les coordonnées GPS du depart et arrivee
		itineraire.setHeureDepart(obj.getString("heureDepart"));
		itineraire.setHeureArrivee(obj.getString("heureArrivee"));
		itineraire.setDuration(obj.getString("duree"));
		itineraire.setCorrespondance(obj.getString("correspondance"));
		//ERROR
		itineraire.setDepartureGPS(getGPSCoordonateGoogle(itineraire.getDeparture()));
		itineraire.setArrivalGPS(getGPSCoordonateGoogle(itineraire.getArrival()));
		
		JSONArray etapes = obj.getJSONArray("etapes");
		
		//Sauvegarde adresse précédente
		String adressePrec = itineraire.getDeparture();
		GPSCoordonate gpsAdressePrec = itineraire.getDepartureGPS();
		
		//Dernier arrêt 
		String arretPrec = "";
		
		//Récupération des étapes du trajet TAN
		for (int i = 0; i < etapes.length(); ++i) {
			EtapeTAN step = new EtapeTAN();
			JSONObject etape = etapes.getJSONObject(i);
			if (etape.getString("marche") == "true"){ // si on doit marcher
				step.setMarche(true);
				// Jusqu'à l'arrêt
				JSONObject arretStop = etape.optJSONObject("arretStop");
				if (arretStop != null){
					step.setLibelleArret(arretStop.getString("libelle"));
				}

				arretPrec = step.getLibelleArret();
			}
			else{
				System.out.println("Bus");
				step.setMarche(false); // alors parcours de tous les arrêts
				// Sur quelle ligne TAN ?
				JSONObject ligne = etape.optJSONObject("ligne");
				if (ligne != null){
					System.out.println(ligne.getString("numLigne"));
					step.setNumligne(ligne.getString("numLigne"));
				}
				JSONObject arretStop = etape.optJSONObject("arretStop");
				if (arretStop != null){
					System.out.println(arretStop.getString("libelle"));
					step.setLibelleArret(arretStop.getString("libelle"));
				}		
				
				//recuperer coordonnees de l'ancien step qui était marche
				if (itineraire.getSteps().size() > 0){
					//if (itineraire.getSteps().get(itineraire.getSteps().size()-1).isMarche())
					//{
						System.out.println(arretPrec);
						System.out.println(step.getNumligne());
						GPSCoordonate stepPrec = getGPSCoordonnateTANLigne(arretPrec, step.getNumligne());
						System.out.println(gpsAdressePrec.getLat());
						TrajetGoogle t = setTrajetGoogle(getTrajetGoogle(gpsAdressePrec.getLat() +',' + gpsAdressePrec.getLng(), stepPrec.getLat() + ','+ stepPrec.getLng(), "walking"));
						List<GPSCoordonate> way = chargerList(t);
						itineraire.getSteps().get(itineraire.getSteps().size()-1).setCoordonnees(way);
						gpsAdressePrec = stepPrec;
					//}
				}
				GPSCoordonate coordonnees = null;
				if (step.getNumligne() != null){
					coordonnees = getGPSCoordonnateTANLigne(step.getLibelleArret(), step.getNumligne());
					step.setCoordonnees(getListeArret(gpsAdressePrec, coordonnees, step.getNumligne()));
				}
				
				System.out.println("dep : " + gpsAdressePrec.getLat());
				System.out.println("arr : " + coordonnees.getLat());
				//Récupérer tous les arrêts entre deux arrêts d'une ligne
				
				gpsAdressePrec = coordonnees;
				arretPrec = step.getLibelleArret();
			}
			
			step.setHeureDepart(etape.getString("heureDepart"));
			step.setHeureArrivee(etape.getString("heureArrivee"));
			step.setDuree(etape.getString("duree"));
			
			//ajout de la step
			itineraire.setSteps(step);
		}
		return itineraire;
	}
	
	public List<GPSCoordonate> chargerList(TrajetGoogle t){
		List<GPSCoordonate> res = new ArrayList<GPSCoordonate>();
		for(int i = 0; i != t.getSteps().size() ; i++){
			for(int j =0; j != t.getSteps().get(i).getWay().size(); j++){
				res.add(t.getSteps().get(i).getWay().get(j));
			}
		}
		return res;
	}
	
	public GPSCoordonate getGPSCoordonnateStopTAN(String nameStop, String pathFile) throws FileNotFoundException, UnsupportedEncodingException{
		 
		//filePath est une variable globale (fichier stops.txt) 
		Scanner scanner=new Scanner(new File(pathFile));
		boolean trouve = false;	
		GPSCoordonate coord = new GPSCoordonate();
		
		// On boucle sur chaque champ detecté
		while (scanner.hasNextLine() && !trouve) {
		
			String line = scanner.nextLine();
			//traitement de la ligne 
			//On verifie si l'arret coorespondant a la ligne correspond bien a celui recherché
			String str[]=line.split(",");
			//Dans ce tableau, le champ d'indice 3 correspond à la latitude
			//et le champ d'indice 4 correspond à la longitude
			String arret = str[1].substring(1,str[1].length()-1);
			arret = new String(arret.getBytes(), "UTF-8");
			if (nameStop.trim().equals(arret.trim())){	
				coord.setLat(str[3]);
				coord.setLng(str[4]);
				trouve = true;
			}		
		}
		 
		scanner.close();
		return coord;

	}
	
	public GPSCoordonate getGPSCoordonnateTANLigne(String nameStop, String numLigne) throws FileNotFoundException, UnsupportedEncodingException{
		 
		//filePath est une variable globale (fichier stops.txt) 
		Scanner scanner=new Scanner(new File(filepath_Stops));
		boolean trouve = false;	
		GPSCoordonate coord = null;
		List<GPSCoordonate> listeArret = new ArrayList<GPSCoordonate>();
		GPSCoordonate res = new GPSCoordonate();
		
		// On boucle sur chaque champ detecté
		while (scanner.hasNextLine()) {
			coord = new GPSCoordonate();
			String line = scanner.nextLine();
			//traitement de la ligne 
			//On verifie si l'arret coorespondant a la ligne correspond bien a celui recherché
			String str[]=line.split(",");
			//Dans ce tableau, le champ d'indice 3 correspond à la latitude
			//et le champ d'indice 4 correspond à la longitude
			String arret = str[1].substring(1,str[1].length()-1);
			arret = new String(arret.getBytes(), "UTF-8");
			if (nameStop.trim().equals(arret.trim())){	
				coord.setLat(str[3]);
				coord.setLng(str[4]);
				listeArret.add(coord);
				trouve = true;
			}		
		}
		 
		scanner.close();		
		boolean trouveBon = false;
		
		for(int i=0; i < listeArret.size() ; i++){
			scanner=new Scanner(new File(filepath_Shapes));
			// On boucle sur chaque champ detecté
			while (scanner.hasNextLine() && !trouveBon) {
			
				String line = scanner.nextLine();
				
				//traitement de la ligne 
				//On découpe les éléments de la ligne
				String str[]=line.split(",");
				//Dans ce tableau, le champ d'indice 2 correspond à la latitude
				//et le champ d'indice 3 correspond à la longitude
				//le champ d'indice 1 correspond à la ligne (juste les deux premieres lettres)
				
				String ligne;
				if (str[0].length() == 5){
					ligne = str[0].substring(0,1);
				}
				else{
					ligne = str[0].substring(0,2);
				}
				
				if (str[1].trim().equals(listeArret.get(i).getLat().trim()) && str[2].trim().equals(listeArret.get(i).getLng().trim()) && numLigne.trim().equals(ligne.trim())){
					res.setLat(str[1]);
					res.setLng(str[2]);
					trouveBon = true;
				}									
			}
			scanner.close();
		}
		
		return res;

	}
	
	
	/*
	--	GOOGLE  --
 * Fonction permettant de récupérer les coordonnées GPS d'une adresse
 * latitude et longitude
 */
public GPSCoordonate getGPSCoordonateGoogle(String adresse)
		throws IOException, JSONException {
	String url_build = prefix_url_geocode + "?address="
			+ URLEncoder.encode(adresse, "UTF-8")
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
	System.out.println(str);
	JSONObject res = obj.getJSONArray("results").getJSONObject(0);
    JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");
    
    GPSCoordonate coord = new GPSCoordonate();
    coord.setLat(loc.getString("lat"));
    coord.setLng(loc.getString("lng"));
	
	return coord;
}

public List<GPSCoordonate> getListeArret(GPSCoordonate depart, GPSCoordonate arrivee, String numLigne) throws FileNotFoundException{
	
	//filePath est une variable globale (fichier shapes.txt) 
	Scanner scanner=new Scanner(new File(filepath_Shapes));
	boolean trouvePremier = false;
	boolean trouveDernier = false;
	List<GPSCoordonate> res = new ArrayList<GPSCoordonate>();
	
	// On boucle sur chaque champ detecté
	while (scanner.hasNextLine() && !trouveDernier) {
	
		String line = scanner.nextLine();
		
		//traitement de la ligne 
		//On découpe les éléments de la ligne
		String str[]=line.split(",");
		//Dans ce tableau, le champ d'indice 2 correspond à la latitude
		//et le champ d'indice 3 correspond à la longitude
		//le champ d'indice 1 correspond à la ligne (juste les deux premieres lettres)
		String ligne;
		if (str[0].length() == 5){
			ligne = str[0].substring(0,1);
		}
		else{
			ligne = str[0].substring(0,2);
		}
		
		if (ligne.trim().equals(numLigne.trim())){		
		
			if (!trouvePremier){				
				// si c'est la bonne ligne alors il faut vérifier la bonne coordonnées
				if ((depart.getLat().trim().equals(str[1].trim()) && depart.getLng().trim().equals(str[2].trim())) || (arrivee.getLat().trim().equals(str[1].trim()) && arrivee.getLng().trim().equals(str[2].trim()))){
					GPSCoordonate coordonnees = new GPSCoordonate();
					coordonnees.setLat(str[1]);
					coordonnees.setLng(str[2]);
					System.out.println(str[1]);
					System.out.println(str[2]);
					res.add(coordonnees);
					trouvePremier = true;
				}
			}
			else {
				if ((depart.getLat().trim().equals(str[1].trim()) && depart.getLng().trim().equals(str[2].trim()) && !str[1].trim().equals(res.get(0).getLat().trim()) && !str[2].trim().equals(res.get(0).getLng().trim())) || ((arrivee.getLat().trim().equals(str[1].trim()) && arrivee.getLng().trim().equals(str[2].trim()) && !str[1].trim().equals(res.get(0).getLat().trim()) && !str[2].trim().equals(res.get(0).getLng().trim())))){
					trouveDernier = true;
					System.out.println("Dernier ");
				}	
				GPSCoordonate coordonnees = new GPSCoordonate();
				coordonnees.setLat(str[1]);
				coordonnees.setLng(str[2]);				
				System.out.println(str[1]);
				System.out.println(str[2]);
				res.add(coordonnees);
				res.add(coordonnees);					
			}				
		}		
	}
	
	if(!res.get(0).getLat().trim().equals(depart.getLat().trim()) && !res.get(0).getLng().trim().equals(depart.getLng().trim())){
		List<GPSCoordonate> result = new ArrayList<GPSCoordonate>();
		for(int i=res.size()-1; i>=0; i--){
		    result.add(res.get(i));}
		res = result;
	}
	
	return res;
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
		   p.setLat(Double.toString(lat / 1E5));
		   p.setLng(Double.toString(lng / 1E5));
		   poly.add(p);
		  }
		  return poly;
	 }
	
}
