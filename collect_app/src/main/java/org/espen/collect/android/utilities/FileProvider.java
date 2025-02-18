package org.espen.collect.android.utilities;

import android.net.Uri;

public interface FileProvider {
    Uri getURIForFile(String filePath);
}
