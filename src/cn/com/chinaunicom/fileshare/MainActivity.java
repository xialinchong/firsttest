package cn.com.chinaunicom.fileshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.yidianhulian.Api;
import com.yidianhulian.CallApiTask;
import com.yidianhulian.CallApiTask.CallApiListener;

public class MainActivity extends Activity implements CallApiListener{
	private LinkedList<Object> linklist = new LinkedList<Object>();
	private List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
	private SimpleAdapter adapter;
	private ListView mDirList;
	private final int DEFAULT_LOAD = 1;
	private final int LOAD_FOLDER_CONTENT = 2;
	private final int EMPTY_FOLDER = 3;
	
	private ProgressDialog dialog ;
	
	private Map<String, Object> currDir =  null;
	
	private Handler myhandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
			switch (msg.what) {
			case DEFAULT_LOAD:
				//接口调用出错
				Toast.makeText(MainActivity.this, "服务器错误！", Toast.LENGTH_LONG).show();
				break;
			case EMPTY_FOLDER:
				Toast.makeText(MainActivity.this, "空文件夹！", Toast.LENGTH_LONG).show();
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data.getBooleanExtra("success", true)){
			Map<String, Object> v = (Map<String, Object>)data.getSerializableExtra("validataMap");
			Map<String, String> map = new HashMap<String, String>();
			map.put( v.get("dirid").toString(), v.get("type").toString() );
			FSApp.getInstance().validataMap.add(map);
			//loadData();
		}else{
			linklist.pop();
			currDir = (Map<String, Object>) linklist.getFirst();
			Toast.makeText(this, "密码未验证或验证失败", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		linklist.push("isRoot");
		ActionBar actionBar = getActionBar();
		actionBar.setTitle("联通手机知识库");
		actionBar.setHomeButtonEnabled(true);
		actionBar.setDisplayHomeAsUpEnabled(true);
		
		setContentView(R.layout.main_layout);
		dialog = new ProgressDialog(this);
		dialog.setMessage("数据加载中,请稍等 …");
		adapter = new SimpleAdapter(this, datas, R.layout.diritem, 
				new String[]{"dirname","dirdesc"}, new int[]{R.id.dirname,R.id.dirdesc}){

					@Override
					public View getView(int position, View convertView,
							ViewGroup parent) {
						View rowView= super.getView(position, convertView, parent);
						ImageView iv = (ImageView)rowView.findViewById(R.id.folderimg);
						ImageView lock = (ImageView)rowView.findViewById(R.id.locked);
						ImageView newimg = (ImageView)rowView.findViewById(R.id.newimg);
						
//						LayoutParams para1 = iv.getLayoutParams();
//						para1.width = 50;
//						para1.height = 50;
//						iv.setLayoutParams(para1);
//						LayoutParams para2 = lock.getLayoutParams();
//						para2.width = 24;
//						para2.height = 24;
//						lock.setLayoutParams(para2);
//						lock.setScaleType(ImageView.ScaleType.FIT_END);
						
						String is_empty = datas.get(position).get("is_empty").toString();
						String type     = datas.get(position).get("type").toString();
						String has_unread = datas.get(position).get("has_unread").toString();
						
						if ( "yes".equals(has_unread) ) {
							newimg.setVisibility(View.VISIBLE);
						} else if ( "no".equals(has_unread) ) {
							newimg.setVisibility(View.GONE);
						} 
						
						//1.文件夹为空,图片设成空文件夹的图片
						if ( "folder".equals(type) && "yes".equals(is_empty) ) {
							iv.setImageResource(R.drawable.folder_empty);
						}
						else if ( "folder".equals(type) && "no".equals(is_empty) ) {
							iv.setImageResource(R.drawable.folder);
						}
						//3.文件图片
						else if ( ! "folder".equals(type) ) {
							if ( "word".equals(type) ) {
								iv.setImageResource(R.drawable.word);
							} 
							//Excel文件的图片
							else if ( "excel".equals(type) ) {
								iv.setImageResource(R.drawable.excel);
							} 
							//ppt文件的图片
							else if ( "ppt".equals(type) ) {
								iv.setImageResource(R.drawable.ppt);
							} 
							//pdf文件的图片
							else if ( "pdf".equals(type) ) {
								iv.setImageResource(R.drawable.pdf);
							}
							//图片文件的图片
							else if ( "image".equals(type) ) {
								iv.setImageResource(R.drawable.image);
							}
							//未知类型的图片
							else {
								iv.setImageResource(R.drawable.qt);
							}
						}
						if ( !datas.get(position).get("is_locked").equals("is_locked")) {
							lock.setVisibility(View.GONE);
						} else if ( datas.get(position).get("is_locked").equals("is_locked")) {
							lock.setVisibility(View.VISIBLE);
						}
						return rowView;
					}
		};
		
		mDirList = (ListView)findViewById(R.id.dirlist);
		mDirList.setAdapter(adapter);
		mDirList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String is_empty = datas.get(position).get("is_empty").toString();
				String type     = datas.get(position).get("type").toString();
				//1.文件夹为空,图片设成空文件夹的图片
				if ( "folder".equals(type) && "yes".equals(is_empty) ) {
					//提示文件夹为空，不做任何其他数据处理
					MainActivity.this.myhandler.sendEmptyMessage(EMPTY_FOLDER);
				}
				//2.文件夹或文件未上锁
				else {
					if ( "folder".equals( datas.get(position).get("type") ) ) {
						currDir = datas.get(position);
						if(linklist.indexOf(currDir)==-1)
							linklist.push(currDir);
						loadData();
					} else {
						Intent intent = new Intent();
						intent.putExtra("currDir", (Serializable)datas.get(position));
						intent.setClass(MainActivity.this, LookFileActivity.class);
						MainActivity.this.startActivity(intent);
					}
				}	
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadData();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			if ( ! linklist.isEmpty()) {
				linklist.pop();
			}
			
			if( ! linklist.isEmpty()){
				Object item1 = linklist.getFirst();
				if (item1 instanceof String && item1.equals("isRoot")) {
					currDir = null;
				}else{
					currDir = (Map<String, Object>) item1;
				}
				loadData();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if ( ! linklist.isEmpty()) {
			linklist.pop();
		}
		
		if( ! linklist.isEmpty()){
			Object item = linklist.getFirst();
			if (item instanceof String && item.equals("isRoot")) {
				currDir = null;
			}else{
				currDir = (Map<String, Object>) item;
			}
			loadData();
			return;
		}
		
		super.onBackPressed();
	}

	private void loadData(){
		Map<String, String> map = new HashMap<String, String>();
		if (currDir != null ) {
			map.put( currDir.get("dirid").toString(), currDir.get("type").toString() );
		}
		if(currDir != null
			&& FSApp.getInstance().validataMap.indexOf(map) == -1
			&& "is_locked".equalsIgnoreCase(currDir.get("is_locked").toString()) ) {
			//验证机制：将文件夹的id传入密码验证的activity中进行验证，
			//成功返回一个标志，传到要显示的页面保存起来，下次进入不再验证。
			Intent intent = new Intent();
			intent.putExtra("currDir", (Serializable)currDir);
			intent.setClass(MainActivity.this, PswValidataActivity.class);
			MainActivity.this.startActivityForResult(intent, PswValidataActivity.validate_PSW);

		}else{
			dialog.show();// = ProgressDialog.show(MainActivity.this, "", "数据加载中,请稍等 …", true, true);
			new CallApiTask(DEFAULT_LOAD, MainActivity.this).execute();
//			new CallApiTask(DEFAULT_LOAD, this).execute();
		}
	}
	
	@Override
	public Dialog getProgressDialog(String title) {
		return null;
	}

	@Override
	public JSONObject callApi(int what) {
		Map<String, String> item = new HashMap<String, String>();
		item.put("user_id", FSApp.getInstance().UserId);
		
		if (currDir != null && currDir.get("type").toString().equals("folder")) {
			item.put("id", currDir.get("dirid").toString());
		} else if (currDir != null  && !currDir.get("type").toString().equals("folder")) {
			item.put("id", currDir.get("folder_id").toString());
		}
		
		return Api.get("http://ufileshare.sinaapp.com/filelist_api.php", item);
	}

	@Override
	public void onCallApiSuccess(int what, JSONObject result) {
		switch (what) {
		case DEFAULT_LOAD:
		case LOAD_FOLDER_CONTENT:
			//两种case 成功以后都是在列表上显示
			try {
				JSONArray array = result.getJSONArray("datas");
				datas.clear();
				for (int i = 0; i < array.length(); i++) {
					Map<String, Object> item = new HashMap<String, Object>();
					JSONObject jsonObject = (JSONObject) array.get(i);
					
					String type		  = jsonObject.getString("type");
					String dirdesc    = null;
					String is_empty	  = "no";
					if ( !"folder".equals(type) ) {
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
						
					} else {
						String cntf		  = jsonObject.getString("cnt_f");
						String cntfd	  = jsonObject.getString("cnt_fd");
						if ( (Integer.valueOf(cntf) + Integer.valueOf(cntfd)) <= 0 && "folder".equals(type) ) {
							is_empty = "yes";
						}
						String filestr    = "文件  " + cntf;
						String folderstr  = "  文件夹  " + cntfd;
						dirdesc	  = filestr + folderstr;
						item.put( "parent_id", jsonObject.getString("parent_id") );
						item.put( "parents", jsonObject.getString("parents") );
					}
					
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
				MainActivity.this.myhandler.sendEmptyMessage(0);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
			
	}

	@Override
	public void onCallApiFail(int what, JSONObject result) {
		switch (what) {
		case DEFAULT_LOAD:
		case LOAD_FOLDER_CONTENT:
			MainActivity.this.myhandler.sendEmptyMessage(DEFAULT_LOAD);
			break;
		default:
			break;
		}
	}
}
