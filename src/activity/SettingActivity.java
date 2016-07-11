package activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager.Query;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.AndroidCharacter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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
import receiver.AutoUpdateReceiver;
import service.AutoUpdateService;

public class SettingActivity extends Activity {
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
		//��ȡ���ݿ�ʵ��
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		//��ȡ��ע�����б�
		listCity = (ListView) findViewById(R.id.list_city);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listCity.setAdapter(adapter);
		//��ȡ����Դ��Ϣ
		weatherInfoList = coolWeatherDB.loadWeatherInfo();
		for(WeatherInfo weatherInfo:weatherInfoList){
			String info = weatherInfo.getCountryName() + "        " + weatherInfo.getWeatherType() + "         " + weatherInfo.getHighTemp() + "/" + weatherInfo.getLowTemp();
			dataList.add(info);
		}
		
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
					Intent intent = new Intent(SettingActivity.this,AutoUpdateService.class);
					stopService(intent);
				}
			}
		});
		
		
	}
	
}
