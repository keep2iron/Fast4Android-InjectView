package keep2iron.github.io.compiler;

import io.github.keep2iron.fast4android.annotations.InjectView;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;

/**
 * Created by keep2iron on 2017/4/7.
 * write the powerful code !
 * website : keep2iron.github.io
 *
 * VariableElement表示被注入的对象
 * resId记录了被注入对象的id
 */
public class InjectViewField {
    private VariableElement mVariableElement;
    private int mResId;

    InjectViewField(Element element) {
        if (element.getKind() != ElementKind.FIELD) {
            throw new IllegalArgumentException(String.format("Only fields can be annotated with @%s", InjectView.class.getSimpleName()));
        }

        mVariableElement = (VariableElement) element;
        InjectView annotation = mVariableElement.getAnnotation(InjectView.class);
        mResId = annotation.value();
    }

    public VariableElement getVariableElement() {
        return mVariableElement;
    }

    public int getResId() {
        return mResId;
    }
}