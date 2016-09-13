package com.wolkabout.hexiwear;

import com.unity3d.player.*;
import com.wolkabout.hexiwear.model.Characteristic;
import com.wolkabout.hexiwear.model.HexiwearDevice;
import com.wolkabout.hexiwear.model.Mode;
import com.wolkabout.hexiwear.service.BluetoothService;
import com.wolkabout.hexiwear.service.BluetoothService_;
import com.wolkabout.hexiwear.util.HexiwearDevices;
import com.wolkabout.hexiwear.util.HexiwearDevices_;


import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;


public class UnityPlayerActivity extends Activity implements ServiceConnection
{
	protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code

	private BluetoothDevice device;
	private HexiwearDevice hexiwearDevice;
	private HexiwearDevices hexiwearDevices;
	private BluetoothService bluetoothService;
	private boolean isBound;
	private Mode mode = Mode.IDLE;
	private static final String TAG = UnityPlayerActivity.class.getSimpleName();
	private String readingBattery;
	public static String readingTemperature = "StandBy";
	private String readingHumidity;
	private String readingPressure;
	private String readingHeartRate;
	private String readingLight;
	private String readingSteps;
	private String readingCalories;
	private String[] accelerationReadings;
	private String[] magnetReadings;
	private String[] gyroscopeReadings;

	// Setup activity layout
	@Override protected void onCreate (Bundle savedInstanceState)
	{
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		init_(savedInstanceState);

		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

		mUnityPlayer = new UnityPlayer(this);
		setContentView(mUnityPlayer);
		mUnityPlayer.requestFocus();
	}

