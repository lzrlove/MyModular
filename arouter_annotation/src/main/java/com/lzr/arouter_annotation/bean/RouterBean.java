package com.lzr.arouter_annotation.bean;



import javax.lang.model.element.Element;


public class RouterBean {
    public enum TypeEnum{
        ACTIVITY
    }

    private TypeEnum typeEnum; //枚举类型 Activity
    private Element element; //类节点
    private Class<?> myClass; //被注解的class对象 如：MainActivity.class
    private String path; //路由地址 /app/MainActivity
    private String group; //路由组  app order
    private RouterBean(TypeEnum typeEnum, Class<?>myClass,
                      String path,String group){
        this.typeEnum = typeEnum;
        this.myClass = myClass;
        this.path = path;
        this.group = group;
    }

    public TypeEnum getTypeEnum() {
        return typeEnum;
    }

    public void setTypeEnum(TypeEnum typeEnum) {
        this.typeEnum = typeEnum;
    }

    public Element getElement() {
        return element;
    }

    public Class<?> getMyClass() {
        return myClass;
    }

    public String getPath() {
        return path;
    }

    public String getGroup() {
        return group;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public void setMyClass(Class<?> myClass) {
        this.myClass = myClass;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    //对外提供简易版构造方法
    public static RouterBean create(TypeEnum typeEnum,Class<?>clazz,String path,String group){
        return new RouterBean(typeEnum,clazz,path,group);
    }


    private RouterBean(Builder builder){
        this.typeEnum = builder.typeEnum;
        this.element = builder.element;
        this.myClass = builder.clazz;
        this.path = builder.path;
        this.group = builder.group;
    }
    //构建者模式
    public static class Builder{
        private TypeEnum typeEnum;
        private Element element;
        private Class<?>clazz;
        private String path;
        private String group;
        public Builder addType(TypeEnum typeEnum){
            this.typeEnum= typeEnum;
            return  this;
        }

        public Builder addElement(Element element){
            this.element = element;
            return  this;
        }

        public Builder addClazz(Class<?> clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder addPath(String path) {
            this.path = path;
            return this;
        }

        public Builder addGroup(String group) {
            this.group = group;
            return this;
        }

        //最后是build
        public RouterBean build(){
            if (path == null || path.length() == 0){
                throw new IllegalArgumentException("path is null, example:/app/MainActivity");
            }
            return new RouterBean(this);
        }
    }
}
