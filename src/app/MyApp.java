package app;

import engine.*;
import static engine.SEEngine.*;

import static org.lwjgl.glfw.GLFW.*;
import java.util.*;
import static engine.SETextures.*;
import static engine.SEObjects.*;

public class MyApp implements SEProgram {
    public static void main(String[] args) {
        SEstart(new MyApp());
    }

    @Override public SEProgramData program() {
        SEProgramData data = new SEProgramData();
        data.windowWidth = 600;
        data.windowHeight = 600;
        data.programName = "MyApp";
        data.maxObjects = 60;
        data.texMemoryWidth = 1024;
        data.texMemoryHeight = 1024;
        data.compatibleVersions = "after:SEEarly3";
        data.keyFunc = keyFunc;
        return data;
    }
    
    @Override public void setup() {
        SEdirection(SE_TOP_TO_BOTTOM);
        LoadData.loadBasicData();
        
    }
    
    @Override public void update() {
    }
    
    boolean myThing = false;
    
    SEProgramData.SEKeyFunc keyFunc = (int key, int action) -> {
        
    };
}
