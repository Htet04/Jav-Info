package com.jav.info;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.HashMap;

/*
Reminder: recheck code and rewrite it. 

Needed : 
	[✓]fetch data and store it in sharedpreferences
	[]Action for viewing cache data onClick
	[✓]Save Data
	[✓]Reload Data
	Others Fun... :)
*/

public class MainActivity extends AppCompatActivity {
	private Toolbar _toolbar;
	private TextView text;
	private GridView grid1;
	private AlertDialog viewd;
	private SwipeRefreshLayout _srl;
	private FloatingActionButton _fab;
	private boolean isSearch = false;
	private boolean opdelete = false;
	private int cp = 0;
	private int rp = 0;
	private HashMap<String, Object> map = new HashMap<>();
	private ArrayList<HashMap<String, Object>> _dsrc = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> cache;
	private RequestNetwork rq;
	private RequestNetwork.RequestListener rql;
	private SharedPreferences set;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initialLogic(savedInstanceState);
		initialLogic();
		/*
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
		}*/
	}

	private void initialLogic(Bundle sa) {
		//initialize functions
		registerReceiver(isNerworkAviable, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		grid1 = (GridView) findViewById(R.id.grid1);
		_fab = (FloatingActionButton) findViewById(R.id._fab);
		_srl = (SwipeRefreshLayout) findViewById(R.id._srl);
		_toolbar = findViewById(R.id._toolbar);
		set = getSharedPreferences("data", Activity.MODE_PRIVATE);
		setSupportActionBar(_toolbar);
		rq = new RequestNetwork(this);

		rql = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> headers) {
				try {
					set.edit().putString("data", response).commit();
					_dsrc = JsonToArray(response);
					grid1.setAdapter(new Gridview1Adapter(_dsrc));
				} catch (Exception e) {
					shm(e.getMessage());
				}
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
					_toolbar.getMenu().findItem(1).setVisible(true);
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
				rp = i;
				
				//for searching data cache
				if(isSearch){
				    int pio = 0;
				    for(int poi = 0;poi<_dsrc.size();poi++){
					    if(cache.get(rp).get("i").toString()==getId(pio)){
							rp = pio;
					    }
					    pio++;
					}
				}
				
				if (opdelete) {
					//deleting data
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
							try {
								_dsrc.remove(rp);
							} catch (Exception e) {
								shm(e.getMessage());
							}
							save();
							((BaseAdapter) grid1.getAdapter()).notifyDataSetChanged();
							d.dismiss();
						}
					});
					dd.show();
				} else {
					diabox(rp);
				}
			}
		});

		_srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				if (Fileo.isConnected(getApplicationContext())) {
					rq.startRequestNetwork(RequestNetworkController.GET, dataUrl(), "", rql);
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

	//here main logic whith few of code :)
	private void initialLogic() {
		try {
			if (set.getString("data", "").isEmpty()) {
				_srl.setRefreshing(true);
				rq.startRequestNetwork(RequestNetworkController.GET, dataUrl(), "", rql);
			} else {
				_dsrc = JsonToArray(set.getString("data", ""));
				grid1.setAdapter(new Gridview1Adapter(_dsrc));
			}
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
			txt.setTypeface(pds());
			try {
				txt.setText(_data.get(_position).get("i").toString());
				setImgUrl(img, _data.get(_position).get("tn").toString());
			} catch (Exception e) {
				shm("Grid Problem: " + e.getMessage());
			}
			return _view;
		}
	}

	//infomation dialog with add info function
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
		final EditText ed_tn = (EditText) cvforiinfo.findViewById(R.id.ed_tn);
		/*
		ed_i.setFocusableInTouchMode(true);
		ed_c.setFocusableInTouchMode(true);
		ed_d.setFocusableInTouchMode(true);
		ed_s.setFocusableInTouchMode(true);
		ed_t.setFocusableInTouchMode(true);
		ed_r.setFocusableInTouchMode(true);
		ed_tn.setFocusableInTouchMode(true);
		*/
		ed_i.setTypeface(pds());
		ed_c.setTypeface(pds());
		ed_d.setTypeface(pds());
		ed_s.setTypeface(pds());
		ed_t.setTypeface(pds());
		ed_r.setTypeface(pds());
		ed_tn.setTypeface(pds());
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
						map.put("tn", ed_tn.getText().toString());
						_dsrc.add(0, map);
					} else {
						// Edit data
						_dsrc.get(po).put("i", ed_i.getText().toString());
						_dsrc.get(po).put("c", ed_c.getText().toString());
						_dsrc.get(po).put("d", ed_d.getText().toString());
						_dsrc.get(po).put("s", ed_s.getText().toString());
						_dsrc.get(po).put("t", ed_t.getText().toString());
						_dsrc.get(po).put("r", ed_r.getText().toString());
						_dsrc.get(po).put("tn", ed_tn.getText().toString());
					}
					save();
					((BaseAdapter) grid1.getAdapter()).notifyDataSetChanged();

				} catch (Exception e) {
					shm("Add Info Error: " + e.getMessage());
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

		//view info
		try {
			if (po != -1) {
				ed_i.setText(getId(po));
				ed_c.setText(getCasts(po));
				ed_d.setText(getDirector(po));
				ed_s.setText(getStudio(po));
				ed_t.setText(getRuntime(po));
				ed_r.setText(getRelease(po));
				ed_tn.setText(getImgUrl(po));
			}
		} catch (Exception e) {
			shm("-Load info error-".concat(e.getMessage()));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//group id,item id,order ,title

		if (menu instanceof MenuBuilder) {
			MenuBuilder mb = (MenuBuilder) menu;
			mb.setOptionalIconsVisible(true);
		}

		MenuItem m0 = menu.add(0, 1, 1, "Search");
		final SearchView sv = new SearchView(MainActivity.this);
		sv.setPadding(8,8,8,8);
		sv.setFitsSystemWindows(true);
		m0.setActionView(sv);

		m0.setIcon(Cs(R.drawable.ic_search));
		m0.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		final MenuItem m_export = menu.add(1, 2, 2, "Export Data");
		m_export.setIcon(Cs(R.drawable.ic_export));

		final MenuItem m_delete = menu.add(1, 3, 3, "Delete");
		m_delete.setIcon(Cs(R.drawable.ic_delete));
		m_delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
			@Override
			public boolean onMenuItemClick(MenuItem mi){
				_toolbar.getMenu().findItem(1).setVisible(false);
				return false;
			}
		});
		
		final MenuItem m_tp = menu.add(1,4,4,"Terms & Privacy");
		m_tp.setIcon(Cs(R.drawable.ic_privacy));

		sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String query) {
				try {
					searchItem(query);
				} catch (Exception e) {
					shm(e.getMessage());

				}
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String text) {
				shm(text);
				return false;
			}
		});

		sv.setOnSearchClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isSearch = true;
				menu.setGroupEnabled(1, false);
				menu.setGroupVisible(1, false);
			}
		});
		sv.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				isSearch= false;
				menu.setGroupEnabled(1, true);
				menu.setGroupVisible(1, true);
				return false;
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (3): {
			if (opdelete) {
				opdelete = false;
				_toolbar.setTitle(getString(R.string.app_name));
				_fab.setRotation((float) 0);
				_toolbar.getMenu().findItem(1).setVisible(true);
			} else {
				opdelete = true;
				_toolbar.setTitle("Delete Items By Clicking");
				_fab.setRotation((float) 45);
			}
			break;
		}
		case (2): {
			copyClip(ToJson(_dsrc).replace("},{", "},\n{"));
			break;
		}
		case (1): {

			break;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	/*
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
	*/
	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(isNerworkAviable);
	}

	BroadcastReceiver isNerworkAviable = new BroadcastReceiver() {
		@Override
		public void onReceive(Context p1, Intent p2) {
			//hi
		}
	};

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

	private Typeface pds() {
		return Typeface.createFromAsset(getAssets(), "fonts/pdsmm_2.5.3_Regular.ttf");
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

	private String getImgUrl(int po) {
		return _dsrc.get(po).get("tn").toString();
	}
	
	private String dataUrl(){
		return getResources().getString(R.string.dataUrl);
	}

	private void setImgUrl(ImageView img, String url) {
		Glide.with(ctx()).load(Uri.parse(url)).into(img);
	}

	private void save() {
		set.edit().putString("data", ToJson(_dsrc)).commit();
	}

	private void copyClip(String str) {
		((ClipboardManager) getSystemService(getApplicationContext().CLIPBOARD_SERVICE))
				.setPrimaryClip(ClipData.newPlainText("clipboard", str));
	}

	private void searchItem(String str) {
		int n = 0;
		cache = new ArrayList<>();
		if (str == "") {
			grid1.setAdapter(new Gridview1Adapter(_dsrc));
		} else {
			for (int i = 0; i < _dsrc.size(); i++) {
				if (_dsrc.get(n).get("i").toString().contains(str.toUpperCase())) {
					cache.add(_dsrc.get(n));
				}
				n++;
			}
			grid1.setAdapter(new Gridview1Adapter(cache));
		}
	}

	private String ToJson(ArrayList<HashMap<String, Object>> ar) {
		return new String(new Gson().toJson(ar));
	}

	private ArrayList<HashMap<String, Object>> JsonToArray(String s) {
		return new Gson().fromJson(s, new TypeToken<ArrayList<HashMap<String, Object>>>() {
		}.getType());
	}

	private HashMap<String, Object> JsonToMap(String s) {
		return new Gson().fromJson(s, new TypeToken<HashMap<String, Object>>() {
		}.getType());
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