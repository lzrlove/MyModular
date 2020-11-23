package com.lzr.modular;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzr.arouter_annotation.ARouter;
import com.lzr.test.TestClass;

@ARouter(path = "MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String url = BuildConfig.SERVER_URL;


    }
}