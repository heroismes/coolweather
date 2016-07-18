package model;

public class WeatherInfo {

	private int id;
	private String countryName;
	private String weatherCode;
	private String highTemp;
	private String lowTemp;
	private String weatherType;
	private String updateTime;
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getUpdateTime(){
		return updateTime;
	}
	
	public void setUpdateTime(String updateTime){
		this.updateTime = updateTime;
	}
	
	public String getWeatherType(){
		return weatherType;
	}
	
	public void setWeatherType(String weatherType){
		this.weatherType = weatherType;
	}
	
	public String getCountryName(){
		return countryName;
	}
	
	public void setCountryName(String countryName){
		this.countryName = countryName;
	}
	
	public String getWeatherCode(){
		return weatherCode;
	}
	
	public void setWeatherCode(String weatherCode){
		this.weatherCode = weatherCode;
	}
	
	public String getHighTemp(){
		return highTemp;
	}
	
	public void setHighTemp(String highTemp){
		this.highTemp = highTemp;
	}
	
	public String getLowTemp(){
		return lowTemp;
	}
	
	public void setLowTemp(String lowTemp){
		this.lowTemp = lowTemp;
	}
}
