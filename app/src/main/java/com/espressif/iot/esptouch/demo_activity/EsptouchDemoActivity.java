package com.espressif.iot.esptouch.demo_activity;

import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.task.IEsptouchTask;
import com.espressif.iot_esptouch_demo.R;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

public class EsptouchDemoActivity extends Activity implements OnClickListener {
	
	private static final String TAG = "EsptouchDemoActivity";
	private TextView           txtSSID;
	private EditText           inputPassword;
	private Button             btnConfirm;
	private EspWifiAdminSimple WifiAdmin;
	private Spinner            spinnerTaskCount;
	private static final int PERMISSION_REQUEST_CODE = 200;
	private Context context;
	boolean permissionGranted = false;
	WifiManager wifiManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.esptouch_demo_activity);
		context = this;
		permissions(this);
		wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		WifiAdmin = new EspWifiAdminSimple(this);
		txtSSID =  findViewById(R.id.tvApSssidConnected);
		inputPassword = findViewById(R.id.edtApPassword);
		btnConfirm = findViewById(R.id.btnConfirm);
		btnConfirm.setOnClickListener(this);
		initSpinner();
		
		
	}
	
	
	
	private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
		
		new AlertDialog.Builder(EsptouchDemoActivity.this)
				.setMessage(message)
				.setPositiveButton("OK", okListener)
				.setNegativeButton("Cancel", null)
				.create()
				.show();
	}
	
	
	
	public boolean isPermissionGranted() {
		
		return !permissionGranted;
	}
	
	public void setPermissionGranted(boolean permissionGranted) {
		
		this.permissionGranted = permissionGranted;
	}
	

	
	private void initSpinner() {
		
		spinnerTaskCount =findViewById(R.id.spinnerTaskResultCount);
		int[]     spinnerItemsInt     = getResources().getIntArray(R.array.taskResultCount);
		int       length              = spinnerItemsInt.length;
		Integer[] spinnerItemsInteger = new Integer[length];
		for (int i = 0; i < length; i++) {
			spinnerItemsInteger[i] = spinnerItemsInt[i];
		}
		ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
		                                                          android.R.layout.simple_list_item_1, spinnerItemsInteger);
		spinnerTaskCount.setAdapter(adapter);
		spinnerTaskCount.setSelection(1);
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		// display the connected ap's ssid
		String apSsid = WifiAdmin.getWifiConnectedSsid();
		if ( apSsid != null ) {
			txtSSID.setText(apSsid);
		}
		else {
			txtSSID.setText("");
		}
		// check whether the wifi is connected
		boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
		btnConfirm.setEnabled(!isApSsidEmpty);
	}
	
	@Override
	public void onClick(View v) {
		if ( isPermissionGranted() ) {
			permissions(this);
		}
		if ( v == btnConfirm ) {
			String apSsid     = txtSSID.getText().toString();
			String apPassword = inputPassword.getText().toString();
			String apBssid    = WifiAdmin.getWifiConnectedBssid();
			String taskResultCountStr = Integer.toString(spinnerTaskCount
					                                             .getSelectedItemPosition());
			if ( IEsptouchTask.DEBUG ) {
				Log.d(TAG, "btnConfirm is clicked, mEdtApSsid = " + apSsid
						+ ", " + " inputPassword = " + apPassword);
			}
			new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword, taskResultCountStr);
		}
	}
	
	private class EsptouchAsyncTask2 extends AsyncTask<String, Void, IEsptouchResult> {
		
		private ProgressDialog mProgressDialog;
		
		private com.espressif.iot.esptouch.IEsptouchTask mEsptouchTask;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();
		
		@Override
		protected void onPreExecute() {
			
			mProgressDialog = new ProgressDialog(EsptouchDemoActivity.this);
			mProgressDialog
					.setMessage("Esptouch is configuring, please wait for a moment...");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					
					synchronized (mLock) {
						if ( IEsptouchTask.DEBUG ) {
							Log.i(TAG, "progress dialog is canceled");
						}
						if ( mEsptouchTask != null ) {
							mEsptouchTask.interrupt();
						}
					}
				}
			});
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
			                          "Waiting...", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						
						}
					});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(false);
		}
		
		@Override
		protected IEsptouchResult doInBackground(String... params) {
			
			synchronized (mLock) {
				String apSsid     = params[0];
				String apBssid    = params[1];
				String apPassword = params[2];
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, EsptouchDemoActivity.this);
			}
			IEsptouchResult result = mEsptouchTask.executeForResult();
			return result;
		}
		
		@Override
		protected void onPostExecute(IEsptouchResult result) {
			
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(true);
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
					"Confirm");
			// it is unnecessary at the moment, add here just to show how to use isCancelled()
			if ( !result.isCancelled() ) {
				if ( result.isSuc() ) {
					mProgressDialog.setMessage("Esptouch success, bssid = "
							                           + result.getBssid() + ",InetAddress = "
							                           + result.getInetAddress().getHostAddress());
				}
				else {
					mProgressDialog.setMessage("Esptouch fail");
				}
			}
		}
	}
	
	private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				String text = result.getBssid() + " is connected to the wifi";
				Toast.makeText(EsptouchDemoActivity.this, text,
				               Toast.LENGTH_LONG).show();
			}
			
		});
	}
	
	private IEsptouchListener myListener = new IEsptouchListener() {
		
		@Override
		public void onEsptouchResultAdded(final IEsptouchResult result) {
			
			onEsptoucResultAddedPerform(result);
		}
	};
	
	private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {
		
		private ProgressDialog mProgressDialog;
		
		private com.espressif.iot.esptouch.IEsptouchTask mEsptouchTask;
		// without the lock, if the user tap confirm and cancel quickly enough,
		// the bug will arise. the reason is follows:
		// 0. task is starting created, but not finished
		// 1. the task is cancel for the task hasn't been created, it do nothing
		// 2. task is created
		// 3. Oops, the task should be cancelled, but it is running
		private final Object mLock = new Object();
		
		@Override
		protected void onPreExecute() {
			
			mProgressDialog = new ProgressDialog(EsptouchDemoActivity.this);
			mProgressDialog
					.setMessage("Esptouch is configuring, please wait for a moment...");
			mProgressDialog.setCanceledOnTouchOutside(false);
			mProgressDialog.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					
					synchronized (mLock) {
						if ( IEsptouchTask.DEBUG ) {
							Log.i(TAG, "progress dialog is canceled");
						}
						if ( mEsptouchTask != null ) {
							mEsptouchTask.interrupt();
						}
					}
				}
			});
			mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE,
			                          "Waiting...", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						
						}
					});
			mProgressDialog.show();
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(false);
		}
		
		@Override
		protected List<IEsptouchResult> doInBackground(String... params) {
			
			int taskResultCount = -1;
			synchronized (mLock) {
				// !!!NOTICE
				String apSsid             = WifiAdmin.getWifiConnectedSsidAscii(params[0]);
				String apBssid            = params[1];
				String apPassword         = params[2];
				String taskResultCountStr = params[3];
				taskResultCount = Integer.parseInt(taskResultCountStr);
				mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, EsptouchDemoActivity.this);
				mEsptouchTask.setEsptouchListener(myListener);
			}
			List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
			return resultList;
		}
		
		@Override
		protected void onPostExecute(List<IEsptouchResult> result) {
			
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE)
					.setEnabled(true);
			mProgressDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText(
					"Confirm");
			IEsptouchResult firstResult = result.get(0);
			// check whether the task is cancelled and no results received
			if ( !firstResult.isCancelled() ) {
				int count = 0;
				// max results to be displayed, if it is more than maxDisplayCount,
				// just show the count of redundant ones
				final int maxDisplayCount = 5;
				// the task received some results including cancelled while
				// executing before receiving enough results
				if ( firstResult.isSuc() ) {
					StringBuilder sb = new StringBuilder();
					for (IEsptouchResult resultInList : result) {
						sb.append("Esptouch success, bssid = "
								          + resultInList.getBssid()
								          + ",InetAddress = "
								          + resultInList.getInetAddress()
								.getHostAddress() + "\n");
						count++;
						if ( count >= maxDisplayCount ) {
							break;
						}
					}
					if ( count < result.size() ) {
						sb.append("\nthere's " + (result.size() - count)
								          + " more result(s) without showing\n");
					}
					mProgressDialog.setMessage(sb.toString());
				}
				else {
					mProgressDialog.setMessage("Esptouch fail");
				}
			}
		}
	}
	public void permissions(Activity activity) {
		
		if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isPermissionGranted() ) {
			Dexter.withActivity(activity)
					.withPermissions(
							Manifest.permission.INTERNET,
							Manifest.permission.CHANGE_NETWORK_STATE,
							Manifest.permission.CHANGE_WIFI_STATE,
							Manifest.permission.ACCESS_NETWORK_STATE,
							Manifest.permission.ACCESS_WIFI_STATE,
							Manifest.permission.ACCESS_COARSE_LOCATION,
							Manifest.permission.ACCESS_FINE_LOCATION
					).withListener(new MultiplePermissionsListener() {
				@Override
				public void onPermissionsChecked(MultiplePermissionsReport report) {
					//Check if permission granted then set true
					if ( !report.isAnyPermissionPermanentlyDenied() ) {
						setPermissionGranted(true);
					}
					else {
						setPermissionGranted(false);
					}
				}
				
				@Override
				public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
					
					if ( isPermissionGranted() ) {
						token.continuePermissionRequest();
					}
				}
				
				
			}).check();
		}
	}
}
