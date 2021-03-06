/*
 * Copyright 2010 JBoss Inc
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

package org.drools.planner.core.move;

import java.util.Collection;

import org.drools.planner.core.score.director.ScoreDirector;
import org.drools.planner.core.testdata.util.CodeAssertable;

public class DummyMove implements Move, CodeAssertable {

    protected String code;

    public DummyMove() {
    }

    public DummyMove(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return true;
    }

    public Move createUndoMove(ScoreDirector scoreDirector) {
        return new DummyMove("undo " + code);
    }

    public void doMove(ScoreDirector scoreDirector) {
        // do nothing
    }

    public Collection<? extends Object> getPlanningEntities() {
        return null;
    }

    public Collection<? extends Object> getPlanningValues() {
        return null;
    }

    @Override
    public String toString() {
        return code;
    }

}
