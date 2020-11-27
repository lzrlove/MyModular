package com.lzr.modular;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lzr.arouter_annotation.ARouter;
import com.lzr.arouter_annotation.Parameter;
import com.lzr.arouter_apis.RouterManager;


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

    public void jump(View view) {
        Log.d("lzr:===","====");
        RouterManager.getInstance()
                .build("/order/Order_MainActivity")
                .withString("name","订单模块")
                .navgation(this);
    }
}