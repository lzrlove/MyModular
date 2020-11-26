package com.lzr.arouter_compiler;

import com.google.auto.service.AutoService;
import com.lzr.arouter_annotation.Parameter;
import com.lzr.arouter_compiler.utils.ProcessorConfig;
import com.lzr.arouter_compiler.utils.ProcessorUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedAnnotationTypes({ProcessorConfig.PARAMETER_PACKAGE})
@SupportedSourceVersion(SourceVersion.RELEASE_7)

public class ParameterProcessor extends AbstractProcessor {


    private Elements elementUtils;
    private Filer filer;
    private Types typeUtils;
    private Messager messager;
    //TypeElement：Personal_MainActivity   List<Element>: name age sex
    private Map<TypeElement, List<Element>> tempParameterMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        elementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
        typeUtils = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        if (!ProcessorUtils.isEmpty(set)) {
            //获取所有被Parameter注解的元素集合
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Parameter.class);
            if (!ProcessorUtils.isEmpty(elements)) {
                //element == name sex age
                //添加缓存
                for (Element element : elements) {
                    //拿父节点 // enclosingElement == Personal_MainActivity == key
                    TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
                    //map是否包含此key
                    if (tempParameterMap.containsKey(enclosingElement)) {
                        tempParameterMap.get(enclosingElement).add(element);
                    } else {
                        List<Element> fileds = new ArrayList<>();
                        fileds.add(element);
                        tempParameterMap.put(enclosingElement, fileds);
                    }
                }

                if (ProcessorUtils.isEmpty(tempParameterMap)) return true;

                //
                TypeElement activityType = elementUtils.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
                TypeElement parameterType = elementUtils.getTypeElement(ProcessorConfig.AROUTER_AIP_PARAMETER_GET);

                //Object targetParameter
                ParameterSpec parameterSpec = ParameterSpec.builder(TypeName.OBJECT, ProcessorConfig.PARAMETER_NAME).build();

                //遍历仓库
                for (Map.Entry<TypeElement, List<Element>> entry : tempParameterMap.entrySet()) {

                    TypeElement typeElement = entry.getKey(); //Order_MainActivity

                    //判断是不是继承Activity
                    if (!typeUtils.isSubtype(typeElement.asType(), activityType.asType())) {
                        messager.printMessage(Diagnostic.Kind.NOTE, "Parameter annotation is only Activity");
                    }

                    //生成方法
                    MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(ProcessorConfig.PARAMETER_METHOD_NAME)
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addParameter(parameterSpec);
                    //Personal_MainActivity t = (Personal_MainActivity) targetParameter;
                    methodSpec.addStatement("$T t = ($T)" + ProcessorConfig.PARAMETER_NAME, ClassName.get(typeElement), ClassName.get(typeElement));

                    //t.name = t.getIntent().getStringExtra("name");
                    //t.sex = t.getIntent().getStringExtra("sex");
                    for (Element element : entry.getValue()) {
                        TypeMirror typeMirror = element.asType();
                        int ordinal = typeMirror.getKind().ordinal(); //获取注解的属性类型序列号 用来判断是int boolean string

                        String filedName = element.getSimpleName().toString(); //name age sex
                        String annotationValue = element.getAnnotation(Parameter.class).name(); //获取注解的值
                        annotationValue = ProcessorUtils.isEmpty(annotationValue) ? filedName : annotationValue;
                        //最终拼接
                        String finalValue = "t." + filedName;
                        String methodContent = finalValue + "= t.getIntent().";
                        if (ordinal == TypeKind.INT.ordinal()) { //如果是int
                            methodContent += "getIntExtra($S," + finalValue + ")";
                        } else if (ordinal == TypeKind.BOOLEAN.ordinal()) {
                            methodContent += "getBooleanExtra($S," + finalValue + ")";
                        } else {
                            if (typeMirror.toString().equalsIgnoreCase(ProcessorConfig.STRING)) {
                                methodContent += "getStringExtra($S)";
                            }
                        }
                        methodSpec.addStatement(methodContent, annotationValue);
                    }

                    // 最终生成的类文件名（类名$$Parameter） 例如：Personal_MainActivity$$Parameter
                    String finalClassName = typeElement.getSimpleName() + ProcessorConfig.PARAMETER_FILE_NAME;
                    messager.printMessage(Diagnostic.Kind.NOTE, "parameter====" + finalClassName);

                    //public class Personal_MainActivity$$Parameter implements ParameterGet {
                    TypeSpec typeSpec = TypeSpec.classBuilder(finalClassName)
                            .addSuperinterface(ClassName.get(parameterType))
                            .addModifiers(Modifier.PUBLIC)
                            .addMethod(methodSpec.build())
                            .build();
                    try {
                        JavaFile.builder(ClassName.get(typeElement).packageName(), typeSpec).build().writeTo(filer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        messager.printMessage(Diagnostic.Kind.NOTE, "Parameter file failed...");
                    }

                }

            }

        }
        return false;
    }
}
