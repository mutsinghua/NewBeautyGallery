package org.androidbook.gallery.beauty.ui.view;

import org.androidbook.gallery.newbeauty.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class ProcessImageView extends RelativeLayout
{

	private ImageView imageView;
	private ProgressBar progressbar;
	
	public ProcessImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public ProcessImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public ProcessImageView(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		RelativeLayout.LayoutParams rlp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		imageView = new ImageView(getContext());
		imageView.setScaleType(ScaleType.CENTER_CROP);
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(imageView, rlp);
		
		rlp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		progressbar = new ProgressBar(getContext());
		rlp.addRule(RelativeLayout.CENTER_IN_PARENT);
		addView(progressbar, rlp);
	}
	
	public void setImageBitmap(Bitmap bitmap,boolean waiting)
	{
		if( waiting)
		{
			progressbar.setVisibility(View.VISIBLE);
		}
		else
		{
			progressbar.setVisibility(View.GONE);
		}
		imageView.setImageBitmap(bitmap);
	}
	
	public void setImageResource(int resid,boolean waiting)
	{
		if( waiting)
		{
			progressbar.setVisibility(View.VISIBLE);
		}
		else
		{
			progressbar.setVisibility(View.GONE);
		}
		imageView.setImageResource(resid);
	}
}
