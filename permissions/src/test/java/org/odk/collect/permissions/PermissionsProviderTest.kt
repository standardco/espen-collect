package org.odk.collect.permissions

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.net.Uri
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.CoreMatchers.`is`
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

class PermissionsProviderTest {

    private var permissionsChecker = mock<PermissionsChecker>()
    private var activity = mock<Activity>()
    private var uri = mock<Uri>()
    private var contentResolver = mock<ContentResolver>()
    private var permissionListener = mock<PermissionListener>()
    private val permissionsApi = TestRequestPermissionsAPI()
    private val permissionsDialogCreator = mock<PermissionsDialogCreator>()
    private val locationAccessibilityChecker = mock<LocationAccessibilityChecker>()

    private lateinit var permissionsProvider: PermissionsProvider

    @Before
    fun setup() {
        permissionsProvider = PermissionsProvider(permissionsChecker, permissionsApi, permissionsDialogCreator, locationAccessibilityChecker)
    }

    @Test
    fun `isCameraPermissionGranted() when camera permission is granted returns true`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)).thenReturn(true)

        assertThat(permissionsProvider.isCameraPermissionGranted, `is`(true))
    }

    @Test
    fun `isCameraPermissionGranted() when camera permission is not granted returns false`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA)).thenReturn(false)

        assertThat(permissionsProvider.isCameraPermissionGranted, `is`(false))
    }

    @Test
    fun `requestCameraPermission when camera permission is granted calls PermissionListener#granted`() {
        permissionsApi.setGrantedPermission(Manifest.permission.CAMERA)

        permissionsProvider.requestCameraPermission(activity, permissionListener)

        verify(permissionListener).granted()
        verifyNoMoreInteractions(permissionListener)
        verifyNoInteractions(permissionsDialogCreator)
    }

    @Test
    fun `requestCameraPermission() when camera permission is not granted calls PermissionListener#denied`() {
        permissionsProvider.requestCameraPermission(activity, permissionListener)

        verify(permissionListener).denied()
        verifyNoMoreInteractions(permissionListener)
    }

    @Test
    fun `requestCameraPermission() when camera permission is not granted calls PermissionsDialogCreator#showAdditionalExplanation`() {
        permissionsProvider.requestCameraPermission(activity, permissionListener)

        verify(permissionsDialogCreator).showAdditionalExplanation(
            activity,
            org.odk.collect.strings.R.string.camera_runtime_permission_denied_title,
            org.odk.collect.strings.R.string.camera_runtime_permission_denied_desc,
            R.drawable.ic_photo_camera,
            permissionListener
        )
        verifyNoMoreInteractions(permissionsDialogCreator)
    }

    @Test
    fun `areLocationPermissionsGranted() when location permission is granted returns true`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(true)

        assertThat(permissionsProvider.areLocationPermissionsGranted(), `is`(true))
    }

    @Test
    fun `areLocationPermissionsGranted() when location permission is not granted returns false`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)).thenReturn(false)

        assertThat(permissionsProvider.areLocationPermissionsGranted(), `is`(false))
    }

    @Test
    fun `requestEnabledLocationPermissions() when location permission is granted and location is enabled in settings calls PermissionListener#granted`() {
        whenever(locationAccessibilityChecker.isLocationEnabled(activity)).thenReturn(true)
        permissionsApi.setGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionsProvider.requestEnabledLocationPermissions(activity, permissionListener)

        verify(permissionListener).granted()
        verifyNoMoreInteractions(permissionListener)
        verifyNoInteractions(permissionsDialogCreator)
    }

    @Test
    fun `requestEnabledLocationPermissions() when location permission is granted and location is disabled in settings calls PermissionsDialogCreator#showEnableGPSDialog`() {
        whenever(locationAccessibilityChecker.isLocationEnabled(activity)).thenReturn(false)
        permissionsApi.setGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

        permissionsProvider.requestEnabledLocationPermissions(activity, permissionListener)

        verify(permissionsDialogCreator).showEnableGPSDialog(activity, permissionListener)
        verifyNoInteractions(permissionListener)
    }

    @Test
    fun `requestEnabledLocationPermissions() wen location permission is not granted calls PermissionListener#denied`() {
        permissionsProvider.requestEnabledLocationPermissions(activity, permissionListener)

        verify(permissionListener).denied()
        verifyNoMoreInteractions(permissionListener)
    }

    @Test
    fun `requestEnabledLocationPermissions() when location permission is not granted calls PermissionsDialogCreator#showAdditionalExplanation`() {
        permissionsProvider.requestEnabledLocationPermissions(activity, permissionListener)

        verify(permissionsDialogCreator).showAdditionalExplanation(
            activity,
            org.odk.collect.strings.R.string.location_runtime_permissions_denied_title,
            org.odk.collect.strings.R.string.location_runtime_permissions_denied_desc,
            R.drawable.ic_room_24dp,
            permissionListener
        )

        verifyNoMoreInteractions(permissionsDialogCreator)
    }

    @Test
    fun `requestRecordAudioPermission() when audio permission is granted calls PermissionListener#granted`() {
        permissionsApi.setGrantedPermission(Manifest.permission.RECORD_AUDIO)

        permissionsProvider.requestRecordAudioPermission(activity, permissionListener)

        verify(permissionListener).granted()
        verifyNoMoreInteractions(permissionListener)
        verifyNoInteractions(permissionsDialogCreator)
    }

    @Test
    fun `requestRecordAudioPermission() when audio permission is not granted calls PermissionListener#denied`() {
        permissionsProvider.requestRecordAudioPermission(activity, permissionListener)

        verify(permissionListener).denied()
        verifyNoMoreInteractions(permissionListener)
    }

    @Test
    fun `requestRecordAudioPermission() when audio permission is not granted calls PermissionsDialogCreator#showAdditionalExplanation`() {
        permissionsProvider.requestRecordAudioPermission(activity, permissionListener)

        verify(permissionsDialogCreator).showAdditionalExplanation(
            activity,
            org.odk.collect.strings.R.string.record_audio_runtime_permission_denied_title,
            org.odk.collect.strings.R.string.record_audio_runtime_permission_denied_desc,
            org.odk.collect.icons.R.drawable.ic_baseline_mic_24,
            permissionListener
        )

        verifyNoMoreInteractions(permissionsDialogCreator)
    }

    @Test
    fun `areCameraAndRecordAudioPermissionsGranted() when camera and audio permission is granted returns true`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)).thenReturn(true)

        assertThat(permissionsProvider.areCameraAndRecordAudioPermissionsGranted(), `is`(true))
    }

    @Test
    fun `areCameraAndRecordAudioPermissionsGranted() when camera and audio permission is not granted returns false`() {
        whenever(permissionsChecker.isPermissionGranted(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)).thenReturn(false)

        assertThat(permissionsProvider.areCameraAndRecordAudioPermissionsGranted(), `is`(false))
    }

    @Test
    fun `requestCameraAndRecordAudioPermissions() when camera and audio permission is granted calls PermissionListener#granted`() {
        permissionsApi.setGrantedPermission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

        permissionsProvider.requestCameraAndRecordAudioPermissions(activity, permissionListener)

        verify(permissionListener).granted()
        verifyNoMoreInteractions(permissionListener)
        verifyNoInteractions(permissionsDialogCreator)
    }

    @Test
    fun `requestCameraAndRecordAudioPermissions() when camera and audio permission is not granted calls PermissionListener#denied`() {
        permissionsProvider.requestCameraAndRecordAudioPermissions(activity, permissionListener)

        verify(permissionListener).denied()
        verifyNoMoreInteractions(permissionListener)
    }

    @Test
    fun `requestCameraAndRecordAudioPermissions() when camera and audio permission is not granted calls PermissionsDialogCreator#showAdditionalExplanation`() {
        permissionsProvider.requestCameraAndRecordAudioPermissions(activity, permissionListener)

        verify(permissionsDialogCreator).showAdditionalExplanation(
            activity,
            org.odk.collect.strings.R.string.camera_runtime_permission_denied_title,
            org.odk.collect.strings.R.string.camera_runtime_permission_denied_desc,
            R.drawable.ic_photo_camera,
            permissionListener
        )
        verifyNoMoreInteractions(permissionsDialogCreator)
    }

    @Test
    fun `requestReadUriPermission() when request read uri permission granted calls PermissionListener#granted`() {
        permissionsProvider.requestReadUriPermission(
            activity,
            uri,
            contentResolver,
            permissionListener
        )
        verify(permissionListener).granted()
    }

    @Test
    fun `requestReadUriPermission() when throws any exception calls PermissionListener#granted#denied`() {
        whenever(contentResolver.query(uri, null, null, null, null)).thenThrow(
            RuntimeException::class.java
        )
        permissionsProvider.requestReadUriPermission(
            activity,
            uri,
            contentResolver,
            permissionListener
        )
        verify(permissionListener).denied()
    }

    @Test
    fun `granted listener is not called when Activity is finishing`() {
        permissionsApi.setGrantedPermission(Manifest.permission.CAMERA)

        whenever(activity.isFinishing).doReturn(true)

        permissionsProvider.requestPermissions(
            activity,
            permissionListener,
            Manifest.permission.CAMERA
        )

        verifyNoInteractions(permissionListener)
    }

    @Test
    fun `denied listener is not called when Activity is finishing`() {
        permissionsApi.setGrantedPermission(Manifest.permission.CAMERA)

        whenever(activity.isFinishing).doReturn(true)

        permissionsProvider.requestPermissions(
            activity,
            permissionListener,
            Manifest.permission.CAMERA
        )

        verifyNoInteractions(permissionListener)
    }

    private class TestRequestPermissionsAPI : RequestPermissionsAPI {

        private var grantedPermissions: List<String> = emptyList()

        override fun requestPermissions(
            activity: Activity,
            listener: PermissionListener,
            vararg permissions: String
        ) {
            if (grantedPermissions.containsAll(permissions.asList())) {
                listener.granted()
            } else {
                listener.denied()
            }
        }

        fun setGrantedPermission(vararg grantedPermissions: String) {
            this.grantedPermissions = grantedPermissions.asList()
        }
    }
}
