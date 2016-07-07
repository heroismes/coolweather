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
			JSONObject todayInfo = fiveDayInfo.getJSONObject(0);
			String cityName = jsonObject.getString("city");
			String temp1 = todayInfo.getString("high").split(" ")[1];
			String temp2 = todayInfo.getString("low").split(" ")[1];
			String weatherDesp = todayInfo.getString("type");
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//将返回的天气信息储存到sharedPreferences中
	public static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,String temp2,String weatherDesp){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", timeFormat.format(new Date()));
		editor.putString("current_date", simpleDateFormat.format(new Date()));
		editor.commit();
	}
}
