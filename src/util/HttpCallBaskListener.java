package util;

public interface HttpCallBaskListener {

	//回调函数，当服务器返回数据成功时调用
	public void onFinish(String response);
	//当服务器放回失败时调用
	public void onError(Exception e);
}
