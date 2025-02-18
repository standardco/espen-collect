package org.espen.collect.android.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import org.espen.collect.android.activities.AboutActivity;
import org.espen.collect.android.activities.AppListActivity;
import org.espen.collect.android.activities.DeleteFormsActivity;
import org.espen.collect.android.activities.FirstLaunchActivity;
import org.espen.collect.android.activities.FormDownloadListActivity;
import org.espen.collect.android.activities.FormFillingActivity;
import org.espen.collect.android.activities.FormMapActivity;
import org.espen.collect.android.activities.InstanceChooserList;
import org.espen.collect.android.application.Collect;
import org.espen.collect.android.application.initialization.ApplicationInitializer;
import org.espen.collect.android.application.initialization.ExistingProjectMigrator;
import org.espen.collect.android.audio.AudioRecordingControllerFragment;
import org.espen.collect.android.audio.AudioRecordingErrorDialogFragment;
import org.espen.collect.android.backgroundwork.AutoUpdateTaskSpec;
import org.espen.collect.android.backgroundwork.SendFormsTaskSpec;
import org.espen.collect.android.backgroundwork.SyncFormsTaskSpec;
import org.espen.collect.android.configure.qr.QRCodeScannerFragment;
import org.espen.collect.android.configure.qr.QRCodeTabsActivity;
import org.espen.collect.android.configure.qr.ShowQRCodeFragment;
import org.espen.collect.android.entities.EntitiesRepositoryProvider;
import org.espen.collect.android.external.AndroidShortcutsActivity;
import org.espen.collect.android.external.FormUriActivity;
import org.espen.collect.android.external.FormsProvider;
import org.espen.collect.android.external.InstanceProvider;
import org.espen.collect.android.formentry.BackgroundAudioPermissionDialogFragment;
import org.espen.collect.android.formentry.ODKView;
import org.espen.collect.android.formentry.repeats.DeleteRepeatDialogFragment;
import org.espen.collect.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import org.espen.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.espen.collect.android.formhierarchy.FormHierarchyActivity;
import org.espen.collect.android.formlists.blankformlist.BlankFormListActivity;
import org.espen.collect.android.formmanagement.FormSourceProvider;
import org.espen.collect.android.formmanagement.FormsDataService;
import org.espen.collect.android.fragments.BarCodeScannerFragment;
import org.espen.collect.android.fragments.dialogs.FormsDownloadResultDialog;
import org.espen.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.espen.collect.android.instancemanagement.send.InstanceUploaderActivity;
import org.espen.collect.android.instancemanagement.send.InstanceUploaderListActivity;
import org.espen.collect.android.mainmenu.MainMenuActivity;
import org.espen.collect.android.openrosa.OpenRosaHttpInterface;
import org.espen.collect.android.preferences.dialogs.AdminPasswordDialogFragment;
import org.espen.collect.android.preferences.dialogs.ChangeAdminPasswordDialog;
import org.espen.collect.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat;
import org.espen.collect.android.preferences.dialogs.ServerAuthDialogFragment;
import org.espen.collect.android.preferences.screens.BasePreferencesFragment;
import org.espen.collect.android.preferences.screens.BaseProjectPreferencesFragment;
import org.espen.collect.android.preferences.screens.ExperimentalPreferencesFragment;
import org.espen.collect.android.preferences.screens.FormManagementPreferencesFragment;
import org.espen.collect.android.preferences.screens.FormMetadataPreferencesFragment;
import org.espen.collect.android.preferences.screens.IdentityPreferencesFragment;
import org.espen.collect.android.preferences.screens.MapsPreferencesFragment;
import org.espen.collect.android.preferences.screens.ProjectDisplayPreferencesFragment;
import org.espen.collect.android.preferences.screens.ProjectManagementPreferencesFragment;
import org.espen.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.espen.collect.android.preferences.screens.ProjectPreferencesFragment;
import org.espen.collect.android.preferences.screens.ServerPreferencesFragment;
import org.espen.collect.android.preferences.screens.UserInterfacePreferencesFragment;
import org.espen.collect.android.projects.ManualProjectCreatorDialog;
import org.espen.collect.android.projects.ProjectResetter;
import org.espen.collect.android.projects.ProjectSettingsDialog;
import org.espen.collect.android.projects.ProjectsDataService;
import org.espen.collect.android.projects.QrCodeProjectCreatorDialog;
import org.espen.collect.android.storage.StoragePathProvider;
import org.espen.collect.android.tasks.DownloadFormListTask;
import org.espen.collect.android.tasks.InstanceUploaderTask;
import org.espen.collect.android.tasks.MediaLoadingTask;
import org.espen.collect.android.utilities.AuthDialogUtility;
import org.espen.collect.android.utilities.FormsRepositoryProvider;
import org.espen.collect.android.utilities.InstancesRepositoryProvider;
import org.espen.collect.android.utilities.SavepointsRepositoryProvider;
import org.espen.collect.android.utilities.ThemeUtils;
import org.espen.collect.android.widgets.QuestionWidget;
import org.espen.collect.android.widgets.items.SelectOneFromMapDialogFragment;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.network.NetworkStateProvider;
import org.odk.collect.draw.DrawActivity;
import org.odk.collect.googlemaps.GoogleMapFragment;
import org.odk.collect.location.LocationClient;
import org.odk.collect.maps.MapFragmentFactory;
import org.odk.collect.maps.layers.ReferenceLayerRepository;
import org.odk.collect.permissions.PermissionsChecker;
import org.odk.collect.permissions.PermissionsProvider;
import org.odk.collect.projects.ProjectsRepository;
import org.odk.collect.settings.ODKAppSettingsImporter;
import org.odk.collect.settings.SettingsProvider;
import org.odk.collect.webpage.ExternalWebPageHelper;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 * <p>
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 * <p>
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 * <p>
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 * <p>
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 **/

