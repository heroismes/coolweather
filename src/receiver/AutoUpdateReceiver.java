package receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import service.AutoUpdateService;

public class AutoUpdateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// 从广播接收器开启服务
		Intent i = new Intent(context,AutoUpdateService.class);
		context.startService(i);
	}
}
