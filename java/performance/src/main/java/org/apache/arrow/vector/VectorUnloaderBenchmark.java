/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.arrow.vector;

import java.util.concurrent.TimeUnit;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.ipc.message.ArrowRecordBatch;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarks for {@link VectorUnloader}.
 */
@State(Scope.Benchmark)
public class VectorUnloaderBenchmark {
  // checkstyle:off: MissingJavadocMethod

  private static final int ALLOCATOR_CAPACITY = 1024 * 1024;

  private static final int VECTOR_COUNT = 10;

  private BufferAllocator allocator;

  private VarCharVector [] vectors;

  private VectorUnloader unloader;

  private ArrowRecordBatch recordBatch;

  /**
   * Setup benchmarks.
   */
  @Setup(Level.Trial)
  public void prepare() {
    allocator = new RootAllocator(ALLOCATOR_CAPACITY);
  }

  @Setup(Level.Invocation)
  public void prepareInvoke() {
    vectors = new VarCharVector[VECTOR_COUNT];
    for (int i = 0; i < VECTOR_COUNT; i++) {
      vectors[i] = new VarCharVector("vector", allocator);
      vectors[i].allocateNew(100, 10);
    }

    unloader = new VectorUnloader(VectorSchemaRoot.of(vectors));
  }

  @TearDown(Level.Invocation)
  public void tearDownInvoke() {
    if (recordBatch != null) {
      recordBatch.close();
    }
    for (int i = 0; i < VECTOR_COUNT; i++) {
      vectors[i].close();
    }
  }

  /**
   * Tear down benchmarks.
   */
  @TearDown(Level.Trial)
  public void tearDown() {
    allocator.close();
  }

  @Benchmark
  @BenchmarkMode(Mode.AverageTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public void unloadBenchmark() {
    recordBatch = unloader.getRecordBatch();
  }

  public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
            .include(VectorUnloaderBenchmark.class.getSimpleName())
            .forks(1)
            .build();

    new Runner(opt).run();
  }
  // checkstyle:on: MissingJavadocMethod
}
