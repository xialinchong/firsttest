package cn.com.chinaunicom.fileshare;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yidianhulian.Api;
import com.yidianhulian.CallApiTask;
import com.yidianhulian.CallApiTask.CallApiListener;
import com.yidianhulian.Util;

public class LoginActivity extends Activity implements CallApiListener {

	private Button mloginBtn = null;
	private EditText mlogin_name = null;
	private EditText mlogin_pwd = null;
	private final int EMPTY_PWD_NAME = 1;
	private final int ERROE_PWD_NAME = 2;
	private final int SERVICE_ERROR = 3;
	private final int SUCCESS = 4;
	private ProgressDialog dialog;
	
	FSApp app;
	public Set<String> loginSet = new TreeSet<String>();
	
	private Handler myhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case EMPTY_PWD_NAME:
				Toast.makeText(LoginActivity.this, "用户名或密码不能为空！", Toast.LENGTH_LONG).show();
				break;
			case ERROE_PWD_NAME:
				Toast.makeText(LoginActivity.this, "用户名或密码错误！", Toast.LENGTH_LONG).show();
				break;
			case SERVICE_ERROR:
				//接口调用出错
				Toast.makeText(LoginActivity.this, "服务器错误！", Toast.LENGTH_LONG).show();
				break;
			case SUCCESS:
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setClass(LoginActivity.this, MainActivity.class);
				LoginActivity.this.startActivity(intent);
				break;
			default:
				break;
			}
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		app = (FSApp)LoginActivity.this.getApplication();
		
		mlogin_name = (EditText) findViewById(R.id.login_name);
		mlogin_pwd = (EditText) findViewById(R.id.login_pwd);

		
		mloginBtn = (Button) findViewById(R.id.login_btn);

		mloginBtn.setOnClickListener(new mloginBtnOnclickListener());

	}

	@Override
	protected void onResume() {
		super.onResume();
		app.getArray();
		if (app.login_name != null && app.login_pwd != null) {
			Intent intent = new Intent();
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setClass(LoginActivity.this, MainActivity.class);
			LoginActivity.this.startActivity(intent);
			return;
		}
	}

//	@Override
//	public void onBackPressed() {
//		super.onBackPressed();
////		android.os.Process.killProcess(android.os.Process.myPid());
//	}

	@Override
	public Dialog getProgressDialog(String title) {
		return null;
	}

	@Override
	public JSONObject callApi(int what) {
		Map<String, String> mitem = new HashMap<String, String>();
		mitem.put("login_name", mlogin_name.getText().toString());
		mitem.put("login_pwd", mlogin_pwd.getText().toString());
		return Api.post(app.HOST + "user_login_api.php", mitem);
	}

	@Override
	public void onCallApiSuccess(int what, JSONObject result) {
		try {
			app.clearMapXml(LoginActivity.this);
			app.UserId = result.getString("id");
			app.login_name = mlogin_name.getText().toString();
			app.login_pwd  = Util.md5( mlogin_pwd.getText().toString() );
			app.saveArray("UserId", app.UserId, LoginActivity.this);
			app.saveArray("login_name", app.login_name, LoginActivity.this);
			app.saveArray("login_pwd",  app.login_pwd, LoginActivity.this);
			LoginActivity.this.myhandler.sendEmptyMessage(SUCCESS);
		} catch (Exception e) {
			e.printStackTrace();
			LoginActivity.this.myhandler.sendEmptyMessage(SERVICE_ERROR);
		}
	}

	@Override
	public void onCallApiFail(int what, JSONObject result) {
		try {
			mloginBtn.setClickable(true);
			String msg= result.getString("msg");
			if ("Wrong login_name or pwd".equals(msg)) {
				LoginActivity.this.myhandler.sendEmptyMessage(ERROE_PWD_NAME);
			} else {
				LoginActivity.this.myhandler.sendEmptyMessage(SERVICE_ERROR);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	class mloginBtnOnclickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			
			mloginBtn.setClickable(false);
			if (mlogin_name.getText().toString().equals("") 
					|| mlogin_pwd.getText().toString().equals("")) {
				LoginActivity.this.myhandler.sendEmptyMessage(EMPTY_PWD_NAME);
			}else {
				dialog = ProgressDialog.
						show(LoginActivity.this, "", "登录中，请稍等 …", true, true);
				new CallApiTask(0, LoginActivity.this).execute();
			}
		}
	}
}
