/*
 * Copyright 2014 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.services.refactoring.model.index.terms;

import org.uberfire.commons.validation.PortablePreconditions;

public class FieldIndexTerm implements IndexTerm {

    public static final String TERM = "field";

    private final String fieldName;

    public FieldIndexTerm( final String fieldName ) {
        this.fieldName = PortablePreconditions.checkNotNull( "fieldName",
                                                             fieldName );
    }

    @Override
    public String getTerm() {
        return TERM;
    }

    @Override
    public String getValue() {
        return fieldName;
    }

}