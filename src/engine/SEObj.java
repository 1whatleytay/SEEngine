package engine;

//A small object that contains a point to it's entry in OpenGL plus a bunch of
//other small properties.
public class SEObj {
    protected SEObj() {}
    //Is true once wrapped. Only matters when SEdoubleWrappedObjects is false
    protected boolean isWrapped = false;
    //Pointer to it's entry in OpenGL
    protected int object = 0;
    
    //Perform any changes to the variables x, y, w, h and tex, then call
    //SEojSave(SEObj) to save your changes.
    
    //Coordinates in pixels on screen...
    public int x = 0, y = 0;
    //Width and height in pixels
    public int w = 0, h = 0;
    //The current texture of the object.
    public SETex tex = new SETex();
    
    //The coordinate in pixels of the center of the object in pixels. Updates
    //after a call to SEobjSave(SEObj) or genCenter().
    public int centerX = 0, centerY = 0;
    //Updates the varibles centerX and centerY.
    public void genCenter() { centerX = x + w / 2; centerY = y + h / 2; }
    //Returns the point to this object's entry in OpenGL.
    public int getObjectName() { return object; }
}
