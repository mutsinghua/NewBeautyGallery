package org.androidbook.gallery.beauty.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

public class SplashView extends View {

	private int[] colors = { 0xff84ECB9, 0xff52BBB7, 0xffEB4255, 0xffFAB64B };

	private Paint paint = new Paint();

	public SplashView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		aniHandler.sendEmptyMessageDelayed(0, 1000);
	}

	public SplashView(Context context, AttributeSet attrs) {
		super(context, attrs);
		aniHandler.sendEmptyMessageDelayed(0, 1000);
	}

	public SplashView(Context context) {
		super(context);
		aniHandler.sendEmptyMessageDelayed(0, 1000);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();
		int unit = width / 4;
		if (whitePositin == 0) {
			paint.setColor(0xffffffff);

		} else {

			paint.setColor(colors[0]);
		}
		paint.setStyle(Style.FILL);
		canvas.drawRect(0, 0, unit, height, paint);
		if (whitePositin == 1) {
			paint.setColor(0xffffffff);

		} else {

			paint.setColor(colors[1]);
		}
		canvas.drawRect(unit, 0, unit * 2, height, paint);
		if (whitePositin == 2) {
			paint.setColor(0xffffffff);

		} else {

			paint.setColor(colors[2]);
		}
		canvas.drawRect(unit * 2, 0, unit * 3, height, paint);
		if (whitePositin == 3) {
			paint.setColor(0xffffffff);

		} else {

			paint.setColor(colors[3]);
		}
		canvas.drawRect(unit * 3, 0, width, height, paint);
	}

	private int whitePositin = 0;

	private Handler aniHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			whitePositin = (++whitePositin) % colors.length;
			invalidate();
			sendEmptyMessageDelayed(0, 300);
		};
	};
	
	protected void onDetachedFromWindow() {
		aniHandler.removeMessages(0);
	};
}
