package org.androidbook.gallery.beauty.ui;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.beauty.ui.view.ProcessImageView;
import org.androidbook.gallery.constants.NetConstants;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.net.MainController;
import org.androidbook.net.WinHttpRequest;
import org.androidbook.netdata.ImageCacheManager;
import org.androidbook.netdata.PhotoDataManager;
import org.androidbook.netdata.xml.data.Picture;
import org.androidbook.netdata.xml.data.WinImageDb;
import org.androidbook.utils.FileData;
import org.androidbook.utils.Util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.admogo.AdMogoLayout;

public class ShowThumbActivity extends BaseActivity
{

	protected static final String TAG = "ShowThumbActivity";
	// public static ThumbManager thumbManager;
//	protected ProgressBar progressBar;

	protected ThumbAdapter ta;
	protected GridView gv;
	private com.admogo.AdMogoLayout adm;
	protected TextView pointView;
	private int categoryId;
	ArrayList<Picture> picList = new ArrayList<Picture>();
	

	public static void startActivity(int categoryId, Context context)
	{
		Intent intent = new Intent(context, ShowThumbActivity.class);
		intent.putExtra("categoryId", categoryId);
		context.startActivity(intent);
	}

	
	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putInt("categoryId", categoryId);
	}


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.thumb_show);

		if (savedInstanceState != null)
		{
			categoryId = savedInstanceState.getInt("categoryId");
		} else
		{
			categoryId = getIntent().getIntExtra("categoryId", 0);
		}

		gv = (GridView) findViewById(R.id.thumb_grid);
		ta = new ThumbAdapter();
		// 动画
		int center = getResources().getDimensionPixelSize(R.dimen.grid_item_size);
		Animation ani = Util.getRanAnimation(center / 2, null);
		LayoutAnimationController lac = new LayoutAnimationController(ani);
		lac.setOrder(LayoutAnimationController.ORDER_RANDOM);
		lac.setDelay(0.5f);
		lac.setInterpolator(new AccelerateInterpolator());
		gv.setLayoutAnimation(lac);
		pointView = (TextView) findViewById(R.id.need_points);
		// Log.v(TAG, "winImageDb.value"+winImageDb.value);
		// thumbManager.checkThumb(handle);

		pointView.setVisibility(View.VISIBLE);

		gv.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
//				PhotoShowActivity.startActivity(ShowThumbActivity.this, position);
				ImageLookerActivity.startActivity(ShowThumbActivity.this, BeautyApplication.instance.getPhotoManager().getPictureUrlList(), position);
			}
		});
		
		
		ad();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			setColsForScreen();
//			adm.setVisibility(View.GONE);
		}
		gv.setAdapter(ta);
		BeautyApplication.instance.setPhotoManager(new PhotoDataManager());
		BeautyApplication.instance.getPhotoManager().requestPhotoList(categoryId, handle);
		setTitle(BeautyApplication.instance.getNewDataManager().getImageDbbyCategoryId(categoryId).name);
	}

	private void ad()
	{
		adm = (AdMogoLayout) findViewById(R.id.admogo_layout);
	}

	private Handler handle = new Handler()
	{
		public void handleMessage(Message msg)
		{
			if( isFinishing())
			{
				return;
			}
			switch (msg.what)
			{
			case R.id.GET_PHOTOLIST_SUCCESS:
				picList = (ArrayList<Picture>) msg.obj;
				ta.notifyDataSetChanged();
				break;
			case R.id.GET_PHOTOLIST_FAILED:
				Toast.makeText(ShowThumbActivity.this, R.string.server_not_availble, Toast.LENGTH_LONG).show();
				break;
			case R.id.GET_ICON:
				ta.notifyDataSetChanged();
				break;
				
			}
			pointView.setVisibility(View.GONE);
		}
	};

	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.thumb_menu, menu);
		getMenuInflater().inflate(R.menu.main_frame_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_like_list:
		
			final WinImageDb db = BeautyApplication.instance.getNewDataManager().getImageDbbyCategoryId(categoryId);
			WinHttpRequest request = new WinHttpRequest(NetConstants.REQUEST_ILIKE_URL + db.id, null);
			MainController.getInstance().send(request);
			handle.postDelayed(new Runnable()
			{

				@Override
				public void run()
				{
					Toast.makeText(BeautyApplication.instance, "\"" + db.name + "\"" + BeautyApplication.instance.getString(R.string.recomm_plus), Toast.LENGTH_LONG).show();

				}
			}, 500);
			break;
		case R.id.menu_refresh:
			refresh();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void refresh()
	{
		Toast.makeText(ShowThumbActivity.this, R.string.wait_downloading, Toast.LENGTH_LONG).show();
		BeautyApplication.instance.getPhotoManager().requestPhotoList(categoryId, handle);
		super.refresh();
	}


	@Override
	protected void onResume()
	{
		super.onResume();
	}

	@Override
	protected void onPause()
	{
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		gv.setAdapter(null);
		ta = null;
		super.onDestroy();
	}

	class ThumbAdapter extends BaseAdapter
	{

		ArrayList<View> viewList = new ArrayList<View>();
		@Override
		public int getCount()
		{
			return picList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return picList.get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return picList.get(position).id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			
			if (view == null)
			{
				view = getLayoutInflater().inflate(R.layout.thumb_grid_item, null);
			}
			Picture pic = (Picture) getItem(position);
			Bitmap bitmap = ImageCacheManager.getInstance().get(pic.getThumbUrl(), handle);
			ProcessImageView thumb = (ProcessImageView) view.findViewById(R.id.thumb);
			if (bitmap == null)
			{
				thumb.setImageBitmap(null, true);
			} else
			{
				thumb.setImageBitmap(bitmap, false);
			}
			
			return view;
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		setColsForScreen();
		
	}

	private void setColsForScreen()
	{
		requestUpdateScreen();
//		int cols = screenWidth / (getResources().getDimensionPixelSize(R.dimen.grid_item_size) + 10);
		gv.setNumColumns(gv.AUTO_FIT);

	}
}
