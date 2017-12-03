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

import java.util.*;

import static engine.SEConstants.*;

/**
 * A wrapper adding extra functionality to normal objects.
 * Make sure {@link engine.SEEngine#SEuseWrappedObjects} is enabled or else wrapped objects will not have an effect.
 * @author desgroup
 * @version SEAlpha4a
 */
public class SEWrappedObj {

    private SEWrappedObj() {}

    /**
     * Stores all unique {@link engine.SEWrappedObj} to draw later.
     */
    protected static ArrayList<SEWrappedObj> knownObjects = new ArrayList<>();

    /**
     * Provides linked list functionality if {@link engine.SEEngine#SEuseWrappedObjectDepth} is enabled.
     * Stores the next object in order in reference to {@link engine.SEWrappedObj#knownObjects}.
     */
    protected static ArrayList<Integer> next = new ArrayList<>();

    /**
     * Provides linked list functionality if {@link engine.SEEngine#SEuseWrappedObjectDepth} is enabled.
     * Stores the first element in reference to {@link engine.SEWrappedObj#knownObjects}.
     */
    protected static int first[] = {-1};

    /**
     * Returns how many unique {@link engine.SEWrappedObj} are registered.
     * @return The amount of unique, registered {@link engine.SEWrappedObj}.
     */
    public static int SEgetWrappedObjectCount() { return knownObjects.size(); }

    /**
     * Deletes all wrapped objects.
     * The entire draw buffer is cleared.
     */
    public static void SEclearWrappedObjects() {
        knownObjects.clear();
        next.clear();
        first = new int[]{-1};
    }

    /**
     * All contained objects. 
     */
    protected SEObj[] objs;

    /**
     * Start of a current draw range.
     */
    protected int[] drawRangesStart;

    /**
     * Amount of objects in a current draw range.
     */
    protected int[] drawRangesCount;

    /**
     * All offsets attached to this wrapped object.
     */
    protected ArrayList<SEOffset> offsets = new ArrayList<>();

    /**
     * The current matrix attached to this wrapped object.
     */
    protected SERLogic.Data matrix;

    /**
     * Determines if {@link engine.SEWrappedObj#matrixCenter} (value of true) or {@link engine.SEWrappedObj#matrixCenterX}, {@link engine.SEWrappedObj#matrixCenterY} (value of false) will be used as a matrix center.
     */
    protected boolean useObjectForMatrixCenter;

    /**
     * An object to be used as a matrix center if {@link engine.SEWrappedObj#useObjectForMatrixCenter} is true.
     */
    protected SEObj matrixCenter;

    /**
     * An x coordinate to be used as a matrix center if {@link engine.SEWrappedObj#useObjectForMatrixCenter} is false.
     */
    protected int matrixCenterX;
    /**
     * A y coordinate to be used as a matrix center if {@link engine.SEWrappedObj#useObjectForMatrixCenter} is false.
     */
    protected int matrixCenterY;

    /**
     * Pointer to where this particular wrap object is found [][][].
     */
    protected int pointer = -1;

    /**
     * Gets a particular wrapped object.
     * Modifications may be made.
     * @param obj The index of the object to return.
     * @return The object at index obj.
     */
    public SEObj getObject(int obj) { return objs[obj]; }

    /**
     * Gets a copy of all the objects in this wrapped object.
     * Modifications may be made to the objects.
     * @return An array containing all the wrapped objects.
     */
    public SEObj[] getObjects() { return Arrays.copyOf(objs, objs.length); }

