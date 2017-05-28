package io.github.keep2iron.fast4android;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by keep2iron on 2017/4/6.
 * write the powerful code ！
 * website : keep2iron.github.io
 */

public class Fast4Android {
    //构造器的映射集合
    private static Map<Class<?>, Constructor<? extends Unbinder>> BINDERS = new LinkedHashMap<>();
    private static Map<Class<?>, Unbinder> UNBINDERS = new LinkedHashMap<>();

    private static Application.ActivityLifecycleCallbacks mActivityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            Unbinder unbinder = UNBINDERS.get(activity.getClass());
            if(unbinder != null) unbinder.unbind();
        }
    };

    /**
     * 这两个注解
     */
    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Activity target) {
        View sourceView = target.getWindow().getDecorView();

        Unbinder unbinder = createBinding(target, sourceView);
        if(UNBINDERS.get(target.getClass()) != null)
            UNBINDERS.put(target.getClass(),unbinder);
        target.getApplication().registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        return unbinder;
    }

    /**
     * 创建binding对象
     *
     * @param target
     * @param sourceView
     * @return
     */
    private static Unbinder createBinding(@NonNull Activity target, @NonNull View sourceView) {
        Constructor<? extends Unbinder> constructor = findBindingConstructorForClass(target);

        Unbinder unbinder = null;
        try {
            unbinder = constructor.newInstance(sourceView, target);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (unbinder == null)
            unbinder = Unbinder.EMPTY;

        return unbinder;
    }

    /**
     * 对指定的target,获取生成文件的类Constructor,形如xxxxx_Binding.java这样的类的构造器对象
     *
     * @param target
     * @return
     */
    @Nullable
    private static Constructor<? extends Unbinder> findBindingConstructorForClass(Object target) {
        Class<?> clazz = target.getClass();

        Constructor<? extends Unbinder> ctor = BINDERS.get(clazz);

        if (ctor != null) {
            return ctor;
        }

        String clsName = clazz.getName();
        try {
            Class<?> bindingClass = Class.forName(clsName + "_Binding");
            ctor = (Constructor<? extends Unbinder>) bindingClass.getConstructor(View.class, clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        BINDERS.put(clazz, ctor);

        return ctor;
    }
}