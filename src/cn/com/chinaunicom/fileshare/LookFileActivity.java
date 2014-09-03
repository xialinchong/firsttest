package cn.com.chinaunicom.fileshare;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
	
	private Map<String, Object> currDir =  null;
	List<Map<String, String>> versionmap;
	private String[] strarr;// = new String[]{};
	private String currId = null;
	
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

			default:
				break;
			}
		}
		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lookfile);
		
		mlookhistory = (Button)findViewById(R.id.lookhistory);
		
		mopenway     = (Button)findViewById(R.id.other_ways);
		mopenway.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String type = currDir.get("type").toString();
//				loadData(OTHER_WAYS);
			}
		});
		
		mreaded = (Button)findViewById(R.id.readed);
		mreaded.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog = ProgressDialog.show(LookFileActivity.this, "", "�����У����Ե� ��", true, true);
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
						new CallApiTask(MARK_AS_READED, LookFileActivity.this).execute();
//					}
//				}).start();
//				new CallApiTask(MARK_AS_READED, LookFileActivity.this).execute();
			}
		});
		mlooknew = (Button)findViewById(R.id.looknew);
		mlooknew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData(LOAD_NEW_FILE);
			}
		});
		//�����Ĳ˵����ɶԻ���
		mlookhistory.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showListDialog();
			}
		});
		
		currDir = (Map<String, Object>)getIntent().getSerializableExtra("currDir");
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
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadData(LOAD_FILE);
	}

	private void loadData(int what){
		if ("no".equals( has_new )) {
			mlooknew.setVisibility(View.GONE);
			filedesc.setText(desc + "  ���°汾");
		} else {
			filedesc.setText(desc);
		}
		
		if ("no".equals( currDir.get("has_unread").toString() )) {
			mreaded.setVisibility(View.GONE);
		}
		if ("null".equals( currDir.get("history").toString() )) {
			mlookhistory.setVisibility(View.GONE);
		}
		if(currDir != null ){
			Map<String, String> map = new HashMap<String, String>();
			map.put( currDir.get("dirid").toString(), currDir.get("type").toString() );
			if ( FSApp.getInstance().validataMap.indexOf(map) == -1
					&& "is_locked".equalsIgnoreCase(currDir.get("is_locked").toString()) ) {
				//��֤���ƣ����ļ���id����������֤��activity�н�����֤��
				//�ɹ�����һ����־������Ҫ��ʾ��ҳ�汣���������´ν��벻����֤��
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
			FSApp.getInstance().validataMap.add(map);
			loadData(LOAD_FILE);
		}else{
			this.finish();
		}
	}

	private void showListDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("��ʷ�汾");
		builder.setItems(strarr, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				currId = versionmap.get(which).get("id");
				loadData(LOAD_FILE);
//				Toast.makeText(LookFileActivity.this, versionmap.get(which).get("id"), Toast.LENGTH_LONG).show();;
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
		item.put("user_id", FSApp.getInstance().UserId);
		
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
//		case OTHER_WAYS:
//			item.put("id", currDir.get("dirid").toString());
//			item.put("load_type", "open_way");
//			break;
		default:
			break;
		}
		return Api.get("http://ufileshare.sinaapp.com/handle_file_post_get.php", item);
	}

	@Override
	public void onCallApiSuccess(int what, JSONObject result) {
		switch (what) {
		case LOAD_FILE:
			//�鿴�ļ�
			//�����µ�currDir
			break;
		case LOAD_NEW_FILE:
			mlooknew.setVisibility(View.GONE);
			break;
		case MARK_AS_READED:
			//û���κ����ݴ����Ƿ�ϲ���default
			try {
				JSONObject datas = result.getJSONObject("datas");
				if (datas.getString("is_insert").equals("yes")) {
					mreaded.setVisibility(View.GONE);
				}
				LookFileActivity.this.myhandler.sendEmptyMessage(MARK_AS_READED);
//				loadData(LOAD_FILE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void onCallApiFail(int what, JSONObject result) {
		//����ʱ��ʲô����
	}
	
}
