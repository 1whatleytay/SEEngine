package engine;

public class SEProgramData {
    public int windowWidth = 600, windowHeight = 600;
    public String programName = "Program";
    public int maxObjects = 20;
    public int texMemoryWidth = 256, texMemoryHeight = 256;
    public String compatibleVersions = "all";

    public interface SEKeyFunc { void key(int key, int action); }
    public interface SEMouseFunc { void mouse(int x, int y, int button, int action); }
    public SEKeyFunc keyFunc = (int key, int action)->{};
    public SEMouseFunc mouseFunc = (int x, int y, int button, int action)->{};
}