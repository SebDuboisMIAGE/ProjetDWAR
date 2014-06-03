<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<title>SmartTraveler</title>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<link href="<c:url value="/stylesheets/main.css" />" rel="stylesheet">
<link href="<c:url value="/javascript/main.js" />" rel="javascript">
<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA&sensor=true">
	
</script>
<!--
<script type="text/javascript">
	 function initialize() {
		var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.departureGPS.lng}"/>');//nantes

		var mapOptions = {
			center : latlng,
			zoom : 12,
			mapTypeId : google.maps.MapTypeId.ROADMAP};

		var carte = new google.maps.Map(document.getElementById("<c:out value="${visibility_carte}"/>"),
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

function trajetVoiture() 
{ 
	document.getElementById('res_depart').innerHTML = "Adresse de départ :<c:out value="${TrajetGoogleDriving.departure}"></c:out>";
	document.getElementById('res_arrivee').innerHTML = "Adresse de destination :<c:out value="${TrajetGoogleDriving.arrival}"></c:out>";
	document.getElementById('res_duree').innerHTML = "Durée :<c:out value="${TrajetGoogleDriving.duration}"></c:out>";
	document.getElementById('res_distance').innerHTML = "Distance :<c:out value="${TrajetGoogleDriving.distance}"></c:out>";
	document.getElementById('res_consigne').innerHTML = "Trajet :<c:forEach items="${TrajetGoogleDriving.steps}" var="step"><c:out value="${step.consigne}"></c:out></c:forEach>";

	//Raffraichissement de la map
	var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleDriving.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleDriving.departureGPS.lng}"/>');//nantes

	var mapOptions = {
		center : latlng,
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP};

	var carte = new google.maps.Map(document.getElementById("carte"),mapOptions);
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
	document.getElementById('res_depart').innerHTML = "Adresse de départ :<c:out value="${TrajetGoogleWalking.departure}"></c:out>";
	document.getElementById('res_arrivee').innerHTML = "Adresse de destination :<c:out value="${TrajetGoogleWalking.arrival}"></c:out>";
	document.getElementById('res_duree').innerHTML = "Durée :<c:out value="${TrajetGoogleWalking.duration}"></c:out>";
	document.getElementById('res_distance').innerHTML = "Distance :<c:out value="${TrajetGoogleWalking.distance}"></c:out>";
	document.getElementById('res_consigne').innerHTML = "Trajet :<c:forEach items="${TrajetGoogleWalking.steps}" var="step"><c:out value="${step.consigne}"></c:out></c:forEach>";

	//Raffraichissement de la map
	var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleWalking.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleWalking.departureGPS.lng}"/>');//nantes

	var mapOptions = {
		center : latlng,
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP};

	var carte = new google.maps.Map(document.getElementById("carte"),mapOptions);
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
	document.getElementById('res_depart').innerHTML = "Adresse de départ :<c:out value="${TrajetGoogleBicycling.departure}"></c:out>";
	document.getElementById('res_arrivee').innerHTML = "Adresse de destination :<c:out value="${TrajetGoogleBicycling.arrival}"></c:out>";
	document.getElementById('res_duree').innerHTML = "Durée :<c:out value="${TrajetGoogleBicycling.duration}"></c:out>";
	document.getElementById('res_distance').innerHTML = "Distance :<c:out value="${TrajetGoogleBicycling.distance}"></c:out>";
	document.getElementById('res_consigne').innerHTML = "Trajet :<c:forEach items="${TrajetGoogleBicycling.steps}" var="step"><c:out value="${step.consigne}"></c:out></c:forEach>";

	//Raffraichissement de la map
	var latlng = new google.maps.LatLng('<c:out value="${TrajetGoogleBicycling.departureGPS.lat}"/>', '<c:out value="${TrajetGoogleBicycling.departureGPS.lng}"/>');//nantes

	var mapOptions = {
		center : latlng,
		zoom : 12,
		mapTypeId : google.maps.MapTypeId.ROADMAP};

	var carte = new google.maps.Map(document.getElementById("carte"),mapOptions);
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

</script>
 -->
<script>
	function initialize() {
		var carte;
		carte = initialize_A('<c:out value="${visibility_carte}"/>',
				'<c:out value="${TrajetGoogleDriving.departureGPS.lat}"/>',
				'<c:out value="${TrajetGoogleDriving.departureGPS.lng}"/>',
				'<c:out value="${TrajetGoogleDriving.arrivalGPS.lat}"/>',
				'<c:out value="${TrajetGoogleDriving.arrivalGPS.lng}"/>');
		var parcours = new Array();
		<c:forEach items="${TrajetGoogleDriving.steps}" var="step">
		<c:forEach items="${step.way}" var="w">
		pushElemTracer(parcours, '<c:out value="${w.lat}"/>',
				'<c:out value="${w.lng}"/>');
		</c:forEach>
		</c:forEach>
		tracer(parcours, carte);
	}

	function consignes() {
		<c:forEach items="${TrajetGoogleDriving.steps}" var="step">
		trajetConsigne('<c:out value="${step.consigne}"/>');
		</c:forEach>
	}
	function initialize_A(id_carte, lat_dep, long_dep, lat_ar, long_ar) {
		var latlng = new google.maps.LatLng(47.15, -1, 6079);// nantes

		var mapOptions = {
			center : latlng,
			zoom : 8,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};

		var carte = new google.maps.Map(document.getElementById(id_carte),
				mapOptions);
		// création des marqueurs de départ et d'arrivée
		var depart = new google.maps.Marker({
			position : new google.maps.LatLng(lat_dep, long_dep),
			map : carte

		});
		var arrivee = new google.maps.Marker({
			position : new google.maps.LatLng(lat_ar, long_ar),
			map : carte
		});
		return carte;
	}

	function pushElemTracer(parcours, lat, long) {
		parcours.push(new google.maps.LatLng(lat, long));
	}

	function tracer(tracer, carte) {
		traceParcoursCar.setMap(carte);
	}

	function trajetIndic(depart, arrivee, duree, distance) {
		document.getElementById('res_depart').innerHTML = "Adresse de départ : "
				+ depart;
		document.getElementById('res_arrivee').innerHTML = "Adresse de destination : "
				+ arrivee;
		document.getElementById('res_duree').innerHTML = "Durée : " + duree;
		document.getElementById('res_distance').innerHTML = "Distance : "
				+ distance;
		consignes();
	}
	function trajetConsigne(consigne) {
		document.getElementById('res_consigne').innerHTML = "Trajet : "
				+ consigne;
	}
