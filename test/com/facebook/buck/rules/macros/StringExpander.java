/*
 * Copyright 2015-present Facebook, Inc.
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

package com.facebook.buck.rules.macros;

import com.facebook.buck.io.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.rules.BuildRule;
import com.facebook.buck.rules.BuildRuleResolver;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.nio.file.Path;

class StringExpander implements MacroExpander {

  private final String toReturn;

  public StringExpander(String toReturn) {
    this.toReturn = toReturn;
  }

  @Override
  public String expand(
      BuildTarget target,
      Function<Optional<String>, Path> cellNames,
      BuildRuleResolver resolver,
      ProjectFilesystem filesystem,
      String input)
      throws MacroException {
    return toReturn;
  }

  @Override
  public ImmutableList<BuildRule> extractAdditionalBuildTimeDeps(
      BuildTarget target,
      Function<Optional<String>, Path> cellNames,
      BuildRuleResolver resolver,
      String input)
      throws MacroException {
    return ImmutableList.of();
  }

  @Override
  public ImmutableList<BuildTarget> extractParseTimeDeps(
      BuildTarget target,
      Function<Optional<String>, Path> cellNames,
      String input)
      throws MacroException {
    return ImmutableList.of();
  }

  @Override
  public Object extractRuleKeyAppendables(
      BuildTarget target,
      Function<Optional<String>, Path> cellNames,
      BuildRuleResolver resolver,
      String input)
      throws MacroException {
    return new Object();
  }

}
