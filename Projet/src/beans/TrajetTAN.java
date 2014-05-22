package beans;

import java.util.ArrayList;
import java.util.List;

public class TrajetTAN {
	//TODO
	private String duration; // en m
	private String departure;
	private String arrival;
	private String heureDepart;
	private String heureArrivee;
	private String correspondance;
	
	private List<EtapeTAN> steps;
	
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getDeparture() {
		return departure;
	}
	public void setDeparture(String departure) {
		this.departure = departure;
	}
	public String getArrival() {
		return arrival;
	}
	public void setArrival(String arrival) {
		this.arrival = arrival;
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
	public String getCorrespondance() {
		return correspondance;
	}
	public void setCorrespondance(String correspondance) {
		this.correspondance = correspondance;
	}
	public List<EtapeTAN> getSteps() {
		return steps;
	}
	public void setSteps(EtapeTAN step) {
		if (this.steps == null)
		{
			this.steps = new ArrayList<EtapeTAN>();
		}
		this.steps.add(step);
	}
	
	

}
