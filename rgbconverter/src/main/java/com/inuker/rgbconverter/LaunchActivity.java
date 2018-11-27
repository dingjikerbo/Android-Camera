package com.inuker.rgbconverter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.inuker.library.BaseActivity;

/**
 * Created by dingjikerbo on 17/8/22.
 */

public class LaunchActivity extends BaseActivity {

    public static final int READ_NORMAL = 0;
    public static final int READ_PBUFFER = 1;
    public static final int READ_FBO = 2;
    public static final int READ_PBO = 3;
    public static final int READ_PBOS = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_activity);

        ViewGroup container = (ViewGroup) findViewById(R.id.container);

        String[] converterTips = getResources().getStringArray(R.array.converters);
        for (int i = 0; i < converterTips.length; i++) {
            Button btn = new Button(this);
            btn.setText(converterTips[i]);
            btn.setOnClickListener(new ClickListener(i));
            container.addView(btn);
        }
    }

    private class ClickListener implements View.OnClickListener {

        int idx;

        ClickListener(int idx) {
            this.idx = idx;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.putExtra("index", idx + 1);
            startActivity(intent);
        }
    }
}
