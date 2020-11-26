package com.lzr.modular;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzr.arouter_annotation.ARouter;
import com.lzr.arouter_annotation.Parameter;


@ARouter(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Parameter(name = "lzr")
    String name;

    @Parameter
    int age = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }
}