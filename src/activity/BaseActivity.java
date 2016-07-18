package activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import util.ActivityCollector;

public class BaseActivity extends Activity {
//该活动类作为所有活动的基类
	
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
