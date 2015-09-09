package org.androidbook.netdata.xml.data;

import org.androidbook.gallery.constants.NetConstants;

/**
 * 对应一个目录
 * @author rexzou
 *
 */
public class WinImageDb
{
	public String discription = "";
	public int id;
	public String name = "";
	public int count;
	public String thumb_url = "";
	public String category = "";
	public int like = 0;
	public int read = 0;
	public int hot = 0;
	public static final String CATEGORY = "c";
	public static final String CATEGORY_NAME = "name";
	public static final String CATEGORY_ID = "id";
	public static final String CATEGORY_THUMB = "thumb";
	public static final String CATEGORY_DESC = "desc";
	public static final String CATEGORY_COUNT = "count";
	public static final String CATEGORY_VERSION = "version";
	public static final String CATEGORY_VERSION_CODE = "code";
	public static final String CATEGORY_DIR = "dir";
	public static final String CATEGORY_HOT = "hot";
	public String getThumbUrl()
	{
		return NetConstants.REQUEST_DATA_URL + category + "/"+ thumb_url;
	}




	@Override
	public boolean equals(Object o)
	{
		if (o == this)
		{
			return true;
		}
		if (o instanceof WinImageDb)
		{
			WinImageDb oo = (WinImageDb) o;
			if (oo.id == id)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return Integer.valueOf(id).hashCode();
	}

}