package benchmark;

import org.openjdk.jmh.annotations.Benchmark;

public class benchmarkTest {
    public static final int ArraySize = 100000;

    public static final int[] intArray = new int[ArraySize];

    @Benchmark
    public void testIntArray_SimplestOperation(){
        int length = intArray.length;
        int min = Integer.MIN_VALUE;

        for (int i = 0; i < length; i++) {
            if(intArray[i] > min)
                min = intArray[i];
        }
    }
}

