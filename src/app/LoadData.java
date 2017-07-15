package app;

import engine.*;

public class LoadData {
    
    static SETex[] player_body;
    static SETex[] player_arm;
    
    static String asset(String ass) { return MyApp.class.getResource("/assets/" + ass).getPath(); }
    
    static SETex[] loadBach(String name, String ext, int count) {
        SETex[] textures = new SETex[count];
        for (int a = 0; a < count; a++) textures[a] = SETextures.SEloadTexture(asset(name+a+"."+ext));
        return textures;
    }
    
    static void loadBasicData() {
        player_body = loadBach("Sprites/char", "png", 2);
        player_arm = loadBach("Sprites/arm", "gif", 2);
    }
}
