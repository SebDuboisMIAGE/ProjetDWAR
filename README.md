ProjetDWAR
==========
DUBOIS Sébastien
FONTAINE Julien
MORIN Louis





				
Smart Traveler – Spécifications
 
L’objectif de notre application est de pouvoir proposer un service comparatif d’un itinéraire (sur l’agglomération nantaise) demandé en fonction de plusieurs moyens de transport : voiture, vélo, transport en commun et à pied. 
Nous utiliserons pour cela les capacités des servlets pour effectuer tous les traitements back office, c’est-à-dire toutes les requêtes vers les organismes externes (TAN, Google API), ainsi que tous les calculs d’itinéraires. 
Etant donné que nous proposons un comparatif avec les transports en commun du réseau TAN, nous devons nous fier aux adresses que la TAN propose dans ses services. 
Pour cela, l’utilisateur souhaitant effectuer un comparatif devra tout d’abord effectuer une recherche en saisissant une adresse de départ et une adresse d’arrivée. 
En fonction des saisies de l’utilisateur, nous contactons par requête l’API TAN afin de récupérer une liste d’adresse correspondant à la recherche. L’utilisateur choisit ensuite dans les deux listes déroulantes les adresses qu’il souhaite puis lance le calcul de l’itinéraire selon tous les modes de transport.

Principe du Servlet de Recherche :
Dans ce servlet, nous récupérons l’adresse de départ et l’adresse d’arrivée que l’utilisateur a saisie dans l’interface Web. 
Pour chacune de ces deux adresses, nous récupérons la liste des adresses TAN correspondantes. 
Pour cela, par un appel aux services TAN via une requête POST http et un URL de type https://www.tan.fr/ewp/mhv.php/itineraire/address.json?nom=adressesaisie, nous récupérons une chaine de caractère au format JSON, que nous devons parser pour en extraire les résultats attendus, à savoir la liste des adresses TAN correspondantes à une adresse saisie.	 Les deux listes sont par la suite renvoyées vers la page pour être affichées à l’utilisateur.

Principe du Servlet de Calcul d’itinéraire :
Après que l’utilisateur ait choisi une adresse de départ et une adresse d’arrivée parmi les listes d’adresse TAN récupérées par le servlet de Recherche, un deuxième servlet permet le calcul de l’itinéraire en fonction de plusieurs moyens de transport. 
Quatre moyens de transport seront comparés par le servlet, selon différentes méthodes de traitement. 
Premièrement, le servlet calcule l’itinéraire demandé selon le mode de transport Voiture. 
Pour ceci, l’API Google est notre seul service indispensable. 
Pour ce faire, nous effectuons un appel aux services Google via une requête POST http et une URL de type https://maps.googleapis.com/maps/api/directions/json?origin=adresseDep&destination=adresseArr&mode=Driving, de la même façon que pour le Servlet de Recherche, nous récupérons une chaine de caractère au format JSON, que nous parsons pour en récupérer les informations dont nous avons besoin, comprenant par ailleurs la liste des coordonnées GPS permettant le tracé futur de l’itinéraire sur une carte.
De la même façon que l’itinéraire Voiture, l’itinéraire Vélo et l’itinéraire Piéton font appels aux services Google afin de récupérer les informations des itinéraires. 
La seule chose qui change est le mode de transport renseigné dans l’URL, qui était « Driving » pour l’itinéraire Voiture, et qui changera en « Bicycling » pour l’itinéraire vélo et en « Walking » pour l’itinéraire piéton. 
Le reste du traitement reste inchangé.
La partie la plus complexe reste le calcul de l’itinéraire TAN. 
Tout d’abord, la TAN a choisi de donner un identifiant unique pour chacune des adresses contenues dans son service. 
En fonction des adresses choisies par l’utilisateur, on commence ainsi par récupérer les identifiant des deux adresses en effectuant un appel au service TAN via une requête http (Pratiquement la même que pour la recherche, mais nous ne récupérons pas une liste, juste une seule adresse).
Une fois les identifiants de l’adresse de départ et d’arrivée récupérés, nous pouvons exécuter la requête POST http qui permettra de récupérer les informations de l’itinéraire TAN. L’URL de ce nouvel appel aux services TAN est du type https://www.tan.fr/ewp/mhv.php/itineraire/resultat.json?depart=idDep&arrive=idArr&... . 
La réponse récupérée au format JSON est alors parsée, et c’est au moment du parsage que le traitement est complexe. 
L’itinéraire peut être découpé en plusieurs sous itinéraire, par exemple, une partie à pied, l’autre en bus et Tramway. 
Dans la cas d’un sous itinéraire piéton, la consigne est de se rendre à pied jusqu’à un arrêt TAN. 
A ce niveau-là, nous ne savons pas encore à quelle ligne appartient l’arrêt renseigné. 
Il faut donc sauvegarder le libellé de l’arrêt dans un premier. 
Après chaque période de marche (sauf si c’est le dernier), il y a un sous itinéraire TAN, qui indique l’arrêt TAN de départ, l’arrêt TAN d’arrivée et la ligne. 
Afin de calculer l’itinéraire TAN, nous aurons besoin de plusieurs fichiers disponibles sur l’open Data de Nantes, un (Stops.txt) qui liste tous les arrêts TAN en fonction de leur libellé, de leurs coordonnées GPS, et un second (Shapes.txt) qui liste les coordonnées GPS d’une ligne dans l’ordre. 
Dans un premier temps, on récupère la liste des adresses TAN dans le fichier Stops.txt en fonction du libellé des arrêts. 
Attention, un arrêt peut être présent sur différentes lignes et ce fichier ne renseigne pas la ligne, c’est pourquoi on récupère une liste et non un seul élément. 
A partir de ces deux listes, nous effectuons une recherche dans le fichier Shapes.txt afin de garder uniquement l’adresse TAN qui correspond à la ligne recherchée. Nous nous retrouvons ainsi avec les adresses TAN (libellé + coordonnées GPS) de l’arrêt de départ et l’arrêt d’arrivée. Il ne reste donc plus qu’à parcourir le fichier Shapes.txt afin de trouver tous les arrêts intermédiaires entre l’arrêt de départ et l’arrêt d’arrivée.
On répète ce traitement pour tous les sous itinéraires, afin de construire l’itinéraire global. 
Les quatre itinéraires sont donc renvoyés vers la page Web pour être affichés à l’utilisateur.
Affichage des résultats :
On affiche la Map sur toute résolution de la page. 
La recherche est en fait un volet glissant qui peut être réduit selon le besoin de l’utilisateur. 
Lorsque les itinéraires ont été calculés par le servlet, une fenêtre de type draggable permet de basculer entre les différents itinéraires selon le mode de transport sélectionné. En fonction du mode de transport sélectionné, l’affichage du trajet sur la Map change en conséquence. La fenêtre draggable affiche de plus un tableau comparatif entre les différents moyens de transport : Durée, Distance, calories dépensées, prix. Par manque de temps, nous n’avons pas implémenté les fonctions de calcul de coûts, calorie, écologie

Historique des recherches :
Notre application permet une authentification par les comptes Google. 
Grâce à cette authentification, nous gérons un historique des recherches de l’utilisateur connecté. 
Attention, l’historique comprend les trois dernières recherches effectuées, pour des raisons de surcharge.
