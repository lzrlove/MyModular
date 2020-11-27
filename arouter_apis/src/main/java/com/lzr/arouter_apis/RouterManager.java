package com.lzr.arouter_apis;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import android.util.LruCache;

import androidx.annotation.RequiresApi;

import com.lzr.arouter_annotation.bean.RouterBean;

public class RouterManager {
    private String group;
    private String path;

    private static RouterManager instance;

    public static RouterManager getInstance() {
        if (instance == null) {
            synchronized (RouterManager.class) {
                if (instance == null) {
                    instance = new RouterManager();
                }
            }
        }
        return instance;
    }

    private LruCache<String, ARouterGroup> groupLruCache;
    private LruCache<String, ARouterPath> pathLruCache;

    private RouterManager() {
        groupLruCache = new LruCache<>(100);
        pathLruCache = new LruCache<>(100);
    }

    private final static String FILE_GROUP_NAME = "ARouter$$Group$$";

    /***
     * @param path 例如：/order/Order_MainActivity
     *      * @return
     */
    public BundleManager build(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new IllegalArgumentException("不按常理出牌 path乱搞的啊，正确写法：如 /order/Order_MainActivity");
        }

        // 同学可以自己增加
        // ...

        if (path.lastIndexOf("/") == 0) { // 只写了一个 /
            throw new IllegalArgumentException("不按常理出牌 path乱搞的啊，正确写法：如 /order/Order_MainActivity");
        }

        // 截取组名  /order/Order_MainActivity  finalGroup=order
        String finalGroup = path.substring(1, path.indexOf("/", 1)); // finalGroup = order

        if (TextUtils.isEmpty(finalGroup)) {
            throw new IllegalArgumentException("不按常理出牌 path乱搞的啊，正确写法：如 /order/Order_MainActivity");
        }

        // 证明没有问题，没有抛出异常
        this.path = path;  // 最终的效果：如 /order/Order_MainActivity
        this.group = finalGroup; // 例如：order，personal

        // TODO 走到这里后  grooup 和 path 没有任何问题   app，order，personal      /app/MainActivity

        return new BundleManager(); // Builder设计模式 之前是写里面的， 现在写外面吧
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Object navgation(Context context, BundleManager bundleManager) {
        String groupClassName = context.getPackageName() + "."+ FILE_GROUP_NAME + group;

        ARouterGroup aRouterGroup = groupLruCache.get(group);
        if (aRouterGroup == null) {
            try {
                Class<?> aClass = Class.forName(groupClassName);
                aRouterGroup = (ARouterGroup) aClass.newInstance();
                groupLruCache.put(groupClassName, aRouterGroup);

                ARouterPath aRouterPath = pathLruCache.get(group);
                if (aRouterPath == null){
                    Class<? extends ARouterPath> aClazz = aRouterGroup.getGroupMap().get(group);
                    aRouterPath = aClazz.newInstance();
                    pathLruCache.put(path,aRouterPath);

                    if (aRouterPath != null){
                        RouterBean routerBean = aRouterPath.getPathMap().get(path);
                        if (routerBean != null){
                            switch (routerBean.getTypeEnum()){
                                case ACTIVITY:
                                    Intent intent = new Intent(context,routerBean.getMyClass());
                                    intent.putExtras(bundleManager.getBundle());
                                    context.startActivity(intent,bundleManager.getBundle());
                                    break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
