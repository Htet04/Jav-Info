package com.jav.info;

import android.net.ConnectivityManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jav.info.Fileo;
import com.jav.info.Dow;

import android.os.Bundle;

import android.app.*;
import android.app.Activity;

import android.view.*;

import android.widget.*;

import android.net.Uri;
import android.graphics.drawable.*;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.content.*;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.*;
import android.content.SharedPreferences;

import java.lang.*;
import java.io.*;
import java.util.*;
import okhttp3.internal.connection.ConnectInterceptor;

public class MainActivity extends AppCompatActivity {
	private Toolbar _toolbar;
	private TextView text;
	private GridView grid1;
	private ProgressDialog pgd;
	private AlertDialog viewd;
	private SwipeRefreshLayout _srl;
	private FloatingActionButton _fab;
	private int pp = 0;
	private boolean opdelete = false;
	private ArrayList<String> idtd = new ArrayList<>();
	private HashMap<String, Object> map = new HashMap<>();

	private ArrayList<String> itd = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> _dsrc = new ArrayList<>();
	private String dataPath = "/storage/emulated/0/Adult/data/R_data.json";
	private RequestNetwork rq;
	private RequestNetwork.RequestListener rql;
	private SharedPreferences set;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialLogic(savedInstanceState);
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
					|| checkSelfPermission(
							Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
				requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
			} else {
				initialLogic();
			}
		} else {
			initialLogic();
		}
	}

	private void initialLogic(Bundle sa) {
		//initialize functions
		registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		registerReceiver(isNerworkAviable, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		grid1 = (GridView) findViewById(R.id.grid1);
		_fab = (FloatingActionButton) findViewById(R.id._fab);
		_srl = (SwipeRefreshLayout) findViewById(R.id._srl);
		_toolbar = findViewById(R.id._toolbar);
		set = getSharedPreferences("settings", Activity.MODE_PRIVATE);
		setSupportActionBar(_toolbar);
		rq = new RequestNetwork(this);

		if (Fileo.isExistFile(Fileo.getPackageDataDir(getApplicationContext()).concat("/data.json"))) {
			//shm("yes");
		} else {
			Fileo.writeFile(Fileo.getPackageDataDir(getApplicationContext()).concat("/data.json"), "test");
			//shm("no");
		}
		rql = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> headers) {
				shm(response);
				_srl.setRefreshing(false);
			}

			@Override
			public void onErrorResponse(String tag, String msg) {
				shm(msg);
				_srl.setRefreshing(false);
			}
		};

		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (opdelete) {
					_toolbar.setTitle(getString(R.string.app_name));
					opdelete = false;
					_fab.setRotation((float) 0);
				} else {
					diabox(-1);
				}
			}
		});

		grid1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int i, long l) {
				final int _position = i;

				if (2 == 2)
					return;

				if (opdelete) {
					AlertDialog dd = new AlertDialog.Builder(MainActivity.this, AlertDialog.THEME_DEVICE_DEFAULT_DARK)
							.create();
					dd.setTitle("Are you sure?");
					dd.setMessage("To delete ".concat(getId(i)));
					dd.setButton(AlertDialog.BUTTON_NEGATIVE, "NO", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface d, int di) {
							d.dismiss();
						}
					});
					dd.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface d, int di) {
							_dsrc.remove(i);
							saveData();
							((BaseAdapter) grid1.getAdapter()).notifyDataSetChanged();
							d.dismiss();
						}
					});
					dd.show();
				} else {
					if (!Fileo.isExistFile(picPath(_position))) {
						if (Fileo.isConnected(MainActivity.this)) {
							pgd = new ProgressDialog(MainActivity.this);
							pgd.setTitle("Downloading " + getId(_position));
							pgd.setIndeterminate(true);
							pgd.setCancelable(false);
							pgd.setCanceledOnTouchOutside(false);
							pgd.show();
							Dow.startDownload("https://img2.javmost.com/file_image/" + getId(_position) + ".jpg",
									getId(_position) + ".jpg", "Adult/img/", MainActivity.this);
							pp = i;
						} else {
							diabox(_position);
						}
					} else {
						diabox(_position);
					}
				}
			}
		});

		_srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (Fileo.isConnected(getApplicationContext())) {
					rq.startRequestNetwork(RequestNetworkController.GET, "https://pastebin.com/raw/6g9LswwF", "", rql);
				} else {
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							shm("No Network Connection!");
							_srl.setRefreshing(false);
						}
					}, 1000);
				}
			}
		});
	}

	private void initialLogic() {
		try {
			_dsrc = new Gson().fromJson(Fileo.readFile(dataPath), new TypeToken<ArrayList<HashMap<String, Object>>>() {
			}.getType());
			grid1.setAdapter(new Gridview1Adapter(_dsrc));
		} catch (Exception e) {
			shm(e.getMessage());
		}
	}

	// GridView bind customView
	public class Gridview1Adapter extends BaseAdapter {

		ArrayList<HashMap<String, Object>> _data;

		public Gridview1Adapter(ArrayList<HashMap<String, Object>> _arr) {
			_data = _arr;
		}

		@Override
		public int getCount() {
			return _data.size();
		}

		@Override
		public HashMap<String, Object> getItem(int _index) {
			return _data.get(_index);
		}

		@Override
		public long getItemId(int _index) {
			return _index;
		}

		@Override
		public View getView(final int _position, View _v, ViewGroup _container) {
			LayoutInflater _inflater = getLayoutInflater();
			View _view = _v;
			if (_view == null) {
				_view = _inflater.inflate(R.layout.cvforgird, null);
			}
			final TextView txt = (TextView) _view.findViewById(R.id.text1);
			final ImageView img = (ImageView) _view.findViewById(R.id.img);

			try {
				txt.setText(getId(_position));
				Fileo.setImgFromPath(img, picPath(_position));
			} catch (Exception e) {
				CEr("Grid Problem", e.getMessage());
			}
			return _view;
		}
	}

	public void diabox(final int po) {

		viewd = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).create();
		viewd.setTitle("Infomation Box");
		viewd.setCancelable(false);
		viewd.setCanceledOnTouchOutside(false);
		View cvforiinfo = getLayoutInflater().inflate(R.layout.cvforinfo, null);
		final EditText ed_i = (EditText) cvforiinfo.findViewById(R.id.ed_i);
		final EditText ed_c = (EditText) cvforiinfo.findViewById(R.id.ed_c);
		final EditText ed_d = (EditText) cvforiinfo.findViewById(R.id.ed_d);
		final EditText ed_s = (EditText) cvforiinfo.findViewById(R.id.ed_s);
		final EditText ed_t = (EditText) cvforiinfo.findViewById(R.id.ed_t);
		final EditText ed_r = (EditText) cvforiinfo.findViewById(R.id.ed_r);
		/*
		ed_i.setFocusableInTouchMode(true);
		ed_c.setFocusableInTouchMode(true);
		ed_d.setFocusableInTouchMode(true);
		ed_s.setFocusableInTouchMode(true);
		ed_t.setFocusableInTouchMode(true);
		ed_r.setFocusableInTouchMode(true);
		*/
		viewd.setView(cvforiinfo);
		viewd.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface di1, int pp1) {
				if ((ed_i.getText().toString().isEmpty() || !(ed_i.getText().toString().length() < 7))
						&& !(ed_i.getText().toString().contains("-")))
					return;
				try {
					if (po == -1) {
						// Add data
						map = new HashMap<>();
						map.put("i", ed_i.getText().toString());
						map.put("c", ed_c.getText().toString());
						map.put("d", ed_d.getText().toString());
						map.put("s", ed_s.getText().toString());
						map.put("t", ed_t.getText().toString());
						map.put("r", ed_r.getText().toString());
						_dsrc.add(_dsrc.size(), map);
					} else {
						// Edit data
						_dsrc.get(po).put("i", ed_i.getText().toString());
						_dsrc.get(po).put("c", ed_c.getText().toString());
						_dsrc.get(po).put("d", ed_d.getText().toString());
						_dsrc.get(po).put("s", ed_s.getText().toString());
						_dsrc.get(po).put("t", ed_t.getText().toString());
						_dsrc.get(po).put("r", ed_r.getText().toString());
					}
					saveData();
					((BaseAdapter) grid1.getAdapter()).notifyDataSetChanged();
					if (set.getString("isScroll", "") == "yes") {
						grid1.smoothScrollToPosition(_dsrc.size());
					}
				} catch (Exception e) {
					CEr("Add/Edit data Error", e.getMessage());
				}
				di1.dismiss();
			}
		});
		viewd.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface di1, int pp1) {
				di1.cancel();
			}
		});

		viewd.show();
		try {
			if (po != -1) {
				ed_i.setText(getId(po));
				ed_c.setText(getCasts(po));
				ed_d.setText(getDirector(po));
				ed_s.setText(getStudio(po));
				ed_t.setText(getRuntime(po));
				ed_r.setText(getRelease(po));
			}
		} catch (Exception e) {
			CEr("Dialog Load Data Error", e.getMessage());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//group id,item id,order ,title

		if (menu instanceof MenuBuilder) {
			MenuBuilder mb = (MenuBuilder) menu;
			mb.setOptionalIconsVisible(true);
		}

		MenuItem m0 = menu.add(0, 3, 1, "Search");
		final SearchView sv = new SearchView(MainActivity.this);
		m0.setActionView(sv);
		sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String query) {
				shm(query);
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String text) {
				shm(text);
				return false;
			}
		});
		m0.setIcon(Cs(R.drawable.ic_search));
		m0.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		MenuItem m1 = menu.add(0, 1, 2, "Re-Load");
		m1.setIcon(Cs(R.drawable.ic_reload));
	
		MenuItem m2 = menu.add(0, 2, 3, "Save Data");
		m2.setIcon(Cs(R.drawable.ic_content_save));
		
		MenuItem m3 = menu.add(0, 4, 4, "Delete");
		m3.setIcon(Cs(R.drawable.ic_delete));

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (4): {
			if (opdelete) {
				opdelete = false;
				_toolbar.setTitle(getString(R.string.app_name));
				_fab.setRotation((float) 0);
			} else {
				opdelete = true;
				_toolbar.setTitle("Delete Items By Clicking");
				_fab.setRotation((float) 45);
			}
			break;
		}
		case (3): {

			break;
		}
		case (2): {

			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
		case (1): {
			if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				initialLogic();
			} else {
				requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE,
						Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
			}
			break;
		}
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(onComplete);
	}

	BroadcastReceiver onComplete = new BroadcastReceiver() {

		@Override
		public void onReceive(Context p1, Intent p2) {
			if (Fileo.isExistFile(picPath(pp))) {
				shm("Download Complete");
			} else {
				shm("File not found");
			}
			pgd.dismiss();
		}

	};

	BroadcastReceiver isNerworkAviable = new BroadcastReceiver() {
		@Override
		public void onReceive(Context p1, Intent p2) {
			
		}
	};

	//Error Dialog
	public void CEr(String t, String m) {
		final AlertDialog ad = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_DARK).create();

		ad.setIcon(R.drawable.ic_plus);
		ad.setTitle(t);
		ad.setMessage(m);
		ad.setCancelable(false);
		ad.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int p) {
				ad.dismiss();
				finish();
			}
		});
		ad.show();
	}

	//Mini Functions
	private void creatNoti() {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			CharSequence name = "test";
			String description = "test";
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel channel = new NotificationChannel("test", name, importance);
			channel.setDescription(description);
			// Register the channel with the system; you can't change the importance
			// or other notification behaviors after this
			NotificationManager notificationManager = getSystemService(NotificationManager.class);
			notificationManager.createNotificationChannel(channel);
		}
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

		NotificationCompat.Builder b = new NotificationCompat.Builder(this, "test").setSmallIcon(R.drawable.ic_delete)
				.setContentTitle("test").setContentText("testttt").setPriority(NotificationCompat.PRIORITY_DEFAULT)
				.setAutoCancel(true).setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
				.setContentIntent(pendingIntent);

		NotificationManagerCompat nm = NotificationManagerCompat.from(this);
		nm.notify(1, b.build());
	}

	private Drawable Cs(int id) {
		Drawable ad = getDrawable(id);
		ad.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
		return ad;
	}

	private float getDip(int _in) {
		return Fileo.getDip(getApplicationContext(), _in);
	}

	private String getId(int po) {
		return _dsrc.get(po).get("i").toString();
	}

	private String getCasts(int po) {
		return _dsrc.get(po).get("c").toString();
	}

	private String getDirector(int po) {
		return _dsrc.get(po).get("d").toString();
	}

	private String getStudio(int po) {
		return _dsrc.get(po).get("s").toString();
	}

	private String getRuntime(int po) {
		return _dsrc.get(po).get("t").toString();
	}

	private String getRelease(int po) {
		return _dsrc.get(po).get("r").toString();
	}

	private String picPath(int i) {
		return "/storage/emulated/0/Adult/img/" + getId(i) + ".jpg";
	}

	private void saveData() {
		Fileo.writeFile("/storage/emulated/0/Adult/data/R_data.json", new Gson().toJson(_dsrc).replace("[{", "[\n{")
				.replace("}]", "}\n]").replace("\",\"", "\",\n\"").replace("},{", "},\n{").toString());
	}

	private Context ctx() {
		return getApplicationContext();
	}

	private void shm(Object s) {
		if (s == null)
			return;
		{
			Toast.makeText(ctx(), String.valueOf(s).toString(), Toast.LENGTH_SHORT).show();
		}
	}
}