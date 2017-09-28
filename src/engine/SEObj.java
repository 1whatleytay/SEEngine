/*
 * SEEngine OpenGL 2.0 Engine
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

/**
 * Stores some information for an object on screen.
 * To create one, use {@link engine.SEObjects#SEcreateObject(int, int, int, int, SETex)}.
 * @author desgroup
 * @version SEAlpha2a
 */
public class SEObj {

    /**
     * Hidden basic constructor.
     */
    protected SEObj() {}

    /**
     * A hidden blank object used with {@link engine.SEObjects#SEvisible(SEObj, boolean)}.
     * May have a different {@link engine.SEObj#object} value but should be otherwise blank.
     */
    protected static SEObj blank = new SEObj();
    
    /**
     * Value is true if the object is has been hidden and is currently hidden with {@link engine.SEObjects#SEvisible(SEObj, boolean)}.
     */
    protected boolean isHidden = false;
    /**
     * Value is true if the object has been wrapped with {@link engine.SEObjects#SEwrapObjects(SEObj[])} or has been created wrapped.
     */
    protected boolean isWrapped = false;

    /**
     * Location in the OpenGL buffer {@link engine.SEObjects#mainBuffer} where the object data hides itself.
     */
    protected int object = 0;
    
    //Perform any changes to the variables x, y, w, h and tex, then call
    //SEojSave(SEObj) to save your changes.
    
    //Coordinates in pixels on screen...

    public int
            /**
            * The x coordinate of the object (in pixels).
            * If you make any changes, be sure to save them with {@link engine.SEObjects#SEobjSave(SEObj)}.
            */
            x = 0,
            /**
            * The y coordinate of the object (in pixels).
            * If you make any changes, be sure to save them with {@link engine.SEObjects#SEobjSave(SEObj)}.
            */
            y = 0,
            /**
            * The width of the object (in pixels).
            * If you make any changes, be sure to save them with {@link engine.SEObjects#SEobjSave(SEObj)}.
            */
            w = 0,
            /**
            * The height of the object (in pixels).
            * If you make any changes, be sure to save them with {@link engine.SEObjects#SEobjSave(SEObj)}.
            */
            h = 0;

    /**
     * The texture the object will show over itself.
     * The texture will be stretched to meet the width and height of the object.
     * If you make any changes, be sure to save them with {@link engine.SEObjects#SEobjSave(SEObj)}.
     */
    public SETex tex = new SETex();
    
    //The coordinate in pixels of the center of the object in pixels. Updates
    //after a call to SEobjSave(SEObj) or genCenter().

    public int
            /**
            * The x coordinate of the center of the object.
            * Call {@link engine.SEObj#genCenter()} to update both centerX and centerY.
            */
            centerX = 0,
            /**
            * The y coordinate of the center of the object.
            * Call {@link engine.SEObj#genCenter()} to update both centerX and centerY.
            */
            centerY = 0;

    /**
     * Updates the {@link engine.SEObj#centerX} and {@link engine.SEObj#centerY} variables.
     */
    public void genCenter() { centerX = x + w / 2; centerY = y + h / 2; }
    //Returns the point to this object's entry in OpenGL.

    /**
     * Returns {@link engine.SEObj#object}.
     * @return The pointer to the object data in the OpenGL buffer {@link engine.SEObjects#mainBuffer}.
     * @deprecated
     */
    public int getObjectName() { return object; }
}
