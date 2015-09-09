package org.androidbook.netdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.constants.NetConstants;
import org.androidbook.gallery.db.LocalSqlDb;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.net.IHttpListener;
import org.androidbook.net.MainController;
import org.androidbook.net.WinHttpRequest;
import org.androidbook.netdata.xml.ISAXService;
import org.androidbook.netdata.xml.SAXCategoryService;
import org.androidbook.netdata.xml.SAXPictureService;
import org.androidbook.netdata.xml.data.Picture;
import org.androidbook.netdata.xml.data.WinImageDb;
import org.androidbook.utils.FileData;
import org.apache.http.HttpEntity;

import android.content.res.Resources.NotFoundException;
import android.os.Handler;
import android.os.Message;

public class NetDataManager
{

	/**
	 * 拉取推荐列表
	 * 
	 * @author rexzou
	 * 
	 */
	public static class HotDataManager implements IHttpListener
	{
		public static final int NOT_YET = 0;
		public static final int REQUESTING = 1;
		public static final int REQUESTED = 2;

		
		
		private ArrayList<WinImageDb> retList = new ArrayList<WinImageDb>();
		private int requestProcess = NOT_YET;
		protected ArrayList<Handler> requestHandler = new ArrayList<Handler>();
		
		public String localCachePath = FileData.getCachePath() + "hot.dat";
		public synchronized void requestGetData(final Handler handler)
		{
			if (getRequestProcess() == REQUESTED)
			{
				sendSuccessResult(handler);
			} else
			{
				if (handler != null)
				{
					requestHandler.add(handler);
				}
				if (getRequestProcess() == NOT_YET)
				{
					setRequestProcess(REQUESTING);
					WinHttpRequest request = new WinHttpRequest();
					request.url = NetConstants.REQUEST_HOT_URL;
					request.listener = this;
					MainController.getInstance().send(request);

				}

			}
		}

		public void clear()
		{
			setRequestProcess(NOT_YET);
			requestHandler.clear();
			retList.clear();
		}
		
		public void notifyResult(boolean successful)
		{
			for (Handler handler : requestHandler)
			{
				if (successful)
				{
					sendSuccessResult(handler);
				} else
				{
					sendFailedResult(handler);
				}
			}
			requestHandler.clear();
		}

		public void sendSuccessResult(Handler handler)
		{
			Message msg = Message.obtain();
			msg.what = R.id.GET_HOT_SUCCESS;
			msg.obj = retList;
			handler.sendMessage(msg);
		}

		public void sendFailedResult(Handler handler)
		{
			Message msg = Message.obtain();
			msg.what = R.id.GET_HOT_FAILED;
			msg.obj = retList;
			handler.sendMessage(msg);
		}

