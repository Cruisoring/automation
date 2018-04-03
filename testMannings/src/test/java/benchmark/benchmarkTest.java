package benchmark;

import org.openjdk.jmh.annotations.Benchmark;

public class benchmarkTest {
    @Benchmark
    public String firstBenchmark() {
        int dec = 123456789;
        return Integer.toBinaryString(dec);
    }
}
