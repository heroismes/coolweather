package util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import android.R.integer;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import model.City;
import model.CoolWeatherDB;
import model.Country;
import model.Province;
import model.WeatherInfo;

public class Utility {

	//处理省级数据
	public synchronized static boolean handleProvincesResponse(String response,CoolWeatherDB db){
		if(!TextUtils.isEmpty(response)){
			String[] provinces = response.split(",");
			if (provinces != null && provinces.length > 0) {
				for(String p : provinces){
					String[] data = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(data[1]);
					province.setProvinceCode(data[0]);
					//把数据加入到数据库中
					db.saveProvince(province);
				}
				return true;
			}
			
		}
		return false;
	}
	//保存城市数据
	public synchronized static boolean handleCitiesResponse(String response,int provinceId,CoolWeatherDB db){
		if(!TextUtils.isEmpty(response)){
			String[] allcities = response.split(",");
			if (allcities != null && allcities.length > 0) {
				for(String c:allcities){
					String[] array = c.split("\\|");
					City city = new City();
					city.setCityCode(array[0]);
					city.setCityName(array[1]);
					city.setProvinceId(provinceId);
					db.saveCity(city);
				}
				
				return true;
			}
		}
		return false;
	}
	
	//保存县级数据
	public synchronized static boolean handleCountriesResponse(String response,int cityId,CoolWeatherDB db){
		if (!TextUtils.isEmpty(response)) {
			String[] allCountries = response.split(",");
			if (allCountries != null && allCountries.length > 0) {
				for(String c:allCountries){
					String[] array = c.split("\\|");
					Country country = new Country();
					country.setCountryCode(array[0]);
					country.setCountryName(array[1]);
					country.setCityId(cityId);
					
					db.saveCountry(country);
				}
				return true;
			}
		}
		return false;
	}
	
	//解析服务器返回的天气json数据，并将解析出的数据储存到本地
	public static void handleWeatherResponse(Context context,String response,String weatherCode){
		try {
			JSONObject weatherInfo = new JSONObject(response);
			JSONObject jsonObject = weatherInfo.getJSONObject("data");
			JSONArray fiveDayInfo = jsonObject.getJSONArray("forecast");
			String[][] weatherInfos = new String[6][3];
			for(int i=0;i<fiveDayInfo.length();i++){
				JSONObject object = fiveDayInfo.getJSONObject(i);
				//获取天气类型及温度
				weatherInfos[i][0] = object.getString("type");
				weatherInfos[i][1] = object.getString("high").split(" ")[1].split("℃")[0];
				weatherInfos[i][2] = object.getString("low").split(" ")[1];
			}
			//获取昨天的天气信息
			JSONObject yesWeahterInfo = jsonObject.getJSONObject("yesterday");
			weatherInfos[5][0] = yesWeahterInfo.getString("type");
			weatherInfos[5][1] = yesWeahterInfo.getString("high").split(" ")[1].split("℃")[0];
			weatherInfos[5][2] = yesWeahterInfo.getString("low").split(" ")[1];
			
			JSONObject todayInfo = fiveDayInfo.getJSONObject(0);
			String currentTemp = jsonObject.getString("wendu");
			String warns = jsonObject.getString("ganmao");
			String cityName = jsonObject.getString("city");
			String week = todayInfo.getString("date").split("日")[1];
			String weatherDesp = todayInfo.getString("type");
			//saveCarfuWeather(weatherCode, cityName, temp1, temp2, CoolWeatherDB db);
			saveWeatherInfo(context,cityName,weatherCode,weatherDesp,week,currentTemp,weatherInfos);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//将返回的天气信息储存到sharedPreferences中
	public static void saveWeatherInfo(Context context,String cityName,String weatherCode,String weatherDesp,String week,String currentTemp,String[][] weatherInfos){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("cur_temp", currentTemp);
		editor.putString("publish_time", timeFormat.format(new Date()));
		editor.putString("current_date", simpleDateFormat.format(new Date()));
		editor.putString("week", week);
		
		for(int i=0;i<weatherInfos.length;i++){
			for(int j=0;j<weatherInfos[i].length;j++){
				editor.putString("temp"+i+j, weatherInfos[i][j]);
			}
		}
		
		editor.commit();
	}
	
	//把关注城市的天气代码存入数据库
	public static void saveCarfuWeather(String weatherCode,String countryName,String highTemp,String lowTemp,String weatherType,CoolWeatherDB db){
		if (!TextUtils.isEmpty(weatherCode)) {
			WeatherInfo weatherInfo = new WeatherInfo();
			weatherInfo.setCountryName(countryName);
			weatherInfo.setWeatherCode(weatherCode);
			weatherInfo.setHighTemp(highTemp);
			weatherInfo.setLowTemp(lowTemp);
			weatherInfo.setWeatherType(weatherType);
			
			db.saveWeatherInfo(weatherInfo);
		}
	}
	
	//更新用户关注的城市列表的天气信息
	public static void updateWeatherInfo(String weatherCode,String response,CoolWeatherDB db){
		try {
			JSONObject weatherInfo = new JSONObject(response);
			JSONObject jsonObject = weatherInfo.getJSONObject("data");
			JSONArray fiveDayInfo = jsonObject.getJSONArray("forecast");
			JSONObject todayInfo = fiveDayInfo.getJSONObject(0);
			
			String temp1 = todayInfo.getString("high").split(" ")[1];
			String temp2 = todayInfo.getString("low").split(" ")[1];
			String weatherDesp = todayInfo.getString("type");
			
			db.updateWeatherInfo(weatherCode, temp1, temp2, weatherDesp);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
