package engine;

import java.util.*;

import static engine.SEConstants.*;

/**
 * Represents a region where mouse input is being listened to.
 */
public class SEButton {

    /**
     * A list of every button that exists.
     */
    protected static ArrayList<SEButton> buttons = new ArrayList<>();

    /**
     * The x position (relative to the top of the window, in pixels) where the region exists.
     */
    public int x;
    /**
     * The y position (relative to the top of the window, in pixels) where the region exists.
     */
    public int y;
    /**
     * The width (in pixels) of the region.
     */
    public int w;
    /**
     * The height (in pixels) of the region.
     */
    public int h;

    /**
     * The function to be called when an event (one of the MOUSE_ constants) have occurred.
     */
    public SEButtonFunc func;

    protected boolean mouseOver = false;

    /**
     * Position constructor.
     * @param x The x position (relative to the top of the window, in pixels) where the area starts.
     * @param y The x position (relative to the top of the window, in pixels) where the area starts.
     * @param w The width of the area.
     * @param h The height of the area.
     * @param func The message function of the area. Will be called when one of the MOUSE_ constants occur.
     */
    public SEButton(int x, int y, int w, int h, SEButtonFunc func) {
        x = x; y = y; w = w; h = h; func = func;
        buttons.add(this);
    }

    /**
     * {@link engine.SEObj} constructor.
     * @param obj The object to have it's properties carried to the object. A change to the object will not update the bundle.
     * @param func The message function of the area. Will be called when one of the MOUSE_ constant occur.
     */
    public SEButton(SEObj obj, SEButtonFunc func) { this(obj.x, obj.y, obj.w, obj.h, func); }
}
