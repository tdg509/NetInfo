package com.example.netinfo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class FileViewer extends Activity {

	String fName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_viewer);
		fName = getIntent().getStringExtra("filename");//"/data/data/123.dat";
		TextView fv = (TextView) findViewById(R.id.FileView);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    try {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            sb.append("\n");
	            line = br.readLine();
	          
	        }
	        String everything = sb.toString();
	        fv.setText(everything);	        
	    } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_viewer, menu);
		return true;
	}

}
