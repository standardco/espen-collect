/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.espen.collect.android.exception;

/**
 * Author: Meletis Margaritis
 * Date: 01/05/13
 * Time: 10:57
 */
public class ExternalDataException extends RuntimeException {

    public ExternalDataException(String detailMessage) {
        super(detailMessage);
    }

    public ExternalDataException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
