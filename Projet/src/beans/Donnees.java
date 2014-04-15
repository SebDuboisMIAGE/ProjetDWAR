package beans;

public class Donnees {
	
	private String departure;
	private String arrival;
	
	public void setDeparture(String dep){
		this.departure = dep;
	}
	public String getDeparture(){
		return departure;
	}
	public void setArrival (String arrivee){
		this.arrival= arrivee;
	}
	public String getArrival(){
		return arrival;
	}
}

