package com.lzr.arouter_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)  //作用于类上
@Retention(RetentionPolicy.CLASS) //编译期间
public @interface ARouter {
    String path();

    String group() default "";
}
