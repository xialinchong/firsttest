package cn.com.chinaunicom.fileshare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Application;

public class FSApp extends Application {

	private static FSApp mInstance = null;
	//�ѵ�¼�û���id��¼��ȫ��
	public String UserId = null;
//	public List<Map<String, Object>> validataMap = new ArrayList<Map<String, Object>>();
	public List<Map<String, String>> validataMap = new ArrayList<Map<String, String>>();
	
	public static FSApp getInstance() {
		if(mInstance==null)
			mInstance = new FSApp();
		return mInstance;
	}
	
	private FSApp(){
		
	}
}
