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

import static engine.SEConstants.*;

/**
 * This data structure is returned from a call to {@link engine.SEProgram#program()}.
 * It contains important data about a {@link engine.SEProgramData} object and how it should run.
 * Changing the data inside the object then returning the object through {@link engine.SEProgram#program()} can give you more control over your application.
 * @author desgroup
 * @version SEAlpha2a
 */
public class SEProgramData {

    public int
            /**
            * The width of the created window.
            */
            windowWidth = 600,
            /**
             * The height of the created window.
             */
            windowHeight = 600;

    /**
     * The name of the program.
     * Will be used for the title of the window.
     */
    public String programName = "Program";

    /**
     * Maximum amount of unique {@link engine.SEObj} that can exist at one time during the program's lifetime.
     * A call to {@link engine.SEObjects#SEcreateObject(int, int, int, int, SETex)} with all objects created will fail.
     */
    public int maxObjects = 512;
    
    public int
            /**
            * Maximum Texture Memory Width.
            * This major texture is used to store every single loaded {@link engine.SETex} object.
            * A call to {@link engine.SETextures#SEloadTexture(SERLogic.Data)} with no fitting space will fail.
            */
            texMemoryWidth = 1024,
            /**
             * Maximum Texture Memory Height.
             * This major texture is used to store every single loaded {@link engine.SETex} object.
             * A call to {@link engine.SETextures#SEloadTexture(SERLogic.Data)} with no fitting space will fail.
             */
            texMemoryHeight = 1024;

    /**
     * All compatible versions of SEEngine should be listed here separated by commas.
     * An "after:" prefix will allow every version after the version listed after the ":"
     * A "before:" prefix will prevent any version after or equal to the version listed after the ":"
     */
    public String compatibleVersions = "all";

    /**
     * Amount of texture components to use for textures.
     * Negative values (including 0) and the value 2 is not supported.
     * Value 1 is gray scale (by the red component), 3 is full color, and 4 is full transparency.
     * Any other value or {@link engine.SEConstants#FOURTH_COMPONENT_AS_DISCARD} will have an approximate on/off transparency mode (&lt;0.5 is fully transparent).
     */
    public byte textureComponents = 4;

    /**
     * If this value is true, the program will start in full screen.
     */
    public boolean isFullScreen = false;

    /**
     * Determines how much of the previous program is inherited in {@link engine.SEEngine#SEswitchPrograms(SEControlledProgram, boolean)} calls.
     * Should be one of the INHERIT_ constants.
     */
    public byte inheritData = INHERIT_NONE;

    /**
     * If this value is true, a quick clear will be used for values that have been inherited or are the same during a {@link engine.SEEngine#SEswitchPrograms(SEControlledProgram, boolean)}.
     * A quick clear only invalidates data instead of completely erasing it.
     */
    public boolean useQuickClear = false;

    /**
     * Controls the background color of the program.
     * The first component is red, follow by green, blue and alpha.
     */
    public float[] bkgColor = {0, 0, 0, 1};
    
    /**
     * Returns the name of the message type.
     * @param msgType The message type. One of the MSG_ constants.
     * @return A string describing the MSG_ constant.
     */
    public static String SEmsgDesc(byte msgType) {
        String desc = "MSG_NO_DESC";
        switch (msgType) {
            case MSG_TYPE_DEBUG: desc = "MSG_DEBUG"; break;
            case MSG_TYPE_INFO: desc = "MSG_INFO"; break;
            case MSG_TYPE_OPT: desc = "MSG_OPT"; break;
            case MSG_TYPE_OPT_FUNC: desc = "MSG_OPT_FUNC"; break;
            case MSG_TYPE_FAIL: desc = "MSG_FAIL"; break;
            case MSG_TYPE_FAIL_FATAL: desc = "MSG_FAIL_FATAL"; break;
            case MSG_TYPE_OPENGL: desc = "MSG_OPENGL"; break;
            case MSG_TYPE_OTHER: desc = "MSG_OTHER"; break;
            case MSG_TYPE_EXTERNAL: desc = "MSG_EXTERNAL"; break;
        }
        return desc;
    }
    
    /**
     * A keyboard messaging interface for {@link engine.SEProgramData#keyFunc}.
     */
    public interface SEKeyFunc { 

        /**
         * Called when a key is pressed, released or any other GLFW key related event.
         * @param key The key where the action applies to. One of the GLFW_KEY_ constants.
         * @param action The action applying to the key. A GLFW_ constant.
         */
        void key(int key, int action); }

    /**
     * A mouse messaging interface for {@link engine.SEProgramData#mouseFunc}.
     */
    public interface SEMouseFunc { 

        /**
         * Called when a mouse button is press, released, the mouse is moved or any other GLFW mouse/cursor related event.
         * @param x X coordinate relative to the top left of the window of cursor at the time of the action.
         * @param y Y coordinate relative to the top left of the window of cursor at the time of the action.
         * @param button The mouse button where that action applies (if any). One of the GLFW_MOUSE_BUTTON_ constants.
         * @param action The action applying to the mouse. One of the MOUSE_ constants.
         */
        void mouse(int x, int y, int button, int action); }

    /**
     * A messaging interface for simple messages for {@link engine.SEProgramData#messageFunc}.
     */
    public interface SEMessageFunc { 

        /**
         * Called when the engine wishes to send a message to the application.
         * @param type The type of the message. One of the MSG_ constants.
         * @param msg The specific message sent to your computer.
         */
        void msg(byte type, int msg); }
    
    /**
     * Your program's supplied key function.
     */
    public SEKeyFunc keyFunc = (int key, int action)->{};

    /**
     * Your program's supplied mouse function.
     */
    public SEMouseFunc mouseFunc = (int x, int y, int button, int action)->{};

    /**
     * Your program's supplied message function.
     */
    public SEMessageFunc messageFunc = (byte type, int msg)->{};
}