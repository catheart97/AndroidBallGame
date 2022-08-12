package catheart97.ballgame;

public interface GLObject
{
    // METHODS //////////////////////////////////////////////////////////////////////////
    void draw(float[] view, float[] projection, float[] light_position);

    void setPosition(float[] position);

    void setScale(float scale);

    float[] getPosition();

    float getScale();

    void setLightning(boolean lightning);
}
