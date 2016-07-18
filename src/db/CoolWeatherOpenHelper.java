package db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class CoolWeatherOpenHelper extends SQLiteOpenHelper {

	//省级建表语句
	private static final String CREATE_PROVINCE	= "create table Province(id integer primary key autoincrement,province_name text,province_code text)";
	//市级建表语句
	private static final String CREATE_CITY = "create table City(id integer primary key autoincrement,city_name text,city_code text,province_id integer)";
	//县级建表语句
	private static final String CREATE_COUNTRY = "create table Country(id integer primary key autoincrement,country_name text,country_code text,city_id integer)";
	//保存用户关注城市的天气
	private  static final String CREATE_WEATHERINFO = "create table WeatherInfo(id integer primary key autoincrement,country_name text,high_temp text,low_temp text,weather_type text,weather_code text,update_time text)";
	public CoolWeatherOpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTRY);
		db.execSQL(CREATE_WEATHERINFO);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

}
