package beans;

import java.util.List;

public class EtapeTAN {

	private boolean marche;
	private String numligne;
	private String libelleArret;
	private List<GPSCoordonate> coordonnees;
	private String heureDepart;
	private String heureArrivee;
	private String duree;
	
	public boolean isMarche() {
		return marche;
	}
	public void setMarche(boolean marche) {
		this.marche = marche;
	}
	public String getNumligne() {
		return numligne;
	}
	public void setNumligne(String numligne) {
		this.numligne = numligne;
	}
	public String getLibelleArret() {
		return libelleArret;
	}
	public void setLibelleArret(String libelleArret) {
		this.libelleArret = libelleArret;
	}
	public String getHeureDepart() {
		return heureDepart;
	}
	public void setHeureDepart(String heureDepart) {
		this.heureDepart = heureDepart;
	}
	public String getHeureArrivee() {
		return heureArrivee;
	}
	public void setHeureArrivee(String heureArrivee) {
		this.heureArrivee = heureArrivee;
	}
	public String getDuree() {
		return duree;
	}
	public void setDuree(String duree) {
		this.duree = duree;
	}
	public List<GPSCoordonate> getCoordonnees() {
		return coordonnees;
	}
	public void setCoordonnees(List<GPSCoordonate> coordonnees) {
		this.coordonnees = coordonnees;
	}
	
}
