package org.espen.collect.android.formentry.media;

import android.content.Context;

import org.espen.collect.android.audio.AudioHelper;

public interface AudioHelperFactory {

    AudioHelper create(Context context);
}
