package activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import model.City;
import model.CoolWeatherDB;
import model.Country;
import model.Province;
import util.HttpCallBaskListener;
import util.HttpUtil;
import util.Utility;

public class ChooseAreaActivity extends BaseActivity {

	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	//省市县列表
	private List<Province> provinceList;
	private List<City> cityList;
	private List<Country> countryList;
	
	//选中的省份
	private Province selectedProvince;
	//选中的城市
	private City selectedCity;
	private int currentLevel;
	//用于判断是否是从WeatherActivity跳转过来的
	private boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//从intent中取出from_weather_activity，用于判断是否是从WeatherActivity中过来的
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		
		//判断之前是否已经选定过城市，如果是，则直接跳转
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.getBoolean("city_selected", false) && !isFromWeatherActivity) {
			Intent intent = new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if (currentLevel == LEVEL_PROVINCE) {
					selectedProvince = provinceList.get(position);
					//加载市级数据
					queryCities();
				}else if (currentLevel == LEVEL_CITY) {
					selectedCity = cityList.get(position);
					//加载县级数据
					queryCounties();
				}else if (currentLevel == LEVEL_COUNTRY) {
					String countryCode = countryList.get(position).getCountryCode();
					Intent intent = new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("country_code", countryCode);
					startActivity(intent);
					finish();
				}
			}
		});
		//加载省级数据
		queryProvinces();
	}
	//查询省份，优先从数据库查，如果没有从服务器查
	private void queryProvinces(){
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size() > 0){
			dataList.clear();
			for(Province province:provinceList)
				dataList.add(province.getProvinceName());
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}
		else
			queryFromServer(null,"province");
	}
	
	//查询城市，优先从数据库查，如果没有从服务器查
	private void queryCities(){
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size() > 0){
			dataList.clear();
			for(City city:cityList)
				dataList.add(city.getCityName());
			
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}
		else
			queryFromServer(selectedProvince.getProvinceCode(),"city");
	}
	
	//查询区县，优先从数据库查，如果没有从服务器查
		private void queryCounties(){
			countryList = coolWeatherDB.loadCountries(selectedCity.getId());
			if(countryList.size() > 0){
				dataList.clear();
				for(Country country:countryList)
					dataList.add(country.getCountryName());
				
				adapter.notifyDataSetChanged();
				listView.setSelection(0);
				titleText.setText(selectedCity.getCityName());
				currentLevel = LEVEL_COUNTRY;
			}else
				queryFromServer(selectedCity.getCityCode(),"country");
		}
		//从服务器查询
		private void queryFromServer(final String code,final String type){
			String address;
			if(!TextUtils.isEmpty(code))
				address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
			else
				address = "http://www.weather.com.cn/data/list3/city.xml";
			
			showProgressDialog();
			HttpUtil.sendHttpRequest(address, new HttpCallBaskListener() {
				
				@Override
				public void onFinish(String response) {
					// TODO Auto-generated method stub
					boolean result = false;
					if("province".equals(type)){
						result = Utility.handleProvincesResponse(response, coolWeatherDB);
					}else if ("city".equals(type)) {
						result = Utility.handleCitiesResponse(response, selectedProvince.getId(), coolWeatherDB);
					}else if ("country".equals(type)) {
						result = Utility.handleCountriesResponse(response, selectedCity.getId(), coolWeatherDB);
					}
					
					if(result){
						//返回主线程处理逻辑
						runOnUiThread(new Runnable() {
							public void run() {
								closeProgressDialog();
								if("province".equals(type)){
									queryProvinces();
								}else if ("city".equals(type)) {
									queryCities();
								}else if ("country".equals(type)) {
									queryCounties();
								}
							}
						});
					}
				}
				
				@Override
				public void onError(Exception e) {
					// TODO Auto-generated method stub

					runOnUiThread(new Runnable() {
						public void run() {
							closeProgressDialog();
							
							Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
						}
					});
				}
			});
		}
		
		//显示进度对话框
		private void showProgressDialog(){
			if (progressDialog == null) {
				progressDialog = new ProgressDialog(this);
				progressDialog.setMessage("正在加载");
				progressDialog.setCanceledOnTouchOutside(false);
			}
			
			progressDialog.show();
		}
		
		//关闭进度对话框
		private void closeProgressDialog(){
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		}
		
		//捕获back键，根据当前的级别来判断是返回市列表、省列表还是直接退出
		@Override
		public void onBackPressed(){
			if(currentLevel == LEVEL_COUNTRY)
				queryCities();
			else if (currentLevel == LEVEL_CITY) {
				queryProvinces();
			} else {
				if (isFromWeatherActivity) {
					Intent intent = new Intent(this,WeatherActivity.class);
					startActivity(intent);
				}
				finish();
			}
		}
}

