package org.odk.collect.android.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import org.odk.collect.android.views.BarcodeViewDecoder
import org.odk.collect.androidshared.utils.CompressionUtils
import java.io.IOException

class StubBarcodeViewDecoder : BarcodeViewDecoder() {
    var liveData = MutableLiveData<BarcodeResult>()

    override fun waitForBarcode(view: DecoratedBarcodeView): LiveData<BarcodeResult> {
        return liveData
    }

    fun scan(settings: String?) {
        try {
            val result = Result(
                CompressionUtils.compress(settings),
                byteArrayOf(),
                arrayOf(),
                BarcodeFormat.AZTEC
            )
            val barcodeResult = BarcodeResult(result, null)
            liveData.postValue(barcodeResult)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
