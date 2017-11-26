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
 * A blank program implementing the {@link engine.SEControlledProgram} interface.
 * @author desgroup
 * @version SEAlpha2a
 */
public class SEProgram implements SEControlledProgram {
    
    /**
     * To be returned on startup.
     * Change during initialization to change program info. 
     */
    public SEProgramData programData = new SEProgramData();

    /**
     * Returns the program data to the engine.
     * @return Program data.
     */
    @Override public SEProgramData program() { return programData; }
    
    /**
     * Called once on startup.
     */
    @Override public void setup() { }

    /**
     * Called every frame while running.
     */
    @Override public void update() { }
    
}
