package engine;

import java.io.File;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

public class Shaders {
    static String[] shaderSources = {
              "#version 120\n"
            + "\n"
            + "attribute vec2 position;\n"
            + "attribute vec2 texCoord;\n"
            + "\n"
            + "varying vec2 texCoord_out;\n"
            + "\n"
            + "void main() {\n"
            + " texCoord_out = texCoord;\n"
            + " gl_Position = vec4(position, 0.0, 1.0);\n"
            + "}",
        
              "#version 120\n"
            + "\n"
            + "uniform sampler2D tex;\n"
            + "\n"
            + "varying vec2 texCoord_out;\n"
            + "\n"
            + "void main() {\n"
            + " gl_FragColor = texture2D(tex, texCoord_out);\n"
            + "}",
    };
    
    static int shaderProgram = -1;
    
    static int att_position = -1;
    static int att_texCoord = -1;
    static boolean loadProgram() {
        //Load, Compile, Link...
        shaderProgram = glCreateProgram();
        int vShader = glCreateShader(GL_VERTEX_SHADER);
        int fShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(vShader, shaderSources[0]);
        glShaderSource(fShader, shaderSources[1]);
        glCompileShader(vShader);
        if (glGetShaderi(vShader, GL_COMPILE_STATUS) != GL_TRUE) {
            Engine.log("Failed to compile vertex shader:\n" + glGetShaderInfoLog(vShader));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        glCompileShader(fShader);
        if (glGetShaderi(fShader, GL_COMPILE_STATUS) != GL_TRUE) {
            Engine.log("Failed to compile fragment shader:\n" + glGetShaderInfoLog(fShader));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        glAttachShader(shaderProgram, vShader);
        glAttachShader(shaderProgram, fShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) != GL_TRUE) {
            Engine.log("Failed to link shader program:\n" + glGetProgramInfoLog(shaderProgram));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        //Use/Setup...
        glUseProgram(shaderProgram);
        //Attributes...
        att_position = glGetAttribLocation(shaderProgram, "position");
        att_texCoord = glGetAttribLocation(shaderProgram, "texCoord");
        //Uniforms...
        /*None!*/
        //Values...
        /*None!*/
        return true;
    }
    
    static void createPointer() {
        glUseProgram(shaderProgram);
        glVertexAttribPointer(att_position, 2, GL_FLOAT, false, Float.SIZE/8*4, 0);
        glVertexAttribPointer(att_texCoord, 2, GL_FLOAT, false, Float.SIZE/8*4, Float.SIZE/8*2);
        glEnableVertexAttribArray(att_position);
        glEnableVertexAttribArray(att_texCoord);
    }
}

/*1gucha@hdsb.ca
quvg*/