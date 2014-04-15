<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!-- The HTML 4.01 Transitional DOCTYPE declaration-->
<!-- above set at the top of the file will set     -->
<!-- the browser's rendering engine into           -->
<!-- "Quirks Mode". Replacing this declaration     -->
<!-- with a "Standards Mode" doctype is supported, -->
<!-- but may lead to some differences in layout.   -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<html>
  <head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
    <style type="text/css">
      html { height: 100% }
      body { height: 100%; margin: 0; padding: 0 }
      #map-canvas { height : 75% }
      #Recherche { height: 25%; position: relative }
    </style>
    <script type="text/javascript"
      src="https://maps.googleapis.com/maps/api/js?key=AIzaSyA3ol1gtWbndHLBeXy0AWIDFDBx6JnLMZA&sensor=true">
    </script>
    <script type="text/javascript">
      function initialize() {
    	var map = null;
        
    	var mapOptions = {
          center: new google.maps.LatLng(47.15, -1,6079),
          zoom: 8
        };
		
        map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
        
      }
	  google.maps.event.addDomListener(window, 'load', initialize);
    </script>
  </head>
  <body>  	
  	<div id="Recherche">
  		<c:out value="Itinéraire" />
  		<form name="input" action="http://localhost:8888/projet" method="post">
	  		<div id="departure">
	  			<input type="text" name="from" id="from" value="" tabindex="1" placeholder="Départ" class="valid" autocomplete="off">
	  		</div>
	  		<div id="arrival">
	  			<input type="text" name="to" id="to" value="" tabindex="1" placeholder="Arrivée" class="valid" autocomplete="off">
	  		</div>
	  		<input type="submit" class="submitItinerary" value="Rechercher">
	  	</form>
  	</div>	
	<div id="map-canvas"/> 
  </body>
</html>
