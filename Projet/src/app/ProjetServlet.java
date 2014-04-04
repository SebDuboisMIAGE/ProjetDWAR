package app;

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

import com.google.appengine.labs.repackaged.org.json.JSONObject;

@SuppressWarnings("serial")
public class ProjetServlet extends HttpServlet {
	
	public static String prefix_url = "http://maps.google.com/maps/api/geocode/json";
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
	
		// Récupération des saisies dans le HTML
		String departure = req.getParameter("from");
		String arrivee = req.getParameter("to");
		
		// Vérification par rapport à la TAN
	    GPSCoordonate GPSdeparture = getJSONwithAdress(departure);
	    GPSCoordonate GPSarrivee = getJSONwithAdress(arrivee);
		
	    
		
		out.println("<HTML><BODY>");
		out.println("<H2>hello </H2>");
		out.println("<BR><BR>");
		out.println("info:");
		out.println("<BR><BR>");
		out.println("<H2>metoda GET</H2>");
		//out.println("<BR>"+ geocoderResponse.getResults().get(0).getGeometry().getLocation().getLat().toString() +"<BR>");
		out.println("SERVER_NAME=<BR>");
		out.println("REQUEST_METHOD=<BR>");
		out.println("QUERY_STRING=<BR>");
		out.println("REMOTE_HOST=<BR>");
		out.println("REMOTE_ADDR=");
		out.println("</BODY></HTML>"); 
	};
	
public GPSCoordonate getJSONwithAdress(String adress){
	try{
		String url_build = prefix_url + "?address=" + URLEncoder.encode(adress, "UTF-8") + "&sensor=false";
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
	    
	    return new GPSCoordonate(loc.getDouble("lat"),loc.getDouble("lng"));
	}
	catch(Exception e){
		e.printStackTrace();
		return null;
	}	
}
}
