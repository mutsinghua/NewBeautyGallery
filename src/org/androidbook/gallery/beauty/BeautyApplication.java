package org.androidbook.gallery.beauty;

import java.util.Locale;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.FavourateManager;
import org.androidbook.netdata.NetDataManager;
import org.androidbook.netdata.NetDataManager.HotDataManager;
import org.androidbook.netdata.NetDataManager.HotPhotoManager;
import org.androidbook.netdata.PhotoDataManager;

import android.app.Application;
import android.content.Context;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;

public class BeautyApplication extends Application
{
	public static final String TAG = "BeautyApplication";
	public static BeautyApplication instance;
	public PowerManager pm ;
	private NetDataManager.HotDataManager hotDataManager;
	private NetDataManager.NewDataManager newDataManager;
	private NetDataManager.HotPhotoManager hotPhotoManager;
	private FavourateManager favManager = null;
	private PhotoDataManager photoManager = null;
	
	private LayoutInflater layoutInflater;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		instance = this;
		setHotDataManager(new NetDataManager.HotDataManager());
		setNewDataManager(new NetDataManager.NewDataManager());
		getHotDataManager().requestGetData(null);
		getNewDataManager().requestGetData(null);
		setFavManager(new FavourateManager());
		layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		getFavManager().loadinit();
		hotPhotoManager = new HotPhotoManager();
		hotPhotoManager.requestGetData(null);
		pm = (PowerManager) instance.getSystemService(Context.POWER_SERVICE);
	}

	@Override
	public void onTerminate()
	{
		getHotDataManager().clear();
		getNewDataManager().clear();
		getHotPhotoManager().clear();
		super.onTerminate();
	}

	@Override
	public void onLowMemory()
	{
		// TODO Auto-generated method stub
		super.onLowMemory();
	}

	public NetDataManager.HotDataManager getHotDataManager()
	{
		return hotDataManager;
	}

	private void setHotDataManager(NetDataManager.HotDataManager hotDataManager)
	{
		this.hotDataManager = hotDataManager;
	}

	public NetDataManager.NewDataManager getNewDataManager()
	{
		return newDataManager;
	}

	private void setNewDataManager(NetDataManager.NewDataManager newDataManager)
	{
		this.newDataManager = newDataManager;
	}

	public LayoutInflater getLayoutInflater()
	{
		return layoutInflater;
	}

	public PhotoDataManager getPhotoManager()
	{
		if(photoManager == null)
		{
			photoManager = new PhotoDataManager();
		}
		return photoManager;
	}

	public void setPhotoManager(PhotoDataManager photoManager)
	{
		this.photoManager = photoManager;
	}

	public FavourateManager getFavManager()
	{
		return favManager;
	}

	private void setFavManager(FavourateManager favManager)
	{
		this.favManager = favManager;
	}

	public NetDataManager.HotPhotoManager getHotPhotoManager()
	{
		return hotPhotoManager;
	}

	public void setHotPhotoManager(NetDataManager.HotPhotoManager hotPhotoManager)
	{
		this.hotPhotoManager = hotPhotoManager;
	}

}
