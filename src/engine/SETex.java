package engine;

//Contains minor data to keep track of the texture it is supposed to represent.
public class SETex {
    protected SETex() {}
    
    //Where this particular texture starts in the large mesh texture that the
    //engine uses, in pixel coordinates.
    protected double texX = 0, texY = 0;
    //The dimensions of this particular texture, in pixels.
    protected double texW = 0, texH = 0;
    
    //Debug functions, probably best if you don't use these.
    public int getStartX() { return (int)(texX * (double)SETextures.getTexDimWidth()); }
    public int getStartY() { return (int)(texY * (double)SETextures.getTexDimHeight()); }
    public int getWidth() { return (int)(texW * (double)SETextures.getTexDimWidth()); }
    public int getHeight() { return (int)(texH * (double)SETextures.getTexDimHeight()); }
    
    @Override public boolean equals(Object a) {
        SETex ar = (SETex)a;
        return texX == ar.texX && texY == ar.texY && texW == ar.texW && texH == ar.texH;
    }
}