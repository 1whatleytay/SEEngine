package engine;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import static engine.SERLogic.*;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

//Handles loading images among other things.
public class SERImages {
    private SERImages() {}
    
    //Sets some texture parameters so the texture will function properly.
    public static int stexture_border = GL_REPEAT;
    public static int stexture_filter = GL_NEAREST;
    public static void setupTexture() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, stexture_border);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, stexture_border);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, stexture_filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, stexture_filter);
    }
    
    //Some component reference. To know how many components match up with which
    //OpenGL constants.
    public static byte components = 4;
    public static final int[] COMPONENT_REFERENCE = {
        GL_NONE,
        GL_R,
        GL_NONE,
        GL_RGB,
        GL_RGBA,
    };
    
    //Loads a texture found in an image file located at path into a Data
    //structure.
    public static Data SEgetImageData(String path) {
        path = path.replace("%20", " ");
        BufferedImage img;
        float[] glTexture = null;
        int width = 0, height = 0;
        File apath = new File(path);
        if (apath.exists() && apath.isFile()) {
            try {
                img = ImageIO.read(apath);
                width = img.getWidth(); height = img.getHeight();
                glTexture = new float[width * height * components];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int col = img.getRGB(x, y);
                        switch (components) {
                            case 4:
                                glTexture[(x + y * width) * components + 3] = (float)((col & 0xff000000) >>> 24)/255.0f;
                            case 3:
                                glTexture[(x + y * width) * components + 2] = (float)(col & 0xff)/255.0f;
                                glTexture[(x + y * width) * components + 1] = (float)((col & 0xff00) >> 8)/255.0f;
                            case 1:
                                glTexture[(x + y * width) * components] = (float)((col & 0xff0000) >> 16)/255.0f;
                            default: break;
                        }
                        
                        
                        
                    }
                }
            } catch (Exception ex) {}
        } else { System.out.println("Texture " + path + " does not exist!"); return null; }
        if (glTexture == null) { System.err.println("Could not load texture! Path: " + path); return null; }
        return new Data(glTexture, width, height);
    }
    
    //Quickly loads the data data into texture texture.
    public static void loadTexture(Data data, int texture) {
        if (data == null) { System.out.println("Failed to load texture."); return; }
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, COMPONENT_REFERENCE[components], data.width, data.height, 0, COMPONENT_REFERENCE[components], GL_FLOAT, data.data);
        setupTexture();
    }
    //String or image file alternative to loadTexture(Data, int).
    public static void loadTexture(String texturePath, int texture) { loadTexture(SEgetImageData(texturePath), texture); }
}
