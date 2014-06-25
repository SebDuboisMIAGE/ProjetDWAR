<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="com.google.appengine.api.users.*" %>
<% UserService userService = UserServiceFactory.getUserService(); %>
<html>
<head>
<title>SmartTraveler</title>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<link href="<c:url value="/stylesheets/main.css" />" rel="stylesheet">
<link href="<c:url value="/javascript/main.js" />" rel="javascript">
<script type="text/javascript" 
		src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script>
	<script type="text/javascript" 
		src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/jquery-ui.js"></script>
	<link rel="stylesheet" type="text/css" 
		href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.1/themes/base/jquery-ui.css"/>
<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA&sensor=true">
	
</script>
<script type="text/javascript">
	var directionsDisplay;
	var listeItineraires;
	var carte;
	var directionsService = new google.maps.DirectionsService();
	var infowindow = new google.maps.InfoWindow();
	
	 function initialize() {
		var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.departureGPS.lng}"/>');//nantes
		var centre = new google.maps.LatLng(47.21345774,-1.55633177);
		var mapOptions = {
			center : centre,
			zoom : 11,
			mapTypeId : google.maps.MapTypeId.ROADMAP};

		carte = new google.maps.Map(document.getElementById("carte"), mapOptions);
		//création des marqueurs de départ et d'arrivée
		var depart = new google.maps.Marker({
			position : new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.departureGPS.lng}"/>'),
			map : carte
			
		});
		var arrivee = new google.maps.Marker({
			position : new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.arrivalGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.arrivalGPS.lng}"/>'),
			map : carte
		});

		//création de la polyline pour dessiner le trajet
		var parcoursCar = new Array();
		<c:forEach items="${TrajetGoogleDriving.steps}" var="step">
			<c:forEach items="${step.way}" var="w">
				parcoursCar.push(new google.maps.LatLng('<c:out value="${w.lat}"/>', '<c:out value="${w.lng}"/>'));
			</c:forEach>
		</c:forEach>

		var traceParcoursCar = new google.maps.Polyline({
			path : parcoursCar,//chemin du tracé
			strokeColor : "#FF0000",//couleur du tracé
			strokeOpacity : 1.0,//opacité du tracé
			strokeWeight : 2
		//grosseur du tracé
		});

		//lier le tracé à la carte
		//ceci permet au tracé d'être affiché sur la carte
		traceParcoursCar.setMap(carte);

	}

function trajetVoiture() 
{ 
	document.getElementById('velo').src="images/velo-nc.png";
	document.getElementById('voiture').src="images/voiture-c.png";
	document.getElementById('pieton').src="images/pied-nc.png";
	document.getElementById('bus').src="images/bus-nc.png";	
	//document.getElementById('res_depart').innerHTML = "Adresse de départ :<c:out value="${TrajetGoogleDriving.departure}"></c:out>";
	//document.getElementById('res_arrivee').innerHTML = "Adresse de destination :<c:out value="${TrajetGoogleDriving.arrival}"></c:out>";
	//document.getElementById('res_duree').innerHTML = "Durée :<c:out value="${TrajetGoogleDriving.duration}"></c:out>";
	//document.getElementById('res_distance').innerHTML = "Distance :<c:out value="${TrajetGoogleDriving.distance}"></c:out>";
	//document.getElementById('res_consigne').innerHTML = "Trajet :<c:forEach items="${TrajetGoogleDriving.steps}" var="step"><c:out value="${step.consigne}"></c:out></c:forEach>";

	//Raffraichissement de la map
	var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.departureGPS.lng}"/>');//nantes

	var mapOptions = {
		center : latlng,
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP};

	carte = new google.maps.Map(document.getElementById("carte"),
			mapOptions);
	//création des marqueurs de départ et d'arrivée
	var depart = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.departureGPS.lng}"/>'),
		map : carte
		
	});
	var arrivee = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.arrivalGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.arrivalGPS.lng}"/>'),
		map : carte
	});

	//création de la polyline pour dessiner le trajet
	var parcoursCar = new Array();
	<c:forEach items="${TrajetGoogleDriving.steps}" var="step">
		<c:forEach items="${step.way}" var="w">
			parcoursCar.push(new google.maps.LatLng('<c:out value="${w.lat}"/>', '<c:out value="${w.lng}"/>'));
			parcoursCar.push(new google.maps.LatLng('<c:out value="${w.lat}"/>', '<c:out value="${w.lng}"/>'));
		</c:forEach>
	</c:forEach>

	var traceParcoursCar = new google.maps.Polyline({
		path : parcoursCar,//chemin du tracé
		strokeColor : "#FF0000",//couleur du tracé
		strokeOpacity : 1.0,//opacité du tracé
		strokeWeight : 2
	//grosseur du tracé
	});

	//lier le tracé à la carte
	//ceci permet au tracé d'être affiché sur la carte
	traceParcoursCar.setMap(carte);
} 

