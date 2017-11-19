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

import static engine.SEConstants.*;
import java.util.Arrays;

/**
 * This data structure is returned from a call to {@link engine.SEProgram#program()}.
 * It contains important data about a {@link engine.SEProgramData} object and how it should run.
 * Changing the data inside the object then returning the object through {@link engine.SEProgram#program()} can give you more control over your application.
 * @author desgroup
 * @version SEAlpha2a
 */
public class SEProgramData {
    
    public SEProgramData(SEProgramData copy) {
        windowWidth = copy.windowWidth; windowHeight = copy.windowHeight;
        programName = copy.programName; maxObjects = copy.maxObjects;
        texMemoryWidth = copy.texMemoryWidth; texMemoryHeight = copy.texMemoryHeight;
        compatibleVersions = copy.compatibleVersions; textureComponents = copy.textureComponents;
        isFullScreen = copy.isFullScreen;
        inheritData = copy.inheritData; useQuickClear = copy.useQuickClear;
        bkgColor = Arrays.copyOf(copy.bkgColor, 4);
        functions = new SEFunctionBundle(copy.functions);
    }
    
    public SEProgramData() {}
    
    /**
    * The width of the created window.
    */
    public int windowWidth = 600;
    /**
     * The height of the created window.
     */
    public int windowHeight = 600;

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
    
    /**
    * Maximum Texture Memory Width.
    * This major texture is used to store every single loaded {@link engine.SETex} object.
    * A call to {@link engine.SETextures#SEloadTexture(SERLogic.Data)} with no fitting space will fail.
    */
    public int texMemoryWidth = 1024;
    /**
     * Maximum Texture Memory Height.
     * This major texture is used to store every single loaded {@link engine.SETex} object.
     * A call to {@link engine.SETextures#SEloadTexture(SERLogic.Data)} with no fitting space will fail.
     */
    public int texMemoryHeight = 1024;

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
     * Your program's supplied response functions.
     */
    public SEFunctionBundle functions = new SEFunctionBundle();
}