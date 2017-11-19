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

import org.lwjgl.opengl.GL;

import org.lwjgl.glfw.GLFW;
import static org.lwjgl.glfw.GLFW.*;

import static org.lwjgl.system.MemoryUtil.NULL;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.util.HashMap;
import java.util.ArrayList;

import static engine.SEConstants.*;

/**
 * Handles starting the engine and controlling flow and features of the program(s).
 * @author desgroup
 * @version SEAlpha3a
 */
public class SEEngine {
    
    private SEEngine() {}

    /**
     * Calls SEcollapseDrawSpace every time an object is deleted.
     * Only has an effect with SEwrappedObjects disabled.
     */
    public static boolean SEcollapseObjectDrawSpaceOnDeletion = false;
    /**
     * Switches mode to use Wrapped Objects.
     * Allows matrix and offset transformations for many wrapped objects.
     */
    public static boolean SEwrappedObjects = false;
    /**
     * Looks for OpenGL errors every frame and notifies your application with a MSG_OPENGL message.
     */
    public static boolean SEcatchOpenGLErrors = false;
    /**
     * Allows {@link engine.SEObj} to be wrapped twice.
     */
    public static boolean SEdoubleWrappedObjects = false;
    /**
     * Allows {@link engine.SEObjects#SEdepth(SEWrappedObj, int)} to control the depth of a {@link enigne.SEWrappedObj}.
     */
    public static boolean SEwrappedObjectDepth = false;
    /**
     * Once enabled, {@link engine.SEEngine} will return from {@link engine.SEEngine#SEstart(SEProgram)} as soon as the program returns.
     * Using this option to quit the window (almost) insures a safe termination of GLFW and other libraries.
     */
    public static boolean SEshouldQuit = false;
    /**
     * Allows MSG_DEBUG messages sent by the engine to be sent to your application.
     * Meant for debugging the engine.
     * If unsure, leave  this feature off.
     */
    public static boolean SEuseDebug = false;
    /**
     * Disables automatic window drawing.
     * To continue drawing, re-enable this setting or call SEdraw() when you wish to update the screen.
     */
    public static boolean SEdrawOnCommand = false;
    /**
     * Allows FPS calculations.
     * To get the current FPS measurement, call {@link engine.SEEngine#SEgetFPS()}
     */
    public static boolean SEcalcFPS = false;
    /**
     * Prevents binding {@link enigine.SEObjects#ORIGIN_OFFSET} to all {@link engine.SEWrappedObj} on creation.
     */
    public static boolean SEpreventBindOriginOffset = false;
    /**
     * Prevents overwriting descriptions.
     * Breaks {@link engine.SEEngine#logWithDescription(byte, int, String)} and descriptions that may be variable.
     */
    public static boolean SEpreventDoubleDescriptions = false;
    public static boolean SEuseLayers = false;
    
    private static boolean isRunning = false;
    
    private static HashMap<String, Integer> versions() {
        HashMap<String, Integer> vers = new HashMap<>();
        vers.put("SEEarly0", 0); vers.put("SEEarly1", 1); vers.put("SEEarly2", 2);
        vers.put("SEEarly3", 3); vers.put("SEEarly4", 4); vers.put("SEAlpha0a", 5);
        vers.put("SEAlpha1a", 6); vers.put("SEAlpha1b", 7); vers.put("SEAlpha1c", 8);
        vers.put("SEAlpha2a", 9); vers.put("SEAlpha2b", 10);
        return vers;
    }
    private static final HashMap<String, Integer> VERSIONS = versions();
    
    private static boolean isCompatible(String cversions) {
        String[] version = cversions.replace(" ", "").split(",");
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
        return built;
    }
    
    private static boolean msgFuncExists = false;

    /**
     * A string of all messages sent to your application separated by newlines.
     */
    public static String engineLog = "";

    /**
     * Sends a message to the current application with a type and a description.
     * @param type The type of the message being sent. Should be a MSG_ constant from {@link engine.SEProgramData}.
     * @param message A indicator of the specific message.
     */
    public static void log(byte type, int message) {
        if (msgFuncExists) programData.functions.messageFunc.msg(type, message);
        engineLog += SEgetMessageDescription(message) + "\n"; 
    }
    
