package cn.com.chinaunicom.fileshare;

import java.io.Serializable;
import java.util.Map;

import com.yidianhulian.Util;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PswValidataActivity extends Activity {

	private Map<String, Object> currDir =  null;
	private EditText psw =null;
	private Button confirm = null;
//	private Button cancel = null;
//	private Button home =null;
	private String pswStr = null;
	private String intentpsw = null;
	public static final int validate_PSW = 123;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getActionBar();
		
		setContentView(R.layout.psw_validata);
		
		Intent intent = getIntent();
		currDir = (Map<String, Object>)intent.getSerializableExtra("currDir");
		actionBar.setTitle("   " + currDir.get("dirname").toString()+ "-密码验证");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		psw  = (EditText)findViewById(R.id.psw);
		intentpsw   = currDir.get("psw").toString();
		
		confirm     = (Button)findViewById(R.id.confirm);
		
		confirm.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				pswStr = Util.md5(psw.getText().toString());
				//md5加密pswStr
				if (pswStr.equals(intentpsw)) {
					Intent itn = new Intent();
					itn.putExtra("validataMap", (Serializable)currDir);
					itn.putExtra("success", true);
					PswValidataActivity.this.setResult(PswValidataActivity.validate_PSW, itn);
					PswValidataActivity.this.finish();
				} else {
					Toast.makeText(PswValidataActivity.this, "文件密码错误!", Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			Intent itn = new Intent();
			itn.putExtra("validataMap", (Serializable)currDir);
			itn.putExtra("success", false);
			PswValidataActivity.this.setResult(PswValidataActivity.validate_PSW, itn);
			PswValidataActivity.this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		
		Intent itn = new Intent();
		itn.putExtra("validataMap", (Serializable)currDir);
		itn.putExtra("success", false);
		PswValidataActivity.this.setResult(PswValidataActivity.validate_PSW, itn);
		PswValidataActivity.this.finish();
		super.onBackPressed();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

}
