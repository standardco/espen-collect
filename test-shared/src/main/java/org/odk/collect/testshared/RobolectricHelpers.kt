package org.odk.collect.testshared

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.Intent
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.os.Looper
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.servicetest.ServiceScenario
import org.odk.collect.servicetest.ServiceScenario.Companion.launch
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowEnvironment
import org.robolectric.shadows.ShadowMediaMetadataRetriever
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.ShadowMediaPlayer.MediaInfo
import org.robolectric.shadows.util.DataSource

object RobolectricHelpers {

    var services: MutableMap<Class<*>, ServiceScenario<*>> = HashMap()

    @JvmStatic
    @JvmOverloads
    fun <T : FragmentActivity> createThemedActivity(
        clazz: Class<T>,
        theme: Int = com.google.android.material.R.style.Theme_MaterialComponents
    ): T {
        val activity = Robolectric.buildActivity(clazz)
        activity.get()!!.setTheme(theme)
        return activity.setup().get()
    }

    @JvmStatic
    fun getCreatedFromResId(drawable: Drawable): Int {
        return Shadows.shadowOf(drawable).createdFromResId
    }

    @JvmStatic
    @JvmOverloads
    fun setupMediaPlayerDataSource(testFile: String, duration: Int = 322450): DataSource {
        val dataSource = DataSource.toDataSource(testFile)
        ShadowMediaMetadataRetriever.addMetadata(
            dataSource,
            MediaMetadataRetriever.METADATA_KEY_DURATION,
            duration.toString()
        )
        ShadowMediaPlayer.addMediaInfo(dataSource, MediaInfo(duration, 0))
        return dataSource
    }

    fun mountExternalStorage() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED)
    }

    @JvmStatic
    fun <T : ViewGroup> populateRecyclerView(view: T): T {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            if (child is RecyclerView) {
                child.measure(0, 0)
                child.layout(0, 0, 100, 10000)
                break
            } else if (child is ViewGroup) {
                populateRecyclerView(child)
            }
        }
        return view
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment?> getFragmentByClass(
        fragmentManager: FragmentManager,
        fragmentClass: Class<F>
    ): F? {
        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.javaClass.isAssignableFrom(fragmentClass)) {
                return fragment as F
            }
        }
        return null
    }

    @JvmStatic
    fun runLooper() {
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    fun clearServices() {
        services.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun runServices(keepServices: Boolean) {
        val application = ApplicationProvider.getApplicationContext<Application>()

        // Run pending start commands
        while (Shadows.shadowOf(application).peekNextStartedService() != null) {
            val intent = Shadows.shadowOf(application).nextStartedService
            val serviceClass: Class<*> = try {
                Class.forName(intent.component!!.className)
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(e)
            }
            if (keepServices) {
                if (services.containsKey(serviceClass)) {
                    services[serviceClass]!!.startWithNewIntent(intent)
                } else {
                    val serviceController: ServiceScenario<*> =
                        launch(serviceClass as Class<Service>, intent)
                    services[serviceClass] = serviceController
                }
            } else {
                launch(serviceClass as Class<Service>, intent)
            }
        }

        // Run pending stops - only need to stop previously started services
        if (keepServices) {
            while (true) {
                val intent = Shadows.shadowOf(application).nextStoppedService ?: break
                val serviceClass: Class<*> = try {
                    Class.forName(intent.component!!.className)
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException(e)
                }
                if (services.containsKey(serviceClass)) {
                    services[serviceClass]!!.moveToState(Lifecycle.State.DESTROYED)
                    services.remove(serviceClass)
                }
            }
        }
    }

    inline fun <reified A : Activity> ActivityController<A>.recreateWithProcessRestore(
        resultCode: Int? = null,
        result: Intent? = null,
        resetProcess: () -> Unit
    ): ActivityController<A> {
        // Destroy activity with saved instance state
        val outState = Bundle()
        this.saveInstanceState(outState).pause().stop().destroy()

        // Reset process
        resetProcess()

        // Recreate with saved instance state
        val recreated = Robolectric.buildActivity(A::class.java, this.intent).create(outState)
            .start()
            .restoreInstanceState(outState)
            .postCreate(outState)

        // Return result
        if (resultCode != null) {
            val startedActivityForResult = shadowOf(this.get()).nextStartedActivityForResult
            shadowOf(recreated.get()).receiveResult(
                startedActivityForResult.intent,
                resultCode,
                result
            )
        }

        // Resume activity
        return recreated.resume()
            .visible()
            .topActivityResumed(true)
    }
}
