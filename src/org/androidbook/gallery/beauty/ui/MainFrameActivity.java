package org.androidbook.gallery.beauty.ui;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.newbeauty.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

public class MainFrameActivity extends TabActivity
{
	private RadioGroup group;
	private TabHost tabHost;


	public static final String TAB_RECOMMAND="tab_recommand";
	public static final String TAB_NEW="tab_new";
	public static final String TAB_SEARCH="tab_search";
	public static final String TAB_FAV="tab_fav";
	public static final String TAB_MORE="tab_more";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.frametab);
		group = (RadioGroup)findViewById(R.id.main_radio);
		tabHost = getTabHost();
		Intent it = new Intent(this,DBListActivity.class);
		it.putExtra("LIST_TYPE", R.id.LIST_TYPE_HOT);
		tabHost.addTab(tabHost.newTabSpec(TAB_RECOMMAND)
	                .setIndicator(TAB_RECOMMAND)
	                .setContent(it));
		it = new Intent(this,DBListActivity.class);
		it.putExtra("LIST_TYPE", R.id.LIST_TYPE_NEW);
	    tabHost.addTab(tabHost.newTabSpec(TAB_NEW)
	                .setIndicator(TAB_NEW)
	                .setContent(it));
	    tabHost.addTab(tabHost.newTabSpec(TAB_SEARCH)
	    		.setIndicator(TAB_SEARCH)
	    		.setContent(new Intent(this,SearchActivity.class)));
	    tabHost.addTab(tabHost.newTabSpec(TAB_FAV)
	    		.setIndicator(TAB_FAV)
	    		.setContent(new Intent(this,ShowFavListActivity.class)));
	    tabHost.addTab(tabHost.newTabSpec(TAB_MORE)
	    		.setIndicator(TAB_MORE)
	    		.setContent(new Intent(this,HotPictureListActivity.class)));
	    group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio_button0:
					tabHost.setCurrentTabByTag(TAB_RECOMMAND);
					break;
				case R.id.radio_button1:
					tabHost.setCurrentTabByTag(TAB_NEW);
					break;
				case R.id.radio_button2:
					tabHost.setCurrentTabByTag(TAB_SEARCH);
					break;
				case R.id.radio_button3:
					tabHost.setCurrentTabByTag(TAB_FAV);
					break;
				case R.id.radio_button4:
					tabHost.setCurrentTabByTag(TAB_MORE);
					break;
				default:
					break;
				}
			}
		});
	}
	@Override
	protected void onDestroy()
	{
	
		BeautyApplication.instance.getHotDataManager().clear();
		BeautyApplication.instance.getNewDataManager().clear();
		BeautyApplication.instance.getHotPhotoManager().clear();
		super.onDestroy();
	}
	
	
}
