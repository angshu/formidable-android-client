package com.thoughtworks.ict4h.formidable.activities;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.thoughtworks.ict4h.formidable.FormidableActivity;
import com.thoughtworks.ict4h.formidable.R;
import com.thoughtworks.ict4h.formidable.service.StorageService;

public class FormsListingActivity extends ListActivity {    

	private ArrayList<String> folderList;
    
    private Intent storageSvcIntent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.storageSvcIntent = new Intent(this, StorageService.class);
        startService(storageSvcIntent);
        this.setContentView(R.layout.forms_list);
        getListView().setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onFormSelected(i);
            }
        });
        reloadList();
    }
    
    @Override
	protected void onDestroy() {
    	stopService(storageSvcIntent);
		super.onDestroy();
	}

    private void onFormSelected(int formIndex) {
        String formName = folderList.get(formIndex);
        Intent intent = new Intent(this, FormidableActivity.class);
        intent.putExtra("formName",formName);
        startActivity(intent);
    }


    @Override
    protected void onStart() {
        super.onStart();
        reloadList();
    }

    private void reloadList() {
        try {
            String forms_directory = getResources().getString(R.string.forms_directory);
            String[] formsList = getAssets().list(forms_directory);
            folderList = new ArrayList<String>();
            for (String item : formsList) {
                try{
                    InputStream is = getAssets().open(forms_directory + "/" + item);
                    is.close();
                }
                catch (IOException e){
                    folderList.add(item);
                }
            }
            setListAdapter(new ArrayAdapter<String>(this,  android.R.layout.simple_list_item_1, folderList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}