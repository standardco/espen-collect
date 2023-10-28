package org.espen.collect.android.support

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import org.espen.collect.android.utilities.ScreenContext

class WidgetTestActivity : FragmentActivity(), org.espen.collect.android.utilities.ScreenContext {
    @JvmField
    val viewsRegisterForContextMenu = mutableListOf<View>()

    override fun getActivity(): FragmentActivity {
        return this
    }

    override fun getViewLifecycle(): LifecycleOwner {
        return this
    }

    override fun registerForContextMenu(view: View) {
        super.registerForContextMenu(view)
        viewsRegisterForContextMenu.add(view)
    }
}