function trajetPieton() 
{ 
	document.getElementById('velo').src="images/velo-nc.png";
	document.getElementById('voiture').src="images/voiture-nc.png";
	document.getElementById('pieton').src="images/pied-c.png";
	document.getElementById('bus').src="images/bus-nc.png";		
	//document.getElementById('res_depart').innerHTML = "Adresse de départ :<c:out value="${TrajetGoogleWalking.departure}"></c:out>";
	//document.getElementById('res_arrivee').innerHTML = "Adresse de destination :<c:out value="${TrajetGoogleWalking.arrival}"></c:out>";
	//document.getElementById('res_duree').innerHTML = "Durée :<c:out value="${TrajetGoogleWalking.duration}"></c:out>";
	//document.getElementById('res_distance').innerHTML = "Distance :<c:out value="${TrajetGoogleWalking.distance}"></c:out>";
	//document.getElementById('res_consigne').innerHTML = "Trajet :<c:forEach items="${TrajetGoogleWalking.steps}" var="step"><c:out value="${step.consigne}"></c:out></c:forEach>";

	//Raffraichissement de la map
	var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleWalking.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleWalking.departureGPS.lng}"/>');//nantes

	var mapOptions = {
		center : latlng,
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP};

	carte = new google.maps.Map(document.getElementById("carte"),
			mapOptions);
	//création des marqueurs de départ et d'arrivée
	var depart = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetGoogleWalking.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleWalking.departureGPS.lng}"/>'),
		map : carte
		
	});
	var arrivee = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetGoogleWalking.arrivalGPS.lat}"/>', '<c:out value="${TrajetGoogleWalking.arrivalGPS.lng}"/>'),
		map : carte
	});

	//création de la polyline pour dessiner le trajet
	var parcoursCar = new Array();
	<c:forEach items="${TrajetGoogleWalking.steps}" var="step">
		<c:forEach items="${step.way}" var="w">
			parcoursCar.push(new google.maps.LatLng('<c:out value="${w.lat}"/>', '<c:out value="${w.lng}"/>'));
			parcoursCar.push(new google.maps.LatLng('<c:out value="${w.lat}"/>', '<c:out value="${w.lng}"/>'));
		</c:forEach>
	</c:forEach>

	var traceParcoursCar = new google.maps.Polyline({
		path : parcoursCar,//chemin du tracé
		strokeColor : "#0000FF",//couleur du tracé
		strokeOpacity : 1.0,//opacité du tracé
		strokeWeight : 2
	//grosseur du tracé
	});

	//lier le tracé à la carte
	//ceci permet au tracé d'être affiché sur la carte
	traceParcoursCar.setMap(carte);
} 

function trajetVelo() 
{ 
	document.getElementById('velo').src="images/velo-c.png";
	document.getElementById('voiture').src="images/voiture-nc.png";
	document.getElementById('pieton').src="images/pied-nc.png";
	document.getElementById('bus').src="images/bus-nc.png";		
	//document.getElementById('res_depart').innerHTML = "Adresse de départ :<c:out value="${TrajetGoogleBicycling.departure}"></c:out>";
	//document.getElementById('res_arrivee').innerHTML = "Adresse de destination :<c:out value="${TrajetGoogleBicycling.arrival}"></c:out>";
	//document.getElementById('res_duree').innerHTML = "Durée :<c:out value="${TrajetGoogleBicycling.duration}"></c:out>";
	//document.getElementById('res_distance').innerHTML = "Distance :<c:out value="${TrajetGoogleBicycling.distance}"></c:out>";
	//document.getElementById('res_consigne').innerHTML = "Trajet :<c:forEach items="${TrajetGoogleBicycling.steps}" var="step"><c:out value="${step.consigne}"></c:out></c:forEach>";

	//Raffraichissement de la map
	var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleBicycling.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleBicycling.departureGPS.lng}"/>');//nantes

	var mapOptions = {
		center : latlng,
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP};

	carte = new google.maps.Map(document.getElementById("carte"),
			mapOptions);
	//création des marqueurs de départ et d'arrivée
	var depart = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetGoogleBicycling.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleBicycling.departureGPS.lng}"/>'),
		map : carte
		
	});
	var arrivee = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetGoogleBicycling.arrivalGPS.lat}"/>', '<c:out value="${TrajetGoogleBicycling.arrivalGPS.lng}"/>'),
		map : carte
	});

	//création de la polyline pour dessiner le trajet
	var parcoursCar = new Array();
	<c:forEach items="${TrajetGoogleBicycling.steps}" var="step">
		<c:forEach items="${step.way}" var="w">
			parcoursCar.push(new google.maps.LatLng('<c:out value="${w.lat}"/>', '<c:out value="${w.lng}"/>'));
		</c:forEach>
	</c:forEach>

	var traceParcoursCar = new google.maps.Polyline({
		path : parcoursCar,//chemin du tracé
		strokeColor : "#00FF00",//couleur du tracé
		strokeOpacity : 1.0,//opacité du tracé
		strokeWeight : 2
	//grosseur du tracé
	});

	//lier le tracé à la carte
	//ceci permet au tracé d'être affiché sur la carte
	traceParcoursCar.setMap(carte);
}

