package activity;

import java.io.BufferedReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.coolweather.app.R;

import android.R.drawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import model.CoolWeatherDB;
import service.AutoUpdateService;
import util.ActivityCollector;
import util.HttpCallBaskListener;
import util.HttpUtil;
import util.Utility;

public class WeatherActivity extends BaseActivity implements OnClickListener {

	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView currentDateText;
	private TextView currentTempText;
	
	//private Button switchCity;
	private Button refreshWeather;
	private Button setSys;
	
	private CoolWeatherDB coolWeatherDB;
	String countryCode;
	
	//六天天气信息的控件
	private TextView tvFourDay;
	private TextView tvFiveDay;
	private TextView tvSixDay;
	
	private ImageView ivYester;
	private ImageView ivToday;
	private ImageView ivTomorrow;
	private ImageView ivFour;
	private ImageView ivFive;
	private ImageView ivSix;
	
	private TextView tvYesterTemp1;
	private TextView tvYesterTemp2;
	private TextView tvtodayTemp1;
	private TextView tvtodayTemp2;
	private TextView tvTomorrTemp1;
	private TextView tvTomorrTemp2;
	private TextView tvFourTemp1;
	private TextView tvFourTemp2;
	private TextView tvFiveTemp1;
	private TextView tvFiveTemp2;
	private TextView tvSixTemp1;
	private TextView tvSixTemp2;
	
	
	SharedPreferences preferences;
	//用于格式化日期
	SimpleDateFormat sdf = new SimpleDateFormat("M/d");
	
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
		currentDateText = (TextView) findViewById(R.id.current_date);
		currentTempText = (TextView) findViewById(R.id.current_temp);
		
		//初始化6天天气信息的控件
		tvFourDay = (TextView) findViewById(R.id.four_day);
		tvFiveDay = (TextView) findViewById(R.id.five_day);
		tvSixDay = (TextView) findViewById(R.id.six_day);
		
		ivYester = (ImageView) findViewById(R.id.iv_yesteday);
		ivToday = (ImageView) findViewById(R.id.iv_today);
		ivTomorrow = (ImageView) findViewById(R.id.iv_tommorrow);
		ivFour = (ImageView) findViewById(R.id.iv_four);
		ivFive = (ImageView) findViewById(R.id.iv_five);
		ivSix = (ImageView) findViewById(R.id.iv_six);
		
