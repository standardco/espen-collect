package org.espen.collect.android.instrumented.tasks;

import org.junit.Test;
import org.espen.collect.android.formmanagement.ServerFormsDetailsFetcher;
import org.espen.collect.android.tasks.DownloadFormListTask;
import org.espen.collect.android.utilities.WebCredentialsUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DownloadFormListTaskTest {

    @Test
    public void whenAlternateCredentialsAreSet_shouldServerFormsDetailsFetcherBeUpdated() {
        ServerFormsDetailsFetcher serverFormsDetailsFetcher = mock(ServerFormsDetailsFetcher.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        DownloadFormListTask task = new DownloadFormListTask(serverFormsDetailsFetcher);
        task.setAlternateCredentials(webCredentialsUtils, "https://test-server.com", "testUser", "testPassword");
        verify(serverFormsDetailsFetcher).updateUrl("https://test-server.com");
        verify(serverFormsDetailsFetcher).updateCredentials(webCredentialsUtils);
    }

    @Test
    public void whenAlternateCredentialsDoNotContainUrl_shouldNotUrlBeUpdated() {
        ServerFormsDetailsFetcher serverFormsDetailsFetcher = mock(ServerFormsDetailsFetcher.class);
        WebCredentialsUtils webCredentialsUtils = mock(WebCredentialsUtils.class);

        DownloadFormListTask task = new DownloadFormListTask(serverFormsDetailsFetcher);

        task.setAlternateCredentials(webCredentialsUtils, null, "testUser", "testPassword");
        verify(serverFormsDetailsFetcher, never()).updateUrl(anyString());
        verify(serverFormsDetailsFetcher).updateCredentials(webCredentialsUtils);

        task.setAlternateCredentials(webCredentialsUtils, "", "testUser", "testPassword");
        verify(serverFormsDetailsFetcher, never()).updateUrl(anyString());
        verify(serverFormsDetailsFetcher, times(2)).updateCredentials(webCredentialsUtils);
    }
}
