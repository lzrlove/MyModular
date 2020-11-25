package com.lzr.personal;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.lzr.arouter_annotation.ARouter;

@ARouter(path = "/personal/Personal_Main2Activity")
public class Personal_Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal__main);
    }
}
