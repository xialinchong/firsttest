package cn.com.chinaunicom.fileshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.yidianhulian.Api;
import com.yidianhulian.CallApiTask;
import com.yidianhulian.CallApiTask.CallApiListener;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class SearchFileActivity extends Activity implements CallApiListener {

	private EditText name = null;
//	private Button search = null;
	private ListView filelist = null;
	
	private ListAdapter adapter;
	private List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
	
	FSApp app;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_search);
		
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("查询文档");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		app = (FSApp)SearchFileActivity.this.getApplication();
		name   = (EditText) findViewById(R.id.filesname);
		name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				/*判断是否是“search”键*/
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    /*隐藏软键盘*/
                    InputMethodManager imm = (InputMethodManager) v
                            .getContext().getSystemService(
                                    Context.INPUT_METHOD_SERVICE);
                    if (imm.isActive()) {
                        imm.hideSoftInputFromWindow(
                                v.getApplicationWindowToken(), 0);
                    }
                     
                    new CallApiTask(0, SearchFileActivity.this).execute();
                    return true;
                }
				return false;
			}
		});
		
//		search = (Button) findViewById(R.id.search);
//		search.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				new CallApiTask(0, SearchFileActivity.this).execute();
//			}
//		});
		
		adapter = new ListAdapter(this, datas);
		
		filelist = (ListView) findViewById(R.id.filelist);
		filelist.setAdapter(adapter);
		filelist.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent();
				intent.putExtra("currDir", (Serializable)datas.get(position));
				intent.setClass(SearchFileActivity.this, LookFileActivity.class);
				SearchFileActivity.this.startActivity(intent);
			}
		});
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
		item.put("s", name.getText().toString());
		return Api.get(app.HOST + "search_files.php", item);
	}

	@Override
	public void onCallApiSuccess(int what, JSONObject result) {
		try {
			JSONArray array = result.getJSONArray("datas");
			datas.clear();
			for (int i = 0; i < array.length(); i++) {
				Map<String, Object> item = new HashMap<String, Object>();
				JSONObject jsonObject = (JSONObject) array.get(i);
				
				String type		  = jsonObject.getString("type");
				String dirdesc    = null;
				String is_empty	  = "no";
				item.put("html_file", jsonObject.getString("html_file") );
				item.put( "file_id", jsonObject.getString("version_id") );
				item.put( "file_path", jsonObject.getString("file_path") );
				item.put( "file_size", jsonObject.getString("file_size") );
				item.put( "folder_id", jsonObject.getString("folder_id") );
				item.put( "downloads", jsonObject.getString("downloads") );
				item.put( "reads", jsonObject.getString("reads") );
				item.put("has_new", jsonObject.getString("has_new"));
				item.put("history", jsonObject.getString("history"));
				
				JSONArray versions = jsonObject.getJSONArray("versions");
				List<Map<String, String>> versionmap = new ArrayList<Map<String, String>>();
				for (int j = 0 ; j < versions.length() ; j++) {
					JSONObject versionsobj = (JSONObject) versions.get(j);
					Map<String, String> veritem = new HashMap<String, String>();
					veritem.put("id", versionsobj.getString("id"));
					veritem.put("desc", versionsobj.getString("desc"));
					versionmap.add(veritem);
				}
				item.put("versions", versionmap);
				dirdesc  = jsonObject.getString("modified_on") + 
						"    " + jsonObject.getString("file_size");
				
				item.put( "dirid", jsonObject.getString("id") );
				item.put( "dirname", jsonObject.getString("name") );
				item.put( "psw", jsonObject.getString("psw") );
				item.put( "is_locked", jsonObject.getString("is_locked") );
				item.put( "created_on", jsonObject.getString("created_on") );
				item.put( "modified_on", jsonObject.getString("modified_on") );
				item.put( "dirdesc",  dirdesc);
				item.put("has_unread", jsonObject.getString("has_unread"));
				
				item.put( "type",  type );
				item.put( "is_empty", is_empty );
				datas.add(item);
			}
			adapter.notifyDataSetChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCallApiFail(int what, JSONObject result) {
		datas.clear();
		adapter.notifyDataSetChanged();
		Toast.makeText(SearchFileActivity.this, "没有找到匹配的文件!", Toast.LENGTH_LONG)
		.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
		}
		return super.onOptionsItemSelected(item);
	}

}