function trajetTAN() 
{ 
	document.getElementById('velo').src="images/velo-nc.png";
	document.getElementById('voiture').src="images/voiture-nc.png";
	document.getElementById('pieton').src="images/pied-nc.png";
	document.getElementById('bus').src="images/bus-c.png";	
	//document.getElementById('res_depart').innerHTML = "Adresse de départ :<c:out value="${TrajetTan.departure}"></c:out>";
	//document.getElementById('res_arrivee').innerHTML = "Adresse de destination :<c:out value="${TrajetTan.arrival}"></c:out>";
	//document.getElementById('res_duree').innerHTML = "Durée :<c:out value="${TrajetTan.duration}"></c:out>";
	//document.getElementById('res_distance').innerHTML = "Distance :<c:out value="0"></c:out>";
	//document.getElementById('res_consigne').innerHTML = "Trajet :<c:forEach items="${TrajetTan.steps}" var="step"><c:out value="OK"></c:out></c:forEach>";

	//Raffraichissement de la map
	var latlng = new google.maps.LatLng('<c:out value="${TrajetTan.departureGPS.lat}"/>', '<c:out value="${TrajetTan.departureGPS.lng}"/>');//nantes

	var mapOptions = {
		center : latlng,
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP};

	carte = new google.maps.Map(document.getElementById("carte"),mapOptions);
	//création des marqueurs de départ et d'arrivée
	var depart = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetTan.departureGPS.lat}"/>', '<c:out value="${TrajetTan.departureGPS.lng}"/>'),
		map : carte
		
	});
	var arrivee = new google.maps.Marker({
		position : new google.maps.LatLng('<c:out value="${TrajetTan.arrivalGPS.lat}"/>', '<c:out value="${TrajetTan.arrivalGPS.lng}"/>'),
		map : carte
	});	

	var parcoursCar = new Array();
	<c:forEach items="${TrajetTan.steps}" var="step">	
		<c:forEach items="${step.coordonnees}" var="w">
			parcoursCar.push(new google.maps.LatLng('<c:out value="${w.lat}"/>','<c:out value="${w.lng}"/>'));
		</c:forEach>
	</c:forEach>

	var traceParcoursCar = new google.maps.Polyline({
		path : parcoursCar,//chemin du tracé
		strokeColor : "#9400D3",//couleur du tracé
		strokeOpacity : 1.0,//opacité du tracé
		strokeWeight : 2
	//grosseur du tracé
	});

	//lier le tracé à la carte
	//ceci permet au tracé d'être affiché sur la carte
	traceParcoursCar.setMap(carte);
}

</script>
<script>
	$(function() {
		$( "#draggable" )
			.draggable() 
		<c:if test="${drag == 1}">
			document.getElementById("draggable").style.display="block";
		</c:if>
		<c:if test="${drag == 0}">
			document.getElementById("draggable").style.display="none";
		</c:if>
	});

</script>

</head>
<body onload="initialize()">
			
