package org.androidbook.gallery.beauty.ui;

import java.util.ArrayList;
import java.util.HashMap;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.beauty.ui.view.ProcessImageView;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.ImageCacheManager;
import org.androidbook.netdata.xml.data.Picture;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

public class ShowFavListActivity extends BaseActivity
{

	private ListView listview;

	private FavAdapter adapter;

	private HashMap<Integer, Boolean> select = new HashMap<Integer, Boolean>();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.imagedb_layout);

		listview = (ListView) findViewById(R.id.listView_imagedb);
		adapter = new FavAdapter();
		listview.setAdapter(adapter);
		

		listview.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				// PhotoShowActivity.startActivity(ShowFavThumbActivity.this,
				// position);
				ShowFavThumbActivity.startActivity((String) adapter.getItem(position), ShowFavListActivity.this);
			}
		});
	}

	@Override
	protected void onResume()
	{
		BeautyApplication.instance.getFavManager().refresh(netHandler);
		super.onResume();
		
		
	}

	@Override
	public void refresh()
	{
		BeautyApplication.instance.getFavManager().refresh(netHandler);
		super.refresh();
	}

	private Handler netHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case R.id.MSG_LOCAL_SCAN_DONE:
			case R.id.CREATE_FAV_FOLDER:
			case R.id.GET_ICON:
				adapter.notifyDataSetChanged();
				break;
			
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.fav_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.select_all:
			for (int i = 0; i < adapter.getCount(); i++)
			{
				select.put(i, true);
			}
			adapter.notifyDataSetChanged();
			break;
		case R.id.unselect_all:
			select.clear();
			adapter.notifyDataSetChanged();
			break;
		case R.id.rev_select_all:
			for (int i = 0; i < adapter.getCount(); i++)
			{
				Boolean selected = select.get(i);
				if (selected == null || !selected)
				{
					select.put(i, true);
				} else
				{
					select.put(i, false);
				}
				adapter.notifyDataSetChanged();
			}
			break;
		case R.id.create_folder:
			BeautyApplication.instance.getFavManager().showNewFolderDialog(this,netHandler);
			break;
		case R.id.delete_folder:
			showDeleteConfirm();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showDeleteConfirm()
	{
		final ArrayList<String> folderToDelete = new ArrayList<String>();
		for (Integer position : select.keySet())
		{
			Boolean selected = select.get(position);
			if (selected != null && selected && position <BeautyApplication.instance.getFavManager().getFavFolderList().size())
			{
				folderToDelete.add(BeautyApplication.instance.getFavManager().getFavFolderList().get(position));
			}
		}
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(R.string.confirm_delete);
		String deleteText = getString(R.string.confirm_delete_text);
		deleteText = deleteText.replace("N", String.valueOf(folderToDelete.size()));
		adb.setMessage(deleteText);
		adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
		{
			
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				DeleteFolderTask dft = new DeleteFolderTask();
				dft.execute(folderToDelete);
				
			}
		});
		adb.setNegativeButton(R.string.cancel, null);
		adb.create().show();
	}

	public OnCheckedChangeListener checkedChange = new OnCheckedChangeListener()
	{
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
		{
			Integer position = (Integer) buttonView.getTag();
			select.put(position, isChecked);
		}
	};

	class FavAdapter extends BaseAdapter
	{

		@Override
		public int getCount()
		{
			return BeautyApplication.instance.getFavManager().getFavFolderList().size();
		}

		@Override
		public Object getItem(int position)
		{
			return BeautyApplication.instance.getFavManager().getFavFolderList().get(position);
		}

		@Override
		public long getItemId(int position)
		{
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			View view = convertView;
			if (view == null)
			{
				view = BeautyApplication.instance.getLayoutInflater().inflate(R.layout.fav_item, null);
			}
			String favFolderName = (String) getItem(position);
			ArrayList<Picture> favlist = BeautyApplication.instance.getFavManager().getFolderPicture(favFolderName);
			CheckBox favName = (CheckBox) view.findViewById(R.id.fav_name);
			
			favName.setText(favFolderName + "("+favlist.size()+getString(R.string.count_uint)+")");
			favName.setTag(new Integer(position));
			favName.setOnCheckedChangeListener(checkedChange);
			Boolean checked = select.get(position);
			if (checked == null || !checked)
			{
				favName.setChecked(false);
			} else
			{
				favName.setChecked(true);
			}

			ProcessImageView thumb1 = (ProcessImageView) view.findViewById(R.id.thumb1);
			ProcessImageView thumb2 = (ProcessImageView) view.findViewById(R.id.thumb2);
			ProcessImageView thumb3 = (ProcessImageView) view.findViewById(R.id.thumb3);

			ArrayList<Picture> favData = BeautyApplication.instance.getFavManager().getFolderPicture(favFolderName);
			if (favData.size() > 0)
			{
				Bitmap map = ImageCacheManager.getInstance().get(favData.get(0).url, netHandler);
				if (map != null)
				{
					thumb1.setImageBitmap(map, false);
				}
			} else
			{
				thumb1.setImageResource(R.drawable.ic_launcher, false);
			}
			if (favData.size() > 1)
			{
				Bitmap map = ImageCacheManager.getInstance().get(favData.get(1).url, netHandler);
				if (map != null)
				{
					thumb2.setImageBitmap(map, false);
				}
			} else
			{
				thumb2.setImageResource(R.drawable.ic_launcher, false);
			}
			if (favData.size() > 2)
			{
				Bitmap map = ImageCacheManager.getInstance().get(favData.get(2).url, netHandler);
				if (map != null)
				{
					thumb3.setImageBitmap(map, false);
				}
			} else
			{
				thumb3.setImageResource(R.drawable.ic_launcher, false);
			}
			return view;
		}

	}

	ProgressDialog progress;
	
	public class DeleteFolderTask extends AsyncTask<ArrayList<String>, Integer, Integer>
	{

		
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			progress = new ProgressDialog(ShowFavListActivity.this);
			progress.setMessage(getString(R.string.deleting));
			progress.show();
			progress.setCancelable(false);
		}

		@Override
		protected void onPostExecute(Integer result)
		{
			if( progress.isShowing())
			{
				progress.dismiss();
			}
			BeautyApplication.instance.getFavManager().refresh(netHandler);
			
			super.onPostExecute(result);
		}

		@Override
		protected Integer doInBackground(ArrayList<String>... params)
		{
			ArrayList<String> folderToDelete = params[0];
			for( int i=0;i<folderToDelete.size();i++)
			{
				BeautyApplication.instance.getFavManager().deleteFolder(folderToDelete.get(i));
			}
			return folderToDelete.size();
		}
		
		
		
	}
}