	// Quit Unity
	@Override protected void onDestroy ()
	{
		mUnityPlayer.quit();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(onModeChangedReceiver_);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onBondRequestedReceiver_);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onConnectionStateChangedReceiver_);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(onDataAvailableReceiver_);
		this.unregisterReceiver(onStopReadingReceiver_);

		if (isBound) {
			unbindService(this);
			isBound = false;
		}
		super.onDestroy();
	}

	// Pause Unity
	@Override protected void onPause()
	{
		super.onPause();
		mUnityPlayer.pause();
	}

	// Resume Unity
	@Override protected void onResume()
	{
		super.onResume();
		mUnityPlayer.resume();
	}

	// This ensures the layout will be correct.
	@Override public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		mUnityPlayer.configurationChanged(newConfig);
	}

	// Notify Unity of the focus change.
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		mUnityPlayer.windowFocusChanged(hasFocus);
	}

	// For some reason the multiple keyevent type is not supported by the ndk.
	// Force event injection by overriding dispatchKeyEvent().
	@Override public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
			return mUnityPlayer.injectEvent(event);
		return super.dispatchKeyEvent(event);
	}

	// Pass any events not handled by (unfocused) views straight to UnityPlayer
	public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
	public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
	public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
	/*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }

	private void injectExtras_() {
		Bundle extras_ = getIntent().getExtras();
		if (extras_!= null) {
			if (extras_.containsKey(DEVICE_EXTRA)) {
				this.device = extras_.getParcelable(DEVICE_EXTRA);
			}
		}
	}

	@Override
	public void setIntent(Intent newIntent) {
		super.setIntent(newIntent);
		injectExtras_();
	}

	public final static String DEVICE_EXTRA = "device";
	private final IntentFilter intentFilter1_ = new IntentFilter();
	private final BroadcastReceiver onModeChangedReceiver_ = new BroadcastReceiver() {
		public final static String MODE_EXTRA = "mode";

		public void onReceive(Context context, Intent intent) {
			Bundle extras_ = ((intent.getExtras()!= null)?intent.getExtras():new Bundle());
			Mode mode = ((Mode) extras_.getSerializable(MODE_EXTRA));
			UnityPlayerActivity.this.onModeChanged(mode);
		}
	}
			;
	private final IntentFilter intentFilter2_ = new IntentFilter();
	private final BroadcastReceiver onBondRequestedReceiver_ = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			Log.d(TAG, "Bonding device. Please wait…<");
		}
	}
			;
	private final IntentFilter intentFilter3_ = new IntentFilter();
	private final BroadcastReceiver onConnectionStateChangedReceiver_ = new BroadcastReceiver() {
		public final static String CONNECTION_STATE_EXTRA = "connectionState";

		public void onReceive(Context context, Intent intent) {
			Bundle extras_ = ((intent.getExtras()!= null)?intent.getExtras():new Bundle());
			boolean connectionState = extras_.getBoolean(CONNECTION_STATE_EXTRA);
			if(connectionState){
				Log.d(TAG, "Connected");
			}else {
				Log.d(TAG, "Re-Connected");
			}
		}
	}
			;
	private final IntentFilter intentFilter4_ = new IntentFilter();
	private final BroadcastReceiver onDataAvailableReceiver_ = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			UnityPlayerActivity.this.onDataAvailable(intent);
		}
	}
			;
	private final IntentFilter intentFilter5_ = new IntentFilter();
	private final BroadcastReceiver onStopReadingReceiver_ = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			UnityPlayerActivity.this.onStopReading();
		}
	};
	void onModeChanged(Mode mode) {
		this.mode = mode;
		Log.d(TAG, mode.name());

	}
	private void init_(Bundle savedInstanceState) {
		this.hexiwearDevices = HexiwearDevices_.getInstance_(this);
		injectExtras_();
		intentFilter1_.addAction("modeChanged");
		intentFilter2_.addAction("noBond");
		intentFilter3_.addAction("ConnectionStateChange");
		intentFilter4_.addAction("dataAvailable");
		intentFilter5_.addAction("stop");
		startService();
		LocalBroadcastManager.getInstance(this).registerReceiver(onModeChangedReceiver_, intentFilter1_);
		LocalBroadcastManager.getInstance(this).registerReceiver(onBondRequestedReceiver_, intentFilter2_);
		LocalBroadcastManager.getInstance(this).registerReceiver(onConnectionStateChangedReceiver_, intentFilter3_);
		LocalBroadcastManager.getInstance(this).registerReceiver(onDataAvailableReceiver_, intentFilter4_);
		this.registerReceiver(onStopReadingReceiver_, intentFilter5_);
	}

	void startService() {
		hexiwearDevice = hexiwearDevices.getDevice(device.getAddress());
		if (hexiwearDevices.shouldKeepAlive(hexiwearDevice)) {
			BluetoothService_.intent(this).start();
		}
		isBound = bindService(BluetoothService_.intent(this).get(), this, BIND_AUTO_CREATE);
	}

	void onDataAvailable(Intent intent) {

		final String uuid = intent.getStringExtra(BluetoothService.READING_TYPE);
		final String data = intent.getStringExtra(BluetoothService.STRING_DATA);

		if (data.isEmpty()) {
			return;
		}

		final Characteristic characteristic = Characteristic.byUuid(uuid);
		if (characteristic == null) {
			Log.w(TAG, "UUID " + uuid + " is unknown. Skipping.");
			return;
		}

		Log.d(TAG,data);

		switch (characteristic) {
			case BATTERY:
				readingBattery = data;
				break;
			case TEMPERATURE:
				readingTemperature = data;
				break;
			case HUMIDITY:
				readingHumidity = data;
				break;
			case PRESSURE:
				readingPressure = data;
				break;
			case HEARTRATE:
				readingHeartRate = data;
				break;
			case LIGHT:
				readingLight = data;
				break;
			case STEPS:
				readingSteps = data;
				break;
			case CALORIES:
				readingCalories = data;
				break;
			case ACCELERATION:
				accelerationReadings = data.split(";");
				break;
			case MAGNET:
				magnetReadings = data.split(";");
				break;
			case GYRO:
				gyroscopeReadings = data.split(";");
				break;
			default:
				break;
		}
	}

	void onStopReading() {
		Log.i(TAG, "Stop command received. Finishing...");
		finish();
	}
	@Override
	public void onServiceConnected(final ComponentName name, final IBinder service) {
		final BluetoothService.ServiceBinder binder = (BluetoothService.ServiceBinder) service;
		bluetoothService = binder.getService();
		if (!bluetoothService.isConnected()) {
			bluetoothService.startReading(device);
		}
		final Mode mode = bluetoothService.getCurrentMode();
		if (mode != null) {
			onModeChanged(mode);
		}
	}

	@Override
	public void onServiceDisconnected(final ComponentName name) {
		// Something terrible happened.
	}
}
