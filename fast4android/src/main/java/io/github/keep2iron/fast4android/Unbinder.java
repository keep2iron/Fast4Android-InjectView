package io.github.keep2iron.fast4android;

import android.support.annotation.UiThread;

/**
 * Created by keep2iron on 2017/4/6.
 * write the powerful code ！
 * website : keep2iron.github.io
 */

public interface Unbinder {
  @UiThread void unbind();

  Unbinder EMPTY = new Unbinder() {
    @Override public void unbind() {}
  };
}