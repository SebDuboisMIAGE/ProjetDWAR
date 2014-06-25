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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import beans.AdresseTAN;
import beans.Donnees;
import beans.Historique;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class RechercheServlet extends HttpServlet {

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
		req.setAttribute("visibility_calculer", "visible_calculer");
		
		Donnees data = new Donnees();
		data.setArrival(arrivee); 
		data.setDeparture(departure);
		
		req.setAttribute("donnees", data);		
				
		// Requete TAN
		try {
			//Récupération des listes d'adresses de départ et d'arrivee via la TAN
			List<AdresseTAN> listeChoixDepart = getChoixAdresseTAN(getAdresseTaN(departure));
			req.setAttribute("ListeDepart", listeChoixDepart);
			List<AdresseTAN> listeChoixArrivee = getChoixAdresseTAN(getAdresseTaN(arrivee));
			req.setAttribute("ListeArrivee", listeChoixArrivee);
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		req.setAttribute("drag", 0);
		
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        if (user != null) {   
			Filter FilterId = new FilterPredicate("id", FilterOperator.EQUAL, user.getEmail());
			Query q = new Query("Historique").setFilter(FilterId);

			// Use PreparedQuery interface to retrieve results
			PreparedQuery pq = datastore.prepare(q);
			List<Historique> listeHisto = new ArrayList<Historique>();
			Historique histo;
			for (Entity result : pq.asIterable()) {
				histo = new Historique();
				histo.setDepart((String) result.getProperty("depart"));
				histo.setArrivee((String) result.getProperty("arrivee"));
				listeHisto.add(histo);
			}        
			req.setAttribute("historique", listeHisto);
        }   
		
		// Renvoit des données dans la page JSP
		this.getServletContext().getRequestDispatcher("/index.jsp").forward(req,resp);

	};

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
			e.printStackTrace();
		}
		
		return liste;
	}	
	
}
