package net.bkshrader.snake3d;

import processing.core.PVector;

/**
 * Created by Bradley on 2/19/2017.
 */
public class VectorUtils {
    private static Game g;

    static{
        g = Game.getInstance();
    }

    public static PVector rotateVector(PVector direction, PVector axis, float theta) {
        float x = direction.x, y = direction.y, z = direction.z;
        float u = axis.x, v = axis.y, w = axis.z;

        float xPrime = (float) (u * (u * x + v * y + w * z) * (1f - Math.cos(theta))
                        + x * Math.cos(theta)
                        + (-w * y + v * z) * Math.sin(theta));
        float yPrime = (float) (v * (u * x + v * y + w * z) * (1f - Math.cos(theta))
                        + y * Math.cos(theta)
                        + (w * x - u * z) * Math.sin(theta));
        float zPrime = (float) (w * (u * x + v * y + w * z) * (1f - Math.cos(theta))
                        + z * Math.cos(theta)
                        + (-v * x + u * y) * Math.sin(theta));

        return new PVector(xPrime, yPrime, zPrime).normalize();
    }

    public static PVector rotateVector(PVector direction, PVector axis) {
        return rotateVector(direction, axis, (float) (Math.PI / 2f));
    }

    public static PVector coercePositionToGrid(PVector pos) {
        PVector out = pos.copy();
        out.x = (float) Math.floor((g.worldDimension * pos.x) / g.worldDimension);
        out.y = (float) Math.floor((g.worldDimension * pos.y) / g.worldDimension);
        out.z = (float) Math.floor((g.worldDimension * pos.z) / g.worldDimension);
        return out;
    }

    public static PVector coerceVectorToAxis(PVector vector) {
        PVector a = vector.copy().normalize();
        do {
            a.x = ((Math.abs(a.x) < .01f) ? 0f : Math.signum(a.x));
            a.y = ((Math.abs(a.y) < .01f) ? 0f : Math.signum(a.y));
            a.z = ((Math.abs(a.z) < .01f) ? 0f : Math.signum(a.z));
        } while (!a.equals(a.copy().normalize()));
        return a;
    }
}
