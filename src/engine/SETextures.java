package engine;

import static org.lwjgl.opengl.GL11.*;
import static engine.SERLogic.Data;

import static engine.SEProgramData.*;
import org.lwjgl.opengl.GL11;

//Handles loading and storing textures.
public class SETextures {
    private SETextures() {}
    
    //Stores immediately accessible data on the system for quick edititng
    private static Data texMap = null;
    //Stores which pixels are open for storage and which aren't
    private static boolean[] texSpace = null;
    //OpenGL texture
    private static int mainTex = -1;
    
    //Small math function to access data in texMap
    private static int texMap(int x, int y, int width) { return (x + y * width) * SERImages.components; }
    
    protected static int getTexDimWidth() { return texMap.width; }
    protected static int getTexDimHeight() { return texMap.height; }
    
    //A black solid color texture
    public static final SETex BLANK_TEXTURE = new SETex();
    
    //Loads texture data tex into system and returns the a reference object
    //SETex. You'll need to create an SETex object if you wish to use your
    //data in objects. You can receive a data object from
    //SERImages.getAsGLTexture(String) or by filling in a Data object yourself.
    //The width and height of the Data structure must be the one of the actual
    //image, however the Data's data array will consider every
    //textureComponents (from SEProgramData structure) elements to be one pixel,
    //and after width pixels has been read, the height increments. Basically,
    //OpenGL texture data (in order of red, green, blue, alpha per pixel).
    public static SETex SEloadTexture(Data tex) {
        if (tex == null) { SEEngine.log(MSG_FAIL, "Texture data is null!"); return null; }
        if (tex.width > texMap.width || tex.height > texMap.height) { SEEngine.log(MSG_OPT, "Texture Does Not Fit in Given Memory"); return null; }
        boolean found = false;
        int findX = 0, findY = 0;
        for (int scanX = 0; scanX < texMap.width - tex.width + 1; scanX++) {
            for (int scanY = 0; scanY < texMap.height - tex.height + 1; scanY++) {
                boolean isFound = false;
                for (int scanPX = 0; scanPX < tex.width; scanPX++) {
                    for (int scanPY = 0; scanPY < tex.height; scanPY++) {
                        if (texSpace[(scanX + scanPX) + (scanY + scanPY) * texMap.width]) { isFound = true; break; }
                    }
                    if (isFound) break;
                }
                if (!isFound) { found = true; findX = scanX; findY = scanY; break; }
            }
            if (found) break;
        }
        if (!found) { SEEngine.log(MSG_OPT, "Out Of Texture Memory"); return null; }
        for (int x = 0; x < tex.width; x++) {
            for (int y = 0; y < tex.height; y++) {
                int texMapPoint = texMap(x + findX, y + findY, texMap.width);
                int texPoint = texMap(x, y, tex.width);
                for (int a = 0; a < SERImages.components; a++) {
                    texMap.data[texMapPoint + a] = tex.data[texPoint + a];
                }
                texSpace[(x + findX) + (y + findY) * texMap.width] = true;
            }
        }
        glBindTexture(GL_TEXTURE_2D, mainTex);
        glTexSubImage2D(GL_TEXTURE_2D, 0, findX, findY, tex.width, tex.height, SERImages.COMPONENT_REFERENCE[SERImages.components], GL_FLOAT, tex.data);
        SETex texture = new SETex();
        texture.texX = (float)findX / (float)texMap.width; texture.texY = (float)findY / (float)texMap.height;
        texture.texW = (float)tex.width / (float)texMap.width;  texture.texH = (float)tex.height / (float)texMap.height;
        return texture;
    }
    
    //String versions of SEloadTexture(Data)
    public static SETex SEloadTexture(String path) { return SEloadTexture(SERImages.SEgetImageData(path)); }
    
