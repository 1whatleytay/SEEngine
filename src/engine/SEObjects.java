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

import static org.lwjgl.opengl.GL15.*;

import java.util.*;

import static engine.SEConstants.*;

/**
 *  Handles creating and editing objects.
 * Use static methods in this class to add, move and edit {@link engine.SEObj} and {@link engine.SEWrappedObj}
 * @author desgroup
 * @version SEAlpha3a
 */
public class SEObjects {
    private SEObjects() {}
    
    private static final int OBJECTSIZE = 4;
    private static final int OBJECTWIDTH = 4;
    private static boolean[] objectSpace = null;
    private static float[] objectMap = null;

    /**
     * The main OpenGL buffer containing all Object Data.
     */
    protected static int mainBuffer = -1;
    
    /**
     * Counts how far the furthest object is in {@link engine.SEObjects#mainBuffer}.
     * The engine will only draw as far as this drawSpace.
     * Is not used if {@link engine.SEEngine#SEwrappedObjects} is enabled.
     */
    public static int objectDrawSpace = -1;
    
    //Stores all the known wrapped objects along with a linked list for depth
    //if SEwrappedObjects and SEwrappedObjectDepth is enabled, respectively.

    /**
     * Stores all unique {@link engine.SEWrappedObj} to draw later.
     */
    protected static ArrayList<SEWrappedObj> knownObjects = new ArrayList<>();

    /**
     * Provides linked list functionality if {@link engine.SEEngine#SEwrappedObjectDepth} is enabled.
     * Stores the next object in order in reference to {@link engine.SEObjects#knownObjects}.
     */
    protected static ArrayList<Integer> next = new ArrayList<>();

    /**
     * Provides linked list functionality if {@link engine.SEEngine#SEwrappedObjectDepth} is enabled.
     * Stores the first element in reference to {@link engine.SEObjects#knownObjects}.
     */
    protected static int first[] = {-1};
    
    /**
     * Returns how many unique {@link engine.SEWrappedObj} are registered.
     * @return The amount of unique, registered {@link engine.SEWrappedObj}.
     */
    public static int SEgetWrappedObjectCount() { return knownObjects.size(); }
    
    private static int[] currentOffset = {0, 0};
    private static String currentOffsetName = ORIGIN_OFFSET;
    //All the offsets orginised by names.

    /**
     * A HashMap containing every offset name (key) and every offset value (value).
     */
    protected static HashMap<String, int[]> offsets = new HashMap<>();

    /**
     * Changes the current offset to newOffset.
     * The default offset is equal to {@link engine.SEConstants#ORIGIN_OFFSET}.
     * @param newOffset The offset name to be changed to.
     */
    public static void SEswapOffsets(String newOffset) {
        offsets.put(currentOffsetName, currentOffset); //Is this even needed? (I think we might need it)
        currentOffset = offsets.get(newOffset);
        if (currentOffset == null) { currentOffset = new int[]{0, 0}; offsets.put(newOffset, currentOffset); }
        currentOffsetName = newOffset;
    }

    /**
     * Returns the current offset name.
     * @return The current offset name.
     */
    public static String SEcurrentOffsetName() { return currentOffsetName; }
    //Moves the current offset's x by either xMov or mov[0] and it's y by either
    //yMov or mov[1] in pixels.

    /**
     * Shifts the current offset by xMov, yMov pixels.
     * @param xMov The amount (in pixels) to shift the offset by in the x direction.
     * @param yMov The amount (in pixels) to shift the offset by in the y direction.
     */
    public static void SEmoveOffset(int xMov, int yMov) { currentOffset[0] += xMov; currentOffset[1] += yMov; }

    /**
     * Array version of {@link engine.SEObjects#SEmoveOffset(int, int)}.
     * @param mov An array containing the amount (in pixels) to shift the current offset by in the x and y directions through the first and second array element, respectively.
     */
    public static void SEmoveOffset(int[] mov) { SEmoveOffset(mov[0], mov[1]); }
    //Sets the current offset to either xOffset, yOffset or offset[0], offset[1]
    //in pixels.

