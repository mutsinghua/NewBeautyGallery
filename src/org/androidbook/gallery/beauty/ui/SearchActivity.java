package org.androidbook.gallery.beauty.ui;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.beauty.ui.view.ImageDbAdapter;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.xml.data.WinImageDb;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.umeng.xp.common.ExchangeConstants;
import com.umeng.xp.controller.ExchangeDataService;
import com.umeng.xp.view.ExchangeViewManager;

public class SearchActivity extends BaseActivity
{
	private EditText searchText;
	private ImageDbAdapter adapter;
	private ListView catList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.search_layout);

		setTitle(R.string.search);
		searchText = (EditText) findViewById(R.id.searchText);
		Button searchBt = (Button) findViewById(R.id.search_bt);
		searchBt.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				SearchTask st = new SearchTask();
				st.execute(searchText.getText().toString());
			}
		});
		adapter = new ImageDbAdapter(0);
		catList = (ListView) findViewById(R.id.listView_imagedb);
		catList.setAdapter(adapter);
		catList.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				ShowThumbActivity.startActivity((int) adapter.getItemId(position), SearchActivity.this);
			}
		});
		
		ImageView imageview = (ImageView) findViewById(R.id.ad_umeng_xp);
		Drawable drawable = getResources().getDrawable(R.drawable.umeng_xp_handler_rc);
		new ExchangeViewManager(this, new ExchangeDataService())
		            .addView(ExchangeConstants.type_list_curtain, imageview, drawable);
	}
	
	class SearchTask extends AsyncTask<String, Integer, ArrayList<WinImageDb>>
	{

		
		
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			dialog = ProgressDialog.show(SearchActivity.this, "",
					getString(R.string.search), true,false);
			dialog.show();
		}

		@Override
		protected void onPostExecute(ArrayList<WinImageDb> result)
		{
			super.onPostExecute(result);
			adapter.setRetList(result);
			adapter.notifyDataSetChanged();
			if( dialog!= null && dialog.isShowing())
			{
				dialog.dismiss();
			}
			if( result.size()==0)
			{
				Toast.makeText(SearchActivity.this, R.string.search_no_result, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		protected ArrayList<WinImageDb> doInBackground(String... params)
		{
			ArrayList<WinImageDb> searchResult = new ArrayList<WinImageDb>();
			if( params.length != 1)
			{
				return searchResult;
			}
			String keyword = params[0];
			String[] words = keyword.split(" ");
			ArrayList<WinImageDb> allList = BeautyApplication.instance.getNewDataManager().getRetList();
			for( int i=0;i<allList.size();i++)
			{
				WinImageDb db = allList.get(i);
				boolean found = true;
				for( int j=0;j<words.length;j++)
				{
					String key = words[j];
					if( !TextUtils.isEmpty(key))
					{
						if( db.name.indexOf(key) >=0)
						{
							continue;
						}
						else
						{
							found =false;
							break;
						}
					}
				}
				if(found)
				{
					searchResult.add(db);
				}
			}
				
			return searchResult;
		}
		
	}
}