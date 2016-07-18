package activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.coolweather.app.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import model.CoolWeatherDB;
import model.WeatherInfo;
import android.widget.EditText;
import android.widget.ListView;
import service.AutoUpdateService;
import util.HttpCallBaskListener;
import util.HttpUtil;
import util.Utility;

public class SettingActivity extends BaseActivity {
	private CheckBox autoUpdate;
	private EditText updateRate;
	private ListView listCity;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	//�б���ѡ�е���
	private WeatherInfo selectWeatherInfo;
	private List<WeatherInfo> weatherInfoList;
	
	private Button submit;
	private Button addCity;
	private Button backWeather;
	
	SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.setting_layout);
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final SharedPreferences.Editor editor = preferences.edit();
		updateRate = (EditText) findViewById(R.id.update_rate);
		autoUpdate = (CheckBox) findViewById(R.id.auto_update);
		submit = (Button) findViewById(R.id.update_time);
		addCity = (Button) findViewById(R.id.add);
		backWeather = (Button) findViewById(R.id.back);
		//��ȡ���ݿ�ʵ��
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		//��ȡ��ע�����б�
		listCity = (ListView) findViewById(R.id.list_city);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listCity.setAdapter(adapter);
		//�������ݿ��е�������Ϣ
		loadWeatherInfo();
		
		if (preferences.getBoolean("auto_update", false)) {
			autoUpdate.setChecked(preferences.getBoolean("auto_update", false));
			updateRate.setEnabled(true);
			submit.setEnabled(true);
		}
		
		int fRate = preferences.getInt("update_rate", 0);
		if (fRate > 0) {
			updateRate.setText(String.valueOf(fRate));
		}else {
			updateRate.setText("");
		}
		backWeather.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//��������ҳ��
				onBackPressed();
			}
		});
		
		addCity.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(SettingActivity.this,ChooseAreaActivity.class);
				intent.putExtra("from_weather_activity", true);
				startActivity(intent);
				finish();
			}
		});
		
		listCity.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				selectWeatherInfo = weatherInfoList.get(position);
				
				Intent intent = new Intent(SettingActivity.this,WeatherActivity.class);
				//��ѡ�г��е����������ŵ�intent��
				intent.putExtra("weatherinfo_code", selectWeatherInfo.getWeatherCode());
				startActivity(intent);
				finish();
			}
		});
		
		//����ȷ����ť�¼�
		submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				int rate = Integer.parseInt(updateRate.getText().toString());
				if (rate < 10 && rate > 0) {
					editor.putBoolean("auto_update", autoUpdate.isChecked());
					editor.putInt("update_rate", rate);
					editor.commit();
					
					Intent intent = new Intent(SettingActivity.this,AutoUpdateService.class);
					intent.putExtra("updaterate", rate);
					startService(intent);
				}else {
					AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this);
					dialog.setTitle("ע��");
					dialog.setMessage("������С��10������");
					dialog.setCancelable(true);
					dialog.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							dialog.dismiss();
						}
					});
					
					dialog.show();
				}
			}
		});
		
		//���ø�ѡ��ť�¼�
		autoUpdate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if (isChecked) {
					//���ѡ���Զ����£����������Ϊ�ɱ༭
					updateRate.setEnabled(true);
					submit.setEnabled(true);
				}else {
					updateRate.setEnabled(false);
					submit.setEnabled(false);
					//�Ѹ�ѡ���ֵ����sharedPreferences��
					editor.putBoolean("auto_update", autoUpdate.isChecked());
					editor.putInt("update_rate", 0);
					editor.commit();
					Intent intent = new Intent(SettingActivity.this,AutoUpdateService.class);
					stopService(intent);
				}
			}
		});
		
		
	}
	//����������Ϣ
	private void loadWeatherInfo(){
		weatherInfoList = coolWeatherDB.loadWeatherInfo();
		
		if (weatherInfoList.size() > 0) {
			WeatherInfo wInfo = weatherInfoList.get(0);
			try {
				Date updateTime = (Date) sFormat.parse(wInfo.getUpdateTime());
				long mtime = new java.util.Date().getTime() - updateTime.getTime();
				//�ó��ϴθ��¾������ڵ�ʱ��
				long hour = mtime/1000/60/60;
				//��������ϴθ��µ�ʱ����С��2�򲻸���
				if (hour > 2) {
					for(WeatherInfo weatherInfo:weatherInfoList){
						String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + weatherInfo.getWeatherCode();
						updateWeatherInfo(address);
					}
				}
				//��ѯ������Ϣ
				queryWeatherInfo();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	//��ѯ���ݿ��е�������Ϣ
	private void queryWeatherInfo(){
		weatherInfoList = coolWeatherDB.loadWeatherInfo();
		
		for(WeatherInfo weatherInfo:weatherInfoList){
			String info = weatherInfo.getCountryName() + "        " + weatherInfo.getWeatherType() + "         " + weatherInfo.getHighTemp() + "/" + weatherInfo.getLowTemp();
			//��ȡ������������
			dataList.add(info);
		}
	}
	
	//���³����б��е�������Ϣ
	private void updateWeatherInfo(final String address){
		HttpUtil.sendHttpRequest(address, new HttpCallBaskListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				String[] mString = address.split("=");
				Utility.updateWeatherInfo(mString[1], response, coolWeatherDB);
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
			}
		});
	}
}