    /**
     * Changes the current offset to xOffset, yOffset pixels.
     * @param xOffset The new x value the offset will be set to (in pixels).
     * @param yOffset The new y value the offset will be set to (in pixels).
     */
    public static void SEsetOffset(int xOffset, int yOffset) { currentOffset[0] = xOffset; currentOffset[1] = yOffset; }

    /**
     * Array version of {@link engine.SEObjects#SEsetOffset(int, int)}.
     * @param offset An array containing the new x and y values (in pixels) for the current offset through the first and second array element, respectively.
     */
    public static void SEsetOffset(int[] offset) { currentOffset[0] = offset[0]; currentOffset[1] = offset[1]; }

    /**
     * Returns an array to the current offset's value.
     * Modifications may be made to this array and will be reflected in the offset.
     * @return An array containing the current offset value.
     */
    public static int[] SEgetOffset() { return currentOffset; }

    /**
     * Attaches the current offset to objs.
     * Multiple offsets may be bound to one {@link engine.SEWrappedObj}.
     * Once an offset is bound, all objects wrapped within the SEWrappedObj will be moved by the offset value.
     * You may bind the same offset twice or more, which will have the effect of amplifying the offset bound.
     * To remove an offset, use {@link engine.SEObjects#SEunbindOffset(SEWrappedObj)}.
     * @param objs The {@link engine.SEWrappedObj} to bind the offset to.
     */
    public static void SEbindOffset(SEWrappedObj objs) { objs.offsetNames.add(currentOffsetName); }
    
    private static boolean hasWarnedPointlessUnbindOffset = false;
    
    /**
     * Removes the current offset from objs.
     * Only removes one instance of the offset from the {@link engine.SEWrappedObj}.
     * Afterwards, the offset name will have less of an effect (if there are multiple instances of the same offset) or no effect.
     * @param objs The {@link engine.SEWrappedObj} to remove the offset from.
     */
    public static void SEunbindOffset(SEWrappedObj objs) {
        int findInstance = -1;
        for (int a = 0; a < objs.offsetNames.size(); a++)
            if (objs.offsetNames.get(a).equals(currentOffsetName)) { findInstance = a; break; }
        if (findInstance == -1 && !hasWarnedPointlessUnbindOffset) {
            SEEngine.log(MSG_TYPE_OPT, MSG_UNBIND_OFFSET_WARNING);
            hasWarnedPointlessUnbindOffset = true;
            return;
        }
        objs.offsetNames.remove(findInstance);
    }
    
    /**
     * The offset to make current on a call to {@link engine.SEObjects#SEstashOffset()}.
     */
    public static String lastStash = ORIGIN_OFFSET;

    /**
     * Sets the current offset to newOffset and stores the last offset to {@link engine.SEObjects#lastStash}.
     * @param newOffset The new offset to be set as the current offset.
     */
    public static void SEstashOffset(String newOffset) { lastStash = currentOffsetName; SEswapOffsets(newOffset); }

    /**
     * Restores the last offset changed by a call to {@link engine.SEObjects#SEstashOffset(String)}.
     */
    public static void SEstashOffset() { SEswapOffsets(lastStash); }

    /**
     * Establishes the current offset in the shader.
     */
    protected static void fixOffsets() { SEIShaders.offset(currentOffset[0], currentOffset[1]); }

    /**
     * Resets the matrix in objs to an identity matrix.
     * @param objs The {@link engine.SEWrappedObj} to have it's matrix reset.
     */
    public static void SEresetMatrix(SEWrappedObj objs) { objs.matrix = SERLogic.genIdentityMatrix(); }

