package com.lzr.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzr.arouter_annotation.ARouter;

@ARouter(path = "/personal/Personal_MainActivity")
public class Personal_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal__main);
    }
}
