package com.example.netinfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

public class WriteToFileService extends Service {
	
	private PhoneStateListener MyServiceListener;
	private TelephonyManager tm;
	private LocationListener MyLocationListener;
	private LocationManager lm;
	private int lac, cellId, signal;
	private String phoneState;
	private File outFile;
	private final static int myID = 10;
	
	@Override
	public int onStartCommand(Intent senderIntent, int flags, int startId) {
		Intent localIntent = new Intent(this, MainActivity.class);
		localIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendIntent = PendingIntent.getActivity(this, 0, localIntent, 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
	    builder.setTicker("Starting to record data").setContentTitle("NetInfo").setContentText("Recording data").setWhen(System.currentTimeMillis())
	    		.setAutoCancel(false).setOngoing(true).setContentIntent(pendIntent).setSmallIcon(R.drawable.hexagon);
	    Notification notification = builder.build();
	    notification.flags |= Notification.FLAG_NO_CLEAR;		
		startForeground(myID, notification);
				
		MyLocationListener = new LocationListener() {			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub				
			}			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub				
			}			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub				
			}			
			@Override
			public void onLocationChanged(Location location) {
				// TODO Auto-generated method stub				
			}
		};
		lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, MyLocationListener);
		
		outFile = new File(getApplicationContext().getFilesDir(), senderIntent.getStringExtra("name"));
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		MyServiceListener = new PhoneStateListener() {
            @Override
            public void onCellLocationChanged(CellLocation location){
            	super.onCellLocationChanged(location);
            	if ((tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) || (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS)) {
            		GsmCellLocation MyLocation = (GsmCellLocation)tm.getCellLocation();
            		lac = MyLocation.getLac();
            		cellId = MyLocation.getCid();
            	}
            	if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA) {
            		CdmaCellLocation MyLocation = (CdmaCellLocation)tm.getCellLocation();
            		lac = 0;
            		cellId = MyLocation.getBaseStationId();
            	}
            	WriteDataToFile(lac, cellId, signal, phoneState);
            }
            
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            	super.onSignalStrengthsChanged(signalStrength);
            	signal = 2 * signalStrength.getGsmSignalStrength() - 113;	
            	WriteDataToFile(lac, cellId, signal, phoneState);
            }
            
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
            	super.onCallStateChanged(state, incomingNumber);
            	switch (state) {
            	case TelephonyManager.CALL_STATE_IDLE:
            		phoneState = "Idle";
            		break;
            	case TelephonyManager.CALL_STATE_RINGING:
            		phoneState = "Ringing";
            		break;
            	case TelephonyManager.CALL_STATE_OFFHOOK:
            		phoneState = "On-going call";
            		break;
            	default:
            		phoneState = "";
            		break;
            	}
            	WriteDataToFile(lac, cellId, signal, phoneState);
            }
		};
		tm.listen(MyServiceListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CALL_STATE);
	    return Service.START_STICKY;
	  }
	
	private void WriteDataToFile(int lac, int cellId, int signal, String phoneState) {
		try {
			FileWriter fw = new FileWriter(outFile.getAbsolutePath(), true);
			BufferedWriter bw = new BufferedWriter(fw);			
			bw.append(new SimpleDateFormat("ddMMyyyy_HHmmss").format(Calendar.getInstance().getTime()) + " ");
			bw.append(String.valueOf(lac) + " ");
			bw.append(String.valueOf(cellId) + " ");
			bw.append(String.valueOf(signal) + " ");
			bw.append(phoneState + "\n");			
			bw.close();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(getApplicationContext(), "stopped recording", Toast.LENGTH_LONG).show();
		tm.listen(MyServiceListener, PhoneStateListener.LISTEN_NONE);
		//wl.release();
		stopForeground(true);
	}
}
