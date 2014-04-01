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

@SuppressWarnings("serial")
public class ProjetServlet extends HttpServlet {
	
	public static String prefix_url = "http://maps.google.com/maps/api/geocode/xml";
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		PrintWriter out = resp.getWriter();
	
		String departure = req.getParameter("from");
		String arrivee = req.getParameter("to");		
	    GPSCoordonate GPSdeparture = getXMLwithAdress(departure);
		
	    System.out.println("lat/lng=" + GPSdeparture.lat + "," + GPSdeparture.lng);	    
		
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
	
public GPSCoordonate getXMLwithAdress(String adress){
	float lat = Float.NaN;
    float lng = Float.NaN;
	try{
		String url_build = prefix_url + "?address=" + URLEncoder.encode(adress, "UTF-8") + "&sensor=false";
		System.out.println(url_build);
		URL url = new URL(url_build);
		// prepare an HTTP connection to the geocoder
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	    Document geocoderResultDocument = null;
	    try {
	      // open the connection and get results as InputSource.
	      conn.connect();
	      InputSource geocoderResultInputSource = new InputSource(conn.getInputStream());
	
	      // read result and parse into XML Document
	      geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);
	    } finally {
	      conn.disconnect();
	    }	
	    // prepare XPath
	    XPath xpath = XPathFactory.newInstance().newXPath();
	
	    // extract the result
	    NodeList resultNodeList = null;
	    
	    //extract the coordinates of the first result
	    try {
			resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// TODO Bloc catch généré automatiquement
			e.printStackTrace();
		}    
	    
	    for(int i=0; i<resultNodeList.getLength(); ++i) {
	      Node node = resultNodeList.item(i);
	      
	      if("lat".equals(node.getNodeName())){
	    	  lat = Float.parseFloat(node.getTextContent());
	      }
	      if("lng".equals(node.getNodeName())) lng = Float.parseFloat(node.getTextContent());
	    }
	    return new GPSCoordonate(lat,lng);
	}
	catch(Exception e){
		e.printStackTrace();
		return null;
	}	
}

public static String get(String url) throws IOException{
 
	String source ="";
	URL oracle = new URL(url);
	BufferedReader in = new BufferedReader(new InputStreamReader(oracle.openStream()));
	String inputLine;
	 
	while ((inputLine = in.readLine()) != null)
	source +=inputLine;
	in.close();
	return source;
}
}
