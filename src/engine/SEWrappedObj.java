package engine;

import java.util.*;

//The SEWrappedObj class contains multiple objects. With it's power, you can
//create offsets that apply to certain groups of objects, rotate or scale the
//objects within, or change the overall depth of the objects compared to other
//SEWrappedObj structures. To use these, make sure SEuseWrappedObjects is
//enabled. Otherwise, the changed done here will be ignored.
public class SEWrappedObj {
    //The objects that are currently wrapped.
    protected SEObj[] objs;
    //The draw ranges to use with glMultiDrawArrays(int, int[], int[]).
    protected int[] drawRangesStart, drawRangesCount;
    //The name of the current offset active for the SEWrappedObj. Set with
    //SEbindOffset(SEWrappedObj).
    protected String offsetName;
    //A matrix that can rotate or scale the wrapped objects. Set with calls to
    //SEresetMatrix, SErotateMatrix, SEscaleMatrix and SEcustomMatrix.
    protected SERLogic.Data matrix;
    //Determines if the object in matrixCenter will be used as the matrix center
    //of the SEWrappedObj or not.
    protected boolean useObjectForMatrixCenter;
    //The object that might be responsible for the matrix center. Set with
    //SEmatrixCenter(SEWrappedObj, SEObj).
    protected SEObj matrixCenter;
    //The numbers, in pixels that might be responsible for the matrix center.
    //Set with SEmatrixCenter(SEWrappedObj, int, int)
    protected int matrixCenterX, matrixCenterY;
    //A value that may be used to speed up calls to SEdepth if
    //SEexperimentalDepth is enabled.
    protected int pointerForDepth = -1;
    //Gets the object at obj.
    public SEObj getObject(int obj) { return objs[obj]; }
    //Gets the array of SEObj that are wrapped.
    public SEObj[] getObjects() { return Arrays.copyOf(objs, objs.length); }
    //Generates the draw ranges that will actually draw the wrapped objects.
    //Called once the wrapped objects have changed or once it's created.
    protected void genDrawRanges() {
        int maxLength = 0;
        for (SEObj obj : objs) { if (obj.object > maxLength) maxLength = obj.object; }
        boolean[] objsSpace = new boolean[maxLength + 1];
        for (SEObj obj : objs) { objsSpace[obj.object] = true; }
        ArrayList<SERLogic.Range> ranges = new ArrayList<>();
        boolean isNewRange = false;
        SERLogic.Range cRange = new SERLogic.Range();
        for (int a = 0; a < objsSpace.length; a++) {
            if (!isNewRange) {
                if (objsSpace[a]) {
                    cRange = new SERLogic.Range();
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
        SERLogic.Range[] drawRanges = new SERLogic.Range[ranges.size()];
        ranges.toArray(drawRanges);
        drawRangesStart = new int[drawRanges.length]; drawRangesCount = new int[drawRanges.length];
        for (int a = 0; a < drawRanges.length; a++) {
            drawRangesStart[a] = drawRanges[a].start * 4; drawRangesCount[a] = drawRanges[a].count * 4;
        }
    }
}