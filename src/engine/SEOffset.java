package engine;

import java.util.Arrays;
import java.util.HashMap;

import static engine.SEConstants.*;

/**
 * Stores some information about an offset.
 * Bind to a {@link engine.SEWrappedObj} with {@link engine.SEWrappedObj#bind(SEOffset)}.
 * @author desgroup
 * @version SEAlpha4a
 */
public class SEOffset {
    private static int[] offset = null;

    /**
     * Constructor.
     * @param x The starting offset in the x direction (in pixels).
     * @param y The starting offset in the y direction (in pixels).
     */
    public SEOffset(int x, int y) { offset = new int[] {x, y}; }

    /**
     * Changes this offset to xOffset, yOffset pixels.
     * @param xOffset The new x value the offset will be set to (in pixels).
     * @param yOffset The new y value the offset will be set to (in pixels).
     */
    public void put(int xOffset, int yOffset) {offset[0] = xOffset; offset[1] = yOffset; }

    /**
     * Array version of {@link engine.SEOffset#put(int, int)}..
     * @param O An array containing the new x and y values (in pixels) for the current offset through the first and second array element, respectively.
     *          Must be 2 elements long.
     */
    public void put(int[] O) { if (O.length == 2) { offset[0] = O[0]; offset[1] = O[1]; } else SEEngine.log(SEMessageType.MSG_TYPE_OPT_FUNC, SEMessage.MSG_OFFSET_TOO_LARGE); }

    /**
     * Shifts this offset by xMov, yMov pixels.
     * @param xMov The amount (in pixels) to shift the offset by in the x direction.
     * @param yMov The amount (in pixels) to shift the offset by in the y direction.
     */
    public void move(int xMov, int yMov) { offset[0] += xMov; offset[1] += yMov; }

    /**
     * Array version of {@link engine.SEOffset#move(int, int)}.
     * @param O An array containing the amount (in pixels) to shift the current offset by in the x and y directions through the first and second array element, respectively.
     *            Must be 2 elements long.
     */
    public void move(int[] O) { if (O.length == 2) { offset[0] += O[0]; offset[1] += O[1]; } else SEEngine.log(SEMessageType.MSG_TYPE_OPT_FUNC, SEMessage.MSG_OFFSET_TOO_LARGE); }

    /**
     * Returns an array containing the offset values.
     * The values in the array may be changed.
     * @return An array containing the offset values.
     */
    public int[] getOffset() { return offset; }

    /**
     * Establishes the offset offset into the shader.
     */
    protected void fix() { SEIShaders.offset(offset[0], offset[1]); }

    @Override public boolean equals(Object obj) { return Arrays.equals(((SEOffset)obj).offset, offset); }
}
