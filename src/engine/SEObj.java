/*
 * SEEngine OpenGL 2.1 Engine
 * Copyright (C) 2017  desgroup

 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package engine;

import java.util.ArrayList;
import java.util.HashMap;

import static engine.SEConstants.*;
import static org.lwjgl.opengl.GL15.*;

/**
 * Stores some information for an object to be displayed on screen.
 * @author desgroup
 * @version SEAlpha4a
 */
public class SEObj {

    private SEObj() {}

    private static final int OBJECTSIZE = 4;
    private static final int OBJECTWIDTH = 4;
    private static boolean[] objectSpace = null;
    private static float[] objectMap = null;

    /**
     * The main OpenGL buffer containing all Object Data.
     */
    protected static int mainBuffer = -1;

    /**
     * Counts how far the furthest object is in {@link engine.SEObj#mainBuffer}.
     * The engine will only draw as far as this drawSpace.
     * Is not used if {@link engine.SEEngine#SEuseWrappedObjects} is enabled.
     */
    public static int objectDrawSpace = -1;

    /**
     * X direction.
     * 1 for normal, -1 for backwards.
     */
    protected static byte ampX = 1;

    /**
     * Y direction.
     * 1 for normal, -1 for backwards.
     */
    protected static byte ampY = -1;

    /**
     * Applies one of DIRECTION_ constants.
     * @param direction One of the DIRECTION_ constants to set the direction to.
     */
    public static void SEdirection(SEDirection direction) {
        switch (direction) {
            case DIRECTION_BOTTOM_TO_TOP: ampY = 1; break;
            case DIRECTION_TOP_TO_BOTTOM: ampY = -1; break;
            case DIRECTION_LEFT_TO_RIGHT: ampX = 1; break;
            case DIRECTION_RIGHT_TO_LEFT: ampX = -1; break;
            default: break;
        }
    }

    /**
     * Lowers {@link engine.SEObj#objectDrawSpace} to the lowest value without losing functionality.
     */
    public static void SEcollapseObjectDrawSpace() {
        for (int a = objectDrawSpace - 1; a >= 0; a--) {
            if (objectSpace[a]) { objectDrawSpace = a + 1; break; }
            if (a == 0) objectDrawSpace = 0;
        }
    }

    /**
     * Returns the maximum amount of unique {@link engine.SEObj} able to exist at one time.
     * Should be the same as your {@link engine.SEProgram} {@link engine.SEProgramData#maxObjects} value.
     * @return The maximum amount of unique {@link engine.SEObj} able to exist at one time.
     */
    public static int SEgetMaxOjectCount() { return objectSpace.length; }

    /**
     * A hidden blank object used with {@link engine.SEObj#visible(boolean)}.
     * May have a different {@link engine.SEObj#object} value but should be otherwise blank.
     */
    protected static SEObj blank = new SEObj();
    
    /**
     * Value is true if the object is has been hidden and is currently hidden with {@link engine.SEObj#visible(boolean)}.
     */
    protected boolean isHidden = false;
    /**
     * Value is true if the object has been wrapped with {@link engine.SEWrappedObj#SEWrappedObj(SEObj[])} or has been created wrapped.
     */
    protected boolean isWrapped = false;

    /**
     * Location in the OpenGL buffer {@link engine.SEObj#mainBuffer} where the object data hides itself.
     */
    protected int object = 0;

    /**
    * The x coordinate of the object (in pixels).
    * If you make any changes, be sure to save them with {@link engine.SEObj#save()}.
    */
    public int x = 0;
    /**
    * The y coordinate of the object (in pixels).
    * If you make any changes, be sure to save them with {@link engine.SEObj#save()}.
    */
    public int y = 0;
    /**
    * The width of the object (in pixels).
    * If you make any changes, be sure to save them with {@link engine.SEObj#save()}.
    */
    public int w = 0;
    /**
    * The height of the object (in pixels).
    * If you make any changes, be sure to save them with {@link engine.SEObj#save()}.
    */
    public int h = 0;

