package cn.com.chinaunicom.fileshare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class FSApp extends Application {

//	private static FSApp mInstance = null;
	//�ѵ�¼�û���id��¼��ȫ��
	public String UserId = null;
//	public List<Map<String, Object>> validataMap = new ArrayList<Map<String, Object>>();
	public List<Map<String, String>> validataMap = new ArrayList<Map<String, String>>();
	public Set<String> loginSet = new TreeSet<String>();
//	public static FSApp getInstance() {
//		if(mInstance==null)
//			mInstance = new FSApp();
//		return mInstance;
//	}
	
	public FSApp(){
		
	}
	
	/**
	 * 
	 * @param fileType �ļ��л����ļ�����
	 * @param fileName ��validata��loginData
	 * @param key 
	 * @param value 
	 */
	public void saveArray(String fileType, String value, Context context) {
		if ( ! "".equals(fileType) && ! "".equals(value) ) {
			SharedPreferences sharedPreferences = 
					context.getSharedPreferences("validata", context.MODE_APPEND);
			SharedPreferences.Editor editor = sharedPreferences.edit();
			
			Set<String> idSet = sharedPreferences.getStringSet( fileType, new HashSet<String>() );
			idSet.add( value );
			editor.putStringSet( fileType, idSet );
			editor.commit();
//			editor.clear();
//			editor.commit();
		}
	}
	
	@Override
	public void onCreate() {
		getArray();
		super.onCreate();
	}

	public void getArray() {
		SharedPreferences sharedPreferences = getSharedPreferences("validata",FSApp.MODE_APPEND);
		Map<String, Set<String>> map = (Map<String, Set<String>>) sharedPreferences.getAll();
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			
			Set<String> set = map.get(key);
			for (String id : set) {
				if (key.equals("user_login")) {
					loginSet.add(id);
				} else if (key.equals("UserId")) {
					UserId = id;
				} else {
					Map<String, String> m = new HashMap<String, String>();
					m.put(id, key);
					validataMap.add(m);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param ���û���¼��ʱ������ļ�����д������
	 */
	public void clearMapXml(Context context) {
		SharedPreferences sharedPreferences = 
				context.getSharedPreferences("validata", context.MODE_APPEND);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.clear();
		editor.commit();
	}
	
	/**
	 * 
	 * @param set1�ļ���ȡ���ĵ�¼��Ϣ��װ��set
	 * @param set2��ǰ�ı���������Ϣ���û��������룩
	 * @return true��������set�������
	 */
	public boolean isSetEqual(Set<String> set1, Set<String> set2) {
		
		if(set1 == null && set2 == null){
			return true;
		}

		if (set1 == null || set2 == null || set1.size() != set2.size()
				|| set1.size() == 0 || set2.size() == 0) {
			return false;
		}

		Iterator ite1 = set1.iterator();
		Iterator ite2 = set2.iterator();

		boolean isFullEqual = true;
		
		while (ite2.hasNext()) {
			if (!set1.contains(ite2.next())) {
				isFullEqual = false;
			}
		}

		return isFullEqual;
	}
}
