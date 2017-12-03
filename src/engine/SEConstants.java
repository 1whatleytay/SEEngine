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

/**
 * Holds all constants the engine may use to communicate.
 * @author desgroup
 * @version SEAlpha4a
 */
public class SEConstants {

    /**
     * A bundle of a program and its data.
     */
    protected static class SEProgramBundle {
        SEControlledProgram program;
        SEProgramData programData;
    }

    /**
     * A bundle of a layer and its data.
     */
    protected static class SELayerBundle {
        SEControlledLayer layer;
        SELayerData layerData;
    }

    /**
     * A function to run when an event occurs within an {@link engine.SEButton}.
     * Passed to {@link engine.SEButton#SEButton(int, int, int, int, SEButtonFunc)} to create a {@link engine.SEButton}.
     */
    public interface SEButtonFunc {
        /**
         * This function will be called when a message is need to be sent to the respective SEButtonBundle.
         * @param obj The {@link engine.SEButton} that originated the call.
         * @param action The action that occurred within the {@link engine.SEButton}. One of the MOUSE_ constants.
         */
        void func(SEButton obj, SEMouseAction action);
    }

    /**
     * A simple, no return, no param function for queues among other things.
     */
    public interface SEInfoFunc {
        /**
         * A simple void, no param function.
         */
        void func();
    }

    /**
     * A keyboard messaging interface for sending keyboard information to the application.
     */
    public interface SEKeyFunc {
        /**
         * Called when a key is pressed, released or any other GLFW key related event.
         * @param key The key where the action applies to. One of the GLFW_KEY_ constants.
         * @param action The action applying to the key. A GLFW_ constant.
         */
        void key(int key, int action);
    }

    /**
     * A mouse messaging interface for sending mouse information to the application.
     */
    public interface SEMouseFunc {
        /**
         * Called when a mouse button is press, released, the mouse is moved or any other GLFW mouse/cursor related event.
         * @param x X coordinate relative to the top left of the window of cursor at the time of the action.
         * @param y Y coordinate relative to the top left of the window of cursor at the time of the action.
         * @param button The mouse button where that action applies (if any). One of the GLFW_MOUSE_BUTTON_ constants.
         * @param action The action applying to the mouse. One of the MOUSE_ constants.
         */
        void mouse(int x, int y, int button, SEMouseAction action);
    }

    /**
     * A messaging interface for sending messages to the application.
     */
    public interface SEMessageFunc {
        /**
         * Called when the engine wishes to send a message to the application.
         * @param type The type of the message. One of the MSG_ constants.
         * @param msg The specific message sent to your computer.
         */
        void msg(SEMessageType type, SEMessage msg);
    }

    /**
     * A bundle of functions containing key, mouse and message callbacks.
     */
    public static class SEFunctionBundle {
        /**
         * Constructor.
         * @param key The key callback function.
         * @param mouse The mouse callback function.
         * @param message The message callback function.
         */
        public SEFunctionBundle(SEKeyFunc key, SEMouseFunc mouse, SEMessageFunc message) {
            keyFunc = key; mouseFunc = mouse; messageFunc = message;
        }

        /**
         * Copy constructor.
         * @param copy The class to copy.
         */
        public SEFunctionBundle(SEFunctionBundle copy) {
            keyFunc = copy.keyFunc;
            mouseFunc = copy.mouseFunc;
            messageFunc = copy.messageFunc;
        }

        /**
         * Void constructor.
         */
        public SEFunctionBundle() {}

        /**
         * The key callback.
         */
        public SEKeyFunc keyFunc = (int key, int action) -> {};

        /**
         * The mouse callback.
         */
        public SEMouseFunc mouseFunc = (int x, int y, int button, SEMouseAction action) -> {};

        /**
         * The message callback.
         */
        public SEMessageFunc messageFunc = (SEMessageType type, SEMessage msg) -> {};
    }
    
    /**
     * A global offset bound to every {@link engine.SEWrappedObj} unless {@link engine.SEEngine#SEpreventBindOriginOffset} was enabled on the creation of the object.
     */
    public static final SEOffset ORIGIN_OFFSET = new SEOffset(0, 0);
    
    /**
     * Makes the program compatible with every version of SEEngine.
     * Used with: {@link engine.SEProgramData#compatibleVersions}.
     */
    public static final String ALL_VERSIONS = "all";
    /**
     * Makes the program compatible with the current version SEEngine.
     * Essentially the same thing as {@link engine.SEConstants#ALL_VERSIONS}.
     * Used with: {@link engine.SEProgramData#compatibleVersions}.
     */
    public static final String CURRENT_VERSION = SEEngine.SEversion();
    
