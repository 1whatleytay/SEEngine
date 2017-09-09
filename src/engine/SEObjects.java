package engine;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.*;

import java.nio.*;

import java.util.*;

import static engine.SEProgramData.*;

//Handles everything objects. SEObj, SEWrappedObj and all their parameters are
//handled and modified here.
public class SEObjects {
    private SEObjects() {}
    
    //Vertex count of one object.
    private static final int OBJECTSIZE = 4;
    //Size of one vertex.
    private static final int OBJECTWIDTH = 4;
    //Stores spaces in memory where objects occupy and where objects don't.
    private static boolean[] objectSpace = null;
    //Stores the informtion on every object on the system.
    private static float[] objectMap = null; 
    //The OpenGL buffer containing the draw data of all the objects.
    protected static int mainBuffer = -1;
    //Counts how far OpenGL has to draw to be sure to draw every object. Not
    //used with SEuseWrappedObjects enabled.
    public static int objectDrawSpace = -1;
    
    //Stores all the known wrapped objects along with a linked list for depth
    //if SEuseWrappedObjects and SEwrappedObjectDepth is enabled, respectively.
    protected static ArrayList<SEWrappedObj> knownObjects = new ArrayList<>();
    protected static ArrayList<Integer> next = new ArrayList<>();
    protected static int first[] = {-1};
    
    //Gathers the amount of active Wrapped Objects.
    public static int SEgetWrappedObjectCount() { return knownObjects.size(); }
    
    //Offset bound by default. Just in case you bound something without swapping
    //offsets and need a name to refer to it by.
    public static final String ORIGIN_OFFSET = "offset1";
    //The array that represents the current offset.
    private static int[] currentOffset = {0, 0};
    //The name of the current offset.
    private static String currentOffsetName = ORIGIN_OFFSET;
    //All the offsets orginised by names.
    protected static HashMap<String, int[]> offsets = new HashMap<>();
    
    //Swaps the current offset with the offset name in newOffset. If the offset
    //newOffset is not created, it will create it for you.
    //Bug? Odd jump to center maybe when moving offsets...
    public static void SEswapOffsets(String newOffset) {
        offsets.put(currentOffsetName, currentOffset); //Is this even needed? (I think we might need it)
        currentOffset = offsets.get(newOffset);
        if (currentOffset == null) { currentOffset = new int[]{0, 0}; offsets.put(newOffset, currentOffset); }
        currentOffsetName = newOffset;
    }
    //Teturns the name of the current offset.
    public static String SEcurrentOffsetName() { return currentOffsetName; }
    //Moves the current offset's x by either xMov or mov[0] and it's y by either
    //yMov or mov[1] in pixels.
    public static void SEmoveOffset(int xMov, int yMov) { currentOffset[0] += xMov; currentOffset[1] += yMov; }
    public static void SEmoveOffset(int[] mov) { currentOffset[0] += mov[0]; currentOffset[1] += mov[1]; }
    //Sets the current offset to either xOffset, yOffset or offset[0], offset[1]
    //in pixels.
    public static void SEsetOffset(int xOffset, int yOffset) { currentOffset[0] = xOffset; currentOffset[1] = yOffset; }
    public static void SEsetOffset(int[] offset) { currentOffset[0] = offset[0]; currentOffset[1] = offset[1]; }
    //Returns the current value of the current offset.
    public static int[] SEgetOffset() { return currentOffset; }
    //Attaches the current offset to objs. Now, you can change that offset or
    //even swap to different offsets, but objs will always only be offset by the
    //current offset at the time of this call. If an offset is already attached,
    //the new one will take it's place.
    public static void SEbindOffset(SEWrappedObj objs) { objs.offsetNames.add(currentOffsetName); }
    
    private static boolean hasWarnedPointlessUnbindOffset = false;
    