    private static HashMap<Integer, String> messageDescriptions() {
        HashMap<Integer, String> nMD = new HashMap<>();
        nMD.put(MSG_GENERIC, "A generic placeholder message. Expected more?");
        nMD.put(MSG_INIT, "--> SEEngine is initializing...");
        nMD.put(MSG_LOOP, "--> SEEngine has entered the main program loop...");
        nMD.put(MSG_EXIT, "--> SEEngine is closing...");
        nMD.put(MSG_GLFW_ERROR, "GLFW failed to start.");
        nMD.put(MSG_WINDOW_ERROR, "Could not create window!");
        nMD.put(MSG_SHADERS_ERROR, "Could not load shaders!");
        nMD.put(MSG_INCOMPATIBLE_PROGRAM, "Program claims to be incompatible with " + SEversion());
        nMD.put(MSG_OPENGL_FEEDBACK_ERROR, "Generic OpenGL Error, I think...");
        nMD.put(MSG_LOG_WITH_DESCRIPTION_WARNING, "Calling logWithDescription with SEpreventDoubleDescriptions can cause some unreliable mesages being sent.");
        nMD.put(MSG_GET_FPS_WARNING, "A call to SEgetFPS() was made, but SEcalcFPS was disabled, and the FPS may not be accurate.");
        nMD.put(MSG_DRAW_WARNING, "SEdraw was called even though the frame was going to be drawn anyway. Maybe you're looking for enabling SEdrawOnCommand?");
        nMD.put(MSG_DEBUG_BINDING_POINT, "Generic Debug Message, I think...");
        nMD.put(MSG_SHADERS_VERTEX_COMPILE_ERROR, "Generic Vertex Shader Compiler Issue, I think...");
        nMD.put(MSG_SHADERS_FRAGMENT_COMPILE_ERROR, "Generic Fragment Shader Compiler Issue, I think...");
        nMD.put(MSG_SHADERS_LINK_ERROR, "Generic Shader Link Issue, I think...");
        nMD.put(MSG_UNBIND_OFFSET_WARNING, "An unbind attempt was made to remove an offset that wasn't bound to the objects.");
        nMD.put(MSG_OUT_OF_OBJECT_MEMORY, "Out of object memory!");
        nMD.put(MSG_EMPTY_WRAPPER_CREATED, "An empty wrapper was created.");
        nMD.put(MSG_UNKNOWN_WRAPPED_OBJECT, "An unknown wrapped object was queried.");
        nMD.put(MSG_ALREADY_WRAPPED, "Yikes! SEdoubleWrappedObjects was disabled and a object was wrapped twice!");
        nMD.put(MSG_MISSING_DEPTH_INFO, "Yikes! SEwrappedObjectDepth was switched unexpectedly!");
        nMD.put(MSG_MISSING_TEXTURE, "Generic Missing Texture, I think...");
        nMD.put(MSG_TEXTURE_LOAD_ERROR, "A texture failed to load!");
        nMD.put(MSG_INCOMPATIBLE_MATRICES, "Incompatible Matrices!");
        nMD.put(MSG_NULL_TEXTURE, "An operation was passed a null texture!");
        nMD.put(MSG_OUT_OF_TEXTURE_MEMORY, "Out of texture memory!");
        nMD.put(MSG_INCOMPATIBLE_CONTEXT, "The current context cannot handle an operation!");
        nMD.put(MSG_OPENAL_FEEDBACK_ERROR, "Generic OpenAL Error, I think...");
        nMD.put(MSG_EXPERIMENTAL_SOUND_WARNING, "SESound is experimental. Please call loadSounds() again to complete sound setup.");
        nMD.put(MSG_ADD_LAYER_WARNING, "A Layer is being added, but layers are currently disabled.");
        return nMD;
    }
    
    private static final HashMap<Integer, String> MESSAGE_DESCRIPTIONS = messageDescriptions();
    
    /**
     * Adds a message description to message msg.
     * @param msg The message to have it's description changed.
     * @param description The description to add.
     * @return The passed msg variable.
     */
    public static int SEaddMessageDescription(int msg, String description) {
        if (!SEpreventDoubleDescriptions || MESSAGE_DESCRIPTIONS.get(msg) == null)
            MESSAGE_DESCRIPTIONS.put(msg, description);
        return msg;
    }
    
    /**
     * Gets the description of message msg.
     * @param msg The message to query the description of.
     * @return The description of the message msg.
     */
    public static String SEgetMessageDescription(int msg) { return MESSAGE_DESCRIPTIONS.get(msg); }
    
    private static boolean hasWarnedLWDPrevented = false;
    