    //Samples a texture from somewhere in memory. This might be unsafe.
    //Planned for removal.
    public static SETex SEsampleTexture(int x, int y, int w, int h) {
        SETex tex = new SETex();
        tex.texX = (float)x / (float)texMap.width; tex.texY = (float)y / (float)texMap.height;
        tex.texW = (float)w / (float)texMap.width; tex.texH = (float)h / (float)texMap.height;
        return tex;
    }
    
    //Samples a section of an existing texture. Texture bounderies are not
    //respected. This might be unsafe. Probably buggy!
    public static SETex SEsampleTexture(SETex mainTex, int offX, int offY, int width, int height) {
        SETex tex = new SETex();
        tex.texX = (mainTex.texX * texMap.width + offX) / texMap.width;
        tex.texY = (mainTex.texY * texMap.height + offY) / texMap.height;
        tex.texW = (double)width / (double)texMap.width;
        tex.texH = (double)height / (double)texMap.height;
        return tex;
    }
    
    //Stores the texture data in tex in texture. This might be unsafe.
    //Planned for removal.
    public static void SEoverrideTexture(SETex texture, Data tex) {
        int findX = (int)(texture.texX * texMap.width), findY = (int)(texture.texY * texMap.height);
        for (int x = 0; x < tex.width; x++) {
            for (int y = 0; y < tex.height; y++) {
                int texMapPoint = texMap(x + findX, y + findY, texMap.width);
                int texPoint = texMap(x, y, tex.width);
                for (int a = 0; a < SERImages.components; a++) {
                    texMap.data[texMapPoint + a] = tex.data[texPoint + a];
                }
                texSpace[(x + findX) + (y + findY) * texMap.width] = true;
            }
        }
        glBindTexture(GL_TEXTURE_2D, mainTex);
        glTexSubImage2D(GL_TEXTURE_2D, 0, findX, findY, tex.width, tex.height, SERImages.COMPONENT_REFERENCE[SERImages.components], GL_FLOAT, tex.data);
    }
    
    //Deletes a texture and frees up the space for other things. Once you're
    //done with the SETex you have, call this to free up the space for other
    //textures.
    public static void SEdeleteTexture(SETex texture) {
        if (texture == null) { SEEngine.log(MSG_OPT, "Null Textures cannot be Deleted"); return; }
        for (int x = 0; x < texture.texW * texMap.width; x++) {
            for (int y = 0; y < texture.texH * texMap.height; y++) {
                texSpace[(int)(x + texture.texX * texMap.width) + (int)(y + texture.texY * texMap.height) * texMap.width] = false;
            }
        }
    }
    
    //Loads a single color into memory and returns an SETex. Only use red
    //component for greyscale colors.
    public static SETex SEloadColor(float red, float green, float blue, float alpha) {
        return SEloadTexture(new Data(new float[]{red, green, blue, alpha}, 1, 1));
    }
    
    //SEloadColor with alpha component set to 1.
    public static SETex SEloadColor(float red, float green, float blue) { return SEloadColor(red, green, blue, 1); }
    
    //Fully clears all texture data.
    protected static void clearTextures(int texWidth, int texHeight) {
        texSpace = new boolean[texWidth * texHeight];
        texMap = new Data(new float[texWidth * SERImages.components * texHeight], texWidth, texHeight);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDeleteTextures(mainTex);
        mainTex = glGenTextures();
        SERImages.loadTexture(texMap, mainTex);
    }
    
    //Invalidates texture data for a quick clear.
    protected static void quickClearTextures() {
        texSpace = new boolean[texSpace.length];
    }
    
    //Setup function for SETextures
    protected static void loadTextures(int texWidth, int texHeight) {
        texSpace = new boolean[texWidth * texHeight];
        texMap = new Data(new float[texWidth * SERImages.components * texHeight], texWidth, texHeight);
        mainTex = glGenTextures();
        SERImages.loadTexture(texMap, mainTex);
        BLANK_TEXTURE.texX = 0; BLANK_TEXTURE.texY = 0;
        BLANK_TEXTURE.texW = 0; BLANK_TEXTURE.texH = 0;
    }
}
