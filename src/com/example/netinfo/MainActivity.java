package com.example.netinfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	private TextView tvNetworkName, tvNetworkCodeData, tvNetworkTypeData, tvLACData, tvCIDData, tvSignalStrengthData;
	private PhoneStateListener MyListener;
	private TelephonyManager tm;
	private String fileName;						//name of the file to be written by the service
	private File dirName;							//the /files directory of the app

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        tvNetworkName = (TextView) findViewById(R.id.network_name_data);
        tvNetworkCodeData = (TextView) findViewById(R.id.network_code_data);
        tvNetworkTypeData = (TextView) findViewById(R.id.network_type_data);
        tvLACData = (TextView) findViewById(R.id.lac_data);
        tvCIDData = (TextView) findViewById(R.id.cid_data);
        tvSignalStrengthData = (TextView) findViewById(R.id.signal_strength_data);
        dirName = new File(getApplicationInfo().dataDir + "/files");
        MyListener = new PhoneStateListener() {
            @Override
            public void onCellLocationChanged(CellLocation location){
            	super.onCellLocationChanged(location);
            	if ((tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA) || (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS)) {
            		GsmCellLocation MyLocation = (GsmCellLocation)tm.getCellLocation();
            		tvLACData.setText(Integer.toHexString(MyLocation.getLac()));
                	tvCIDData.setText(Integer.toHexString(MyLocation.getCid()));
            	}
            	if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA) {
            		CdmaCellLocation MyLocation = (CdmaCellLocation)tm.getCellLocation();
            		tvLACData.setText("--");
                	tvCIDData.setText(Integer.toHexString(MyLocation.getBaseStationId()));
            	}            	
            }            
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            	super.onSignalStrengthsChanged(signalStrength);
            	tvSignalStrengthData.setText(String.valueOf(2 * signalStrength.getGsmSignalStrength() - 113 + "dBm"));
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
    	int numOfFiles = 0;
    	MenuItem mi = menu.findItem(R.id.files);
    	if (dirName.exists()) {								//check if there are any files of the app and enable or disable the "files" menu item
    		File file[] = new File(getApplicationInfo().dataDir + "/files").listFiles();
    		numOfFiles = file.length;
    	}
    	if (numOfFiles ==0) {
    		mi.setEnabled(false);
    	} else {
    		mi.setEnabled(true);
    	}
    	mi = menu.findItem(R.id.record);				//toggle menu item between start record and stop record
    	if (isRecording()) {
    		mi.setTitle(R.string.stop);
    	} else {
    		mi.setTitle(R.string.start);
    	}    	
    	super.onPrepareOptionsMenu(menu);
    	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
        switch (item.getItemId()) {
        case R.id.files:			//show files
        	i = new Intent(this, FileListActivity.class);startActivity(i);
        	return true;
        case R.id.record:			//record
        	i = new Intent(this, WriteToFileService.class);		//create intent for the recording service
        	if (!isRecording()) {//  !serviceRunning) {
        		fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime()) + ".dat";	//file name to record into
        		i.putExtra("name", fileName);
        		startService(i);            	
        	} else {
        		getBaseContext().stopService(i);
        	}          
          return true;           
        default:
          return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onResume()
    {
       super.onResume();
       //start listening and update the views in the display
       tm.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS | PhoneStateListener.LISTEN_CELL_LOCATION | PhoneStateListener.LISTEN_CALL_STATE);
       tvNetworkName.setText(tm.getNetworkOperatorName() + " " + tm.getNetworkCountryIso().toUpperCase());
       tvNetworkCodeData .setText(tm.getNetworkOperator());
       switch (tm.getNetworkType()) {
       case (TelephonyManager.NETWORK_TYPE_HSDPA):
    	   tvNetworkTypeData.setText("HSPDA");
       break;
       case (TelephonyManager.NETWORK_TYPE_GPRS):
    	   tvNetworkTypeData.setText("GPRS");
       break;
       case (TelephonyManager.NETWORK_TYPE_UMTS):
    	   tvNetworkTypeData.setText("UMTS");
       break;
       case (TelephonyManager.NETWORK_TYPE_LTE):
    	   tvNetworkTypeData.setText("LTE");
       break;
       case (TelephonyManager.NETWORK_TYPE_UNKNOWN):
    	   tvNetworkTypeData.setText("UNKNOWN");
       break;
       default:
    	   tvNetworkTypeData.setText("OTHER");
       }       
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
    	  super.onSaveInstanceState(savedInstanceState);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);
    }
    
    @Override
    protected void onPause()
    {
       super.onPause();
       tm.listen(MyListener, PhoneStateListener.LISTEN_NONE);		//cancel the telephonyManager listener
    }
    
    private boolean isRecording() {									//check if the recording service is running or not
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (WriteToFileService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}