    /**
     * Logs a message with a description.
     * @param type The type of message.
     * @param bindingPoint The message to send.
     * @param msg The new description of the message.
     */
    public static void logWithDescription(byte type, int bindingPoint, String msg) {
        if (hasWarnedLWDPrevented && SEpreventDoubleDescriptions) {
            log(MSG_TYPE_OPT, MSG_LOG_WITH_DESCRIPTION_WARNING);
            hasWarnedLWDPrevented = true;
        }
        String lastDesc = SEgetMessageDescription(bindingPoint);
        SEaddMessageDescription(bindingPoint, msg);
        log(type, bindingPoint);
        SEaddMessageDescription(bindingPoint, lastDesc);
    }
    
    /**
     * If {@link engine.SEEngine#SEcoreDebug} is enabled, sends a message of type {@link engine.SEConstants#MSG_TYPE_DEBUG} through binding point {@link engine.SEConstants#MSG_DEBUG_BINDING_POINT}.
     * The description of {@link engine.SEConstants#MSG_DEBUG_BINDING_POINT} should be equal to msg at the time during a single threaded application.
     * @param msg A description of the debug message.
     */
    public static void debug(String msg) { if (SEuseDebug) logWithDescription(MSG_TYPE_DEBUG, MSG_DEBUG_BINDING_POINT, msg); }
    
    private static long window = NULL;
    
    private static SEControlledProgram program;
    private static SEProgramData programData;
    
    /**
    * Width of the current window.
    */
    protected static float scWidth = 600;
    /**
     * Height of the current window.
     */
    protected static float scHeight = 600;
    
    /**
     * Gets the current window width.
     * @return The current window width.
     */
    public static int SEgetWindowWidth() { return (int)scWidth; }
    /**
     * Gets the current window height.
     * @return The current window height.
     */
    public static int SEgetWindowHeight() { return (int)scHeight; }
    
    /*String warnings = "";
    
    public static void SEwarn(String info) {
        
    }*/
    
