package net.bkshrader.snake3d;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

/**
 * A Three-dimensional take on the classic snake game
 * Created by Bradley on 2/13/2017.
 */
public class Game extends PApplet {
    private PVector position, normal, velocity, cameraR, actualCameraR, actualUp, worldCenter;
    private ArrayList<PVector> tail;
    private int dimension, worldDimension;


    @Override
    public void settings() {
        this.size(800, 800, P3D);
    }

    @Override
    public void setup() {
        surface.setTitle("Snake 3D");
        noCursor();

        dimension = 50;
        worldDimension = dimension * 10;
        position = new PVector(0, 0, 0);
        worldCenter = new PVector(width / 2, height / 2, 50);
        velocity = new PVector(0, 0, -1);
        normal = new PVector(0, -1, 0);

        refactorCameraR();
        actualCameraR = cameraR;
        actualUp = velocity.copy();

    }

    @Override
    public void draw() {
        background(0);
        stroke(255);
        strokeWeight(1f);
        fill(255);
        lights();
        translate(worldCenter.x, worldCenter.y, worldCenter.z);

        //dilute camera movement
        if (!vectorsEqual(actualCameraR, cameraR))
            actualCameraR.add(cameraR.copy().sub(actualCameraR).mult(0.1f)); //Add a tenth of the difference
        if (!vectorsEqual(actualUp, velocity))
            actualUp.add(velocity.copy().sub(actualUp).mult(0.1f));

        beginCamera();
        camera(actualCameraR.x, actualCameraR.y, actualCameraR.z, 0, 0, 0, -actualUp.x, -actualUp.y, -actualUp.z);
        endCamera();


        pushMatrix();
        //Draw

        //Playing field
        pushStyle();
        noFill();
        stroke(255, 0, 0);
        strokeWeight(2f);
        box(worldDimension);
        popStyle();

        //snake head
        translate(position.x, position.y, position.z);
        box(dimension);

//        actualCameraR = cameraR.copy();
//        actualUp = velocity.copy();
        popMatrix();


        //update position
        position.add(velocity);


//        if(position.x > width || position.x < 0 || position.y > width || position.y < 0 || position.z < -width || position.z > 0)
//            kill();

    }

    @Override
    public void keyPressed() {
        if (key == 'w' || key == 'a' || key == 's' || key == 'd') {
            PVector axis = new PVector();
            switch (key) {
                case 'w':
                    velocity.cross(normal, axis);
                    break;
                case 'a':
                    axis = normal.copy().mult(-1f);
                    break;
                case 's':
                    normal.cross(velocity, axis);
                    break;
                case 'd':
                    axis = normal.copy();
                    break;
            }

            rotateAll(axis);
        }
        //Left arrow - rotate current reference ccw about the y axis (left moves towards player)
        else if (keyCode == LEFT) ;
//            this.rotation.add(0, PI / 2, 0);
            //right arrow - rotate current reference cw about the y axis (left moves away from player)
        else if (keyCode == RIGHT) ;
//            this.rotation.add(0, -PI / 2, 0);
            //Up arrow - rotate current reference cw about the x axis (top moves away from player)
        else if (keyCode == UP) ;
//            this.rotation.add(PI / 2, 0, 0);
            //Down arrow - rotate current reference ccw about the x axis (top moves towards player)
        else if (keyCode == DOWN) ;
//            this.rotation.add(-PI / 2, 0, 0);
    }

    public void kill() {
        //draw killscreen

        noLoop();
    }

    public void refactorCameraR() {
        this.cameraR = normal.copy();
        this.cameraR = this.cameraR.setMag(worldDimension * 1.5f);
    }

    public void rotateAll(PVector axis) {
        velocity = rotateVector(velocity, axis);
        normal = rotateVector(normal, axis);
        refactorCameraR();
    }

    public PVector rotateVector(PVector direction, PVector axis) {
        float x = direction.x, y = direction.y, z = direction.z;
        float u = axis.x, v = axis.y, w = axis.z;
        float theta = PI / 2;


        float xPrime = u * (u * x + v * y + w * z) * (1f - cos(theta))
                + x * cos(theta)
                + (-w * y + v * z) * sin(theta);
        float yPrime = v * (u * x + v * y + w * z) * (1f - cos(theta))
                + y * cos(theta)
                + (w * x - u * z) * sin(theta);
        float zPrime = w * (u * x + v * y + w * z) * (1f - cos(theta))
                + z * cos(theta)
                + (-v * x + u * y) * sin(theta);

        return new PVector(xPrime, yPrime, zPrime).normalize();
    }

    public PVector coercePositionToGrid(PVector pos) {
        PVector out = pos.copy();
        out.x = dimension * floor(dimension * pos.x / width);
        out.y = dimension * floor(dimension * pos.y / width);
        out.z = dimension * floor(dimension * pos.z / width);
        return out;
    }

    public PVector coerceVectorToAxis(PVector vect) {
        PVector a = vect.normalize();
        do {
            a.x = ((a.x < .5f) ? 0f : 1f);
            a.y = ((a.y < .5f) ? 0f : 1f);
            a.z = ((a.z < .5f) ? 0f : 1f);
        } while (!vectorsEqual(a, a.normalize()));
        return a;
    }

    public boolean vectorsEqual(PVector first, PVector... vectors) {
        float ax = first.x, ay = first.y, az = first.z;
        for (PVector v : vectors)
            if ((v.x - ax) / ax > 0.00001f || (v.y - ay) / ay > 0.00001f || (v.z - az) / az > 0.00001f) //Allows 0.001% error
                return false;
        return true;
    }

    public static void main(String[] args) {
        PApplet.main("net.bkshrader.snake3d.Game");
    }
}
