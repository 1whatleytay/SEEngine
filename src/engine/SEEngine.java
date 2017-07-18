package engine;

import org.lwjgl.opengl.GL;

import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.system.MemoryUtil.NULL;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

import java.util.HashMap;

public class SEEngine {
    private SEEngine() {}
    
    public static boolean
            SEcollapseObjectDrawSpaceOnDeletion = false,
            SEuseWrappedObjects = false,
            SEcatchOpenGLErrors = false;
    
    private static HashMap<String, Integer> versions() {
        HashMap<String, Integer> vers = new HashMap<>();
        vers.put("SEEarly0", 0); vers.put("SEEarly1", 1); vers.put("SEEarly2", 2);
        vers.put("SEEarly3", 3); vers.put("SEEarly4", 4); vers.put("SEAlpha0a", 5);
        vers.put("SEAlpha1a", 6);
        return vers;
    }
    
    private static final HashMap<String, Integer> VERSIONS = versions();
    
    public static String engineLog = "";
    public static void log(String log) { System.out.println(log); engineLog += log + "\n"; }
    
    private static long window = NULL;
    
    private static SEProgram program;
    private static SEProgramData programData;
    
    protected static float scWidth = 600, scHeight = 600;
    
    private static void close() {
        log("---Close---");
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    
    private static void render() {
        if (SEuseWrappedObjects) {
            int a = 0;
            for (SEWrappedObj wObj : SEObjects.knownObjects) {
                SEIShaders.matrix(wObj.matrix);
                int[] offset = SEObjects.offsets.get(wObj.offsetName);
                SEIShaders.offset(offset[0], offset[1]);
                glMultiDrawArrays(GL_QUADS, wObj.drawRangesStart, wObj.drawRangesCount);
            }
        } else {
            glUseProgram(SEIShaders.shaderProgram);
            glBindBuffer(GL_ARRAY_BUFFER, SEObjects.mainBuffer);
            SEObjects.fixOffsets();
            glDrawArrays(GL_QUADS, 0, 4 * SEObjects.objectDrawSpace);
        }
    }
    
    private static void loop() {
        log("---Loop---");
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);
            program.update();
            render();
            if (SEcatchOpenGLErrors) {
                int error = glGetError();
                if (error != GL_NO_ERROR) log("An OpenGL error has occured: " + error);
            }
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    
    public static boolean[] keys = new boolean[348];
    
    private static boolean init(SEProgram prog) {
        log("---Init---");
        if (!glfwInit()) { log("GLFW failed to start."); return false; }
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        window = glfwCreateWindow(600, 600, "name", NULL, NULL);
        if (window == NULL) { log("Could not create window!"); return false; }
        glfwMakeContextCurrent(window);
        glfwSetKeyCallback(window, (long window, int key, int scancode, int action, int mods)->{
            if (action == GLFW_PRESS) keys[key] = true;
            else if (action == GLFW_RELEASE) keys[key] = false;
            programData.keyFunc.key(key, action);
        });
        GL.createCapabilities();
        program = prog;
        programData = prog.program();
        SERImages.components = programData.textureComponents;
        if (SERImages.components == 1) SEIShaders.fragComponentMode = SEIShaders.FRAG_MODE_GREYSCALE;
        else if (SERImages.components == 4) {/*Do Nothing...*/} // Fix! GL_BLEND does not enable blending
        else if (SERImages.components >= SEProgramData.FOURTH_COMPONENT_AS_DISCARD) {
            SERImages.components = 4; SEIShaders.fragComponentMode = SEIShaders.FRAG_MODE_ROUND_ALPHA; }
        if (!SEIShaders.loadProgram()) { log("Could not load shaders!"); return false; }
        String[] version = programData.compatibleVersions.replace(" ", "").split(",");
        boolean built = version[0].equals("all");
        for (String a : version) {
            if (a.equals(SEversion())) { built = true; break; }
            if (a.startsWith("before:")) {
                Integer v = VERSIONS.get(a.substring(7)); if (v==null) v=Integer.MAX_VALUE;
                if (VERSIONS.get(SEversion()) < v) { built = true; } else { built = false; break; }
            }
            if (a.startsWith("after:")) { 
                Integer v = VERSIONS.get(a.substring(6)); if (v==null) v=Integer.MAX_VALUE;
                if (VERSIONS.get(SEversion()) > v) { built = true; } else { built = false; break; }
            }
        }
        if (!built) { SEEngine.log("Incompatible SE version."); return false; }
        glfwSetWindowSize(window, programData.windowWidth, programData.windowHeight);
        glfwSetWindowTitle(window, programData.programName);
        SEObjects.loadObjects(programData.maxObjects);
        SETextures.loadTextures(programData.texMemoryWidth, programData.texMemoryHeight);
        scWidth = programData.windowWidth; scHeight = programData.windowHeight;
        prog.setup();
        return true;
    }
    
    public static String SEversion() { return "SEAlpha1a"; }
    
    public static void SEstart(SEProgram prog) {
        if (init(prog)) loop();
        close();
    }
}
