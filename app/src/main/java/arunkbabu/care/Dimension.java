package arunkbabu.care;

/**
 * A DataType used to store Two-Dimensional values of type integer
 */
public class Dimension {
    private int mHeight;
    private int mWidth;

    public Dimension(int height, int width){
        mHeight = height;
        mWidth = width;
    }

    /**
     * The height value
     * @return Height
     */
    public int getHeightDimension(){
        return mHeight;
    }

    /**
     * The width value
     * @return Width
     */
    public int getWidthDimension(){
        return mWidth;
    }

}
