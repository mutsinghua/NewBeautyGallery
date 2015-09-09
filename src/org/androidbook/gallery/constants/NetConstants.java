package org.androidbook.gallery.constants;

import org.androidbook.gallery.beauty.BeautyApplication;

public class NetConstants
{
	public final static  String WEBSITE="http://www.win16.com/";
	public final static String BK_DOMAIN = "http://www.androidbook.org/";
	public final  static String DIR = "newgallery/";
	public final static String DIR_SUB_AD = "1";
	public final static String DIR_SUB_NM = "0";
	private final static String LIKE_PICTURE = "likepicture.asp?picid=";
	private final static String HOT_PIC = "hotpic.asp";
	private final static String ILIKE = "ilike.asp?categoryid=";
	private final static String CATEGORY_HOT = "hot.asp";
	private final static String CATEGORY_NEW = "category.asp";
	private final static String PHOTO = "photos.asp?cate=";
	private final static String DIR_DATA = "picsNet/";
	public final static String REQUEST_NEW_URL = WEBSITE + DIR +  CATEGORY_NEW;
	public final static String REQUEST_HOT_URL = WEBSITE + DIR + CATEGORY_HOT;
	public final static String REQUEST_PHOTO_URL = WEBSITE + DIR + PHOTO;
	public final static String REQUEST_HOTPHOTO_URL = WEBSITE + DIR + HOT_PIC;
	public final static String REQUEST_DATA_URL = WEBSITE + DIR + DIR_DATA;
	public final static String REQUEST_ILIKE_URL = WEBSITE + DIR + ILIKE;
	public final static String REQUEST_LIKE_PICTURE = WEBSITE + DIR + LIKE_PICTURE;
	public final static String APK_ADDRESS = "getgallery.asp";
	public final static String HELP_ADDRESS = "help.asp";
	public final static String REQUEST_DOWLOAD_URL = WEBSITE + DIR + APK_ADDRESS;
	public final static String REQUEST_HELP_URL = WEBSITE + DIR + HELP_ADDRESS;
}
