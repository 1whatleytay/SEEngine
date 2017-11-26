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

import static engine.SEConstants.*;

/**
 * A data structure returned from a call to {@link engine.SEControlledLayer#layer()}
 * @author desgroup
 * @version SEAlpha3a
 */
public class SELayerData {
    /**
     * A unique layer name.
     * Used for editing and accessing layers by the program.
     * It should be availible to the program.
     */
    public String layerName = "layer";

    /**
     * An {@link engine.SEConstants.SEFunctionBundle} object to have it's functions called right before the program's own {@link engine.SEConstants.SEFunctionBundle}.
     */
    public SEFunctionBundle preFuncs = new SEFunctionBundle();

    /**
     * An {@link engine.SEConstants.SEFunctionBundle} object to have it's functions called right after the program's own {@link engine.SEConstants.SEFunctionBundle}.
     */
    public SEFunctionBundle postFuncs = new SEFunctionBundle();

    /**
     * A function called once the layer has been added.
     */
    public SEInfoFunc setupFunc = () -> {};
}
