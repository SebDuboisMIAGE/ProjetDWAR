package beans;

import java.util.ArrayList;
import java.util.List;

public class TrajetGoogle {

	private double distance;  // en m
	private double duration; // en m
	private String departure;
	private String arrival;
	private List<EtapeGoogle> steps;
	
	public String getArrival() {
		return arrival;
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
	
	public void setArrival(String arrivee) {
		this.arrival = arrivee;
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
