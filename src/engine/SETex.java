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

import engine.SERLogic.*;

import static engine.SEConstants.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Stores information about a texture in memory.
 * @author desgroup
 * @version SEAlpha4a
 */
public class SETex {

    /**
     * Hidden basic constructor.
     */
    private SETex() {}

    private static int width, height;
    private static int mainTex;
    private static boolean[] texSpace;

    private static int gpuMaxTextureSize;

    /**
     * The x position where this texture starts in the main texture.
     */
    protected double texX = 0;
    /**
     * The y position where this texture starts in the main texture.
     */
    protected double texY = 0;

    /**
     * The width of the texture.
     */
    protected double texW = 0;
    /**
     * The height of the texture.
     */
    protected double texH = 0;

    public static final SETex BLANK_TEXTURE = new SETex();

    @Override public boolean equals(Object a) {
        SETex ar = (SETex)a;
        return texX == ar.texX && texY == ar.texY && texW == ar.texW && texH == ar.texH;
    }

    private static float[] formatColors(float red, float green, float blue, float alpha) {
        switch (SERImages.components) {
            case 3: return new float[] {red, green, blue};
            case 1: return new float[] {red};
            default: return new float[] {red, green, blue, alpha};
        }
    }

    /**
     * {@link engine.SERLogic.Data} constructor.
     * @param tex The data to be loaded into the texture.
     *            The format should be a repeating pattern of {@link engine.SERImages#components} floats, all representing their corresponding component from 0 to 1.
     *            There should be width * height of these patterns, each pattern representing one pixel, reading from top left to the right and then downwards.
     */
    public SETex(Data tex) {
        if (tex == null) { SEEngine.log(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_NULL_TEXTURE); return; }
        if (tex.width > gpuMaxTextureSize || tex.height > gpuMaxTextureSize) { SEEngine.log(SEMessageType.MSG_TYPE_OPENGL, SEMessage.MSG_INCOMPATIBLE_CONTEXT); return; } //This is odd logic.
        if (tex.width > width || tex.height > height) { SEEngine.log(SEMessageType.MSG_TYPE_OPT, SEMessage.MSG_OUT_OF_TEXTURE_MEMORY); return; }
        boolean found = false;
        int findX = 0, findY = 0;
        for (int scanX = 0; scanX < width - tex.width + 1; scanX++) {
            for (int scanY = 0; scanY < height - tex.height + 1; scanY++) {
                boolean isFound = false;
                for (int scanPX = 0; scanPX < tex.width; scanPX++) {
                    for (int scanPY = 0; scanPY < tex.height; scanPY++) {
                        if (texSpace[(scanX + scanPX) + (scanY + scanPY) * width]) { isFound = true; break; }
                    }
                    if (isFound) break;
                }
                if (!isFound) { found = true; findX = scanX; findY = scanY; break; }
            }
            if (found) break;
        }
        if (!found) { SEEngine.log(SEMessageType.MSG_TYPE_OPT, SEMessage.MSG_OUT_OF_TEXTURE_MEMORY); return; }
        for (int x = 0; x < tex.width; x++)
            for (int y = 0; y < tex.height; y++)
                texSpace[(x + findX) + (y + findY) * width] = true;
        glBindTexture(GL_TEXTURE_2D, mainTex);
        glTexSubImage2D(GL_TEXTURE_2D, 0, findX, findY, tex.width, tex.height, SERImages.COMPONENT_REFERENCE[SERImages.components], GL_FLOAT, tex.data);
        texX = (float)findX / (float)width; texY = (float)findY / (float)height;
        texW = (float)tex.width / (float)width; texH = (float)tex.height / (float)height;
    }

    /**
     * Image file constructor.
     * @param path The path to an image file which contains the texture data.
     */
    public SETex(String path) { this(SERImages.SEgetImageData(path)); }

    /**
     * Solid colour constructor.
     * @param red The red component of the texture. From 0 (no red) to 1 (completely red).
     * @param green The green component of the texture. From 0 (no green) to 1 (completely green).
     * @param blue The blue component of the texture. From 0 (no blue) to 1 (completely blue).
     * @param alpha The alpha component of the texture. From 0 (fully transparent) to 1 (fully opaque).
     */
    public SETex(float red, float green, float blue, float alpha) {
        this(new Data(formatColors(red, green, blue, alpha), 1, 1));
    }

    /**
     * 3 component solid colour constructor.
     * @param red The red component of the texture. From 0 (no red) to 1 (completely red).
     * @param green The green component of the texture. From 0 (no green) to 1 (completely green).
     * @param blue The blue component of the texture. From 0 (no blue) to 1 (completely blue).
     */
    public SETex(float red, float green, float blue) { this(red, green, blue, 1); }

    /**
     * Samples a section of a larger texture.
     * Texture boundaries are ignored.
     * @param offX The x coordinate in pixels of the main texture to sample.
     * @param offY The y coordinate in pixels of the main texture to sample.
     * @param w The width of the sampled texture.
     * @param h The height of the sampled texture.
     * @return A sampled texture from mainTex.
     */
    public SETex sample(int offX, int offY, int w, int h) {
        SETex tex = new SETex();
        tex.texX = (texX * width + offX) / width;
        tex.texY = (texY * height + offY) / height;
        tex.texW = (double)w / (double)width;
        tex.texH = (double)h / (double)height;
        return tex;
    }

    /**
     * Deletes and frees up the space allocated by the texture.
     */
    public void delete() {
        for (int x = 0; x < texW * width; x++) {
            for (int y = 0; y < texH * height; y++) {
                texSpace[(int)(x + texX * width) + (int)(y + texY * height) * width] = false;
            }
        }
    }

    /**
     * Fully clears all texture data.
     * @param texWidth The new main texture width.
     * @param texHeight The new main texture height.
     */
    protected static void clearTextures(int texWidth, int texHeight) {
        width = texWidth; height = texHeight;
        texSpace = new boolean[texWidth * texHeight];
        Data texMap = new Data(new float[texWidth * SERImages.components * texHeight], texWidth, texHeight);
        glBindTexture(GL_TEXTURE_2D, 0);
        glDeleteTextures(mainTex);
        mainTex = glGenTextures();
        SERImages.loadTexture(texMap, mainTex);
    }

    /**
     * Invalidates all texture data.
     * Unable to change main texture dimensions.
     */
    protected static void quickClearTextures() {
        texSpace = new boolean[texSpace.length];
    }

    /**
     * Initializes vital texture memory and procedures.
     * @param texWidth The width of the texture to allocate.
     * @param texHeight The height of the texture to allocate.
     */
    protected static void init(int texWidth, int texHeight) {
        width = texWidth; height = texHeight;
        texSpace = new boolean[texWidth * texHeight];
        Data texMap = new Data(new float[texWidth * SERImages.components * texHeight], texWidth, texHeight);
        mainTex = glGenTextures();
        SERImages.loadTexture(texMap, mainTex);
        gpuMaxTextureSize = glGetInteger(GL_MAX_TEXTURE_SIZE);
    }
}