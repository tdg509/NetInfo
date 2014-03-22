package com.calin.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestlocationActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	    
	    Button stopButton = (Button)findViewById(R.id.stop);
	    stopButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TestlocationActivity.this, TestService.class);
				TestlocationActivity.this.stopService(intent);
				TestlocationActivity.this.finish();
			}});
	    	    
	    Intent newintent = new Intent(this, TestService.class);
	    this.startService(newintent);
	}

	@Override
	protected void onStart(){
		super.onStart();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
	}

	@Override
	protected void onPause(){
		super.onPause();
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
	}
}