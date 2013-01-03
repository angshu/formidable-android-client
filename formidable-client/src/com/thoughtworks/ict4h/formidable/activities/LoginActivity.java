package com.thoughtworks.ict4h.formidable.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.thoughtworks.ict4h.formidable.R;

public class LoginActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.login);
    }

    public void login(View view){
        Intent intent = new Intent(this, FormsListingActivity.class);
        startActivity(intent);
    }
}
