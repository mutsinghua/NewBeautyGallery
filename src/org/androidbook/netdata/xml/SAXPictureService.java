package org.androidbook.netdata.xml;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.androidbook.gallery.db.LocalSqlDb;
import org.androidbook.gallery.newbeauty.R;
import org.androidbook.netdata.xml.data.Picture;
import org.androidbook.netdata.xml.data.WinImageDb;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Handler;
import android.os.Message;

public class SAXPictureService implements ISAXService
{

	private ArrayList<Picture> pictureList;
	private int cid = 0;
	private Handler handler;

	public void parse(InputStream is, Handler callback)
	{
		this.handler = callback;

		pictureList = new ArrayList<Picture>();

		SAXParserFactory factory = SAXParserFactory.newInstance();

		try
		{
			SAXParser parser = factory.newSAXParser();
			BookHandler BookHandler = new BookHandler();
			parser.parse(is, BookHandler);

		} catch (Exception e)
		{
			handler.sendEmptyMessage(R.id.PARSE_FAILED);
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
			msg.what = R.id.PARSE_FINISH;
			msg.obj = pictureList;
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
			if (localName.equalsIgnoreCase(Picture.PICTURE))
			{
				Picture cat = new Picture();
				cat.id = Integer.parseInt(attributes.getValue("", Picture.PICTURE_ID));
				
				if (attributes.getValue("", Picture.CATEGORY_ID) != null)
				{
					cat.cate_id = Integer.parseInt(attributes.getValue("", Picture.CATEGORY_ID));
				} else
				{
					cat.cate_id = cid;
				}
				if (attributes.getValue("", Picture.PICTURE_HOT) != null)
				{
					cat.hot = Integer.parseInt(attributes.getValue("", Picture.PICTURE_HOT));
				} 

				if (attributes.getValue("", Picture.PICTURE_DATE) != null)
				{
					cat.date = attributes.getValue("", Picture.PICTURE_DATE);
				} 
				cat.url = (attributes.getValue("", Picture.PICTURE_URL));

				pictureList.add(cat);
			}
			if (localName.equalsIgnoreCase(Picture.PICTURE_PARENT))
			{
				if (attributes.getValue("", Picture.CATEGORY_ID) != null)
				{
					cid = Integer.parseInt(attributes.getValue("", Picture.CATEGORY_ID));
				} 

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