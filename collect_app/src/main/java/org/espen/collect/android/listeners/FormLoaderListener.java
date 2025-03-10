/*
 * Copyright (C) 2009 University of Washington
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

package org.espen.collect.android.listeners;

import org.javarosa.core.model.FormDef;
import org.espen.collect.android.tasks.FormLoaderTask;
import org.espen.collect.android.tasks.ProgressNotifier;

/**
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public interface FormLoaderListener extends ProgressNotifier {
    void loadingComplete(FormLoaderTask task, FormDef fd, String warningMsg);

    void loadingError(String errorMsg);
}
