package engine;

import static org.lwjgl.opengl.GL11.*;
import static engine.SERLogic.Data;

public class SETextures {
    private SETextures() {}
    
    private static Data texMap = null;
    private static boolean[] texSpace = null;
    private static int mainTex = -1;
    
    private static int texMap(int x, int y, int width) { return (x + y * width) * SERImages.components; }
    
    public static final SETex BLANK_TEXTURE = new SETex();
    
    protected static void loadTextures(int texWidth, int texHeight) {
        texMap = new Data(new float[texWidth * SERImages.components * texHeight], texWidth, texHeight);
        texSpace = new boolean[texWidth * texHeight];
        mainTex = glGenTextures();
        SERImages.loadTexture(texMap, mainTex);
        BLANK_TEXTURE.texX = 0; BLANK_TEXTURE.texY = 0;
        BLANK_TEXTURE.texW = 0; BLANK_TEXTURE.texH = 0;
    }
    
    public static SETex SEloadTexture(String path) {
        return SEloadTexture(SERImages.getAsGLTexture(path));
    }
    
    public static SETex SEsampleTexture(int x, int y, int w, int h) {
        SETex tex = new SETex();
        tex.texX = x / texMap.width; tex.texY = y / texMap.height;
        tex.texW = w /texMap.width; tex.texH = h / texMap.height;
        return tex;
    }
    
    public static SETex SEloadTexture(Data tex) {
        if (tex.width > texMap.width || tex.height > texMap.height) { SEEngine.log("Texture Does Not Fit in Given Memory"); return null; }
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
        if (!found) { SEEngine.log("Out Of Texture Memory"); return null; }
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
    
    //Yikes! This might be unsafe, or there might be some kind of stack leak!
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
    
    public static void SEdeleteTexture(SETex texture) {
        if (texture == null) { SEEngine.log("Null Textures cannot be Deleted"); return; }
        for (int x = 0; x < texture.texW * texMap.width; x++) {
            for (int y = 0; y < texture.texH * texMap.height; y++) {
                texSpace[(int)(x + texture.texX * texMap.width) + (int)(y + texture.texY * texMap.height) * texMap.width] = false;
            }
        }
    }
}
