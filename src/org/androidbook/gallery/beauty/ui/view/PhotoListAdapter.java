package org.androidbook.gallery.beauty.ui.view;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.constants.NetConstants;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.net.MainController;
import org.androidbook.net.WinHttpRequest;
import org.androidbook.netdata.ImageCacheManager;
import org.androidbook.netdata.xml.data.Picture;
import org.androidbook.netdata.xml.data.WinImageDb;
import org.androidbook.utils.Util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoListAdapter extends BaseAdapter
{

	private ArrayList<Picture> retList = new ArrayList<Picture>();

	Context context;
	public PhotoListAdapter(Context cnx)
	{
		super();
		context = cnx;
	}

	@Override
	public int getCount()
	{

		return retList.size();

	}

	@Override
	public Object getItem(int position)
	{

		return retList.get(position);
	}

	@Override
	public long getItemId(int position)
	{
		return retList.get(position).id;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View view = convertView;

		Picture pic = (Picture) getItem(position);
		WinImageDb imageDb = BeautyApplication.instance.getNewDataManager().getImageDbbyCategoryId(pic.cate_id);
		if (view == null)
		{
			view = BeautyApplication.instance.getLayoutInflater().inflate(R.layout.imagedb_item, null);
		}

		TextView des = (TextView) view.findViewById(R.id.discrption);
		des.setText(imageDb.name);
		TextView size = (TextView) view.findViewById(R.id.text_size);
		size.setText(BeautyApplication.instance.getString(R.string.favorite)+String.valueOf(pic.hot) + BeautyApplication.instance.getString(R.string.fav_uint));
		TextView bt = (TextView) view.findViewById(R.id.download_bt);
		TextView category = (TextView) view.findViewById(R.id.text_category);
		category.setText(Util.parseDate(pic.date));
		view.setTag(pic);
		bt.setVisibility(View.GONE);
		ProcessImageView thumb = (ProcessImageView) view.findViewById(R.id.thumb_pic);
		Bitmap bitmap = ImageCacheManager.getInstance().get(pic.getThumbUrl(), netHandler);
		if (bitmap == null)
		{
			thumb.setImageBitmap(null, true);
		} else
		{
			thumb.setImageBitmap(bitmap, false);
		}
		return view;
	}
	
	public ArrayList<Picture> getRetList()
	{
		return retList;
	}

	public void setRetList(ArrayList<Picture> retList)
	{
		this.retList = retList;
	}

	private Handler netHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{

			case R.id.GET_ICON:
				notifyDataSetChanged();
				break;
			}
		}
	};
}