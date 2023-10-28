package org.espen.collect.android.support;

import static org.espen.collect.android.utilities.FileUtils.getResourceAsStream;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.espen.collect.android.openrosa.CaseInsensitiveEmptyHeaders;
import org.espen.collect.android.openrosa.CaseInsensitiveHeaders;
import org.espen.collect.android.openrosa.HttpCredentialsInterface;
import org.espen.collect.android.openrosa.HttpGetResult;
import org.espen.collect.android.openrosa.HttpHeadResult;
import org.espen.collect.android.openrosa.HttpPostResult;
import org.espen.collect.android.openrosa.OpenRosaConstants;
import org.espen.collect.android.openrosa.OpenRosaHttpInterface;
import org.odk.collect.shared.strings.Md5;
import org.odk.collect.shared.strings.RandomString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class StubOpenRosaServer implements OpenRosaHttpInterface {

    private static final String HOST = "server.example.com";

    private final List<XFormItem> forms = new ArrayList<>();
    private String username;
    private String password;
    private boolean alwaysReturnError;
    private boolean fetchingFormsError;
    private boolean noHashInFormList;
    private boolean noHashPrefixInMediaFiles;
    private boolean randomHash;

    @NonNull
    @Override
    public HttpGetResult executeGetRequest(@NonNull URI uri, @Nullable String contentType, @Nullable HttpCredentialsInterface credentials) throws Exception {
        if (alwaysReturnError) {
            return new HttpGetResult(null, new HashMap<>(), "", 500);
        }

        if (!uri.getHost().equals(HOST)) {
            return new HttpGetResult(null, new HashMap<>(), "Trying to connect to incorrect server: " + uri.getHost(), 410);
        } else if (credentialsIncorrect(credentials)) {
            return new HttpGetResult(null, new HashMap<>(), "", 401);
        } else if (uri.getPath().equals(OpenRosaConstants.FORM_LIST)) {
            return new HttpGetResult(getFormListResponse(), getStandardHeaders(), "", 200);
        } else if (uri.getPath().equals("/form")) {
            if (fetchingFormsError) {
                return new HttpGetResult(null, new HashMap<>(), "", 500);
            }

            return new HttpGetResult(getFormResponse(uri), getStandardHeaders(), "", 200);
        } else if (uri.getPath().equals("/manifest")) {
            InputStream manifestResponse = getManifestResponse(uri);

            if (manifestResponse != null) {
                return new HttpGetResult(manifestResponse, getStandardHeaders(), "", 200);
            } else {
                return new HttpGetResult(null, new HashMap<>(), "", 404);
            }
        } else if (uri.getPath().equals("/mediaFile")) {
            return new HttpGetResult(getMediaFile(uri), new HashMap<>(), "", 200);
        } else {
            return new HttpGetResult(null, new HashMap<>(), "", 404);
        }
    }

    @NonNull
    @Override
    public HttpHeadResult executeHeadRequest(@NonNull URI uri, @Nullable HttpCredentialsInterface credentials) throws Exception {
        if (alwaysReturnError) {
            return new HttpHeadResult(500, new CaseInsensitiveEmptyHeaders());
        }

        if (!uri.getHost().equals(HOST)) {
            return new HttpHeadResult(410, new CaseInsensitiveEmptyHeaders());
        } else if (credentialsIncorrect(credentials)) {
            return new HttpHeadResult(401, new CaseInsensitiveEmptyHeaders());
        } else if (uri.getPath().equals(OpenRosaConstants.SUBMISSION)) {
            HashMap<String, String> headers = getStandardHeaders();
            headers.put("x-openrosa-accept-content-length", "10485760");

            return new HttpHeadResult(204, new MapHeaders(headers));
        } else {
            return new HttpHeadResult(404, new CaseInsensitiveEmptyHeaders());
        }
    }

    @NonNull
    @Override
    public HttpPostResult uploadSubmissionAndFiles(@NonNull File submissionFile, @NonNull List<File> fileList, @NonNull URI uri, @Nullable HttpCredentialsInterface credentials, @NonNull long contentLength) throws Exception {
        if (alwaysReturnError) {
            return new HttpPostResult("", 500, "");
        }

        if (!uri.getHost().equals(HOST)) {
            return new HttpPostResult("Trying to connect to incorrect server: " + uri.getHost(), 410, "");
        } else if (credentialsIncorrect(credentials)) {
            return new HttpPostResult("", 401, "");
        } else if (uri.getPath().equals(OpenRosaConstants.SUBMISSION)) {
            return new HttpPostResult("", 201, "");
        } else {
            return new HttpPostResult("", 404, "");
        }
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void addForm(String formLabel, String id, String version, String formXML) {
        forms.add(new XFormItem(formLabel, formXML, id, version));
    }

    public void addForm(String formLabel, String id, String version, String formXML, List<String> mediaFiles) {
        forms.add(new XFormItem(formLabel, formXML, id, version, mediaFiles));
    }

    public void removeForm(String formLabel) {
        forms.removeIf(xFormItem -> xFormItem.getFormLabel().equals(formLabel));
    }

    public void alwaysReturnError() {
        alwaysReturnError = true;
    }

    public void errorOnFetchingForms() {
        fetchingFormsError = true;
    }

    public void removeHashInFormList() {
        noHashInFormList = true;
    }

    public void removeMediaFileHashPrefix() {
        noHashPrefixInMediaFiles = true;
    }

    public void returnRandomMediaFileHash() {
        randomHash = true;
    }

    public String getURL() {
        return "https://" + HOST;
    }

    public String getHostName() {
        return HOST;
    }

    private boolean credentialsIncorrect(HttpCredentialsInterface credentials) {
        if (username == null && password == null) {
            return false;
        } else {
            if (credentials == null) {
                return true;
            } else {
                return !credentials.getUsername().equals(username) || !credentials.getPassword().equals(password);
            }
        }
    }

    @NotNull
    private HashMap<String, String> getStandardHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("x-openrosa-version", "1.0");
        return headers;
    }

    @NotNull
    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    private InputStream getFormListResponse() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("<?xml version='1.0' encoding='UTF-8' ?>\n")
                .append("<xforms xmlns=\"http://openrosa.org/xforms/xformsList\">\n");

        for (int i = 0; i < forms.size(); i++) {
            XFormItem form = forms.get(i);

            StringBuilder xform = stringBuilder
                    .append("<xform>\n")
                    .append("<formID>" + form.getID() + "</formID>\n")
                    .append("<name>" + form.getFormLabel() + "</name>\n")
                    .append("<version>" + form.getVersion() + "</version>\n");

            if (!noHashInFormList) {
                String hash = Md5.getMd5Hash(getFormXML(String.valueOf(i)));
                xform.append("<hash>md5:" + hash + "</hash>\n");
            }

            xform.append("<downloadUrl>" + getURL() + "/form?formId=" + i + "</downloadUrl>\n");

            if (!form.getMediaFiles().isEmpty()) {
                xform.append("<manifestUrl>" + getURL() + "/manifest?formId=" + i + "</manifestUrl>\n");
            }

            stringBuilder.append("</xform>\n");
        }

        stringBuilder.append("</xforms>");
        return new ByteArrayInputStream(stringBuilder.toString().getBytes());
    }

    @NotNull
    private InputStream getFormResponse(@NonNull URI uri) throws IOException {
        String formID = uri.getQuery().split("=")[1];
        return getFormXML(formID);
    }

    @SuppressWarnings("PMD.ConsecutiveLiteralAppends")
    private InputStream getManifestResponse(@NonNull URI uri) throws IOException {
        String formID = uri.getQuery().split("=")[1];
        XFormItem xformItem = forms.get(Integer.parseInt(formID));

        if (xformItem.getMediaFiles().isEmpty()) {
            return null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("<?xml version='1.0' encoding='UTF-8' ?>\n")
                    .append("<manifest xmlns=\"http://openrosa.org/xforms/xformsManifest\">\n");

            for (String mediaFile : xformItem.getMediaFiles()) {
                String mediaFileHash;

                if (randomHash) {
                    mediaFileHash = RandomString.randomString(8);
                } else {
                    mediaFileHash = Md5.getMd5Hash(getResourceAsStream("media/" + mediaFile));
                }

                stringBuilder
                        .append("<mediaFile>")
                        .append("<filename>" + mediaFile + "</filename>\n");

                if (noHashPrefixInMediaFiles) {
                    stringBuilder.append("<hash>" + mediaFileHash + " </hash>\n");
                } else {
                    stringBuilder.append("<hash>md5:" + mediaFileHash + " </hash>\n");
                }

                stringBuilder
                        .append("<downloadUrl>" + getURL() + "/mediaFile?name=" + mediaFile + "</downloadUrl>\n")
                        .append("</mediaFile>\n");
            }

            stringBuilder.append("</manifest>");
            return new ByteArrayInputStream(stringBuilder.toString().getBytes());
        }
    }

    @NotNull
    private InputStream getFormXML(String formID) throws IOException {
        String xmlPath = forms.get(Integer.parseInt(formID)).getFormXML();
        return getResourceAsStream("forms/" + xmlPath);
    }

    @NotNull
    private InputStream getMediaFile(URI uri) throws IOException {
        String mediaFileName = uri.getQuery().split("=")[1];
        return getResourceAsStream("media/" + mediaFileName);
    }

    private static class XFormItem {

        private final String formLabel;
        private final String formXML;
        private final String id;
        private final String version;
        private final List<String> mediaFiles;

        XFormItem(String formLabel, String formXML, String id, String version) {
            this(formLabel, formXML, id, version, emptyList());
        }

        XFormItem(String formLabel, String formXML, String id, String version, List<String> mediaFiles) {
            this.formLabel = formLabel;
            this.formXML = formXML;
            this.id = id;
            this.version = version;
            this.mediaFiles = mediaFiles;
        }

        public String getFormLabel() {
            return formLabel;
        }

        public String getFormXML() {
            return formXML;
        }

        public String getVersion() {
            return version;
        }

        public String getID() {
            return id;
        }

        public List<String> getMediaFiles() {
            return mediaFiles;
        }
    }

    private static class MapHeaders implements CaseInsensitiveHeaders {

        private final Map<String, String> headers;

        MapHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        @javax.annotation.Nullable
        @Override
        public Set<String> getHeaders() {
            return headers.keySet();
        }

        @Override
        public boolean containsHeader(String header) {
            return headers.containsKey(header.toLowerCase(Locale.ENGLISH));
        }

        @javax.annotation.Nullable
        @Override
        public String getAnyValue(String header) {
            return headers.get(header.toLowerCase(Locale.ENGLISH));
        }

        @javax.annotation.Nullable
        @Override
        public List<String> getValues(String header) {
            return asList(headers.get(header.toLowerCase(Locale.ENGLISH)));
        }
    }
}
