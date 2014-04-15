package beans;

import java.util.ArrayList;
import java.util.List;

public class Itineraire {

	private double distance;  // en m
	private double duration; // en m
	private String departure;
	private String arrivee;
	private List<PointItineraire> steps;
	
	public String getArrivee() {
		return arrivee;
	}
	
	public String getDeparture() {
		return departure;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public double getDuration() {
		return duration;
	}
	
	public void setArrivee(String arrivee) {
		this.arrivee = arrivee;
	}
	
	public void setDeparture(String departure) {
		this.departure = departure;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public void setDuration(double duration) {
		this.duration = duration;
	}
	
	public List<PointItineraire> getSteps() {
		return steps;
	}
	
	public void setSteps(PointItineraire step) {
		if (this.steps == null)
		{
			this.steps = new ArrayList<PointItineraire>();
		}
		this.steps.add(step);
	}
}
