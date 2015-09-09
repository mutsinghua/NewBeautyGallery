package org.androidbook.netdata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.xml.data.Picture;
import org.androidbook.utils.FileData;
import org.androidbook.utils.FileUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.widget.EditText;

public class FavourateManager
{
	private HashMap<String, ArrayList<Picture>> favMap = new HashMap<String, ArrayList<Picture>>();
	private File favRoot;
	private static final int SCANNING = 1;
	private static final int IDLE = 0;
	private int scanFlag = IDLE;
	String defaultFavName;
	private ArrayList<Handler> notifyHandler = new ArrayList<Handler>();

	private ArrayList<String> favFolderList = new ArrayList<String>();

	public void loadinit()
	{
		favRoot = new File(FileData.getFavPath());
		if (!favRoot.exists())
		{
			favRoot.mkdirs();
		}
		defaultFavName = BeautyApplication.instance.getString(R.string.default_fav);
		File[] lists = favRoot.listFiles();
		if (lists == null || lists.length == 0)
		{
			File defaultFav = new File(FileData.getFavPath() + defaultFavName);
			if (!defaultFav.exists())
			{
				defaultFav.mkdirs();
			}
		}
		refresh(null);
	}

	public void showNewFolderDialog(Context context,final Handler callback)
	{
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(R.string.create_fav_folder);
		final EditText et = new EditText(context);
		et.setSingleLine();
		et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
		ab.setView(et);
		ab.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String input = et.getEditableText().toString();
				boolean suc = addFavFolder(input);
				if( callback != null)
				{
					Message msg = Message.obtain();
					msg.what = R.id.CREATE_FAV_FOLDER;
					msg.obj = input;
					msg.arg1 = suc?1:0;
					callback.sendMessage(msg);
				}
			}
		});
		ab.setNegativeButton(R.string.cancel, null);
		ab.create().show();
	}
	
	public boolean addFavFolder(String folderName)
	{
		File newFolder = new File(FileData.getFavPath() + folderName);
		if (newFolder.exists())
		{
			return false;
		} else
		{
			boolean ret = newFolder.mkdirs();
			refresh(null);
			return ret;
		}
		
	}

	public void addToFav(String folderName, String localFullPath)
	{
		File soure = new File(localFullPath);
		File targPath = new File(FileData.getFavPath() + "/" + folderName);
		if (!targPath.exists())
		{
			targPath.mkdirs();
		}
		FileData.copyFile(localFullPath, FileData.getFavPath() + "/" + folderName + "/" +soure.getName());
		FileData.copyFile(localFullPath, FileData.getFavPath() + "/" + folderName + "/" +soure.getName() + "_thumb");
	}

	public void deleteFolder(String folderName)
	{
		File targPath = new File(FileData.getFavPath() + "/" + folderName);
		FileUtils.clearDir(FileData.getFavPath() + "/" + folderName);
		targPath.delete();
	}

	public void deletePicture(String localFullPath)
	{
		File soure = new File(localFullPath);
		soure.delete();
		soure = new File(localFullPath+"_thumb");
		soure.delete();
	}
	
	public synchronized void refresh(Handler handler)
	{
		if (handler != null)
		{
			notifyHandler.add(handler);
		}
		if (scanFlag != SCANNING)
		{
			scanFlag = SCANNING;
			ScanTask scan = new ScanTask();
			scan.execute(favRoot);
		}
	}

	public ArrayList<Picture> getFolderPicture(String foldName)
	{
		return favMap.get(foldName);
	}

	public ArrayList<String> getFavFolderList()
	{
		return favFolderList;
	}

	private void setFavFolderList(ArrayList<String> favFolderList)
	{
		this.favFolderList = favFolderList;
	}

	class ScanTask extends AsyncTask<File, Integer, HashMap<String, ArrayList<Picture>>>
	{
		@Override
		protected HashMap<String, ArrayList<Picture>> doInBackground(File... params)
		{
			HashMap<String, ArrayList<Picture>> temp = new HashMap<String, ArrayList<Picture>>();
			ArrayList<String> tmpList = new ArrayList<String>();
			File favRoot = params[0];
			File[] favFolder = favRoot.listFiles();
			if (favFolder == null)
			{
				return null;
			}
			for (int i = 0; i < favFolder.length; i++)
			{
				if (favFolder[i].isDirectory())
				{
					String fileName = favFolder[i].getName();
					File[] photoListFiles = favFolder[i].listFiles();
					ArrayList<Picture> favPhotos = new ArrayList<Picture>();
					for (int j = 0; j < photoListFiles.length; j++)
					{
						String photoName = photoListFiles[j].getName();
						// 去了缩略图
						if (photoName.indexOf("thumb") > 0)
						{
							continue;
						}
						File thumb = new File(photoListFiles[j].getAbsolutePath() + "_thumb");
						if (thumb.exists())
						{
							Picture pic = new Picture();
							pic.url = photoListFiles[j].getAbsolutePath();
							favPhotos.add(pic);
						}
					}
					temp.put(fileName, favPhotos);
					if (defaultFavName.equalsIgnoreCase(fileName))
					{
						tmpList.add(0, fileName);
					} else
					{
						tmpList.add(fileName);
					}
				}
			}
			scanFlag = IDLE;
			favMap.clear();
			favMap = temp;
			setFavFolderList(tmpList);
			return favMap;
		}

		@Override
		protected void onPostExecute(HashMap<String, ArrayList<Picture>> result)
		{
			super.onPostExecute(result);
			for (Handler handler : notifyHandler)
			{
				handler.sendEmptyMessage(R.id.MSG_LOCAL_SCAN_DONE);
			}
			notifyHandler.clear();
		}

	}

	
}
