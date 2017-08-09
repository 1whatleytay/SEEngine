package engine;

import java.io.File;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import org.lwjgl.opengl.GL20;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

import static engine.SEProgramData.*;

//Handles SEEngine's shaders.
public class SEIShaders {
    private SEIShaders() {}
    
    //A list of shader sources to use depending on your program's SEProgramData.
    private static final String[] shaderSources = {
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
    
    //Number that represent different fragment shaders in the shader list.
    protected static final byte
            FRAG_MODE_NORMAL = 0x20,
            FRAG_MODE_ROUND_ALPHA = 0x21,
            FRAG_MODE_GREYSCALE = 0x22;
    
    //One of the numbers above that points to one of the fragment shaders.
    protected static byte fragComponentMode = FRAG_MODE_NORMAL;
    
    //The OpenGL shader program.
    protected static int shaderProgram = -1;
    
    //Shader attributes...
    private static int att_position = -1;
    private static int att_texCoord = -1;
    
    //Shader uniforms...
    private static int uni_offset = -1;
    private static int uni_matrix_center = -1;
    private static int uni_matrix = -1;
    
    //Loads the correct shaders based on fragComponentMode.
    protected static boolean loadProgram() {
        //Load, Compile, Link...
        shaderProgram = glCreateProgram();
        int vShader = glCreateShader(GL_VERTEX_SHADER);
        int fShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(vShader, shaderSources[0]);
        glShaderSource(fShader, shaderSources[fragComponentMode - 0x20 + 1]);
        glCompileShader(vShader);
        if (glGetShaderi(vShader, GL_COMPILE_STATUS) != GL_TRUE) {
            SEEngine.log(MSG_FAIL_FATAL, "Failed to compile vertex shader:\n" + glGetShaderInfoLog(vShader));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        glCompileShader(fShader);
        if (glGetShaderi(fShader, GL_COMPILE_STATUS) != GL_TRUE) {
            SEEngine.log(MSG_FAIL_FATAL, "Failed to compile fragment shader:\n" + glGetShaderInfoLog(fShader));
            glDeleteProgram(shaderProgram); glDeleteShader(vShader); glDeleteShader(fShader);
            return false;
        }
        glAttachShader(shaderProgram, vShader);
        glAttachShader(shaderProgram, fShader);
        glLinkProgram(shaderProgram);
        if (glGetProgrami(shaderProgram, GL_LINK_STATUS) != GL_TRUE) {
            SEEngine.log(MSG_FAIL_FATAL, "Failed to link shader program:\n" + glGetProgramInfoLog(shaderProgram));
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
    
    //Some shader interaction functions...
    protected static void matrix(SERLogic.Data data) { glUniformMatrix2fv(uni_matrix, false, data.data); }
    protected static void matrix_center(int xMat, int yMat) { glUniform2f(uni_matrix_center, (xMat / SEEngine.scWidth - 0.5f) * 2 * SEObjects.ampX, (yMat / SEEngine.scHeight - 0.5f) * 2 * SEObjects.ampY); }
    protected static void offset(int xOffset, int yOffset) { glUniform2f(uni_offset, (float)xOffset / (float)SEEngine.scWidth * SEObjects.ampX, (float)yOffset / (float)SEEngine.scHeight * SEObjects.ampY); }
    
    //Add Vertex Attribute Pointers to the currently bound buffer...
    protected static void createPointer() {
        glUseProgram(shaderProgram);
        glVertexAttribPointer(att_position, 2, GL_FLOAT, false, Float.SIZE/8*4, 0);
        glVertexAttribPointer(att_texCoord, 2, GL_FLOAT, false, Float.SIZE/8*4, Float.SIZE/8*2);
        glEnableVertexAttribArray(att_position);
        glEnableVertexAttribArray(att_texCoord);
    }
}