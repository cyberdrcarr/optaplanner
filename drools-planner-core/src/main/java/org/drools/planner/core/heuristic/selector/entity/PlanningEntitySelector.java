package org.drools.planner.core.heuristic.selector.entity;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.drools.planner.core.domain.entity.PlanningEntityDescriptor;
import org.drools.planner.core.domain.entity.PlanningEntitySorter;
import org.drools.planner.core.phase.AbstractSolverPhaseScope;
import org.drools.planner.core.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.drools.planner.core.score.director.ScoreDirector;

/**
 * Determines the order in which the planning entities of 1 planning entity class are selected for an algorithm
 */
@Deprecated
public class PlanningEntitySelector extends SolverPhaseLifecycleListenerAdapter
        implements Iterable<Object> {

    private PlanningEntityDescriptor planningEntityDescriptor;

    private PlanningEntitySelectionOrder selectionOrder = PlanningEntitySelectionOrder.ORIGINAL;
    private boolean resetInitializedPlanningEntities = false;

    private List<Object> selectedPlanningEntityList = null;

    public PlanningEntitySelector(PlanningEntityDescriptor planningEntityDescriptor) {
        this.planningEntityDescriptor = planningEntityDescriptor;
    }

    public void setSelectionOrder(PlanningEntitySelectionOrder selectionOrder) {
        this.selectionOrder = selectionOrder;
    }

    public void setResetInitializedPlanningEntities(boolean resetInitializedPlanningEntities) {
        this.resetInitializedPlanningEntities = resetInitializedPlanningEntities;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public void phaseStarted(AbstractSolverPhaseScope phaseScope) {
        validate();
        initSelectedPlanningEntityList(phaseScope);
    }

    private void validate() {
        if (selectionOrder == PlanningEntitySelectionOrder.DECREASING_DIFFICULTY) {
            PlanningEntitySorter planningEntitySorter = planningEntityDescriptor.getPlanningEntitySorter();
            if (!planningEntitySorter.isSortDifficultySupported()) {
                throw new IllegalStateException("The selectionOrder (" + selectionOrder
                        + ") can not be used on a PlanningEntity ("
                        + planningEntityDescriptor.getPlanningEntityClass().getName()
                        + ") that has no support for difficulty sorting. Check the @PlanningEntity annotation.");
            }
        }
    }

    private void initSelectedPlanningEntityList(AbstractSolverPhaseScope phaseScope) {
        List<Object> workingPlanningEntityList = phaseScope.getWorkingPlanningEntityList();
        for (Iterator<Object> it = workingPlanningEntityList.iterator(); it.hasNext(); ) {
            Object planningEntity = it.next();
            if (!planningEntityDescriptor.getPlanningEntityClass().isInstance(planningEntity)) {
                it.remove();
            } else if (planningEntityDescriptor.isInitialized(planningEntity)) {
                if (resetInitializedPlanningEntities) { // TODO this should be extracted to a custom solver phase before this phase
                    ScoreDirector scoreDirector = phaseScope.getScoreDirector();
                    scoreDirector.beforeEntityRemoved(planningEntity);
                    planningEntityDescriptor.uninitialize(planningEntity);
                    scoreDirector.afterEntityRemoved(planningEntity);
                } else {
                    // Do not plan the initialized planning entity
                    it.remove();
                }
            }
        }
        switch (selectionOrder) {
            case ORIGINAL:
                break;
            case RANDOM:
                Collections.shuffle(workingPlanningEntityList, phaseScope.getWorkingRandom());
                break;
            case DECREASING_DIFFICULTY:
                PlanningEntitySorter planningEntitySorter = planningEntityDescriptor.getPlanningEntitySorter();
                planningEntitySorter.sortDifficultyDescending(
                        phaseScope.getWorkingSolution(), workingPlanningEntityList);
                break;
            default:
                throw new IllegalStateException("The selectionOrder (" + selectionOrder + ") is not implemented");
        }
        selectedPlanningEntityList = workingPlanningEntityList;
    }

    @Override
    public void phaseEnded(AbstractSolverPhaseScope phaseScope) {
        selectedPlanningEntityList = null;
    }

    public Iterator<Object> iterator() {
        return selectedPlanningEntityList.iterator();
    }

}
