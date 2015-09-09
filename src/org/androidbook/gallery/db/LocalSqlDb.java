package org.androidbook.gallery.db;

import java.util.ArrayList;

import org.androidbook.gallery.beauty.BeautyApplication;
import org.androidbook.netdata.xml.data.WinImageDb;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public class LocalSqlDb extends SQLiteOpenHelper
{
	private static final String TAG = "LocalSqlDb";
	private static LocalSqlDb instance ;
	private static final String DB_NAME = "data";
	private SQLiteDatabase database;

	public static final int DB_VERSION = 2;
	public static final String TABLE_NAME = "PHOTO_LIST";
	
	public static final String COL_DB_ID = "db_id";
	public static final String COL_DB_NAME = "db_name";
	public static final String COL_DB_DESC = "db_description";
	public static final String COL_DB_SIZE = "db_size";
	public static final String COL_DB_CATE = "db_category";
	public static final String COL_DB_THUMB = "db_thumb";
	public static final String COL_DB_LIKE = "db_like";
	public static final String COL_DB_READ = "db_read";
	public static final String COL_DB_RESERVER = "db_reserver";
	public static final String[] COLS_FOR_ID = {COL_DB_ID};
	
	private static final String CREATE_PHOTO_LIST_TABLE = "create table if not exists PHOTO_LIST(" +
				"db_id INTEGER PRIMARY KEY, " +
				"db_name TEXT," +
				"db_description TEXT," +
				"db_count INTEGER,"+
				"db_category TEXT,"+
				"db_thumb TEXT,"+
				"db_size INTEGER,"+
				"db_like INTEGER,"+
				"db_read INTEGER,"+
				"db_reserver TEXT"+
				");";
	
	
	
	/**
	 * 创建表sql
	 */
	private final static String CREATE_DATA_INFO_TABLE = "create table if not exists DATA_INFO(KEY_NAME TEXT PRIMARY KEY, TEXT_VALUE TEXT, INTEGER_VALUE, BYTE_ARRAY BLOB);";
	public static final String DATA_TABLE_NAME = "DATA_INFO";
	
	private LocalSqlDb(Context context, String name, CursorFactory factory, int version)
	{
		super(context, name, factory, version);
		instance = this;
	}

	public static LocalSqlDb getInstance()
	{
//		Log.v(TAG, "getInstance");
		if( instance == null)
		{
			instance = new LocalSqlDb( BeautyApplication.instance,  DB_NAME,  null,  DB_VERSION);
			
			instance.open();
		}
		return instance;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(CREATE_DATA_INFO_TABLE);
		db.execSQL(CREATE_PHOTO_LIST_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL("drop table if exists PHOTO_LIST;");
		db.execSQL(CREATE_PHOTO_LIST_TABLE);
	}
	
	private void open()
	{
//		Log.v(TAG, "open");
		database = getWritableDatabase();
	
	}

	public void close()
	{
//		Log.v(TAG, "close");
		if( database != null && database.isOpen())
		{
			database.close();
//			Log.v(TAG, "closeed");
		}
	}
	/**
	 * 读取本地数据库中的所有列表
	 * @param db
	 * @return
	 */
	public ArrayList<WinImageDb> readWholeList()
	{
		ArrayList<WinImageDb> ret  =new ArrayList<WinImageDb>();
		Cursor cursor =  database.query(TABLE_NAME, null, null, null, null, null, COL_DB_ID + " desc");
		if( cursor != null)
		{
			if( cursor.moveToFirst())
			{
				do
				{
					WinImageDb imageDb = new WinImageDb();
					imageDb.id = getIntFromCursor(cursor, COL_DB_ID);
					imageDb.name = getStringFromCursor(cursor, COL_DB_NAME);
					imageDb.discription = getStringFromCursor(cursor, COL_DB_DESC);
					imageDb.count = getIntFromCursor(cursor, COL_DB_SIZE);
					imageDb.thumb_url = getStringFromCursor(cursor, COL_DB_THUMB);
					imageDb.category = getStringFromCursor(cursor, COL_DB_CATE);
					imageDb.like = getIntFromCursor(cursor, COL_DB_LIKE);
					imageDb.read = getIntFromCursor(cursor, COL_DB_READ);
					ret.add(imageDb);
				}
				while(cursor.moveToNext());
			}
			cursor.close();
		}
		return ret;
		
	}
	
	public static void destory()
	{
		if( instance != null)
		{
//			Log.v(TAG, "destory");
				instance.close();

		}
		instance = null;
	}

	public void beginTransaction()
	{
		database.beginTransaction();
	}
	
	public void endTransaction()
	{
		database.setTransactionSuccessful();
		database.endTransaction();

	}
	
	public void updateLikeValue(WinImageDb db)
	{
		ContentValues dv = new ContentValues(1);
		dv.put(COL_DB_LIKE, db.like);
		database.update(TABLE_NAME, dv, COL_DB_ID + "=?", new String[]{String.valueOf(db.id)});
	}
	
	/**
	 * 更新已读标志
	 * @param db
	 * @param read
	 */
	public void updateReadValue(WinImageDb db, int read)
	{
		ContentValues dv = new ContentValues(1);
		dv.put(COL_DB_READ, db.read);
		database.update(TABLE_NAME, dv, COL_DB_ID + "=?", new String[]{String.valueOf(db.id)});
	}
	
	/**
	 * 清除阅读历史
	 */
	public void clearReadValue()
	{
		ContentValues dv = new ContentValues(1);
		dv.put(COL_DB_READ, 0);
		database.update(TABLE_NAME, dv, null, null);
	}
	/**
	 * 插入网络上的数据
	 * @param widb
	 * @param forceupdate
	 */
	public void insertWinImageDb (WinImageDb widb)
	{
		
		Cursor cursor = database.query(TABLE_NAME, COLS_FOR_ID, COL_DB_ID +"=?", new String[]{String.valueOf(widb.id)}, null, null, null);
		if( cursor != null)
		{
			if( cursor.moveToFirst())
			{

			}
			else
			{
				 //没数据
				ContentValues cv = new ContentValues();
				cv.put(COL_DB_NAME, widb.name);
				cv.put(COL_DB_DESC, widb.discription);
				cv.put(COL_DB_CATE, widb.category);
				cv.put(COL_DB_SIZE, widb.count);
				cv.put(COL_DB_ID, widb.id);
				cv.put(COL_DB_THUMB, widb.thumb_url);
				database.insert(TABLE_NAME, null, cv);
				
			}
			cursor.close();
		}
	}
	
	public static int getIntFromCursor(Cursor cursor, String colName)
	{
		return cursor.getInt(cursor.getColumnIndex(colName));
	}
	
	public static String getStringFromCursor(Cursor cursor, String colName)
	{
		return cursor.getString(cursor.getColumnIndex(colName));
	}
	
	/**
	 * 读取整形
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getInt(String key, int defaultValue)
	{
		int ret = defaultValue;
		Cursor curor = database.query(DATA_TABLE_NAME, new String[]{"KEY_NAME","INTEGER_VALUE"}, "KEY_NAME=?", new String[]{key},null,null,null);
		if( curor.moveToFirst())
		{
			ret = curor.getInt(1);
		}
		curor.close();
		return ret;
	}
	
	/**
	 * 读取字符串
	 * @param key
	 * @return
	 */
	public  String getString(String key,String defaultValue)
	{
		String ret = defaultValue;
		Cursor curor = database.query(DATA_TABLE_NAME, new String[]{"KEY_NAME","TEXT_VALUE"}, "KEY_NAME=?", new String[]{key},null,null,null);
		if( curor.moveToFirst())
		{
			ret = curor.getString(1);
		}
		curor.close();
		return ret;
	}
	
	/**
	 * 读取二进制数据
	 * @param key
	 * @return
	 */
	public byte[] getBytes(String key)
	{
		byte[] ret = null;
		Cursor curor = database.query(DATA_TABLE_NAME, new String[]{"KEY_NAME","BYTE_ARRAY"}, "KEY_NAME=?", new String[]{key},null,null,null);
		if( curor.moveToFirst())
		{
			ret = curor.getBlob(1);
		}
		curor.close();
		return ret;
	}
	
	/**
	 * 写入int
	 * @param key
	 * @param value
	 * @return
	 */
	public  boolean setInt(String key,int value)
	{
		try
		{
		ContentValues cv = new ContentValues();
		cv.put("KEY_NAME", key);
		cv.put("INTEGER_VALUE", value);
		int affectiveLine = database.update(DATA_TABLE_NAME, cv, "KEY_NAME=?", new String[]{key});
		if( affectiveLine == 0)
		{
			database.insert(DATA_TABLE_NAME, null, cv);
		}
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 写入string
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean  setString(String key,String value)
	{
		try
		{
		ContentValues cv = new ContentValues();
		cv.put("KEY_NAME", key);
		cv.put("TEXT_VALUE", value);
		int affectiveLine = database.update(DATA_TABLE_NAME, cv, "KEY_NAME=?", new String[]{key});
		if( affectiveLine == 0)
		{
			database.insert(DATA_TABLE_NAME, null, cv);
		}
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 写入 byte数组
	 * @param key
	 * @param value
	 * @return
	 */
	public  boolean setBytes(String key,byte[] value)
	{
		try
		{
		ContentValues cv = new ContentValues();
		cv.put("KEY_NAME", key);
		cv.put("BYTE_ARRAY", value);
		int affectiveLine = database.update(DATA_TABLE_NAME, cv, "KEY_NAME=?", new String[]{key});
		if( affectiveLine == 0)
		{
			database.insert(DATA_TABLE_NAME, null, cv);
		}
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
