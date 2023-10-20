/*
 * Copyright (C) 2017 University of Washington
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

package org.odk.collect.android.utilities;

import static org.odk.collect.strings.localization.LocalizedApplicationKt.getLocalizedString;
import static java.util.Arrays.asList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.CharMatcher;

import org.apache.commons.io.IOUtils;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.actions.setgeopoint.SetGeopointActionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xform.util.XFormUtils;
import org.odk.collect.android.application.EspenCollect;
import org.odk.collect.async.OngoingWorkListener;
import org.odk.collect.shared.strings.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import timber.log.Timber;

/**
 * Static methods used for common file operations.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public final class FileUtils {

    public static final String FORMID = "formid";
    public static final String VERSION = "version"; // arbitrary string in OpenRosa 1.0
    public static final String TITLE = "title";
    public static final String SUBMISSIONURI = "submission";
    public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey";
    public static final String AUTO_DELETE = "autoDelete";
    public static final String AUTO_SEND = "autoSend";
    public static final String GEOMETRY_XPATH = "geometryXpath";

    /** Suffix for the form media directory. */
    public static final String MEDIA_SUFFIX = "-media";

    /** Filename of the last-saved instance data. */
    public static final String LAST_SAVED_FILENAME = "last-saved.xml";

    /** Valid XML stub that can be parsed without error. */
    public static final String STUB_XML = "<?xml version='1.0' ?><stub />";

    private FileUtils() {
    }

    public static void saveAnswerFileFromUri(Uri uri, File destFile, Context context) {
        try (InputStream fileInputStream = context.getContentResolver().openInputStream(uri);
             OutputStream fileOutputStream = new FileOutputStream(destFile)) {
            IOUtils.copy(fileInputStream, fileOutputStream);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    public static File createDestinationMediaFile(String fileLocation, String fileExtension) {
        return new File(fileLocation
                + File.separator
                + System.currentTimeMillis()
                + "."
                + fileExtension);
    }

    public static boolean createFolder(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }

    public static String copyFile(File sourceFile, File destFile) {
        if (sourceFile.exists()) {
            String errorMessage = actualCopy(sourceFile, destFile);
            if (errorMessage != null) {
                try {
                    Thread.sleep(500);
                    Timber.e(new Error("Retrying to copy the file after 500ms: " + sourceFile.getAbsolutePath()));
                    errorMessage = actualCopy(sourceFile, destFile);
                } catch (InterruptedException e) {
                    Timber.i(e);
                }
            }
            return errorMessage;
        } else {
            String msg = "Source file does not exist: " + sourceFile.getAbsolutePath();
            Timber.e(new Error(msg));
            return msg;
        }
    }

    private static String actualCopy(File sourceFile, File destFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel src = null;
        FileChannel dst = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            src = fileInputStream.getChannel();
            fileOutputStream = new FileOutputStream(destFile);
            dst = fileOutputStream.getChannel();
            dst.transferFrom(src, 0, src.size());
            dst.force(true);
            return null;
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                Timber.e(e, "FileNotFoundException while copying file");
            } else if (e instanceof IOException) {
                Timber.e(e, "IOException while copying file");
            } else {
                Timber.e(e, "Exception while copying file");
            }
            return e.getMessage();
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(src);
            IOUtils.closeQuietly(dst);
        }
    }

    /**
     * Given a form definition file, return a map containing form metadata. The form ID is required
     * by the specification and will always be included. Title and version are optionally included.
     * If the form definition contains a submission block, any or all of submission URI, base 64 RSA
     * public key, auto-delete and auto-send may be included.
     */
    public static HashMap<String, String> getMetadataFromFormDefinition(File formDefinitionXml) throws XFormParser.ParseException {
        FormDef formDef = XFormUtils.getFormFromFormXml(formDefinitionXml.getAbsolutePath(), "jr://file/" + LAST_SAVED_FILENAME);

        final HashMap<String, String> fields = new HashMap<>();

        fields.put(TITLE, formDef.getTitle());
        fields.put(FORMID, formDef.getMainInstance().getRoot().getAttributeValue(null, "id"));
        String version = formDef.getMainInstance().getRoot().getAttributeValue(null, "version");
        if (version != null && StringUtils.isBlank(version)) {
            version = null;
        }
        fields.put(VERSION, version);

        if (formDef.getSubmissionProfile() != null) {
            fields.put(SUBMISSIONURI, formDef.getSubmissionProfile().getAction());

            final String key = formDef.getSubmissionProfile().getAttribute("base64RsaPublicKey");
            if (key != null && key.trim().length() > 0) {
                fields.put(BASE64_RSA_PUBLIC_KEY, key.trim());
            }

            fields.put(AUTO_DELETE, formDef.getSubmissionProfile().getAttribute("auto-delete"));
            fields.put(AUTO_SEND, formDef.getSubmissionProfile().getAttribute("auto-send"));
        }

        fields.put(GEOMETRY_XPATH, getOverallFirstGeoPoint(formDef));
        return fields;
    }

    /**
     * Returns an XPath path representing the first geopoint of this form definition or null if the
     * definition does not contain any field of type geopoint.
     *
     * The first geopoint is either of:
     *      (1) the first geopoint in the body that is not in a repeat
     *      (2) if the form has a setgeopoint action, the first geopoint in the instance that occurs
     *          before (1) or (1) if there is no geopoint defined before it in the instance.
     */
    private static String getOverallFirstGeoPoint(FormDef formDef) {
        TreeReference firstTopLevelBodyGeoPoint = getFirstToplevelBodyGeoPoint(formDef);

        if (!formDef.hasAction(SetGeopointActionHandler.ELEMENT_NAME)) {
            return firstTopLevelBodyGeoPoint == null ? null : firstTopLevelBodyGeoPoint.toString(false);
        } else {
            return getInstanceGeoPointBefore(firstTopLevelBodyGeoPoint, formDef.getMainInstance().getRoot());
        }
    }

    /**
     * Returns the reference of the first geopoint in the body that is not in a repeat.
     */
    private static TreeReference getFirstToplevelBodyGeoPoint(FormDef formDef) {
        if (formDef.getChildren().size() == 0) {
            return null;
        } else {
            return getFirstTopLevelBodyGeoPoint(formDef, formDef.getMainInstance());
        }
    }

    /**
     * Returns the reference of the first child of the given element that is of type geopoint and
     * is not contained in a repeat.
     */
    private static TreeReference getFirstTopLevelBodyGeoPoint(IFormElement element, FormInstance primaryInstance) {
        if (element instanceof QuestionDef) {
            QuestionDef question = (QuestionDef) element;
            int dataType = primaryInstance.resolveReference((TreeReference) element.getBind().getReference()).getDataType();

            if (dataType == Constants.DATATYPE_GEOPOINT) {
                return (TreeReference) question.getBind().getReference();
            }
        } else if (element instanceof FormDef || element instanceof GroupDef) {
            if (element instanceof GroupDef && ((GroupDef) element).getRepeat()) {
                return null;
            } else {
                for (IFormElement child : element.getChildren()) {
                    // perform recursive depth-first search
                    TreeReference geoRef = getFirstTopLevelBodyGeoPoint(child, primaryInstance);
                    if (geoRef != null) {
                        return geoRef;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns the XPath path for the first geopoint in the primary instance that is before the given
     * reference and not in a repeat.
     */
    private static String getInstanceGeoPointBefore(TreeReference firstBodyGeoPoint, TreeElement element) {
        if (element.getRef().equals(firstBodyGeoPoint)) {
            return null;
        } else if (element.getDataType() == Constants.DATATYPE_GEOPOINT) {
            return element.getRef().toString(false);
        } else if (element.hasChildren()) {
            Set<TreeElement> childrenToAvoid = new HashSet<>();

            for (int i = 0; i < element.getNumChildren(); i++) {
                if (element.getChildAt(i).getMultiplicity() == TreeReference.INDEX_TEMPLATE) {
                    childrenToAvoid.addAll(element.getChildrenWithName(element.getChildAt(i).getName()));
                } else if (!childrenToAvoid.contains(element.getChildAt(i))) {
                    String geoPath = getInstanceGeoPointBefore(firstBodyGeoPoint, element.getChildAt(i));
                    if (geoPath != null) {
                        return geoPath;
                    }
                }
            }
        }

        return null;
    }

    public static void deleteAndReport(File file) {
        if (file != null && file.exists()) {
            // remove garbage
            if (!file.delete()) {
                Timber.d("%s will be deleted upon exit.", file.getAbsolutePath());
                file.deleteOnExit();
            } else {
                Timber.d("%s has been deleted.", file.getAbsolutePath());
            }
        }
    }

    public static String getFormBasename(File formXml) {
        return getFormBasename(formXml.getName());
    }

    public static String getFormBasename(String formFilePath) {
        return formFilePath.substring(0, formFilePath.lastIndexOf('.'));
    }

    public static String constructMediaPath(String formFilePath) {
        return getFormBasename(formFilePath) + MEDIA_SUFFIX;
    }

    public static File getFormMediaDir(File formXml) {
        final String formFileName = getFormBasename(formXml);
        return new File(formXml.getParent(), formFileName + MEDIA_SUFFIX);
    }

    public static String getFormBasenameFromMediaFolder(File mediaFolder) {
        /*
         * TODO (from commit 37e3467): Apparently the form name is neither
         * in the formController nor the formDef. In fact, it doesn't seem to
         * be saved into any object in JavaRosa. However, the mediaFolder
         * has the substring of the file name in it, so we extract the file name
         * from here. Awkward...
         */
        return mediaFolder.getName().split(MEDIA_SUFFIX)[0];
    }

    public static File getLastSavedFile(File formXml) {
        return new File(getFormMediaDir(formXml), LAST_SAVED_FILENAME);
    }

    public static String getLastSavedPath(File mediaFolder) {
        return mediaFolder.getAbsolutePath() + File.separator + LAST_SAVED_FILENAME;
    }

    /**
     * Returns the path to the last-saved file for this form,
     * creating a valid stub if it doesn't yet exist.
     */
    public static String getOrCreateLastSavedSrc(File formXml) {
        File lastSavedFile = getLastSavedFile(formXml);

        if (!lastSavedFile.exists()) {
            write(lastSavedFile, STUB_XML.getBytes(StandardCharsets.UTF_8));
        }

        return "jr://file/" + LAST_SAVED_FILENAME;
    }

    /**
     * @param mediaDir the media folder
     */
    public static void checkMediaPath(File mediaDir) {
        if (mediaDir.exists() && mediaDir.isFile()) {
            Timber.e(new Error("The media folder is already there and it is a FILE!! We will need to delete it and create a folder instead"));
            boolean deleted = mediaDir.delete();
            if (!deleted) {
                throw new RuntimeException(
                        getLocalizedString(EspenCollect.getInstance(), org.odk.collect.strings.R.string.fs_delete_media_path_if_file_error,
                                mediaDir.getAbsolutePath()));
            }
        }

        // the directory case
        boolean createdOrExisted = createFolder(mediaDir.getAbsolutePath());
        if (!createdOrExisted) {
            throw new RuntimeException(
                    getLocalizedString(EspenCollect.getInstance(), org.odk.collect.strings.R.string.fs_create_media_folder_error,
                            mediaDir.getAbsolutePath()));
        }
    }

    public static void purgeMediaPath(String mediaPath) {
        File tempMediaFolder = new File(mediaPath);
        File[] tempMediaFiles = tempMediaFolder.listFiles();

        if (tempMediaFiles != null) {
            for (File tempMediaFile : tempMediaFiles) {
                deleteAndReport(tempMediaFile);
            }
        }

        deleteAndReport(tempMediaFolder);
    }

    public static byte[] read(File file) {
        byte[] bytes = new byte[(int) file.length()];
        try (InputStream is = new FileInputStream(file)) {
            is.read(bytes);
        } catch (IOException e) {
            Timber.e(e);
        }
        return bytes;
    }

    public static void write(File file, byte[] data) {
        // Make sure the directory path to this file exists.
        file.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    /** Sorts file paths as if sorting the path components and extensions lexicographically. */
    public static int comparePaths(String a, String b) {
        // Regular string compareTo() is incorrect, because it will sort "/" and "."
        // after other punctuation (e.g. "foo/bar" will sort AFTER "foo-2/bar" and
        // "pic.jpg" will sort AFTER "pic-2.jpg").  Replacing these delimiters with
        // '\u0000' and '\u0001' causes paths to sort correctly (assuming the paths
        // don't already contain '\u0000' or '\u0001').  This is a bit of a hack,
        // but it's a lot simpler and faster than comparing components one by one.
        String sortKeyA = a.replace('/', '\u0000').replace('.', '\u0001');
        String sortKeyB = b.replace('/', '\u0000').replace('.', '\u0001');
        return sortKeyA.compareTo(sortKeyB);
    }

    public static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    public static void grantFilePermissions(Intent intent, Uri uri, Context context) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    @SuppressWarnings("PMD.DoNotHardCodeSDCard")
    public static String expandAndroidStoragePath(String path) {
        if (path != null && path.startsWith("/storage/emulated/0/")) {
            return "/sdcard/" + path.substring("/storage/emulated/0/".length());
        }

        return path;
    }

    public static String getMimeType(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath());
        String mimeType = extension != null ? MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) : null;

        if (mimeType == null || mimeType.isEmpty()) {
            FileNameMap fileNameMap = URLConnection.getFileNameMap();
            mimeType = fileNameMap.getContentTypeFor(file.getAbsolutePath());
        }

        if (mimeType == null || mimeType.isEmpty()) {
            mimeType = URLConnection.guessContentTypeFromName(file.getName());
        }

        return mimeType;
    }

    public static List<File> listFiles(File file) {
        if (file != null && file.exists()) {
            return asList(file.listFiles());
        } else {
            return new ArrayList<>();
        }
    }

    public static String getFilenameError(String filename) {
        String possiblyRestricted = "?:\"*|/\\<>\u0000";
        boolean containsAt = filename.contains("@");
        boolean containsNonAscii = CharMatcher.ascii().matchesAllOf(filename);
        boolean containsPossiblyRestricted = CharMatcher.anyOf(possiblyRestricted).matchesAnyOf(possiblyRestricted);
        return "Problem with project name file. Contains @: " + containsAt + ", Contains non-ascii: " + containsNonAscii + ", Contains restricted: " + containsPossiblyRestricted;
    }

    /**
     * Common routine to take a downloaded document save the contents in the file
     * 'file'.
     * <p>
     * The file is saved into a temp folder and is moved to the final place if everything
     * is okay, so that garbage is not left over on cancel.
     */
    public static void interuptablyWriteFile(InputStream inputStream, File destinationFile, File tempDir, OngoingWorkListener listener)
            throws IOException, InterruptedException {

        File tempFile = File.createTempFile(
                destinationFile.getName(),
                ".tempDownload",
                tempDir
        );

        // WiFi network connections can be renegotiated during a large download sequence.
        // This will cause intermittent download failures.  Silently retry once after each
        // failure.  Only if there are two consecutive failures do we abort.
        boolean success = false;
        int attemptCount = 0;
        final int maxAttemptCount = 2;
        while (!success && ++attemptCount <= maxAttemptCount) {
            // write connection to file
            InputStream is = null;
            OutputStream os = null;

            try {
                is = inputStream;
                os = new FileOutputStream(tempFile);

                byte[] buf = new byte[4096];
                int len;
                while ((len = is.read(buf)) > 0 && (listener == null || !listener.isCancelled())) {
                    os.write(buf, 0, len);
                }
                os.flush();
                success = true;

            } catch (Exception e) {
                Timber.e(e);
                // silently retry unless this is the last attempt,
                // in which case we rethrow the exception.

                FileUtils.deleteAndReport(tempFile);

                if (attemptCount == maxAttemptCount) {
                    throw e;
                }
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
                if (is != null) {
                    try {
                        // ensure stream is consumed...
                        final long count = 1024L;
                        while (is.skip(count) == count) {
                            // skipping to the end of the http entity
                        }
                    } catch (Exception e) {
                        // no-op
                    }
                    try {
                        is.close();
                    } catch (Exception e) {
                        Timber.w(e);
                    }
                }
            }

            if (listener != null && listener.isCancelled()) {
                FileUtils.deleteAndReport(tempFile);
                throw new InterruptedException();
            }
        }

        Timber.d("Completed downloading of %s. It will be moved to the proper path...", tempFile.getAbsolutePath());

        FileUtils.deleteAndReport(destinationFile);

        String errorMessage = FileUtils.copyFile(tempFile, destinationFile);

        if (destinationFile.exists()) {
            Timber.d("Copied %s over %s", tempFile.getAbsolutePath(), destinationFile.getAbsolutePath());
            FileUtils.deleteAndReport(tempFile);
        } else {
            String msg = EspenCollect.getInstance().getString(org.odk.collect.strings.R.string.fs_file_copy_error,
                    tempFile.getAbsolutePath(), destinationFile.getAbsolutePath(), errorMessage);
            throw new RuntimeException(msg);
        }
    }

    public static void copyFileFromAssets(Context context, String fileDestPath, String fileSourcePath) throws IOException {
        copyStreamToPath(getAssetAsStream(context, fileSourcePath), fileDestPath);
    }

    public static File copyFileFromResources(String fileSourcePath, File fileDest) throws IOException {
        copyStreamToPath(getResourceAsStream(fileSourcePath), fileDest.getAbsolutePath());
        return fileDest;
    }

    public static void copyFileFromResources(String fileSourcePath, String fileDestPath) throws IOException {
        copyStreamToPath(getResourceAsStream(fileSourcePath), fileDestPath);
    }

    @NonNull
    public static InputStream getAssetAsStream(Context context, String fileSourcePath) throws IOException {
        return context.getAssets().open(fileSourcePath);
    }

    @Nullable
    public static InputStream getResourceAsStream(String fileSourcePath) {
        return FileUtils.class.getResourceAsStream("/" + fileSourcePath);
    }

    private static void copyStreamToPath(InputStream inputStream, String fileDestPath) throws IOException {
        try (InputStream input = inputStream;
             OutputStream output = new FileOutputStream(fileDestPath)) {
            IOUtils.copy(input, output);
        }
    }
}
