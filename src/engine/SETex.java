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

/**
 * Stores information about a texture in memory.
 * To create one, use {@link engine.SETextures#SEloadTexture(engine.SERLogic.Data)}.
 * @author desgroup
 * @version SEAlpha2a
 */
public class SETex {

    /**
     * Hidden basic constructor.
     */
    protected SETex() {}
    
    protected double
            /**
             * The x position where this texture starts in the main texture.
             */
            texX = 0,
            /**
             * The y position where this texture starts in the main texture.
             */
            texY = 0;
    
    protected double
            /**
             * The width of the texture.
             */
            texW = 0,
            /**
             * The height of the texture.
             */
            texH = 0;

    /**
     * Returns the x position of the texture in pixel coordinates.
     * @return The x position of the texture in pixel coordinates.
     * @deprecated
     */
    public int getStartX() { return (int)(texX * (double)SETextures.getTexDimWidth()); }

    /**
     * Returns the y position of the texture in pixel coordinates.
     * @return The y position of the texture in pixel coordinates.
     * @deprecated
     */
    public int getStartY() { return (int)(texY * (double)SETextures.getTexDimHeight()); }

    /**
     * Returns the width of the texture in pixel coordinates.
     * @return The width of the texture in pixel coordinates.
     * @deprecated
     */
    public int getWidth() { return (int)(texW * (double)SETextures.getTexDimWidth()); }

    /**
     * Returns the height of the texture in pixel coordinates.
     * @return The height of the texture in pixel coordinates.
     * @deprecated
     */
    public int getHeight() { return (int)(texH * (double)SETextures.getTexDimHeight()); }
    
    @Override public boolean equals(Object a) {
        SETex ar = (SETex)a;
        return texX == ar.texX && texY == ar.texY && texW == ar.texW && texH == ar.texH;
    }
}