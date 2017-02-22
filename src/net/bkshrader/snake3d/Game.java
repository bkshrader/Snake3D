package net.bkshrader.snake3d;

import processing.core.PApplet;
import processing.core.PVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

import static net.bkshrader.snake3d.VectorUtils.coercePositionToGrid;
import static net.bkshrader.snake3d.VectorUtils.coerceVectorToAxis;
import static net.bkshrader.snake3d.VectorUtils.rotateVector;

/**
 * A Three-dimensional take on the classic snake game
 * Created by Bradley on 2/13/2017.
 */
public final class Game extends PApplet {
    private static Game instance;

    public Snake player;
    public Camera camera;
    public PVector worldCenter, foodPosition;
    public int dimension, worldDimension;
    public boolean boost;


    //settings
    public boolean invertControls;
    public int difficulty;
    public int worldSize;


    @Override
    public void settings() {
        instance = this;
        this.size(800, 800, P3D);
    }

    @Override
    public void setup() {
        surface.setTitle("Snake 3D");
        surface.setResizable(true);

//        importSettings();
        worldSize = 50;
        difficulty = 50;

        //Changes with settings TODO: Move to method
        dimension = 5;
        worldDimension = dimension * worldSize;
        worldCenter = new PVector(width / 2, height / 2, 50);

        player = new Snake(this);

        camera = new Camera(this);

        boost = false;

        placeFood();
    }

    @Override
    public void draw() {
        if (player.isDead) //Change this to modify end screen
            return;

        //Setup
        background(0);
        stroke(255);
        strokeWeight(1f);
        fill(255);
        lights();
        translate(worldCenter); //Modifies coordinate system to be relative to center of world

        camera.place();

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
        player.draw();

        //End Draw
        popMatrix();

        //Update positions
        if (frameCount % Math.max(difficulty - player.tail.size() - 1, 1) == 0 || boost) {
            player.update();
        }


        if (player.isDead || player.isOutOfField())
            kill();

    }

    @Override
    public void keyPressed() {
        if ("wasd".contains(Character.toString(key).toLowerCase())) {
            PVector axis = new PVector();

            switch (key) {
                case 'w':
                case 'S':
                    player.velocity.cross(player.normal, axis);
                    break;
                case 'a':
                case 'A':
                    axis = player.normal.copy().mult(-1f);
                    break;
                case 's':
                case 'W':
                    player.normal.cross(player.velocity, axis);
                    break;
                case 'd':
                case 'D':
                    axis = player.normal.copy();
                    break;
            }

            rotateAll(axis);
        }

        if (key == ' ') {
            if (!player.isDead)
                boost = true;
            else
                setup();
        }

        //Arrow Keys allow camera to peek around corner for better view
        else {
            if (keyCode == LEFT) {
                camera.peek(camera.normal);
            } else if (keyCode == RIGHT) {
                camera.peek(camera.normal.copy().mult(-1f));
            } else if (keyCode == UP) {
                camera.peek(player.normal.copy().cross(coerceVectorToAxis(player.velocity.copy())));
            } else if (keyCode == DOWN) {
                camera.peek(coerceVectorToAxis(player.velocity.copy()).cross(player.normal.copy()));
            }
        }
    }

    @Override
    public void keyReleased() {
        if (key == ' ') {
            boost = false;
        } else if (keyCode == LEFT || keyCode == RIGHT || keyCode == UP || keyCode == DOWN) {
            camera.refactor();
        }
    }

    //TODO: Total rewrite
    private void importSettings() {
        try {
            File settings = new File("../settings.txt");
            if (!(settings.exists() && settings.isFile())) {
                //Create default settings file
                //noinspection ResultOfMethodCallIgnored
                settings.createNewFile();
                PrintWriter settingsWriter = new PrintWriter(settings);
                settingsWriter.println("50:worldSize");
                settingsWriter.println("50:difficulty");
                settingsWriter.println("true:invertControls");
                settingsWriter.close();
                throw new FileNotFoundException("No settings file found. Generated new one.");
            } else {
                Scanner settingScanner = new Scanner(settings);
                worldSize = Integer.parseInt(settingScanner.nextLine().split(":")[0]);
                difficulty = Integer.parseInt(settingScanner.nextLine().split(":")[0]);
                invertControls = Boolean.parseBoolean(settingScanner.nextLine().split(":")[0]);
            }
        } catch (Exception e) {
            System.err.println("Unable to load settings, intitialized to defaults instead");
            e.printStackTrace();

            //Default Settings
            worldSize = 50;
            difficulty = 50;
        }
    }


    public void translate(PVector translationVector) {
        translate(translationVector.x, translationVector.y, translationVector.z);
    }

    public void placeFood() {
        do {
            float x = (float) (Math.random() - 0.5f) * (worldDimension / dimension);
            float y = (float) (Math.random() - 0.5f) * (worldDimension / dimension);
            float z = (float) (Math.random() - 0.5f) * (worldDimension / dimension);

            foodPosition = coercePositionToGrid(new PVector(x, y, z));
        } while (player.collidesBody(foodPosition));
    }



    private void kill() {
        player.isDead = true;

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
        text(String.format("Score: %d", player.tail.size() - 1), width / 2f, height / 2f + 10);
        text("Press spacebar to restart", width / 2f, height / 2f + 50);
        popStyle();
        popMatrix();
    }

    private void rotateAll(PVector axis) {
        player.velocity = rotateVector(player.velocity, axis);
        player.velocity = coerceVectorToAxis(player.velocity);
        player.normal = rotateVector(player.normal, axis);
        player.normal = coerceVectorToAxis(player.normal);
        camera.refactor();
    }

    public static Game getInstance() {
        if (instance == null)
            throw new IllegalStateException("Game has not been initialized yet");
        return instance;
    }

    public static void main(String[] args) {
        PApplet.main("net.bkshrader.snake3d.Game");
    }
}
