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
			String[][] weatherInfos = new String[6][3];
			for(int i=0;i<fiveDayInfo.length();i++){
				JSONObject object = fiveDayInfo.getJSONObject(i);
				//��ȡ�������ͼ��¶�
				weatherInfos[i][0] = object.getString("type");
				weatherInfos[i][1] = object.getString("high").split(" ")[1].split("��")[0];
				weatherInfos[i][2] = object.getString("low").split(" ")[1];
			}
			//��ȡ�����������Ϣ
			JSONObject yesWeahterInfo = jsonObject.getJSONObject("yesterday");
			weatherInfos[5][0] = yesWeahterInfo.getString("type");
			weatherInfos[5][1] = yesWeahterInfo.getString("high").split(" ")[1].split("��")[0];
			weatherInfos[5][2] = yesWeahterInfo.getString("low").split(" ")[1];
			
			JSONObject todayInfo = fiveDayInfo.getJSONObject(0);
			String currentTemp = jsonObject.getString("wendu");
			String warns = jsonObject.getString("ganmao");
			String cityName = jsonObject.getString("city");
			String week = todayInfo.getString("date").split("��")[1];
			String weatherDesp = todayInfo.getString("type");
			//saveCarfuWeather(weatherCode, cityName, temp1, temp2, CoolWeatherDB db);
			saveWeatherInfo(context,cityName,weatherCode,weatherDesp,week,currentTemp,weatherInfos);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	//�����ص�������Ϣ���浽sharedPreferences��
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
	
	//�����û���ע�ĳ����б��������Ϣ
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