    /**
     * Uses a on-off transparency system instead of a full blending solution (4 texture components).
     * Used with: {@link engine.SEProgramData#textureComponents}.
     */
    public static final byte FOURTH_COMPONENT_AS_DISCARD = 0x10;

    /**
     * Different fragment modes for SEEngine.
     */
    protected enum SEFragMode {
        /**
         * Shader: Basic pass through.
         */
        FRAG_MODE_NORMAL,
        /**
         * Shader: Discards fragments with &lt;0.5 alpha.
         */
        FRAG_MODE_ROUND_ALPHA,
        /**
         * Shader: Red to all components.
         */
        FRAG_MODE_GREYSCALE
    }

    /**
     * Different directions for {@link engine.SEObj#SEdirection(SEDirection)}.
     */
    public enum SEDirection {
        /**
         * Direction: Positive Y towards bottom.
         */
        DIRECTION_TOP_TO_BOTTOM,
        /**
         * Direction: Positive Y towards top.
         */
        DIRECTION_BOTTOM_TO_TOP,
        /**
         * Direction: Positive X towards right.
         */
        DIRECTION_LEFT_TO_RIGHT,
        /**
         * Direction: Positive X towards left.
         */
        DIRECTION_RIGHT_TO_LEFT
    }

    /**
     * All message types for the message callback.
     */
    public enum SEMessageType {
        /**
         * Indicates a debug message.
         */
        MSG_TYPE_DEBUG,
        /**
         * Indicates an informatic message.
         */
        MSG_TYPE_INFO,
        /**
         * Indicates a process requiring optimization.
         */
        MSG_TYPE_OPT,
        /**
         * Indicates a process requiring optimization that may screw with the functionality of your program.
         */
        MSG_TYPE_OPT_FUNC,
        /**
         * Indicates a failure in either the engine or the program.
         */
        MSG_TYPE_FAIL,
        /**
         * Indicates an incompatibility or complete failure in the engine that requires the engine to abort.
         */
        MSG_TYPE_FAIL_FATAL,
        /**
         * Indicates an error in OpenGL.
         */
        MSG_TYPE_OPENGL,
        /**
         * Indicates a message that contains something else.
         */
        MSG_TYPE_OTHER,
        /**
         * Indicates a message delivered to the current program by an external interface.
         */
        MSG_TYPE_EXTERNAL
    }

    /**
     * Different inherit modes for {@link engine.SEProgramData#inheritData}.
     */
    public enum SEInheritMode {
        /**
         * Inherits nothing from the previous program.
         */
        INHERIT_NONE,
        /**
         * Inherits values that are higher or equal to the current program data.
         */
        INHERIT_MINIMUM,
        /**
         * Inherits everything possible.
         */
        INHERIT_MOST
    }

    /**
     * Different mouse actions for the mouse callback.
     */
    public enum SEMouseAction {
        /**
         * A Mouse Action where a mouse have moved within the specified area.
         */
        MOUSE_MOVE,
        /**
         * Mouse Action: A mouse button has been pressed down within the specified area.
         */
        MOUSE_PRESS,
        /**
         * Mouse Action: A mouse button has been let go the specified area.
         */
        MOUSE_RELEASE,
        /**
         * Mouse Action: A mouse has entered the specified area.
         */
        MOUSE_ENTER,
        /**
         * Mouse Action: A mouse has exited the specified area.
         */
        MOUSE_EXIT
    }

