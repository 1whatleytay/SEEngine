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

import static org.lwjgl.opengl.GL11.*;
import static engine.SERLogic.Data;

import static engine.SEConstants.*;

/**
 * Handles loading, storing and accessing textures.
 * @author desgroup
 * @version SEAlpha3a
 */
public class SETextures {
    private SETextures() {}
    
    private static int width = 0, height = 0;
    private static boolean[] texSpace = null;
    private static int mainTex = -1;
    
    /**
     * The max texture size of the current system.
     * Initialized during {@link engine.SEEngine#init(SEControlledProgram)}.
     */
    protected static int gpuMaxTextureSize = 0;
    
    /**
     * Gets the main texture width.
     * @return The main texture width.
     */
    protected static int getTexDimWidth() { return width; }

    /**
     * Gets the main texture height.
     * @return The main texture height.
     */
    protected static int getTexDimHeight() { return height; }

    /**
     * A placeholder texture.
     */
    public static final SETex BLANK_TEXTURE = new SETex();

    /**
     * Loads data in tex to a texture and returns that texture object.
     * The format in Data should be a repeating pattern of {@link engine.SERImages#components} floats, all representing their corresponding component from 0 to 1.
     * There should be width * height of these patterns, each pattern representing one pixel, reading from top left to the right and then downwards.
     * @param tex The data to be loaded.
     * @return A texture who's contents is tex.
     */
    public static SETex SEloadTexture(Data tex) {
        if (tex == null) { SEEngine.log(MSG_TYPE_FAIL, MSG_NULL_TEXTURE); return null; }
        if (tex.width > gpuMaxTextureSize || tex.height > gpuMaxTextureSize) { SEEngine.log(MSG_TYPE_OPENGL, MSG_INCOMPATIBLE_CONTEXT); return null; } //This is odd logic.
        if (tex.width > width || tex.height > height) { SEEngine.log(MSG_TYPE_OPT, MSG_OUT_OF_TEXTURE_MEMORY); return null; }
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
        if (!found) { SEEngine.log(MSG_TYPE_OPT, MSG_OUT_OF_TEXTURE_MEMORY); return null; }
        for (int x = 0; x < tex.width; x++)
            for (int y = 0; y < tex.height; y++)
                texSpace[(x + findX) + (y + findY) * width] = true;
        glBindTexture(GL_TEXTURE_2D, mainTex);
        glTexSubImage2D(GL_TEXTURE_2D, 0, findX, findY, tex.width, tex.height, SERImages.COMPONENT_REFERENCE[SERImages.components], GL_FLOAT, tex.data);
        SETex texture = new SETex();
        texture.texX = (float)findX / (float)width; texture.texY = (float)findY / (float)height;
        texture.texW = (float)tex.width / (float)width;  texture.texH = (float)tex.height / (float)height;
        return texture;
    }

    /**
     * String version of {@link engine.SETextures#SEloadTexture(engine.SERLogic.Data)}.
     * @param path The path to an image file which contains the information to load into a texture.
     * @return The texture that contains the information inside the image file provided by path.
     */
    public static SETex SEloadTexture(String path) { return SEloadTexture(SERImages.SEgetImageData(path)); }

    /**
     * Samples a section of a larger texture.
     * Texture boundaries are ignored.
     * @param mainTex The texture to sample.
     * @param offX The x coordinate in pixels of the main texture to sample.
     * @param offY The y coordinate in pixels of the main texture to sample.
     * @param w The width of the sampled texture.
     * @param h The height of the sampled texture.
     * @return A sampled texture from mainTex.
     */
    public static SETex SEsampleTexture(SETex mainTex, int offX, int offY, int w, int h) {
        SETex tex = new SETex();
        tex.texX = (mainTex.texX * width + offX) / width;
        tex.texY = (mainTex.texY * height + offY) / height;
        tex.texW = (double)w / (double)width;
        tex.texH = (double)h / (double)height;
        return tex;
    }

    /**
     * Overwrites the texture texture with the data tex.
     * @param texture The texture to overwrite.
     * @param tex The data to overwrite the texture with.
     * @deprecated
     */
    @Deprecated public static void SEoverrideTexture(SETex texture, Data tex) {
        int findX = (int)(texture.texX * width), findY = (int)(texture.texY * height);
        for (int x = 0; x < tex.width; x++)
            for (int y = 0; y < tex.height; y++)
                texSpace[(x + findX) + (y + findY) * width] = true;
        glBindTexture(GL_TEXTURE_2D, mainTex);
        glTexSubImage2D(GL_TEXTURE_2D, 0, findX, findY, tex.width, tex.height, SERImages.COMPONENT_REFERENCE[SERImages.components], GL_FLOAT, tex.data);
    }

    /**
     * Deletes and frees up the space allocated by texture.
     * @param texture The texture to delete.
     */
    public static void SEdeleteTexture(SETex texture) {
        if (texture == null) { SEEngine.log(MSG_TYPE_OPT, MSG_NULL_TEXTURE); return; }
        for (int x = 0; x < texture.texW * width; x++) {
            for (int y = 0; y < texture.texH * height; y++) {
                texSpace[(int)(x + texture.texX * width) + (int)(y + texture.texY * height) * width] = false;
            }
        }
    }

    /**
     * Loads a solid color into a texture.
     * @param red The red component of the texture. From 0 (no red) to 1 (full red).
     * @param green The green component of the texture. From 0 (no green) to 1 (full green).
     * @param blue The blue component of the texture. From 0 (no blue) to 1 (full blue).
     * @param alpha The alpha component of the texture. From 0 (fully transparent) to 1 (fully opaque).
     * @return A texture containing a solid color.
     */
    public static SETex SEloadColor(float red, float green, float blue, float alpha) {
        return SEloadTexture(new Data(new float[]{red, green, blue, alpha}, 1, 1));
    }

    /**
     * 3 component version of {@link engine.SETextures#SEloadColor(float, float, float, float)}.
     * @param red The red component of the texture. From 0 (no red) to 1 (full red).
     * @param green The green component of the texture. From 0 (no green) to 1 (full green).
     * @param blue The blue component of the texture. From 0 (no blue) to 1 (full blue).
     * @return A texture containing a solid color.
     */
    public static SETex SEloadColor(float red, float green, float blue) { return SEloadColor(red, green, blue, 1); }

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
     * Sets up the current texture space.
     * @param texWidth The main texture width.
     * @param texHeight The main texture height.
     */
    protected static void loadTextures(int texWidth, int texHeight) {
        width = texWidth; height = texHeight;
        texSpace = new boolean[texWidth * texHeight];
        Data texMap = new Data(new float[texWidth * SERImages.components * texHeight], texWidth, texHeight);
        mainTex = glGenTextures();
        SERImages.loadTexture(texMap, mainTex);
        BLANK_TEXTURE.texX = 0; BLANK_TEXTURE.texY = 0;
        BLANK_TEXTURE.texW = 0; BLANK_TEXTURE.texH = 0;
    }
}
