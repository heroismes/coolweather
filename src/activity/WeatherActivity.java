package activity;

import com.coolweather.app.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import util.HttpCallBaskListener;
import util.HttpUtil;
import util.Utility;

public class WeatherActivity extends Activity {

	private LinearLayout weatherInfoLayout;
	private TextView cityNameText;
	private TextView publishText;
	private TextView weatherDespText;
	private TextView temp1Text;
	private TextView temp2Text;
	private TextView currentDateText;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		//��ʼ���ؼ�
		weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText = (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText = (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text = (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		String countryCode = getIntent().getStringExtra("country_code");
		
		if(!TextUtils.isEmpty(countryCode)){
			publishText.setText("ͬ����");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		}else {
			showWeather();
		}
	}
	
	//��ѯ�ؼ������Ӧ����������
	private void queryWeatherCode(String countryCode){
		String address = "http://www.weather.com.cn/data/list3/city" + countryCode + ".xml";
		queryFromServer(address, "countryCode");
	}
	//��ѯ������������Ӧ������
	private void queryWeatherInfo(String weatherCode){
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + weatherCode;
		queryFromServer(address, "weatherCode");
	}
	//�ӷ�������ѯ������Ϣ
	private void queryFromServer(final String address,final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallBaskListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				if ("countryCode".equals(type)) {
					if (!TextUtils.isEmpty(response)) {
						//�ӷ��������ص����ݽ�������������
						String[] array = response.split("\\|");
						if (array != null && array.length == 2) {
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if ("weatherCode".equals(type)) {
					//��ȡ��������
					String[] mStrings = address.split("=");
					//����ӷ������з��ص���������
					Utility.handleWeatherResponse(WeatherActivity.this, response,mStrings[1]);
					//�������̴߳����߼�
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
						publishText.setText("ͬ��ʧ��");
					}
				});
			}
		});
	}
	
	//��SharedPreferences�ļ��ж�ȡ�洢��������Ϣ������ʾ�ڽ�����
	private void showWeather(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(preferences.getString("city_name", ""));
		temp1Text.setText(preferences.getString("temp1", ""));
		temp2Text.setText(preferences.getString("temp2", ""));
		weatherDespText.setText(preferences.getString("weather_desp", ""));
		publishText.setText("����" + preferences.getString("publish_time", "") + "����");
		currentDateText.setText(preferences.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
	}
}
