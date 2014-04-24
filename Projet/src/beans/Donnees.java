package beans;

public class Donnees {
	
	private String departure;
	private String arrival;
	private int cost;
	private int time;
	private int ecological;
	private int calorie;
	
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
	public void setCost(int cost) {
		this.cost = cost;
	}
	public int getCost() {
		return cost;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public int getTime() {
		return time;
	}
	public void setEcological(int ecological) {
		this.ecological = ecological;
	}
	public int getEcological() {
		return ecological;
	}
	public void setCalorie(int calorie) {
		this.calorie = calorie;
	}
	public int getCalorie() {
		return calorie;
	}
}

