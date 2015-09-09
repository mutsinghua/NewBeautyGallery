package org.androidbook.gallery.beauty.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.beauty.ui.view.WinImageSwitcher;
import org.androidbook.gallery.beauty.ui.view.WinImageSwitcher.OnImageDrawableChangeListener;
import org.androidbook.gallery.constants.NetConstants;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.net.HttpTask;
import org.androidbook.net.IHttpListener;
import org.androidbook.net.MainController;
import org.androidbook.net.WinHttpRequest;
import org.androidbook.netdata.ImageCacheManager;
import org.androidbook.netdata.PhotoDataManager;
import org.androidbook.utils.FileData;
import org.androidbook.utils.SQLUtils;
import org.androidbook.utils.Util;
import org.apache.http.HttpEntity;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Gallery.LayoutParams;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.admogo.AdMogoLayout;

public class PhotoShowActivity extends BaseActivity implements OnTouchListener, ViewFactory
{

	static BaseActivity mContext = null;

	private int switchTime = 3;
	boolean isautoplay = false;
	private static final int MENU_LAYOUT_TIMEOUT = 5000;
	private static final int AD_LAYOUT_TIMEOUT = 100000;
	private static final int MAX_BITMAP_SIZE = 1200;

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{

		getMenuInflater().inflate(R.menu.show_image_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_autoplay:
			if (isautoplay)
			{
				isautoplay = false;
				stopAutoPlay();
			} else
			{
				buildTimeChooser(item);
			}
			break;
		case R.id.menu_setwall:
		{
			try
			{
				WallpaperManager.getInstance(PhotoShowActivity.this).setBitmap(bitmap);
			} catch (IOException e)
			{
				showToast(R.string.set_wall_paper_failed);
				break;
			}
			showToast(R.string.set_wall_paper_success);
		}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private static final String TAG = "Touch";

	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist;

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	private int imagePosition;
	static final int HIDE_BUTTON_LAYER = 1;
	static final int SHOW_BUTTON_LAYER = 2;
	static AtomicInteger tapCount = new AtomicInteger();
	static final int FIRST_TAP = 3;
	static final int SECOND_TAP = 4;
	static final int TIMEOUT = 5;
	static final int GO_NEXT = 1;
	static final int STOP_NEXT = 2;
	public static Bitmap bitmap;
	private Queue<WeakReference<Bitmap>> usedBitmap = new LinkedList<WeakReference<Bitmap>>();
	// public static ThumbManager thumbManager;
	private View buttonLayout;
	private WinImageSwitcher mainImageViewSwitch;

	private Handler timeHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case HIDE_BUTTON_LAYER:
				buttonLayout.startAnimation(AnimationUtils.loadAnimation(PhotoShowActivity.this, android.R.anim.fade_out));
				buttonLayout.setVisibility(View.INVISIBLE);
				break;
			case SHOW_BUTTON_LAYER:
				buttonLayout.startAnimation(AnimationUtils.loadAnimation(PhotoShowActivity.this, android.R.anim.fade_in));
				buttonLayout.setVisibility(View.VISIBLE);
				break;
			case FIRST_TAP:
				timeHandler.sendEmptyMessageDelayed(TIMEOUT, 700);
				tapCount.getAndIncrement();
				break;
			case SECOND_TAP:
				if (tapCount.get() == 1)
				{
					timeHandler.removeMessages(TIMEOUT);
					doDoubleTap((ImageView) msg.obj);
					tapCount.set(0);
				}
				break;
			case TIMEOUT:
				timeHandler.removeMessages(TIMEOUT);
				tapCount.set(0);
				break;
			}
		}
	};

	private void doDoubleTap(ImageView view)
	{
		relocateBitmap(view);
	}
	
	private Handler handlerAutoPlay = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case GO_NEXT:

				startAutoPlay();
				goNext();
				break;
			}
		}
	};

	private ProgressBar processbar_circle;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.image_show);
		//
		// if (thumbManager == null)
		// {
		// finish();
		// return;
		// }

		progressBar = (ProgressBar) findViewById(R.id.processbar);
		processbar_circle = (ProgressBar) findViewById(R.id.processbar_circle);

		imagePosition = -1;
		if (savedInstanceState == null)
		{
			Intent intent = getIntent();
			imagePosition = intent.getIntExtra("IMAGE_POSITION", 0);
		} else
		{
			imagePosition = savedInstanceState.getInt("IMAGE_POSITION", 0);
		}

		mainImageViewSwitch = (WinImageSwitcher) findViewById(R.id.image);
		mainImageViewSwitch.setFactory(this);
		mainImageViewSwitch.setInAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
		mainImageViewSwitch.setOutAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_out));

		mainImageViewSwitch.setOnDrawableChangleListener(new OnImageDrawableChangeListener()
		{

			@Override
			public void onDrawableChange(ImageView imageView)
			{
				relocateBitmap(imageView);

			}
		});

		initButton();
		buttonLayout = findViewById(R.id.menuLayer);
		timeHandler.sendEmptyMessageDelayed(HIDE_BUTTON_LAYER, MENU_LAYOUT_TIMEOUT);

		admogo = (AdMogoLayout) findViewById(R.id.admogo_layout);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			hideAd();
		}
		getImage(imagePosition);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			hideAd();
		} else
		{
			showAd();
		}
	}

	private void showAd()
	{
		admogo.setVisibility(View.VISIBLE);
		handlerAd.sendEmptyMessageDelayed(HIDE_ADMOGO, AD_LAYOUT_TIMEOUT);
	}

	private void hideAd()
	{
		admogo.setVisibility(View.GONE);
		handlerAd.removeMessages(HIDE_ADMOGO);
	}
	private PowerManager.WakeLock wakeLock;
	private void startAutoPlay()
	{
		if (wakeLock == null)
		{
			wakeLock = BeautyApplication.instance.pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "photoshow");
		}
		// if(!wakeLock.isHeld())
		// {
		wakeLock.acquire();
		// }
		handlerAutoPlay.sendEmptyMessageDelayed(GO_NEXT, (switchTime + 2) * 1000);
	}

	private void stopAutoPlay()
	{
		// if(wakeLock !=null && wakeLock.isHeld())
		// {
		if (wakeLock != null)
		{
			wakeLock.release();
		}
		// }
		handlerAutoPlay.removeMessages(GO_NEXT);
	}
	
	private static final int HIDE_ADMOGO = 1;
	private Handler handlerAd = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case HIDE_ADMOGO:
				hideAd();
				break;

			}
		}
	};

	@Override
	public View makeView()
	{
		ImageView imageview = new ImageView(this);
		imageview.setScaleType(ImageView.ScaleType.MATRIX);
		imageview.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		imageview.setOnTouchListener(this);
		return imageview;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem item = menu.findItem(R.id.menu_autoplay);
		if (isautoplay)
		{
			item.setTitle(R.string.stopplay);
			item.setIcon(R.drawable.stop_play);

		} else
		{
			item.setTitle(R.string.autoplay);
			item.setIcon(R.drawable.autoplay);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onDestroy()
	{
		while (usedBitmap.peek() != null)
		{
			WeakReference<Bitmap> wr = usedBitmap.poll();
			Bitmap bm = wr.get();
			if (bm != null && !bm.isRecycled())
			{
				bm.recycle();
			}
		}
		mContext = null;
		bitmap = null;
		// thumbManager = null;
		super.onDestroy();
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		handlerAd.sendEmptyMessageDelayed(HIDE_ADMOGO, AD_LAYOUT_TIMEOUT);

	}

	public void initButton()
	{
		findViewById(R.id.menu_toleft).setOnClickListener(onclickListener);
		findViewById(R.id.menu_toright).setOnClickListener(onclickListener);
		findViewById(R.id.menu_crop).setOnClickListener(onclickListener);
		findViewById(R.id.menu_fav).setOnClickListener(onclickListener);
		findViewById(R.id.menu_share).setOnClickListener(onclickListener);
	}

	private OnClickListener onclickListener = new OnClickListener()
	{

		@Override
		public void onClick(View v)
		{
			timeHandler.removeMessages(HIDE_BUTTON_LAYER);
			timeHandler.sendEmptyMessageDelayed(HIDE_BUTTON_LAYER, MENU_LAYOUT_TIMEOUT);
			switch (v.getId())
			{
			case R.id.menu_toleft:
			{
				int position = imagePosition;
				int oldposition = position;
				position--;

				if (position < 0)// 修正
				{
					position = BeautyApplication.instance.getPhotoManager().getPictureList().size() - 1;
				}
				if (oldposition == position)
				{
					showToast(R.string.only_one);
				} else
				{
					imagePosition = position;
					getImage(position);
				}
			}
				break;
			case R.id.menu_toright:
			{
				goNext();
			}
				break;
			case R.id.menu_share:
			{
				shareBitmap(PhotoShowActivity.this, bitmap);
				break;
			}
			case R.id.menu_fav:
				WinHttpRequest request = new WinHttpRequest();
				request.url = NetConstants.REQUEST_LIKE_PICTURE + BeautyApplication.instance.getPhotoManager().getPictureList().get(imagePosition).id;
				MainController.getInstance().send(request);
				if (mContext instanceof ShowFavThumbActivity)
				{
					showToast(R.string.already_fav);
				} else
				{
					Thread t = new Thread()
					{
						public void run()
						{
							runOnUiThread(new Runnable()
							{

								@Override
								public void run()
								{
									addFav();
								}
							});
						}
					};
					t.start();

				}

				break;

			case R.id.menu_crop:
			{
				CropImageActivity.launchCropperOrFinish(PhotoShowActivity.this, bitmap);
			}
				break;
			}

		}
	};

	private void addFav()
	{
		
		ArrayList<String> favArrayList = new ArrayList<String>();
		favArrayList.addAll(BeautyApplication.instance.getFavManager().getFavFolderList());
		favArrayList.add(getString(R.string.create_fav_folder));
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setItems(favArrayList.toArray(new String[] {}), favItemLisener);
		ab.setTitle(R.string.add_fav_to);
		ab.create().show();
	}

	private DialogInterface.OnClickListener favItemLisener = new DialogInterface.OnClickListener()
	{

		@Override
		public void onClick(DialogInterface dialog, int which)
		{
			ArrayList<String> favArrayList = BeautyApplication.instance.getFavManager().getFavFolderList();
			if (which == favArrayList.size())
			{
				createFavDialog();
			} else
			{
				String filePath = getRealFulPath(imagePosition);
				BeautyApplication.instance.getFavManager().addToFav(favArrayList.get(which), filePath);
				showToast(R.string.fav_success);
			}
			dialog.dismiss();
		}
	};

	public String getRealFulPath(int position)
	{
		String tmpurl = BeautyApplication.instance.getPhotoManager().getPictureList().get(position).url;
		String url = tmpurl;
		if (!tmpurl.startsWith("/"))
		{
			url = BeautyApplication.instance.getPhotoManager().getPictureList().get(position).getUrl();
		}

		String filePath = ImageCacheManager.getIconFullPath(url);
		return filePath;
	}

	private void createFavDialog()
	{
		BeautyApplication.instance.getFavManager().showNewFolderDialog(this, downloadHandler);
	}

	private ImageDownloadTask downloadTask;

	private void getImage(int position)
	{

		if (downloadTask != null)
		{
			downloadTask.notifyHandler = null;
		}
		downloadTask = new ImageDownloadTask(position, downloadHandler);
		downloadTask.execute(position);
		progressBar.setVisibility(View.VISIBLE);
		processbar_circle.setVisibility(View.VISIBLE);
		progressBar.setProgress(0);
	}

	private com.admogo.AdMogoLayout admogo;

	private ProgressBar progressBar;

	private void goNext()
	{
		int position = imagePosition;
		int oldposition = position;

		position++;

		if (position >= BeautyApplication.instance.getPhotoManager().getPictureList().size())// 修正
		{
			position = 0;
		}

		if (oldposition == position)
		{
			showToast(R.string.only_one);
			stopAutoPlay();
			isautoplay = false;
		} else
		{
			imagePosition = position;
			getImage(position);

		}

	}

	public static boolean shareBitmap(Context context, Bitmap bitmap)
	{
		String path = FileData.getDataPath() + "tmp.dat";
		byte[] data = FileData.serializeBitmap(bitmap);
		FileData.writeDataToNewFile(path, data);
		Intent sharei = new Intent(Intent.ACTION_SEND);
		sharei.setType("image/*");
		Uri uri = Uri.fromFile(new File(path));
		sharei.putExtra(Intent.EXTRA_STREAM, uri);
		sharei.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Intent dd = Intent.createChooser(sharei, context.getString(R.string.share));

		if (dd != null)
		{
			context.startActivity(dd);
			return true;
		} else
		{
			return false;
		}
	}

	public Bitmap getSafeBitmap(byte[] data)
	{

		try
		{

			bitmap = FileData.getLimitBitmap(data, MAX_BITMAP_SIZE, MAX_BITMAP_SIZE);
		} catch (OutOfMemoryError ee)
		{
			// Log.e(TAG, "oom2");
			System.gc();
			showToast(R.string.out_of_memery);
			bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_gallery_bg);
		}

		return bitmap;
	}

	private final void switchBitmap()
	{
		// byte[] bitmapdata = SQLUtils.getPic(imagePosition, null);
		// bitmap = getSafeBitmap(bitmapdata);
		mainImageViewSwitch.setImageBitmap(bitmap);

		if (imagePosition % 2 == 0)
		{
			// bitmap = getSafeBitmap(bitmapdata);
			// mainImageViewSwitch.setImageBitmap(bitmap);
			WeakReference<Bitmap> wf = new WeakReference<Bitmap>(bitmap);
			usedBitmap.add(wf);
			while (usedBitmap.size() >= 4)
			{
				WeakReference<Bitmap> wr = usedBitmap.poll();
				Bitmap bm = wr.get();
				if (bm != null && !bm.isRecycled())
				{
					bm.recycle();
				}
			}
		}
		// bitmap = getSafeBitmap(bitmapdata);

	}

	// public void relocateBitmap(ImageView imageView)
	// {
	//
	// int bitmapwith = bitmap.getWidth();
	// int bitmapheight = bitmap.getHeight();
	// double scalw = bitmapwith * 1.0 / screenWidth;
	// int pureScreenHight = (screenHight - Util.getPxFromDp(50, this));
	// double scalh = bitmapheight * 1.0 / pureScreenHight; // 减去下面按钮的高度
	// float scal = 1 / (float) (scalw > scalh ? scalw : scalh);
	// matrix = new Matrix();
	// if (bitmapwith > bitmapheight)// 作修正
	// {
	// // 如果是横副
	// if (screenWidth > screenHight) // 如果屏幕也是横的
	// {
	// matrix.postScale(scal, scal);
	// // do nothing
	// } else
	// {
	// // 图片是横的，屏幕是竖的
	// scal = (float) (1 / scalh);
	// matrix.postScale(scal, scal);
	// matrix.postTranslate(-((bitmapwith * scal) - screenWidth) / 2, 0);
	// }
	// } else
	// {// 竖幅
	// if (screenWidth > screenHight) // 如果屏幕也是横的
	// {
	// matrix.postScale(scal, scal);
	// // scal = (float) (1/ scalw);
	// // matrix.postScale(scal, scal);
	// // matrix.postTranslate(dx, 0);
	// } else
	// {
	// // 图片是竖的，屏幕是竖的
	//
	// // do nothing
	// matrix.postScale(scal, scal);
	// }
	// }
	//
	// // matrix.postScale(scal, scal,(bitmapwith * scal)/2,(bitmapheight
	// // *scal)/2);
	//
	// imageView.setImageMatrix(matrix);
	//
	// }

	public void relocateBitmap(ImageView imageView)
	{
		if (bitmap == null)
		{
			return;
		}
		int bitmapwith = bitmap.getWidth();
		int bitmapheight = bitmap.getHeight();
		double scalw = bitmapwith * 1.0 / screenWidth;
		double scalh = bitmapheight * 1.0 / screenHight;
		float scal = 1 / (float) (scalw > scalh ? scalw : scalh);
		matrix = new Matrix();
		matrix.postScale(scal, scal);

		imageView.setImageMatrix(matrix);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		outState.putInt("IMAGE_POSITION", imagePosition);
		super.onSaveInstanceState(outState);
	}

	public static void startActivity(Context context, int imageid)
	{
		mContext = (BaseActivity) context;

		Intent intent = new Intent(context, PhotoShowActivity.class);
		intent.putExtra("IMAGE_POSITION", imageid);
		context.startActivity(intent);
	}

	public boolean onTouch(View v, MotionEvent event)
	{
		// Handle touch events here...
		ImageView view = (ImageView) v;
		timeHandler.removeMessages(HIDE_BUTTON_LAYER);
		timeHandler.removeMessages(SHOW_BUTTON_LAYER);
		if (isautoplay)
		{
			isautoplay = (false);
			stopAutoPlay();

		}
		if (buttonLayout.getVisibility() == View.INVISIBLE)
		{
			timeHandler.sendEmptyMessage(SHOW_BUTTON_LAYER);
		}
		// Dump touch event to log
		// dumpEvent(event);

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			// Log.d(TAG, "mode=DRAG");
			mode = DRAG;
			break;
		case MotionEvent.ACTION_UP:
			mode = NONE;
			// Log.d(TAG, "mode=NONE");
			timeHandler.sendEmptyMessageDelayed(HIDE_BUTTON_LAYER, MENU_LAYOUT_TIMEOUT);
			if (tapCount.get() == 0)
			{
				timeHandler.sendEmptyMessage(FIRST_TAP);
			} else if (tapCount.get() == 1)
			{
				Message msg = Message.obtain();
				msg.what = SECOND_TAP;
				msg.obj = view;
				timeHandler.sendMessage(msg);
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			// Log.d(TAG, "mode=NONE");
			timeHandler.sendEmptyMessageDelayed(HIDE_BUTTON_LAYER, MENU_LAYOUT_TIMEOUT);
			tapCount.set(0);
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			// Log.d(TAG, "oldDist=" + oldDist);
			if (oldDist > 10f)
			{
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				// Log.d(TAG, "mode=ZOOM");
			}
			tapCount.set(0);
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG)
			{
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			} else if (mode == ZOOM)
			{
				float newDist = spacing(event);
				// Log.d(TAG, "newDist=" + newDist);
				if (newDist > 10f)
				{
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			tapCount.set(0);
			break;
		}

		// Perform the transformation
		view.setImageMatrix(matrix);
		return true; // indicate event was handled
	}

	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(MotionEvent event)
	{
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
		{
			sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++)
		{
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]");
		// Log.d(TAG, sb.toString());
	}

	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private void buildTimeChooser(final MenuItem item)
	{
		LayoutInflater factory = LayoutInflater.from(this);
		final View fontDialogView = factory.inflate(R.layout.dialog_seek, null);
		final AlertDialog seekDialog = new AlertDialog.Builder(this).setView(fontDialogView).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int whichButton)
			{

				SeekBar seekbar = (SeekBar) fontDialogView.findViewById(R.id.font_sekkbar);
				int position = seekbar.getProgress();
				switchTime = position;
				isautoplay = true;
				item.setTitle(R.string.stopplay);
				startAutoPlay();
			}
		}).setNegativeButton(R.string.cancel, null).create();

		SeekBar seekbar = (SeekBar) fontDialogView.findViewById(R.id.font_sekkbar);
		seekbar.setMax(60);

		seekbar.setProgress(switchTime);
		seekDialog.show();

	}

	@Override
	protected void onPause()
	{
		isautoplay = false;
		stopAutoPlay();
		super.onPause();
	}

	private Handler downloadHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case R.id.DOWNLOADING:
				progressBar.setProgress(msg.arg1);
				break;
			case R.id.DOWNLOAD_FINISH:
				if (msg.arg1 == imagePosition)
				{
					progressBar.setVisibility(View.GONE);
					processbar_circle.setVisibility(View.GONE);
					bitmap = (Bitmap) msg.obj;
					switchBitmap();
				}
				break;
			case R.id.CREATE_FAV_FOLDER:
				if (msg.arg1 == 1)
				{
					String filePath = getRealFulPath(imagePosition);
					BeautyApplication.instance.getFavManager().addToFav((String) msg.obj, filePath);
					showToast(R.string.fav_success);
				} else
				{
					showToast(R.string.create_fav_folder_failed);
				}
				break;
			}
		}
	};

	class ImageDownloadTask extends AsyncTask<Integer, Integer, Bitmap> implements IHttpListener
	{
		public int downloadingPosition;
		public Handler notifyHandler;
		private Bitmap bimap;

		public ImageDownloadTask(int downloadingPosition, Handler notifyHandler)
		{
			super();
			this.downloadingPosition = downloadingPosition;
			this.notifyHandler = notifyHandler;
		}

		private Bitmap getLocalCache()
		{

			String filePath = getRealFulPath(downloadingPosition);
			File file = new File(filePath);
			bitmap = null;
			if (file.exists())
			{
				byte[] data;
				try
				{
					data = FileData.readByteFromInputStream(new FileInputStream(file));
					bitmap = getSafeBitmap(data);
				} catch (FileNotFoundException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return bitmap;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(Bitmap result)
		{
			super.onPostExecute(result);

		}

		@Override
		protected Bitmap doInBackground(Integer... params)
		{
			bimap = getLocalCache();
			if (bitmap == null)
			{
				runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						String tmpurl = BeautyApplication.instance.getPhotoManager().getPictureList().get(downloadingPosition).url;
						String url = tmpurl;
						if (!tmpurl.startsWith("/"))
						{
							url = BeautyApplication.instance.getPhotoManager().getPictureList().get(downloadingPosition).getUrl();
						}

						bitmap = ImageCacheManager.getInstance().get(url + "_thumb");
						if (bitmap != null)
						{
							mainImageViewSwitch.setImageBitmap(bitmap);
						}

					}
				});

				WinHttpRequest request = new WinHttpRequest();
				request.url = BeautyApplication.instance.getPhotoManager().getPictureList().get(downloadingPosition).getUrl();
				request.listener = this;
				HttpTask httpTask = new HttpTask(request);
				httpTask.excute();
			} else
			{
				if (notifyHandler != null)
				{
					Message outMes = Message.obtain();
					outMes.what = R.id.DOWNLOAD_FINISH;
					outMes.arg1 = downloadingPosition;
					outMes.obj = bitmap;
					notifyHandler.sendMessage(outMes);
				}
			}
			return bitmap;
		}

		@Override
		public void handleData(HttpEntity res, WinHttpRequest request) throws Exception
		{

			FileData.writeFileFromInputStream(res, ImageCacheManager.getIconFullPath(request.url), netHandler, request, null);

		}

		@Override
		public void onFinish(WinHttpRequest req)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(int errorCode, WinHttpRequest req)
		{

			runOnUiThread(new Runnable()
			{

				@Override
				public void run()
				{
					Toast.makeText(PhotoShowActivity.this, R.string.server_not_availble, Toast.LENGTH_LONG).show();
					progressBar.setVisibility(View.GONE);
					processbar_circle.setVisibility(View.GONE);
				}
			});

		}

		private Handler netHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case R.id.DOWNLOADING:
					int received = msg.arg1;
					int fileSize = msg.arg2;
					if (notifyHandler != null)
					{
						Message outMes = Message.obtain();
						outMes.what = R.id.DOWNLOADING;
						outMes.arg1 = received * 100 / fileSize;
						notifyHandler.sendMessage(outMes);
					}
					break;
				case R.id.DOWNLOAD_FINISH:
					if (notifyHandler != null)
					{
						Message outMes = Message.obtain();
						outMes.what = R.id.DOWNLOAD_FINISH;
						outMes.arg1 = downloadingPosition;
						outMes.obj = getLocalCache();
						notifyHandler.sendMessage(outMes);
					}
					break;
				case R.id.DOWNLOAD_FAILED:
				case R.id.DOWNLOAD_CANCEL:
					if (notifyHandler != null)
					{
						notifyHandler.sendEmptyMessage(R.id.DOWNLOAD_FAILED);
					}
				default:
					break;
				}
			}
		};
	}
	
	
}
