package net.bkshrader.snake3d;

import processing.core.PApplet;
import processing.core.PVector;

/** A Three-dimensional take on the classic snake game
 * Created by Bradley on 2/13/2017.
 */
public class Game extends PApplet {
    private PVector rotation;

    @Override
    public void settings() {
        this.size(800, 600, P3D);
    }

    @Override
    public void setup() {
        surface.setTitle("Snake 3D");
//        noCursor();

        rotation = new PVector(0, 0, 0);
    }

    @Override
    public void draw() {
        background(0);
        stroke(255);
        strokeWeight(1f);
        fill(255);
        lights();

        translate(width/2, height/2, -50);

        rotateX(rotation.x);
        rotateY(rotation.y);
        rotateZ(rotation.z);

        box(50);
    }

    @Override
    public void keyPressed() {
        //W - move up in current reference
        //A - move left in current reference
        //S - move down in current reference
        //D - move right in current reference

        //Left arrow - rotate current reference ccw about the y axis (left moves towards player)
        if (keyCode == LEFT)
            this.rotation.add(0, PI / 4, 0);
            //right arrow - rotate current reference cw about the y axis (left moves away from player)
        else if (keyCode == RIGHT)
            this.rotation.add(0, -PI / 4, 0);
            //Up arrow - rotate current reference cw about the x axis (top moves away from player)
        else if (keyCode == UP)
            this.rotation.add(PI / 4, 0, 0);
            //Down arrow - rotate current reference ccw about the x axis (top moves towards player)
        else if (keyCode == DOWN)
            this.rotation.add(-PI / 4, 0, 0);
    }


    public static void main(String[] args) {
        PApplet.main("net.bkshrader.snake3d.Game");
    }
}
