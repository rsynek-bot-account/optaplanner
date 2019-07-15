/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.score.stream.common;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntBiFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongBiFunction;
import java.util.function.ToLongFunction;

import org.optaplanner.core.api.score.stream.bi.BiConstraintCollector;
import org.optaplanner.core.api.score.stream.uni.UniConstraintCollector;

/**
 *
 */
public final class ConstraintCollectors {

    // ************************************************************************
    // count
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, Integer> count() {
        return new UniConstraintCollector<>(
                () -> new int[1],
                (resultContainer, a) -> {
                    resultContainer[0]++;
                    return (() -> resultContainer[0]--);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A> UniConstraintCollector<A, ?, Long> countLong() {
        return new UniConstraintCollector<>(
                () -> new long[1],
                (resultContainer, a) -> {
                    resultContainer[0]++;
                    return (() -> resultContainer[0]--);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Integer> countBi() {
        return new BiConstraintCollector<>(
                () -> new int[1],
                (resultContainer, a, b) -> {
                    resultContainer[0]++;
                    return (() -> resultContainer[0]--);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Long> countLongBi() {
        return new BiConstraintCollector<>(
                () -> new long[1],
                (resultContainer, a, b) -> {
                    resultContainer[0]++;
                    return (() -> resultContainer[0]--);
                },
                resultContainer -> resultContainer[0]);
    }

    // ************************************************************************
    // countDistinct
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, Integer> countDistinct(Function<A, ?> groupValueMapping) {
        class CountDistinctResultContainer {
            int count = 0;
            Map<Object, int[]> objectCountMap = new HashMap<>();
        }
        return new UniConstraintCollector<>(
                CountDistinctResultContainer::new,
                (resultContainer, a) -> {
                    Object value = groupValueMapping.apply(a);
                    int[] objectCount = resultContainer.objectCountMap.computeIfAbsent(value, k -> new int[1]);
                    if (objectCount[0] == 0) {
                        resultContainer.count++;
                    }
                    objectCount[0]++;
                    return (() -> {
                        int[] objectCount2 = resultContainer.objectCountMap.get(value);
                        if (objectCount2 == null) {
                            throw new IllegalStateException("Impossible state: the value (" + value
                                    + ") of A (" + a + ") is removed more times than it was added.");
                        }
                        objectCount2[0]--;
                        if (objectCount2[0] == 0) {
                            resultContainer.objectCountMap.remove(value);
                            resultContainer.count--;
                        }
                    });
                },
                resultContainer -> resultContainer.count);
    }

    public static <A> UniConstraintCollector<A, ?, Long> countDistinctLong(Function<A, ?> groupValueMapping) {
        class CountDistinctResultContainer {
            long count = 0L;
            Map<Object, long[]> objectCountMap = new HashMap<>();
        }
        return new UniConstraintCollector<>(
                CountDistinctResultContainer::new,
                (resultContainer, a) -> {
                    Object value = groupValueMapping.apply(a);
                    long[] objectCount = resultContainer.objectCountMap.computeIfAbsent(value, k -> new long[1]);
                    if (objectCount[0] == 0L) {
                        resultContainer.count++;
                    }
                    objectCount[0]++;
                    return (() -> {
                        long[] objectCount2 = resultContainer.objectCountMap.get(value);
                        if (objectCount2 == null) {
                            throw new IllegalStateException("Impossible state: the value (" + value
                                    + ") of A (" + a + ") is removed more times than it was added.");
                        }
                        objectCount2[0]--;
                        if (objectCount2[0] == 0L) {
                            resultContainer.objectCountMap.remove(value);
                            resultContainer.count--;
                        }
                    });
                },
                resultContainer -> resultContainer.count);
    }

    // ************************************************************************
    // sum
    // ************************************************************************

    public static <A> UniConstraintCollector<A, ?, Integer> sum(ToIntFunction<? super A> groupValueMapping) {
        return new UniConstraintCollector<>(
                () -> new int[1],
                (resultContainer, a) -> {
                    int value = groupValueMapping.applyAsInt(a);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A> UniConstraintCollector<A, ?, Long> sum(ToLongFunction<? super A> groupValueMapping) {
        return new UniConstraintCollector<>(
                () -> new long[1],
                (resultContainer, a) -> {
                    long value = groupValueMapping.applyAsLong(a);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Integer> sum(ToIntBiFunction<? super A, ? super B> groupValueMapping) {
        return new BiConstraintCollector<>(
                () -> new int[1],
                (resultContainer, a, b) -> {
                    int value = groupValueMapping.applyAsInt(a, b);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }

    public static <A, B> BiConstraintCollector<A, B, ?, Long> sum(ToLongBiFunction<? super A, ? super B> groupValueMapping) {
        return new BiConstraintCollector<>(
                () -> new long[1],
                (resultContainer, a, b) -> {
                    long value = groupValueMapping.applyAsLong(a, b);
                    resultContainer[0] += value;
                    return (() -> resultContainer[0] -= value);
                },
                resultContainer -> resultContainer[0]);
    }


    private ConstraintCollectors() {
    }

}
