/*
 * Copyright 2014-present Facebook, Inc.
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

package com.facebook.buck.rules.coercer;

import com.facebook.buck.io.ProjectFilesystem;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSortedMap;

import java.nio.file.Path;
import java.util.Map;

public class SortedMapTypeCoercer<K extends Comparable<K>, V>
    implements TypeCoercer<ImmutableSortedMap<K, V>> {
  private final TypeCoercer<K> keyTypeCoercer;
  private final TypeCoercer<V> valueTypeCoercer;

  SortedMapTypeCoercer(TypeCoercer<K> keyTypeCoercer, TypeCoercer<V> valueTypeCoercer) {
    this.keyTypeCoercer = keyTypeCoercer;
    this.valueTypeCoercer = valueTypeCoercer;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<ImmutableSortedMap<K, V>> getOutputClass() {
    return (Class<ImmutableSortedMap<K, V>>) (Class<?>) ImmutableSortedMap.class;
  }

  @Override
  public boolean hasElementClass(Class<?>... types) {
    return keyTypeCoercer.hasElementClass(types) || valueTypeCoercer.hasElementClass(types);
  }

  @Override
  public Optional<ImmutableSortedMap<K, V>> getOptionalValue() {
    return Optional.of(ImmutableSortedMap.<K, V>of());
  }

  @Override
  public void traverse(ImmutableSortedMap<K, V> object, TypeCoercer.Traversal traversal) {
    traversal.traverse(object);
    for (Map.Entry<K, V> element : object.entrySet()) {
      keyTypeCoercer.traverse(element.getKey(), traversal);
      valueTypeCoercer.traverse(element.getValue(), traversal);
    }
  }

  @Override
  public ImmutableSortedMap<K, V> coerce(
      Function<Optional<String>, Path> cellRoots,
      ProjectFilesystem filesystem,
      Path pathRelativeToProjectRoot,
      Object object)
      throws CoerceFailedException {
    if (object instanceof Map) {
      ImmutableSortedMap.Builder<K, V> builder = ImmutableSortedMap.naturalOrder();

      for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
        K key = keyTypeCoercer.coerce(
            cellRoots,
            filesystem,
            pathRelativeToProjectRoot,
            entry.getKey());
        V value = valueTypeCoercer.coerce(
            cellRoots,
            filesystem,
            pathRelativeToProjectRoot,
            entry.getValue());
        builder.put(key, value);
      }

      return builder.build();
    } else {
      throw CoerceFailedException.simple(object, getOutputClass());
    }
  }
}
