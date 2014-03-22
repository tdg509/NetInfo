package com.calin.test;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

public class TestService extends Service {

	private static final int MAIN_SERVICE_NOTIFICATION_ID = 1;

	private boolean listeningToNetwork = false;
	private boolean listeningToGps = false;
	private TestLocationListener networkListener = new TestLocationListener();
	private TestLocationListener gpsListener = new TestLocationListener();
	private LocationManager locman = null;
	private Notification serviceRunningNotification = null;
//	private PowerManager pm;
//	private PowerManager.WakeLock wl;
	
	private class TestLocationListener implements LocationListener{
		@Override
		public void onLocationChanged(Location location) {			
			long time = location.getTime();
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			float accuracy = location.getAccuracy();
			float speed = location.getSpeed();
			String provider = location.getProvider();
			
			String message = provider + " location update; time (" + time + "), lat (" + 
					lat + "), lng (" + lng + "), precision (" + accuracy + "), speed (" + speed + ")";
			Log.i("TestLocation", message);
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.i("TestLocation", "Provider disabled: " + provider);
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.i("TestLocation", "Provider enabled: " + provider);
		}

		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {					
			switch(status){
				case LocationProvider.OUT_OF_SERVICE:
					Log.i("TestLocation", "Provider status change: OUT OF SERVICE (" + provider + ")" );
					break;
					
				case LocationProvider.AVAILABLE:
					Log.i("TestLocation", "Provider status change: available (" + provider + ")");
					break;
					
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
					Log.i("TestLocation", "Provider status change: TEMPORARILY_UNAVAILABLE (" + provider + ")");
					break;
					
				default:
					Log.i("TestLocation", "Provider status change: " + status +  "(" + provider + ")");
					break;
			}
		}		
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		
//		pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "My Tag");
//		wl.acquire();
//		
		locman = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
        serviceRunningNotification = createServiceActiveNotification();
		
        startForeground(MAIN_SERVICE_NOTIFICATION_ID, serviceRunningNotification);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i("TestLocation", "service.onStartCommand with intent " + intent + "; flags= " + flags);
		startNetworkLocationUpdates();
		
		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		
		stopNetworkLocationUpdates();
		
		//wl.release();
	}
	
	private void startNetworkLocationUpdates()
	{
		if (!listeningToNetwork){
			locman.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, networkListener);
			listeningToNetwork = true;
		}
	}
	
	private void startGpsLocationUpdates()
	{
		if (!listeningToGps){
			locman.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, gpsListener);
			listeningToGps = true;
		}
	}
	
	private void stopNetworkLocationUpdates()
	{
		if (listeningToNetwork){
			locman.removeUpdates(networkListener);
			listeningToNetwork = false;
		}		
	}
	
	private void stopGpsLocationUpdates()
	{
		if (listeningToGps){
			locman.removeUpdates(gpsListener);
			listeningToGps = false;
		}		
	}
	
	   private Notification createServiceActiveNotification()
	   {
	      CharSequence text = "Service started";
	      CharSequence title = "network location testing";

	      Notification notification = new Notification(R.drawable.ic_stat_example, text,
	                                                   System.currentTimeMillis());

	      notification.flags = Notification.FLAG_NO_CLEAR;

	      Intent intent = new Intent(this, TestlocationActivity.class);

	      PendingIntent launchIntent = PendingIntent.getActivity(getApplicationContext(), 0,
	                                                             intent, PendingIntent.FLAG_CANCEL_CURRENT);

	      notification.setLatestEventInfo(this, title, text, launchIntent);

	      return notification;
	   }
}
