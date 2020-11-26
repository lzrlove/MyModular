package com.lzr.arouter_compiler;

import com.google.auto.service.AutoService;
import com.lzr.arouter_annotation.ARouter;
import com.lzr.arouter_annotation.bean.RouterBean;
import com.lzr.arouter_compiler.utils.ProcessorConfig;
import com.lzr.arouter_compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


// AutoService则是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor.class)

//支持对哪个注解进行处理，注解的类型
@SupportedAnnotationTypes({"com.lzr.arouter_annotation.ARouter"})

//指定编译器版本
@SupportedSourceVersion(SourceVersion.RELEASE_7)

public class ARouterProcessor extends AbstractProcessor {

    //操作Element的工具类（类，函数，属性，其实都是Element）
    private Elements elementTool;

    private Messager messager;

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private Filer filer;

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private Types typeTool;
    private String options;
    private String aptPackage;

    //key:personal  value:List<RouterBean>
    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>();
    //key:personal  value:ARouter$$Path$$personal.class
    private Map<String, String> mAllGroupMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
        typeTool = processingEnvironment.getTypeUtils();

        messager.printMessage(Diagnostic.Kind.NOTE, "11my init is running");

        options = processingEnvironment.getOptions().get(ProcessorConfig.OPTIONS);
        aptPackage = processingEnvironment.getOptions().get(ProcessorConfig.APT_PACKAGE);
        messager.printMessage(Diagnostic.Kind.NOTE, "22my init options:" + options);
        messager.printMessage(Diagnostic.Kind.NOTE, "22my init aptPackage:" + aptPackage);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if(set.isEmpty()){
            messager.printMessage(Diagnostic.Kind.NOTE, "second my annotation is running");
            return false;
        }

        //获取所有被 @ARouter 注解的 元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);

        //获取Activity的类型
        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
        //显示类的描述信息  用来判断被注解的类是不是Activity的类型
        TypeMirror activityMirror = activityType.asType();


        for (Element element : elements) {  //elements == MainActivity
            //获取简单类名：MainActivity
            String className = element.getSimpleName().toString();
            messager.printMessage(Diagnostic.Kind.NOTE, "44 ARouter annotation class:" + className);

            //拿到注解
            ARouter aRouter = element.getAnnotation(ARouter.class);

            //TODO 以系列检查工作
            //对路由对象进行封装
            RouterBean routerBean = new RouterBean.Builder()
                    .addElement(element)
                    .addPath(aRouter.path())
                    .addGroup(aRouter.group())
                    .build();

            //被注解的类只能是继承Activity
            TypeMirror elementMirror = element.asType();
            if (typeTool.isSubtype(elementMirror, activityMirror)) { //证明是继承自Activity
                routerBean.setTypeEnum(RouterBean.TypeEnum.ACTIVITY);
            } else {
                throw new RuntimeException("55 @ARouter annotation only Activity");
            }

            if (checkRouterPath(routerBean)) {
                //给缓存一赋值
                List<RouterBean> routerBeans = mAllPathMap.get(routerBean.getGroup());
                //缓存没有就新建routebean加入
                if (ProcessorUtils.isEmpty(routerBeans)) {
                    routerBeans = new ArrayList<>();
                    routerBeans.add(routerBean);
                    mAllPathMap.put(routerBean.getGroup(), routerBeans);
                } else {
                    routerBeans.add(routerBean);
                }
            } else {
                messager.printMessage(Diagnostic.Kind.NOTE, "66 ====");
            }
        }


