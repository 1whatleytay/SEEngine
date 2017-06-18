package engine;

public class Logic {
    public static class Data {
        public float[] data; public int width, height;
        public Data(float[] dat, int w, int h) { data = dat; width = w; height = h; }
        public Data(){}
    }
    public static Data multiplyMatrices(Data mata, Data matb) {
        Data mat = new Data();
        if (mata.width != matb.height) { System.out.println("Incompatible Matrices!"); return null; }
        mat.width = matb.width; mat.height = mata.height;
        mat.data = new float[mat.width * mat.height];
        for (int x = 0; x < mat.width; x++) {
            for (int y = 0; y < mat.height; y++) {
                float[] rowa = new float[mata.width];
                float[] colb = new float[matb.height];
                for (int a = 0; a < mata.width; a++) rowa[a] = mata.data[a + y * mata.width];
                for (int a = 0; a < matb.height; a++) colb[a] = matb.data[x + a * matb.width];
                float finalValue = 0;
                for (int a = 0; a < rowa.length; a++) finalValue += rowa[a] * colb[a];
                mat.data[x + y * mat.width] = finalValue;
            }
        }
        return mat;
    }
    public static Data genScaleMatrix(float x, float y) { return new Data(new float[]{x,0,0,y}, 2, 2); }
    public static Data genRotationMatrix(float a) { return new Data(new float[]{(float)Math.cos(a), (float)-Math.sin(a),(float)Math.sin(a),(float)Math.cos(a)}, 2, 2); }
    public static Data genIdentityMatrix() { return new Data(new float[]{1,0,0,1}, 2, 2); }
}
