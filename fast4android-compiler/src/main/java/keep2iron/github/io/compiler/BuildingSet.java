package keep2iron.github.io.compiler;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created by keep2iron on 2017/4/7.
 * write the powerful code!
 * website : keep2iron.github.io
 * <p>
 *
 * 一个BuildingSet相当于一个动态生成的java文件
 */
public class BuildingSet {
    static final ClassName FAST4ANDROID = ClassName.get("io.github.keep2iron.fast4android", "Fast4Android");
    static final ClassName VIEW = ClassName.get("android.view", "View");
    static final ClassName Unbinder = ClassName.get("io.github.keep2iron.fast4android", "Unbinder");

    private Element mElement;
    TypeName mTargetTypeName;       //被注入对象的类型      例如注入到activity中  typeName表示Activity
    ClassName mInjectClassName;     //

    private ImmutableList.Builder<InjectViewField> mInjectViewBuilder = new ImmutableList.Builder<>();

    /**
     * 构建一个BuildingSet
     *  @param typeName              用于表示被注入的对象的类型
     * @param buildClassName        用于表示生成文件的完整类名
     */
    public BuildingSet(Element element, TypeName typeName, ClassName buildClassName) {
        this.mTargetTypeName = typeName;
        this.mInjectClassName = buildClassName;
        this.mElement = element;
    }

    /**
     * 为方法添加注入元素
     *
     * @param field     被注入对象
     */
    public void addInjectViewField(InjectViewField field) {
        mInjectViewBuilder.add(field);
    }

    /**
     * 开始构建
     */
    public JavaFile build() {
        //构建注入集合
        ImmutableList<InjectViewField> injectViewFields = mInjectViewBuilder.build();

        FieldSpec targetFiled = FieldSpec.builder(mTargetTypeName,"target",Modifier.PRIVATE).build();

        //构建 构建构造函数，参数是绑定的view对象 和   被注入的对象
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(VIEW, "source")
                .addParameter(mTargetTypeName, "target");
        constructorBuilder.addStatement("this.target = target");
        for (InjectViewField field : injectViewFields) {
            buildViewInject(constructorBuilder, field);
        }

        //生成实现Unbinder接口的unbind方法
        MethodSpec.Builder unbinderMethod = MethodSpec.methodBuilder("unbind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(void.class);
        for (InjectViewField field : injectViewFields) {
            buildUnbinderMethod(unbinderMethod, field);
        }
        unbinderMethod.addStatement("target = null");

        TypeSpec typeSpec = TypeSpec.classBuilder(mElement.getEnclosingElement().getSimpleName() + "_Binding")
                .addModifiers(Modifier.PUBLIC)
                .addField(targetFiled)
                .addSuperinterface(Unbinder)
                .addMethod(unbinderMethod.build())
                .addMethod(constructorBuilder.build())
                .build();
        return JavaFile.builder(mInjectClassName.packageName(), typeSpec).build();
    }


    /**
     * 为方法添加生成的注入代码
     *
     * @param method                方法的构建对象
     * @param injectViewField       要被注入的变量
     */
    private void buildViewInject(MethodSpec.Builder method, InjectViewField injectViewField) {
        int resId = injectViewField.getResId();
        VariableElement variableElement = injectViewField.getVariableElement();

        TypeName typeName = TypeName.get(variableElement.asType());
        variableElement.getConstantValue();

        method.addStatement("target.$L = ($T)source.findViewById($L)",variableElement.getSimpleName().toString(),typeName,resId);

        method.build();
    }

    private void buildUnbinderMethod(MethodSpec.Builder method, InjectViewField injectViewField){
        VariableElement variableElement = injectViewField.getVariableElement();

        method.addStatement("target.$L = null",variableElement.getSimpleName().toString());
    }
}