package org.androidbook.net;

import android.os.HandlerThread;
import android.os.Looper;

public class MainController {
	private static MainController instance;

	private HttpController httpController;

	private HandlerThread mHTread;

	private MainController() {
		httpController = new HttpController(Constant.HTTP_ICON_THREAD_NUMBER);
		mHTread = new HandlerThread("nonoUIThread");
		mHTread.start();
	}

	public static boolean isNull() {
		return instance == null;
	}

	public void init() {

	}

	public Looper getThreadLooper()
	{
		return mHTread.getLooper();
	}
	public void send(WinHttpRequest request) {
		httpController.send(request);
	}

	public static void close() {
		if (instance != null) {
			instance.httpController.close();

		}

		instance = null;
	}

	public synchronized static MainController getInstance() {
		if (instance == null) {
			instance = new MainController();
		}
		return instance;
	}

	public static void destory() {
		if (instance != null) {
			instance.httpController = null;
			instance.mHTread.stop();
		}

		instance = null;

	}
}