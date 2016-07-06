package model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.VoicemailContract.Voicemails;
import db.CoolWeatherOpenHelper;

public class CoolWeatherDB {

	//���ݿ���
	private static final String DB_NAME = "cool_weather";
	//���ݿ�汾
	private static final int VERSION = 1;
	private static CoolWeatherDB coolWeatherDB;
	private SQLiteDatabase database;
	
	//���캯��˽�л�
	private CoolWeatherDB(Context context){
		CoolWeatherOpenHelper coolWeatherOpenHelper = new CoolWeatherOpenHelper(context, DB_NAME, null, VERSION);
		//��ȡ���ݿ�ʵ��
		database = coolWeatherOpenHelper.getWritableDatabase();
	}
	//��ȡCoolWeatherDBʵ��
	public synchronized static CoolWeatherDB getInstance(Context context){
		if(coolWeatherDB == null)
			coolWeatherDB = new CoolWeatherDB(context);
		return coolWeatherDB;
	}
	//����ʡ������
	public void saveProvince(Province province){
		if (province != null) {
			ContentValues values = new ContentValues();
			values.put("province_name", province.getProvinceName());
			values.put("province_code", province.getProvinceCode());
			database.insert("Province", null, values);
		}
	}
	//��ȡ���ݿ��е�ʡ������
	public List<Province> getProvince(){
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
	
	//����������
	public void saveCity(City city){
		if (city != null) {
			ContentValues values = new ContentValues();
			values.put("city_name", city.getCityName());
			values.put("city_code", city.getCityCode());
			values.put("province_id", city.getProvinceId());
			database.insert("City", null, values);
		}
	}
	
	//��ȡ�м�����
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
	
	//�����ؼ�����
	public void saveCountry(Country country){
		if (country != null) {
			ContentValues values = new ContentValues();
			values.put("country_name", country.getCountryName());
			values.put("country_code", country.getCountryCode());
			values.put("city_id", country.getCityId());
			database.insert("Country", null, values);
		}
		
	}
	
	//�����ݿ��ȡ�ؼ�����
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
