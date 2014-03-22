package com.example.netinfo;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileListActivity extends Activity {
	
	ListView listview;
	String path;
	String fileName[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_list);
		listview = (ListView) findViewById(R.id.filelist);
		path = getApplicationInfo().dataDir + "/files";		//path of the files		
	}

	@Override
	protected void onResume() {
		super.onResume();
		File file[] = new File(path).listFiles();							//get all the files into an array
		String fileName[] = new String[file.length];
		for (int i=0; i<file.length; i++) {									//convert file names into strings
			fileName[i] = file[i].toString().replace(path + "/", "");
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, fileName);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentAdapter, View v, int position, long id) {
				TextView clickedItem = (TextView) v;
				String selectedFile = path + "/" + clickedItem.getText();
				Intent i = new Intent(getApplicationContext(), FileViewer.class);
				i.putExtra("filename", selectedFile);
				startActivity(i);
			}			
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.file_list, menu);
		return true;
	}

}
