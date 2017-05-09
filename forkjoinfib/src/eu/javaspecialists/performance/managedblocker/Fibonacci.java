package eu.javaspecialists.performance.managedblocker;

import java.math.*;
import java.util.concurrent.*;

public class Fibonacci {
    // demo 1: test100_000_000() time = 45455
    // demo 2: test100_000_000() time = 22691
    // demo 3: test100_000_000() time = 15048


    public BigInteger f(int n) {
        if (n == 0) return BigInteger.ZERO;
        if (n == 1) return BigInteger.ONE;

        int half = (n + 1) / 2;

        RecursiveTask<BigInteger> f0_task = new RecursiveTask<BigInteger>() {
            protected BigInteger compute() {
                return f(half - 1);
            }
        };
        f0_task.fork();
        BigInteger f1 = f(half);
        BigInteger f0 = f0_task.join();

        long time = n > 10000 ? System.currentTimeMillis() : 0;
        try {
            if (n % 2 == 1) {
                return f0.multiply(f0).add(f1.multiply(f1));
            } else {
                return f0.shiftLeft(1).add(f1).multiply(f1);
            }
        } finally {
            time = n > 10000 ? System.currentTimeMillis() - time : 0;
            if (time > 50) {
                System.out.println("fib(" + n +
                    ") took " + time + " ms");
            }
        }
    }
}
