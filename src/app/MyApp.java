package app;

import engine.*;
import static engine.Engine.*;

import static org.lwjgl.glfw.GLFW.*;
import java.util.*;
import static engine.Textures.*;
import static engine.Objects.*;

public class MyApp implements SEProgram {
    public static void main(String[] args) {
        SEstart(new MyApp());
    }
    
    String asset(String ass) { return MyApp.class.getResource("/assets/" + ass).getPath(); }

    @Override public SEProgramData program() {
        SEProgramData data = new SEProgramData();
        data.windowWidth = 600;
        data.windowHeight = 600;
        data.programName = "MyApp";
        data.maxObjects = 60;
        data.texMemoryWidth = 1024;
        data.texMemoryHeight = 1024;
        data.compatibleVersions = "after:SEEarly3";
        data.keyFunc = (int key, int action)->{key(key,action);};
        return data;
    }
    
    @Override public void setup() {
        SEdirection(SE_TOP_TO_BOTTOM);
        SEcreateObject(40, 40, 40, 40, Textures.SEloadTexture(asset("Maps/Map2.png")));
        SEcreateObject(80, 80, 40, 40, Textures.SEloadTexture(asset("Sprites/char0.png")));
    }
    
    @Override public void update() {
    }
    
    boolean myThing = false;
    
    public void key(int key, int action) {
    }
}