		tvYesterTemp1 = (TextView) findViewById(R.id.yes_temp1);
		tvYesterTemp2 = (TextView) findViewById(R.id.yes_temp2);
		tvtodayTemp1 = (TextView) findViewById(R.id.today_temp1);
		tvtodayTemp2 = (TextView) findViewById(R.id.today_temp2);
		tvTomorrTemp1 = (TextView) findViewById(R.id.tom_temp1);
		tvTomorrTemp2 = (TextView) findViewById(R.id.tom_temp2);
		tvFourTemp1 = (TextView) findViewById(R.id.four_temp1);
		tvFourTemp2 = (TextView) findViewById(R.id.four_temp2);
		tvFiveTemp1 = (TextView) findViewById(R.id.five_temp1);
		tvFiveTemp2 = (TextView) findViewById(R.id.five_temp2);
		tvSixTemp1 = (TextView) findViewById(R.id.six_temp1);
		tvSixTemp2 = (TextView) findViewById(R.id.six_temp2);
		
		
		//切换城市和更新天气按钮实例
		//switchCity = (Button) findViewById(R.id.switch_city);
		refreshWeather = (Button) findViewById(R.id.refresh_weather);
		setSys = (Button) findViewById(R.id.setting);
		//注册按钮点击事件
		//switchCity.setOnClickListener(this);
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
			currentDateText.setVisibility(View.INVISIBLE);
			currentTempText.setVisibility(View.INVISIBLE);
			weatherDespText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		} else if (!TextUtils.isEmpty(weatherInfoCode)) {
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			currentDateText.setVisibility(View.INVISIBLE);
			currentTempText.setVisibility(View.INVISIBLE);
			weatherDespText.setVisibility(View.INVISIBLE);
			queryWeatherInfo(weatherInfoCode);
		}else if (!TextUtils.isEmpty(weatherCode)) {
			//打开app时实现刷新天气
			publishText.setText("同步中");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			currentDateText.setVisibility(View.INVISIBLE);
			currentTempText.setVisibility(View.INVISIBLE);
			weatherDespText.setVisibility(View.INVISIBLE);
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
	//返回当前日期加上days的日期
	private Date getDate(int days){
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		date = calendar.getTime();
		
		return date;
	}
	
	//从SharedPreferences文件中读取存储的天气信息，并显示在界面上
	private void showWeather(){
		cityNameText.setText(preferences.getString("city_name", ""));
		weatherDespText.setText(preferences.getString("weather_desp", ""));
		publishText.setText("今天" + preferences.getString("publish_time", "") + "发布");
		currentDateText.setText(preferences.getString("current_date", "") + "   " + preferences.getString("week", ""));
		
		//gei六天天气预区域报赋值
		tvFourDay.setText(sdf.format(getDate(2)));
		tvFiveDay.setText(sdf.format(getDate(3)));
		tvSixDay.setText(sdf.format(getDate(4)));
		
		ivToday.setImageResource(getWeatherImage(preferences.getString("temp00", "")));
		ivTomorrow.setImageResource(getWeatherImage(preferences.getString("temp10", "")));
		ivFour.setImageResource(getWeatherImage(preferences.getString("temp20", "")));
		ivFive.setImageResource(getWeatherImage(preferences.getString("temp30", "")));
		ivSix.setImageResource(getWeatherImage(preferences.getString("temp40", "")));
		ivYester.setImageResource(getWeatherImage(preferences.getString("temp50", "")));
		
		
		tvYesterTemp1.setText(preferences.getString("temp01", ""));
		tvYesterTemp2.setText(preferences.getString("temp02", ""));
		tvtodayTemp1.setText(preferences.getString("temp11", ""));
		tvtodayTemp2.setText(preferences.getString("temp12", ""));
		tvTomorrTemp1.setText(preferences.getString("temp21", ""));
		tvTomorrTemp2.setText(preferences.getString("temp22", ""));
		tvFourTemp1.setText(preferences.getString("temp31", ""));
		tvFourTemp2.setText(preferences.getString("temp32", ""));
		tvFiveTemp1.setText(preferences.getString("temp41", ""));
		tvFiveTemp2.setText(preferences.getString("temp42", ""));
		tvSixTemp1.setText(preferences.getString("temp51", ""));
		tvSixTemp2.setText(preferences.getString("temp52", ""));
		
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		currentDateText.setVisibility(View.VISIBLE);
		currentTempText.setVisibility(View.VISIBLE);
		weatherDespText.setVisibility(View.VISIBLE);
		
		//判断countryCode是否为空，如果不为空，则表明是从chooseAreaActivity跳转过来的
		if (!TextUtils.isEmpty(countryCode)) {
			Utility.saveCarfuWeather(preferences.getString("weather_code", ""), preferences.getString("city_name", ""), preferences.getString("temp11", ""), preferences.getString("temp12", ""),preferences.getString("weather_desp", ""), coolWeatherDB);
		}
		
		if (preferences.getBoolean("auto_update", false)) {
			//开启服务后台更新天气
			
			Intent intent = new Intent(this,AutoUpdateService.class);
			intent.putExtra("updaterate",preferences.getInt("update_rate", 0));
			startService(intent);
		}
		
		
	}

	//获取天气对应的图片
	private int getWeatherImage(String type){
		if (!TextUtils.isEmpty(type)) {
			if (type.contains("晴")) {
				//判断天气类型是否包含晴
				if (type.contains("云")) {
					return R.drawable.weathericon_qinzhuanduoyun;
				}
				return R.drawable.weathericon_sun;
			}else if (type.contains("雨")) {
				if (type.contains("雷")) {
					return R.drawable.weathericon_leizhenyu;
				}else if (type.contains("阵")) {
					return R.drawable.weathericon_zhenyu;
				}else if (type.contains("小")) {
					return R.drawable.weathericon_xiaoyu;
				}else {
					return R.drawable.weathericon_dayu;
				}
			}else if (type.contains("云")) {
				if (type.contains("雨")) {
					return R.drawable.weathericon_duoyunzhuanxiaoyu;
				}
				return R.drawable.weathericon_duoyun;
			}else if (type.contains("雪")) {
				return R.drawable.weathericon_snow;
			}
		}
		//表示找不到对应的图片信息
		return -1;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
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
	
	@Override
	public void onBackPressed(){
		//防止回退到其他城市天气页面,返回则直接退出程序
		ActivityCollector.finishAll();
	}
	
}
