package engine;

import java.util.ArrayList;
import static engine.SEEngine.*;
import java.util.Arrays;

public class SERLogic {
    private SERLogic() {}
    
    //2 component Range structure
    public static class Range {
        int start, count;
        public Range(int s, int c) { start = s; count = c; }
        public Range() {}
        @Override public boolean equals(Object a) { Range ar = (Range)a; return start == ar.start && count == ar.count; }
    }
    
    //2D Data storing structure
    //Data at coordinate x, y is stored at "x + y * width" in the float[] data
    //The length of float[] data may not always reflect width * height
    //For example, a specific x, y coordinate needs to store 4 components
    public static class Data {
        public float[] data; public int width, height;
        public Data(float[] dat, int w, int h) { data = dat; width = w; height = h; }
        public Data(){}
        @Override public boolean equals(Object a) { Data ar = (Data)a;  return width == ar.width && height == ar.height && Arrays.equals(data, ar.data); }
    }
    
    //Sorry for my awful description:
    //Keeps track of time passing using the System.currentTimeMillis(); Create
    //one and call hasBeen() method with with an amount of time in milliseconds
    //(time). If it has been time time since the last time hasBeen returned
    //true (or if it has never returned true, since the initilization of the
    //object), hasBeen returns true again.
    public static class Alarm {
        long lastRing = 0;
        boolean accountForDelay = false;
        public Alarm() { lastRing = System.currentTimeMillis(); }
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
    
    //Matrix Multiplication Function
    public static Data multiplyMatrices(Data mata, Data matb) {
        Data mat = new Data();
        if (mata.width != matb.height) { System.out.println("Incompatible Matrices!"); return null; }
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
    
    //Matrix Generation Functions
    public static Data genScaleMatrix(float x, float y) { return new Data(new float[]{x,0,0,y}, 2, 2); }
    public static Data genRotationMatrix(float a) {
        return new Data(new float[]{(float)Math.cos(a),
        (float)-Math.sin(a),(float)Math.sin(a),
        (float)Math.cos(a)}, 2, 2); }
    public static Data genIdentityMatrix() { return new Data(new float[]{1,0,0,1}, 2, 2); }
    
    //Linked List Access Functions
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
