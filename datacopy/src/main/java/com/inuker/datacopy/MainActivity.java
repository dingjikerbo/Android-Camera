package com.inuker.datacopy;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inuker.library.BaseActivity;
import com.inuker.library.EventDispatcher;
import com.inuker.library.EventListener;

import static com.inuker.datacopy.Events.EVENTS_TIME_UPDATE;

public class MainActivity extends BaseActivity implements EventListener {

    private Class[] DATACOPYS = {
            DataCopy1.class,
            DataCopy2.class,
            DataCopy3.class,
    };

    private TextView mTvTime;
    private DataCopy mDataCopy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewGroup surfaceContainer = (ViewGroup) findViewById(R.id.full);

        mTvTime = (TextView) findViewById(R.id.fps);

        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(new CameraSurfaceRender(glSurfaceView));
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        surfaceContainer.addView(glSurfaceView);

        int idx = getIntent().getIntExtra("index", 0);
        mDataCopy = DataCopy.load(DATACOPYS[idx]);
        mDataCopy.start();

        EventDispatcher.observe(EVENTS_TIME_UPDATE, this);
    }

    @Override
    protected void onDestroy() {
        mDataCopy.stop();
        EventDispatcher.unObserve(EVENTS_TIME_UPDATE, this);
        super.onDestroy();
    }

    @Override
    public void onEvent(int event, Object object) {
        mTvTime.setText(String.format("CopyTime: %dms", object != null ? object : 0));
    }
}
