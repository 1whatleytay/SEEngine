/*
 * SEEngine OpenGL 2.0 Engine
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
 * @version SEAlpha2a
 */
public class SEConstants {
    /**
     * A global offset bound to every {@link engine.SEWrappedObj} unless {@link engine.SEEngine#SEpreventBindOriginOffset} was enabled on the creation of the object.
     */
    public static final String ORIGIN_OFFSET = "offset1";
    
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
     * Shader: Basic pass through.
     */
    protected static final byte FRAG_MODE_NORMAL = 0x20;
    /**
     * Shader: Discards fragments with &lt;0.5 alpha.
     */
    protected static final byte FRAG_MODE_ROUND_ALPHA = 0x21;
    /**
     * Shader: Red to all components.
     */
    protected static final byte FRAG_MODE_GREYSCALE = 0x22;
    
    /**
    * Direction: Positive Y towards bottom.
    */
    public static final byte DIRECTION_TOP_TO_BOTTOM = 0x30;
    /**
     * Direction: Positive Y towards top.
     */
    protected static final byte DIRECTION_BOTTOM_TO_TOP = 0x31;
    /**
     * Direction: Positive X towards right.
     */
    protected static final byte DIRECTION_LEFT_TO_RIGHT = 0x32;
    /**
     * Direction: Positive X towards left.
     */
    protected static final byte DIRECTION_RIGHT_TO_LEFT = 0x33;
    
    /**
    * Indicates a debug message.
    */
    public static final byte MSG_TYPE_DEBUG = 0x40;
    /**
     * Indicates an informatic message.
     */
    protected static final byte MSG_TYPE_INFO = 0x41;
    /**
     * Indicates a process requiring optimization.
     */
    protected static final byte MSG_TYPE_OPT = 0x42;
    /**
     * Indicates a process requiring optimization that may screw with the functionality of your program.
     */
    protected static final byte MSG_TYPE_OPT_FUNC = 0x43;
    /**
     * Indicates a failure in either the engine or the program.
     */
    protected static final byte MSG_TYPE_FAIL = 0x44;
    /**
     * Indicates an incompatibility or complete failure in the engine that requires the engine to abort.
     */
    protected static final byte MSG_TYPE_FAIL_FATAL = 0x45;
    /**
     * Indicates an error in OpenGL.
     */
    protected static final byte MSG_TYPE_OPENGL = 0x46;
    /**
     * Indicates a message that contains something else.
     */
    protected static final byte MSG_TYPE_OTHER = 0x47;
        /**
         * Indicates a message delivered to the current program by an external interface.
         */
    protected static final byte MSG_TYPE_EXTERNAL = 0x48;
    
    /**
     * Inherits nothing from the previous program.
     */
    public static final byte INHERIT_NONE = 0x50;
    /**
     * Inherits values that are higher or equal to the current program data.
     */
    protected static final byte INHERIT_MINIMUM = 0x51;
    /**
     * Inherits everything possible.
     */
    protected static final byte INHERIT_MOST = 0x52;
    
    /**
     * A Mouse Action where a mouse have moved within the specified area.
     */
    public static final byte MOUSE_MOVE = 0x60;
    /**
     * Mouse Action: A mouse button has been pressed down within the specified area.
     */
    protected static final byte MOUSE_PRESS = 0x61;
    /**
     * Mouse Action: A mouse button has been let go the specified area.
     */
    protected static final byte MOUSE_RELEASE = 0x62;
    /**
     * Mouse Action: A mouse has entered the specified area.
     */
    protected static final byte MOUSE_ENTER = 0x63;
    /**
     * Mouse Action: A mouse has exited the specified area.
     */
    protected static final byte MOUSE_EXIT = 0x64;
    
