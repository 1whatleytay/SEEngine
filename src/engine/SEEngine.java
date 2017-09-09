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
import java.util.ArrayList;

import static engine.SEProgramData.*;
import org.lwjgl.opengl.GL11;

public class SEEngine {
    /*
    Bugs:
        Offset seems to offet the object by a little more than it's supposed to be (center of screen or something similar)
    */
    
    private SEEngine() {}
    
    //Abilities and features that are currently not included by default.
    //Some may actually be controlls, like SEshouldQuit.
    //Try not to flicker these settings on and off. That may cause some issues
    //with the engine or the application.
    public static boolean
            //To see any performance increase, only enable when
            //SEuseWrappedObjects is disabled. Calls SEcollapseDrawSpace every
            //time an object is deleted.
            SEcollapseObjectDrawSpaceOnDeletion = false,
            //Uses and draws with wrapped objects or the SEWrappedObj class.
            //If an object is not wrapped, it will not be drawn. However,
            //wrapped objects offer a bunch of new features that you cannot
            //access with normal objects, while remaining somewhat efficient.
            SEuseWrappedObjects = false,
            //Looks for OpenGL errors every frame. If it finds one, it'll be
            //sure to tell you with a MSG_OPENGL message.
            SEcatchOpenGLErrors = false,
            //Once enabled, objects will be able to be wrapped twice. Enable
            //this for particle machines that use wrapped objects, that may
            //need to wrap a single object over and over.
            SEdoubleWrappedObjects = false,
            //Enabled depth for wrapped objects. Changes how SEWrappedObjs are
            //drawn a little to make space for the depth mechanic. To move the
            //depth of a wrapped object, call SEdepth on the object.
            SEwrappedObjectDepth = false,
            //Enable this to quit the application as soon as possible. Might not
            //work if the application is frozen, however, it's safe and quits
            //everything properly.
            SEshouldQuit = false,
            //Enable this to receive MSG_DEBUG messages from the engine. May or
            //may not send you anything, depending on your version.
            SEcoreDebug = false,
            //An alternative depth measurement. May speed up SEdepth calls
            //ever so slightly.
            SEexperimentalDepth = false,
            //When enabled, the window no longer updates automatically. Call
            //SEdraw to update the screen yourself. Mostly for applications with
            //little infrequent changes, like tools.
            SEdrawOnCommand = false,
            //When enabled, FPS will start being calculated. If diabled,
            //SEgetFPS will be reading off false infomation and may not return
            //the right value.
            SEcalcFPS = false,
            //Is true right before the main program's setup function is called.
            //Usually true once a window is open too.
            SEisRunning = false,
            SEdoNotBindOriginOffset = false;
    
    //HashMap of known versions. It's okay if your version isn't listed here,
    //we'll do our best guess to determine where your version is in the timeline
    //of SEversions.
    private static HashMap<String, Integer> versions() {
        HashMap<String, Integer> vers = new HashMap<>();
        vers.put("SEEarly0", 0); vers.put("SEEarly1", 1); vers.put("SEEarly2", 2);
        vers.put("SEEarly3", 3); vers.put("SEEarly4", 4); vers.put("SEAlpha0a", 5);
        vers.put("SEAlpha1a", 6); vers.put("SEAlpha1b", 7); vers.put("SEAlpha1c", 8);
        return vers;
    }
    private static final HashMap<String, Integer> VERSIONS = versions();
    
    //Determines if the string passed through cversions is compatible with the
    //current version.
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
    
    //Some messaging functions.
    //msgFuncExists is true if the program actually set a messaging function.
    //engineLog is a String of all past logged events with log(byte, String).
    //log(byte, String) logs the message and sends the message to your program
    //if you program has a messaging function.
    private static boolean msgFuncExists = false;
    public static String engineLog = "";
    public static void log(byte type, String log) { if (msgFuncExists) programData.messageFunc.msg(type, log); engineLog += log + "\n"; }
    
    //Calls log(byte, String) with type of MSG_DEBUG and the provided String log
    //if SEcoreDebug is enabled. Mostly for internal messaging, if use at all.
    public static void debug(String log) { if (SEcoreDebug) log(MSG_DEBUG, log); }
    
    //The GLFW window.
    private static long window = NULL;
    
    //Your program, program functions and program data all here for convenient
    //access.
    private static SEProgram program;
    private static SEProgramData programData;
    
    //The width and height of the window.
    protected static float scWidth = 600, scHeight = 600;
    
    //A function to clean up the engine before closing.
    private static void close() {
        SEisRunning = false;
        log(MSG_INFO, "---Close---");
        glfwDestroyWindow(window);
        glfwTerminate();
    }
    
