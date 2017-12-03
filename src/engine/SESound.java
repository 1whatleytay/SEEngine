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

import org.lwjgl.openal.ALC.*;
import org.lwjgl.openal.AL.*;
import org.lwjgl.openal.AL10;

import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.ALC11.*;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;
import org.lwjgl.openal.ALC10;

public class SESound {
    private static long device;
    private static int mainBuffer;
    private static boolean experimentalSoundWarning = false;
    public static void loadSounds() {
        if (!experimentalSoundWarning) {
            SEEngine.log(SEMessageType.MSG_TYPE_INFO, SEMessage.MSG_EXPERIMENTAL_SOUND_WARNING);
            experimentalSoundWarning = false;
            return;
        }
        device = alcOpenDevice((CharSequence)null);
        int error;
        error = alGetError();
        if (error != AL_NO_ERROR) SEEngine.logWithDescription(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_OPENAL_FEEDBACK_ERROR, "OpenAL failed to create device! Error: " + error);
        mainBuffer = alGenBuffers();
        error = alGetError();
        if (error != AL_NO_ERROR) SEEngine.logWithDescription(SEMessageType.MSG_TYPE_FAIL, SEMessage.MSG_OPENAL_FEEDBACK_ERROR, "OpenAL failed to create main buffer! Error: " + error);
    }
}
