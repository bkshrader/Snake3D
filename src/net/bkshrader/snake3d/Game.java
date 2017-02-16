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
    private PVector position, normal, velocity, cameraR, actualCameraR, cameraUp, actualUp, worldCenter, foodPosition;
    private LinkedList<PVector> tail;
    private int dimension, worldDimension;
    private boolean boost, dead;

    //TODO: Make an option that doesn't require recompiling
    private final boolean invertControls = true;
    private final int difficulty = 50;
    private final int worldSize = 50;


    @Override
    public void settings() {
        this.size(800, 800, P3D);
    }

    @Override
    public void setup() {
        surface.setTitle("Snake 3D");
        noCursor();

        dimension = 5;
        worldDimension = dimension * worldSize;
        position = new PVector(0, 0, 0);
        worldCenter = new PVector(width / 2, height / 2, 50);
        velocity = new PVector(0, 0, -1);
        normal = new PVector(0, -1, 0);

        refactorCamera();
        actualCameraR = cameraR.copy();
        actualUp = cameraUp.copy();

        tail = new LinkedList<>();
        tail.add(position.copy());

        dead = false;
        boost = false;

        placeFood();
    }

    @Override
    public void draw() {
        if (dead)
            return;

        //Setup
        background(0);
        stroke(255);
        strokeWeight(1f);
        fill(255);
        lights();
        translate(worldCenter); //Modifies coordinate system to be relative to center of world

        //Dilute camera movement
        if (!actualCameraR.equals(cameraR))
            actualCameraR.add(cameraR.copy().sub(actualCameraR).mult(0.1f));

        if (!actualUp.equals(cameraUp))
            actualUp.add(cameraUp.copy().sub(actualUp).mult(0.1f));

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

            //Test Collision
            pushStyle();
            float actualDimension = dimension;
            if (currentPosition.equals(position) && snakeTail.hasNext()) {
                actualDimension *= 1.1f;
                stroke(255, 0, 0);
                fill(255, 0, 0);
                dead = true;
            }
            box(actualDimension);
            popStyle();
            popMatrix();
        }

        //End Draw
        popMatrix();

        //Update positions
        if (frameCount % Math.max(difficulty - tail.size() + 1, 0) == 0 || boost) {
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


        if (dead || outOfField())
            kill();

    }

    @Override
    public void keyPressed() {

        if (key == 'w' || key == 'a' || key == 's' || key == 'd') {
            PVector axis = new PVector();

            switch (key) {
                case 'w':
                    velocity.cross(normal, axis);
                    if(invertControls) {
                        axis.mult(-1f);
                    }
                    break;
                case 'a':
                    axis = normal.copy().mult(-1f);
                    break;
                case 's':
                    normal.cross(velocity, axis);
                    if (invertControls) {
                        axis.mult(-1f);
                    }
                    break;
                case 'd':
                    axis = normal.copy();
                    break;
            }

            rotateAll(axis);
        }

        if (key == ' ') {
            if (!dead)
                boost = true;
            else
                setup();
        }

        //Arrow Keys allow camera to peek around corner for better view
        else {

            if (keyCode == LEFT) {
                peekCamera(cameraUp);
            } else if (keyCode == RIGHT) {
                peekCamera(cameraUp.copy().mult(-1f));
            } else if (keyCode == UP) {
                peekCamera(normal.copy().cross(coerceVectorToAxis(velocity.copy())));
            } else if (keyCode == DOWN) {
                peekCamera(coerceVectorToAxis(velocity.copy()).cross(normal.copy()));
            }
        }
    }

    @Override
    public void keyReleased() {
        if (key == ' ') {
            boost = false;
        } else if (keyCode == LEFT || keyCode == RIGHT || keyCode == UP || keyCode == DOWN) {
            refactorCamera();
        }
    }

    private boolean outOfField() {
        return position.x > 0.5f * worldSize ||
                position.x < -0.5f * worldSize||
                position.y > 0.5f * worldSize ||
                position.y < -0.5f * worldSize||
                position.z > 0.5f * worldSize ||
                position.z < -0.5f * worldSize;
    }

    private void translate(PVector translationVector) {
        translate(translationVector.x, translationVector.y, translationVector.z);
    }

    private void placeFood() {
        do {
            float x = (float) (Math.random() - 0.5f) * (worldDimension / dimension);
            float y = (float) (Math.random() - 0.5f) * (worldDimension / dimension);
            float z = (float) (Math.random() - 0.5f) * (worldDimension / dimension);

            foodPosition = coercePositionToGrid(new PVector(x, y, z));
        } while (collidesBody(foodPosition));
    }

    private boolean collidesBody(PVector point) {
        point = coercePositionToGrid(point);
        for (PVector body : tail) {
            if (body != null && body.equals(point))
                return true;
        }
        return false;
    }

    private void kill() {
        dead = true;

        //draw kill screen
        pushMatrix();
        pushStyle();

        camera();
        noLights();

        translate(0, 0, worldDimension);

        textAlign(CENTER, BOTTOM);
        textSize(42);
        fill(255);
        text("Game Over", width / 2, height / 2 - 10);
        textAlign(CENTER, TOP);
        textSize(24);
        text(String.format("Score: %d", tail.size() - 1), width / 2f, height / 2f + 10);
        text("Press spacebar to restart", width / 2f, height / 2f + 50);
        popStyle();
        popMatrix();
    }

    private void refactorCamera() {
        this.cameraR = velocity.copy();
        this.cameraR = rotateVector(cameraR, normal, PI / 10);
        this.cameraR = rotateVector(cameraR, normal.cross(velocity), PI / 10);
        this.cameraR = this.cameraR.setMag(worldDimension * 1.5f);
        this.cameraR.mult(-1f);

        this.cameraUp = normal.copy();
    }

    private void peekCamera(PVector axis) {
        this.cameraR = rotateVector(velocity.copy(), axis);
        this.cameraR = this.cameraR.setMag(worldDimension * 1.5f);
        this.cameraR.mult(-1f);

        this.cameraUp = coerceVectorToAxis(rotateVector(normal.copy(), axis));
    }

    private void rotateAll(PVector axis) {
        velocity = rotateVector(velocity, axis);
        velocity = coerceVectorToAxis(velocity);
        normal = rotateVector(normal, axis);
        normal = coerceVectorToAxis(normal);
        refactorCamera();
    }

    private PVector rotateVector(PVector direction, PVector axis, float theta) {
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

    private PVector rotateVector(PVector direction, PVector axis) {
        return rotateVector(direction, axis, PI / 2);
    }

    private PVector coercePositionToGrid(PVector pos) {
        PVector out = pos.copy();
        out.x = floor((worldDimension * pos.x) / worldDimension);
        out.y = floor((worldDimension * pos.y) / worldDimension);
        out.z = floor((worldDimension * pos.z) / worldDimension);
        return out;
    }

    private PVector coerceVectorToAxis(PVector vector) {
        PVector a = vector.normalize();
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
