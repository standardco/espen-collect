package org.odk.collect.androidtest

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Gets the value of a [LiveData] or waits for it to have one, with a timeout.
 *
 * Use this extension from host-side (JVM) tests. It's recommended to use it alongside
 * `InstantTaskExecutorRule` or a similar mechanism to execute tasks synchronously.
 *
 * From: https://github.com/android/architecture-components-samples/blob/master/GithubBrowserSample/app/src/test-common/java/com/android/example/github/util/LiveDataTestUtil.kt
 */
@JvmOverloads
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS,
    afterObserve: () -> Unit = {}
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = Observer<T> { o ->
        data = o
        latch.countDown()
    }

    this.observeForever(observer)
    afterObserve.invoke()

    // Don't wait indefinitely if the LiveData is not set.
    if (latch.await(time, timeUnit)) {
        this.removeObserver(observer)
    } else {
        this.removeObserver(observer)
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

fun <T> LiveData<T>.recordValues(block: (List<T>) -> Unit) {
    val list = mutableListOf<T>()
    val observer = Observer<T> {
        list.add(it)
    }

    try {
        this.observeForever(observer)
        block(list)
    } finally {
        this.removeObserver(observer)
    }
}
