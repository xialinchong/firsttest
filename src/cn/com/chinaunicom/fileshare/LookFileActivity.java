package cn.com.chinaunicom.fileshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.anim;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yidianhulian.Api;
import com.yidianhulian.CallApiTask;
import com.yidianhulian.CallApiTask.CallApiListener;

public class LookFileActivity extends Activity implements CallApiListener {

	private static final int MARK_AS_READED = 1;
	private static final int LOAD_FILE = 2;
	private static final int LOAD_NEW_FILE = 3;
	private static final int SERVICE_ERROR = 4;
	
	private ProgressDialog dialog ;
	
//	private static final int OTHER_WAYS = 4;
	private String has_new = null;
	private String desc = null;
	private Button mreaded = null;
	private Button mlookhistory = null;
	private Button mlooknew = null;
	private Button mopenway = null;
	
	private ImageView filelogo = null;
	private TextView filename  = null;
	private TextView filedesc  = null;
	private WebView myWebView = null;
	
	private Map<String, Object> currDir =  null;
	List<Map<String, String>> versionmap;
	private String[] strarr;// = new String[]{};
	private String currId = null;
	
	FSApp app;
	
	private Handler myhandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case MARK_AS_READED:
				loadData(LOAD_FILE);
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				break;
			case SERVICE_ERROR:
				Toast.makeText(LookFileActivity.this, "服务器错误！", Toast.LENGTH_LONG).show();
				break;
				