@Singleton
@Component(modules = {
        AppDependencyModule.class
})
public interface AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        Builder appDependencyModule(AppDependencyModule testDependencyModule);

        AppDependencyComponent build();
    }

    void inject(Collect collect);

    void inject(AboutActivity aboutActivity);

    void inject(FormFillingActivity formFillingActivity);

    void inject(InstanceUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(ProjectDisplayPreferencesFragment projectDisplayPreferencesFragment);

    void inject(ProjectManagementPreferencesFragment projectManagementPreferencesFragment);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    void inject(QuestionWidget questionWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataPreferencesFragment formMetadataPreferencesFragment);

    void inject(FormMapActivity formMapActivity);

    void inject(GoogleMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(SendFormsTaskSpec sendFormsTaskSpec);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(FormHierarchyActivity formHierarchyActivity);

    void inject(FormManagementPreferencesFragment formManagementPreferencesFragment);

    void inject(IdentityPreferencesFragment identityPreferencesFragment);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    void inject(BarCodeScannerFragment barCodeScannerFragment);

    void inject(QRCodeScannerFragment qrCodeScannerFragment);

    void inject(ProjectPreferencesActivity projectPreferencesActivity);

    void inject(ResetDialogPreferenceFragmentCompat resetDialogPreferenceFragmentCompat);

    void inject(SyncFormsTaskSpec syncWork);

    void inject(ExperimentalPreferencesFragment experimentalPreferencesFragment);

    void inject(AutoUpdateTaskSpec autoUpdateTaskSpec);

    void inject(ServerAuthDialogFragment serverAuthDialogFragment);

    void inject(BasePreferencesFragment basePreferencesFragment);

    void inject(InstanceUploaderActivity instanceUploaderActivity);

    void inject(ProjectPreferencesFragment projectPreferencesFragment);

    void inject(DeleteFormsActivity deleteFormsActivity);

    void inject(SelectMinimalDialog selectMinimalDialog);

    void inject(AudioRecordingControllerFragment audioRecordingControllerFragment);

    void inject(SaveAnswerFileErrorDialogFragment saveAnswerFileErrorDialogFragment);

    void inject(AudioRecordingErrorDialogFragment audioRecordingErrorDialogFragment);

    void inject(InstanceChooserList instanceChooserList);

    void inject(FormsProvider formsProvider);

    void inject(InstanceProvider instanceProvider);

    void inject(BackgroundAudioPermissionDialogFragment backgroundAudioPermissionDialogFragment);

    void inject(ChangeAdminPasswordDialog changeAdminPasswordDialog);

    void inject(MediaLoadingTask mediaLoadingTask);

    void inject(ThemeUtils themeUtils);

    void inject(BaseProjectPreferencesFragment baseProjectPreferencesFragment);

    void inject(AndroidShortcutsActivity androidShortcutsActivity);

    void inject(ProjectSettingsDialog projectSettingsDialog);

    void inject(ManualProjectCreatorDialog manualProjectCreatorDialog);

    void inject(QrCodeProjectCreatorDialog qrCodeProjectCreatorDialog);

    void inject(FirstLaunchActivity firstLaunchActivity);

    void inject(FormUriActivity formUriActivity);

    void inject(MapsPreferencesFragment mapsPreferencesFragment);

    void inject(FormsDownloadResultDialog formsDownloadResultDialog);

    void inject(SelectOneFromMapDialogFragment selectOneFromMapDialogFragment);

    void inject(DrawActivity drawActivity);

    void inject(BlankFormListActivity blankFormListActivity);

    void inject(DeleteRepeatDialogFragment deleteRepeatDialogFragment);

    void inject(AppListActivity appListActivity);

    void inject(DownloadFormListTask downloadFormListTask);

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    SettingsProvider settingsProvider();

    ApplicationInitializer applicationInitializer();

    ODKAppSettingsImporter settingsImporter();

    ProjectsRepository projectsRepository();

    ProjectsDataService currentProjectProvider();

    StoragePathProvider storagePathProvider();

    FormsRepositoryProvider formsRepositoryProvider();

    InstancesRepositoryProvider instancesRepositoryProvider();

    SavepointsRepositoryProvider savepointsRepositoryProvider();

    FormSourceProvider formSourceProvider();

    ExistingProjectMigrator existingProjectMigrator();

    ProjectResetter projectResetter();

    MapFragmentFactory mapFragmentFactory();

    Scheduler scheduler();

    LocationClient locationClient();

    PermissionsProvider permissionsProvider();

    PermissionsChecker permissionsChecker();

    ReferenceLayerRepository referenceLayerRepository();

    NetworkStateProvider networkStateProvider();

    EntitiesRepositoryProvider entitiesRepositoryProvider();

    FormsDataService formsDataService();

    ProjectDependencyModuleFactory projectDependencyModuleFactory();

    ExternalWebPageHelper externalWebPageHelper();
}
