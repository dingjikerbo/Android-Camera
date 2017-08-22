package com.inuker.rgbconverter;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.inuker.library.BaseActivity;
import com.inuker.library.EventDispatcher;
import com.inuker.library.EventListener;

import static com.inuker.rgbconverter.Events.BITMAP_AVAILABLE;
import static com.inuker.rgbconverter.Events.FPS_AVAILABLE;

public class MainActivity extends BaseActivity implements EventListener {

    private ViewGroup mFullSurfaceContainer;

    private ImageView mImage;

    private int mConverterIndex;

    private Bitmap mBitmap;

    private TextView mTvFps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConverterIndex = getIntent().getIntExtra("index", 1);

        mFullSurfaceContainer = (ViewGroup) findViewById(R.id.full);
        mImage = (ImageView) findViewById(R.id.image);

        mTvFps = (TextView) findViewById(R.id.fps);

        CameraSurfaceView cameraSurfaceView = new CameraSurfaceView(this, getRgbConverter());
        mFullSurfaceContainer.addView(cameraSurfaceView);

        EventDispatcher.observe(this, BITMAP_AVAILABLE, FPS_AVAILABLE);
    }

    public RgbConverter getRgbConverter() {
        switch (mConverterIndex) {
            case 1:
                return new RgbConverter1(this);
            case 2:
                return new RgbConverter2(this);
            case 3:
                return new RgbConverter3(this);
            case 4:
                return new RgbConverter4(this);
            case 5:
                return new RgbConverter5(this);
            default:
                return null;
        }
    }

    @Override
    protected void onDestroy() {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
            mBitmap = null;
        }
        EventDispatcher.unObserve(this, BITMAP_AVAILABLE, FPS_AVAILABLE);
        super.onDestroy();
    }

    private void updateBitmap(final Bitmap bitmap) {
        mImage.post(new Runnable() {
            @Override
            public void run() {
                if (mBitmap != null && !mBitmap.isRecycled()) {
                    mBitmap.recycle();
                }
                mBitmap = bitmap;
                mImage.setImageBitmap(mBitmap);
            }
        });
    }

    private void updateFps(final int fps) {
        mTvFps.post(new Runnable() {
            @Override
            public void run() {
                mTvFps.setText(String.format("readPixels: %dms", fps));
            }
        });
    }

    @Override
    public void onEvent(int event, final Object object) {
        switch (event) {
            case BITMAP_AVAILABLE:
                updateBitmap((Bitmap) object);
                break;
            case FPS_AVAILABLE:
                int time = object != null ? (int) object : 0;
                updateFps(time);
                break;
        }

    }
}
