package io.github.keep2iron.fast4android.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by keep2iron on 2017/4/6.
 * write the powerful code !
 * website : keep2iron.github.io
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface InjectView {
  int value();
}