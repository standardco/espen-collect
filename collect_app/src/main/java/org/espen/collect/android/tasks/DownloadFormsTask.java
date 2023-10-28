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

package org.espen.collect.android.tasks;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;
import static java.util.Collections.emptyMap;

import android.os.AsyncTask;

import org.espen.collect.android.application.EspenCollect;
import org.espen.collect.android.formmanagement.FormDownloadException;
import org.espen.collect.android.formmanagement.FormDownloader;
import org.espen.collect.android.formmanagement.FormsDataService;
import org.espen.collect.android.formmanagement.ServerFormDetails;
import org.espen.collect.android.listeners.DownloadFormsTaskListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Background task for downloading a given list of forms. We assume right now that the forms are
 * coming from the same server that presented the form list, but theoretically that won't always be
 * true.
 *
 * @author msundt
 * @author carlhartung
 *
 * @deprecated Server form should be downloaded using {@link FormsDataService}
 */
public class DownloadFormsTask extends
        AsyncTask<ArrayList<ServerFormDetails>, String, Map<ServerFormDetails, FormDownloadException>> {

    private final FormDownloader formDownloader;
    private DownloadFormsTaskListener stateListener;

    public DownloadFormsTask(FormDownloader formDownloader) {
        this.formDownloader = formDownloader;
    }

    @Override
    protected Map<ServerFormDetails, FormDownloadException> doInBackground(ArrayList<ServerFormDetails>... values) {
        HashMap<ServerFormDetails, FormDownloadException> results = new HashMap<>();

        int index = 1;
        for (ServerFormDetails serverFormDetails : values[0]) {
            try {
                String currentFormNumber = String.valueOf(index);
                String totalForms = String.valueOf(values[0].size());
                publishProgress(serverFormDetails.getFormName(), currentFormNumber, totalForms);

                formDownloader.downloadForm(serverFormDetails, count -> {
                    String message = getLocalizedString(EspenCollect.getInstance(), org.odk.collect.strings.R.string.form_download_progress,
                            serverFormDetails.getFormName(),
                            String.valueOf(count),
                            String.valueOf(serverFormDetails.getManifest().getMediaFiles().size())
                    );

                    publishProgress(message, currentFormNumber, totalForms);
                }, this::isCancelled);

                results.put(serverFormDetails, null);
            } catch (FormDownloadException.DownloadingInterrupted e) {
                return emptyMap();
            } catch (FormDownloadException e) {
                results.put(serverFormDetails, e);
            }

            index++;
        }

        return results;
    }

    @Override
    protected void onCancelled(Map<ServerFormDetails, FormDownloadException> formDetailsStringHashMap) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formsDownloadingCancelled();
            }
        }
    }

    @Override
    protected void onPostExecute(Map<ServerFormDetails, FormDownloadException> value) {
        synchronized (this) {
            if (stateListener != null) {
                stateListener.formsDownloadingComplete(value);
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (stateListener != null) {
                // update progress and total
                stateListener.progressUpdate(values[0],
                        Integer.parseInt(values[1]),
                        Integer.parseInt(values[2]));
            }
        }

    }

    public void setDownloaderListener(DownloadFormsTaskListener sl) {
        synchronized (this) {
            stateListener = sl;
        }
    }
}
