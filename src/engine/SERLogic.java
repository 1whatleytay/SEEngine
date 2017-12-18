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

import java.util.*;

import static engine.SEConstants.*;

/**
 * Contains basic structures and some basic logic processing functions.
 * @author desgroup
 * @version SEAlpha4a
 */
public class SERLogic {
    private SERLogic() {}
    
    /**
     * Returns a path to a specified resource in the project.
     * @param assetPath The path to the resource in the project.
     * @return A path to the runtime location of the resource or null if nothing is found.
     */
    public static String asset(String assetPath) { try { return SERLogic.class.getResource("/" + assetPath).getPath(); } catch (Exception e) { return null; } }

    /**
     * Stores an array of floats along with a width and height for 2D storage.
     */
    public static class Data {

        /**
         * The core data of the object.
         */
        public float[] data;

        /**
         * The width of the data object.
         */
        public int width;

        /**
         * The height of the data object.
         */
        public int height;

        /**
         * Creates a new Data object with a specified data array, width and height.
         * @param dat The core data of the newly created object.
         * @param w The width of the newly created object.
         * @param h The height of the newly created object.
         */
        public Data(float[] dat, int w, int h) { data = dat; width = w; height = h; }
        /**
         * Blank initializer for Data.
         */
        public Data(){}
        
        @Override public boolean equals(Object a) { Data ar = (Data)a;  return width == ar.width && height == ar.height && Arrays.equals(data, ar.data); }
    }
    
    /**
     * Keeps track of small periods of time since creation.
     */
    public static class Alarm {
        long lastRing = 0;
        boolean accountForDelay = false;

        /**
         * Creates a new Alarm object with the last triggered time set to the current moment.
         */
        public Alarm() { lastRing = System.currentTimeMillis(); }

        /**
         * Returns true if it has been time milliseconds since the last trigger, false otherwise.
         * If true is returned, the alarm is triggered again.
         * @param time Time (in milliseconds) you wish to query.
         * @return True if the last trigger happened at least time milliseconds ago.
         */
        public boolean hasBeen(long time) {
            boolean result = false;
            if (lastRing + time <= System.currentTimeMillis()) {
                if (accountForDelay) lastRing += time;
                else lastRing = System.currentTimeMillis();
                result = true;
            }
            return result;
        }
    }
    
    /**
     * Returns true if the point x, y is within the box at mx, my with a width of mw and a height of mh.
     * @param x The x coordinate of the point.
     * @param y The y coordinate of the point.
     * @param mx The x coordinate of the test region.
     * @param my The y coordinate of the test region.
     * @param mw The width of the test region.
     * @param mh The height of the test region.
     * @return True if the point is within the test region.
     */
    public static boolean isPointColliding(int x, int y, int mx, int my, int mw, int mh) {
        return x > mx && x < mx + mw && y > my && y < my + mh;
    }

    private static boolean isSBoxColliding(float x, float y, float w, float h, float mx, float my, float mw, float mh) {
        return ((x > mx && x < mx + mw) || (x + w > mx && x + w < mx + mw)) && ((y > my && y < my + mh) || (y + h > my && y + h < my + mh));
    }

    /**
     * Returns true if the box x, y, w, h is colliding with the box mx, my, mw, mh.
     * @param x The x coordinate of the first box.
     * @param y The y coordinate of the first box.
     * @param w The width of the first box.
     * @param h The height of the first box.
     * @param mx The x coordinate of the test region.
     * @param my The y coordinate of the test region.
     * @param mw The width of the test region.
     * @param mh The height of the test region.
     * @return True if the box is colliding with the test region.
     */
    public static boolean isBoxColliding(float x, float y, float w, float h, float mx, float my, float mw, float mh) {
        return isSBoxColliding(x, y, w, h, mx, my, mw, mh) || isSBoxColliding(mx, my, mw, mh, x, y, w, h);
    }

