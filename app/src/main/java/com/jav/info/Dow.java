package com.jav.info;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.database.Cursor;
import android.app.*;
import android.widget.Toast;

public class Dow {
	
	private static long id = 0;
	private static DownloadManager dm = null;
	public static final int totalSize = 1;
	public static final int currentSize = 2;
	public static final int uri = 3;
	
	public static void startDownload(String url, String name, String path, Context c) {
		if(url.isEmpty()||name.isEmpty()||path.isEmpty())return;
		dm = (DownloadManager)c.getSystemService(Activity.DOWNLOAD_SERVICE);
		try {
			id = dm.enqueue(new DownloadManager.Request(Uri.parse(url)).
			setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI).
			setMimeType("image/jpg").
			setTitle(name).
			setDescription("Downloading " + name).
			setDestinationInExternalPublicDir(path, name));
			} catch (Exception e) {
			Toast.makeText(c.getApplicationContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
		}
	}
	
	public static class get {
		private static Cursor c = dm.query(new DownloadManager.Query().setFilterById(id));
		public static int TotalSize() {
			return new Integer(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
		}
		public static int CurrentSize() {
			return new Integer(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
		}
		public static String Url() {
			return new String(c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI)));
		}
	}
	
}