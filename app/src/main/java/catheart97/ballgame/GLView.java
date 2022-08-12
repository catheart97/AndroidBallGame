package catheart97.ballgame;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GLView
        extends GLSurfaceView
{
    // DATA /////////////////////////////////////////////////////////////////////////////
    private final GLRenderer _renderer;

    // CONSTRUCTORS /////////////////////////////////////////////////////////////////////
    public GLView(Context context)
    {
        super(context);
        setEGLContextClientVersion(3);
        super.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        _renderer = new GLRenderer(context);
        setRenderer(_renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public GLRenderer getRenderer()
    {
        return _renderer;
    }
}
