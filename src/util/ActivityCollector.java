package util;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

public class ActivityCollector {

	//��ռ���
	private static List<Activity> collector = new ArrayList<Activity>();
	
	public static void addActivity(Activity activity){
		collector.add(activity);
	}
	
	public static void removeActivity(Activity activity){
		collector.remove(activity);
	}
	
	public static void finishAll(){
		for(Activity activity:collector){
			activity.finish();
		}
	}
}
