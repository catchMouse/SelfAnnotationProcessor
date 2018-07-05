package com.example.szx.selfannotationprocessor;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.example.szx.anno.BindSelfView;

@BindSelfView(1)
public class MainActivity extends Activity {

    @BindSelfView(R.id.id_textview1)
    TextView textView1;
    @BindSelfView(R.id.id_textview2)
    TextView textView2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    @BindSelfView(2)
    class ViewHolder {
    }
}
