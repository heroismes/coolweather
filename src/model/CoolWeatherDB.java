package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.VoicemailContract.Voicemails;
import android.text.TextUtils;
import db.CoolWeatherOpenHelper;

public class CoolWeatherDB {

	//数据库名
	private static final String DB_NAME = "cool_weather";
	//数据库版本
	private static final int VERSION = 1;
	private static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase database;
	SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	//构造函数私有化
	private CoolWeatherDB(Context context){
		CoolWeatherOpenHelper coolWeatherOpenHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		//获取数据库实例
		database = coolWeatherOpenHelper.getWritableDatabase();
	}
	//获取CoolWeatherDB实例
	public synchronized static CoolWeatherDB getInstance(Context context){
		if(coolWeatherDB == null)
			coolWeatherDB = new CoolWeatherDB(context);
		return coolWeatherDB;
	}
	//保存关注城市的天气信息
	public void saveWeatherInfo(WeatherInfo weatherInfo){
		if (weatherInfo != null && !isExist(weatherInfo.getWeatherCode())) {
			ContentValues values = new ContentValues();
			values.put("country_name", weatherInfo.getCountryName());
			values.put("weather_code", weatherInfo.getWeatherCode());
			values.put("high_temp", weatherInfo.getHighTemp());
			values.put("low_temp", weatherInfo.getLowTemp());
			values.put("weather_type", weatherInfo.getWeatherType());
			//存储当前时间
			values.put("update_time", sFormat.format(new Date()));
			
			database.insert("WeatherInfo", null, values);
		}
	}
	//获取用户关注的天气信息
	public List<WeatherInfo> loadWeatherInfo(){
		List<WeatherInfo> list = new ArrayList<WeatherInfo>();
		Cursor cursor = database.query("WeatherInfo", null, null, null, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				WeatherInfo weatherInfo = new WeatherInfo();
				weatherInfo.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
				weatherInfo.setId(cursor.getInt(cursor.getColumnIndex("id")));
				weatherInfo.setHighTemp(cursor.getString(cursor.getColumnIndex("high_temp")));
				weatherInfo.setLowTemp(cursor.getString(cursor.getColumnIndex("low_temp")));
				weatherInfo.setWeatherCode(cursor.getString(cursor.getColumnIndex("weather_code")));
				weatherInfo.setWeatherType(cursor.getString(cursor.getColumnIndex("weather_type")));
				weatherInfo.setUpdateTime(cursor.getString(cursor.getColumnIndex("update_time")));
				list.add(weatherInfo);
			} while (cursor.moveToNext());
		}
		
		if (cursor != null) {
			cursor.close();
		}
		
		return list;
	}
	//更新数据库中的天气信息
	public void updateWeatherInfo(String weatherCode,String highTemp,String lowTemp,String weatherType ){
		if (!TextUtils.isEmpty(weatherCode)) {
			ContentValues values = new ContentValues();
			values.put("high_temp", highTemp);
			values.put("low_temp", lowTemp);
			values.put("weather_type", weatherType);
			values.put("update_time", sFormat.format(new Date()));
			
			database.update("WeatherInfo", values, "weather_code = ?", new String[]{weatherCode});
		}
	}
	//检查该城市是否已经关注
	private boolean isExist(String weatherCode){
		Cursor cursor = database.query("WeatherInfo", null, "weather_code = ?", new String[] {weatherCode}, null, null, null);
		if (cursor.moveToFirst()) {
			return true;
		}
		return false;
	}
	//保存省份数据
	public void saveProvince(Province province){
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			database.insert("Province", null, values);
		}
	}
	
	//获取数据库中的省份数据
	public List<Province> loadProvinces(){
		List<Province> list = new ArrayList<Province>();
		Cursor cursor = database.query("Province", null, null, null, null, null, null);
		if(cursor.moveToFirst()){
			do{
				Province province = new Province();
				province.setId(cursor.getInt(cursor.getColumnIndex("id")));
				province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
				province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
				list.add(province);
			}while(cursor.moveToNext());
		}
		
		if(cursor != null)
			cursor.close();
		
		return list;
		
	}
	
	//保存市数据
	public void saveCity(City city){
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			database.insert("City", null, values);
		}
	}
	
	//获取市级数据
	public List<City> loadCities(int provinceId){
		List<City> list = new ArrayList<City>();
		Cursor cursor = database.query("City", null, "province_id = ?", new String[]{String.valueOf(provinceId)}, null, null, null);
		if (cursor.moveToFirst()) {
			do {
				City city = new City();
				city.setId(cursor.getInt(cursor.getColumnIndex("id")));
				city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
				city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
				city.setProvinceId(provinceId);
				list.add(city);
			} while (cursor.moveToNext());
		}
		
		if(cursor != null)
			cursor.close();
		
		return list;
	}
	
	//保存县级数据
	public void saveCountry(Country country){
		if (country != null) {
			ContentValues values = new ContentValues();
			values.put("country_name", country.getCountryName());
			values.put("country_code", country.getCountryCode());
			values.put("city_id", country.getCityId());
			database.insert("Country", null, values);
		}
		
	}
	
	//从数据库获取县级数据
	public List<Country> loadCountries(int cityId){
		List<Country> list = new ArrayList<Country>();
		Cursor cursor = database.query("Country", null, "city_id = ?", new String[]{String.valueOf(cityId)}, null, null, null);
		if(cursor.moveToFirst()){
			do {
				Country country = new Country();
				country.setId(cursor.getInt(cursor.getColumnIndex("id")));
				country.setCountryName(cursor.getString(cursor.getColumnIndex("country_name")));
				country.setCountryCode(cursor.getString(cursor.getColumnIndex("country_code")));
				country.setCityId(cursor.getInt(cursor.getColumnIndex("city_id")));
				list.add(country);
			} while (cursor.moveToNext());
		}
		
		if(cursor != null)
			cursor.close();
		
		return list;
	}
}
