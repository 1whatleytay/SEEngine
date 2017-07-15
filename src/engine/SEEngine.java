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
    
    private static HashMap<String, Integer> versions() {
        HashMap<String, Integer> vers = new HashMap<>();
        vers.put("SEEarly0", 0); vers.put("SEEarly1", 1); vers.put("SEEarly2", 2);
        vers.put("SEEarly3", 3); vers.put("SEEarly4", 4); vers.put("SEAlpha0a", 5);
        return vers;
    }
    
    private static HashMap<String, Integer> versions = versions();
    
    public static final int
            SE_TOP_TO_BOTTOM = 0x100,
            SE_BOTTOM_TO_TOP = 0x101,
            SE_LEFT_TO_RIGHT = 0x102,
            SE_RIGHT_TO_LEFT = 0x103;
    
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
        glUseProgram(SEIShaders.shaderProgram);
        glBindBuffer(GL_ARRAY_BUFFER, SEObjects.mainBuffer);
        SEObjects.fixOffsets();
        glDrawArrays(GL_QUADS, 0, 4 * SEObjects.objectDrawSpace);
    }
    
    private static void loop() {
        log("---Loop---");
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);
            program.update();
            render();
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
        if (!SEIShaders.loadProgram()) { log("Could not load shaders!"); return false; }
        program = prog;
        programData = prog.program();
        String[] version = programData.compatibleVersions.replace(" ", "").split(",");
        boolean built = version[0].equals("all");
        for (String a : version) {
            if (a.equals(SEversion())) { built = true; break; }
            if (a.startsWith("before:")) {
                Integer v = versions.get(a.substring(7)); if (v==null) v=Integer.MAX_VALUE;
                if (versions.get(SEversion()) < v) { built = true; } else { built = false; break; }
            }
            if (a.startsWith("after:")) { 
                Integer v = versions.get(a.substring(6)); if (v==null) v=Integer.MAX_VALUE;
                if (versions.get(SEversion()) > v) { built = true; } else { built = false; break; }
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
    
    public static String SEversion() { return "SEAlpha0a"; }
    
    public static void SEstart(SEProgram prog) {
        if (init(prog)) loop();
        close();
    }
}
