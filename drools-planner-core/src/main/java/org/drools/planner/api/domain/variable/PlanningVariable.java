/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.planner.api.domain.variable;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Comparator;

import org.drools.planner.api.domain.entity.PlanningEntity;
import org.drools.planner.api.domain.variable.event.PlanningVariableListener;
import org.drools.planner.core.heuristic.selector.common.decorator.SelectionFilter;
import org.drools.planner.core.heuristic.selector.entity.decorator.NullValueUninitializedEntityFilter;
import org.drools.planner.core.score.director.ScoreDirector;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * Specifies that a bean property should be optimized by Drools Planner.
 * <p/>
 * It is specified on a getter of a java bean property of a {@link PlanningEntity} class.
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface PlanningVariable {

    /**
     * A nullable planning variable will automatically add the planning value null to the {@link ValueRange}.
     * <p/>
     * In repeated planning use cases, it's recommended to specify a {@link #uninitializedEntityFilter()}
     * for every nullable planning variable too.
     * <p/>
     * {@link #nullable()} true is not compatible with {#link #chained} true.
     * {@link #nullable()} true is not compatible with a primitive property type.
     * @return true if null is a valid value for this planning variable
     */
    boolean nullable() default false;

    /**
     * Construction heuristics only change (effectively reset) uninitialized planning variables.
     * An initialized planning variable is ignored by construction heuristics.
     * This is especially useful in repeated planning use cases,
     * in which starting from scratch would waste previous results and time.
     * <p/>
     * If no {@link #uninitializedEntityFilter} is specified,
     * the default considers an entity uninitialized for a variable if its value is null.
     * <p/>
     * The method {@link SelectionFilter#accept(ScoreDirector, Object)}
     * returns false if the selection entity is initialized for this variable
     * and it returns true if the selection entity is uninitialized for this variable
     * @return {@link NullUninitializedEntityFilter} when it is null (workaround for annotation limitation)
     */
    Class<? extends SelectionFilter> uninitializedEntityFilter()
            default NullUninitializedEntityFilter.class;

    interface NullUninitializedEntityFilter extends SelectionFilter {}

    /**
     * Allows a collection of planning values for this variable to be sorted by strength.
     * <p/>
     * Do not use together with {@link #strengthWeightFactoryClass()}.
     * @return {@link NullStrengthComparator} when it is null (workaround for annotation limitation)
     */
    Class<? extends Comparator> strengthComparatorClass()
            default NullStrengthComparator.class;

    interface NullStrengthComparator extends Comparator {}

    /**
     * Allows a collection of planning values for this variable  to be sorted by strength.
     * <p/>
     * Do not use together with {@link #strengthComparatorClass()}.
     * @return {@link NullStrengthWeightFactory} when it is null (workaround for annotation limitation)
     * @see PlanningValueStrengthWeightFactory
     */
    Class<? extends PlanningValueStrengthWeightFactory> strengthWeightFactoryClass()
            default NullStrengthWeightFactory.class;

    interface NullStrengthWeightFactory extends PlanningValueStrengthWeightFactory {}

    /**
     * In some use cases, such as Vehicle Routing, planning entities are chained.
     * A chained variable recursively points to a planning fact, which is called the anchor.
     * So either it points directly to the anchor (that planning fact)
     * or it points to another planning entity with the same planning variable (which recursively points to the anchor).
     * Chains always have exactly 1 anchor, thus they never loop and the tail is always open.
     * Chains never split into a tree: a anchor or planning entity has at most 1 trailing planning entity.
     * <p/>
     * When a chained planning entity changes position, then chain correction must happen:
     * <ul>
     *     <li>divert the chain link at the new position to go through the modified planning entity</li>
     *     <li>close the missing chain link at the old position</li>
     * </ul>
     * For example: Given A <- B <- C <- D <- X <- Y, when B moves between X and Y, pointing to X,
     * then Y is also changed to point to B
     * and C is also changed to point to A,
     * giving the result A <- C <- D <- X <- B <- Y.
     * <p/>
     * {@link #nullable()} true is not compatible with {#link #chained} true.
     * @return true if changes to this variable need to trigger chain correction
     */
    boolean chained() default false;

    @Deprecated // TODO This is probably a failed experiment
    Class<? extends PlanningVariableListener>[] listenerClasses() default {};

}
