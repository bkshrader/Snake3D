package net.bkshrader.snake3d;

import processing.core.PApplet;
import processing.core.PVector;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * A Three-dimensional take on the classic snake game
 * Created by Bradley on 2/13/2017.
 */
public class Game extends PApplet {
    private PVector position, normal, velocity, cameraR, actualCameraR, actualUp, worldCenter, foodPosition;
    private LinkedList<PVector> tail;
    private int dimension, worldDimension;
    private boolean boost;

    private int difficulty = 50;


    @Override
    public void settings() {
        this.size(800, 800, P3D);
    }

    @Override
    public void setup() {
        surface.setTitle("Snake 3D");
        noCursor();

        dimension = 5;
        worldDimension = dimension * 50;
        position = new PVector(0, 0, 0);
        worldCenter = new PVector(width / 2, height / 2, 50);
        velocity = new PVector(0, 0, -1);
        normal = new PVector(0, -1, 0);

        refactorCameraR();
        actualCameraR = cameraR;
        actualUp = velocity.copy();

        placeFood();

        tail = new LinkedList<>();
        tail.add(position.copy());

        boost = false;
    }

    @Override
    public void draw() {
        //Setup
        background(0);
        stroke(255);
        strokeWeight(1f);
        fill(255);
        lights();
        translate(worldCenter); //Modifies coordinate system to be relative to center of world

        //Dilute camera movement
        if (!actualCameraR.equals(cameraR))
            actualCameraR.add(cameraR.copy().sub(actualCameraR).mult(0.1f)); //Add a tenth of the difference

        if (!actualUp.equals(normal))
            actualUp.add(normal.copy().sub(actualUp).mult(0.1f));

        //Place camera in world
        beginCamera();
        camera(actualCameraR.x, actualCameraR.y, actualCameraR.z, 0, 0, 0, -actualUp.x, -actualUp.y, -actualUp.z);
        endCamera();


        pushMatrix();
        //Start Draw

        //Playing field
        pushStyle();
        noFill();
        stroke(255, 0, 0);
        strokeWeight(2f);
        box(worldDimension);
        popStyle();

        //Food pellet
        pushMatrix();
        pushStyle();
        fill(255, 0, 0);
        stroke(255, 0, 0);
        PVector actualFood = foodPosition.copy().mult(dimension);
        translate(actualFood);
        box(dimension);
        popStyle();
        popMatrix();


        //Snake
        Iterator<PVector> snakeTail = tail.iterator();
        while (snakeTail.hasNext()) {
            pushMatrix();
            PVector currentPosition = snakeTail.next();
            PVector actualPosition = currentPosition.copy().mult(dimension);
            translate(actualPosition);
            box(dimension);
            popMatrix();
        }

        //End Draw
        popMatrix();

        //Update positions
        //TODO: change from constant to fraction of food eaten
        //((int) difficulty/tail.size() + 1)
        if (frameCount % 30 == 0 || boost) {
            position.add(velocity);

            //Check if food is eaten
            if (position.equals(foodPosition)) {
//            position.add(velocity);
                tail.addLast(position.copy());
                placeFood();
            } else {
                for (int i = 0; i < tail.size() - 1; i++) {
                    tail.set(i, tail.get(i + 1));
                }
                tail.set(tail.size() - 1, position.copy());
            }
        }

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

        if (key == ' ') {
//            placeFood();
            boost = true;
        }
        //Left arrow - rotate current reference ccw about the y axis (left moves towards player)
        else {
            if (keyCode == LEFT) ;
//            this.rotation.add(0, PI / 2, 0);
                //right arrow - rotate current reference cw about the y axis (left moves away from player)
            else if (keyCode == RIGHT) ;
//            this.rotation.add(0, -PI / 2, 0);
                //Up arrow - rotate current reference cw about the x axis (top moves away from player)
            else if (keyCode == UP) ;
//            this.rotation.add(PI / 2, 0, 0);
                //Down arrow - rotate current reference ccw about the x axis (top moves towards player)
            else if (keyCode == DOWN) ;
        }
    }

    @Override
    public void keyReleased() {
        if (key == ' ') {
            boost = false;
        }
    }

    public void translate(PVector translationVector) {
        translate(translationVector.x, translationVector.y, translationVector.z);
    }

    public void placeFood() {
//        do {
        float xf = (float) Math.random() - 0.5f;
        float yf = (float) Math.random() - 0.5f;
        float zf = (float) Math.random() - 0.5f;
        float x = (float) (xf * (worldDimension / dimension));
        float y = (float) (yf * (worldDimension / dimension));
        float z = (float) (zf * (worldDimension / dimension));

        foodPosition = coercePositionToGrid(new PVector(x, y, z));
//        } while (false); //TODO: check all body collisions
//        System.out.printf("Food placed at %f, %f, %f%n", foodPosition.x, foodPosition.y, foodPosition.z);
    }

    public boolean boxContains(PVector boxPosition, PVector boxDimensions, PVector point) {
        return (point.x < boxPosition.x + boxDimensions.x && point.x > boxPosition.x - boxDimensions.x) && (point.y < boxPosition.y + boxDimensions.y && point.y > boxPosition.y - boxDimensions.y) && (point.z < boxPosition.z + boxDimensions.z && point.z > boxPosition.z - boxDimensions.z);
    }

    public boolean boxContains(PVector boxPosition, float cubeDimension, PVector point) {
        return boxContains(boxPosition, new PVector(cubeDimension, cubeDimension, cubeDimension), point);
    }

    public void kill() {
        //draw killscreen

        noLoop();
    }

    public void refactorCameraR() {
        this.cameraR = velocity.copy();
        this.cameraR = rotateVector(cameraR, normal, PI / 10);
        this.cameraR = rotateVector(cameraR, normal.cross(velocity), PI / 10);
        this.cameraR = this.cameraR.setMag(worldDimension * 1.5f);
        this.cameraR.mult(-1f);
    }

    public void rotateAll(PVector axis) {
        velocity = rotateVector(velocity, axis);
        velocity = coerceVectorToAxis(velocity);
        normal = rotateVector(normal, axis);
        normal = coerceVectorToAxis(normal);
        refactorCameraR();
    }

    public PVector rotateVector(PVector direction, PVector axis, float theta) {
        float x = direction.x, y = direction.y, z = direction.z;
        float u = axis.x, v = axis.y, w = axis.z;

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

    public PVector rotateVector(PVector direction, PVector axis) {
        return rotateVector(direction, axis, PI / 2);
    }

    public PVector coercePositionToGrid(PVector pos) {
        PVector out = pos.copy();
        out.x = floor((worldDimension * pos.x) / worldDimension);
        out.y = floor((worldDimension * pos.y) / worldDimension);
        out.z = floor((worldDimension * pos.z) / worldDimension);
        return out;
    }

    public PVector coerceVectorToAxis(PVector vect) {
        PVector a = vect.normalize();
        do {
            a.x = ((abs(a.x) < .01f) ? 0f : Math.signum(a.x));
            a.y = ((abs(a.y) < .01f) ? 0f : Math.signum(a.y));
            a.z = ((abs(a.z) < .01f) ? 0f : Math.signum(a.z));
        } while (!a.equals(a.normalize()));
        return a;
    }

    public static void main(String[] args) {
        PApplet.main("net.bkshrader.snake3d.Game");
    }
}
