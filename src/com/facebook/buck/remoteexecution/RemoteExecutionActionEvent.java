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

package com.facebook.buck.remoteexecution;

import com.facebook.buck.event.AbstractBuckEvent;
import com.facebook.buck.event.BuckEventBus;
import com.facebook.buck.event.EventKey;
import com.facebook.buck.event.LeafEvents;
import com.facebook.buck.event.WorkAdvanceEvent;
import com.facebook.buck.util.Scope;

/** Tracks events related to Remote Execution Actions. */
public abstract class RemoteExecutionActionEvent extends AbstractBuckEvent
    implements WorkAdvanceEvent {

  protected RemoteExecutionActionEvent(EventKey eventKey) {
    super(eventKey);
  }

  /** The current state of a Remote Execution Actions. */
  public enum State {
    DELETING_STALE_OUTPUTS("del"),
    COMPUTING_ACTION("comp"),
    UPLOADING_INPUTS("upl"),
    EXECUTING("exec"),
    MATERIALIZING_OUTPUTS("dwl"),
    ACTION_SUCCEEDED("suc"),
    ACTION_FAILED("fail");

    private final String abbreviateName;

    State(String abbreviateName) {
      this.abbreviateName = abbreviateName;
    }

    /** Abbreviated name for the current state. */
    public String getAbbreviateName() {
      return abbreviateName;
    }
  }

  /** Takes care of sending both Started and Finished events within a Scope. */
  public static Scope sendEvent(BuckEventBus eventBus, State state) {
    final Started startedEvent = new Started(state);
    eventBus.post(startedEvent);
    final Scope leftEventScope = LeafEvents.scope(eventBus, state.toString().toLowerCase());
    return () -> {
      leftEventScope.close();
      eventBus.post(new Finished(startedEvent));
    };
  }

  /** An action just moved into this state. */
  public static class Started extends RemoteExecutionActionEvent {
    private final State state;

    private Started(State state) {
      super(EventKey.unique());
      this.state = state;
    }

    public State getState() {
      return state;
    }

    @Override
    protected String getValueString() {
      return state.toString();
    }
  }

  /** An action just exited from this state. */
  public static class Finished extends RemoteExecutionActionEvent {

    private final Started startedEvent;

    private Finished(Started startedEvent) {
      super(startedEvent.getEventKey());
      this.startedEvent = startedEvent;
    }

    public Started getStartedEvent() {
      return startedEvent;
    }

    @Override
    protected String getValueString() {
      return startedEvent.getValueString();
    }
  }

  @Override
  public String getEventName() {
    return getClass().getSimpleName();
  }
}
