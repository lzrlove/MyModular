package com.lzr.personal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.lzr.arouter_annotation.ARouter;
import com.lzr.arouter_annotation.Parameter;
import com.lzr.arouter_apis.ParameterManager;

@ARouter(path = "/personal/Personal_MainActivity")
public class Personal_MainActivity extends AppCompatActivity {
    @Parameter
    String name;

    @Parameter
    int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal__main);
        ParameterManager.getInstance().loadParameter(this);
        Log.e("lzr:==", "personal/Personal_MainActivity name:" + name + ",age:==" + age);
    }
}
