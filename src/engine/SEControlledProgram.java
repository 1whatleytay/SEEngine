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
 * The main program interface.
 * To start {@link engine.SEEngine} with {@link engine.SEEngine#SEstart(SEControlledProgram)} you need to pass one of these programs with all the methods implemented.
 * @author desgroup
 * @version SEAlpha2a
 */
public interface SEControlledProgram {

    /**
     * Returns data on the program object.
     * Called when a program is being loaded or swapped.
     * Does not guarantee all engine functions to be available.
     * @return An {@link engine.SEProgramData} object with the desired variables changed and filled out.
     */
    SEProgramData program();

    /**
     * Called when a program has finished being loaded.
     * Most engine functions should be available.
     */
    void setup();
    /**
     * Called every frame after setup until the program closes.
     * If {@link engine.SEEngine#SEdrawOnCommand} is disabled, the screen is also redrawn after the call.
     */
    void update();
}
