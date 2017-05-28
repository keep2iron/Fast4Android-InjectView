package io.github.keep2iron;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import io.github.keep2iron.fast4android.Fast4Android;
import io.github.keep2iron.fast4android.annotations.InjectView;

public class MainActivity extends Activity {

    @InjectView(R.id.tvTextView)
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Fast4Android.bind(this);

        textView.setText("注入成功");
    }
}