package org.espen.collect.androidshared.ui.multiclicksafe

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import org.espen.collect.androidshared.ui.multiclicksafe.MultiClickGuard.allowClick

class MultiClickSafeImageButton : AppCompatImageButton {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    )

    override fun performClick(): Boolean {
        return allowClick() && super.performClick()
    }
}
