/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.impl.verifier;

import java.util.Map;
import java.util.Set;

import org.apache.camel.ComponentVerifier;
import org.apache.camel.ComponentVerifier.VerificationError;

public class DefaultResultVerificationError implements VerificationError {
    private final Code code;
    private final String description;
    private final Set<String> parameters;
    private final Map<Attribute, Object> attributes;

    public DefaultResultVerificationError(Code code, String description, Set<String> parameters, Map<Attribute, Object> attributes) {
        this.code = code;
        this.description = description;
        this.parameters = parameters;
        this.attributes = attributes;
    }

    @Override
    public Code getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Set<String> getParameterKeys() {
        return parameters;
    }

    @Override
    public Map<Attribute, Object> getDetails() {
        return attributes;
    }

    @Override
    public String toString() {
        return "DefaultResultError{"
            + "code='" + code + '\''
            + ", description='" + description + '\''
            + ", parameters=" + parameters
            + ", attributes=" + attributes
            + '}';
    }
}
