package com.lzr.order;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.lzr.arouter_annotation.ARouter;
import com.lzr.arouter_annotation.Parameter;
import com.lzr.arouter_apis.ParameterManager;
import com.lzr.arouter_apis.RouterManager;

@ARouter(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Parameter
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order__main);
        ParameterManager.getInstance().loadParameter(this);
        Log.e("lzr:==", "personal/Personal_MainActivity name:" + name);
    }

    public void jump(View view) {
        RouterManager.getInstance()
                .build("/personal/Personal_MainActivity")
                .withString("name","personal")
                .withInt("age",99)
                .navgation(this);

    }
}