    //Draws a wrapped object when render time comes.
    private static void drawWrappedObject(SEWrappedObj wObj) {
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
    
    //Renders everything availible.
    private static void render() {
        if (SEuseWrappedObjects) {
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
    
    //Is true once a warning (triggered by calling SEdraw with SEdrawOnCommand
    //disabled) has been issued. Used for one time warnings.
    private static boolean hasWarnedSEdrawOnCommand = false;
    
    //Completely updates the screen. Use for when SEdrawOnCommand is enabled (or
    //when the screen no longer updating).
    public static void SEdraw() {
        if (!hasWarnedSEdrawOnCommand && !SEdrawOnCommand) {
            log(MSG_OPT, "SEdraw was called even though the frame was going to be drawn anyway. Maybe you're looking for enabling SEdrawOnCommand?");
            hasWarnedSEdrawOnCommand = true;
        }
        glClear(GL_COLOR_BUFFER_BIT);
        render();
        glfwSwapBuffers(window);
    }
    
    //Fps calculation variables...
    private static int majorfps = 0;
    private static int minorfps = 0;
    private static SERLogic.Alarm fpsAlarm = new SERLogic.Alarm();
   
    //Is true once a warning (triggered by calling SEgetFPS with SEcalcFPS
    //disabled) has been issued. Used for one time warnings.
    private static boolean hasWarnedSEcalcFPS = false;
    
    //Gets the current FPS. Updates every second when SEcalcFPS is enabled.
    public static int SEgetFPS() {
        if (!hasWarnedSEcalcFPS && !SEcalcFPS) {
            log(MSG_OPT, "A call to SEgetFPS() was made, but SEcalcFPS was disabled, and the FPS may not be accurate.");
            hasWarnedSEcalcFPS = true;
        }
        return majorfps;
    }
    
    //Main program loop
    private static void loop() {
        log(MSG_INFO, "---Loop---");
        while (!glfwWindowShouldClose(window) && !SEshouldQuit) {
            if (SEcalcFPS) {
                minorfps++;
                if (fpsAlarm.hasBeen(1000)) {
                    majorfps = minorfps;
                    minorfps = 0;
                }
            }
            program.update();
            if (!SEdrawOnCommand) {
                glClear(GL_COLOR_BUFFER_BIT);
                render();
                glfwSwapBuffers(window);
            }
            if (SEcatchOpenGLErrors) {
                int error = glGetError();
                if (error != GL_NO_ERROR) log(MSG_OPENGL, "An OpenGL error has occured: " + error);
            }
            glfwPollEvents();
        }
    }
    
    //Ressource function for calculating inheritance.
    private static boolean shouldInherit(byte inh, int oldVal, int newVal) {
        return (inh >= INHERIT_MOST || (inh == INHERIT_MINIMUM && oldVal >= newVal));
    }
    
    
    //Ressource function for SEswitchPrograms(SEProgram, boolean).
    private static SEProgram SEswitchPrograms(SEProgram newProgram, boolean doSetup, boolean doChanges) {
        SEProgram oldProgram = program;
        SEProgramData oldProgramData = programData;
        SEProgramData newProgramData = newProgram.program();
        if (doChanges) {
            glfwSetWindowTitle(window, newProgramData.programName);
            if (oldProgramData.isFullScreen != newProgramData.isFullScreen && newProgramData.inheritData <= INHERIT_NONE);
            if ((oldProgramData.windowWidth != newProgramData.windowWidth || oldProgramData.windowHeight != newProgramData.windowHeight) && newProgramData.inheritData <= INHERIT_NONE)
                glfwSetWindowSize(window, newProgramData.windowWidth, newProgramData.windowHeight);
            SERImages.components = newProgramData.textureComponents;
            if (oldProgramData.textureComponents != newProgramData.textureComponents) {
                if (SERImages.components == 1) SEIShaders.fragComponentMode = SEIShaders.FRAG_MODE_GREYSCALE;
                else if (SERImages.components == 4) {
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                }
                else if (SERImages.components >= SEProgramData.FOURTH_COMPONENT_AS_DISCARD) {
                    SERImages.components = 4; SEIShaders.fragComponentMode = SEIShaders.FRAG_MODE_ROUND_ALPHA; }
                if (!SEIShaders.loadProgram()) { log(MSG_FAIL, "Could not load shaders!"); return null; }
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
            msgFuncExists = newProgramData.messageFunc != null;
        }
        if (!isCompatible(newProgramData.compatibleVersions)) { log(MSG_FAIL, "New program passed through SEswitchPrograms is not compatible."); return oldProgram; }
        program = newProgram;
        programData = newProgramData;
        if (doSetup) newProgram.setup();
        return oldProgram;
    }
    
    //Switches the current program with a new program supplied through
    //newProgram. If doSetup is true, the setup function is called. Keep
    //doSetup true if you want a proper setup for newProgram. Set to false
    //if the program has already initialized. isFullScreen proprety of the
    //program is ignored. Possibly buggy.
    public static SEProgram SEswitchPrograms(SEProgram newProgram, boolean doSetup) {
        return SEswitchPrograms(newProgram, doSetup, true);
    }
    
    //A basic void no params for SEque.
    public static interface SEqueFunc { public void func(); }
    
    //A list of all SEqueFuncs that have been qued with SEque.
    private static ArrayList<SEqueFunc> quedFuncs = new ArrayList<>();
    
    //Ques the function func till right before at least one setup call has run.
    //If a program has already been setup properly, the function is called on
    //the spot.
    public static void SEque(SEqueFunc func) {
        if (SEisRunning) func.func();
        else quedFuncs.add(func);
    }
    
    //Profile of all the org.lwjgl.GLFW.GLFW GLFW_KEYs. just look in this array
    //for a GLFW_KEY. If it's true, it's currently being pressed down.
    public static boolean[] keys = new boolean[348];
    
    //Essentially the same thing of looking into the keys array, except this
    //gets it's data directly from GLFW.
    public static boolean SEgetKeyPosition(int key) { return glfwGetKey(window, key) == GLFW_PRESS; }
    
    //Initialises SEEngine. Passed a class that implements SEProgram and returns
    //a valid SEProgramData structure from it's program function. From that
    //information, the engine will do it's best to set everything up. Called
    //from SEstart(SEProgram), the actual initialiser for your program.
    private static boolean init(SEProgram prog) {
        program = prog;
        programData = prog.program();
        if (programData.messageFunc != null) msgFuncExists = true;
        log(MSG_INFO, "---Init---");
        if (!glfwInit()) { log(MSG_FAIL_FATAL, "GLFW failed to start."); return false; }
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 1);
        window = glfwCreateWindow(programData.windowWidth, programData.windowHeight, programData.programName, programData.isFullScreen ? glfwGetPrimaryMonitor() : NULL, NULL);
        if (window == NULL) { log(MSG_FAIL_FATAL, "Could not create window!"); return false; }
        glfwMakeContextCurrent(window);
        glfwSetKeyCallback(window, (long window, int key, int scancode, int action, int mods)->{
            if (action == GLFW_PRESS) keys[key] = true;
            else if (action == GLFW_RELEASE) keys[key] = false;
            programData.keyFunc.key(key, action);
        });
        GL.createCapabilities();
        SERImages.components = programData.textureComponents;
        if (SERImages.components == 1) SEIShaders.fragComponentMode = SEIShaders.FRAG_MODE_GREYSCALE;
        else if (SERImages.components == 4) {
            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        }
        else if (SERImages.components >= SEProgramData.FOURTH_COMPONENT_AS_DISCARD) {
            SERImages.components = 4; SEIShaders.fragComponentMode = SEIShaders.FRAG_MODE_ROUND_ALPHA; }
        if (!SEIShaders.loadProgram()) { log(MSG_FAIL_FATAL, "Could not load shaders!"); return false; }
        if (!isCompatible(programData.compatibleVersions)) { SEEngine.log(MSG_FAIL_FATAL, "Incompatible SE version."); return false; }
        SEObjects.loadObjects(programData.maxObjects);
        SETextures.loadTextures(programData.texMemoryWidth, programData.texMemoryHeight);
        scWidth = programData.windowWidth; scHeight = programData.windowHeight;
        glClearColor(programData.bkgColor[0], programData.bkgColor[1], programData.bkgColor[2], programData.bkgColor[3]);
        for (SEqueFunc qf : quedFuncs) qf.func();
        SEisRunning = true;
        prog.setup();
        return true;
    }
    
    //The current SEEngine version. SEAlpha1b
    public static String SEversion() { return "SEAlpha1c"; }
    
    //Starts SEEngine. Pass a class that implements SEProgram where the program
    //function returns a valid SEProgramData (aka, not null). We'll handle the
    //thread from here. Once the program has terminated, either through an error
    //or SEshouldQuit, the function returns too.
    public static void SEstart(SEProgram prog) {
        if (init(prog)) loop();
        close();
    }
}
