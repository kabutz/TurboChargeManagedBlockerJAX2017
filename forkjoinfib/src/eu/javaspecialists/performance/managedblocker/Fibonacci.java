package eu.javaspecialists.performance.managedblocker;

import java.math.*;
import java.util.*;
import java.util.concurrent.*;

public class Fibonacci {
    // demo 1: test100_000_000() time = 45455
    // demo 2: test100_000_000() time = 22691
    // demo 3: test100_000_000() time = 15048
    // demo 4: test100_000_000() time = 10387
    // demo 5: test100_000_000() time = 7449
    // demo 6: test100_000_000() time = 7513


    private final BigInteger RESERVED = BigInteger.valueOf(-1000);

    public BigInteger f(int n) {
        Map<Integer, BigInteger> cache = new ConcurrentHashMap<>();
        cache.put(0, BigInteger.ZERO);
        cache.put(1, BigInteger.ONE);
        return f(n, cache);
    }

    private class ReservedFibonacciBlocker implements ForkJoinPool.ManagedBlocker {
        private final int n;
        private final Map<Integer, BigInteger> cache;
        private BigInteger result;

        public ReservedFibonacciBlocker(int n, Map<Integer, BigInteger> cache) {
            this.n = n;
            this.cache = cache;
        }

        public boolean isReleasable() {
            return (result = cache.get(n)) != RESERVED;
        }

        public boolean block() throws InterruptedException {
            synchronized (RESERVED) {
                while (!isReleasable()) {
                    RESERVED.wait();
                }
            }
            return true;
        }
    }

    private BigInteger f(int n, Map<Integer, BigInteger> cache) {
        BigInteger result = cache.putIfAbsent(n, RESERVED);
        if (result == null) {
            int half = (n + 1) / 2;

            RecursiveTask<BigInteger> f0_task = new RecursiveTask<BigInteger>() {
                protected BigInteger compute() {
                    return f(half - 1, cache);
                }
            };
            f0_task.fork();
            BigInteger f1 = f(half, cache);
            BigInteger f0 = f0_task.join();

            long time = n > 10000 ? System.currentTimeMillis() : 0;
            try {
                if (n % 2 == 1) {
                    result = f0.multiply(f0).add(f1.multiply(f1));
                } else {
                    result = f0.shiftLeft(1).add(f1).multiply(f1);
                }
                synchronized (RESERVED) {
                    cache.put(n, result);
                    RESERVED.notifyAll();
                }
            } finally {
                time = n > 10000 ? System.currentTimeMillis() - time : 0;
                if (time > 50) {
                    System.out.println("fib(" + n +
                        ") took " + time + " ms");
                }
            }
        } else if (result == RESERVED) {
            // wait until result != RESERVED
            try {
                ReservedFibonacciBlocker blocker = new ReservedFibonacciBlocker(n, cache);
                ForkJoinPool.managedBlock(blocker);
//                blocker.block();
                result = blocker.result;
            } catch (InterruptedException e) {
                throw new CancellationException("interrupted");
            }
        }
        return result;
    }
}