    /**
     * Generates draw ranges for the current setup of objects.
     */
    protected void genDrawRanges() {
        class Range { int start, count; }
        int maxLength = 0;
        for (SEObj obj : objs) { if (obj.object > maxLength) maxLength = obj.object; }
        boolean[] objsSpace = new boolean[maxLength + 1];
        for (SEObj obj : objs) { objsSpace[obj.object] = true; }
        ArrayList<Range> ranges = new ArrayList<>();
        boolean isNewRange = false;
        Range cRange = new Range();
        for (int a = 0; a < objsSpace.length; a++) {
            if (!isNewRange) {
                if (objsSpace[a]) {
                    cRange = new Range();
                    cRange.start = a;
                    isNewRange = true;
                }
            }
            if (isNewRange) {
                if (objsSpace[a]) {
                    cRange.count++;
                    if (a == objsSpace.length - 1) ranges.add(cRange);
                } else {
                    ranges.add(cRange);
                    isNewRange = false;
                }
            }
        }
        Range[] drawRanges = new Range[ranges.size()];
        ranges.toArray(drawRanges);
        drawRangesStart = new int[drawRanges.length]; drawRangesCount = new int[drawRanges.length];
        for (int a = 0; a < drawRanges.length; a++) {
            drawRangesStart[a] = drawRanges[a].start * 4; drawRangesCount[a] = drawRanges[a].count * 4;
        }
    }

    static private SEObj[] createObjects(int count, int x, int y, int w, int h, SETex tex) {
        SEObj[] objs = new SEObj[count];
        for (int a = 0; a < objs.length; a++) {
            objs[a] = new SEObj(x, y, w, h, tex);
        }
        return objs;
    }

    /**
     * Wrapping constructor.
     * Enable {@link engine.SEEngine#SEuseWrappedObjects} to be sure the wrapper has an effect.
     * @param O The objects to be wrapped.
     */
    public SEWrappedObj(SEObj[] O) {
        if (O.length == 0) SEEngine.log(SEMessageType.MSG_TYPE_OPT, SEMessage.MSG_EMPTY_WRAPPER_CREATED);
        if (!SEEngine.SEdoubleWrappedObjects) {
            for (SEObj obj : O)
                if (obj.isWrapped) { SEEngine.log(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_ALREADY_WRAPPED); return; }
            for (SEObj obj : O) obj.isWrapped = true;
        }
        matrix = SERLogic.genIdentityMatrix();
        if (!SEEngine.SEpreventBindOriginOffset) offsets.add(ORIGIN_OFFSET);
        objs = O;
        genDrawRanges();
        pointer = knownObjects.size();
        knownObjects.add(this);
        if (SEEngine.SEuseWrappedObjectDepth) {
            if (next.size() + 1 != knownObjects.size()) { SEEngine.log(SEMessageType.MSG_TYPE_OPT_FUNC, SEMessage.MSG_MISSING_DEPTH_INFO); }
            if (next.isEmpty()) {
                next.add(-1);
                first[0] = knownObjects.size() - 1;
            } else {
                next.add(-1);
                next.set(SERLogic.find(next, first[0], next.size() - 2), next.size() - 1);
            }
        }
    }

    /**
     * Tight constructor.
     * @param count The amount of objects to create and wrap.
     * @param X The x position (in pixels) of each object.
     * @param Y The y position (in pixels) of each object.
     * @param W The width (in pixels) of each object.
     * @param H The height (in pixels) of each object.
     * @param T The texture of each object.
     */
    public SEWrappedObj(int count, int X, int Y, int W, int H, SETex T) {
        this(createObjects(count, X, Y, W, H, T));
    }

    /**
     * Resets the matrix in objs to an identity matrix.
     */
    public void resetMatrix() { matrix = SERLogic.genIdentityMatrix(); }

    /**
     * Adds a rotation matrix of deg degrees to objs.
     * @param deg The degree of the rotation.
     */
    public void rotateMatrix(float deg) { matrix = SERLogic.multiplyMatrices(matrix, SERLogic.genRotationMatrix(deg)); }

    /**
     * Adds a scale matrix by scaleX in the x axis and scaleY in the y axis to objs.
     * @param scaleX The scale to be applied in the x direction.
     * @param scaleY The scale to be applied in the y direction.
     */
    public void scaleMatrix(float scaleX, float scaleY) { matrix = SERLogic.multiplyMatrices(matrix, SERLogic.genScaleMatrix(scaleX, scaleY)); }

