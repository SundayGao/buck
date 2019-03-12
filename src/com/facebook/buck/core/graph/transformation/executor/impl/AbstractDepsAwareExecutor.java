/*
 * Copyright 2018-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.core.graph.transformation.executor.impl;

import com.facebook.buck.core.graph.transformation.executor.DepsAwareExecutor;
import com.facebook.buck.core.graph.transformation.executor.impl.AbstractDepsAwareTask.TaskStatus;
import com.facebook.buck.core.util.log.Logger;
import com.google.common.base.Verify;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

abstract class AbstractDepsAwareExecutor<T, TaskType extends AbstractDepsAwareTask<T, TaskType>>
    implements DepsAwareExecutor<T, TaskType> {

  private static final Logger LOG = Logger.get(AbstractDepsAwareExecutor.class);
  protected final BlockingDeque<TaskType> workQueue;
  protected final Future<?>[] workers;
  private volatile boolean isShutdown = false;

  protected AbstractDepsAwareExecutor(BlockingDeque<TaskType> workQueue, Future<?>[] workers) {
    this.workQueue = workQueue;
    this.workers = workers;
  }

  protected static void runWorker(AbstractDepsAwareWorker<?> worker) {
    try {
      worker.loopForever();
    } catch (InterruptedException e) {
      LOG.info("Worker was interrupted");
    }
  }

  @Override
  public void close() {
    isShutdown = true;
    for (Future<?> worker : workers) {
      worker.cancel(true);
    }
  }

  @Override
  public boolean isShutdown() {
    return isShutdown;
  }

  @Override
  public Future<T> submit(TaskType task) {
    if (isShutdown) {
      throw new RejectedExecutionException("Executor has already been shutdown");
    }
    if (!task.compareAndSetStatus(TaskStatus.NOT_SCHEDULED, TaskStatus.SCHEDULED)) {
      return task.getResultFuture();
    }
    if (!workQueue.offer(task)) {
      Verify.verify(task.compareAndSetStatus(TaskStatus.SCHEDULED, TaskStatus.NOT_SCHEDULED));
      throw new RejectedExecutionException("Failed to schedule new work. Queue is full");
    }
    return task.getResultFuture();
  }

  @Override
  public ImmutableList<Future<T>> submitAll(Collection<TaskType> tasks) {
    ImmutableList.Builder<Future<T>> futures = ImmutableList.builderWithExpectedSize(tasks.size());
    for (TaskType w : tasks) {
      futures.add(submit(w));
    }
    return futures.build();
  }
}
