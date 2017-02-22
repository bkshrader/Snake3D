package net.bkshrader.snake3d;

import processing.core.PVector;

import java.util.Iterator;
import java.util.LinkedList;

import static net.bkshrader.snake3d.VectorUtils.coercePositionToGrid;

/**
 * Controls position and movement of the snake
 * Created by Bradley on 2/19/2017.
 */
public class Snake {
    private Game host;
    public PVector position, velocity, normal;
    public LinkedList<PVector> tail;
    public boolean isDead;
    public int color;

    public Snake(Game host) {
        this.host = host;

        velocity = new PVector(0, 0, -1);
        normal = new PVector(0, -1, 0);
        position = new PVector(0, 0, 0);

        tail = new LinkedList<>();
        tail.add(position.copy());

        isDead = false;
        color = host.color(255);
    }

    public void draw() {
        //Snake
        Iterator<PVector> snakeTail = tail.iterator();
        while (snakeTail.hasNext()) {
            host.pushMatrix();
            PVector currentPosition = snakeTail.next();
            PVector actualPosition = currentPosition.copy().mult(host.dimension);
            host.translate(actualPosition);

            //Test Collision
            host.pushStyle();
            float actualDimension = host.dimension;
            if (currentPosition.equals(position) && snakeTail.hasNext()) {
                actualDimension *= 1.1f;
                host.stroke(255, 0, 0);
                host.fill(255, 0, 0);
                this.isDead = true;
            } else {
                host.stroke(color);
                host.fill(color);
            }

            host.box(actualDimension);
            host.popStyle();
            host.popMatrix();
        }
    }

    public void update() {
        position.add(velocity);

        //Check if food is eaten
        if (position.equals(host.foodPosition)) {
            tail.addLast(position.copy());
            host.placeFood();
        } else {
            for (int i = 0; i < tail.size() - 1; i++) {
                tail.set(i, tail.get(i + 1));
            }
            tail.set(tail.size() - 1, position.copy());
        }
    }

    public boolean isOutOfField() {
        return position.x > 0.5f * host.worldSize ||
                position.x < -0.5f * host.worldSize ||
                position.y > 0.5f * host.worldSize ||
                position.y < -0.5f * host.worldSize ||
                position.z > 0.5f * host.worldSize ||
                position.z < -0.5f * host.worldSize;
    }

    public boolean collidesBody(PVector point) {
        point = coercePositionToGrid(point);
        for (PVector body : tail) {
            if (body != null && body.equals(point))
                return true;
        }
        return false;
    }
}
