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
        data.textureComponents = SEProgramData.FOURTH_COMPONENT_AS_DISCARD;
        data.compatibleVersions = "all";
        data.keyFunc = keyFunc;
        return data;
    }
    
    SEObj pBase;
    SEObj pArm;
    SEWrappedObj player;
    
    @Override public void setup() {
        SEuseWrappedObjects = true;
        SEcollapseObjectDrawSpaceOnDeletion = true;
        SEdirection(SE_TOP_TO_BOTTOM);
        LoadData.loadBasicData();
        pBase = SEcreateObject(0, 0, 200, 200, LoadData.player_body[0]);
        pArm = SEduplicateObject(pBase);
        SEobjTex(pBase, LoadData.player_arm[0]);
        player = SEwrapObjects(new SEObj[]{pBase, pArm});
        SEswapOffsets("player_offset");
        SEmoveOffset(100, 100);
        SEbindOffset(player);
    }
    
    @Override public void update() {
    }
    
    boolean myThing = false;
    
    SEProgramData.SEKeyFunc keyFunc = (int key, int action) -> {
        
    };
}
