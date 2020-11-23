package com.example.arouter_api;

import com.lzr.arouter_annotation.bean.RouterBean;

import java.util.Map;

public interface ARouterPath {


    //key :/app/MainActivity
    //value:RouterBean=MainActivity.class 封装后的
    Map<String, RouterBean> getPathMap();
}