    public static void SEunbindOffset(SEWrappedObj objs) {
        int findInstance = -1;
        for (int a = 0; a < objs.offsetNames.size(); a++)
            if (objs.offsetNames.get(a).equals(currentOffsetName)) { findInstance = a; break; }
        if (findInstance == -1 && !hasWarnedPointlessUnbindOffset) {
            SEEngine.log(MSG_OPT, "An attempt was made to unbind offset" + currentOffsetName + "on a wrapped object where the offset was not bound.");
            hasWarnedPointlessUnbindOffset = true;
            return;
        }
        objs.offsetNames.remove(findInstance);
    }
    
    public static String lastStash = ORIGIN_OFFSET;
    public static void SEstashOffset(String newOffset) { lastStash = currentOffsetName; SEswapOffsets(newOffset); }
    public static void SEstashOffset() { SEswapOffsets(lastStash); }
    
    //Uses the current offset in any OpenGL draw calls to come.
    protected static void fixOffsets() { SEIShaders.offset(currentOffset[0], currentOffset[1]); }
    
    //Resets the matrix in objs to an identity matrix (aka does nothing).
    public static void SEresetMatrix(SEWrappedObj objs) { objs.matrix = SERLogic.genIdentityMatrix(); }
    //Rotates objs by deg degrees.
    public static void SErotateMatrix(SEWrappedObj objs, float deg) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, SERLogic.genRotationMatrix(deg)); }
    //Scales objs's x by scaleX and y by scaleY
    public static void SEscaleMatrix(SEWrappedObj objs, float scaleX, float scaleY) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, SERLogic.genScaleMatrix(scaleX, scaleY)); }
    //Adds a custom 2x2 matrix transformation to objs's matrix. matrix's width
    //and height must be 2 and it's data must be the length of 2 * 2.
    public static void SEcustomMatrix(SEWrappedObj objs, SERLogic.Data matrix) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, matrix); }
    //Changes the matrix center of objs to a point on screen (eg. rotation
    //origin).
    public static void SEmatrixCenter(SEWrappedObj objs, int x, int y) { objs.useObjectForMatrixCenter = false; objs.matrixCenterX = x; objs.matrixCenterY = y; }
    //Changes the matrix center of objs to the center of an object (eg. rotation
    //origin).
    public static void SEmatrixCenter(SEWrappedObj objs, SEObj obj) { objs.useObjectForMatrixCenter = true; objs.matrixCenter = obj; }
    
    //A function to create object data that OpenGL can read.
    private static float[] SEobjNData(SEObj obj) {
        float[] newObjData = {
            ((float)obj.x / SEEngine.scWidth * 2 - 1) * ampX, ((float)obj.y / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?obj.tex.texW:0)), (float)(obj.tex.texY + (ampY==-1?0:obj.tex.texH)),
            ((float)(obj.x + obj.w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)obj.y / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?0:obj.tex.texW)), (float)(obj.tex.texY + (ampY==-1?0:obj.tex.texH)),
            ((float)(obj.x + obj.w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)(obj.y + obj.h) / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?0:obj.tex.texW)), (float)(obj.tex.texY + (ampY==-1?obj.tex.texH:0)),
            ((float)obj.x / SEEngine.scWidth * 2 - 1) * ampX, ((float)(obj.y + obj.h) / SEEngine.scHeight * 2 - 1) * ampY, (float)(obj.tex.texX + (ampX==-1?obj.tex.texW:0)), (float)(obj.tex.texY + (ampY==-1?obj.tex.texH:0)),
        };
        return newObjData;
    }
    
    //Saves changes to the object obj. You can change obj's x, y, width, height
    //and texture then call SEobjSave to have all those changes go into effect.
    //This solution is often faster than calling one of the
    //SEobjPos/Move/Size/Tex/Data functions.
    public static void SEobjSave(SEObj obj) {
        obj.genCenter();
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferSubData(GL_ARRAY_BUFFER, obj.object * OBJECTSIZE * OBJECTWIDTH * OBJECTWIDTH, SEobjNData(obj));
    }
    //Sets the position of object obj to x, y
    public static void SEobjPos(SEObj obj, int x, int y) { obj.x = x; obj.y = y; SEobjSave(obj); }
    //Sets the size of object obj to w width and h height
    public static void SEobjSize(SEObj obj, int w, int h) { obj.w = w; obj.h = h; SEobjSave(obj); }
    //Changes the texture being used for object obj
    public static void SEobjTex(SEObj obj, SETex tex) { obj.tex = tex; SEobjSave(obj); }
    //Changes all x, y, width and height for object obj, in that order of
    //parameters.
    public static void SEobjData(SEObj obj, int x, int y, int w, int h) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; SEobjSave(obj); }
    //A similar function to the last SEobjData however this has a texture
    //parameter too.
    public static void SEobjData(SEObj obj, int x, int y, int w, int h, SETex tex) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex; SEobjSave(obj); }
    //Moves the object obj x pixels on the x axis and y pixels on the y axis.
    public static void SEobjMove(SEObj obj, int x, int y) { obj.x += x; obj.y += y; SEobjSave(obj); }
    
    //Changes the depth of an SEWrappedObj. The object previously occupying that
    //depth will be pushed upwards. depth must be more than 0 and less than
    //the value of SEgetWrappedObjectCount().
    public static void SEdepth(SEWrappedObj obj, int depth) {
        int find = -1;
        if (SEEngine.SEexperimentalDepth) find = obj.pointerForDepth;
        else for (int a = 0; a < knownObjects.size(); a++) {
            if (knownObjects.get(a) == obj) {
                find = a;
                break;
            }
        }
        if (find == -1 || obj == null) {
            SEEngine.log(MSG_FAIL, "Unable to find SEWrappedObj to change depth.");
            return;
        }
        SERLogic.moveTo(next, first, SERLogic.unFind(next, first[0], find), depth);
    }
    
    public static int SEgetDepth(SEWrappedObj objs) {
        int obj = -1;
        if (SEEngine.SEexperimentalDepth) obj = objs.pointerForDepth;
        else for (int a = 0; a < knownObjects.size(); a++) {
            if (knownObjects.get(a) == objs) {
                obj = a;
                break;
            }
        }
        return SERLogic.unFind(next, first[0], obj);
    }
    
    //The same as your program's SEProgramData's maxObjects value. The maximum
    //amount of availible objects.
    public static int SEmaxObjectCount() { return objectSpace.length; }
    
    //Just returns an array with object obj as it's only element. Use intended
    //with SEwrapObjects(SEObj[])
    public static SEObj[] SEsingle(SEObj obj) { return new SEObj[]{obj}; }
    
    //Returns a wrapper (SEWrappedObj) containing the objects in objs.
    //Please be sure to enable SEuseWrappedObject for this function to have a
    //visible effect.
    public static SEWrappedObj SEwrapObjects(SEObj[] objs) {
        if (objs.length == 0) SEEngine.log(MSG_OPT, "An empty wrapper was created.");
        if (!SEEngine.SEdoubleWrappedObjects) {
            for (SEObj obj : objs)
                if (obj.isWrapped) { SEEngine.log(MSG_FAIL, "Yikes! Object " + obj.object + " is already wrapped!"); return null; }
            for (SEObj obj : objs) obj.isWrapped = true;
        }
        SEWrappedObj wObjs = new SEWrappedObj();
        wObjs.matrix = SERLogic.genIdentityMatrix();
        if (!SEEngine.SEdoNotBindOriginOffset) wObjs.offsetNames.add(ORIGIN_OFFSET);
        wObjs.objs = objs;
        wObjs.genDrawRanges();
        if (SEEngine.SEexperimentalDepth) wObjs.pointerForDepth = knownObjects.size();
        knownObjects.add(wObjs);
        if (SEEngine.SEwrappedObjectDepth) {
            if (next.size() + 1 != knownObjects.size()) { SEEngine.log(MSG_OPT_FUNC, "Yikes! SEwrappedObjectDepth was changed unexpectedly. Unexpected behavior may occur."); }
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
    
    //Returns a wrapped of count objects with at the location of x, y with the
    //size of w, h and the texture of tex.
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
    public static SEObj SEcreateObject(int x, int y, int w, int h, SETex tex) {
        SEObj obj = new SEObj();
        boolean found = false;
        int find = -1;
        for (int a = 0; a < objectSpace.length; a++)  if (!objectSpace[a]) { find = a; found = true; break; }
        if (!found) { SEEngine.log(MSG_OPT, "Out of Object Memory"); }
        objectSpace[find] = true;
        obj.object = find; obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex;
        objectDrawSpace = Math.max(objectDrawSpace, find) + 1; // This math is incorrect, the +1 will always increment
        SEobjSave(obj);
        return obj;
    }
    
    public static SEObj SEcreateObjectW(int x, int y, int w, int h, SETex tex) {
        SEObj obj = SEcreateObject(x, y, w, h, tex);
        SEwrapObjects(SEsingle(obj));
        return obj;
    }
    
    public static SEObj SEcreateObjectWrapped(int x, int y, int w, int h, SETex tex) {
        return SEcreateObjectW(x, y, w, h, tex);
    }
    
    //Duplicates and returns the object obj into a seperately movable object.
    public static SEObj SEduplicateObject(SEObj obj) {
        SEObj newObj = SEcreateObject(obj.x, obj.y, obj.w, obj.h, obj.tex);
        return newObj;
    }
    
    //Deletes an object, clears it's space in the buffer and frees up the space
    //for more objects to be created. If you're running low on your object
    //budget, be sure to call this function on some older unused objects.
    public static void SEdeleteObject(SEObj obj) {
        objectSpace[obj.object] = false;
        SEobjData(obj, 0, 0, 0, 0, SETextures.BLANK_TEXTURE);
        if (SEEngine.SEcollapseObjectDrawSpaceOnDeletion) SEcollapseObjectDrawSpace();
        else if (obj.object == objectDrawSpace - 1) objectDrawSpace--;
    }
    
    //If any deleted objects pushed back the drawspace so that it's now drawing
    //some old deleted objects, this function will do it's best to fix that.
    public static void SEcollapseObjectDrawSpace() {
        for (int a = objectDrawSpace - 1; a >= 0; a--) {
            if (objectSpace[a]) { objectDrawSpace = a + 1; break; }
            if (a == 0) objectDrawSpace = 0;
        }
    }
    
    //Determines what direction x+ and y+ will go.
    protected static byte ampX = 1;
    protected static byte ampY = 1;
    
    public static final byte
            //y+ will go downwards
            TOP_TO_BOTTOM = 0x30,
            //y+ will go upwards
            BOTTOM_TO_TOP = 0x31,
            //x+ will go to the right
            LEFT_TO_RIGHT = 0x32,
            //x+ will go to the left
            RIGHT_TO_LEFT = 0x33;
    
    //Applies one of the above TOP_TO_BOTTOM, BOTTOM_TO_TOP, LEFT_TO_RIGHT or
    //RIGHT_TO_LEFT. By default, BOTTOM_TO_TOP and LEFT_TO_RIGHT will be
    //active. This applies to all objects.
    public static void SEdirection(byte direction) {
        switch (direction) {
            case BOTTOM_TO_TOP: ampY = 1; break;
            case TOP_TO_BOTTOM: ampY = -1; break;
            case LEFT_TO_RIGHT: ampX = 1; break;
            case RIGHT_TO_LEFT: ampX = -1; break;
            default: break;
        }
    }
    
    //Fully clears all object data.
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
    
    //Invalidates object data for a quick clear.
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
    
    //Setup function for SEObjects
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
