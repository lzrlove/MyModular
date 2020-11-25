package com.lzr.arouter_apis;

import java.util.Map;

public interface ARouterGroup {


    //Key: app order personal
    //value: app组下面的所有的path==class
    Map<String,Class<? extends ARouterPath>> getGroupMap();
}
