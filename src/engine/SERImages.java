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

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import static engine.SERLogic.*;

import static org.lwjgl.opengl.GL11.*;

import static engine.SEConstants.*;

/**
 * Handles loading images among other image related things.
 * @author desgroup
 * @version SEAlpha4a
 */
public class SERImages {
    private SERImages() {}

    /**
     * OpenGL border texture parameter.
     */
    public static int stexture_border = GL_REPEAT;

    /**
     * OpenGL filter texture parameter.
     */
    public static int stexture_filter = GL_NEAREST;

    /**
     * Sets up a texture using the specified parameters found in {@link engine.SERImages#stexture_border} and {@link engine.SERImages#stexture_border}.
     */
    public static void setupTexture() {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, stexture_border);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, stexture_border);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, stexture_filter);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, stexture_filter);
    }

    /**
     * Component mode for local functions.
     */
    protected static byte components = 4;

    /**
     * Array of OpenGL component modes upon different values of {@link engine.SERImages#components}.
     */
    protected static final int[] COMPONENT_REFERENCE = {
        GL_NONE,
        GL_R,
        GL_NONE,
        GL_RGB,
        GL_RGBA,
    };

    /**
     * Gets image data from the file specified by path and returns a {@link engine.SERLogic.Data} object containing it.
     * @param path The path to the image file containing the requested data.
     * @return The gathered data as a {@link engine.SERLogic.Data} structure.
     */
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
        } else { SEEngine.logWithDescription(SEMessageType.MSG_TYPE_OPT_FUNC, SEMessage.MSG_MISSING_TEXTURE, "Texture " + path + " does not exist!"); return null; }
        if (glTexture == null) { SEEngine.log(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_TEXTURE_LOAD_ERROR); return null; }
        return new Data(glTexture, width, height);
    }

    /**
     * Loads data into the OpenGL texture.
     * @param data The data to load into texture.
     * @param texture The OpenGL texture to add data to.
     */
    public static void loadTexture(Data data, int texture) {
        if (data == null) { SEEngine.log(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_TEXTURE_LOAD_ERROR); return; }
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexImage2D(GL_TEXTURE_2D, 0, COMPONENT_REFERENCE[components], data.width, data.height, 0, COMPONENT_REFERENCE[components], GL_FLOAT, data.data);
        setupTexture();
    }

    /**
     * String version of {@link engine.SERImages#loadTexture(engine.SERLogic.Data, int)}.
     * @param texturePath Path to an image file containing the texture data to load into texture.
     * @param texture The OpenGL texture to add data to.
     */
    public static void loadTexture(String texturePath, int texture) { loadTexture(SEgetImageData(texturePath), texture); }
}
