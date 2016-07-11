package service;

import com.coolweather.app.R;

import activity.WeatherActivity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import receiver.AutoUpdateReceiver;
import util.HttpCallBaskListener;
import util.HttpUtil;
import util.Utility;

public class AutoUpdateService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Notification notification = new Notification(R.drawable.logo, "����", System.currentTimeMillis());
		Intent intent = new Intent(this,WeatherActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notification.setLatestEventInfo(this, "this is a ddd", "zzzz", pIntent);
		startForeground(1, notification);
	}
	@Override
	public int onStartCommand(Intent intent,int flags,int startId){
		//�����̣߳�����������Ϣ
		new Thread(new Runnable() {
			public void run() {
				updateWeather();
			}
		}).start();
		int hour = intent.getIntExtra("updaterate", 1);
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		int time = hour*60*60*1000;//30���ӵĺ�����
		long triggerAttime = SystemClock.elapsedRealtime() + time;
		Intent i = new Intent(this,AutoUpdateReceiver.class);
		PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, i, 0);
		//ÿ30���ӿ���һ�η����������
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAttime, pIntent);
		return super.onStartCommand(intent, flags, startId);
	}
	
	//����������Ϣ
	private void updateWeather(){
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final String weatherCode = preferences.getString("weather_code", "");
		String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + weatherCode;
		HttpUtil.sendHttpRequest(address, new HttpCallBaskListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				Utility.handleWeatherResponse(AutoUpdateService.this, response, weatherCode);
				//����������Ϣ
				Intent intent = new Intent(AutoUpdateService.this,WeatherActivity.class);
				startActivity(intent);
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				e.printStackTrace();
			}
		});
	}
}
