/*
 * Copyright 2012 JBoss Inc
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

package org.drools.planner.config.heuristic.selector.move.composite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.drools.planner.config.EnvironmentMode;
import org.drools.planner.config.heuristic.selector.common.SelectionOrder;
import org.drools.planner.config.heuristic.selector.move.MoveSelectorConfig;
import org.drools.planner.config.util.ConfigUtils;
import org.drools.planner.core.domain.solution.SolutionDescriptor;
import org.drools.planner.core.heuristic.selector.Selector;
import org.drools.planner.core.heuristic.selector.common.decorator.FixedSelectorProbabilityWeightFactory;
import org.drools.planner.core.heuristic.selector.common.SelectionCacheType;
import org.drools.planner.core.heuristic.selector.common.decorator.SelectionProbabilityWeightFactory;
import org.drools.planner.core.heuristic.selector.move.MoveSelector;
import org.drools.planner.core.heuristic.selector.move.composite.UnionMoveSelector;

@XStreamAlias("unionMoveSelector")
public class UnionMoveSelectorConfig extends MoveSelectorConfig {

    @XStreamImplicit()
    private List<MoveSelectorConfig> moveSelectorConfigList = null;

    private Class<? extends SelectionProbabilityWeightFactory> selectorProbabilityWeightFactoryClass = null;

    public List<MoveSelectorConfig> getMoveSelectorConfigList() {
        return moveSelectorConfigList;
    }

    public void setMoveSelectorConfigList(List<MoveSelectorConfig> moveSelectorConfigList) {
        this.moveSelectorConfigList = moveSelectorConfigList;
    }

    public Class<? extends SelectionProbabilityWeightFactory> getSelectorProbabilityWeightFactoryClass() {
        return selectorProbabilityWeightFactoryClass;
    }

    public void setSelectorProbabilityWeightFactoryClass(Class<? extends SelectionProbabilityWeightFactory> selectorProbabilityWeightFactoryClass) {
        this.selectorProbabilityWeightFactoryClass = selectorProbabilityWeightFactoryClass;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public MoveSelector buildBaseMoveSelector(EnvironmentMode environmentMode, SolutionDescriptor solutionDescriptor,
            SelectionCacheType minimumCacheType, SelectionOrder resolvedSelectionOrder) {
        List<MoveSelector> moveSelectorList = new ArrayList<MoveSelector>(moveSelectorConfigList.size());
        for (MoveSelectorConfig moveSelectorConfig : moveSelectorConfigList) {
            moveSelectorList.add(
                    moveSelectorConfig.buildMoveSelector(environmentMode, solutionDescriptor,
                            minimumCacheType, resolvedSelectionOrder));
        }

        boolean randomSelection = resolvedSelectionOrder == SelectionOrder.RANDOM;
        SelectionProbabilityWeightFactory selectorProbabilityWeightFactory;
        if (selectorProbabilityWeightFactoryClass != null) {
            if (resolvedSelectionOrder != SelectionOrder.RANDOM) {
                throw new IllegalArgumentException("The moveSelectorConfig (" + this
                        + ") with selectorProbabilityWeightFactoryClass ("
                        + selectorProbabilityWeightFactoryClass + ") has a non-random resolvedSelectionOrder ("
                        + resolvedSelectionOrder + ").");
            }
            selectorProbabilityWeightFactory = ConfigUtils.newInstance(this,
                    "selectorProbabilityWeightFactoryClass", selectorProbabilityWeightFactoryClass);
        } else if (randomSelection) {
            Map<Selector, Double> fixedProbabilityWeightMap = new HashMap<Selector, Double>(
                    moveSelectorConfigList.size());
            for (int i = 0; i < moveSelectorConfigList.size(); i++) {
                MoveSelectorConfig moveSelectorConfig = moveSelectorConfigList.get(i);
                MoveSelector moveSelector = moveSelectorList.get(i);
                Double fixedProbabilityWeight = moveSelectorConfig.getFixedProbabilityWeight();
                if (fixedProbabilityWeight == null) {
                    // Default to equal probability for each move type => unequal probability for each move instance
                    fixedProbabilityWeight = 1.0;
                }
                fixedProbabilityWeightMap.put(moveSelector, fixedProbabilityWeight);
            }
            selectorProbabilityWeightFactory = new FixedSelectorProbabilityWeightFactory(fixedProbabilityWeightMap);
        } else {
            selectorProbabilityWeightFactory = null;
        }
       return new UnionMoveSelector(moveSelectorList, randomSelection,
                selectorProbabilityWeightFactory);
    }

    public void inherit(UnionMoveSelectorConfig inheritedConfig) {
        super.inherit(inheritedConfig);
        moveSelectorConfigList = ConfigUtils.inheritMergeableListProperty(
                moveSelectorConfigList, inheritedConfig.getMoveSelectorConfigList());
        selectorProbabilityWeightFactoryClass = ConfigUtils.inheritOverwritableProperty(
                selectorProbabilityWeightFactoryClass, inheritedConfig.getSelectorProbabilityWeightFactoryClass());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + moveSelectorConfigList + ")";
    }

}
