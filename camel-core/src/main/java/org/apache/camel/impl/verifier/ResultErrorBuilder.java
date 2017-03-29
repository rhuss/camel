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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.camel.ComponentVerifier;
import org.apache.camel.ComponentVerifier.VerificationError;
import org.apache.camel.util.ObjectHelper;

public final class ResultErrorBuilder {
    private VerificationError.Code code;
    private String description;
    private Set<String> parameters;
    private Map<VerificationError.Attribute, Object> attributes;

    public ResultErrorBuilder() {
    }

    // **********************************
    // Accessors
    // **********************************

    public ResultErrorBuilder code(VerificationError.Code code) {
        this.code = code;
        return this;
    }

    public ResultErrorBuilder code(String code) {
        code(asCode(code));
        return this;
    }

    public ResultErrorBuilder description(String description) {
        this.description = description;
        return this;
    }

    public ResultErrorBuilder parameterKey(String parameter) {
        if (parameter != null) {
            if (this.parameters == null) {
                this.parameters = new HashSet<>();
            }

            this.parameters.add(parameter);
        }
        return this;
    }

    public ResultErrorBuilder parameterKeys(Collection<String> parameterList) {
        if (parameterList != null) {
            parameterList.forEach(this::parameterKey);
        }

        return this;
    }

    public ResultErrorBuilder detail(String key, Object value) {
        detail(asAttribute(key), value);
        return this;
    }

    public ResultErrorBuilder detail(VerificationError.Attribute key, Object value) {
        if (value != null) {
            if (this.attributes == null) {
                this.attributes = new HashMap<>();
            }

            this.attributes.put(key, value);
        }
        return this;
    }

    public <T> ResultErrorBuilder detail(String key, Supplier<Optional<T>> supplier) {
        detail(asAttribute(key), supplier);
        return this;
    }

    public <T> ResultErrorBuilder detail(VerificationError.Attribute key, Supplier<Optional<T>> supplier) {
        supplier.get().ifPresent(value -> detail(key, value));
        return this;
    }

    // **********************************
    // Build
    // **********************************

    public VerificationError build() {
        return new DefaultResultVerificationError(
            code,
            description,
            parameters != null ? Collections.unmodifiableSet(parameters) : Collections.emptySet(),
            attributes != null ? Collections.unmodifiableMap(attributes) : Collections.emptyMap()
        );
    }

    // **********************************
    // Helpers
    // **********************************

    public static ResultErrorBuilder withCode(VerificationError.Code code) {
        return new ResultErrorBuilder().code(code);
    }

    public static ResultErrorBuilder withCode(String code) {
        return new ResultErrorBuilder().code(code);
    }

    public static ResultErrorBuilder withHttpCode(int code) {
        return withCode(convertHttpCodeToErrorCode(code))
            .detail(VerificationError.StandardAttribute.TYPE, ComponentVerifier.ERROR_TYPE_HTTP)
            .detail(VerificationError.StandardAttribute.HTTP_CODE, code);
    }

    public static ResultErrorBuilder withHttpCodeAndText(int code, String text) {
        return withCodeAndDescription(convertHttpCodeToErrorCode(code), text)
            .detail(VerificationError.StandardAttribute.TYPE, ComponentVerifier.ERROR_TYPE_HTTP)
            .detail(VerificationError.StandardAttribute.HTTP_CODE, code)
            .detail(VerificationError.StandardAttribute.HTTP_BODY, text);
    }

    private static VerificationError.StandardCode convertHttpCodeToErrorCode(int code) {
        return code >= 400 && code < 500 ? VerificationError.StandardCode.AUTHENTICATION : VerificationError.StandardCode.GENERIC;
    }

    public static ResultErrorBuilder withCodeAndDescription(VerificationError.Code code, String description) {
        return new ResultErrorBuilder().code(code).description(description);
    }

    public static ResultErrorBuilder withUnsupportedScope(String scope) {
        return new ResultErrorBuilder()
            .code(VerificationError.StandardCode.UNSUPPORTED_SCOPE)
            .description("Unsupported scope: " + scope);
    }

    public static ResultErrorBuilder withException(Exception exception) {
        return new ResultErrorBuilder()
            .code(VerificationError.StandardCode.EXCEPTION)
            .description(exception.getMessage())
            .detail(VerificationError.StandardAttribute.TYPE, ComponentVerifier.ERROR_TYPE_EXCEPTION)
            .detail(VerificationError.StandardAttribute.EXCEPTION_INSTANCE, exception)
            .detail(VerificationError.StandardAttribute.EXCEPTION_CLASS, exception.getClass().getName());
    }

    public static ResultErrorBuilder withMissingOption(String optionName) {
        return new ResultErrorBuilder()
            .code(VerificationError.StandardCode.MISSING_OPTION)
            .description(optionName + " should be set")
            .parameterKey(optionName);
    }

    public static ResultErrorBuilder withUnknownOption(String optionName) {
        return new ResultErrorBuilder()
            .code(VerificationError.StandardCode.UNKNOWN_OPTION)
            .description("Unknown option " + optionName)
            .parameterKey(optionName);
    }

    public static ResultErrorBuilder withIllegalOption(String optionName) {
        return new ResultErrorBuilder()
            .code(VerificationError.StandardCode.ILLEGAL_OPTION)
            .description("Illegal option " + optionName)
            .parameterKey(optionName);
    }

    public static ResultErrorBuilder withIllegalOption(String optionName, String optionValue) {
        return ObjectHelper.isNotEmpty(optionValue)
            ? new ResultErrorBuilder()
                .code(VerificationError.StandardCode.ILLEGAL_OPTION_VALUE)
                .description(optionName + " has wrong value (" + optionValue + ")")
                .parameterKey(optionName)
            : withIllegalOption(optionName);
    }

    // **********************************
    // Create error keys
    // **********************************

    public static ComponentVerifier.VerificationError.Code asCode(String code) {
        return new ErrorCode(code);
    }

    public static ComponentVerifier.VerificationError.Attribute asAttribute(String attribute) {
        return new ErrorAttribute(attribute);
    }

    public static class ErrorCode implements ComponentVerifier.VerificationError.Code {

        private String name;

        public ErrorCode(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name.toUpperCase();
        }
    }

    public static class ErrorAttribute implements ComponentVerifier.VerificationError.Attribute {

        private String name;

        public ErrorAttribute(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name.toUpperCase();
        }
    }
}
