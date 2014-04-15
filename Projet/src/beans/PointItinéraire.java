package beans;

public class PointItinéraire {

	private GPSCoordonate depart;
	private GPSCoordonate arrivee;
	private float duree;
	private String consigne;
	
	public GPSCoordonate getArrivee() {
		return arrivee;
	}
	
	public String getConsigne() {
		return consigne;
	}
	
	public GPSCoordonate getDepart() {
		return depart;
	}
	
	public float getDuree() {
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
	
	public void setDuree(float duree) {
		this.duree = duree;
	}
}
