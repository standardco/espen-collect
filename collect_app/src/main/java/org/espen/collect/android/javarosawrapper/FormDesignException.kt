/*
 * Copyright (C) 2020 ODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.espen.collect.android.javarosawrapper

import java.lang.Exception

/**
 * Thrown when there is an issue in form design. This type of error requires the form designer to
 * modify the form.
 */
open class FormDesignException(message: String) : Exception(message)

class RepeatsInFieldListException(message: String) : FormDesignException(message)
