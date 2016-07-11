package activity;

import java.io.BufferedReader;

import com.coolweather.app.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import model.CoolWeatherDB;
import service.AutoUpdateService;
import util.HttpCallBaskListener;
import util.HttpUtil;
import util.Utility;

public class WeatherActivity extends Activity implements OnClickListener {

	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	
	private Button switchCity;
	private Button refreshWeather;
	private Button setSys;
	
	private CoolWeatherDB coolWeatherDB;
	String countryCode;
	
	SharedPreferences preferences;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		//初始化控件
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		
		//切换城市和更新天气按钮实例
		switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		setSys = (Button) findViewById(R.id.setting);
		//注册按钮点击事件
		switchCity.setOnClickListener(this);
		refreshWeather.setOnClickListener(this);
		setSys.setOnClickListener(this);
		//表示从列表选中后跳转
		countryCode = getIntent().getStringExtra("country_code");
		//用于再次进入页面是刷新天气信息
		String weatherCode = preferences.getString("weather_code", "");
		//用于判别是否是从关注列表跳转
		String weatherInfoCode = getIntent().getStringExtra("weatherinfo_code");
		
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		if(!TextUtils.isEmpty(countryCode)){
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		} else if (!TextUtils.isEmpty(weatherInfoCode)) {
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(weatherInfoCode);
		}else if (!TextUtils.isEmpty(weatherCode)) {
			//打开app时实现刷新天气
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(weatherCode);
		}else {
			showWeather();
		}
		
	}
	
	//查询县级代码对应的天气代号
	private void queryWeatherCode(String countryCode){
		String address = "http://www.weather.com.cn/data/list3/city" + countryCode + ".xml";
		queryFromServer(address, "countryCode");
	}
	//查询天气代码所对应的天气
	private void queryWeatherInfo(String weatherCode){
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + weatherCode;
		queryFromServer(address, "weatherCode");
	}
	//从服务器查询天气信息
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallBaskListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//从服务器返回的数据解析出天气代码
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					//获取天气代码
					String[] mStrings = address.split("=");
					//处理从服务器中返回的天气数据
					Utility.handleWeatherResponse(WeatherActivity.this, response,mStrings[1]);
					//返回主线程处理逻辑
					runOnUiThread(new Runnable() {
						public void run() {
							showWeather();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败");
					}
				});
			}
		});
	}
	
	//从SharedPreferences文件中读取存储的天气信息，并显示在界面上
	private void showWeather(){
		cityNameText.setText(preferences.getString("city_name", ""));
		temp1Text.setText(preferences.getString("temp1", ""));
		temp2Text.setText(preferences.getString("temp2", ""));
		weatherDespText.setText(preferences.getString("weather_desp", ""));
		publishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
		currentDateText.setText(preferences.getString("current_date", "") + "   " + preferences.getString("week", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
		//判断countryCode是否为空，如果不为空，则表明是从chooseAreaActivity跳转过来的
		if (!TextUtils.isEmpty(countryCode)) {
			Utility.saveCarfuWeather(preferences.getString("weather_code", ""), preferences.getString("city_name", ""), preferences.getString("temp1", ""), preferences.getString("temp2", ""),preferences.getString("weather_desp", ""), coolWeatherDB);
		}
		
		if (preferences.getBoolean("auto_update", false)) {
			//开启服务后台更新天气
			
			Intent intent = new Intent(this,AutoUpdateService.class);
			intent.putExtra("updaterate",preferences.getInt("update_rate", 0));
			startService(intent);
		}
		
		
	}

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.switch_city:
			Intent intent = new Intent(this,ChooseAreaActivity.class);
			intent.putExtra("from_weather_activity", true);
			startActivity(intent);
			finish();
			break;
		case R.id.refresh_weather:
			publishText.setText("同步中...");
			//获取存储到SharedPreferences中的weatherCode用于更新天气
			String weatherCode = preferences.getString("weather_code", "");
			if (!TextUtils.isEmpty(weatherCode)) {
				queryWeatherInfo(weatherCode);
			}
			break;
		case R.id.setting:
			Intent intent2 = new Intent(this,SettingActivity.class);
			startActivity(intent2);
			break;
		default:
			break;
		}
	}
}
