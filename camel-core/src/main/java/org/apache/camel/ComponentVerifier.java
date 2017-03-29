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
package org.apache.camel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.camel.util.ObjectHelper;

/**
 * Defines the interface used to validate component/endpoint parameters.
 */
public interface ComponentVerifier {

    String ERROR_TYPE_EXCEPTION = "exception";
    String ERROR_TYPE_HTTP = "http";

    enum Scope {
        PARAMETERS,
        CONNECTIVITY;

        private static final Scope[] VALUES = values();

        public static Scope fromString(String scope) {
            for (Scope value : VALUES) {
                if (ObjectHelper.equal(scope, value.name(), true)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("Unknown scope <" + scope + ">");
        }
    }

    /**
     * Represent an error
     */
    interface VerificationError extends Serializable {
        /**
         * @return the error code
         */
        Code getCode();

        /**
         * @return the error description (if available)
         */
        String getDescription();

        /**
         * @return the input parameter name which caused this error
         */
        Set<String> getParameterKeys();

        /**
         * @return a number of key/value pair with additional information related to the verfication.
         */
        Map<Attribute, Object> getDetails();

        // Typed error codes
        interface Code {
            String name();
        }

        /**
         * Standard set of available code
         */
        enum StandardCode implements Code {
            AUTHENTICATION,
            EXCEPTION,
            INTERNAL,
            MISSING_OPTION,
            UNKNOWN_OPTION,
            ILLEGAL_OPTION,
            ILLEGAL_OPTION_GROUP_COMBINATION,
            ILLEGAL_OPTION_VALUE,
            INCOMPLETE_OPTION_GROUP,
            UNSUPPORTED,
            UNSUPPORTED_SCOPE,
            GENERIC
        }

        /**
         * Attribute for detailed error messages
         */
        interface Attribute {
            String name();
        }

        /**
         * Standard set of available attributes
         */
        enum StandardAttribute implements Attribute {
            TYPE,
            EXCEPTION_INSTANCE,
            EXCEPTION_CLASS,
            HTTP_CODE,
            HTTP_BODY,
            GROUP_NAME,
            GROUP_OPTIONS
        }
    }

    /**
     * Represent a validation Result.
     */
    interface Result extends Serializable {
        enum Status {
            OK,
            ERROR,
            UNSUPPORTED
        }

        /**
         * @return the scope against which the parameters have been validated.
         */
        Scope getScope();

        /**
         * @return the status
         */
        Status getStatus();

        /**
         * @return a list of errors
         */
        List<VerificationError> getErrors();
    }

    /**
     * Validate the given parameters against the provided scope.
     *
     * <p>
     * The supported scopes are:
     * <ul>
     *   <li>PARAMETERS: to validate that all the mandatory options are provided and syntactically correct.
     *   <li>CONNECTIVITY: to validate that the given options (i.e. credentials, addresses) are correct.
     * </ul>
     *
     * @param scope the scope of the validation
     * @param parameters the parameters to validate
     * @return the validation result
     */
    Result verify(Scope scope, Map<String, Object> parameters);
}
