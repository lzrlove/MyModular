package com.lzr.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.lzr.arouter_annotation.ARouter;
import com.lzr.arouter_annotation.Parameter;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Parameter(name = "order-lzr")
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order__main);

    }
}