			default:
				break;
			}
		}
		
	};
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookfile);
		
		app = (FSApp)LookFileActivity.this.getApplication();
		currDir = (Map<String, Object>)getIntent().getSerializableExtra("currDir");
		
		mlookhistory = (Button)findViewById(R.id.lookhistory);
		
		mopenway     = (Button)findViewById(R.id.other_ways);
		mopenway.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//wps通过网络路劲打开文件
				Intent wpsIntent = new Intent();
				wpsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				wpsIntent.setAction(Intent.ACTION_VIEW);
				Uri uri = Uri.parse( currDir.get("file_path").toString() );
				wpsIntent.setData(uri);
				
				try {
					startActivity(wpsIntent);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
		
		mreaded = (Button)findViewById(R.id.readed);
		mreaded.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog = ProgressDialog.show(LookFileActivity.this, "", "处理中，请稍等 …", true, true);
				new CallApiTask(MARK_AS_READED, LookFileActivity.this).execute();
			}
		});
		mlooknew = (Button)findViewById(R.id.looknew);
		mlooknew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData(LOAD_NEW_FILE);
			}
		});
		//上下文菜单换成对话框
		mlookhistory.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showListDialog();
			}
		});
		
		myWebView = (WebView)findViewById(R.id.myWebView);
		myWebView.getSettings().setSupportZoom(true);
		myWebView.getSettings().setBuiltInZoomControls(true);
		myWebView.loadUrl( currDir.get("html_file").toString() );
		myWebView.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadData(LOAD_FILE);
	}

	private void initData(){
		versionmap = (List<Map<String, String>>) currDir.get("versions");
		strarr = new String[versionmap.size()];
		int iteor = 0;
		for (Map<String, String> map : versionmap) {
			strarr[iteor]    = map.get("desc");
			iteor++;
		}
		
		filelogo = (ImageView)findViewById(R.id.filelogo);
		filename = (TextView)findViewById(R.id.filename);
		filedesc = (TextView)findViewById(R.id.filedesc);
		String type = currDir.get("type").toString();
		if (type.equals("word")) {
			filelogo.setImageResource(R.drawable.word);
		} else if (type.equals("excel")) {
			filelogo.setImageResource(R.drawable.excel);
		} else if (type.equals("ppt")) {
			filelogo.setImageResource(R.drawable.ppt);
		} else if (type.equals("pdf")) {
			filelogo.setImageResource(R.drawable.pdf);
		} else if (type.equals("image")) {
			filelogo.setImageResource(R.drawable.image);
		} else {
			filelogo.setImageResource(R.drawable.qt);
		}
		
		filelogo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LookFileActivity.this.finish();
			}
		});
		
		filename.setText(currDir.get("dirname").toString());
		desc = currDir.get("modified_on").toString() + 
				"   " + currDir.get("file_size").toString();
		
		has_new = currDir.get("has_new").toString();
		
		if ("no".equals( has_new )) {
			mlooknew.setVisibility(View.GONE);
			filedesc.setText(desc + "  最新版本");
		} else {
			filedesc.setText(desc);
		}
		
		if ("no".equals( currDir.get("has_unread").toString() )) {
			mreaded.setVisibility(View.GONE);
		}
		
		if ("null".equals( currDir.get("history").toString() )) {
			mlookhistory.setVisibility(View.GONE);
		}
	}
	
	private void loadData(int what){
//		initData();
		
		if(currDir != null ){
			Map<String, String> map = new HashMap<String, String>();
			map.put( currDir.get("dirid").toString(), currDir.get("type").toString() );
			
			if ( app.validataMap.indexOf(map) == -1
					&& "is_locked".equalsIgnoreCase(currDir.get("is_locked").toString()) ) {
				//验证机制：将文件的id传入密码验证的activity中进行验证，
				//成功返回一个标志，传到要显示的页面保存起来，下次进入不再验证。
				Intent intent = new Intent();
				intent.putExtra("currDir", (Serializable)currDir);
				intent.setClass(LookFileActivity.this, PswValidataActivity.class);
				LookFileActivity.this.startActivityForResult(intent, PswValidataActivity.validate_PSW);
			} else {
				new CallApiTask(what, this).execute();
			}
		}else{
			new CallApiTask(what, this).execute();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(data.getBooleanExtra("success", true)){
			Map<String, Object> v = (Map<String, Object>)data.getSerializableExtra("validataMap");
			Map<String, String> map = new HashMap<String, String>();
			map.put( v.get("dirid").toString(), v.get("type").toString() );
			app.validataMap.add(map);
			app.saveArray( v.get("type").toString(), 
					v.get("dirid").toString(), LookFileActivity.this );
			loadData(LOAD_FILE);
		}else{
			this.finish();
		}
	}

	private void showListDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("历史版本");
		builder.setItems(strarr, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				currId = versionmap.get(which).get("id");
				loadData(LOAD_FILE);
			}
		});
		
		builder.create().show();
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
		
		switch (what) {
		case LOAD_FILE:
			if (currId != null) {
				item.put("id", currId);
			} else {
				item.put("id", currDir.get("dirid").toString());
			}
			item.put("load_type", "normal");
			break;
		case MARK_AS_READED:
			item.put("load_type", "mark_read");
			item.put("id", currDir.get("dirid").toString());
			break;
		case LOAD_NEW_FILE:
			item.put("id", currDir.get("dirid").toString());
			item.put("load_type", "new");
			break;
		default:
			break;
		}
		return Api.get(app.HOST + "handle_file_post_get.php", item);
	}

	@Override
	public void onCallApiSuccess(int what, JSONObject result) {
		JSONObject datas;
		try {
			datas = result.getJSONObject("datas");
			JSONObject file = datas.getJSONObject("file");
			currDir.clear();
			

			currDir.put( "file_id", file.getString("version_id") );
			currDir.put( "file_path", file.getString("file_path") );
			currDir.put( "file_size", file.getString("file_size") );
			currDir.put( "folder_id", file.getString("folder_id") );
			currDir.put( "downloads", file.getString("downloads") );
			currDir.put( "reads", file.getString("reads") );
			currDir.put("has_new", file.getString("has_new"));
			currDir.put("history", file.getString("history"));
			
			JSONArray versions = file.getJSONArray("versions");
			List<Map<String, String>> versionmap = new ArrayList<Map<String, String>>();
			for (int j = 0 ; j < versions.length() ; j++) {
				JSONObject versionsobj = (JSONObject) versions.get(j);
				Map<String, String> vercurrDir = new HashMap<String, String>();
				vercurrDir.put("id", versionsobj.getString("id"));
				vercurrDir.put("desc", versionsobj.getString("desc"));
				versionmap.add(vercurrDir);
			}
			currDir.put("versions", versionmap);
			String dirdesc  = file.getString("modified_on") + 
					"    " + file.getString("file_size");
			currDir.put( "dirid", file.getString("id") );
			currDir.put( "dirname", file.getString("name") );
			currDir.put( "psw", file.getString("psw") );
			currDir.put( "is_locked", file.getString("is_locked") );
			currDir.put( "created_on", file.getString("created_on") );
			currDir.put( "modified_on", file.getString("modified_on") );
			currDir.put( "dirdesc",  dirdesc);
			currDir.put("has_unread", file.getString("has_unread"));
			
			currDir.put( "type",  file.getString("type") );
			currDir.put( "is_empty", "no" );
			
			switch (what) {
			case LOAD_FILE:
				//查看文件
				//返回新的currDir
				break;
			case LOAD_NEW_FILE:
				mlooknew.setVisibility(View.GONE);
				break;
			case MARK_AS_READED:
				//没有任何数据处理，是否合并到default
				if (datas.getString("is_insert").equals("yes")) {
					mreaded.setVisibility(View.GONE);
				}
				
				LookFileActivity.this.myhandler.sendEmptyMessage(MARK_AS_READED);
				break;
			default:
				break;
			}
			initData();
		} catch (JSONException e1) {
			e1.printStackTrace();
		}			
	}

	@Override
	public void onCallApiFail(int what, JSONObject result) {
		//错误时做什么操作
		LookFileActivity.this.myhandler.sendEmptyMessage(SERVICE_ERROR);
	}
	
}
