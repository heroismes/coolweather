package util;

public interface HttpCallBaskListener {

	//�ص����������������������ݳɹ�ʱ����
	public void onFinish(String response);
	//���������Ż�ʧ��ʱ����
	public void onError(Exception e);
}