    /**
     * Multiplies matrices mata and matb.
     * If mata and matb are incompatible, null will be returned.
     * @param mata A Data structure containing the first matrix.
     * @param matb A Data structure containing the second matrix.
     * @return The product of mata and matb.
     */
    public static Data multiplyMatrices(Data mata, Data matb) {
        Data mat = new Data();
        if (mata.width != matb.height) { SEEngine.log(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_INCOMPATIBLE_MATRICES); return null; }
        mat.width = matb.width; mat.height = mata.height;
        mat.data = new float[mat.width * mat.height];
        for (int x = 0; x < mat.width; x++) {
            for (int y = 0; y < mat.height; y++) {
                float[] rowa = new float[mata.width];
                float[] colb = new float[matb.height];
                for (int a = 0; a < mata.width; a++) rowa[a] = mata.data[a + y * mata.width];
                for (int a = 0; a < matb.height; a++) colb[a] = matb.data[x + a * matb.width];
                float finalValue = 0;
                for (int a = 0; a < rowa.length; a++) finalValue += rowa[a] * colb[a];
                mat.data[x + y * mat.width] = finalValue;
            }
        }
        return mat;
    }
    
    /**
     * Generates a scale matrix using the provided scale factors.
     * @param x The scale factor in the x direction.
     * @param y The scale factor in the y direction.
     * @return A 2x2 Data structure that can apply the two scale factors provided.
     */
    public static Data genScaleMatrix(float x, float y) { return new Data(new float[]{x,0,0,y}, 2, 2); }

    /**
     * Generates a rotation matrix that rotates the final vertex by a degrees.
     * @param a The amount of degrees for the matrix to rotate by.
     * @return A 2x2 Data structure that can apply a rotation of a degrees.
     */
    public static Data genRotationMatrix(float a) {
        a = a * (float)Math.PI / (float)180;
        return new Data(new float[]{(float)Math.cos(a),
        (float)-Math.sin(a),(float)Math.sin(a),
        (float)Math.cos(a)}, 2, 2); }

    /**
     * Returns a 2x2 Data structure that contains a identity matrix.
     * @return A 2x2 Data structure that contains a identity matrix.
     */
    public static Data genIdentityMatrix() { return new Data(new float[]{1,0,0,1}, 2, 2); }

    /**
     * Finds a point in the linked list and returns the place in the actual list.
     * Probably buggy.
     * @param in A list of pointers to represent the order of the linked list.
     * @param first The first element in the linked list.
     * @param elm The position in the linked list you wish to find.
     * @return The array position where elm is located.
     */
    public static int find(ArrayList<Integer> in, int first, int elm) { // elm: pointer to somewhere in the linked list
        if (elm < 0) return -1;
        Integer head = first;
        for (int a = 0; a < elm; a++) {
            head = in.get(head);
            if (head == null || head == -1) {
                head = -1;
                break;
            }
        }
        return head;
    }

    /**
     * Undos {@link engine.SERLogic#find(java.util.ArrayList, int, int)}.
     * Probably buggy.
     * @param in A list of pointers to represent the order of the linked list.
     * @param first The first element in the linked list.
     * @param elm The position in the array you wish to find in the linked list.
     * @return The linked list position where elm is located.
     */
    public static int unFind(ArrayList<Integer> in, int first, int elm) { // elm: pointer to somewhere in the array
        int a;
        int head = first;
        for (a = 0; a < in.size(); a++) {
            if (head == elm) break;
            head = in.get(head);
        }
        return a;
    }
    private static final boolean doWierdZeroBasedStuff = false;

    /**
     * Moves an element in an array list by changing in.
     * Probably buggy.
     * @param in The linked list pointers and also the output.
     * @param first The first element in the array list in a array with one element.
     * @param elm The element in the linked list you wish to move.
     * @param loc The location to move element to.
     */
    public static void moveTo(ArrayList<Integer> in, int[] first, int elm, int loc) {
        if (doWierdZeroBasedStuff) { loc++; elm++; } //For some reason, elm and loc aren't zero based.
        //loc++ and elm++ make it zero based. Don't question until bugs happen.
        int actElm = find(in, first[0], elm);
        int lConnectActElm = in.get(actElm);
        int actElmMinusOne = find(in, first[0], elm - 1);
        in.set(actElm, find(in, first[0], loc));
        int locMinusOne = find(in, first[0], loc - 1);
        if (locMinusOne == -1) first[0] = actElm;
        else in.set(locMinusOne, actElm);
        if (actElmMinusOne == -1) first[0] = elm;
        else in.set(actElmMinusOne, lConnectActElm);
    }
}
