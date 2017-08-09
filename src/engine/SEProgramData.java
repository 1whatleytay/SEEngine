package engine;

//The data structure SEEngine needs to start your program. You don't need to
//change anything to get started, it's already filled out for you. But please do
//customize it to your program, or else you might find some issues you might not
//have forseen.
public class SEProgramData {
    //Set textureCompoennts to FOURTH_COMPONENT_AS_DISCARD if you want an
    //On or Off transparency shader.. Graded opacity is not a factor with
    //this set.
    public static final byte FOURTH_COMPONENT_AS_DISCARD = 0x10;
    //Set compatibleVersions to either of ALL_VERSIONS or CURRENT_VERSION
    //to have your program support the current SEVersion. Although it might
    //be a good idea to look into the version you are using.
    public static final String ALL_VERSIONS = "all";
    public static final String CURRENT_VERSION = SEEngine.SEversion();
    
    //Set windowWidth and windowHeight to the suggested program window width
    //and height.
    public int windowWidth = 600, windowHeight = 600;
    //A program name. Will be the title of the program window.
    public String programName = "Program";
    //Maximum number of SEObjects allowed to be allocated at once.
    //Delete older objects with SEdeleteObject to keep this number safe.
    //A higher number will require more memory.
    public int maxObjects = 20;
    //Maximum texture width and height. The dimensions of the larger texture
    //used to store all the smaller textures. Make sure at least
    //texMemoryWidth * texMemeoryHeight is greater than the amount of pixels
    //you wish to allocate. Even then, you might run out of width or height.
    public int texMemoryWidth = 256, texMemoryHeight = 256;
    //A compatible versions tag. The value of "all" allows all versions (even
    //the broken ones!) while the name of the versions you wish to allow can be
    //seperated by commas. You can also add a "before:" or "after:" tag before
    //the verison number to suggest versions before or after the version after
    //the : which will be respected. To be improved. Multiple before-after
    //ranges are not supported. In that case, just list a bunch of versions.
    public String compatibleVersions = "all";
    //Amount of components textures should have. A value of 1 is grayscale,
    //depending on the loaded texture's red component. A value of 3 is true
    //color. A value of 4 is true color + alpha. See FOURTH_COMPONENT_AS_DISCARD
    //for more modes. Values of less than 0 or 2 are not supported. Values above
    //4 default to FOURTH_COMPONENT_AS_DISCARD.
    public byte textureComponents = 3;
    //Determines if the application window will go fullscreen. If true, the
    //application will take up the enitre monitor. Note: Not respected when
    //switching programs, no matter the inherit.
    public boolean isFullScreen = false;
    
    public static final byte
            //Inherits nothing from the previous program.
            INHERIT_NONE = 0x50,
            //Inherits values only if the values are higher than the ones
            //occupying this SEProgramData (specifically, maxObjects,
            //texMemoryWidth, texMemoryHeight, textureComponents, windowWidth,
            //windowHeight and isFullScreen).
            INHERIT_MINIMUM = 0x51,
            //Inherits everything (specifically, maxObjects, texMemoryWidth,
            //texMemoryHeight, textureComponents, windowWidth, windowHeight and
            //isFullScreen). When combined with useQuickClear, program switches
            //can go slightly faster.
            INHERIT_MOST = 0x52;
    
    //Set to either INHERIT_NONE, INHERIT_MINIMUM or INHERIT_MOST. Other values
    //will default to either INHERIT_NONE (<0x50) or INHERIT_MOST (>0x52).
    //Controls if or what will be inherited from the last program to run.
    //This value is only used if the program is loaded with
    //SEswitchPrograms(SEProgram, boolean). See variable definitions for what
    //each value does.
    public byte inheritData = INHERIT_NONE;
    //When true, object and texture memory will be invalidated instead of
    //cleared when availible. Use with some inherit settings to get more
    //more reliable results (if any).
    public boolean useQuickClear = false;
    
    //The background color of the window. Components from scale 0-1 in order:
    //red, green, blue alpha. 
    public float[] bkgColor = {0, 0, 0, 1};
    
    //Message Types
    public static final byte
            //A debug message. Often given out when SEcoreDebug is true.
            MSG_DEBUG = 0x40,
            //An informatic message. Something like a version number or a
            //point in the code has been reached.
            MSG_INFO = 0x41,
            //Your code could be optimised. Some redundant calls or excessive
            //tasking could trigger one of these.
            MSG_OPT = 0x42,
            //Your code could be optimised. However, how your code may lose
            //functionality because of issue.
            MSG_OPT_FUNC = 0x43,
            //A failure has occured. There isn't enough memory allocated by
            //maxObjects or texMemoryWidth/Height or something has gone wrong
            //internally. SEEngine will try to continue functioning, however the
            //task will not be complete.
            MSG_FAIL = 0x44,
            //A fatal failure has occured. Rather the computer running the code
            //is too outdated or we're lacking in permissions. Anyway, we're
            //shutting down your application.
            MSG_FAIL_FATAL = 0x45,
            //Something went wrong with OpenGL, or maybe OpenGL wants to tell
            //you something. Anyway, enable SEcatchOpenGLErrors if you want
            //to receive some of these.
            MSG_OPENGL = 0x46,
            //Something else unlisted happened. Check the discription for more
            //info.
            MSG_OTHER = 0x47,
            //Some external program decided to call SEEngine.log, so they used
            //this message to contact the running program. Check the description
            //for more info.
            MSG_EXTERNAL = 0x48;
    
    //A simple description functions to the message types above. Just returns
    //the names of the variables.
    public static String SEmsgDesc(byte msgType) {
        String desc = "MSG_NO_DESC";
        switch (msgType) {
            case MSG_DEBUG: desc = "MSG_DEBUG"; break;
            case MSG_INFO: desc = "MSG_INFO"; break;
            case MSG_OPT: desc = "MSG_OPT"; break;
            case MSG_OPT_FUNC: desc = "MSG_OPT_FUNC"; break;
            case MSG_FAIL: desc = "MSG_FAIL"; break;
            case MSG_FAIL_FATAL: desc = "MSG_FAIL_FATAL"; break;
            case MSG_OPENGL: desc = "MSG_OPENGL"; break;
            case MSG_OTHER: desc = "MSG_OTHER"; break;
            case MSG_EXTERNAL: desc = "MSG_EXTERNAL"; break;
        }
        return desc;
    }
    
    public interface SEKeyFunc { void key(int key, int action); }
    public interface SEMouseFunc { void mouse(int x, int y, int button, int action); }
    public interface SEMessageFunc { void msg(byte type, String desc); }
    //Your program's key function. The function should have two ints as params.
    //The key param is one of org.lwjgl.GLFW.GLFW's GLFW_KEYs. For example,
    //the w key is GLFW_KEY_W. Action is one of org.lwjgl.GLFW.GLFW's GLFW_PRESS
    //or GLFW_RELEASE, corresponding to a press or release of the key key.
    public SEKeyFunc keyFunc = (int key, int action)->{};
    //Your program's mouse function. Currently unsupported.
    public SEMouseFunc mouseFunc = (int x, int y, int button, int action)->{};
    //Your program's message function. The function should have a byte and a
    //String as params. type is one of MSG_DEBUG, MSG_INFO, MSG_OPT,
    //MSG_OPT_FUNC, MSG_FAIL, MSG_FAIL_FATAL or MSG_OPENGL. Read more at the
    //declairations of the variables above. desc is a description of the message
    //or what went wrong.
    public SEMessageFunc messageFunc = (byte type, String desc)->{};
}