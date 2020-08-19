/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.impl.constructionheuristic.placer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.optaplanner.core.config.constructionheuristic.placer.QueuedEntityPlacerConfig;
import org.optaplanner.core.config.heuristic.selector.common.SelectionCacheType;
import org.optaplanner.core.config.heuristic.selector.common.SelectionOrder;
import org.optaplanner.core.config.heuristic.selector.entity.EntitySelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.MoveSelectorConfig;
import org.optaplanner.core.config.heuristic.selector.move.composite.CartesianProductMoveSelectorConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.HeuristicConfigPolicy;
import org.optaplanner.core.impl.heuristic.selector.entity.EntitySelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelector;
import org.optaplanner.core.impl.heuristic.selector.move.MoveSelectorFactory;

public class QueuedEntityPlacerFactory extends AbstractEntityPlacerFactory<QueuedEntityPlacerConfig> {

    public QueuedEntityPlacerFactory(QueuedEntityPlacerConfig placerConfig) {
        super(placerConfig);
    }

    @Override
    public QueuedEntityPlacer buildEntityPlacer(HeuristicConfigPolicy configPolicy) {
        EntitySelectorConfig entitySelectorConfig_ = buildEntitySelectorConfig(configPolicy);
        EntitySelector entitySelector = entitySelectorConfig_.buildEntitySelector(configPolicy,
                SelectionCacheType.PHASE, SelectionOrder.ORIGINAL);

        List<MoveSelectorConfig> moveSelectorConfigList_;
        if (ConfigUtils.isEmptyCollection(placerConfig.getMoveSelectorConfigList())) {
            EntityDescriptor entityDescriptor = entitySelector.getEntityDescriptor();
            Collection<GenuineVariableDescriptor> variableDescriptors = entityDescriptor.getGenuineVariableDescriptors();
            List<MoveSelectorConfig> subMoveSelectorConfigList = new ArrayList<>(
                    variableDescriptors.size());
            for (GenuineVariableDescriptor variableDescriptor : variableDescriptors) {
                subMoveSelectorConfigList.add(buildChangeMoveSelectorConfig(
                        configPolicy, entitySelectorConfig_.getId(), variableDescriptor));
            }
            MoveSelectorConfig subMoveSelectorConfig;
            if (subMoveSelectorConfigList.size() > 1) {
                // Default to cartesian product (not a queue) of planning variables.
                subMoveSelectorConfig = new CartesianProductMoveSelectorConfig(subMoveSelectorConfigList);
            } else {
                subMoveSelectorConfig = subMoveSelectorConfigList.get(0);
            }
            moveSelectorConfigList_ = Collections.singletonList(subMoveSelectorConfig);
        } else {
            moveSelectorConfigList_ = placerConfig.getMoveSelectorConfigList();
        }
        List<MoveSelector> moveSelectorList = new ArrayList<>(moveSelectorConfigList_.size());
        for (MoveSelectorConfig moveSelectorConfig : moveSelectorConfigList_) {
            MoveSelector moveSelector = MoveSelectorFactory.create(moveSelectorConfig)
                    .buildMoveSelector(configPolicy, SelectionCacheType.JUST_IN_TIME, SelectionOrder.ORIGINAL);
            moveSelectorList.add(moveSelector);
        }
        return new QueuedEntityPlacer(entitySelector, moveSelectorList);
    }

    public EntitySelectorConfig buildEntitySelectorConfig(HeuristicConfigPolicy configPolicy) {
        EntitySelectorConfig entitySelectorConfig_;
        if (placerConfig.getEntitySelectorConfig() == null) {
            entitySelectorConfig_ = new EntitySelectorConfig();
            EntityDescriptor entityDescriptor = configPolicy.getSolutionDescriptor().deduceEntityDescriptor(null);
            Class<?> entityClass = entityDescriptor.getEntityClass();
            entitySelectorConfig_.setId(entityClass.getName());
            entitySelectorConfig_.setEntityClass(entityClass);
            if (EntitySelectorConfig.hasSorter(configPolicy.getEntitySorterManner(), entityDescriptor)) {
                entitySelectorConfig_.setCacheType(SelectionCacheType.PHASE);
                entitySelectorConfig_.setSelectionOrder(SelectionOrder.SORTED);
                entitySelectorConfig_.setSorterManner(configPolicy.getEntitySorterManner());
            }
        } else {
            entitySelectorConfig_ = placerConfig.getEntitySelectorConfig();
        }
        if (entitySelectorConfig_.getCacheType() != null
                && entitySelectorConfig_.getCacheType().compareTo(SelectionCacheType.PHASE) < 0) {
            throw new IllegalArgumentException("The queuedEntityPlacer (" + placerConfig
                    + ") cannot have an entitySelectorConfig (" + entitySelectorConfig_
                    + ") with a cacheType (" + entitySelectorConfig_.getCacheType()
                    + ") lower than " + SelectionCacheType.PHASE + ".");
        }
        return entitySelectorConfig_;
    }
}