    /**
     * There was a generic, unspecific and broad event.
     */
    public static final int MSG_GENERIC = 0x00;
    /**
     * The engine is initializing.
     * Brace for fatal errors!
     */
    public static final int MSG_INIT = 0x01;
    /**
     * The engine has entered the main program loop.
     * You can now be assured surprising incompatibility errors are behind you.
     */
    public static final int MSG_LOOP = 0x02;
    /**
     * The engine is exiting.
     * It's just cleaning up some things, then it'll get right back to you.
     */
    public static final int MSG_EXIT = 0x03;
    /**
     * GLFW failed to start.
     * We're not completely sure what went wrong, but it could be the machine the engine is running on.
     */
    public static final int MSG_GLFW_ERROR = 0x04;
    /**
     * GLFW could not create a window.
     * We're not completely sure what went wrong, but it could be the environment of the application.
     */
    public static final int MSG_WINDOW_ERROR = 0x05;
    /**
     * Something went wrong while loading the shaders.
     * Usually preceded by a {@link engine.SEConstants#MSG_SHADERS_VERTEX_COMPILE_ERROR}, {@link engine.SEConstants#MSG_SHADERS_FRAGMENT_COMPILE_ERROR}, {@link engine.SEConstants#MSG_SHADERS_LINK_ERROR}.
     */
    public static final int MSG_SHADERS_ERROR = 0x06;
    /**
     * The program has claimed to be incompatible with the current version of SEEngine.
     * This is treated as fatal... usually.
     */
    public static final int MSG_INCOMPATIBLE_PROGRAM = 0x07;
    /**
     * {@link engine.SEEngine#SEcatchOpenGLErrors} is enabled and OpenGL has reported back with an error.
     * Check the description for more information.
     */
    public static final int MSG_OPENGL_FEEDBACK_ERROR = 0x08;
    /**
     * A message was sent to your application that uses a variable descriptions, but was possibly blocked.
     * {@link engine.SEEngine#SEpreventDoubleDescriptions} may be enabled which makes it impossible for descriptions to change.
     */
    public static final int MSG_LOG_WITH_DESCRIPTION_WARNING = 0x09;
    /**
     * A call to {@link engine.SEEngine#SEgetFPS()} was made, but {@link engine.SEEngine#SEcalcFPS} was disabled so the returned value may not be accurate.
     */
    public static final int MSG_GET_FPS_WARNING = 0x0a;
    /**
     * A call to {@link engine.SEEngine#SEdraw()} was made, but the screen was going to be drawn anyways.
     */
    public static final int MSG_DRAW_WARNING = 0x0b;
    /**
     * A binding point for {@link engine.SEConstants#MSG_TYPE_DEBUG} messages.
     * If you receive a {@link engine.SEConstants#MSG_TYPE_DEBUG}, the description of it is probably here.
     */
    public static final int MSG_DEBUG_BINDING_POINT = 0x0c;
    /**
     * The vertex shader failed to compile.
     */
    public static final int MSG_SHADERS_VERTEX_COMPILE_ERROR = 0x0d;
    /**
     * The fragment shader failed to compile.
     */
    public static final int MSG_SHADERS_FRAGMENT_COMPILE_ERROR = 0x0e;
    /**
     * The shaders failed to link into a program.
     */
    public static final int MSG_SHADERS_LINK_ERROR = 0x0f;
    /**
     * An attempt was made to unbind an offset from an object that does not have that particular offset bound.
     */
    public static final int MSG_UNBIND_OFFSET_WARNING = 0x10;
    /**
     * The object memory has been exhausted and the newest object cannot be created.
     */
    public static final int MSG_OUT_OF_OBJECT_MEMORY = 0x11;
    /**
     * A wrapper was created with no objects inside it.
     */
    public static final int MSG_EMPTY_WRAPPER_CREATED = 0x12;
    /**
     * There was a request to find a wrapped object that doesn't exist.
     * If you get this error, it's probably an internal bug.
     */
    public static final int MSG_UNKNOWN_WRAPPED_OBJECT = 0x13;
    /**
     * {@link engine.SEEngine#SEdoubleWrappedObjects} was enabled, but an object was asked to be wrapped again after already being wrapped.
     */
    public static final int MSG_ALREADY_WRAPPED = 0x14;
    /**
     * Some information was missing when checking the depth buffer.
     * This can happen when flickering {@link engine.SEEngine#SEwrappedObjectDepth} on and off.
     */
    public static final int MSG_MISSING_DEPTH_INFO = 0x15;
    /**
     * We couldn't find the texture data when loading a texture.
     */
    public static final int MSG_MISSING_TEXTURE = 0x16;
    /**
     * There was an issue when loading a texture into a Data object.
     */
    public static final int MSG_TEXTURE_LOAD_ERROR = 0x17;
    /**
     * The two matrices attempted to be multiplied are incompatible.
     */
    public static final int MSG_INCOMPATIBLE_MATRICES = 0x18;
    /**
     * The texture passed was null.
     */
    public static final int MSG_NULL_TEXTURE = 0x19;
    /**
     * There isn't enough memory to load the new texture.
     */
    public static final int MSG_OUT_OF_TEXTURE_MEMORY = 0x1a;
    /**
     * The current computer could not create the appropriate context to create your application.
     */
    public static final int MSG_INCOMPATIBLE_CONTEXT = 0x1b;
}
