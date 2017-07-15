package app;

import engine.*;
import java.util.*;

public class Items {
    public static class ItemData {
        SEObj obj;
        SETex[] anim_textures;
    }
    private static ItemData[] itemData = new ItemData[SEObjects.SEmaxObjectCount()];
}
