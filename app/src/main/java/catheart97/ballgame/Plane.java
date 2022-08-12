package catheart97.ballgame;

import android.content.Context;
import android.opengl.GLES30;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Plane
    implements GLObject
{
    // DATA /////////////////////////////////////////////////////////////////////////////
    private FloatBuffer _vertex_buffer;
    private FloatBuffer _normal_buffer;
    private int _shader_program;

    static final int COORDS_PER_VERTEX = 3;
    static float _coords[] = new float[]{
            1.0f,1.0f,1.0f,
            1.0f,-1.0f, 1.0f,
            -1.0f,1.0f, 1.0f,
            -1.0f, 1.0f,1.0f,
            1.0f,-1.0f,1.0f,
            -1.0f,-1.0f,1.0f,
    }; // vertex buffer
    static float _normals[] = new float[]{
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f,
            0.0f, 0.0f, -1.0f
    };
    private int _vertex_stride = 0;
    public float[] _position = {.0f, 0.f, 0.0f};
    public float _scale = 1.f;
    float _color[] = {.4f,.4f,.4f, 1.0f};

    private boolean _lightning = true;

    float _ambient_strength = 0.4f;

    // CONSTRUCTORS //////////////////////////////////////////////////////////////////////


    public Plane(Context context)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(_coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        _vertex_buffer = bb.asFloatBuffer();
        _vertex_buffer.put(_coords);
        _vertex_buffer.position(0);

        bb = ByteBuffer.allocateDirect(_coords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        _normal_buffer = bb.asFloatBuffer();
        _normal_buffer.put(_normals);
        _normal_buffer.position(0);

        int vertexShader = GLRenderer.loadShader(GLES30.GL_VERTEX_SHADER, GLRenderer.load_from_assets(context, "VertexShader.glsl"));
        int fragmentShader = GLRenderer.loadShader(GLES30.GL_FRAGMENT_SHADER, GLRenderer.load_from_assets(context, "FragmentShader.glsl"));

        _shader_program = GLES30.glCreateProgram();
        GLES30.glAttachShader(_shader_program, vertexShader);
        GLRenderer.checkGlError("glAttachShader");
        GLES30.glAttachShader(_shader_program, fragmentShader);
        GLRenderer.checkGlError("glAttachShader");

        GLES30.glBindAttribLocation(_shader_program, 0, "vpos"); // !!!
        GLES30.glBindAttribLocation(_shader_program, 1, "vnormal"); // !!!

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
        Matrix.scaleM(model, 0, _scale, _scale, _scale);

        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(
                0, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                _vertex_stride, _vertex_buffer);

        GLES30.glEnableVertexAttribArray(1);
        GLES30.glVertexAttribPointer(
                1, COORDS_PER_VERTEX,
                GLES30.GL_FLOAT, false,
                _vertex_stride, _normal_buffer);

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
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, _coords.length / 3);
        GLES30.glDisableVertexAttribArray(0);
    }

    @Override
    public void setPosition(float[] position)
    {
        if (position.length == 3)
            _position = position;
    }

    @Override
    public void setScale(float scale)
    {
        _scale = scale;
    }

    @Override
    public float[] getPosition()
    {
        return _position;
    }

    @Override
    public float getScale()
    {
        return _scale;
    }

    @Override
    public void setLightning(boolean lightning)
    {
        _lightning = lightning;
    }
}
