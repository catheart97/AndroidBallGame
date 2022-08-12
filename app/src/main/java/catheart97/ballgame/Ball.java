package catheart97.ballgame;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import static java.lang.Math.PI;

public class Ball
        implements GLObject
{
    // DATA /////////////////////////////////////////////////////////////////////////////
    private final FloatBuffer _vertex_buffer;
    private ShortBuffer _index_buffer;
    private final int _shader_program;

//    public float _angle = 10;

    static final int COORDS_PER_VERTEX = 3;
    static float _coords[]; // vertex buffer
    static short _ibo[]; // index buffer
    private int _vertex_stride;

    public float[] _position = {.0f, 0.f, 0.0f};
    public float _radius = .1f;
    float _color[] = {1.0f,1.0f,1.0f,1.0f};
//    float _color[] = {179.f / 255.f, 1.0f, 9.f / 255.f, 1.0f};

    float _ambient_strength = 0.2f;

    public boolean _lightning = true;

    // CONSTRUCTORS //////////////////////////////////////////////////////////////////////
    public Ball(Context context)
    {
        ArrayList<Float> points = new ArrayList<>();
        short res = 32;
        float iter = 2.0f * ((float) PI) / (((float)res) - 1.0f);

        float left_border = 0.0f;
        float right_border = (float) PI * 2.0f + 0.5f * iter;

        for (float p = left_border; p < right_border; p += iter)
        {
            for (float t = left_border; t < right_border; t += iter)
            {
                float r = 1.f;
                points.add((float) (r * Math.cos(p) * Math.cos(t)));
                points.add((float) (r * Math.cos(p) * Math.sin(t)));
                points.add((float) (r * Math.sin(p)));
            }
        }

        ArrayList<Short> ibo = new ArrayList<>(); // IBO

        short k = 0;
        for (int i = 0; i < res - 1; i++)
        {
            ibo.add(k);
            for (int j = 0; j < res; j++)
            {
                ibo.add((short) (k + j));
                ibo.add((short) (k + j + res));
            }
            ibo.add((short) (k + res + res - 1));
            k += res;
        }

        _ibo = new short[ibo.size()];
        for (int i = 0; i < ibo.size(); ++i)
        {
            _ibo[i] = ibo.get(i);
        }

        _coords = new float[points.size()];
        for (int i = 0; i < points.size(); ++i)
        {
            _coords[i] = points.get(i);
        }

        ByteBuffer bb_i = ByteBuffer.allocateDirect(_ibo.length * 2);
        bb_i.order(ByteOrder.nativeOrder());
        _index_buffer = bb_i.asShortBuffer();
        _index_buffer.put(_ibo);
        _index_buffer.position(0);


        _vertex_stride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

        ByteBuffer bb = ByteBuffer.allocateDirect(_coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        _vertex_buffer = bb.asFloatBuffer();
        _vertex_buffer.put(_coords);
        _vertex_buffer.position(0);

        int vertexShader = GLRenderer.loadShader(GLES30.GL_VERTEX_SHADER, GLRenderer.load_from_assets(context, "VertexShaderBall.glsl"));
        int fragmentShader = GLRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER, GLRenderer.load_from_assets(context, "FragmentShader.glsl"));

        _shader_program = GLES30.glCreateProgram();
        GLES30.glAttachShader(_shader_program, vertexShader);
        GLRenderer.checkGlError("glAttachShader");
        GLES30.glAttachShader(_shader_program, fragmentShader);
        GLRenderer.checkGlError("glAttachShader");

        GLES30.glBindAttribLocation(_shader_program, 0, "vpos"); // !!!

        GLES30.glLinkProgram(_shader_program);
        GLRenderer.checkGlError("glLinkProgram");
    }

    @Override
    public void draw(float[] view, float[] projection, float[] light_position)
    {
        GLES30.glUseProgram(_shader_program);

        float[] model = new float[16];
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, _position[0], _position[1], _position[2]);
        Matrix.scaleM(model, 0, _radius, _radius, _radius);
        //        Matrix.rotateM(model, 0, _angle, 1,0,0);

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(
                0, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                _vertex_stride, _vertex_buffer);

        int mColorHandle = GLES30.glGetUniformLocation(_shader_program, "uColor");
        GLES30.glUniform4fv(mColorHandle, 1, _color, 0);

        int mLightHandle = GLES30.glGetUniformLocation(_shader_program, "uLightPos");
        GLES30.glUniform3fv(mLightHandle, 1, light_position, 0);

        int mAmbient = GLES30.glGetUniformLocation(_shader_program, "uAmbientStrength");
        GLES30.glUniform1fv(mAmbient, 1, new float[]{_ambient_strength}, 0);

        int mLightningHandle = GLES30.glGetUniformLocation(_shader_program, "uLightning");
        GLES30.glUniform1fv(mLightningHandle, 1, _lightning ? new float[]{1.0f} : new float[]{0.0f}, 0);

        int mModel = GLES30.glGetUniformLocation(_shader_program, "uModel");
        GLRenderer.checkGlError("glGetUniformLocation");

        GLES30.glUniformMatrix4fv(mModel, 1, false, model, 0);
        GLRenderer.checkGlError("glUniformMatrix4fv");

        int mView = GLES30.glGetUniformLocation(_shader_program, "uView");
        GLRenderer.checkGlError("glGetUniformLocation");

        GLES30.glUniformMatrix4fv(mView, 1, false, view, 0);
        GLRenderer.checkGlError("glUniformMatrix4fv");

        int mProjection = GLES30.glGetUniformLocation(_shader_program, "uProjection");
        GLRenderer.checkGlError("glGetUniformLocation");

        GLES30.glUniformMatrix4fv(mProjection, 1, false, projection, 0);
        GLRenderer.checkGlError("glUniformMatrix4fv");

        GLES30.glLineWidth(1.0f);
//        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, _coords.length / 3);
        GLES30.glDrawElements(GLES30.GL_TRIANGLE_STRIP,
                              _index_buffer.capacity(),
                              GLES30.GL_UNSIGNED_SHORT,
                              _index_buffer);
        GLES30.glDisableVertexAttribArray(0);
    }

    @Override
    public void setPosition(float[] position)
    {
        if (position.length == 3)
        {
            _position = position;
        }
    }

    @Override
    public float[] getPosition()
    {
        return _position;
    }

    @Override
    public float getScale()
    {
        return _radius;
    }

    @Override
    public void setScale(float scale)
    {
        _radius = scale;
    }

    public float distance_to(Ball other)
    {
        float x = _position[0] - other._position[0];
        float y = _position[1] - other._position[1];
        float z = _position[2] - other._position[2];

        return (float)(Math.sqrt(x*x + y*y + z*z) - (_radius + other._radius));
    }

    @Override
    public void setLightning(boolean lightning)
    {
        _lightning = lightning;
    }
}

