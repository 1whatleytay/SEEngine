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

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import static engine.SEConstants.*;
import static engine.SEProgramData.*;

/**
 * Handles creating, loading and changing shaders.
 * @author desgroup
 * @version SEAlpha2a
 */
public class SEIShaders {
    private SEIShaders() {}
    
    private static final String[] SHADER_SOURCES = {
            //Vertex Shader
              "#version 120\n"
            + "\n"
            + "attribute vec2 position;\n"
            + "attribute vec2 texCoord;\n"
            + "\n"
            + "varying vec2 texCoord_out;\n"
            + "\n"
            + "uniform vec2 offset;\n"
            + "uniform vec2 matrix_center;\n"
            + "uniform mat2 matrix;\n"
            + "\n"
            + "void main() {\n"
            + " texCoord_out = texCoord;\n"
            + " gl_Position = vec4((position - matrix_center) * matrix + matrix_center + offset, 0.0, 1.0);\n"
            + "}",
            //Frag Shader
              "#version 120\n"
            + "\n"
            + "uniform sampler2D tex;\n"
            + "\n"
            + "varying vec2 texCoord_out;\n"
            + "\n"
            + "void main() {\n"
            + " gl_FragColor = texture2D(tex, texCoord_out);\n"
            + "}",
            //Frag Shader Round Alpha
              "#version 120\n"
            + "\n"
            + "uniform sampler2D tex;\n"
            + "\n"
            + "varying vec2 texCoord_out;\n"
            + "\n"
            + "void main() {\n"
            + " vec4 texColor = texture2D(tex, texCoord_out);\n"
            + " if (texColor.a < 0.5) discard;\n"
            + " gl_FragColor = texColor;\n"
            + "}",
            //Frag Shader Grey Scale
            "#version 120\n"
            + "\n"
            + "uniform sampler2D tex;\n"
            + "\n"
            + "varying vec2 texCoord_out;\n"
            + "\n"
            + "void main() {\n"
            + " vec4 texColor = texture2D(tex, texCoord_out);\n"
            + " gl_FragColor = vec4(texColor.r, texColor.r, texColor.r, texColor.a);\n"
            + "}",
    };

    /**
     * The current fragment shader.
     * Should be a FRAG_MODE_ constant.
     */
    protected static byte fragComponentMode = FRAG_MODE_NORMAL;

    /**
     * The OpenGL shader program.
     */
    protected static int shaderProgram = -1;
    
    private static int att_position = -1;
    private static int att_texCoord = -1;
    
    private static int uni_offset = -1;
    private static int uni_matrix_center = -1;
    private static int uni_matrix = -1;

    /**
     * Loads and initializes the requested shaders.
     * @return True if the function was successful, false otherwise.
     */
    protected static boolean loadProgram() {
        //Load, Compile, Link...
        shaderProgram = glCreateProgram();
        int vShader = glCreateShader(GL_VERTEX_SHADER);
        int fShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(vShader, SHADER_SOURCES[0]);
        glShaderSource(fShader, SHADER_SOURCES[fragComponentMode - 0x20 + 1]);
        glCompileShader(vShader);
        if (glGetShaderi(vShader, GL_COMPILE_STATUS) != GL_TRUE) {
            SEEngine.logWithDescription(MSG_TYPE_FAIL_FATAL, MSG_SHADERS_VERTEX_COMPILE_ERROR, "Failed to compile vertex shader:\n" + glGetShaderInfoLog(vShader));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        glCompileShader(fShader);
        if (glGetShaderi(fShader, GL_COMPILE_STATUS) != GL_TRUE) {
            SEEngine.logWithDescription(MSG_TYPE_FAIL_FATAL, MSG_SHADERS_VERTEX_COMPILE_ERROR, "Failed to compile fragment shader:\n" + glGetShaderInfoLog(fShader));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        glAttachShader(shaderProgram, vShader);
        glAttachShader(shaderProgram, fShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) != GL_TRUE) {
            SEEngine.logWithDescription(MSG_TYPE_FAIL_FATAL, MSG_SHADERS_LINK_ERROR, "Failed to link shader program:\n" + glGetProgramInfoLog(shaderProgram));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        //Use/Setup...
        glUseProgram(shaderProgram);
        //Attributes...
        att_position = glGetAttribLocation(shaderProgram, "position");
        att_texCoord = glGetAttribLocation(shaderProgram, "texCoord");
        //Uniforms...
        uni_offset = glGetUniformLocation(shaderProgram, "offset");
        uni_matrix_center = glGetUniformLocation(shaderProgram, "matrix_center");
        uni_matrix = glGetUniformLocation(shaderProgram, "matrix");
        //Values...
        glUniform2f(uni_offset, 0f, 0f);
        glUniform2f(uni_matrix_center, 0f, 0f);
        glUniformMatrix2fv(uni_matrix, false, SERLogic.genIdentityMatrix().data);
        return true;
    }
    
    /**
     * Sets the current matrix to data.
     * @param data The new matrix (2x2 Data Structure) to be set as the current matrix.
     */
    protected static void matrix(SERLogic.Data data) { glUniformMatrix2fv(uni_matrix, false, data.data); }

    /**
     * Changes the current matrix center to xMat, yMat.
     * @param xMat The x position of the new matrix center (in pixels).
     * @param yMat The y position of the new matrix center (in pixels).
     */
    protected static void matrix_center(int xMat, int yMat) { glUniform2f(uni_matrix_center, (xMat / SEEngine.scWidth - 0.5f) * 2 * SEObjects.ampX, (yMat / SEEngine.scHeight - 0.5f) * 2 * SEObjects.ampY); }

    /**
     * Changes the current offset (for the shader) to xOffset, yOffset.
     * @param xOffset The x offset to be used.
     * @param yOffset The y offset to be used.
     */
    protected static void offset(int xOffset, int yOffset) { glUniform2f(uni_offset, (float)xOffset / (float)SEEngine.scWidth * SEObjects.ampX * 2, (float)yOffset / (float)SEEngine.scHeight * SEObjects.ampY * 2); }
    
    /**
     * Adds Vertex Attribute Pointers to the currently bound buffer.
     */
    protected static void createPointer() {
        glUseProgram(shaderProgram);
        glVertexAttribPointer(att_position, 2, GL_FLOAT, false, Float.SIZE/8*4, 0);
        glVertexAttribPointer(att_texCoord, 2, GL_FLOAT, false, Float.SIZE/8*4, Float.SIZE/8*2);
        glEnableVertexAttribArray(att_position);
        glEnableVertexAttribArray(att_texCoord);
    }
}