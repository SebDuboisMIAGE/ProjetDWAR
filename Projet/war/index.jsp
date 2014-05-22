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

<script type="text/javascript"
	src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA&sensor=true">
	
</script>

<script type="text/javascript">
	function initialize() {
		var latlng = new google.maps.LatLng(47.15, -1, 6079);//nantes

		var mapOptions = {
			center : latlng,
			zoom : 8,
			mapTypeId : google.maps.MapTypeId.ROADMAP};

		var carte = new google.maps.Map(document.getElementById("carte"),
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
			<!-- 
			<c:forEach items="${TrajetGoogleDriving.steps}" var="step">
				<c:out value="${step.depart.lat}"></c:out>
			</c:forEach>
			-->

		</div>
		<div id="liste_depart">
			<FORM>
				<SELECT name="listedepart" size="1">
				<c:forEach items="${ListeDepart}" var="adresse">
					<OPTION>
						<c:out value="${adresse.adresse}"></c:out>
					</OPTION>
				</c:forEach>
				</SELECT>
			</FORM>
		</div>
		<div id="liste_arrivee">
			<FORM>
				<SELECT name="listearrivee" size="1">
				<c:forEach items="${ListeArrivee}" var="adresse">
					<OPTION>
						<c:out value="${adresse.adresse}"></c:out>
					</OPTION>
				</c:forEach>
				</SELECT>
			</FORM>
		</div>
		<br/>
		<div id="pond_cost">
			Coût : 
			<input type="text" name="cost" id="cost"
					value="<c:out value="${donnees.cost}"/>" tabindex="1"
					class="pond"/>
		</div>
		<div id="pond_time">
			Temps : 
			<input type="text" name="time" id="time"
					value="<c:out value="${donnees.time}"/>" tabindex="1"
					class="pond"/>
		</div>
		<div id="pond_ecological">
			Ecologique : 
			<input type="text" name="ecolo" id="ecolo"
					value="<c:out value="${donnees.ecological}"/>" tabindex="1"
					class="pond"/>
		</div>
		<div id="pond_calorie">
			Calorie : 
			<input type="text" name="calorie" id="calorie"
					value="<c:out value="${donnees.calorie}"/>" tabindex="1"
					class="pond"/>
		</div>
	</div>

	<div id='<c:out value="${carte}"/>' style="width: 100%; height: 100%"></div>
</body>
</html>
