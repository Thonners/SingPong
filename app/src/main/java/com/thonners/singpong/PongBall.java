package com.thonners.singpong;

import android.util.Log;

import java.util.Random;

/**
 * Class defining the ball to be bounced around the pitch
 *
 * @author Thonners
 * @since 12/02/16
 * @version 1.0
 */
public class PongBall {

    private static final String LOG_TAG = "PongBall" ;

    private PongPitchSurfaceView pitch ;
    private int ballRadius = 50 ;
    // Array locations - in position/velocity, etc. To make it easier to read
    private static final int X = 0 ;
    private static final int Y = 1 ;
    // Location and velocity components
    private int[] position = new int[2] ;
    private int[] velocity  = new int[2];    // Ball's velocity components. Units are pitch steps / timestep.

    /**
     * PongBall Default Constructor
     */
    public PongBall(PongPitchSurfaceView pitch) {
        this.pitch = pitch ;
        initialise() ;
    }

    /**
     * Method to initialise the member variables. Location set to the pitch centre spot, and the velocity to some random vector, with a non-zero X component.
     */
    public void initialise() {
        // Positions - set to centre spot
        position = pitch.getCentreSpot() ;
        // Velocities (Randomise to start)
        int speed = 5 ; // Net speed of ball
        int xComponent = new Random().nextInt(1000) ;
        int yComponent = new Random().nextInt(1000) ;
        double magnitude = Math.sqrt(xComponent*xComponent + yComponent*yComponent);
        Log.d(LOG_TAG, "xC = " + xComponent + ", yC = " + yComponent + ", magnitude = " + magnitude + ", xVel = " + ((int) (speed * xComponent / magnitude)));
        velocity[X] = ((int) (speed * xComponent / magnitude)) ;
        velocity[Y] = ((int) (speed * yComponent / magnitude)) ;
        // Check X velocity is non-zero
        if(velocity[X] == 0) {
            Log.d(LOG_TAG, "Caught zero x component for velocity so calling initialise again...");
            initialise();
        }
    }

    /**
     * Method to return the location of the PongBall for the next timestep.
     * Integrates the velocity and adds it to the previous position.
     *
     * @return  Position for next time step.
     */
    private int[] updatePosition() {
        // Integrate old position + velocity (timestep = 1) to get new position
        return new int[]{position[X] + velocity[X] , position[Y] + velocity[Y]} ;
    }

    /**
     * Method to calculate the velocity of the PongBall for the next timestep.
     * If the ball is in free space, velocity is unchanged. If it is touching a wall/paddle, the
     * new velocity is given by the formula:
     * v1 = v0 - 2*(v0 . n)*n
     * where v1 is the new velocity, v0 is the initial velocity, and n is the wall/paddle normal vector.
     *
     * Calculations are done as doubles, until the final return where the values are cast back to ints.
     *
     * @return          int array of {xVel, yVel} for the ball's next timestep.
     */
    private int[] updateVelocity() {
        // Reflection normal
        double[] normal = pitch.getReflectionNormal(position[X], position[Y]) ;
        // Factor to be multiplied to each of the normal vector's components before adding to previous velocity's components:
        double nFactor = -2.0*(velocity[X]*normal[X] + velocity[Y]*normal[Y]) ;
        double xComponent = velocity[X] + nFactor*normal[X] ;
        double yComponent = velocity[Y] + nFactor*normal[Y] ;
        //Log.d(LOG_TAG,"Current position: " + getPositionX() +", " + getPositionY() + ". Velocity = " + xComponent + ", " + yComponent);
        // Cast to ints in the final return.
        return new int[]{(int) xComponent, (int) yComponent} ;
    }


    /****************************** Public Methods ************************************************/
    /**
     * Public method to update the position and velocity parameters of the ball.
     */
    public void update() {
        velocity = updateVelocity();
        position = updatePosition();
    }

    /**
     * Method to return the X coordinate of the ball's position
     * @return X position
     */
    public int getPositionX(){
        return position[0];
    }
    /**
     * Method to return the Y coordinate of the ball's position
     * @return Y position
     */
    public int getPositionY(){
        return position[1];
    }

    /**
     * Method to return the ball's radius
     * @return int Ball's radius
     */
    public int getBallRadius() {
        return ballRadius;
    }
}
