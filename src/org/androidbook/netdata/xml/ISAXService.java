package org.androidbook.netdata.xml;

import java.io.InputStream;

import android.os.Handler;

public interface ISAXService
{
	public void parse(InputStream is, Handler callback);
}