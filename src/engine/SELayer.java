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
 * A blank layer implementing the {@link engine.SEControlledLayer} interface.
 * @author desgroup
 * @version SEAlpha3a
 */
public class SELayer implements SEControlledLayer {

    /**
     * A predefined {@link engine.SELayerData} object to be returned to the engine.
     */
    public SELayerData layerData = new SELayerData();

    /**
     * Returns the {@link engine.SELayer#layerData} object to the engine.
     * @return The {@link engine.SELayer#layerData} object.
     */
    @Override public SELayerData layer() { return layerData; }

    /**
     * Default function for a fallback if the user wishes to not implement the {@link engine.SEControlledLayer#pre()} function.
     * Meant to be overridden.
     */
    @Override public void pre() { }

    /**
     * Default function for a fallback if the user wishes to not implement the {@link engine.SEControlledLayer#post()} function.
     * Meant to be overridden.
     */
    @Override public void post() { }
    
}
