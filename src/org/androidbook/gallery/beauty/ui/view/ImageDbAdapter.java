package org.androidbook.gallery.beauty.ui.view;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.constants.NetConstants;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.net.MainController;
import org.androidbook.net.WinHttpRequest;
import org.androidbook.netdata.ImageCacheManager;
import org.androidbook.netdata.xml.data.WinImageDb;

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

public class ImageDbAdapter extends BaseAdapter
{

	private int type;
	private ArrayList<WinImageDb> retList = new ArrayList<WinImageDb>();

	public ImageDbAdapter(int type)
	{
		super();
		this.type = type;
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

		WinImageDb imageDb = (WinImageDb) getItem(position);
		if (view == null)
		{
			view = BeautyApplication.instance.getLayoutInflater().inflate(R.layout.imagedb_item, null);
		}

		TextView des = (TextView) view.findViewById(R.id.discrption);
		des.setText(imageDb.name);
		TextView size = (TextView) view.findViewById(R.id.text_size);
		size.setText(String.valueOf(imageDb.count) + BeautyApplication.instance.getString(R.string.count_uint));
		TextView bt = (TextView) view.findViewById(R.id.download_bt);
		bt.setOnClickListener(ilikeClick);
		bt.setTag(view);
		TextView category = (TextView) view.findViewById(R.id.text_category);
		if( type == R.id.LIST_TYPE_HOT)
		{
			category.setText(imageDb.hot + BeautyApplication.instance.getString(R.string.how_many_people_recommend));
		}
		else
		{
			category.setText(imageDb.discription);
		}
		view.setTag(imageDb);
		if( imageDb.read ==0)
		{
			des.setTextColor(0xfff977e8);
		}
		else
		{
			des.setTextColor(0xffc5198a);
		}
		ProcessImageView thumb = (ProcessImageView) view.findViewById(R.id.thumb_pic);
		Bitmap bitmap = ImageCacheManager.getInstance().get(imageDb.getThumbUrl(), netHandler);
		if (bitmap == null)
		{
			thumb.setImageBitmap(null, true);
		} else
		{
			thumb.setImageBitmap(bitmap, false);
		}
		return view;
	}

	private void showAnimation(View view)
	{

		ImageView heart = (ImageView) view.findViewById(R.id.heart);
		heart.setVisibility(View.VISIBLE);
		Animation anim = AnimationUtils.loadAnimation(BeautyApplication.instance, R.anim.scale_out);
		heart.startAnimation(anim);
		heart.setVisibility(View.INVISIBLE);

	}

	private OnClickListener ilikeClick = new OnClickListener()
	{

		@Override
		public void onClick(View bt)
		{
			View v = (View) bt.getTag();
			final WinImageDb db = (WinImageDb) v.getTag();
			WinHttpRequest request = new WinHttpRequest(NetConstants.REQUEST_ILIKE_URL + db.id, null);
			MainController.getInstance().send(request);
			showAnimation(v);
			netHandler.postDelayed(new Runnable()
			{

				@Override
				public void run()
				{
					Toast.makeText(BeautyApplication.instance, "\"" + db.name + "\"" + BeautyApplication.instance.getString(R.string.recomm_plus), Toast.LENGTH_LONG).show();

				}
			}, 500);
			db.hot++;
			notifyDataSetChanged();
		}
	};

	public ArrayList<WinImageDb> getRetList()
	{
		return retList;
	}

	public void setRetList(ArrayList<WinImageDb> retList)
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