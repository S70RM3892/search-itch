package com.zat.quicklookup;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** アプリ一覧から開いたときの使い方画面（ここで選択して試せる） */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int pad = (int) (20 * getResources().getDisplayMetrics().density);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad * 2, pad, pad);

        TextView usage = new TextView(this);
        usage.setText(R.string.usage);
        usage.setTextSize(16);
        usage.setLineSpacing(0, 1.3f);
        root.addView(usage);

        EditText sample = new EditText(this);
        sample.setText(R.string.sample);
        sample.setTextSize(18);
        root.addView(sample);

        ScrollView sv = new ScrollView(this);
        sv.addView(root);
        setContentView(sv);
    }
}
