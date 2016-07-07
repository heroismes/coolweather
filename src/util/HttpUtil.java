package util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
//����������
public class HttpUtil {

	public static void sendHttpRequest(final String address, final HttpCallBaskListener listener){
		new Thread(new Runnable() {
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					
					InputStream inputStream = connection.getInputStream();
					BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
					
					StringBuilder response = new StringBuilder();
					String line;
					//line = reader.readLine();
					while ((line = reader.readLine()) != null) {
						response.append(line);
						
					}
					reader.close();
					inputStream.close();
					if(listener != null)
						//�ص���������
						listener.onFinish(response.toString());
				} catch (Exception e) {
					// TODO: handle exception
					if (listener != null) {
						listener.onError(e);
					}
					
				}finally {
					if(connection != null)
						connection.disconnect();
					
				}
			}
		}).start();
	}
}