    /**
     * Adds a custom (Data 2x2) Matrix to the object objs.
     * @param M The matrix to apply to objs.
     */
    public void customMatrix(SERLogic.Data M) { matrix = SERLogic.multiplyMatrices(matrix, M); }

    /**
     * Changes the matrix center of objs to x, y on screen.
     * @param x The x position (in pixels) relative to the top left of the screen to have the matrix center set.
     * @param y The y position (in pixels) relative to the top left of the screen to have the matrix center set.
     */
    public void matrixCenter(int x, int y) { useObjectForMatrixCenter = false; matrixCenterX = x; matrixCenterY = y; }

    /**
     * Changes the matrix center of objs to a specific center of the object obj.
     * If the object center changes, the matrix center will follow.
     * @param obj The object who's center will be used as a matrix center.
     */
    public void matrixCenter(SEObj obj) { useObjectForMatrixCenter = true; matrixCenter = obj; }

    /**
     * Gets the current and variable depth of this wrapped object.
     * @return The current depth of objs.
     */
    public int getDepth() { return SERLogic.unFind(next, first[0], pointer); }

    /**
     * Changes the depth of this wrapped object.
     * Changes will not be reflected if {@link engine.SEEngine#SEuseWrappedObjectDepth} is disabled.
     * If a wrapped object already exists at this depth level, it will be pushed upwards.
     * The depth of a wrapped object might change over the course of an application.
     * To tell the current depth value of this wrapped object, use {@link engine.SEWrappedObj#getDepth()}.
     * @param depth The depth of the object. Cannot be negative and must be less than {@link SEWrappedObj#SEgetWrappedObjectCount()}, 0 being the bottom of the stack and {@link engine.SEWrappedObj#SEgetWrappedObjectCount()} - 1 being the top.
     */
    public void depth(int depth) {
        if (pointer == -1) { SEEngine.log(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_UNKNOWN_WRAPPED_OBJECT); return; } // Do we need this?
        SERLogic.moveTo(next, first, SERLogic.unFind(next, first[0], pointer), depth);
    }

    /**
     * Attaches the offset offset to this wrapped object.
     * Multiple offsets may be bound this wrapped object.
     * Once an offset is bound, all objects wrapped within the SEWrappedObj will be moved by the offset value.
     * You may bind the same offset twice or more, which will have the effect of amplifying the offset bound.
     * To remove an offset, use {@link engine.SEWrappedObj#unbind(SEOffset)}.
     * @param offset The {@link engine.SEWrappedObj} to bind the offset to.
     */
    public void bind(SEOffset offset) { offsets.add(offset); }

    private static boolean hasWarnedPointlessUnbindOffset = false;

    /**
     * Removes the offset offset from this wrapped object.
     * Only removes one instance of the offset, other instances will be ignored.
     * @param offset The offset to remove.
     */
    public void unbind(SEOffset offset) {
        int findInstance = -1;
        for (int a = 0; a < offsets.size(); a++)
            if (offsets.get(a).equals(offset)) { findInstance = a; break; }
        if (findInstance == -1 && !hasWarnedPointlessUnbindOffset) {
            SEEngine.log(SEMessageType.MSG_TYPE_OPT, SEMessage.MSG_UNBIND_OFFSET_WARNING);
            hasWarnedPointlessUnbindOffset = true;
            return;
        }
        offsets.remove(findInstance);
    }

    /**
     * Deletes a wrapped object.
     * Does not delete the objects within the wrapped object, simply removes the wrapper from the draw call.
     * Repeatedly deleting wrapped objects can cause slowdown.
     */
    public void delete() { knownObjects.set(pointer, null); }

    /**
     * Content version of {@link engine.SEWrappedObj#delete()}.
     * @param contents If true, the contents of this wrapped object will be deleted as well.
     */
    public void delete(boolean contents) {
        if (contents) for (SEObj obj : objs) obj.delete();
        delete();
    }
}