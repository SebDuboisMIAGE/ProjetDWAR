package beans;

public class PointItineraire {

	private GPSCoordonate depart;
	private GPSCoordonate arrivee;
	private double duree;
	private String consigne;
	private double distance;
	
	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public GPSCoordonate getArrivee() {
		return arrivee;
	}
	
	public String getConsigne() {
		return consigne;
	}
	
	public GPSCoordonate getDepart() {
		return depart;
	}
	
	public double getDuree() {
		return duree;
	}
	
	public void setArrivee(GPSCoordonate arrivee) {
		this.arrivee = arrivee;
	}
	
	public void setConsigne(String consigne) {
		this.consigne = consigne;
	}
	
	public void setDepart(GPSCoordonate depart) {
		this.depart = depart;
	}
	
	public void setDuree(double duree) {
		this.duree = duree;
	}
}