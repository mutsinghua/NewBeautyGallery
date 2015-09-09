package org.androidbook.gallery.beauty.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.androidbook.gallery.newbeauty.R;
import org.androidbook.utils.FileData;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class SplashActivity extends BaseActivity
{
	private final int GOINTO_PHRASE_2 = 2;
	private final int GOINTO_PHRASE_1 = 1;
	private final int SHOW_IMAGE1 = 10;
	private final int SHOW_IMAGE2 = 11;
	private final int SHOW_IMAGE3 = 12;
	private final int SHOW_IMAGE4 = 13;
	private final int SHOW_TITLE = 14;
	protected static final String TAG = "SplashActivity";

	private int phrase = 0;

	public static final String SCREEN_NAME= "device.screen";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		getWindow().getDecorView().setDrawingCacheEnabled(true);
		

		setContentView(R.layout.splash_phrase_1);
	
		shader = (ImageView) findViewById(R.id.title_shader);
//		AppOffersManager.init(this, NetConstants.YOUMI_App_ID, NetConstants.YOUMI_App_KEY, false);
		// Message msg = Message.obtain();
		// msg.what = GOINTO_PHRASE_2;
		// handler.sendMessageDelayed(msg, 1000);
	
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		if( phrase != 0) //显示标题阶段
		{
			return;
		}
		Animation ani = AnimationUtils.loadAnimation(this, R.anim.to_right);
		ani.setFillAfter(true);
		ani.setAnimationListener(new AnimationListener()
		{

			@Override
			public void onAnimationStart(Animation animation)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationRepeat(Animation animation)
			{
				// TODO Auto-generated method stub

			}

			@Override
			public void onAnimationEnd(Animation animation)
			{
				shader.setVisibility(View.GONE);

				handler.sendEmptyMessageDelayed(GOINTO_PHRASE_1, 1000);

			}
		});
		
		shader.startAnimation(ani);
	}

	private void phraseTwo()
	{
		setContentView(R.layout.splash);
		shader = null;
		
		handler.sendEmptyMessage(SHOW_IMAGE1);
		handler.sendEmptyMessageDelayed(SHOW_IMAGE4,300);
		handler.sendEmptyMessageDelayed(SHOW_IMAGE3,600);
		handler.sendEmptyMessageDelayed(SHOW_IMAGE2,900);
		handler.sendEmptyMessageDelayed(SHOW_TITLE,1200);
		handler.sendEmptyMessageDelayed(0,3000);
	}

	private void startListActivity()
	{
		getScreen();
		Intent intent = new Intent(SplashActivity.this, MainFrameActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
		finish();
	}

	

	@Override
	protected void onDestroy()
	{
	
		handler.removeMessages(0);
		super.onDestroy();
	}

	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case GOINTO_PHRASE_1:
					findViewById(R.id.title_image).startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.disapear));
					handler.sendEmptyMessageDelayed(GOINTO_PHRASE_2, 300);
					phrase=GOINTO_PHRASE_1;
					break;
				case GOINTO_PHRASE_2:

					phraseTwo();
					phrase = GOINTO_PHRASE_2;
					break;
				case SHOW_IMAGE1:
				{
					View view1 = findViewById(R.id.sp_image1);
					view1.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.appear));
					view1.setVisibility(View.VISIBLE);
				}
					break;
				case SHOW_IMAGE2:
				{
					View view1 = findViewById(R.id.sp_image2);
					view1.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.appear));
					view1.setVisibility(View.VISIBLE);
				}
					break;
				case SHOW_IMAGE3:
				{
					View view1 = findViewById(R.id.sp_image3);
					view1.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.appear));
					view1.setVisibility(View.VISIBLE);
				}
					break;
				case SHOW_IMAGE4:
				{
					View view1 = findViewById(R.id.sp_image4);
					view1.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.appear));
					view1.setVisibility(View.VISIBLE);
				}
					break;
				case SHOW_TITLE:
				{
					View view1 = findViewById(R.id.sp_title);
					view1.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.rotate_scale_alpha));
					view1.setVisibility(View.VISIBLE);
					break;
				}
				default:
					

						startListActivity();

					break;
			}

		}
	};
	private ImageView shader;
	
	public void getScreen()
	{
		String path = FileData.getDataPath();
		path = path + SCREEN_NAME;
		File file = new File(path);
		if( !file.exists())
		{
			Bitmap bit = getWindow().getDecorView().getDrawingCache();	
			FileOutputStream fos = null;
			try
			{
				fos = new FileOutputStream(file);
				bit.compress(Bitmap.CompressFormat.JPEG, 70, fos);
				fos.flush();
			} catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				if( fos != null)
				{
					try
					{
						fos.close();
					} catch (IOException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}
	
	}
}