    /**
     * List of messages for message callbacks.
     */
    public enum SEMessage {
        /**
         * There was a generic, unspecific and broad event.
         */
         MSG_GENERIC,
        /**
         * The engine is initializing.
         * Brace for fatal errors!
         */
         MSG_INIT,
        /**
         * The engine has entered the main program loop.
         * You can now be assured surprising incompatibility errors are behind you.
         */
         MSG_LOOP,
        /**
         * The engine is exiting.
         * It's just cleaning up some things, then it'll get right back to you.
         */
         MSG_EXIT,
        /**
         * GLFW failed to start.
         * We're not completely sure what went wrong, but it could be the machine the engine is running on.
         */
         MSG_GLFW_ERROR,
        /**
         * GLFW could not create a window.
         * We're not completely sure what went wrong, but it could be the environment of the application.
         */
         MSG_WINDOW_ERROR,
        /**
         * Something went wrong while loading the shaders.
         * Usually preceded by a {@link engine.SEConstants.SEMessage#MSG_SHADERS_VERTEX_COMPILE_ERROR}, {@link engine.SEConstants.SEMessage#MSG_SHADERS_FRAGMENT_COMPILE_ERROR}, {@link engine.SEConstants.SEMessage#MSG_SHADERS_LINK_ERROR}.
         */
         MSG_SHADERS_ERROR,
        /**
         * The program has claimed to be incompatible with the current version of SEEngine.
         * This is treated as fatal... usually.
         */
         MSG_INCOMPATIBLE_PROGRAM,
        /**
         * {@link engine.SEEngine#SEcatchOpenGLErrors} is enabled and OpenGL has reported back with an error.
         * Check the description for more information.
         */
         MSG_OPENGL_FEEDBACK_ERROR,
        /**
         * A message was sent to your application that uses a variable descriptions, but was possibly blocked.
         * {@link engine.SEEngine#SEpreventDoubleDescriptions} may be enabled which makes it impossible for descriptions to change.
         */
         MSG_LOG_WITH_DESCRIPTION_WARNING,
        /**
         * A call to {@link engine.SEEngine#SEgetFPS()} was made, but {@link engine.SEEngine#SEcalcFPS} was disabled so the returned value may not be accurate.
         */
         MSG_GET_FPS_WARNING,
        /**
         * A call to {@link engine.SEEngine#SEdraw()} was made, but the screen was going to be drawn anyways.
         */
         MSG_DRAW_WARNING,
        /**
         * A binding point for {@link engine.SEConstants.SEMessageType#MSG_TYPE_DEBUG} messages.
         * If you receive a {@link engine.SEConstants.SEMessageType#MSG_TYPE_DEBUG}, the description of it is probably here.
         */
         MSG_DEBUG_BINDING_POINT,
        /**
         * The vertex shader failed to compile.
         */
         MSG_SHADERS_VERTEX_COMPILE_ERROR,
        /**
         * The fragment shader failed to compile.
         */
         MSG_SHADERS_FRAGMENT_COMPILE_ERROR,
        /**
         * The shaders failed to link into a program.
         */
         MSG_SHADERS_LINK_ERROR,
        /**
         * An attempt was made to unbind an offset from an object that does not have that particular offset bound.
         */
         MSG_UNBIND_OFFSET_WARNING,
        /**
         * The object memory has been exhausted and the newest object cannot be created.
         */
         MSG_OUT_OF_OBJECT_MEMORY,
        /**
         * A wrapper was created with no objects inside it.
         */
         MSG_EMPTY_WRAPPER_CREATED,
        /**
         * There was a request to find a wrapped object that doesn't exist.
         * If you get this error, it's probably an internal bug.
         */
         MSG_UNKNOWN_WRAPPED_OBJECT,
        /**
         * {@link engine.SEEngine#SEdoubleWrappedObjects} was enabled, but an object was asked to be wrapped again after already being wrapped.
         */
         MSG_ALREADY_WRAPPED,
        /**
         * Some information was missing when checking the depth buffer.
         * This can happen when flickering {@link engine.SEEngine#SEuseWrappedObjectDepth} on and off.
         */
         MSG_MISSING_DEPTH_INFO,
        /**
         * We couldn't find the texture data when loading a texture.
         */
         MSG_MISSING_TEXTURE,
        /**
         * There was an issue when loading a texture into a Data object.
         */
         MSG_TEXTURE_LOAD_ERROR,
        /**
         * The two matrices attempted to be multiplied are incompatible.
         */
         MSG_INCOMPATIBLE_MATRICES,
        /**
         * The texture passed was null.
         */
         MSG_NULL_TEXTURE,
        /**
         * There isn't enough memory to load the new texture.
         */
         MSG_OUT_OF_TEXTURE_MEMORY,
        /**
         * The current computer could not create the appropriate context to create your application.
         */
         MSG_INCOMPATIBLE_CONTEXT,
        /**
         * Turns out OpenAL has reported back with an error.
         * Check the description for more information.
         */
         MSG_OPENAL_FEEDBACK_ERROR,
        /**
         * SESound is still experimental, don't play with it too much.
         * To initlize SESounds completely, call loadSounds trice.
         */
         MSG_EXPERIMENTAL_SOUND_WARNING,
        /**
         * A layer is being added, but layers themselves are disabled.
         * To enable layers, enable {@link engine.SEEngine#SEuseLayers}.
         */
         MSG_ADD_LAYER_WARNING,
        /**
         * The layer you're requesting for doesn't exist.
         * Or at least, we think it doesn't.
         */
         MSG_UNKNOWN_LAYER,
        /**
         * You passed an array to an offset that is not 2 in size.
         */
         MSG_OFFSET_TOO_LARGE,
        /**
         * Fake keys are disabled, yet you're submitting anyway.
         */
         MSG_FAKE_KEYS_DISABLED_WARNING
    }
}
