package engine;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.*;

import java.nio.*;

import java.util.*;

public class SEObjects {
    private SEObjects() {}
    
    private static final int OBJECTSIZE = 4;
    private static final int OBJECTWIDTH = 4;
    private static boolean[] objectSpace = null;
    private static float[] objectMap = null; 
    protected static int mainBuffer = -1;
    public static int objectDrawSpace = -1;
    
    public static ArrayList<SEWrappedObj> knownObjects = new ArrayList<>();
    
    public static final String ORIGIN_OFFSET = "offset1";
    private static int[] currentOffset = {0, 0};
    private static String currentOffsetName = ORIGIN_OFFSET;
    protected static HashMap<String, int[]> offsets = new HashMap<>();
    
    public static void SEswapOffsets(String newOffset) {
        offsets.put(currentOffsetName, currentOffset);
        currentOffset = offsets.get(newOffset);
        if (currentOffset == null) { currentOffset = new int[]{0, 0}; offsets.put(newOffset, currentOffset); }
        currentOffsetName = newOffset;
    }
    public static String SEcurrentOffsetName() { return currentOffsetName; }
    public static void SEmoveOffset(int xMov, int yMov) { currentOffset[0] += xMov; currentOffset[1] += yMov; }
    public static void SEmoveOffset(int[] mov) { currentOffset[0] += mov[0]; currentOffset[1] += mov[1]; }
    public static void SEsetOffset(int xOffset, int yOffset) { currentOffset[0] = xOffset; currentOffset[1] = yOffset; }
    public static void SEsetOffset(int[] offset) { currentOffset[0] = offset[0]; currentOffset[1] = offset[1]; }
    public static int[] SEgetOffset() { return currentOffset; }
    public static void SEbindOffset(SEWrappedObj objs) { objs.offsetName = currentOffsetName; }
    
    protected static void fixOffsets() { SEIShaders.offset(currentOffset[0], currentOffset[1]); }
    
