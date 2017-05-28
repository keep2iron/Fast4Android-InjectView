package keep2iron.github.io.compiler;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import io.github.keep2iron.fast4android.annotations.InjectView;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * * 1.首先进行继承AbstractProcessor
 * 2.重写process.
 * <p>
 * 那么有几个类
 * Element    被注解的元素
 * Elements   操作元素的工具类
 * TypeElement  封装元素的类型信息
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("io.github.keep2iron.fast4android.annotations.InjectView")
public class Fast4AndroidProcessor extends AbstractProcessor {
    private Elements mElementUtils;

    Filer filer;

    /**
     * 一个父类元素对应一个生成的文件
     * <p>
     * String是注解的父类元素的的完整路径
     * BuildingSet相当于作为生成文件的一个工具类
     */
    Map<String, BuildingSet> mBuildCache = new HashMap<>();

    /**
     * 每一个注解处理器类都必须有一个空的构造函数。然而，这里有一个特殊额init()方法，它会被处理工具调用，
     * 并输入ProcessingEnvironment参数
     * ProcessingEnviroment提供很多有用的工具类Elements,Types和Filer。
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        //初始化工具类
        mElementUtils = processingEnvironment.getElementUtils();
        filer = processingEnvironment.getFiler();
    }


    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //获取被注解的元素集合
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(InjectView.class);
        //遍历被注解元素
        for (Element element : elements) {
            InjectViewField field = new InjectViewField(element);
            //获取被注入对象持有类        比如某一个Activity
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();
            PackageElement packageElement = mElementUtils.getPackageOf(element);
            //获取这个类的构建集合
            BuildingSet buildingSet = mBuildCache.get(typeElement.getQualifiedName().toString());
            if (buildingSet == null) {
                buildingSet = new BuildingSet(element,TypeName.get(typeElement.asType()), ClassName.get(typeElement));
                mBuildCache.put(typeElement.getQualifiedName().toString(), buildingSet);
            }

            //将这个被注入的field加入构建队列中
            buildingSet.addInjectViewField(field);
        }

        //从缓存中拿出javaFile，进行构建，将这些保存
        for (Map.Entry<String, BuildingSet> entry : mBuildCache.entrySet()) {
            try {
                entry.getValue().build().writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * 打印错误信息
     *
     * @param element 元素
     * @param message 消息
     * @param args    参数
     */
    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    /**
     * 打印信息
     *
     * @param kind    类型
     * @param element 元素
     * @param message 消息
     * @param args    参数
     */
    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }
}