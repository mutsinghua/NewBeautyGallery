package org.androidbook.gallery.beauty.ui;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.beauty.ui.view.ProcessImageView;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.FavourateManager;
import org.androidbook.netdata.ImageCacheManager;
import org.androidbook.netdata.PhotoDataManager;
import org.androidbook.netdata.xml.data.Picture;
import org.androidbook.utils.Util;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.admogo.AdMogoLayout;

public class ShowFavThumbActivity extends BaseActivity {

	protected static final String TAG = "ShowThumbActivity";
	// public static ThumbManager thumbManager;
	// protected ProgressBar progressBar;

	protected ThumbAdapter ta;
	protected GridView gv;
	private com.admogo.AdMogoLayout adm;
	private String folderName;
	ArrayList<Picture> picList = new ArrayList<Picture>();

	public static void startActivity(String favFolderName, Context context) {
		Intent intent = new Intent(context, ShowFavThumbActivity.class);
		intent.putExtra("favFolderName", favFolderName);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.thumb_show);

		if (savedInstanceState != null) {
			folderName = savedInstanceState.getString("favFolderName");
		} else {
			folderName = getIntent().getStringExtra("favFolderName");
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

		gv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// PhotoShowActivity.startActivity(ShowFavThumbActivity.this,
				// position);
				ImageLookerActivity.startActivity(ShowFavThumbActivity.this, BeautyApplication.instance.getPhotoManager().getPictureUrlList(), position);
			}
		});
		gv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final int pos = position;
				AlertDialog.Builder al = new Builder(ShowFavThumbActivity.this).setMessage(R.string.fav_delete_sure)
						.setPositiveButton(R.string.ok, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								BeautyApplication.instance.getFavManager().deletePicture(picList.get(pos).url);
								picList.remove(pos);
								ta.notifyDataSetChanged();
							}
						}).setNegativeButton(R.string.cancel, null);
				al.create().show();
				return true;
			}

		});
		ad();
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setColsForScreen();
			adm.setVisibility(View.GONE);
		}
		picList = BeautyApplication.instance.getFavManager().getFolderPicture(folderName);
		gv.setAdapter(ta);

		setTitle(folderName);
		BeautyApplication.instance.setPhotoManager(new PhotoDataManager());
		BeautyApplication.instance.getPhotoManager().setPictureList(picList);
		if (ta.getCount() == 0) {
			Toast.makeText(ShowFavThumbActivity.this, R.string.no_fav_picture, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("favFolderName", folderName);
		super.onSaveInstanceState(outState);
	}

	private void ad() {
		adm = (AdMogoLayout) findViewById(R.id.admogo_layout);
	}

	private Handler handle = new Handler() {
		public void handleMessage(Message msg) {
			if (isFinishing()) {
				return;
			}
			switch (msg.what) {
			case R.id.GET_ICON:
				ta.notifyDataSetChanged();
				break;
			}

		}
	};

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		// if (thumbManager != null)
		// {
		// thumbManager.destroy();
		// thumbManager = null;
		// }
		gv.setAdapter(null);
		ta = null;
		super.onDestroy();
	}

	class ThumbAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return picList.size();
		}

		@Override
		public Object getItem(int position) {
			return picList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return picList.get(position).id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;

			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.thumb_grid_item, null);
			}
			Picture pic = (Picture) getItem(position);
			Bitmap bitmap = ImageCacheManager.getInstance().get(pic.url + "_thumb", handle);
			ProcessImageView thumb = (ProcessImageView) view.findViewById(R.id.thumb);
			if (bitmap == null) {
				thumb.setImageBitmap(null, true);
			} else {
				thumb.setImageBitmap(bitmap, false);
			}

			return view;
		}

	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setColsForScreen();
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			adm.setVisibility(View.GONE);
		} else {
			adm.setVisibility(View.VISIBLE);
		}
	}

	private void setColsForScreen() {
		requestUpdateScreen();
		// int cols = screenWidth /
		// (getResources().getDimensionPixelSize(R.dimen.grid_item_size) + 10);
		gv.setNumColumns(gv.AUTO_FIT);

	}
}
