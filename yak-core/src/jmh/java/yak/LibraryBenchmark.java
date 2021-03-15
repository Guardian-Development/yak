package yak;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * An example benchmark, proving we cna use jmh when we need it.
 */
@State(Scope.Benchmark)
public class LibraryBenchmark {

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  public void exampleBenchmark(final Blackhole bh) {
    final var test = new Library();
    bh.consume(test.someLibraryMethod());
  }

}
