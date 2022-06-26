package com.jav.info;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import android.graphics.Color;
import android.content.*;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.*;

import java.lang.*;
import java.io.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {

	private TextView text;
	private GridView grid1;
	private ProgressDialog pgd;
	private AlertDialog viewd;
	private FloatingActionButton _fab;
	private boolean isScroll = false;
	private boolean opdelete = false;
	private HashMap<String, Object> map = new HashMap<>();
	private ArrayList<String> itd = new ArrayList<>();
	private ArrayList<HashMap<String, Object>> _dsrc = new ArrayList<>();
	private String dataPath = "/storage/emulated/0/Adult/data/R_data.json";
	private RequestNetwork rq;
	private RequestNetwork.RequestListener rql;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

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

		registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		grid1 = (GridView) findViewById(R.id.grid1);
		_fab = (FloatingActionButton) findViewById(R.id._fab);

		//initialize functions
		rq = new RequestNetwork(this);
		rql = new RequestNetwork.RequestListener() {
			@Override
			public void onResponse(String tag, String response, HashMap<String, Object> headers) {
				shm(response);
			}

			@Override
			public void onErrorResponse(String tag, String msg) {
				shm(msg);
			}
		};

		_fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				diabox(-1);
			}
		});
		grid1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> av, View v, int i, long l) {
				final int _position = i;
				if (opdelete) {
					itd.add(getId(_position));
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
							final java.util.Timer _timer = new java.util.Timer();
							final java.util.TimerTask t_m = new java.util.TimerTask() {
								@Override
								public void run() {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											if (Fileo.isConnected(MainActivity.this)) {
												if (Fileo.isExistFile(
														"/storage/emulated/0/Adult/img/" + getId(_position) + ".jpg")) {
													pgd.dismiss();
													((BaseAdapter) grid1.getAdapter()).notifyDataSetChanged();
													_timer.cancel();
												}
											} else {
												pgd.dismiss();
												_timer.cancel();
												shm("Network disconnect");
											}
										}
									});
								}
							};

							_timer.scheduleAtFixedRate(t_m, 0, 500);
						} else {
							diabox(_position);
						}
					} else {
						diabox(_position);
					}
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
					if (isScroll) {
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

		viewd.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface p1) {
				//shm("show");
			}
		});
		viewd.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface p1) {
				//shm("dismiss");
			}
		});
		viewd.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface p1) {
				//shm("cancel");
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
		menu.add(0, 1, 1, "Scroll after add").setCheckable(true).setChecked(isScroll);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case (1): {
			if (isScroll) {
				isScroll = false;
			} else {
				isScroll = true;
			}
			item.setChecked(isScroll);
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
		shm(grantResults[0]);
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
			shm("Download Complete");
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