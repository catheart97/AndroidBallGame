package catheart97.ballgame;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import androidx.core.graphics.ColorUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class GLRenderer
        implements GLSurfaceView.Renderer,
                   SensorEventListener
{
    // DATA /////////////////////////////////////////////////////////////////////////////
    private static final String TAG = "GLRenderer";
    private ArrayList<GLObject> _objects;
    private Ball _ball, _hole;

    private final float[] _projection = new float[16];
    private final float[] _view = new float[16];

    private float[] _light_pos = {4.0f, 4.0f, -3.0f};
    private float _ratio;

    private Context _context;

    private SensorEvent _event;

    public boolean _running = true, _draw_ball = true, _new_game = true;

    // CONSTRUCTORS //////////////////////////////////////////////////////////////////////
    public GLRenderer(Context context)
    {
        _context = context;
    }

    // METHODS //////////////////////////////////////////////////////////////////////////
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        // Set the background frame _color
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Matrix.setIdentityM(_projection, 0);
    }

    private boolean init_collision(Ball b)
    {
        if (b.distance_to(_ball) < 0.1f || b.distance_to(_hole) < 0.1f)
            return true;

        for (GLObject object : _objects)
        {
            if (object != b && object instanceof Ball)
            {
                if (b.distance_to((Ball) object) < 0.1f)
                    return true;
            }
        }
        return false;
    }

    float hue2rgb(float p, float q, float t)
    {
        if(t < 0) t += 1;
        if(t > 1) t -= 1;
        if(t < 1/6) return p + (q - p) * 6 * t;
        if(t < 1/2) return q;
        if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
        return p;
    }

    float[] hsltorgb(float[] hsl)
    {
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];
        float[] rgb = new float[4];
        rgb[3] = 1.0f;

        if(s == 0)
        {
            rgb[0] = rgb[1] = rgb[2] = l; // achromatic
        }
        else
        {
            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            rgb[0] = hue2rgb(p, q, h + 1/3);
            rgb[1] = hue2rgb(p, q, h);
            rgb[2] = hue2rgb(p, q, h - 1/3);
        }
        return rgb;
    }

    public float next_float(Random random)
    {
        return (random.nextBoolean() ? 1.0f : -1.0f) * random.nextFloat();
    }

    public void init_objects()
    {
        _objects = new ArrayList<>();
        Random random = new Random();

        _hole._position[2] = 0.0f;

        for (int i = 0; i < 10; ++i)
        {
            Ball b = new Ball(_context);

            while (init_collision(b))
            {
                b.setScale(random.nextFloat() % 0.06f + 0.05f);
                b.setPosition(new float[]{next_float(random) % (_ratio - b.getScale()), next_float(
                        random) % (1 - b.getScale()), 0.1f - b.getScale()});
            }

            float hue = Math.abs(random.nextInt()) % 360;
            float saturation = Math.abs(random.nextInt()) % 100;
            float luminance = Math.abs(random.nextInt()) % 100;
            int color = ColorUtils.HSLToColor(new float[]{hue, saturation, luminance});
            b._color = new float[]{Color.red(color) / 255.f,Color.green(color) / 255.f, Color.blue(color) / 255.f, 1.0f};
            _objects.add(b);
        }

        _hole._position[2] = 1.0f;

        Plane p = new Plane(_context);
        p._position[2] = .1f;
        _objects.add(p);
    }

    public void new_game()
    {
        _draw_ball = true;
        _ball = new Ball(_context);
        _ball._lightning = false;

        _ball._ambient_strength = 1.f;
        _hole = new Ball(_context);
        _hole._lightning = false;
        _hole._color = new float[]{0.0f, 0.0f, 0.0f, 1.0f};

        Random random = new Random();

        while (_hole.distance_to(_ball) < .1f)
        {
            _hole.setPosition(new float[]{next_float(random) % (_ratio - _hole.getScale()), next_float(
                        random) % (1 - _hole.getScale()), 0.0f});
        }

        _hole._position[2] = 1.0f;

        init_objects();
        _new_game = false;
        _running = true;
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_BLEND);

        Matrix.setLookAtM(_view, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        if (_ball != null)
            _light_pos = _ball._position;


        if (_new_game)
            new_game();

        if (_draw_ball)
            _ball.draw(_view, _projection, _light_pos);
        _hole.draw(_view, _projection, _light_pos);
        for (GLObject object : _objects)
        {
            object.setLightning(_draw_ball);
            object.draw(_view, _projection, _light_pos);
        }

        collision();
    }

    public void collision()
    {
        SensorEvent event;
        if ((event = _event) != null)
        {
            final float FACTOR = .01f;
            float[] pos = _ball.getPosition();
            final float[] direction_map = {1.0f, -1.0f};
            final float[] offset_map = {_ratio - _ball.getScale(), 1 - _ball.getScale()};
            final int[] index_map = {0, 1};

            for (int i = 0; i < index_map.length; ++i)
            {
                if (Math.abs(event.values[index_map[i]]) > 0.01f)
                {
                    float new_pos = FACTOR * event.values[index_map[i]] * direction_map[i];
                    if (Math.abs(pos[i] + new_pos) < offset_map[i])
                        pos[i] += new_pos;
                    else
                        pos[i] = offset_map[i] * Math.signum(pos[i]);
                }
            }

            for (GLObject object : _objects)
            {
                Ball b;
                if (object instanceof Ball)
                {
                    b = (Ball) object;
                    if (b.distance_to(_ball) < 0.0f)
                    {
                        show_exit_dialog("You Lost!");
                    }
                }
            }
            if (_ball.distance_to(_hole) < 0.803521f)
            {
                _draw_ball = false;
                show_exit_dialog("You Won!");
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        GLES30.glViewport(0, 0, width, height);
        _ratio = (float) width / height;
        Matrix.orthoM(_projection, 0, -_ratio, _ratio, -1, 1, 1.f, 5.f);

        if (_objects == null)
            new_game();
    }

    public static int loadShader(int type, String shaderCode)
    {
        int shader = GLES30.glCreateShader(type);
        GLES30.glShaderSource(shader, shaderCode);
        checkGlError("glShaderSource");
        GLES30.glCompileShader(shader);
        checkGlError("glCompileShader");

        int[] compiled = new int[1];
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0);

        if (compiled[0] == 0)
        {
            Log.e(TAG, "Error!!!!");
            Log.e(TAG, GLES30.glGetShaderInfoLog(shader));
            GLES30.glDeleteShader(shader);
            return 0;
        }

        return shader;
    }

    public static void checkGlError(String glOperation)
    {
        int error;
        while ((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR)
        {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }


    public static String load_from_assets(Context context, String file_name)
    {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(file_name)));
            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line).append("\n");
            }
        } catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        } finally
        {
            if (reader != null)
            {
                try
                {
                    reader.close();
                } catch (IOException e)
                {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return builder.toString();
    }

    public void show_exit_dialog(String message)
    {

        if (_running)
        {
            _running = false;
            ((MainActivity) _context).runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(_context);
                builder.setMessage(message);
                builder.setPositiveButton(android.R.string.ok, (dialog, which) ->
                {
                    _new_game = true;
                });

                builder.setNegativeButton("Exit", (dialog, which) -> ((MainActivity) _context).finish());

                AlertDialog dialog = builder.create();
                dialog.show();
            });
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && _ball != null && _running)
        {
            _event = event;
        } else
        {
            _event = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }
}