            // 定义（生成类文件实现的接口） 有 Path Group
            TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH);
            TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP);
            messager.printMessage(Diagnostic.Kind.NOTE, "99 pathType.."+pathType.getSimpleName().toString());

            //todo 生成Path类
            try {
                createPathFile(pathType);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "88 Path file failed..");
            }

            try {
                createGroupFile(groupType, pathType);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "88 Group file failed..");
            }
        return true;
    }

    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllGroupMap) || ProcessorUtils.isEmpty(mAllPathMap)) return;

        //Map<String, Class<? extends ARouterPath>>
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Class.class),
                //? extends ARouterPath
                WildcardTypeName.subtypeOf(ClassName.get(pathType)));
        TypeName typeName = ParameterizedTypeName.get(ClassName.get(Map.class), ClassName.get(String.class), parameterizedTypeName);

        //方法
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(typeName);

        //Map<String, Class<? extends ARouterPath>> groupMap  = new HashMap<>();
        methodSpec.addStatement("$T<$T,$T>$N = new $T<>()",
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(pathType))),
                ProcessorConfig.GROUP_VAR1,
                ClassName.get(HashMap.class)
        );

        // groupMap.put("order", ARouter$$Path$$order.class);
        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
            methodSpec.addStatement("$N.put($S,$T.class)",
                    ProcessorConfig.GROUP_VAR1,
                    entry.getKey(),
                    ClassName.get(aptPackage, entry.getValue()));
        }
        methodSpec.addStatement("return $N", ProcessorConfig.GROUP_VAR1);

        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;
        messager.printMessage(Diagnostic.Kind.NOTE, "88 Group File: " +
                aptPackage + "." + finalClassName);


        //public class ARouter$$Group$$order implements ARouterGroup {
        TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(groupType))
                .addMethod(methodSpec.build())
                .build();

        JavaFile.builder(aptPackage, typeSpec).build().writeTo(filer);
    }

    private void createPathFile(TypeElement pathType) throws IOException {
        if (ProcessorUtils.isEmpty(mAllPathMap)) {
            return;
        }

        //返回值Map<String, RouterBean>
        TypeName returnName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                ClassName.get(String.class),
                ClassName.get(RouterBean.class)
        );

        //遍历缓存一
        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "for 循环");
            //方法
            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .returns(returnName);

            // Map<String, RouterBean> pathMap = new HashMap<>();
            methodSpec.addStatement("$T<$T,$T> $N = new $T<>()",
                    ClassName.get(Map.class),
                    ClassName.get(String.class),
                    ClassName.get(RouterBean.class),
                    ProcessorConfig.PATH_VAR1,
                    ClassName.get(HashMap.class));

            // pathMap.put("/order/Order_MainActivity",
            //              RouterBean.create(RouterBean.TypeEnum.ACTIVITY,
            //              Order_MainActivity.class,
            //              "/order/Order_MainActivity",
            //              "order"
            //));多个
            List<RouterBean> pathList = entry.getValue();
            for (RouterBean bean : pathList) {
                methodSpec.addStatement("$N.put($S,$T.create($T.$L,$T.class,$S,$S))",
                        ProcessorConfig.PATH_VAR1,
                        bean.getPath(),
                        ClassName.get(RouterBean.class),
                        ClassName.get(RouterBean.TypeEnum.class),
                        bean.getTypeEnum(),
                        ClassName.get((TypeElement) bean.getElement()),
                        bean.getPath(),
                        bean.getGroup()
                );
            }

            methodSpec.addStatement("return $N", ProcessorConfig.PATH_VAR1);

            // TODO 注意：不能像以前一样，1.方法，2.类  3.包， 因为这里面有implements ，所以 方法和类要合为一体生成才行，这是特殊情况

            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
            messager.printMessage(Diagnostic.Kind.NOTE, "77 Path file: " + aptPackage + "." + finalClassName);

            // 生成类文件：ARouter$$Path$$personal
//            TypeSpec pathTypeSpec = TypeSpec.classBuilder(finalClassName)
//                    .addSuperinterface(ClassName.get(pathType)) // implement ARouterPath 接口实现
//                    .addModifiers(Modifier.PUBLIC)
//                    .addMethod(methodSpec.build())
//                    .build();

            JavaFile.builder(aptPackage, TypeSpec.classBuilder(finalClassName)
                    .addSuperinterface(ClassName.get(pathType)) // implement ARouterPath 接口实现
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(methodSpec.build())
                    .build())
                    .build()
                    .writeTo(filer);
            mAllGroupMap.put(entry.getKey(), finalClassName);

        }

    }


    /**
     * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
     *
     * @param bean 路由详细信息，最终实体封装类
     */
    private final boolean checkRouterPath(RouterBean bean) {
        String group = bean.getGroup(); //  同学们，一定要记住： "app"   "order"   "personal"
        String path = bean.getPath();   //  同学们，一定要记住： "/app/MainActivity"   "/order/Order_MainActivity"   "/personal/Personal_MainActivity"

        // 校验
        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
            return false;
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
            return false;
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
        String finalGroup = path.substring(1, path.indexOf("/", 1));

        // app,order,personal == options

        // @ARouter注解中的group有赋值情况
        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
            // 架构师定义规范，让开发者遵循
            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
            return false;
        } else {
            bean.setGroup(finalGroup);
        }

        // 如果真的返回ture   RouterBean.group  xxxxx 赋值成功 没有问题
        return true;
    }
}
