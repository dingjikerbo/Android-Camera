package com.inuker.multisurfacepreview;

import android.opengl.EGLContext;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.inuker.library.BaseActivity;
import com.inuker.library.utils.LogUtils;

/**
 * 两个SurfaceView共享EglContext，CameraSurfaceView将相机预览绘制到
 * Texture，MiniSurfaceView再将该Texture处理后绘制到Display Surface
 */
public class MainActivity extends BaseActivity {

    private CameraSurfaceView mLeftSurfaceView;
    private MiniSurfaceView mRightSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FrameLayout left = (FrameLayout) findViewById(R.id.left);
        final FrameLayout right = (FrameLayout) findViewById(R.id.right);

        mLeftSurfaceView = new CameraSurfaceView(this);
        left.addView(mLeftSurfaceView);

        mLeftSurfaceView.getEglContext(new CameraSurfaceView.SurfaceCallback() {
            @Override
            public void onCallback(Object object) {
                LogUtils.v("SurfaceCallback %s", object.toString());

                mRightSurfaceView = new MiniSurfaceView(mContext, (EGLContext) object);
                right.addView(mRightSurfaceView);

                mRightSurfaceView.setZOrderOnTop(true);
            }
        });
    }
}