<div id="banniereRecherche" class=banniereRecherche>
	<% if (userService.getCurrentUser() == null) { %>
		<p><a href="<%= userService.createLoginURL("/") %>">Se connecter</a></p>
	<% }
	else { %>
		<p>Bonjour <%= userService.getCurrentUser().getNickname() %>
		<a href="<%= userService.createLogoutURL("/") %>">Se déconnecter</a></p>
	<% } %>
	<div id="volet_clos">
		<div id="firstSearch" class="Recherche">	
			<form id="firstForm" name="input"
				action=<c:url value="/recherche"/> method="post">
				<div id="Search">	
					Départ : <input type="text" name="from" id="from"
						value="<c:out value="${donnees.departure}"/>" tabindex="1"
						class="valid">
					Arrivée : <input type="text" name="to" id="to"
						value="<c:out value="${donnees.arrival}"/>" tabindex="1"
						class="valid">
				</div>
				<input id="Rech" type="submit" class="submitItinerary" value="Rechercher !">
			</form>
		</div>	
		
		<div id='<c:out value="${visibility_calculer}"/>' class="Calculer">
			<div id="listeDeroulante" class="Recherche">
				<form name="input"
					action=<c:url value="/itineraire"/>
					method="post">
					<SELECT name="listedepart" id="listedepart" size="1">
						<c:forEach items="${ListeDepart}" var="adresse">
							<OPTION>
								<c:out value="${adresse.adresse}"></c:out>
							</OPTION>
						</c:forEach>
					</SELECT> 
					<SELECT name="listearrivee" size="1">
						<c:forEach items="${ListeArrivee}" var="adresse">
							<OPTION>
								<c:out value="${adresse.adresse}"></c:out>
							</OPTION>
						</c:forEach>
					</SELECT> 
					<input type="submit" class="submitItinerary2"
						value="Calculer l'itinéraire !">
				</form>	
			</div> 
		</div>
		<center>		
		<a href="#banniereRecherche" class="rechercher">Réduire</a>
		<a href="#volet_clos" class="fermer">Rechercher</a>
		</center>
	</div>
</div>

<div id="carte" class="carte"></div>

<% if (userService.getCurrentUser() != null) { %>
	<div id="histo_clos">
	    <div id="volet">
	    	<center>
			<table id="products-table"  style="overflow-y:scroll" >
			    <thead>
			        <tr>
			            <th>Départ</th> 
			            <th>Arrivée</th>
			        </tr>
			    </thead>
			    <tbody>
			        <c:forEach items="${historique}" var="histo">
						<tr>
							<td>
								<c:out value="${histo.depart}"></c:out>
							</td>
							<td>
								<c:out value="${histo.arrivee}"></c:out>
							</td>
						</tr>
					</c:forEach>
			</tbody>
			</table>
			</center>	    
	        <a href="#volet" class="ouvrir">Historique</a>
	        <a href="#volet_clos" class="fermer2">Réduire</a>
	    </div>
	</div>
<% } %>

<div id="draggable"> 
	<center>
	<div id="transition">
		<input id="velo" type="image" src="images/velo-nc.png" onClick="trajetVelo()" style="width:20%;" />
		<input id="voiture" type="image" src="images/voiture-nc.png" onClick="trajetVoiture()" style="width:20%;" />
		<input id="pieton" type="image" src="images/pied-nc.png" onClick="trajetPieton()" style="width:20%;" />
		<input id="bus" type="image" src="images/bus-nc.png" onClick="trajetTAN()" style="width:20%;" />
	</div>
	</center>
	<center>
	<table width=30%>
		<tr>
			<th width=20%>
			</th>
			<th width=20%>
				Distance
			</th>
			<th width=20%>
				Duree
			</th>
		</tr>
		<tr>
			<td width=20%>
				<p>Voiture</p>
			</td>
			<td width=20%>
				<p><c:out value="${TrajetGoogleDriving.distance}"></c:out></p>
			</td>
			<td width=20%>
				<p><c:out value="${TrajetGoogleDriving.duration}"></c:out></p>
			</td>
		</tr>
		<tr>
			<td width=20%>
				<p>Velo</p>
			</td>
			<td width=20%>
				<p><c:out value="${TrajetGoogleBicycling.distance}"></c:out></p>
			</td>
			<td width=20%>
				<p><c:out value="${TrajetGoogleBicycling.duration}"></c:out></p>
			</td>
		</tr>
		<tr>
			<td width=20%>
				<p>Pieton</p>
			</td>
			<td width=20%>
				<p><c:out value="${TrajetGoogleWalking.distance}"></c:out></p>
			</td>
			<td width=20%>
				<p><c:out value="${TrajetGoogleWalking.duration}"></c:out></p>
			</td>
		</tr>
		<tr>
			<td width=20%>
				<p>Tramway/Bus</p>
			</td>
			<td width=20%>
				<p>-</p>
			</td>
			<td width=20%>
				<p><c:out value="${TrajetTan.duration}"></c:out></p>
			</td>
		</tr>
	</table>
	</center>
</div>

</body>
</html>