    /**
     * The texture the object will show over itself.
     * The texture will be stretched to meet the width and height of the object.
     * If you make any changes, be sure to save them with {@link engine.SEObj#save()}.
     */
    public SETex tex = SETex.BLANK_TEXTURE;

    /**
     * Calculates and returns the x coordinate of the center of the object.
     * @return the x coordinate of the center of the object.
     */
    public int getCenterX() { return x + w / 2; }

    /**
     * Calculates and returns the y coordinate of the center of the object.
     * @return the y coordinate of the center of the object.
     */
    public int getCenterY() { return y + h / 2; }

    private float[] genData() {
        return new float[] {
                ((float)x / SEEngine.scWidth * 2 - 1) * ampX, ((float)y / SEEngine.scHeight * 2 - 1) * ampY, (float)(tex.texX + (ampX==-1?tex.texW:0)), (float)(tex.texY + (ampY==-1?0:tex.texH)),
                ((float)(x + w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)y / SEEngine.scHeight * 2 - 1) * ampY, (float)(tex.texX + (ampX==-1?0:tex.texW)), (float)(tex.texY + (ampY==-1?0:tex.texH)),
                ((float)(x + w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)(y + h) / SEEngine.scHeight * 2 - 1) * ampY, (float)(tex.texX + (ampX==-1?0:tex.texW)), (float)(tex.texY + (ampY==-1?tex.texH:0)),
                ((float)x / SEEngine.scWidth * 2 - 1) * ampX, ((float)(y + h) / SEEngine.scHeight * 2 - 1) * ampY, (float)(tex.texX + (ampX==-1?tex.texW:0)), (float)(tex.texY + (ampY==-1?tex.texH:0)),
        };
    }

    private void pSave() {
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferSubData(GL_ARRAY_BUFFER, object * OBJECTSIZE * OBJECTWIDTH * OBJECTWIDTH, genData());
    }

    /**
     * Saves all changes to the object.
     */
    public void save() { if (!isHidden) pSave(); }

    /**
     * Constructor.
     * @param X The x position (in pixels) of the object.
     * @param Y The y position (in pixels) of the object.
     * @param W The width (in pixels) of the object.
     * @param H The height (in pixels) of the object.
     * @param T The texture of the object.
     */
    public SEObj(int X, int Y, int W, int H, SETex T) {
        boolean found = false;
        int find = -1;
        for (int a = 0; a < objectSpace.length; a++)  if (!objectSpace[a]) { find = a; found = true; break; }
        if (!found) { SEEngine.log(SEMessageType.MSG_TYPE_OPT, SEMessage.MSG_OUT_OF_OBJECT_MEMORY); }
        objectSpace[find] = true;
        object = find; x = X; y = Y; w = W; h = H; tex = T;
        objectDrawSpace = Math.max(objectDrawSpace, find + 1); // Let's try this. I didn't really think about this.
        save();
    }

    /**
     * Wrapped constructor.
     * @param X The x position (in pixels) of the object.
     * @param Y The y position (in pixels) of the object.
     * @param W The width (in pixels) of the object.
     * @param H The height (in pixels) of the object.
     * @param T The texture of the object.
     * @param doWrap If true, the object will be wrapped with a hidden wrapper.
     */
    public SEObj(int X, int Y, int W, int H, SETex T, boolean doWrap) {
        this(X, Y, W, H, T);
        if (doWrap)
            new SEWrappedObj(single());
    }

    /**
     * Changes all x, y, width and height params of this object all at once.
     * @param X The new x position (in pixels) of the object.
     * @param Y The new y position (in pixels) of the object.
     * @param W The new width (in pixels) of the object.
     * @param H The new height (in pixels) of the object.
     */
    public void data(int X, int Y, int W, int H) { x = X; y = Y; w = W; h = H; save(); }

