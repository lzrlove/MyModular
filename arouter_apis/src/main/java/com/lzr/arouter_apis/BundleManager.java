package com.lzr.arouter_apis;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

public class BundleManager {
    private Bundle bundle = new Bundle();

    public Bundle getBundle() {
        return this.bundle;
    }

    public BundleManager withString(String key, String value) {
        bundle.putString(key, value);
        return this;
    }
    public BundleManager withInt(String key,int value){
        bundle.putInt(key,value);
        return this;
    }
    public BundleManager withBoolean(String key,boolean value){
        bundle.putBoolean(key,value);
        return this;
    }

    public Object navgation(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return RouterManager.getInstance().navgation(context, this);
        }
        return null;
    }
}