		private void parseLocalFile()
		{
			
			try
			{
				File file =  new File(localCachePath);
				if( !file.exists())
				{
					notifyResult(false);
					return;
				}
				InputStream is = new FileInputStream(file);
				SAXCategoryService service = new SAXCategoryService();
				service.parse(is, handler);
			} catch (IllegalStateException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void handleData(HttpEntity res, WinHttpRequest request) throws Exception
		{
			
			handleNetData(res, request);
			setRequestProcess(REQUESTED);
		}

		protected void handleNetData(HttpEntity res, WinHttpRequest request) throws Exception
		{
			FileData.writeFileFromInputStream(res, localCachePath, null, request, null);
			parseLocalFile();
		}
		
		@Override
		public void onFinish(WinHttpRequest req)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(int errorCode, WinHttpRequest req)
		{
			setRequestProcess(NOT_YET);
			
			parseLocalFile();
		}

		protected void handleISAXMessage(Message msg)
		{
			if (msg.obj != null)
			{
				retList = (ArrayList<WinImageDb>) msg.obj;
			}
		}

		public ArrayList<WinImageDb> getRetList()
		{
			return retList;
		}

		protected void setRetList(ArrayList<WinImageDb> retList)
		{
			this.retList = retList;
		}

		public int getRequestProcess()
		{
			return requestProcess;
		}

		public void setRequestProcess(int requestProcess)
		{
			this.requestProcess = requestProcess;
		}

		protected Handler handler = new Handler( MainController.getInstance().getThreadLooper())
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case R.id.PARSE_FINISH:
					handleISAXMessage(msg);
//					requestProcess = REQUESTED;
					notifyResult(true);
					break;
				default:
//					requestProcess = REQUESTED;
					notifyResult(false);
					break;
				}

			}
		};

	}

	public static class NewDataManager extends HotDataManager
	{

		private HashMap<Integer, WinImageDb> dbInfo = new HashMap<Integer, WinImageDb>();
		
		private int versionCode;

		@Override
		public synchronized void requestGetData(final Handler handler)
		{

			if (getRequestProcess() == REQUESTED)
			{
				sendSuccessResult(handler);
				return;
			}

			if (handler != null)
			{
				requestHandler.add(handler);
			}

			if (getRequestProcess() == REQUESTING)
			{
				return;
			}

			Thread t = new Thread()
			{

				public void run()

				{
					setRequestProcess(REQUESTING);
					setRetList(LocalSqlDb.getInstance().readWholeList());
					for( int i=0;i<getRetList().size();i++)
					{
						dbInfo.put(getRetList().get(i).id, getRetList().get(i));
					}
					versionCode = getRetList().size();

					WinHttpRequest request = new WinHttpRequest();
					request.url = NetConstants.REQUEST_NEW_URL;
					request.queryString = "version=" + versionCode;
					request.listener = NewDataManager.this;
					MainController.getInstance().send(request);
					
				}
			};
			t.start();
		}

		protected void handleNetData(HttpEntity res, WinHttpRequest request) throws Exception
		{
			InputStream is = null;
			try
			{
				
				is = res.getContent();
				SAXCategoryService service = new SAXCategoryService();
				service.parse(is, handler);
			} catch (IllegalStateException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public WinImageDb getImageDbbyCategoryId(int id)
		{
			return dbInfo.get(id);
		}
		
		protected void handleISAXMessage(Message msg)
		{
			if (msg.obj != null)
			{
				ArrayList<WinImageDb> netretList = (ArrayList<WinImageDb>) msg.obj;
				getRetList().addAll(0, netretList);
				notifyResult(true);
				LocalSqlDb.getInstance().beginTransaction();
				for (int i = 0; i < netretList.size(); i++)
				{
					LocalSqlDb.getInstance().insertWinImageDb(netretList.get(i));
				}
				LocalSqlDb.getInstance().endTransaction();
				
				for( int i=0;i<netretList.size();i++)
				{
					dbInfo.put(netretList.get(i).id, netretList.get(i));
				}
				
			}
		}

		public void sendSuccessResult(Handler handler)
		{
			Message msg = Message.obtain();
			msg.what = R.id.GET_NET_NEW_SUCCESS;
			msg.obj = getRetList();
			handler.sendMessage(msg);
		}

		public void sendFailedResult(Handler handler)
		{
			Message msg = Message.obtain();
			msg.what = R.id.GET_NET_NEW_FAILED;
			msg.obj = getRetList();
			handler.sendMessage(msg);
		}
	}
	
	public static class HotPhotoManager implements IHttpListener
	{
		public static final int NOT_YET = 0;
		public static final int REQUESTING = 1;
		public static final int REQUESTED = 2;
		
		private ArrayList<Picture> retList = new ArrayList<Picture>();
		private int requestProcess = NOT_YET;
		protected ArrayList<Handler> requestHandler = new ArrayList<Handler>();

		public String localCachePath = FileData.getCachePath() + "hotpic.dat";
		private boolean recent = true;
		public synchronized void requestGetData(final Handler handler)
		{
			if (getRequestProcess() == REQUESTED)
			{
				sendSuccessResult(handler);
			} else
			{
				if (handler != null)
				{
					requestHandler.add(handler);
				}
				if (getRequestProcess() == NOT_YET)
				{
					setRequestProcess(REQUESTING);
					WinHttpRequest request = new WinHttpRequest();
					if( recent)
					{
					 request.url = NetConstants.REQUEST_HOTPHOTO_URL + "?type=0";
					}
					 else
					{
						 request.url = NetConstants.REQUEST_HOTPHOTO_URL + "?type=1";
					}
					
					request.listener = this;
					MainController.getInstance().send(request);
					
				}

			}
		}

		public void notifyResult(boolean successful)
		{
			for (Handler handler : requestHandler)
			{
				if (successful)
				{
					sendSuccessResult(handler);
				} else
				{
					sendFailedResult(handler);
				}
			}
			requestHandler.clear();
		}

		public void sendSuccessResult(Handler handler)
		{
			Message msg = Message.obtain();
			msg.what = R.id.GET_HOT_PHOTO_SUCCESS;
			msg.obj = retList;
			handler.sendMessage(msg);
		}

		public void sendFailedResult(Handler handler)
		{
			Message msg = Message.obtain();
			msg.what = R.id.GET_HOT_PHOTO_FAILED;
			msg.obj = retList;
			handler.sendMessage(msg);
		}
		public void clear()
		{
			setRequestProcess(NOT_YET);
			requestHandler.clear();
			retList.clear();
		}
		private void parseLocalFile()
		{
			
			try
			{
				File file =  new File(localCachePath);
				if( !file.exists())
				{
					notifyResult(false);
					return;
				}
				InputStream is = new FileInputStream(file);
				SAXPictureService service = new SAXPictureService();
				service.parse(is, handler);
			} catch (IllegalStateException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		@Override
		public void handleData(HttpEntity res, WinHttpRequest request) throws Exception
		{
			
			handleNetData(res, request);

		}

		protected void handleNetData(HttpEntity res, WinHttpRequest request) throws Exception
		{
			FileData.writeFileFromInputStream(res, localCachePath, null, request, null);
			parseLocalFile();
			setRequestProcess(REQUESTED);
		}
		
		@Override
		public void onFinish(WinHttpRequest req)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void onError(int errorCode, WinHttpRequest req)
		{
			setRequestProcess(NOT_YET);
			
			parseLocalFile();
		}

		protected void handleISAXMessage(Message msg)
		{
			if (msg.obj != null)
			{
				retList = (ArrayList<Picture>) msg.obj;
			}
		}

		public ArrayList<Picture> getRetList()
		{
			return retList;
		}

		protected void setRetList(ArrayList<Picture> retList)
		{
			this.retList = retList;
		}

		public int getRequestProcess()
		{
			return requestProcess;
		}

		public void setRequestProcess(int requestProcess)
		{
			this.requestProcess = requestProcess;
		}

		public boolean isRecent() {
			return recent;
		}

		public void setRecent(boolean recent) {
			this.recent = recent;
		}

		protected Handler handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case R.id.PARSE_FINISH:
					handleISAXMessage(msg);
					
					notifyResult(true);
					break;
				default:
					
					notifyResult(false);
					break;
				}

			}
		};
	}
}
