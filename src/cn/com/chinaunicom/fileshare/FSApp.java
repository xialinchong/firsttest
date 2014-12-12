package cn.com.chinaunicom.fileshare;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONObject;

import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;
import com.yidianhulian.Api;
import com.yidianhulian.CallApiTask;
import com.yidianhulian.CallApiTask.CallApiListener;

public class FSApp extends Application implements CallApiListener {

	// private static FSApp mInstance = null;
	// �ѵ�¼�û���id��¼��ȫ��
	public String UserId = null;
	public List<Map<String, String>> validataMap = new ArrayList<Map<String, String>>();
	// public Set<String> loginSet = new TreeSet<String>();
	public String login_name = null;
	public String login_pwd = null;
	private String devicetoken = null;
	
	//113.10.188.155���� 58.16.63.195�ⲿ
//	public String  HOST = "http://58.16.63.195/";
	public String  HOST = "http://113.10.188.155/";

	// public static FSApp getInstance() {
	// if(mInstance==null)
	// mInstance = new FSApp();
	// return mInstance;
	// }

	public FSApp() {

	}

	/**
	 * 
	 * @param fileType
	 *            �ļ��л����ļ�����
	 * @param fileName
	 *            ��validata��loginData
	 * @param key
	 * @param value
	 */
	public void saveArray(String fileType, String value, Context context) {
		if (!"".equals(fileType) && !"".equals(value)) {
			SharedPreferences sharedPreferences = context.getSharedPreferences(
					"validata", context.MODE_APPEND);
			SharedPreferences.Editor editor = sharedPreferences.edit();

			Set<String> idSet = sharedPreferences.getStringSet(fileType,
					new TreeSet<String>());
			idSet.add(value);
			editor.putStringSet(fileType, idSet);
			editor.commit();
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		XGPushManager.registerPush(getApplicationContext(),
				new XGIOperateCallback() {

					@Override
					public void onSuccess(Object data, int flag) {
						Log.d("TPush", "ע��ɹ����豸tokenΪ��" + data);
						devicetoken = (String) data;
						if (UserId != null && devicetoken != null) {
							new CallApiTask(0, FSApp.this).execute();
						}
					}

					@Override
					public void onFail(Object data, int errCode, String msg) {
						Log.d("TPush", "ע��ʧ�ܣ������룺" + errCode + ",������Ϣ��" + msg);
					}
				});
	}

	public void getArray() {
		SharedPreferences sharedPreferences = getSharedPreferences("validata",
				FSApp.MODE_APPEND);
		Map<String, Set<String>> map = (Map<String, Set<String>>) sharedPreferences
				.getAll();
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();

			Set<String> set = map.get(key);
			for (String value : set) {
				if (key.equals("login_name")) {
					login_name = value;
				} else if (key.equals("UserId")) {
					UserId = value;
				} else if (key.equals("login_pwd")) {
					login_pwd = value;
				} else {
					Map<String, String> m = new HashMap<String, String>();
					m.put(value, key);
					validataMap.add(m);
				}
			}
		}
		
	}

	/**
	 * 
	 * @param ���û���¼��ʱ��
	 *            ������ļ�����д������
	 */
	public void clearMapXml(Context context) {
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				"validata", context.MODE_APPEND);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		validataMap.clear();
		// loginSet.clear();
		UserId = null;
		login_name = null;
		login_pwd = null;
		devicetoken = null;
		editor.clear();
		editor.commit();
	}

	/**
	 * 
	 * @param set1�ļ���ȡ���ĵ�¼��Ϣ��װ��set
	 * @param set2��ǰ�ı���������Ϣ
	 *            ���û��������룩
	 * @return true��������set�������
	 */
	public boolean isSetEqual(Set<String> set1, Set<String> set2) {

		if (set1 == null && set2 == null) {
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

	@Override
	public Dialog getProgressDialog(String title) {
		return null;
	}

	@Override
	public JSONObject callApi(int what) {
		Map<String, String> mitem = new HashMap<String, String>();
		mitem.put("user_id", UserId);
		mitem.put("token", login_pwd);
		mitem.put("deviceToken", devicetoken);
		return Api.get(HOST + "save_android_token.php", mitem);
	}

	@Override
	public void onCallApiSuccess(int what, JSONObject result) {

	}

	@Override
	public void onCallApiFail(int what, JSONObject result) {

	}

}
