package org.androidbook.netdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.androidbook.gallery.constants.NetConstants;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.net.IHttpListener;
import org.androidbook.net.MainController;
import org.androidbook.net.WinHttpRequest;
import org.androidbook.netdata.NetDataManager.NewDataManager;
import org.androidbook.netdata.xml.ISAXService;
import org.androidbook.netdata.xml.SAXCategoryService;
import org.androidbook.netdata.xml.SAXPictureService;
import org.androidbook.netdata.xml.data.Picture;
import org.androidbook.netdata.xml.data.WinImageDb;
import org.androidbook.utils.FileData;
import org.apache.http.HttpEntity;

import android.os.Handler;
import android.os.Message;


/**
 * 处理图片列表
 * @author rexzou
 *
 */
public class PhotoDataManager implements IHttpListener  
{
	private  ArrayList<Picture> pictureList = new ArrayList<Picture>();
	
	private Handler outNotify;
	
	private String url;
	
	private File localCache;
	
	private ArrayList<String> pictureUrlList ;
	
	public void requestPhotoList( int cateId, Handler outNotify)
	{
		this.outNotify = outNotify;
		
		url = NetConstants.REQUEST_PHOTO_URL + cateId;
		String localName = FileData.tranferUrltoLocalPath(url);
		localCache = new File(FileData.getCachePath()+localName);
		boolean cached = loadLocalCache(localCache);
		if( !cached) //无缓存，从网络拉取
		{
		
			WinHttpRequest request = new WinHttpRequest();
			request.url = url;
			request.listener = PhotoDataManager.this;
			MainController.getInstance().send(request);
		}
		else
		{  //有缓存，本地读取
			readXmlFromFile();
		}
	}
	
	private void readXmlFromFile()
	{
		try
		{
			FileInputStream fis = new FileInputStream(localCache);
			readXmlDate(fis);
		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean loadLocalCache(File cache)
	{

		if( cache.exists())
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public void handleData(HttpEntity res, WinHttpRequest request) throws Exception
	{
		FileData.writeFileFromInputStream(res, localCache.getAbsolutePath(), handler, request, null);
		
	}

	@Override
	public void onFinish(WinHttpRequest req)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onError(int errorCode, WinHttpRequest req)
	{
		notifyFailed();
		
	}
	
	private void readXmlDate(InputStream is)
	{
		try
		{
			SAXPictureService service = new SAXPictureService();
			service.parse(is, handler);
		} catch (IllegalStateException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case R.id.PARSE_FINISH:
				handleISAXMessage(msg);
				break;
			case R.id.PARSE_FAILED:
				notifyFailed();
				break;
			case R.id.DOWNLOAD_FINISH:
				localCache = new File(localCache.getAbsolutePath());
				readXmlFromFile();
				break;
			}
		}
	};

	protected void handleISAXMessage(Message msg)
	{
		if (msg.obj != null)
		{
			setPictureList((ArrayList<Picture>) msg.obj);
		}
		if( outNotify != null)
		{
			Message msgret = Message.obtain();
			msgret.what = R.id.GET_PHOTOLIST_SUCCESS;
			msgret.obj = pictureList;
			outNotify.sendMessage(msgret);
		}
	}
	
	private void notifyFailed()
	{
		Message msgret = Message.obtain();
		msgret.what = R.id.GET_PHOTOLIST_FAILED;
		outNotify.sendMessage(msgret);
	}

	public ArrayList<Picture> getPictureList()
	{
		return pictureList;
	}

	public void setPictureList(ArrayList<Picture> fav)
	{
		pictureList = fav;
		pictureUrlList = new ArrayList<String>();
		for( Picture pic : fav)
		{
			try
			{
				pictureUrlList.add(pic.getUrl());
			}
			catch (Exception e) {
				pictureUrlList.add(pic.url);
			}
		}
	}

	public ArrayList<String> getPictureUrlList() {
		return pictureUrlList;
	}

}
