package net.bkshrader.snake3d;

import processing.core.PApplet;
import processing.core.PVector;

import static java.lang.Math.PI;
import static net.bkshrader.snake3d.VectorUtils.coerceVectorToAxis;
import static net.bkshrader.snake3d.VectorUtils.rotateVector;

/**
 * Created by Bradley on 2/19/2017.
 */
public class Camera {
    public PVector position, normal;
    public PVector aPos, aNormal;
    private Game host;

    public Camera(Game host) {
        this.host = host;

        this.refactor();

        this.aPos = position.copy();
        this.aNormal = normal.copy();
    }

    public void place() {
        //Dilute camera movement
        if (!aPos.equals(position))
            aPos.add(position.copy().sub(aPos).mult(0.1f));

        if (!aNormal.equals(normal))
            aNormal.add(normal.copy().sub(aNormal).mult(0.1f));

        //Place camera in world
        host.camera(aPos.x, aPos.y, aPos.z, 0, 0, 0, -aNormal.x, -aNormal.y, -aNormal.z);
    }

    public void peek(PVector axis) {
        this.position = rotateVector(host.player.velocity.copy(), axis);
        this.position = this.position.setMag(host.worldDimension * 1.5f);
        this.position.mult(-1f);

        this.normal = coerceVectorToAxis(rotateVector(host.player.normal.copy(), axis));
    }

    public void refactor() {
        this.position = host.player.velocity.copy();
        this.position = rotateVector(position, host.player.normal, (float) (PI / 10));
        this.position = rotateVector(position, host.player.normal.cross(host.player.velocity), (float) (PI / 10));
        this.position = this.position.setMag(host.worldDimension * 1.5f);
        this.position.mult(-1f);

        this.normal = host.player.normal.copy();
    }
}