</script>
</head>
<body onload="initialize()">

	<h1>SmartTraveler</h1>
	<IMG class="displayed" src="bannieretest.jpg" alt="banniere">
	<div class=transition></div>
	<div id='<c:out value="${visibility_rechercher}"/>' class="Recherche">

		<form name="input"
			action=<c:url value="http://localhost:8888/recherche"/> method="post">
			<div id="departure">
				<h2>
					Départ : <input type="text" name="from" id="from"
						value="<c:out value="${donnees.departure}"/>" tabindex="1"
						class="valid">
				</h2>
			</div>
			<div id="arrival">
				<h2>
					Arrivée : <input type="text" name="to" id="to"
						value="<c:out value="${donnees.arrival}"/>" tabindex="1"
						class="valid">
				</h2>
			</div>
			<input type="submit" class="submitItinerary" value="Rechercher !">
		</form>
	</div>



	<div id='<c:out value="${visibility_calculer}"/>' class="Calculer">
		<div class=transition></div>
		<form name="input"
			action=<c:url value="http://localhost:8888/itineraire"/>
			method="post">
			<SELECT name="listedepart" id="listedepart" size="1">
				<c:forEach items="${ListeDepart}" var="adresse">
					<OPTION>
						<c:out value="${adresse.adresse}"></c:out>
					</OPTION>
				</c:forEach>
			</SELECT> <SELECT name="listearrivee" size="1">
				<c:forEach items="${ListeArrivee}" var="adresse">
					<OPTION>
						<c:out value="${adresse.adresse}"></c:out>
					</OPTION>
				</c:forEach>
			</SELECT> <input type="submit" class="submitItinerary"
				value="Calculer l'itinéraire !">
		</form>

		<div id="pond_cost">
			Coût : <input type="text" name="cost" id="cost"
				value="<c:out value="${donnees.cost}"/>" tabindex="1" class="pond" />
		</div>
		<div id="pond_time">
			Temps : <input type="text" name="time" id="time"
				value="<c:out value="${donnees.time}"/>" tabindex="1" class="pond" />
		</div>
		<div id="pond_ecological">
			Ecologique : <input type="text" name="ecolo" id="ecolo"
				value="<c:out value="${donnees.ecological}"/>" tabindex="1"
				class="pond" />
		</div>
		<div id="pond_calorie">
			Calorie : <input type="text" name="calorie" id="calorie"
				value="<c:out value="${donnees.calorie}"/>" tabindex="1"
				class="pond" />
		</div>
	</div>



	<div id='<c:out value="${visibility_resultat}"/>' class="Resultats">
		<div class=transition></div>

		<input type="button"
			onclick="trajetIndic('<c:out value="${TrajetGoogleDriving.departure}"/>','<c:out value="${TrajetGoogleDriving.arrival}"/>','<c:out value="${TrajetGoogleDriving.duration}"/>','<c:out value="${TrajetGoogleDriving.distance}"/>')"
			value="voiture" /> <input type="button"
			onclick="trajetIndic('<c:out value="${TrajetGoogleWalking.departure}"/>','<c:out value="${TrajetGoogleWalking.arrival}"/>','<c:out value="${TrajetGoogleWalking.duration}"/>','<c:out value="${TrajetGoogleWalking.distance}"/>')"
			value="pieton" /> <input type="button"
			onclick="trajetIndic('<c:out value="${TrajetGoogleBiking.departure}"/>','<c:out value="${TrajetGoogleBiking.arrival}"/>','<c:out value="${TrajetGoogleBiking.duration}"/>','<c:out value="${TrajetGoogleBiking.distance}"/>')"
			value="velo" /> <input type="button" onclick="trajetTAN()"
			value="tramway/Bus" />

		<div id="res_depart">
			Adresse de départ :
			<c:out value="${TrajetGoogleDriving.departure}"></c:out>
		</div>
		<div id="res_arrivee">
			Adresse de destination :
			<c:out value="${TrajetGoogleDriving.arrival}"></c:out>
		</div>
		<div id="res_duree">
			Durée :
			<c:out value="${TrajetGoogleDriving.duration}"></c:out>
		</div>
		<div id="res_distance">
			Distance :
			<c:out value="${TrajetGoogleDriving.distance}"></c:out>
		</div>
		<div id="res_consigne">
			Trajet :
			<c:forEach items="${TrajetGoogleDriving.steps}" var="step">
				<c:out value="${step.consigne}"></c:out>
			</c:forEach>

		</div>
	</div>




	<div id='<c:out value="${visibility_carte}"/>' class="Carte"></div>
</body>
</html>
