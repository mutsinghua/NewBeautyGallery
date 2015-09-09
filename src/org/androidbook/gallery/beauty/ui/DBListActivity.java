package org.androidbook.gallery.beauty.ui;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.beauty.ui.view.ImageDbAdapter;
import org.androidbook.gallery.db.LocalSqlDb;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.NetDataManager.HotDataManager;
import org.androidbook.netdata.xml.data.WinImageDb;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tencent.mobwin.AdView;

/**
 * 显示图片列表
 * 
 * @author rexzou
 * 
 */
public class DBListActivity extends BaseActivity {

	private ArrayList<WinImageDb> retList = new ArrayList<WinImageDb>();

	private ListView lv;
	private ImageDbAdapter adapter;

	private TextView pointView;
	private ProgressBar pb;

	@Override
	protected void onResume() {
		super.onResume();
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imagedb_layout);
		type = 0;
		if (savedInstanceState != null) {
			type = savedInstanceState.getInt("LIST_TYPE");
		} else {
			type = getIntent().getIntExtra("LIST_TYPE", R.id.LIST_TYPE_HOT);
		}
		ListDataRequestFactory.requestData(type, netHandler, false);
		lv = (ListView) findViewById(R.id.listView_imagedb);
		adapter = new ImageDbAdapter(type);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ShowThumbActivity.startActivity((int) adapter.getItemId(position), DBListActivity.this);
				WinImageDb db = (WinImageDb) adapter.getItem(position);
				db.read = 1;
				LocalSqlDb.getInstance().updateReadValue(db, 1);
			}
		});
		waintingLayout = findViewById(R.id.waiting_layer);
		pointView = (TextView) findViewById(R.id.need_points);
		pb = (ProgressBar) findViewById(R.id.progressbar);
		waintingLayout.setVisibility(View.VISIBLE);
		LinearLayout container = (LinearLayout) findViewById(R.id.miniAdLinearLayout);
		if (type == R.id.LIST_TYPE_HOT) {

			AdView adview;

			adview = new com.tencent.mobwin.AdView(this);
			container.addView(adview);

		}

		if (type == R.id.LIST_TYPE_NEW) {

			com.tencent.mobwin.AdView adview;

			adview = new com.tencent.mobwin.AdView(this);
			container.addView(adview);

		}
	}

	private Handler netHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case R.id.GET_HOT_SUCCESS:
			case R.id.GET_NET_NEW_SUCCESS:
				retList = (ArrayList<WinImageDb>) msg.obj;
				adapter.setRetList(retList);
				adapter.notifyDataSetChanged();
				waintingLayout.setVisibility(View.GONE);
				break;
			case R.id.GET_HOT_FAILED:
			case R.id.GET_NET_NEW_FAILED:
				waintingLayout.setVisibility(View.VISIBLE);
				pointView.setVisibility(View.VISIBLE);
				pointView.setText(R.string.server_not_availble);
				pb.setVisibility(View.GONE);
				break;
			}
		}
	};

	private int type;

	@Override
	public void refresh() {
		ListDataRequestFactory.requestData(type, netHandler, true);
		pointView.setVisibility(View.VISIBLE);
		pb.setVisibility(View.VISIBLE);
		waintingLayout.setVisibility(View.VISIBLE);
		super.refresh();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt("LIST_TYPE", type);
		super.onSaveInstanceState(outState);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.category_menu, menu);
		getMenuInflater().inflate(R.menu.main_frame_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			refresh();
			break;
		case R.id.menu_clear: {
			int size = adapter.getCount();
			for (int i = 0; i < size; i++) {
				WinImageDb db = (WinImageDb) adapter.getItem(i);
				db.read = 0;
			}
			adapter.notifyDataSetChanged();
			LocalSqlDb.getInstance().clearReadValue();
		}
			break;
		case R.id.menu_filter: {
			showFilter();
		}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showFilter() {
		ArrayList<String> favArrayList = new ArrayList<String>();
		favArrayList.add(getString(R.string.unread));
		favArrayList.add(getString(R.string.already_read));
		favArrayList.add(getString(R.string.all_read));
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setItems(favArrayList.toArray(new String[] {}), fliterLisener);
		ab.setTitle(R.string.fliter);
		ab.create().show();
	}

	private DialogInterface.OnClickListener fliterLisener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case 0: {
				ArrayList<WinImageDb> unreadlist = new ArrayList<WinImageDb>();
				int size = retList.size();
				for (int i = 0; i < size; i++) {
					if (retList.get(i).read == 0) {
						unreadlist.add(retList.get(i));
					}
				}
				adapter.setRetList(unreadlist);
				adapter.notifyDataSetChanged();
			}
				break;
			case 1: {
				ArrayList<WinImageDb> readlist = new ArrayList<WinImageDb>();
				int size = retList.size();
				for (int i = 0; i < size; i++) {
					if (retList.get(i).read != 0) {
						readlist.add(retList.get(i));
					}
				}
				adapter.setRetList(readlist);
				adapter.notifyDataSetChanged();
			}
				break;
			case 2: {
				adapter.setRetList(retList);
				adapter.notifyDataSetChanged();
			}
				break;
			}
			dialog.dismiss();
		}
	};

	private View waintingLayout;

	private static class ListDataRequestFactory {

		public static void requestData(int type, Handler handler, boolean refresh) {
			if (type == R.id.LIST_TYPE_HOT) {
				if (refresh) {
					BeautyApplication.instance.getHotDataManager().setRequestProcess(HotDataManager.NOT_YET);
				}
				BeautyApplication.instance.getHotDataManager().requestGetData(handler);
			} else {
				if (refresh) {
					BeautyApplication.instance.getNewDataManager().setRequestProcess(HotDataManager.NOT_YET);
				}
				BeautyApplication.instance.getNewDataManager().requestGetData(handler);
			}
		}
	}
}
