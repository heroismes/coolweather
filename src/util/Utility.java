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

	//����ʡ������
	public synchronized static boolean handleProvincesResponse(String response,CoolWeatherDB db){
		if(!TextUtils.isEmpty(response)){
			String[] provinces = response.split(",");
			if (provinces != null && provinces.length > 0) {
				for(String p : provinces){
					String[] data = p.split("\\|");
					Province province = new Province();
					province.setProvinceName(data[1]);
					province.setProvinceCode(data[0]);
					//�����ݼ��뵽���ݿ���
					db.saveProvince(province);
				}
				return true;
			}
			
		}
		return false;
	}
	//�����������
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
	
	//�����ؼ�����
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
	
	//�������������ص�����json���ݣ����������������ݴ��浽����
	public static void handleWeatherResponse(Context context,String response,String weatherCode){
		try {
			JSONObject weatherInfo = new JSONObject(response);
			JSONObject jsonObject = weatherInfo.getJSONObject("data");
			JSONArray fiveDayInfo = jsonObject.getJSONArray("forecast");
			JSONObject todayInfo = fiveDayInfo.getJSONObject(0);
			String roomTemp = jsonObject.getString("wendu");
			String environment = jsonObject.getString("ganmao");
			String cityName = jsonObject.getString("city");
			String week = todayInfo.getString("date").split("��")[1];
			String temp1 = todayInfo.getString("high").split(" ")[1];
			String temp2 = todayInfo.getString("low").split(" ")[1];
			String weatherDesp = todayInfo.getString("type");
			//saveCarfuWeather(weatherCode, cityName, temp1, temp2, CoolWeatherDB db);
			saveWeatherInfo(context,cityName,weatherCode,temp1,temp2,weatherDesp,week);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//�����ص�������Ϣ���浽sharedPreferences��
	public static void saveWeatherInfo(Context context,String cityName,String weatherCode,String temp1,String temp2,String weatherDesp,String week){
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
		editor.putString("week", week);
		editor.commit();
	}
	
	//�ѹ�ע���е���������������ݿ�
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
}
