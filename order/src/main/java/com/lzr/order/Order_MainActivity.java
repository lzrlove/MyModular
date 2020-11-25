package com.lzr.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzr.arouter_annotation.ARouter;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order__main);

    }
}
