package eu.javaspecialists.performance.managedblocker.issue223;

import java.util.concurrent.atomic.*;

public class PositionAtomicNonBlockingFU {
    private volatile double[] xy = {0, 0};
    private final static AtomicReferenceFieldUpdater<PositionAtomicNonBlockingFU, double[]> XY =
        AtomicReferenceFieldUpdater.newUpdater(PositionAtomicNonBlockingFU.class,
            double[].class, "xy");

    public void move(double deltaX, double deltaY) {
        double[] current, next = new double[2];
        do {
            current = xy;
            next[0] = current[0] + deltaX;
            next[1] = current[1] + deltaY;
        } while (!XY.compareAndSet(this, current, next));
    }
}