    private static void close() {
        isRunning = false;
        log(MSG_TYPE_INFO, MSG_EXIT);
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    
    private static void drawWrappedObject(SEWrappedObj wObj) {
        if (wObj == null) return;
        SEIShaders.matrix(wObj.matrix);
        int[] totalOffset = new int[2];
        for (String coffsetName : wObj.offsetNames) {
            int[] coffset = SEObjects.offsets.get(coffsetName);
            totalOffset[0] += coffset[0]; totalOffset[1] += coffset[1];
        }
        SEIShaders.offset(totalOffset[0], totalOffset[1]);
        int xCen = wObj.matrixCenterX; int yCen = wObj.matrixCenterY;
        if (wObj.useObjectForMatrixCenter) { xCen = wObj.matrixCenter.centerX; yCen = wObj.matrixCenter.centerY; }
        SEIShaders.matrix_center(xCen, yCen);
        glMultiDrawArrays(GL_QUADS, wObj.drawRangesStart, wObj.drawRangesCount);
    }
     
    private static void render() {
        if (SEwrappedObjects) {
            if (SEwrappedObjectDepth) {
                int head = SEObjects.first[0];
                for (int a = 0; a < SEObjects.knownObjects.size(); a++) {
                    if (head == -1) break;
                    drawWrappedObject(SEObjects.knownObjects.get(head));
                    head = SEObjects.next.get(head);
                }
            }
            else for (SEWrappedObj wObj : SEObjects.knownObjects) drawWrappedObject(wObj);
        } else {
            glUseProgram(SEIShaders.shaderProgram);
            glBindBuffer(GL_ARRAY_BUFFER, SEObjects.mainBuffer);
            SEObjects.fixOffsets();
            glDrawArrays(GL_QUADS, 0, 4 * SEObjects.objectDrawSpace);
        }
    }
    
    private static boolean hasWarnedSEdrawOnCommand = false;

    /**
     * Updates the screen.
     * If {@link engine.SEEngine#SEdrawOnCommand} is disabled, this function is called automatically.
     */
    public static void SEdraw() {
        if (!hasWarnedSEdrawOnCommand && !SEdrawOnCommand) {
            log(MSG_TYPE_OPT, MSG_DRAW_WARNING);
            hasWarnedSEdrawOnCommand = true;
        }
        glClear(GL_COLOR_BUFFER_BIT);
        render();
        glfwSwapBuffers(window);
    }
    
    private static int majorfps = 0;
    private static int minorfps = 0;
    private static SERLogic.Alarm fpsAlarm = new SERLogic.Alarm();
   
    private static boolean hasWarnedSEcalcFPS = false;

    /**
     * Returns the current FPS count.
     * The FPS count updates every second.
     * Will not provide an accurate value if {@link engine.SEEngine#SEcalcFPS} has not been enabled for at least one second up to the call.
     * @return The current FPS (Frames Per Second) of the program.
     */
    public static int SEgetFPS() {
        if (!hasWarnedSEcalcFPS && !SEcalcFPS) {
            log(MSG_TYPE_OPT, MSG_GET_FPS_WARNING);
            hasWarnedSEcalcFPS = true;
        }
        return majorfps;
    }
    
    private static void loop() {
        log(MSG_TYPE_INFO, MSG_LOOP);
        while (!glfwWindowShouldClose(window) && !SEshouldQuit) {
            if (SEcalcFPS) {
                minorfps++;
                if (fpsAlarm.hasBeen(1000)) {
                    majorfps = minorfps;
                    minorfps = 0;
                }
            }
            if (SEuseLayers) { for (SELayerBundle bundle : knownLayers) bundle.layer.pre(); }
            program.update();
            if (SEuseLayers) { for (SELayerBundle bundle : knownLayers) bundle.layer.post(); }
            if (!SEdrawOnCommand) {
                glClear(GL_COLOR_BUFFER_BIT);
                render();
                glfwSwapBuffers(window);
            }
            if (SEcatchOpenGLErrors) {
                int error = glGetError();
                if (error != GL_NO_ERROR) logWithDescription(MSG_TYPE_OPENGL, MSG_OPENGL_FEEDBACK_ERROR, "New OpenGL error: " + error);
            }
            glfwPollEvents();
        }
    }
    
    private static boolean shouldInherit(byte inh, int oldVal, int newVal) {
        return (inh >= INHERIT_MOST || (inh == INHERIT_MINIMUM && oldVal >= newVal));
    }
    
    private static SEControlledProgram SEswapPrograms(SEControlledProgram newProgram, boolean doSetup, boolean doChanges) {
        SEControlledProgram oldProgram = program;
        SEProgramData oldProgramData = programData;
        SEProgramData newProgramData = newProgram.program();
        if (doChanges) {
            glfwSetWindowTitle(window, newProgramData.programName);
            if (oldProgramData.isFullScreen != newProgramData.isFullScreen && newProgramData.inheritData <= INHERIT_NONE);
            if ((oldProgramData.windowWidth != newProgramData.windowWidth || oldProgramData.windowHeight != newProgramData.windowHeight) && newProgramData.inheritData <= INHERIT_NONE) {
                glfwSetWindowSize(window, newProgramData.windowWidth, newProgramData.windowHeight);
                scWidth = newProgramData.windowWidth; scHeight = newProgramData.windowHeight;
            }
            SERImages.components = newProgramData.textureComponents;
            if (oldProgramData.textureComponents != newProgramData.textureComponents) {
                if (SERImages.components == 1) SEIShaders.fragComponentMode = FRAG_MODE_GREYSCALE;
                else if (SERImages.components == 4) {
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                }
                else if (SERImages.components >= FOURTH_COMPONENT_AS_DISCARD) {
                    SERImages.components = 4; SEIShaders.fragComponentMode = FRAG_MODE_ROUND_ALPHA; }
                if (!SEIShaders.loadProgram()) { log(MSG_TYPE_FAIL, MSG_SHADERS_ERROR); return null; }
            }
            SERImages.components = (byte)Math.min(newProgramData.textureComponents, 4);
            if (
                    newProgramData.useQuickClear &&
                    (oldProgramData.maxObjects == newProgramData.maxObjects ||
                    shouldInherit(
                            newProgramData.inheritData,
                            oldProgramData.maxObjects,
                            newProgramData.maxObjects)))
                SEObjects.quickClearObjects();
            else SEObjects.clearObjects(shouldInherit(newProgramData.inheritData, oldProgramData.maxObjects, newProgramData.maxObjects) ? oldProgramData.maxObjects : newProgramData.maxObjects);
            if (newProgramData.useQuickClear && ((
                    oldProgramData.texMemoryWidth == newProgramData.texMemoryWidth &&
                    oldProgramData.texMemoryHeight == newProgramData.texMemoryHeight &&
                    oldProgramData.textureComponents == newProgramData.textureComponents ) ||
                    shouldInherit(newProgramData.inheritData, oldProgramData.texMemoryWidth, newProgramData.texMemoryWidth) &&
                    shouldInherit(newProgramData.inheritData, oldProgramData.texMemoryHeight, newProgramData.texMemoryHeight) &&
                    shouldInherit(newProgramData.inheritData, oldProgramData.textureComponents, newProgramData.textureComponents)))
                SETextures.quickClearTextures();
            else {
                SETextures.clearTextures(newProgramData.texMemoryWidth, newProgramData.texMemoryHeight);
            }
            glClearColor(newProgramData.bkgColor[0], newProgramData.bkgColor[1], newProgramData.bkgColor[2], newProgramData.bkgColor[3]);
            msgFuncExists = newProgramData.functions.messageFunc != null;
        }
        if (!isCompatible(newProgramData.compatibleVersions)) { log(MSG_TYPE_FAIL, MSG_INCOMPATIBLE_PROGRAM); return oldProgram; }
        program = newProgram;
        programData = new SEProgramData(newProgramData);
        if (doSetup) newProgram.setup();
        return oldProgram;
    }
    
    /**
     * Switches the current program with a new program provided as newProgram.
     * To initialize SEEngine, use {@link engine.SEEngine#SEstart(SEControlledProgram)}
     * @param newProgram The program to replace the old one.
     * @param doSetup If true, newProgram will have it's {@link engine.SEControlledProgram#setup()} method called.
     * @return The last program that was running.
     */
    public static SEControlledProgram SEswapPrograms(SEControlledProgram newProgram, boolean doSetup) {
        return SEswapPrograms(newProgram, doSetup, true);
    }
    
    public static SEControlledProgram SEswapPrograms(SEControlledProgram newProgram) {
        return SEswapPrograms(newProgram, true);
    }
    
    /**
     * Changes the current background color.
     * @param newColor The new background color. Should be an array with 4 elements.
     */
    public static void SEchangeBackgroundColor(float[] newColor) {
        glClearColor(newColor[0], newColor[1], newColor[2], newColor[3]);
    }
    
    private static ArrayList<SEInfoFunc> quedFuncs = new ArrayList<>();
    
    /**
     * Ques one function to be called right before the program's setup method is called or immediately if the engine has already initialized.
     * @param func The code/interface to be called once the engine has setup.
     */
    public static void SEaddQue(SEInfoFunc func) {
        if (isRunning) func.func();
        else quedFuncs.add(func);
    }
    
    private static boolean hasWarnedDisabledLayers = false;
    
    private static ArrayList<SELayerBundle> knownLayers = new ArrayList<>();
    
    public static void SEaddLayer(SEControlledLayer layer) {
        SELayerData layerData = layer.layer();
        if (!SEuseLayers && !hasWarnedDisabledLayers) { log(MSG_TYPE_INFO, MSG_ADD_LAYER_WARNING); hasWarnedDisabledLayers = true; }
        SEaddQue(layerData.setupFunc);
        SELayerBundle bundle = new SELayerBundle();
        bundle.layer = layer;
        bundle.layerData = layerData;
        knownLayers.add(bundle);
    }
    
    public static void SEremoveLayer(String layerName) {
        int find = -1;
        for (int a = 0; a < knownLayers.size(); a++) {
            if (knownLayers.get(a).layerData.layerName.equals(layerName)) {
                find = a;
                break;
            }
        }
        if (find == -1) { log(MSG_TYPE_INFO, MSG_UNKNOWN_LAYER); return; }
        knownLayers.remove(find);
    }

    /**
     * Gets a specific key's state on the keyboard.
     * If a key is currently pressed down, it will return true.
     * @param key The key being queried. One of the many GLFW_KEY_ constants.
     * @return Returns either true if the key is being pressed and false otherwise.
     */
    public static boolean SEgetKeyPosition(int key) { return glfwGetKey(window, key) == GLFW_PRESS; }
    
    private static boolean init(SEControlledProgram prog) {
        program = prog;
        programData = new SEProgramData(prog.program());
        if (programData.functions.messageFunc != null) msgFuncExists = true;
        log(MSG_TYPE_INFO, MSG_INIT);
        if (!glfwInit()) { log(MSG_TYPE_FAIL_FATAL, MSG_GLFW_ERROR); return false; }
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        window = glfwCreateWindow(programData.windowWidth, programData.windowHeight, programData.programName, programData.isFullScreen ? glfwGetPrimaryMonitor() : NULL, NULL);
        if (window == NULL) { log(MSG_TYPE_FAIL_FATAL, MSG_WINDOW_ERROR); return false; }
        glfwMakeContextCurrent(window);
        glfwSetKeyCallback(window, (long windowM, int key, int scancode, int action, int mods)->{
            if (SEuseLayers) for (SELayerBundle bundle : knownLayers) bundle.layerData.preFuncs.keyFunc.key(key, action);
            programData.functions.keyFunc.key(key, action);
            if (SEuseLayers) for (SELayerBundle bundle : knownLayers) bundle.layerData.postFuncs.keyFunc.key(key, action);
        });
        glfwSetCursorPosCallback(window, (long windowM, double x, double y) -> {
            if (SEuseLayers) for (SELayerBundle bundle : knownLayers) bundle.layerData.preFuncs.mouseFunc.mouse((int)x, (int)y, 0, MOUSE_MOVE);
            programData.functions.mouseFunc.mouse((int)x, (int)y, 0, MOUSE_MOVE);
            if (SEuseLayers) for (SELayerBundle bundle : knownLayers) bundle.layerData.postFuncs.mouseFunc.mouse((int)x, (int)y, 0, MOUSE_MOVE);
            for (SEObjects.SEButton bundle : SEObjects.buttons) {
                if (SERLogic.isPointColliding((int)x, (int)y, bundle.x, bundle.y, bundle.w, bundle.h)) {
                    bundle.func.func(bundle, MOUSE_MOVE);
                    if (!bundle.mouseOver) { bundle.mouseOver = true; bundle.func.func(bundle, MOUSE_ENTER);
                    }
                } else if (bundle.mouseOver) { bundle.mouseOver = false; bundle.func.func(bundle, MOUSE_EXIT); }
            }
        });
        glfwSetMouseButtonCallback(window, (long windowM, int button, int action, int mods) -> {
            double[] x = new double[1], y = new double[1];
            glfwGetCursorPos(window, x, y);
            if (SEuseLayers) for (SELayerBundle bundle : knownLayers) bundle.layerData.preFuncs.mouseFunc.mouse((int)x[0], (int)y[0], button, action);
            programData.functions.mouseFunc.mouse((int)x[0], (int)y[0], button, action);
            if (SEuseLayers) for (SELayerBundle bundle : knownLayers) bundle.layerData.postFuncs.mouseFunc.mouse((int)x[0], (int)y[0], button, action);
            for (SEObjects.SEButton bundle : SEObjects.buttons)
                if (SERLogic.isPointColliding((int)x[0], (int)y[0], bundle.x, bundle.y, bundle.w, bundle.h)) bundle.func.func(bundle, action == GLFW_PRESS ? MOUSE_PRESS : MOUSE_RELEASE);
        });
        GL.createCapabilities();
        SERImages.components = programData.textureComponents;
        if (SERImages.components == 1) SEIShaders.fragComponentMode = FRAG_MODE_GREYSCALE;
        else if (SERImages.components == 4) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }
        else if (SERImages.components >= FOURTH_COMPONENT_AS_DISCARD) {
            SERImages.components = 4; SEIShaders.fragComponentMode = FRAG_MODE_ROUND_ALPHA; }
        if (!SEIShaders.loadProgram()) { log(MSG_TYPE_FAIL_FATAL, MSG_SHADERS_ERROR); return false; }
        if (!isCompatible(programData.compatibleVersions)) { SEEngine.log(MSG_TYPE_FAIL_FATAL, MSG_INCOMPATIBLE_PROGRAM); return false; }
        SETextures.gpuMaxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
        debug("Max texture size: " + SETextures.gpuMaxTextureSize);
        SEObjects.loadObjects(programData.maxObjects);
        SETextures.loadTextures(programData.texMemoryWidth, programData.texMemoryHeight);
        scWidth = programData.windowWidth; scHeight = programData.windowHeight;
        SEchangeBackgroundColor(programData.bkgColor);
        for (SEInfoFunc qf : quedFuncs) qf.func();
        isRunning = true;
        prog.setup();
        return true;
    }
    
    public static boolean SEisRunning() { return isRunning; }
    
    /**
     * Returns the current SEEngine version.
     * @return The current SEEngine version.
     */
    public static String SEversion() { return "SEAlpha2b"; }
    
    /**
     * Starts SEEngine.
     * This function will not return until the program passed has also finished running (through {@link engine.SEEngine#SEshouldQuit}) or the user closes the window.
     * The active program can be switched using {@link engine.SEEngine#SEswitchPrograms(SEControlledProgram, boolean)}.
     * @param prog The that will be running.
     */
    public static void SEstart(SEControlledProgram prog) {
        if (init(prog)) loop();
        close();
    }
}
