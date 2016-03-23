package com.thonners.singpong;

/**
 * Class to create the pitch on which the game of pong is to be played
 *
 * @author Thonners
 * @since 09/02/16
 * @version 1.0
 *
 */

public class PongPitch {

    // Pitch params (dictated by the screen)
    private int height ;
    private int width ;

    private int discretisation = 10 ;   // Simplify the pitch by dividing into squares of <discretisation> pixels

    // Pitch Array
    private int[][] pitch ; // Array storing the pitch. 0 defines regular pitch, integers define walls / paddles. [x][y], starting bottom left to top right
    private static final int PITCH_SURFACE = 0 ;
    private static final int PITCH_TOP_WALL     = -100 ;
    private static final int PITCH_LOWER_WALL   = -200 ;
    // Paddle identifiers. In the case of either up or down, also the vertical component of the normal relative to a horizontal component = 5
    private static final int PADDLE_MIDDLE      = 100 ;
    private static final int PADDLE_UP_1        = 1 ;
    private static final int PADDLE_UP_2        = 2 ;
    private static final int PADDLE_UP_3        = 3 ;
    private static final int PADDLE_DOWN_1        = -1 ;
    private static final int PADDLE_DOWN_2        = -2 ;
    private static final int PADDLE_DOWN_3        = -3 ;

    // Pitch normals
    private static final double[] NORMAL_PITCH_SURFACE  = {0.0, 0.0} ;
    private static final double[] NORMAL_PITCH_TOP_WALL = {0.0, -1.0} ;
    private static final double[] NORMAL_PITCH_LOWER_WALL = {0.0, 1.0} ;
    private static final double[] NORMAL_PADDLE_MIDDLE  = {1.0, 0.0} ;
    private static final double NORMAL_PADDLE_HORIZONTAL_COMPONENT = 5.0 ;

    /**
     * PongPitch constructor. Use to create an instance of the pong pitch.
     * @param screenHeight  The height of the available screen.
     * @param screenWidth   The width of the available screen.
     */
    public PongPitch(double screenHeight, double screenWidth){
        getPitchParams(screenHeight, screenWidth) ;
        initialisePitch();
    }

    /**
     * Define the pitch parameters for the member variables using the screen height/width and discretisation factor.
     * @param screenHeight  Height of the screen (in pixels)
     * @param screenWidth   Width of the screen (in pixels)
     */
    private void getPitchParams(double screenHeight, double screenWidth) {
        this.height = (int) screenHeight / discretisation ;
        this.width = (int) screenWidth / discretisation ;
    }

    /**
     * Initialise the pitch, giving it the default playing area with the top and bottom walls defined.
     */
    private void initialisePitch() {
        pitch = new int[width][height] ;    // Initialises to 0, as guaranteed by the java language spec. i.e. default pitch of all PITCH_SURFACE is created.
        // Set all top row and bottom row
        for (int i = 0 ; i < width ; i++) {
            pitch[i][height - 1] = PITCH_TOP_WALL ; // Top wall
            pitch[i][0] = PITCH_LOWER_WALL ;        // Lower wall
        }
    }


    /****************************** Public Methods ************************************************/
    /**
     * Method to return the centre of the pitch.
     * Divides width and height by two to give the centre spot coords.
     * @return centreSpot An int array of the centre spot of the format {xCoord, yCoord}
     */
    public int[] getCentreSpot() {
        int xCentre = width / 2 ;
        int yCentre = height / 2 ;
        return new int[]{xCentre, yCentre} ;
    }


    /**
     * Method to return the unit vector normal to be used in calculating the PongBall's velocity in the next timestep.
     * In the case of the ball being on the pich surface, return 0 to prevent any reflection.
     * @param xPosition Ball's xPosition.
     * @param yPosition Ball's yPosition.
     * @return  The unit vector normal to any reflection surface, or 0 if the ball is on the pitch surface.
     */
    public double[] getReflectionNormal(int xPosition, int yPosition) {
        // Determine where the ball is on the pitch, and return the normal if it needs to be reflected.
        switch(pitch[xPosition][yPosition]) {
            case PITCH_SURFACE:
                return NORMAL_PITCH_SURFACE ;
            case PITCH_LOWER_WALL:
                return NORMAL_PITCH_LOWER_WALL ;    // No actual need to differentiate top/bottom for normals, since n or -n can be used for reflection.
            case PITCH_TOP_WALL:
                return NORMAL_PITCH_TOP_WALL ;
            case PADDLE_MIDDLE:
                return NORMAL_PADDLE_MIDDLE ;
            default:
                // Catch any paddle reflections here.
                // Use the number stored in the pitch[][] to get the vertical component, relative to the standard horizontal one.
                return getNormalisedVector(NORMAL_PADDLE_HORIZONTAL_COMPONENT, pitch[xPosition][yPosition]) ;
        }
    }

    /**
     * Method to normalise a vector.
     * Each component is returned after being multiplied by 1/(the square root of the sum of the components squared).
     * @param xVector   Horizontal vector component.
     * @param yVector   Vertical vector component.
     * @return  The normalised vector.
     */
    private double[] getNormalisedVector(double xVector, double yVector){
        // Normalise the vector.
        double factor = 1 / Math.sqrt(xVector*xVector + yVector*yVector) ;
        double[] unitVector = {factor*xVector, factor*yVector} ;
        return unitVector ;
    }


}