    /**
     * Changes all the x, y, width, height and texture of this object at once.
     * @param X The new x position (in pixels) of the object.
     * @param Y The new y position (in pixels) of the object.
     * @param W The new width (in pixels) of the object.
     * @param H The new height (in pixels) of the object.
     * @param T The new texture of the object.
     */
    public void data(int X, int Y, int W, int H, SETex T) { x = X; y = Y; w = W; h = H; tex = T; save(); }

    /**
     * Changes the size of this object to w width and h height.
     * @param W The new width (in pixels) of the object.
     * @param H The new height (in pixels) of the object.
     */
    public void size(int W, int H) { w = W; h = H; save(); }

    /**
     * Changes the texture of this object to tex.
     * @param T The new texture of the object.
     */
    public void tex(SETex T) { tex = T; save(); }

    /**
     * Changes the position of this object to x, y.
     * @param X The x position (in pixels) where the object will be moved to.
     * @param Y The y position (in pixels) where the object will be moved to.
     */
    public void put(int X, int Y) { x = X; y = Y; save(); }

    /**
     * Moves this object by x (pixels) in the x axis and y (pixels) in the y axis.
     * @param X The amount (in pixels) that the object will be moved on the x axis.
     * @param Y The amount (in pixels) that the object will be moved on the y axis.
     */
    public void move(int X, int Y) { x += X; y += Y; save(); }

    /**
     * Changes the visibility of this object to value.
     * If value is false, the object will be hidden.
     * If the value is true, the object will become visible.
     * Any changes went unsaved by this object will automatically be saved when made visible.
     * @param value The visibility level. True for visible, false for invisible.
     */
    public void visible(boolean value) {
        isHidden = value;
        if (value) save();
        else { blank.object = object; save(); }
    }

    /**
     * Duplicates this object and returns the duplicate.
     * @return The duplicated {@link engine.SEObj} obj.
     */
    public SEObj duplicate() { return new SEObj(x, y, w, h, tex); }

    /**
     * Returns an array where the only element is this object.
     * @return An array of this object.
     */
    public SEObj[] single() { return new SEObj[]{ this }; }

    /**
     * Deletes this object.
     * The object will no longer draw and it's data will be availible for other objects to occupy.
     * {@link engine.SEObj#SEcollapseObjectDrawSpace()} will be called if {@link engine.SEEngine#SEcollapseObjectDrawSpaceOnDeletion} is enabled.
     */
    public void delete() {
        objectSpace[object] = false;
        data(0, 0, 0, 0, SETex.BLANK_TEXTURE);
        if (SEEngine.SEcollapseObjectDrawSpaceOnDeletion) SEcollapseObjectDrawSpace();
        else if (object == objectDrawSpace - 1) objectDrawSpace--;
    }

    /**
     * Resets the object space to contain maxObjects objects.
     * @param maxObjects New maximum amount of objects.
     */
    protected static void clearObjects(int maxObjects) {
        objectSpace = new boolean[maxObjects];
        objectMap = new float[maxObjects * OBJECTSIZE * OBJECTWIDTH];
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferData(GL_ARRAY_BUFFER, objectMap, GL_DYNAMIC_DRAW);
        objectDrawSpace = -1;
    }

    /**
     * Quickly resets the object space by invalidating the current space.
     */
    protected static void quickClearObjects() {
        objectSpace = new boolean[objectSpace.length];
        objectDrawSpace = -1;
    }

    /**
     * Sets up the current object space.
     * @param maxObjects Maximum amount of objects in the current space.
     */
    protected static void init(int maxObjects) {
        objectSpace = new boolean[maxObjects];
        objectMap = new float[maxObjects * OBJECTSIZE * OBJECTWIDTH];
        mainBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferData(GL_ARRAY_BUFFER, objectMap, GL_DYNAMIC_DRAW);
        SEIShaders.createPointer();
    }
}
