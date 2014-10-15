package cn.com.chinaunicom.fileshare;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.yidianhulian.Api;
import com.yidianhulian.CallApiTask;
import com.yidianhulian.CallApiTask.CallApiListener;
import com.yidianhulian.Util;

public class Modify_PSW extends Activity implements CallApiListener {

	private EditText psw = null;
	private EditText newpsw = null;
	private EditText confirm = null;
	private Button submit = null;

	private String newpwd = null;

//	private List<Activity> activityList = new LinkedList<Activity>();
	FSApp app;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar actionBar = getActionBar();
		actionBar.setTitle("ÄúÕýÔÚÐÞ¸ÄµÇÂ¼ÃÜÂë");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.modify_pwd);

//		Intent itn = getIntent();
//		activityList = (List<Activity>) itn.getSerializableExtra("activity");
		app = (FSApp) Modify_PSW.this.getApplication();

		psw = (EditText) findViewById(R.id.oldpsw);

		newpsw = (EditText) findViewById(R.id.newpsw);

		confirm = (EditText) findViewById(R.id.confirm);
		submit = (Button) findViewById(R.id.submit);

		submit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (newpsw.getText().toString()
						.equals(confirm.getText().toString())) {
					if (app.login_pwd.equals( Util.md5( psw.getText().toString() ) ) ) {
						newpwd = newpsw.getText().toString();
						new CallApiTask(0, Modify_PSW.this).execute();
					} else {
						Toast.makeText(Modify_PSW.this, "ÀÏÃÜÂë´íÎó!",
								Toast.LENGTH_LONG).show();
						psw.requestFocus();
					}
				} else {
					Toast.makeText(Modify_PSW.this, "Ç°ºóÃÜÂë²»Ò»ÖÂ!",
							Toast.LENGTH_LONG).show();
					newpsw.requestFocus();
				}
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Dialog getProgressDialog(String title) {
		return null;
	}

	@Override
	public JSONObject callApi(int what) {
		Map<String, String> item = new HashMap<String, String>();
		item.put("user_id", app.UserId);
		item.put("token", app.login_pwd);
		item.put("password", newpwd);

		return Api.post(app.HOST + "user_edit_psw.php", item);
	}

	@Override
	public void onCallApiSuccess(int what, JSONObject result) {
		Toast.makeText(Modify_PSW.this, "ÃÜÂëÐÞ¸Ä³É¹¦!", Toast.LENGTH_LONG).show();
		app.clearMapXml(Modify_PSW.this);
		app.UserId = null;

		Intent intent = new Intent();
		intent.setClass(Modify_PSW.this, LoginActivity.class);
		Modify_PSW.this.startActivity(intent);
//		for (Activity activity : activityList) {
//			activity.finish();
//		}
		Modify_PSW.this.finish();
	}

	@Override
	public void onCallApiFail(int what, JSONObject result) {
		Toast.makeText(Modify_PSW.this, "ÃÜÂëÐÞ¸ÄÊ§°Ü£¬Çë¼ì²éÍøÂç!", Toast.LENGTH_LONG)
				.show();
	}

}
