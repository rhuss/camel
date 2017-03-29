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

import org.apache.camel.impl.verifier.ResultErrorBuilder;
import org.apache.camel.util.ObjectHelper;
import org.omg.CORBA.INTERNAL;

/**
 * Defines the interface used for validating component/endpoint parameters. The central method of this
 * interface is {@link #verify(Scope, Map)} which takes a scope and a set of parameters which should be verified.
 * <p/>
 * The return value is a {@link Result} of the verification
 *
 */
public interface ComponentVerifier {

    /**
     * Verify the given parameters against a provided scope.
     *
     * <p>
     * The supported scopes are:
     * <ul>
     *   <li><strong>{@link Scope#PARAMETERS}</strong>: to validate that all the mandatory options are provided and syntactically correct.</li>
     *   <li><strong>{@link Scope#CONNECTIVITY}</strong>: to validate that the given options (i.e. credentials, addresses) are correct. Verifying with this
     *       scope typically implies reaching out to the backend via some sort of network connection.</li>
     * </ul>
     *
     * @param scope the scope of the verification
     * @param parameters the parameters to verify which are interpreted individually by each component verifier
     * @return the verification result
     */
    Result verify(Scope scope, Map<String, Object> parameters);

    /**
     * The scope defines how the parameters should be verified.
     */
    enum Scope {
        /**
         * Only validate the parameters for their <em>syntactic</em> soundness. Verifications in this scope should
         * be as fast as possible
         */
        PARAMETERS,

        /**
         * Reach out to the backend and verify that a connection can be established. This means, if the verification
         * in this scope succeeds, then it can safely be assumed that the component can be used.
         */
        CONNECTIVITY;

        private static final Scope[] VALUES = values();

        /**
         * Get an instance of this scope from a string representation
         *
         * @param scope the scope as string, which can be in any case
         * @return the scope enum represented by this string
         */
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
     * Object encapsulating the result of a validation
     */
    interface Result extends Serializable {

        /**
         * Status of the verification
         */
        enum Status {
            /**
             * Verification succeeded
             */
            OK,
            /**
             * Error occured during the verification
             */
            ERROR,
            /**
             * Verification is not supported. This can depend on the given scope.
             */
            UNSUPPORTED
        }

        /**
         * Scope of the verification. This is the scope given to the call to {@link #verify(Scope, Map)}  and
         * can be used for correlation.
         *
         * @return the scope against which the parameters have been validated.
         */
        Scope getScope();

        /**
         * Result of the validation as status. This should be the first datum to check after a verification
         * happened.
         *
         * @return the status
         */
        Status getStatus();

        /**
         * Collection of errors happened for the verification. This list is empty (but non null) if the verification
         * succeeded.
         *
         * @return a list of errors. Can be empty when verification was successful
         */
        List<VerificationError> getErrors();
    }

    // =============================================================================================

    /**
     * This interface represents a detailed error in case when the verification fails.
     */
    interface VerificationError extends Serializable {

        /**
         * The overall error code, which can be either a {@link StandardCode} or a custom code. It is
         * recommended to stick to the predefined standard codes
         *
         * @return the general error code.
         */
        Code getCode();

        /**
         * A human readable description of the error in plain english
         *
         * @return the error description (if available)
         */
        String getDescription();

        /**
         * A set of input parameter names which fails the verification. These are keys to the parameter provided
         * to {@link #verify(Scope, Map)}.
         *
         * @return the parameter names which are malformed and caused the failure of the validation
         */
        Set<String> getParameterKeys();

        /**
         * Details about the failed verification. The keys can be either predefined values
         * ({@link ExceptionAttribute}, {@link HttpAttribute}, {@link GroupAttribute}) or it can be free-form
         * custom keys specific to a component. The standard attributes are defined as enums in all uppercase (with
         * underscore as separator), custom attributes are supposed to be in all lower case (also with underscores
         * as separators)
         *
         * @return a number of key/value pair with additional information related to the verification.
         */
        Map<Attribute, Object> getDetails();

        /**
         * Interface defining an error code. This is implemented by the {@link StandardCode} but also
         * own code can be generated by implementing this interface. This is best done via {@link ResultErrorBuilder#asCode(String)}
         * If possible, the standard codes should be reused
         */
        interface Code {
            /**
             * Name of the code. All uppercase for standard codes, all lower case for custom code. Separator is underscore.
             * @return code name
             */
            String name();
        }

        /**
         * Standard set of error codes
         */
        enum StandardCode implements Code {
            /**
             * Authentication failed
             */
            AUTHENTICATION,
            /**
             * An exception occurred
             */
            EXCEPTION,
            /**
             * Internal error while performing the verification
             */
            INTERNAL,
            /**
             * A mandatory parameter is missing
             */
            MISSING_PARAMETER,
            /**
             * A given parameter is not known to the component
             */
            UNKNOWN_PARAMETER,
            /**
             * A given parameter is illegal
             */
            ILLEGAL_PARAMETER,
            /**
             * A combination of parameters is illegal. See {@link VerificationError#getParameterKeys()} for the set
             * of affected parameters
             */
            ILLEGAL_PARAMETER_GROUP_COMBINATION,
            /**
             * A parameter <em>value</em> is not valid
             */
            ILLEGAL_PARAMETER_VALUE,
            /**
             * A group of parameters is not complete in order to be valid
             */
            INCOMPLETE_PARAMETER_GROUP,
            /**
             * The verification is not supported
             */
            UNSUPPORTED,
            /**
             * The requested {@link Scope} is not supported
             */
            UNSUPPORTED_SCOPE,
            /**
             * Generic error which is explained in more details with {@link VerificationError#getDetails()}
             */
            GENERIC
        }

        /**
         * Interface defining an attribute which is a key for the detailed error messages. This is implemented by several
         * standard enums like {@link ExceptionAttribute}, {@link HttpAttribute} or {@link GroupAttribute} but can also
         * implemented for component specific details. This is best done via {@link ResultErrorBuilder#asAttribute(String)}
         * or using one of the other builder method in this error builder (like {@link ResultErrorBuilder#detail(String, Object)}
         *
         * With respecting to name, the same rules as for {@link Code} apply: Standard attributes are all upper case with _
         * as separators, whereas custom attributes are lower case with underscore separators.
         */
        interface Attribute {
            /**
             * @return Name of the attribute
             */
            String name();
        }

        /**
         * Attributes for details about an exception that was raised
         */
        enum ExceptionAttribute implements Attribute {
            /**
             * The exception object that has been thrown. Note that this can be a complex
             * object and can cause large content when e.g. serialized as JSON
             */
            EXCEPTION_INSTANCE,
            /**
             * The exception class
             */
            EXCEPTION_CLASS
        }

        /**
         * HTTP related error details
         */
        enum HttpAttribute implements Attribute {
            /**
             * The erroneous HTTP code that occurred
             */
            HTTP_CODE,
            /**
             * HTTP response's body
             */
            HTTP_TEXT,
            /**
             * If given as details, specifies that a redirect happened and the
             * content of this detail is the redirect URL
             */
            HTTP_REDIRECT
        }

        /**
         * Group related details
         */
        enum GroupAttribute implements Attribute {
            /**
             * Group name
             */
            GROUP_NAME,
            /**
             * Options for the group
             */
            GROUP_OPTIONS
        }
    }
}