    public static void SEresetMatrix(SEWrappedObj objs) { objs.matrix = SERLogic.genIdentityMatrix(); }
    public static void SErotateMatrix(SEWrappedObj objs, float deg) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, SERLogic.genRotationMatrix(deg)); }
    public static void SEscaleMatrix(SEWrappedObj objs, float scaleX, float scaleY) { objs.matrix = SERLogic.multiplyMatrices(objs.matrix, SERLogic.genScaleMatrix(scaleX, scaleY)); }
    
    public static float[] SEobjNData(SEObj obj) {
        float[] newObjData = {
            ((float)obj.x / SEEngine.scWidth * 2 - 1) * ampX, ((float)obj.y / SEEngine.scHeight * 2 - 1) * ampY, obj.tex.texX + (ampX==-1?obj.tex.texW:0), obj.tex.texY + (ampY==-1?0:obj.tex.texH),
            ((float)(obj.x + obj.w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)obj.y / SEEngine.scHeight * 2 - 1) * ampY, obj.tex.texX + (ampX==-1?0:obj.tex.texW), obj.tex.texY + (ampY==-1?0:obj.tex.texH),
            ((float)(obj.x + obj.w) / SEEngine.scWidth * 2 - 1) * ampX, ((float)(obj.y + obj.h) / SEEngine.scHeight * 2 - 1) * ampY, obj.tex.texX + (ampX==-1?0:obj.tex.texW), obj.tex.texY + (ampY==-1?obj.tex.texH:0),
            ((float)obj.x / SEEngine.scWidth * 2 - 1) * ampX, ((float)(obj.y + obj.h) / SEEngine.scHeight * 2 - 1) * ampY, obj.tex.texX + (ampX==-1?obj.tex.texW:0), obj.tex.texY + (ampY==-1?obj.tex.texH:0),
        };
        return newObjData;
    }
    
    public static void SEobjSave(SEObj obj) {
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferSubData(GL_ARRAY_BUFFER, obj.object * OBJECTSIZE * OBJECTWIDTH * OBJECTWIDTH, SEobjNData(obj));
    }
    public static void SEobjPos(SEObj obj, int x, int y) { obj.x = x; obj.y = y; SEobjSave(obj); }
    public static void SEobjSize(SEObj obj, int w, int h) { obj.w = w; obj.h = h; SEobjSave(obj); }
    public static void SEobjTex(SEObj obj, SETex tex) { obj.tex = tex; SEobjSave(obj); }
    public static void SEobjData(SEObj obj, int x, int y, int w, int h) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; SEobjSave(obj); }
    public static void SEobjData(SEObj obj, int x, int y, int w, int h, SETex tex) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex; SEobjSave(obj); }
    public static void SEobjMove(SEObj obj, int x, int y) { obj.x += x; obj.y += y; SEobjSave(obj); }
    
    public static int SEmaxObjectCount() { return objectSpace.length; }
    
    public static SEObj[] SEsingle(SEObj obj) { return new SEObj[]{obj}; }
    
    public static SEWrappedObj SEwrapObjects(SEObj[] objs) {
        SEWrappedObj wObjs = new SEWrappedObj();
        wObjs.matrix = SERLogic.genIdentityMatrix();
        wObjs.offsetName = ORIGIN_OFFSET;
        wObjs.objs = objs;
        wObjs.genDrawRanges();
        knownObjects.add(wObjs);
        return wObjs;
    }
    
    public static SEWrappedObj SEcreateWrappedObjects(int count, int x, int y, int w, int h, SETex tex) {
        SEObj[] objs = new SEObj[count];
        for (int a = 0; a < objs.length; a++) {
            objs[a] = SEcreateObject(x, y, w, h, tex);
        }
        return SEwrapObjects(objs);
    }
    
    public static SEObj SEcreateObject(int x, int y, int w, int h, SETex tex) {
        SEObj obj = new SEObj();
        boolean found = false;
        int find = -1;
        for (int a = 0; a < objectSpace.length; a++)  if (!objectSpace[a]) { find = a; found = true; break; }
        if (!found) { SEEngine.log("Out of Object Memory"); }
        objectSpace[find] = true;
        obj.object = find; obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex;
        objectDrawSpace = Math.max(objectDrawSpace, find) + 1;
        SEobjSave(obj);
        return obj;
    }
    
    public static SEObj SEduplicateObject(SEObj obj) {
        SEObj newObj = SEcreateObject(obj.x, obj.y, obj.w, obj.h, obj.tex);
        return newObj;
    }
    
    public static void SEdeleteObject(SEObj obj) {
        objectSpace[obj.object] = false;
        SEobjData(obj, 0, 0, 0, 0, SETextures.BLANK_TEXTURE);
        if (SEEngine.SEcollapseObjectDrawSpaceOnDeletion) SEcollapseObjectDrawSpace();
        else if (obj.object == objectDrawSpace - 1) objectDrawSpace--;
    }
    
    public static void SEcollapseObjectDrawSpace() {
        for (int a = objectDrawSpace - 1; a >= 0; a--) {
            if (objectSpace[a]) { objectDrawSpace = a + 1; break; }
            if (a == 0) objectDrawSpace = 0;
        }
    }
    
    protected static byte ampX = 1;
    protected static byte ampY = 1;
    
    public static final byte
            SE_TOP_TO_BOTTOM = 0x30,
            SE_BOTTOM_TO_TOP = 0x31,
            SE_LEFT_TO_RIGHT = 0x32,
            SE_RIGHT_TO_LEFT = 0x33;
    
    public static void SEdirection(byte direction) {
        switch (direction) {
            case SE_BOTTOM_TO_TOP: ampY = 1; break;
            case SE_TOP_TO_BOTTOM: ampY = -1; break;
            case SE_LEFT_TO_RIGHT: ampX = 1; break;
            case SE_RIGHT_TO_LEFT: ampX = -1; break;
        }
    }
    
    protected static void loadObjects(int maxObjects) {
        objectSpace = new boolean[maxObjects];
        objectMap = new float[maxObjects * OBJECTSIZE * OBJECTWIDTH];
        mainBuffer = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, mainBuffer);
        glBufferData(GL_ARRAY_BUFFER, objectMap, GL_STATIC_DRAW);
        SEIShaders.createPointer();
        offsets.put(currentOffsetName, currentOffset);
    }
}
