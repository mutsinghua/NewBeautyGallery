package org.androidbook.gallery.beauty.ui;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.beauty.ui.view.PhotoListAdapter;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.NetDataManager.HotPhotoManager;
import org.androidbook.netdata.xml.data.Picture;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HotPictureListActivity extends BaseActivity
{
	private ArrayList<Picture> retList = new ArrayList<Picture>();
	
	private ListView lv;
	private PhotoListAdapter adapter;

	private TextView pointView;
	private ProgressBar pb;
	private boolean bRecent = true;
	private View waintingLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imagedb_layout);

		BeautyApplication.instance.getHotPhotoManager().requestGetData(netHandler);
		lv = (ListView) findViewById(R.id.listView_imagedb);
		adapter = new PhotoListAdapter(this);
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
//				PhotoShowActivity.startActivity(HotPictureListActivity.this, position);
				BeautyApplication.instance.getPhotoManager().setPictureList(retList);
				ImageLookerActivity.startActivity(HotPictureListActivity.this, BeautyApplication.instance.getPhotoManager().getPictureUrlList(), position);
			}
		});
		waintingLayout = findViewById(R.id.waiting_layer);
		pointView = (TextView) findViewById(R.id.need_points);
		pb = (ProgressBar) findViewById(R.id.progressbar);
		pointView.setVisibility(View.VISIBLE);
		pb.setVisibility(View.VISIBLE);
		waintingLayout.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void refresh()
	{
		bRecent = !bRecent;
		BeautyApplication.instance.getHotPhotoManager().setRecent(bRecent);
		BeautyApplication.instance.getHotPhotoManager().setRequestProcess(HotPhotoManager.NOT_YET);
		BeautyApplication.instance.getHotPhotoManager().requestGetData(netHandler);
		pointView.setVisibility(View.VISIBLE);
		pb.setVisibility(View.VISIBLE);
		waintingLayout.setVisibility(View.VISIBLE);
		super.refresh();
	}




	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_frame_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_refresh:
			refresh();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		adapter.notifyDataSetChanged();
	}
	
	private Handler netHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case R.id.GET_HOT_PHOTO_SUCCESS:
				retList = (ArrayList<Picture>) msg.obj;
				adapter.setRetList(retList);
				adapter.notifyDataSetChanged();
//				pointView.setVisibility(View.GONE);
//				pb.setVisibility(View.GONE);
				waintingLayout.setVisibility(View.GONE);
				BeautyApplication.instance.getPhotoManager().setPictureList(retList);
				break;
			case R.id.GET_HOT_PHOTO_FAILED:
				waintingLayout.setVisibility(View.VISIBLE);
				pointView.setVisibility(View.VISIBLE);
				pointView.setText(R.string.server_not_availble);
				pb.setVisibility(View.GONE);
				break;
			}
		}
	};
}
