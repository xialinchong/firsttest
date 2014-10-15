package cn.com.chinaunicom.fileshare;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListAdapter extends BaseAdapter {

	private Context context;
    private List<Map<String, Object>> listItem;
	
    public ListAdapter (Context context,
    		List<Map<String, Object>> listitem) {
    	super();
        this.context = context;
        this.listItem = listitem;
    }
    
	@Override
	public int getCount() {
		return listItem.size();
	}

	@Override
	public Object getItem(int position) {
		return listItem.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String dirnameStr = listItem.get(position).get("dirname").toString();
        String dirdescStr = listItem.get(position).get("dirdesc").toString();
        
        View rowView ;
        
        if (convertView == null) {
            LinearLayout temRl = (LinearLayout) View.inflate(context,
                    R.layout.diritem, null);
            
            rowView = temRl;
        } else {
        	rowView = convertView;
        }
        
        TextView dirname = (TextView)rowView.findViewById(R.id.dirname);
        dirname.setText(dirnameStr);
        TextView dirdesc = (TextView)rowView.findViewById(R.id.dirdesc);
        dirdesc.setText(dirdescStr);
        
		ImageView iv = (ImageView)rowView.findViewById(R.id.folderimg);
		ImageView lock = (ImageView)rowView.findViewById(R.id.locked);
		ImageView newimg = (ImageView)rowView.findViewById(R.id.newimg);
		
		
		String is_empty = listItem.get(position).get("is_empty").toString();
		String type     = listItem.get(position).get("type").toString();
		String has_unread = listItem.get(position).get("has_unread").toString();
		
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
		if ( !listItem.get(position).get("is_locked").equals("is_locked")) {
			lock.setVisibility(View.GONE);
		} else if ( listItem.get(position).get("is_locked").equals("is_locked")) {
			lock.setVisibility(View.VISIBLE);
		}
		return rowView;
	}
}
