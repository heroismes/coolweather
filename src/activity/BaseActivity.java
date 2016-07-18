package activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import util.ActivityCollector;

public class BaseActivity extends Activity {
//�û����Ϊ���л�Ļ���
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		ActivityCollector.addActivity(this);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		ActivityCollector.removeActivity(this);
	}

}
