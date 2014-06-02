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
	document.getElementById('res_consigne').innerHTML = "Trajet : " + consigne;
}
