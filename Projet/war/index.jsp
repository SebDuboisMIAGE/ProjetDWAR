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
<style type="text/css">
html {
	height: 100%
}

body {
	height: 100%;
	margin: 0;
	padding: 0
}

#map-canvas {
	height: 75%
}

#Recherche {
	height: 25%;
	position: relative
}
</style>

<!-- set a JS variable with a EL -->
<script type="text/javascript">
	<c:set var="message" value="Hello"/>
	var message = '<c:out value="${message}"/>';
</script>





<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA&sensor=true">
	
</script>

<script type="text/javascript">
	function initialize() {
		var latlng = new google.maps.LatLng(47.15, -1, 6079);//nantes

		var mapOptions = {
			center : latlng,
			zoom : 8,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};

		var carte = new google.maps.Map(document.getElementById("carte"),
				mapOptions);
		//création des marqueurs de départ et d'arrivée
		var depart = new google.maps.Marker({
			position : new google.maps.LatLng('<c:out value="${TrajetGoogle.departureGPS.lat}"/>', '<c:out value="${TrajetGoogle.departureGPS.lng}"/>'),
			map : carte
		});
		var arrivee = new google.maps.Marker({
			position : new google.maps.LatLng('<c:out value="${TrajetGoogle.arrivalGPS.lat}"/>', '<c:out value="${TrajetGoogle.arrivalGPS.lng}"/>'),
			map : carte
		});

		//création de la polyline pour dessiner le trajet
		var parcoursBus = [
				new google.maps.LatLng(46.781367900048, 6.6401992834884),
				new google.maps.LatLng(46.780821285011, 6.6416348016222),
				new google.maps.LatLng(46.780496546047, 6.6421830461926),
				new google.maps.LatLng(46.779835306991, 6.6426765713417),
				new google.maps.LatLng(46.777748677169, 6.6518819126808),
				new google.maps.LatLng(46.778027878803, 6.6541349682533),
				new google.maps.LatLng(46.778484884759, 6.6557324922045),
				new google.maps.LatLng(46.778752327087, 6.6573654211838),
				new google.maps.LatLng(46.778605381016, 6.6588674582321) ];

		var traceParcoursBus = new google.maps.Polyline({
			path : parcoursBus,//chemin du tracé
			strokeColor : "#FF0000",//couleur du tracé
			strokeOpacity : 1.0,//opacité du tracé
			strokeWeight : 2
		//grosseur du tracé
		});

		//lier le tracé à la carte
		//ceci permet au tracé d'être affiché sur la carte
		traceParcoursBus.setMap(carte);

	}
	//google.maps.event.addDomListener(window, 'load', initialize);
</script>

</head>
<body onload="initialize()">
	<h1>SmartTraveler</h1>
	<div id="Recherche">
		<c:out value="Itinéraire" />
		<form name="input"
			action=<c:url value="/projet"/> method="post">
			<div id="departure">
				<label>Départ : <input type="text" name="from" id="from"
					value="<c:out value="${donnees.departure}"/>" tabindex="1"
					class="valid"></label>
			</div>
			<div id="arrival">
				<label>Arrivée : <input type="text" name="to" id="to"
					value="<c:out value="${donnees.arrival}"/>" tabindex="1"
					class="valid"></label>
			</div>
			<input type="submit" class="submitItinerary" value="Go !">
		</form>
	</div>
	<div id="Resultats_Google">
		<div id="res_depart">
			Adresse de départ :
			<c:out value="${TrajetGoogle.departure}"></c:out>
		</div>
		<div id="res_arrivee">
			Adresse de destination :
			<c:out value="${TrajetGoogle.arrival}"></c:out>
		</div>
		<div id="res_duree">
			Durée :
			<c:out value="${TrajetGoogle.duration}"></c:out>
		</div>
		<div id="res_distance">
			Distance :
			<c:out value="${TrajetGoogle.distance}"></c:out>
			km
		</div>

		<!-- 		<div id="res_consigne">
  			Trajet :
  			
  			<c:forEach items="${itineraire.steps}" var="step">
  				<c:out value="${step.value.consigne}"></c:out>
  			</c:forEach>
  			
  			
  		</div>
  	-->
	</div>

	<div id='<c:out value="${carte}"/>' style="width: 100%; height: 100%" ></div>
</body>
</html>
