package com.lzr.arouter_compiler;

import com.google.auto.service.AutoService;
import com.lzr.arouter_annotation.ARouter;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        elementTool = processingEnvironment.getElementUtils();
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();

        messager.printMessage(Diagnostic.Kind.NOTE, "=========my init is running");

        String options = processingEnvironment.getOptions().get("moduleName");
        messager.printMessage(Diagnostic.Kind.NOTE, "=========my init options:" + options);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        messager.printMessage(Diagnostic.Kind.NOTE, "========my annotation is running");

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
        for (Element element : elements) {  //elements == MainActivity
            //1方法
            MethodSpec methodSpec = MethodSpec.methodBuilder("main")
                    .addModifiers(Modifier.PUBLIC,Modifier.STATIC)
                    .addParameter(String[].class,"args")
                    .returns(void.class)
                    .addStatement("$T.out.println($S)",System.class,"Hello JavaPoet!")
                    .build();

            //2.类
            TypeSpec typeSpec = TypeSpec.classBuilder("TestClass")
                    .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                    .addMethod(methodSpec)
                    .build();

            //3.生成文件
            JavaFile javaFile = JavaFile.builder("com.lzr.test",typeSpec).build();
            try {
                javaFile.writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
                messager.printMessage(Diagnostic.Kind.NOTE, "========the file failed");
            }
        }

        return false;
    }
}
