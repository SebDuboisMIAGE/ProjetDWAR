package servlets;

import java.io.BufferedReader;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Query.Filter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
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
import beans.Historique;
import beans.TrajetGoogle;
import beans.TrajetTAN;

import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.PreparedQuery;
@SuppressWarnings("serial")
public class ItineraireServlet extends HttpServlet{
	
	public static String prefix_url_geocode = "https://maps.google.com/maps/api/geocode/json";
	public static String prefix_url_direction = "https://maps.googleapis.com/maps/api/directions/json";
	public static String key ="AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA";
	public static String filepath_Stops = "files/stops.txt";
	public static String filepath_Shapes = "files/shapes.txt";

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
		
		//Enregistrement historique
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if (user != null) {   
			Entity recherche = new Entity("Historique");		
	
			recherche.setProperty("id", user.getEmail());
			recherche.setProperty("depart", departureChoose);
			recherche.setProperty("arrivee", arriveeChoose);
			datastore.put(recherche);
			Filter FilterId = new FilterPredicate("id", FilterOperator.EQUAL, user.getEmail());
			Query q = new Query("Historique").setFilter(FilterId);

			// Use PreparedQuery interface to retrieve results
			PreparedQuery pq = datastore.prepare(q);
			List<Historique> listeHisto = new ArrayList<Historique>();
			Historique histo;
			int nb = 0;
			//récupéartion des 3 dernières recherches !!!
			for (Entity result : pq.asIterable()) {
				if (nb != 3){
					histo = new Historique();
					histo.setDepart((String) result.getProperty("depart"));
					histo.setArrivee((String) result.getProperty("arrivee"));
					listeHisto.add(histo);
					nb++;
				}
			}        
			req.setAttribute("historique", listeHisto);
        }        
		//Fin enregistrement 
		
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
			e1.printStackTrace();
		}		
		
		req.setAttribute("drag", 1);
		
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
		String day = String.valueOf((Calendar.getInstance().get(Calendar.DAY_OF_MONTH)));
		if (day.length() == 1) day='0' + day;
		int mont = Calendar.getInstance().get(Calendar.MONTH);
		mont++;
		if (mont == 13) mont = 1;
		String month = String.valueOf(mont);
		if (month.length() == 1) month='0' + month;
		System.out.println(month);
		String heure = String.valueOf((Calendar.getInstance().get(Calendar.HOUR_OF_DAY)));
		if (heure.length() == 1) heure='0' + heure;
		String minute = String.valueOf((Calendar.getInstance().get(Calendar.MINUTE)));
		if (minute.length() == 1) minute='0' + minute;
		String d = String.valueOf((Calendar.getInstance().get(Calendar.YEAR))) + "-" + month + "-" + day;
		String urlParameters = "depart=" + URLEncoder.encode(origine.getIdTAN(), "UTF-8") + "&arrive=" + URLEncoder.encode(destination.getIdTAN(), "UTF-8") + "&type=0&accessible=0&temps=" + URLEncoder.encode(d,"UTF-8") + "&retour=0"
				+ "\"";
		System.out.println(urlParameters);
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
				System.out.println(line);
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
		itineraire.setHeureDepart(obj.getString("heureDepart"));
		itineraire.setHeureArrivee(obj.getString("heureArrivee"));
		itineraire.setDuration(obj.getString("duree"));
		itineraire.setCorrespondance(obj.getString("correspondance"));
		itineraire.setDepartureGPS(getGPSCoordonateGoogle(itineraire.getDeparture()));
		itineraire.setArrivalGPS(getGPSCoordonateGoogle(itineraire.getArrival()));
		JSONObject arretDep = obj.getJSONObject("arretDepart");
		JSONArray etapes = obj.getJSONArray("etapes");
		
		//Sauvegarde adresse précédente
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
					System.out.println("Allez arrêt : " + step.getLibelleArret());
					
				}

				arretPrec = step.getLibelleArret();
			}
			else{
				step.setMarche(false); // alors parcours de tous les arrêts
				// Sur quelle ligne TAN ?
				JSONObject ligne = etape.optJSONObject("ligne");
				
				if (ligne != null){
					step.setNumligne(ligne.getString("numLigne"));
				}
				JSONObject arretStop = etape.optJSONObject("arretStop");
				if (arretStop != null){
					step.setLibelleArret(arretStop.getString("libelle"));
				}			
				
				//recuperer coordonnees de l'ancien step qui était marche
				//if (itineraire.getSteps().size() > 0){
				if(itineraire.getSteps() != null)
				{
					if (itineraire.getSteps().get(itineraire.getSteps().size()-1).getMarche())
					{
						System.out.println("GPS dep : lat : " + gpsAdressePrec.getLat()+ " lng : " + gpsAdressePrec.getLng());
						GPSCoordonate stepPrec = getGPSCoordonnateTANLigne(arretPrec, step.getNumligne());
						System.out.println("GPS arr : lat : " + stepPrec.getLat() + " lng : " + stepPrec.getLng());
						List<GPSCoordonate> way = new ArrayList<GPSCoordonate>();
						way.add(gpsAdressePrec);
						way.add(stepPrec);
						itineraire.getSteps().get(itineraire.getSteps().size()-1).setCoordonnees(way);
						gpsAdressePrec = stepPrec;
					}
				}
				//}
				GPSCoordonate coordonnees = null;
				GPSCoordonate coordArretLigne = null;
				if (step.getNumligne() != null){
					if (itineraire.getSteps() == null){
						coordArretLigne = getGPSCoordonnateTANLigne(arretDep.getString("libelle"), step.getNumligne());
						arretDep = null;
					}
					else{
						coordArretLigne =  getGPSCoordonnateTANLigne(itineraire.getSteps().get(itineraire.getSteps().size()-1).getLibelleArret(), step.getNumligne());
					}	
					coordonnees = getGPSCoordonnateTANLigne(step.getLibelleArret(), step.getNumligne());
					// La fonction getListeArret est à revoir
					step.setCoordonnees(getListeArret(coordArretLigne, coordonnees, step.getNumligne()));
					System.out.println("Départ : ");
					System.out.println(coordArretLigne.getLat() + ", " + coordArretLigne.getLng());
					System.out.println("Arrivée : " + step.getLibelleArret());
					System.out.println(coordonnees.getLat() + ", " + coordonnees.getLng());
				}
				else {
					coordonnees = getGPSCoordonnateTANLigne(step.getLibelleArret(), step.getNumligne());
					System.out.println("Départ : ");
					System.out.println(gpsAdressePrec.getLat() + ", " + gpsAdressePrec.getLng());
					System.out.println("Arrivée : " + step.getLibelleArret());
					System.out.println(coordonnees.getLat() + ", " + coordonnees.getLng());
				}				
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
				System.out.println("liste Google : " + t.getSteps().get(i).getWay().get(j).getLat());
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
		BufferedReader scanner = new BufferedReader(new InputStreamReader(
                new FileInputStream(filepath_Stops), "UTF8"));
		@SuppressWarnings("unused")
		boolean trouve = false;	
		GPSCoordonate coord = null;
		List<GPSCoordonate> listeArret = new ArrayList<GPSCoordonate>();
		GPSCoordonate res = new GPSCoordonate();
		String line;
		// On boucle sur chaque champ detecté
		try {
			while ((line = scanner.readLine()) != null) {
				coord = new GPSCoordonate();				
				//traitement de la ligne 
				//On verifie si l'arret coorespondant a la ligne correspond bien a celui recherché
				String str[]=line.split(",");
				//Dans ce tableau, le champ d'indice 3 correspond à la latitude
				//et le champ d'indice 4 correspond à la longitude
				String arret = str[1].substring(1,str[1].length()-1);
				if (nameStop.trim().equals(arret.trim())){	
					coord.setLat(str[3]);
					coord.setLng(str[4]);
					listeArret.add(coord);
					trouve = true;
				}		
			}
			scanner.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		 
				
		boolean trouveBon = false;
		
		for(int i=0; i < listeArret.size() ; i++){
			scanner = new BufferedReader(new InputStreamReader(
	                new FileInputStream(filepath_Shapes), "UTF8"));
			// On boucle sur chaque champ detecté
			try {
				while ((line = scanner.readLine()) != null && !trouveBon) {
					
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
			} catch (IOException e) {
				e.printStackTrace();
			}			
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
	JSONObject res = obj.getJSONArray("results").getJSONObject(0);
    JSONObject loc = res.getJSONObject("geometry").getJSONObject("location");
    
    GPSCoordonate coord = new GPSCoordonate();
    coord.setLat(loc.getString("lat"));
    coord.setLng(loc.getString("lng"));
	
	return coord;
}

public List<GPSCoordonate> getListeArret(GPSCoordonate depart, GPSCoordonate arrivee, String numLigne) throws FileNotFoundException{
	
	//filePath est une variable globale (fichier shapes.txt) 
	@SuppressWarnings("resource")
	Scanner scanner=new Scanner(new File(filepath_Shapes));
	boolean trouvePremier = false;
	boolean trouveDernier = false;
	List<GPSCoordonate> res = new ArrayList<GPSCoordonate>();
	
	System.out.println(depart.getLat());
	String idParcoursCourant = "";
	
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
		// str[0] est un identifiant du parcours (car plusieurs parcours possible)
		
		
		if (ligne.trim().equals(numLigne.trim())){
			
			if (str[0].trim().equals(idParcoursCourant.trim()) || idParcoursCourant == "")
			{
				if (!trouvePremier){
					// si c'est la bonne ligne alors il faut vérifier la bonne coordonnées
					if ((depart.getLat().trim().equals(str[1].trim()) && depart.getLng().trim().equals(str[2].trim())) || (arrivee.getLat().trim().equals(str[1].trim()) && arrivee.getLng().trim().equals(str[2].trim()))){
						
						GPSCoordonate coordonnees = new GPSCoordonate();
						coordonnees.setLat(str[1]);
						coordonnees.setLng(str[2]);
						System.out.println(line);
						res.add(coordonnees);
						idParcoursCourant = str[0];
						trouvePremier = true;
					}
				}
				else {
					if ((depart.getLat().trim().equals(str[1].trim()) && depart.getLng().trim().equals(str[2].trim()) && !str[1].trim().equals(res.get(0).getLat().trim()) && !str[2].trim().equals(res.get(0).getLng().trim())) || ((arrivee.getLat().trim().equals(str[1].trim()) && arrivee.getLng().trim().equals(str[2].trim()) && !str[1].trim().equals(res.get(0).getLat().trim()) && !str[2].trim().equals(res.get(0).getLng().trim())))){
						trouveDernier = true;
						System.out.println("Dernier : ");
					}	
					GPSCoordonate coordonneesInter = new GPSCoordonate();
					System.out.println(line);
					//Test si deja ajoute
					if (!res.get(res.size()-1).getLat().trim().equals(str[1].trim()) && !res.get(res.size()-1).getLng().trim().equals(str[2].trim())){
						coordonneesInter.setLat(str[1]);
						coordonneesInter.setLng(str[2]);		
						res.add(coordonneesInter);
					}								
				}
			}
			else
			{
				trouvePremier = false;
				idParcoursCourant = "";
				res.clear();
			}
		}	
		
	}

	if(!res.get(0).getLat().trim().equals(depart.getLat().trim()) && !res.get(0).getLng().trim().equals(depart.getLng().trim())){
		List<GPSCoordonate> result = new ArrayList<GPSCoordonate>();
		for(int i=res.size()-1; i>=0; i--){
		    result.add(res.get(i));}
		res = result;
	}
	//Lecture de la liste des coordonnées
	
	for (int j = 0; j != res.size(); j++){
		System.out.println("liste : " + res.get(j).getLat());
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
