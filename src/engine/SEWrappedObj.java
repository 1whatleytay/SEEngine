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
/**
 * A wrapper adding extra functionality to normal objects.
 * Make sure {@link engine.SEEngine#SEwrappedObjects} is enabled or else wrapped objects will not have an effect.
 * @author desgroup
 * @version SEAlpha2a
 */
public class SEWrappedObj {
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
    protected ArrayList<String> offsetNames = new ArrayList<>();

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
     * Pointer to where this particular object is found in the array of all registered pointers.
     */
    protected int pointerForDepth = -1;

    /**
     * Gets a particular wrapped object.
     * Modifications may be made.
     * @param obj The index of the object to grab.
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
}