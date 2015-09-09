package org.androidbook.netdata.xml.data;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.gallery.constants.NetConstants;


/**
 * 对应一张图片
 * @author rexzou
 *
 */
public class Picture
{
	public int cate_id;
	public String url;
	public int id;
	public int hot;
	public String date;
	
	public static final String PICTURE = "p";
	public static final String PICTURE_ID = "id";
	public static final String PICTURE_URL = "url";
	public static final String CATEGORY_ID = "cid";
	public static final String PICTURE_PARENT = "pt";
	public static final String PICTURE_HOT = "hot";
	public static final String PICTURE_DATE = "date";
	public String getThumbUrl()
	{
		WinImageDb db = BeautyApplication.instance.getNewDataManager().getImageDbbyCategoryId(cate_id);
		return NetConstants.REQUEST_DATA_URL +db.category+"/"+ url + "_thumb";
	}
	
	public String getUrl()
	{
		WinImageDb db = BeautyApplication.instance.getNewDataManager().getImageDbbyCategoryId(cate_id);
		return NetConstants.REQUEST_DATA_URL +db.category+"/"+ url;
	}
	
}
