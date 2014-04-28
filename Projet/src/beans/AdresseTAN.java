package beans;

public class AdresseTAN {
	private String adresse;
	private String idTan;
	
	public AdresseTAN(){}
	
	public void setAdresse(String a){
		this.adresse = a;
	}
	
	public void setIdTAN(String id){
		this.idTan = id;
	}
	
	public String getAdresse(){
		return this.adresse;
	}
	
	public String getIdTAN(){
		return this.idTan;
	}
}
