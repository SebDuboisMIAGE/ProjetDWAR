package beans;

import java.util.ArrayList;
import java.util.List;

public class TrajetGoogle {

	private String distance;  // en m
	private String duration; // en m
	private String departure;
	private String arrival;
	private GPSCoordonate departureGPS;
	private GPSCoordonate arrivalGPS;
	private List<EtapeGoogle> steps;
		
	public GPSCoordonate getArrivalGPS() {
		return arrivalGPS;
	}
	
public GPSCoordonate getDepartureGPS() {
	return departureGPS;
}

public void setArrivalGPS(GPSCoordonate arrivalGPS) {
	this.arrivalGPS = arrivalGPS;
}

public void setDepartureGPS(GPSCoordonate departureGPS) {
	this.departureGPS = departureGPS;
}
	
	public String getArrival() {
		return arrival;
	}
	
	public String getDeparture() {
		return departure;
	}
	
	public String getDistance() {
		return distance;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public void setArrival(String arrivee) {
		this.arrival = arrivee;
	}
	
	public void setDeparture(String departure) {
		this.departure = departure;
	}
	
	public void setDistance(String distance) {
		this.distance = distance;
	}
	
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	public List<EtapeGoogle> getSteps() {
		return steps;
	}
	
	public void setSteps(EtapeGoogle step) {
		if (this.steps == null)
		{
			this.steps = new ArrayList<EtapeGoogle>();
		}
		this.steps.add(step);
	}
}
