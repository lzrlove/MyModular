package com.lzr.modular;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.lzr.arouter_annotation.ARouter;

@ARouter(path = "/app/Main3Activity")
public class Main3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = BuildConfig.SERVER_URL;


    }
}