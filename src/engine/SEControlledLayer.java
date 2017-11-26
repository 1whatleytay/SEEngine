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

/**
 * Adds an extra layer of processing before or after the update function.
 * @author desgroup
 * @version SEAlpha3a
 */
public interface SEControlledLayer {
    /**
     * Returns data on the layer object.
     * Called when a layer is being added.
     * @return A {@link engine.SELayerData} object with the desired variables changed and filled out.
     */
    SELayerData layer();

    /**
     * Called right before the main program's {@link engine.SEControlledProgram#update()} function is called.
     * The layers to be added first have their pre functions called first.
     * Layer order cannot be modified.
     */
    void pre();

    /**
     * Called right after the main program's {@link engine.SEControlledProgram#update()} function is called.
     * The layers to be added first have their post functions called first.
     * Layer order cannot be modified.
     */
    void post();
}
