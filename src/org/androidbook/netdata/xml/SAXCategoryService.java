package org.androidbook.netdata.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.androidbook.gallery.constants.NetConstants;
import org.androidbook.gallery.db.LocalSqlDb;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.net.Constant;
import org.androidbook.netdata.xml.data.WinImageDb;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;




public class SAXCategoryService implements ISAXService
{
	
	private  ArrayList<WinImageDb> winImageDBList;
	private int version = 0;
	private Handler handler;
	
	
	public void parse(InputStream is, Handler callback)
	{
		this.handler = callback;
		
		winImageDBList = new ArrayList<WinImageDb>();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		
		try
		{
			SAXParser parser = factory.newSAXParser();
			BookHandler BookHandler = new BookHandler();
			parser.parse(is, BookHandler );
			
		} catch (Exception e)
		{
			handler.sendEmptyMessage( R.id.PARSE_FAILED);
			e.printStackTrace();
		} 
	}
	
	private class BookHandler extends DefaultHandler
	{

		@Override
		public void endDocument() throws SAXException
		{
			super.endDocument();
			Message msg = new Message();
			msg.what  = R.id.PARSE_FINISH;
			msg.obj = winImageDBList;
			msg.arg1 = version;
			handler.sendMessage(msg);
			super.endDocument();
		}

		@Override
		public void startDocument() throws SAXException
		{
			// TODO Auto-generated method stub
			super.startDocument();
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
		{
			if(localName.equalsIgnoreCase(WinImageDb.CATEGORY) )
			{
				WinImageDb cat = new WinImageDb();
				cat.id = Integer.parseInt(attributes.getValue("", WinImageDb.CATEGORY_ID));
				cat.name = (attributes.getValue("", WinImageDb.CATEGORY_NAME));
				cat.discription = (attributes.getValue("", WinImageDb.CATEGORY_DESC));
				cat.count = Integer.parseInt(attributes.getValue("", WinImageDb.CATEGORY_COUNT));
				cat.category = (attributes.getValue("", WinImageDb.CATEGORY_DIR));
				cat.thumb_url = (attributes.getValue("", WinImageDb.CATEGORY_THUMB));
				String hotValue = attributes.getValue("", WinImageDb.CATEGORY_HOT) ;
				if( hotValue != null)
				{
					cat.hot = Integer.parseInt(attributes.getValue("", WinImageDb.CATEGORY_HOT));
				}
//				LocalSqlDb.getInstance().insertWinImageDb(cat);
				winImageDBList.add(cat);
//				Log.e("XML", "cat.name"+cat.name);
			}
			if(localName.equalsIgnoreCase(WinImageDb.CATEGORY_VERSION) )
			{
				version = Integer.parseInt(attributes.getValue("", WinImageDb.CATEGORY_VERSION_CODE));
			
			}
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException
		{
			// TODO Auto-generated method stub
			super.endElement(uri, localName, qName);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException
		{
			// TODO Auto-generated method stub
			super.characters(ch, start, length);
		}
		
	}
}