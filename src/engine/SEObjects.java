package engine;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.*;

import java.nio.*;

import java.util.*;

public class SEObjects {
    private static final int OBJECTSIZE = 4;
    private static final int OBJECTWIDTH = 4;
    private static boolean[] objectSpace = null;
    private static float[] objectMap = null; 
    protected static int mainBuffer = -1;
    public static int objectDrawSpace = -1;
    
    public static final String ORIGIN_OFFSET = "offset1";
    
    private static float[] currentOffset = {0f, 0f};
    private static String currentOffsetName = ORIGIN_OFFSET;
    private static HashMap<String, float[]> offsets = new HashMap<>();
    
    public static void SEswapOffsets(String newOffset) {
        offsets.put(currentOffsetName, currentOffset);
        currentOffset = offsets.get(newOffset);
        if (currentOffset == null) { currentOffset = new float[]{0f, 0f}; offsets.put(newOffset, currentOffset); }
        currentOffsetName = newOffset;
    }
    public static String SEcurrentOffsetName() { return currentOffsetName; }
    public static void SEmoveOffset(float xMov, float yMov) { currentOffset[0] += xMov; currentOffset[1] += yMov; }
    public static void SEmoveOffset(float[] mov) { currentOffset[0] += mov[0]; currentOffset[1] += mov[1]; }
    public static void SEsetOffset(float xOffset, float yOffset) { currentOffset[0] = xOffset; currentOffset[1] = yOffset; }
    public static void SEsetOffset(float[] offset) { currentOffset[0] = offset[0]; currentOffset[1] = offset[1]; }
    public static float[] SEgetOffset() { return currentOffset; }
    
    protected static void fixOffsets() { SEIShaders.offset(currentOffset[0], currentOffset[1]); }
    
    public static float[] objNData(SEObj obj) {
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
        glBufferSubData(GL_ARRAY_BUFFER, obj.object * OBJECTSIZE * OBJECTWIDTH * OBJECTWIDTH, objNData(obj));
    }
    public static void SEobjPos(SEObj obj, int x, int y) { obj.x = x; obj.y = y; SEobjSave(obj); }
    public static void SEobjSize(SEObj obj, int w, int h) { obj.w = w; obj.h = h; SEobjSave(obj); }
    public static void SEobjTex(SEObj obj, SETex tex) { obj.tex = tex; SEobjSave(obj); }
    public static void SEobjData(SEObj obj, int x, int y, int w, int h) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; SEobjSave(obj); }
    public static void SEobjData(SEObj obj, int x, int y, int w, int h, SETex tex) { obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex; SEobjSave(obj); }
    public static void SEobjMove(SEObj obj, int x, int y) { obj.x += x; obj.y += y; SEobjSave(obj); }
    
    public static int SEmaxObjectCount() { return objectSpace.length; }
    
    public static SEObj SEcreateObject(int x, int y, int w, int h, SETex tex) {
        SEObj obj = new SEObj();
        boolean found = false;
        int find = -1;
        for (int a = 0; a < objectSpace.length; a++)  if (!objectSpace[a]) { find = a; found = true; break; }
        if (!found) { SEEngine.log("Out of Object Memory"); }
        objectSpace[find] = true;
        obj.object = find; obj.x = x; obj.y = y; obj.w = w; obj.h = h; obj.tex = tex;
        objectDrawSpace = Math.max(objectDrawSpace, find) + 1;
        SEEngine.log("F" + find + " " + objectDrawSpace);
        SEobjSave(obj);
        return obj;
    }
    
    public static void SEdeleteObject(SEObj obj) {
        objectSpace[obj.object] = false;
        SEobjData(obj, 0, 0, 0, 0, SETextures.BLANK_TEXTURE);
        if (obj.object == objectDrawSpace - 1) objectDrawSpace--;
    }
    
    private static int ampX = 1;
    private static int ampY = 1;
    
    public static void SEdirection(int direction) {
        switch (direction) {
            case SEEngine.SE_BOTTOM_TO_TOP: ampY = 1; break;
            case SEEngine.SE_TOP_TO_BOTTOM: ampY = -1; break;
            case SEEngine.SE_LEFT_TO_RIGHT: ampX = 1; break;
            case SEEngine.SE_RIGHT_TO_LEFT: ampX = -1; break;
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
