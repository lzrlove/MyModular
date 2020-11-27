package com.lzr.arouter_apis;

import android.app.Activity;
import android.util.LruCache;

public class ParameterManager {

    private static ParameterManager instance;

    public static ParameterManager getInstance() {
        if (instance == null) {
            synchronized (ParameterManager.class) {
                if (instance == null) {
                    instance = new ParameterManager();
                }
            }
        }
        return instance;
    }


    private LruCache<String, ParameterGet> lruCache;

    private ParameterManager() {
        lruCache = new LruCache<>(100);
    }

    static final String FILE_SUFFIX_NAME = "$$Parameter"; // 为了这个效果：Order_MainActivity + $$Parameter

    // 使用者 只需要调用这一个方法，就可以进行参数的接收
    public void loadParameter(Activity activity){
        String className = activity.getClass().getName(); //Personal_MainActivity

        ParameterGet parameterGet = lruCache.get(className);
        if (parameterGet == null){
            // 类加载Personal_MainActivity + $$Parameter
            try {
                Class<?> aClass = Class.forName(className + FILE_SUFFIX_NAME);
                parameterGet = (ParameterGet) aClass.newInstance();
                lruCache.put(className,parameterGet);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        parameterGet.getParameter(activity);
    }
}
