package catheart97.ballgame;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class MainActivity
        extends Activity
{
    // DATA /////////////////////////////////////////////////////////////////////////////
    private GLView _view;
    private SensorManager _sensor_manager;

    // METHODS //////////////////////////////////////////////////////////////////////////
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (detectOpenGLES30())
        {
            _view = new GLView(this);
            setContentView(_view);
        } else
        {
            Log.e("openglcube", "OpenGL ES 3.0 not supported on device.  Exiting...");
            finish();
        }

        startSensor();
        _view.setPreserveEGLContextOnPause(true);
    }

    private void startSensor()
    {
        _sensor_manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        _sensor_manager.registerListener(_view.getRenderer(),
                                         _sensor_manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                         SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopSensor()
    {
        _sensor_manager.unregisterListener(_view.getRenderer());
    }

    private boolean detectOpenGLES30()
    {
        ActivityManager am =
                (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo info = am.getDeviceConfigurationInfo();
        return (info.reqGlEsVersion >= 0x30000);
    }

    @Override
    protected void onPause()
    {
//        _view.onPause();
        _view.getRenderer()._running = false;
        stopSensor();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
//        _view.onResume();
        _view.getRenderer()._running = true;
        startSensor();
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}