    /**
     * Adds a rotation matrix of deg degrees to objs.
     * @param objs The {@link engine.SEWrappedObj} to have it's matrix changed.
     * @param deg The degree of the rotation.
     */
    public static void SErotateMatrix(SEWrappedObj objs, float deg) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, SERLogic.genRotationMatrix(deg)); }

    /**
     * Adds a scale matrix by scaleX in the x axis and scaleY in the y axis to objs.
     * @param objs The {@link engine.SEWrappedObj} to have it's matrix changed.
     * @param scaleX The scale to be applied in the x direction.
     * @param scaleY The scale to be applied in the y direction.
     */
    public static void SEscaleMatrix(SEWrappedObj objs, float scaleX, float scaleY) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, SERLogic.genScaleMatrix(scaleX, scaleY)); }

    /**
     * Adds a custom (Data 2x2) Matrix to the object objs.
     * @param objs The {@link engine.SEWrappedObj} to have it's matrix changed.
     * @param matrix The matrix to apply to objs.
     */
    public static void SEcustomMatrix(SEWrappedObj objs, SERLogic.Data matrix) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, matrix); }

    /**
     * Changes the matrix center of objs to x, y on screen.
     * @param objs The object to have it's matrix center set.
     * @param x The x position (in pixels) relative to the top left of the screen to have the matrix center set.
     * @param y The y position (in pixels) relative to the top left of the screen to have the matrix center set.
     */
    public static void SEmatrixCenter(SEWrappedObj objs, int x, int y) { objs.useObjectForMatrixCenter = false; objs.matrixCenterX = x; objs.matrixCenterY = y; }

    /**
     * Changes the matrix center of objs to a specific center of the object obj.
     * If the object center changes, the matrix center will follow.
     * @param objs The object to have it's matrix center set.
     * @param obj The object who's center will be used as a matrix center.
     */
    public static void SEmatrixCenter(SEWrappedObj objs, SEObj obj) { objs.useObjectForMatrixCenter = true; objs.matrixCenter = obj; }
    
    private static float[] SEobjNData(SEObj obj) {
        float[] newObjData = {
            ((float)obj.x / SEEngine.scWidth * 2 - 1) * ampX, ((float)obj.y / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?obj.tex.texW:0)), (float)(obj.tex.texY + (ampY==-1?0:obj.tex.texH)),
            ((float)(obj.x + obj.w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)obj.y / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?0:obj.tex.texW)), (float)(obj.tex.texY + (ampY==-1?0:obj.tex.texH)),
            ((float)(obj.x + obj.w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)(obj.y + obj.h) / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?0:obj.tex.texW)), (float)(obj.tex.texY + (ampY==-1?obj.tex.texH:0)),
            ((float)obj.x / SEEngine.scWidth * 2 - 1) * ampX, ((float)(obj.y + obj.h) / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?obj.tex.texW:0)), (float)(obj.tex.texY + (ampY==-1?obj.tex.texH:0)),
        };
        return newObjData;
    }
    
    private static void objSave(SEObj obj) {
        obj.genCenter();
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferSubData(GL_ARRAY_BUFFER, obj.object * OBJECTSIZE * OBJECTWIDTH * OBJECTWIDTH, SEobjNData(obj));
    }

    /**
     * Saves all changes done to the {@link engine.SEObj} obj.
     * @param obj The object that will have it's changes updated.
     */
    public static void SEobjSave(SEObj obj) {
        if (!obj.isHidden) objSave(obj);
    }
    
    /**
     * Changes the position of the {@link engine.SEObj} obj to x, y.
     * @param obj The object to have it's position changed.
     * @param x The x position (in pixels) where the object will be moved to.
     * @param y The y position (in pixels) where the object will be moved to.
     */
    public static void SEobjPos(SEObj obj, int x, int y) { obj.x = x; obj.y = y; SEobjSave(obj); }

    /**
     * Changes the size of the {@link engine.SEObj} obj to w width and h height.
     * @param obj The object to have it's size changed.
     * @param w The new width (in pixels) of the object.
     * @param h The new height (in pixels) of the object.
     */
    public static void SEobjSize(SEObj obj, int w, int h) { obj.w = w; obj.h = h; SEobjSave(obj); }

    /**
     * Changes the texture of the {@link engine.SEObj} obj to tex.
     * @param obj The object to have it's texture changed.
     * @param tex The new texture of the object.
     */
    public static void SEobjTex(SEObj obj, SETex tex) { obj.tex = tex; SEobjSave(obj); }

    /**
     * Changes all x, y, width and height params of the {@link engine.SEObj} obj at once.
     * @param obj The object to apply the changes to.
     * @param x The new x position (in pixels) of the object.
     * @param y The new y position (in pixels) of the object.
     * @param w The new width (in pixels) of the object.
     * @param h The new height (in pixels) of the object.
     */
    public static void SEobjData(SEObj obj, int x, int y, int w, int h) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; SEobjSave(obj); }

    /**
     * Changes all the x, y, width, height and texture of the {@link engine.SEObj} obj at once.
     * @param obj The object to apply the changes to.
     * @param x The new x position (in pixels) of the object.
     * @param y The new y position (in pixels) of the object.
     * @param w The new width (in pixels) of the object.
     * @param h The new height (in pixels) of the object.
     * @param tex The new texture of the object.
     */
    public static void SEobjData(SEObj obj, int x, int y, int w, int h, SETex tex) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex; SEobjSave(obj); }

    /**
     * Moves the {@link engine.SEObj} obj by x (pixels) in the x axis and y (pixels) in the y axis.
     * @param obj The object to be moved.
     * @param x The amount (in pixels) that the object will be moved on the x axis.
     * @param y The amount (in pixels) that the object will be moved on the y axis.
     */
    public static void SEobjMove(SEObj obj, int x, int y) { obj.x += x; obj.y += y; SEobjSave(obj); }
    
    /**
     * Changes the visibility of the {@link engine.SEObj} obj to value.
     * If value is false, the object will be hidden.
     * If the value is true, the object will become visible.
     * Any changes went unsaved by {@link engine.SEObjects#SEobjSave(engine.SEObj)} will automatically be saved when made visible.
     * @param obj The object to have it's visibility changed.
     * @param value The visibility level. True for visible, false for invisible.
     */
    public static void SEvisible(SEObj obj, boolean value) {
        obj.isHidden = value;
        if (value) {
            SEObj.blank.object = obj.object;
            objSave(SEObj.blank);
        } else objSave(obj);
    }
    
    /**
     * A function to run when an event occurs within an {@link engine.SEObjects.SEButtonBundle}.
     * Passed to {@link engine.SEObjects#SEcreateButton(int, int, int, int, SEButtonFunction)} to create a {@link engine.SEObjects.SEButtonBundle}.
     */
    public static interface SEButtonFunction {
        /**
         * This function will be called when a message is need to be sent to the respective SEButtonBundle.
         * @param obj The {@link engine.SEObjects.SEButtonBundle} that originated the call.
         * @param action The action that occurred within the {@link engine.SEObjects.SEButtonBundle}. One of the MOUSE_ constants.
         */
        void func(SEButton obj, byte action); }

    /**
     * Represents a region where mouse input is being listened to.
     */
    public static class SEButton {
        public int
                /**
                 * The x position (relative to the top of the window, in pixels) where the region exists.
                 */
                x,
                /**
                 * The y position (relative to the top of the window, in pixels) where the region exists.
                 */
                y,
                /**
                 * The width (in pixels) of the region.
                 */
                w,
                /**
                 * The height (in pixels) of the region.
                 */
                h; 

        /**
         * The function to be called when an event (one of the MOUSE_ constants) have occurred.
         */
        public SEButtonFunction func;
        
        protected boolean mouseOver = false;
    }
    
    /**
     * A list of every {@link engine.SEObjects.SEButtonBundle} that exists.
     */
    protected static ArrayList<SEButton> buttons = new ArrayList<>();
    
    /**
     * Creates a new {@link engine.SEObjects.SEButtonBundle} at x, y with a size of w width and h height and a message function of func.
     * @param x The x position (relative to the top of the window, in pixels) where the area starts.
     * @param y The x position (relative to the top of the window, in pixels) where the area starts.
     * @param w The width of the area.
     * @param h The height of the area.
     * @param func The message function of the area. Will be called when one of the MOUSE_ constants occur.
     * @return A representation of the area. Changes made to the object will be reflected in the area.
     */
    public static SEButton SEcreateButton(int x, int y, int w, int h, SEButtonFunction func) {
        SEButton bundle = new SEButton();
        bundle.x = x; bundle.y = y; bundle.w = w; bundle.h = h;
        bundle.func = func;
        buttons.add(bundle);
        return bundle;
    }

    /**
     * Object version of {@link engine.SEObjects#SEcreateButton(int, int, int, int, SEButtonFunction)}.
     * @param obj The object to have it's properties carried to the {@link engine.SEObjects.SEButtonBundle}. A change to the object will not update the bundle.
     * @param func The message function of the area. Will be called when one of the MOUSE_ constant occur.
     * @return A representation of the area. Changes made to the object will be reflected in the area.
     */
    public static SEButton SEcreateButton(SEObj obj, SEButtonFunction func) {
        return SEcreateButton(obj.x, obj.y, obj.w, obj.h, func);
    }
    
    /**
     * Gets the current and variable depth of objs.
     * @param objs The object to query the depth of.
     * @return The current depth of objs.
     */
    public static int SEgetDepth(SEWrappedObj objs) { return SERLogic.unFind(next, first[0], objs.pointerForDepth); }
    
    /**
     * Changes the depth of an {@link engine.SEWrappedObj}.
     * Changes will not be reflected if {@link engine.SEEngine#SEwrappedObjectDepth} is disabled.
     * The object that already exists in that spot will be pushed upwards.
     * Normal objects cannot have their depth changed, however, you can wrap them and change their depth.
     * To tell the variable and current depth of an {@link engine.SEWrappedObj} use {@link engine.SEObjects#SEgetDepth(SEWrappedObj)}.
     * @param obj The wrapped object to have it's depth changed.
     * @param depth The depth of the object. Cannot be negative and must be less than {@link engine.SEObjects#SEgetWrappedObjectCount()}, 0 being the bottom of the stack and {@link engine.SEObjects#SEgetWrappedObjectCount()} - 1 being the top.
     */
    public static void SEdepth(SEWrappedObj obj, int depth) {
        if (obj == null || obj.pointerForDepth == -1) { SEEngine.log(MSG_TYPE_FAIL, MSG_UNKNOWN_WRAPPED_OBJECT); return; }
        SERLogic.moveTo(next, first, SERLogic.unFind(next, first[0], obj.pointerForDepth), depth);
    }

    /**
     * Returns the maximum amount of unique {@link engine.SEObj} able to exist at one time.
     * Should be the same as your {@link engine.SEProgram} {@link engine.SEProgramData#maxObjects} value.
     * @return The maximum amount of unique {@link engine.SEObj} able to exist at one time.
     */
    public static int SEmaxObjectCount() { return objectSpace.length; }

    /**
     * Returns an array of {@link engine.SEObj} with it's only element being obj.
     * @param obj The one element to be returned as an array.
     * @return An array of {@link engine.SEObj} with it's only element being obj.
     */
    public static SEObj[] SEsingle(SEObj obj) { return new SEObj[]{obj}; }

    /**
     * Wraps and returns the {@link engine.SEWrappedObj} that represents the wrap.
     * Enable {@link engine.SEEngine#SEwrappedObjects} to be sure the wrapper has an effect.
     * @param objs The objects to be wrapped.
     * @return A wrapped containing the multiple {@link engine.SEObj} in objs.
     */
    public static SEWrappedObj SEwrapObjects(SEObj[] objs) {
        if (objs.length == 0) SEEngine.log(MSG_TYPE_OPT, MSG_EMPTY_WRAPPER_CREATED);
        if (!SEEngine.SEdoubleWrappedObjects) {
            for (SEObj obj : objs)
                if (obj.isWrapped) { SEEngine.log(MSG_TYPE_FAIL, MSG_ALREADY_WRAPPED); return null; }
            for (SEObj obj : objs) obj.isWrapped = true;
        }
        SEWrappedObj wObjs = new SEWrappedObj();
        wObjs.matrix = SERLogic.genIdentityMatrix();
        if (!SEEngine.SEpreventBindOriginOffset) wObjs.offsetNames.add(ORIGIN_OFFSET);
        wObjs.objs = objs;
        wObjs.genDrawRanges();
        wObjs.pointerForDepth = knownObjects.size();
        knownObjects.add(wObjs);
        if (SEEngine.SEwrappedObjectDepth) {
            if (next.size() + 1 != knownObjects.size()) { SEEngine.log(MSG_TYPE_OPT_FUNC, MSG_MISSING_DEPTH_INFO); }
            if (next.isEmpty()) {
                next.add(-1);
                first[0] = knownObjects.size() - 1;
            } else {
                next.add(-1);
                next.set(SERLogic.find(next, first[0], next.size() - 2), next.size() - 1);
            }
        }
        return wObjs;
    }

    /**
     * Creates count objects at position x, y and dimensions w width and h height all with texture tex and all wrapped in the returned {@link engine.SEWrappedObj}.
     * Each object will be created with the same position, size and texture.
     * Object parameters can be changed by getting the object from {@link engine.SEWrappedObj#getObject(int)}.
     * @param count The amount of objects to create and wrap.
     * @param x The x position (in pixels) of each object.
     * @param y The y position (in pixels) of each object.
     * @param w The width (in pixels) of each object.
     * @param h The height (in pixels) of each object.
     * @param tex The texture of each object.
     * @return A {@link engine.SEWrappedObj} containing count objects, all with the same params specified through x, y, w, h and tex.
     */
    public static SEWrappedObj SEcreateWrappedObjects(int count, int x, int y, int w, int h, SETex tex) {
        SEObj[] objs = new SEObj[count];
        for (int a = 0; a < objs.length; a++) {
            objs[a] = SEcreateObject(x, y, w, h, tex);
        }
        return SEwrapObjects(objs);
    }
    
    //Creates an object at x, y on screen (in pixels) with the width and height
    //of w, h (in pixels) and a texture of tex. To gather a texture, try looking
    //into SEloadTexture(String|Data) or just use BLANK_TEXTURE.

    /**
     * Creates an object at x, y, with w width and h height and a texture of tex and returns a reference.
     * If modifications are made to the return objects, be sure to save them with {@link engine.SEObjects#SEobjSave(SEObj)}.
     * @param x The x position (in pixels) of the object.
     * @param y The y position (in pixels) of the object.
     * @param w The width (in pixels) of the object.
     * @param h The height (in pixels) of the object.
     * @param tex The texture of the object.
     * @return A reference to the created object.
     */
    public static SEObj SEcreateObject(int x, int y, int w, int h, SETex tex) {
        SEObj obj = new SEObj();
        boolean found = false;
        int find = -1;
        for (int a = 0; a < objectSpace.length; a++)  if (!objectSpace[a]) { find = a; found = true; break; }
        if (!found) { SEEngine.log(MSG_TYPE_OPT, MSG_OUT_OF_OBJECT_MEMORY); }
        objectSpace[find] = true;
        obj.object = find; obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex;
        objectDrawSpace = Math.max(objectDrawSpace, find + 1); // Let's try this. I didn't really think about this.
        SEobjSave(obj);
        return obj;
    }
    
    /**
     * Wrapped version of {@link engine.SEObjects#SEcreateObject(int, int, int, int, SETex)}.
     * The created object will be created with a parent wrapper.
     * @param x The x position (in pixels) of the object.
     * @param y The y position (in pixels) of the object.
     * @param w The width (in pixels) of the object.
     * @param h The height (in pixels) of the object.
     * @param tex The texture of the object.
     * @return A reference to the created object.
     */
    public static SEObj SEcreateObjectW(int x, int y, int w, int h, SETex tex) {
        SEObj obj = SEcreateObject(x, y, w, h, tex);
        SEwrapObjects(SEsingle(obj));
        return obj;
    }
    
    /**
     * Renamed version of {@link engine.SEObjects#SEcreateObjectW(int, int, int, int, SETex)}.
     * @param x The x position (in pixels) of the object.
     * @param y The y position (in pixels) of the object.
     * @param w The width (in pixels) of the object.
     * @param h The height (in pixels) of the object.
     * @param tex The texture of the object.
     * @return A reference to the created object.
     */
    public static SEObj SEcreateObjectWrapped(int x, int y, int w, int h, SETex tex) {
        return SEcreateObjectW(x, y, w, h, tex);
    }
    
    //Duplicates and returns the object obj into a seperately movable object.

    /**
     * Duplicates the {@link engine.SEObj} obj and returns the duplicate.
     * @param obj The object to duplicate.
     * @return The duplicated {@link engine.SEObj} obj.
     */
    public static SEObj SEduplicateObject(SEObj obj) {
        SEObj newObj = SEcreateObject(obj.x, obj.y, obj.w, obj.h, obj.tex);
        return newObj;
    }

    /**
     * Deletes the {@link engine.SEObj} obj.
     * The object will no longer draw and it's data will be freed.
     * {@link engine.SEObjects#SEcollapseObjectDrawSpace()} will be called if {@link engine.SEEngine#SEcollapseObjectDrawSpaceOnDeletion} is enabled.
     * @param obj The object to be deleted.
     */
    public static void SEdeleteObject(SEObj obj) {
        objectSpace[obj.object] = false;
        SEobjData(obj, 0, 0, 0, 0, SETextures.BLANK_TEXTURE);
        if (SEEngine.SEcollapseObjectDrawSpaceOnDeletion) SEcollapseObjectDrawSpace();
        else if (obj.object == objectDrawSpace - 1) objectDrawSpace--;
    }
    
    /**
     * Deletes a wrapped object.
     * Does not delete the objects within, simply removes the wrapper from the draw call.
     * @param obj The wrapped object to delete.
     */
    public static void SEdeleteWrappedObject(SEWrappedObj obj) { knownObjects.set(obj.pointerForDepth, null); }
    
    /**
     * Lowers {@link engine.SEObjects#objectDrawSpace} to the lowest value without losing functionality.
     */
    public static void SEcollapseObjectDrawSpace() {
        for (int a = objectDrawSpace - 1; a >= 0; a--) {
            if (objectSpace[a]) { objectDrawSpace = a + 1; break; }
            if (a == 0) objectDrawSpace = 0;
        }
    }
    
    //Determines what direction x+ and y+ will go.

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
    public static void SEdirection(byte direction) {
        switch (direction) {
            case DIRECTION_BOTTOM_TO_TOP: ampY = 1; break;
            case DIRECTION_TOP_TO_BOTTOM: ampY = -1; break;
            case DIRECTION_LEFT_TO_RIGHT: ampX = 1; break;
            case DIRECTION_RIGHT_TO_LEFT: ampX = -1; break;
            default: break;
        }
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
        knownObjects = new ArrayList<>();
        next = new ArrayList<>();
        first = new int[]{-1};
        currentOffsetName = ORIGIN_OFFSET;
        currentOffset = new int[]{0, 0};
        offsets = new HashMap<>();
        offsets.put(currentOffsetName, currentOffset);
        objectDrawSpace = -1;
    }

    /**
     * Quickly resets the object space by invalidating the current space.
     */
    protected static void quickClearObjects() {
        objectSpace = new boolean[objectSpace.length];
        knownObjects = new ArrayList<>();
        next = new ArrayList<>();
        first = new int[]{-1};
        currentOffsetName = ORIGIN_OFFSET;
        currentOffset = new int[]{0, 0};
        offsets = new HashMap<>();
        offsets.put(currentOffsetName, currentOffset);
        objectDrawSpace = -1;
    }
    
    /**
     * Sets up the current object space.
     * @param maxObjects Maximum amount of objects in the current space.
     */
    protected static void loadObjects(int maxObjects) {
        objectSpace = new boolean[maxObjects];
        objectMap = new float[maxObjects * OBJECTSIZE * OBJECTWIDTH];
        mainBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferData(GL_ARRAY_BUFFER, objectMap, GL_DYNAMIC_DRAW);
        SEIShaders.createPointer();
        offsets.put(currentOffsetName, currentOffset);
    }
}
