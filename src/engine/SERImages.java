package engine;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import static engine.SERLogic.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL21.*;

public class SERImages {
    
    static int stexture_border = GL_REPEAT;
    static int stexture_filter = GL_NEAREST;
    static void setupTexture() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, stexture_border);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, stexture_border);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, stexture_filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, stexture_filter);
    }
    public static Data getAsGLTexture(String path) {
        path = path.replace("%20", " ");
        BufferedImage img;
        float[] glTexture = null;
        int width = 0, height = 0;
        File apath = new File(path);
        if (apath.exists()) {
            try {
                img = ImageIO.read(apath);
                width = img.getWidth(); height = img.getHeight();
                glTexture = new float[width * height * 3];
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        int col = img.getRGB(x, y);
                        glTexture[(x + y * width) * 3] = (float)((col & 0xff0000) >> 16)/255.0f;
                        glTexture[(x + y * width) * 3 + 1] = (float)((col & 0xff00) >> 8)/255.0f;
                        glTexture[(x + y * width) * 3 + 2] = (float)(col & 0xff)/255.0f;
                    }
                }
            } catch (Exception ex) {}
        } else { System.out.println("Texture " + path + " does not exist!"); return null; }
        if (glTexture == null) { System.err.println("Could not load texture! Path: " + path); return null; }
        return new Data(glTexture, width, height);
    }
    public static void loadTexture(Data data, int texture) {
        if (data == null) { System.out.println("Failed to load texture."); return; }
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, data.width, data.height, 0, GL_RGB, GL_FLOAT, data.data);
        setupTexture();
    }
    public static void loadTexture(String texturePath, int texture) { loadTexture(getAsGLTexture(texturePath), texture); }
}
