package com.thonners.singpong;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Class to create the pitch on which the game of pong is to be played.
 *
 * @author Thonners
 * @since 07/03/16
 * @version 1.0
 *
 */
public class PongPitchSurfaceView extends SurfaceView implements Runnable {

    private Thread thread = null;
    private SurfaceHolder surfaceHolder;
    volatile boolean running = false;
    private Paint paintBall = new Paint(Paint.ANTI_ALIAS_FLAG);
    private PongBall ball = new PongBall(this);
    private int ballRadius = ball.getBallRadius();
    private PlayActivity playActivity ;

    private static final String LOG_TAG = "PongPitchSurfaceView" ;

    // Pitch params (dictated by the screen)
    private int height ;
    private int width ;

    private int discretisation = 1 ;   // Simplify the pitch by dividing into squares of <discretisation> pixels

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
     * Default Constructor
     * @param context Application Context
     */
    public PongPitchSurfaceView(Context context) {
        super(context) ;
        initialiseSurface(context);
    }

    /**
     * Default Constructor
     * @param context Application Context
     * @param attr  Attribute Set
     */
    public PongPitchSurfaceView(Context context, AttributeSet attr) {
        super(context,attr) ;
        initialiseSurface(context);
    }

    /**
     * Default Constructor
     * @param context Application Context
     * @param attr  Attribute Set
     * @param defStyle  Default Style
     */
    public PongPitchSurfaceView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        initialiseSurface(context);
    }

    private void initialiseSurface(Context context) {
        playActivity = (PlayActivity) context ;
        setWillNotDraw(false);
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(LOG_TAG, "surfaceCreated called.");
                           /*Canvas c = holder.lockCanvas(null);
                           onDraw(c);
                           holder.unlockCanvasAndPost(c);*/
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                                       int width, int height) {
                Log.d(LOG_TAG, "surfaceChanged called.");
                setHeight(height);
                setWidth(width);
                initialisePitch();
                //run();
            }
        });
    }

    private void setHeight(int height) {
        this.height = height ;
    }
    private void setWidth(int width) {
        this.width = width ;
    }
    @Override
    public void onDraw(Canvas canvas) {
        height = getMeasuredHeight() ;
        width = getMeasuredWidth() ;

        Log.d(LOG_TAG, "Pitch height = " + height) ;
        Log.d(LOG_TAG, "Pitch width = " + width) ;

        //initialisePitch();
        //run();
    }

    public void onResumePongPitchSurfaceView(){
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void onPausePongPitchSurfaceView(){
        boolean retry = true;
        running = false;
        while(retry){
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        Log.d(LOG_TAG, "run() called.");
        while(running){
            if(surfaceHolder.getSurface().isValid()){
                // Update ball's position
                ball.update();

                //Log.d(LOG_TAG,"Trying to draw to canvas") ;
                // Get position of ball
                float ballX = (float) ball.getPositionX();
                float ballY = (float) ball.getPositionY();
                // Check hasn't left pitch
                if (ballY < 0 || ballY > height) {
                    Log.e(LOG_TAG, "Error: ballY: " + ballY + " out of bounds.");
                    running = false ;
                }
                if (ballX < 0 ) {
                    // Goal scored by right-hand side player
                    running = false ;
                    playActivity.showGoalScoredToast(getResources().getString(R.string.player_one));
                }
                if (ballX > width) {
                    // Goal scored by left-hand side player
                    running = false ;
                    playActivity.showGoalScoredToast(getResources().getString(R.string.player_two));
                }

                //Log.d(LOG_TAG,"ballX = " + ballX + ", ballY = " + ballY);

                // Draw on canvas
                Canvas canvas = surfaceHolder.lockCanvas();
                // Clear the view
                canvas.drawColor(Color.BLACK);
                // Draw the ball
                canvas.drawCircle(ballX, ballY, ballRadius, paintBall);
                // Post the canvas
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    /****************************** Public Methods ************************************************/
    /**
     * Initialise the pitch, giving it the default playing area with the top and bottom walls defined.
     */
    public void initialisePitch() {
        Log.d(LOG_TAG,"initialisePitch called");
        height = getMeasuredHeight() ;
        width = getMeasuredWidth() ;
        Log.d(LOG_TAG,"height = " + height + ", width = " + width);
        pitch = new int[width][height] ;    // Initialises to 0, as guaranteed by the java language spec. i.e. default pitch of all PITCH_SURFACE is created.
        // Set all top row and bottom row
        for (int i = 0 ; i < width ; i++) {
            // Offset the boundaries to the ball's radius, so it reflects when it touches a wall. Fill up all the spaces so that if the vertical velocity is greater than one it will still be reflected. (Assuming that the velocity is less than the radius of the ball)
            for (int j = 0; j <= ballRadius; j++){
                pitch[i][height - 1 - j] = PITCH_LOWER_WALL;   // Lower wall
                pitch[i][j] = PITCH_TOP_WALL;              // Top wall
            }
        }

        // Paint settings for the ball
        paintBall.setStyle(Paint.Style.FILL);
        paintBall.setColor(Color.RED);
        Log.d(LOG_TAG, "initialisePitch returning... pitch.length = " + pitch.length);

        // Initialise the ball's position
        ball.initialise();
    }
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
     * In the case of the ball being on the pitch surface, return 0 to prevent any reflection.
     * @param xPosition Ball's xPosition.
     * @param yPosition Ball's yPosition.
     * @return  The unit vector normal to any reflection surface, or 0 if the ball is on the pitch surface.
     */
    public double[] getReflectionNormal(int xPosition, int yPosition) {
        try {
            // Determine where the ball is on the pitch, and return the normal if it needs to be reflected.
            Log.d(LOG_TAG, "getReflectionNormal called. pitch.length = " + pitch.length);
            switch (pitch[xPosition][yPosition]) {
                case PITCH_SURFACE:
                    return NORMAL_PITCH_SURFACE;
                case PITCH_LOWER_WALL:
                    return NORMAL_PITCH_LOWER_WALL;    // No actual need to differentiate top/bottom for normals, since n or -n can be used for reflection.
                case PITCH_TOP_WALL:
                    return NORMAL_PITCH_TOP_WALL;
                case PADDLE_MIDDLE:
                    return NORMAL_PADDLE_MIDDLE;
                default:
                    // Catch any paddle reflections here.
                    // Use the number stored in the pitch[][] to get the vertical component, relative to the standard horizontal one.
                    return getNormalisedVector(NORMAL_PADDLE_HORIZONTAL_COMPONENT, pitch[xPosition][yPosition]);
            }
        } catch (Exception e) {
            Log.d(LOG_TAG,"Caught exception when trying to get reflection normal.");
            e.printStackTrace();
            return NORMAL_PITCH_SURFACE ;
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
