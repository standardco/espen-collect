# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is ESPEN Collect, a fork of [ODK Collect](https://github.com/getodk/collect) - an Android app for filling out XForms in resource-constrained environments. The codebase is a mature, production Android application with a 10+ year history and a mix of Java and Kotlin code.

## Build and Development Commands

### Building
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing configuration)
./gradlew assembleOdkCollectRelease

# Build both app and test APK for Firebase Test Lab
./gradlew collect_app:assembleDebug collect_app:assembleDebugAndroidTest
```

### Running Tests
```bash
# Run all unit tests (JUnit and Robolectric)
./gradlew testDebug

# Run instrumented tests (requires emulator or device)
./gradlew connectedAndroidTest

# Run code quality checks (PMD, ktlint, checkstyle, lint)
./gradlew checkCode

# Run Firebase Test Lab tests (requires gcloud CLI and project setup)
./gradlew testLab

# Run single test class
./gradlew testDebug --tests "org.espen.collect.android.SomeTestClass"

# Run single test method
./gradlew testDebug --tests "org.espen.collect.android.SomeTestClass.testSomeMethod"
```

### Linting and Code Quality
```bash
# Check code style (ktlint, checkstyle, PMD, Android lint)
./gradlew checkCode

# Auto-fix Kotlin style issues
./gradlew ktlintFormat
```

### Debugging and Development
```bash
# Build and deploy debug app to connected device/emulator
./gradlew collect_app:installDebug

# Run with logging
./gradlew testDebug --info

# Generate cache of dependencies locally
./gradlew cacheToMavenLocal
```

### Custom Tasks
```bash
# Release check (runs testLab and assembles release)
./gradlew releaseCheck

# Run benchmarks with connected device
./benchmark.sh
```

## Architecture and Code Organization

### Multi-Module Structure
ESPEN Collect uses a multi-module Gradle architecture to organize code by feature rather than by layer. Key modules:

- **collect_app** - Main application module containing the core form entry experience
- **forms** - Form definition and parsing logic
- **location** - Geolocation and mapping functionality (GeoPoint, GeoTrace, GeoShape)
- **geo** - Geo-specific utilities
- **maps** - Map abstraction layer (supports Mapbox, osmdroid, Google Maps)
- **osmdroid** - OpenStreetMap map engine
- **google-maps** - Google Maps engine
- **mapbox** - Mapbox engine (conditionally included, requires API token)
- **audio-recorder** - Audio recording functionality
- **image-loader** - Image loading and caching
- **settings** - Application settings and preferences UI
- **analytics** - Analytics event tracking
- **strings** - String resources and translations
- **icons** - Shared Material Design icons
- **material** - Material Design component configurations
- **entities** - Domain entities and data models
- **db** - Database and file storage
- **projects** - Project management
- **upgrade** - App upgrade and migration logic
- **shared** / **androidshared** - Shared utilities across modules

When making changes, consider whether code can be isolated in a feature module and exposed through a clean interface.

### Core Application Flow

1. **Form Loading**: Forms are loaded from OpenRosa servers or local disk via the forms repository pattern
2. **Form Entry**: The primary form filling experience happens in `FormFillingActivity` with a side-to-side swipe view made up of `ODKView` components
3. **Question Rendering**: Questions are rendered using a `QuestionWidget` framework - each question type has a corresponding widget implementation
4. **Form State**: Form state is managed by a process singleton `FormController` (historical; being refactored)
5. **Data Storage**: Form instances are stored in SQLite with flat file storage, accessed through repository objects

### Key Architectural Components

- **FormFillingActivity** - "God" activity containing most form entry logic (actively being refactored to move responsibilities)
- **FormController** - Process singleton managing form state (historical component, being refactored)
- **QuestionWidget** - Abstract base class for all question type renderings (documented in `docs/WIDGETS.md`)
- **Repository Pattern** - `FormsRepository`, form instance repositories, and other data access abstractions
- **Settings Abstraction** - `SharedPreferences` wrapped in app's own `Settings` interface
- **AsyncTask to Flow** - Legacy async work being migrated from `AsyncTask` to `Flow`/`LiveData` with scheduler abstractions
- **Dagger2 DI** - Dependency injection for Activities and black-box components (see CODE-GUIDELINES.md for usage)

### Modern Development Patterns

The codebase is actively evolving toward:
- **Data services architecture** - Using AndroidX Architecture Components (Fragment, View, ViewModel)
- **Kotlin-first** - All new code written in Kotlin
- **Reactive async** - Migration from `AsyncTask` to `Flow`/`LiveData`
- **Modular extraction** - Moving code into focused feature modules
- **Test coverage** - Enforcing tests for new code

## Testing

Tests are organized into several categories:

### JUnit Tests
Plain JUnit tests with mocked Android dependencies. Run on the JVM. Located in `collect_app/src/test/java`. Fast and isolated.

### Robolectric Tests
JUnit tests that use Robolectric's fake Android SDK. Also in `collect_app/src/test/java` but annotated with `@RunWith(RobolectricTestRunner.class)` or `@RunWith(AndroidJUnit4.class)`. Test Android-specific behavior like lifecycle and view rendering.

### Feature Tests
Espresso-based UI tests that drive whole features. Located in `collect_app/src/androidTest/java/feature`. Use page objects from `org.espen.collect.android.support.pages` package for readability.

### Regression Tests
QA-written tests in `collect_app/src/androidTest/java/regression`. Being incrementally transitioned to feature tests.

### Instrumented Tests
Tests in `collect_app/src/androidTest/java/instrumented` that run on device/emulator but don't interact with UI. Avoid when possible - prefer Robolectric or JUnit.

**Important**: When writing tests:
- Use [Hamcrest matchers](http://hamcrest.org/JavaHamcrest/) (`assertThat`) instead of JUnit asserts
- Use backtick test names in Kotlin: `` `#getHello returns hello string` ``
- Avoid "should" and "will" in test names; use third-person singular verbs ("returns", "sets", "fetches")
- Follow TDD when possible

## Code Standards

### Language and Style
- **Kotlin** - Use for all new code. Follow [Kotlin conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Java** - Follow [Android style rules](http://source.android.com/source/code-style.html) and [Google Java style guide](https://google.github.io/styleguide/javaguide.html)
- **Linting** - Code is checked with ktlint, checkstyle, PMD, and Android lint. Run `./gradlew checkCode` before committing.

### Kotlin Test Naming
```kotlin
@Test
fun `#getHello returns hello string`() {
    assertThat(subject.getHello(), equalTo("hello"))
}

@Test
fun `#getHello returns goodbye string when subject is angry`() {
    subject.setAngry(true)
    assertThat(subject.getHello(), equalTo("goodbye"))
}
```

### UI and Components
- Use [Material Design components](https://material.io/components?platform=android) instead of custom components
- Use Material 3 theming with theme attributes for colors (not direct color values)
- Use `ThemeUtils` to fetch Material theme attributes in code
- Use [string resources](https://developer.android.com/guide/topics/resources/string-resource.html) for all user-facing text (not literal strings)
- Place icons in the `icons` module (usually Material Icons acquired via Android Studio's Vector Asset tool)

### Custom View Pattern
Views should follow these principles:
- Be as dumb and stateless as possible
- Not interact with repositories, network, or business logic layers
- Have a single `Listener` interface with one `setListener` method
- Listener methods should represent "meaningful" events (not low-level like `onClick` on complex widgets)
- Ideally have one setter for data, or a `render` method for complex views

### Dependency Injection
- Use normal Java constructors for regular objects
- Use Dagger2 for Activities and Fragments (black-box components)
- Add `@Provides` methods to `AppDependencyModule` for new dependencies
- Swap dependencies in tests using `RobolectricHelpers.overrideAppDependencyModule()`

## APIs and Local Development

### Google Maps API
1. Get a [Google Maps API key](https://developers.google.com/maps/documentation/android-api/signup)
2. Edit/create `secrets.properties` with:
   ```
   GOOGLE_MAPS_API_KEY=AIbzvW8e0ub...
   ```

### Mapbox SDK
1. Create a [Mapbox account](https://www.mapbox.com/signup/) (Pay-As-You-Go plan doesn't require credit card)
2. Get access token from account page under "Tokens" -> "Default public token"
3. Create token with "DOWNLOADS:READ" secret scope
4. Edit/create `secrets.properties` with:
   ```
   MAPBOX_ACCESS_TOKEN=pk.eyJk3bumVp4i...
   MAPBOX_DOWNLOADS_TOKEN=sk....
   ```

Note: Mapbox is not available on x86 devices by default to reduce APK size. Force inclusion with: `./gradlew assembleDebug -Px86Libs`

## Common Development Scenarios

### Running Forms Locally Without a Server
1. Download or create an XForm (XML) or XLSForm
2. Convert XLSForm to XForm using [pyxform](https://github.com/XLSForm/pyxform)
3. Push to emulator/device:
   ```bash
   adb push my_form.xml /sdcard/Android/data/org.odk.collect.android/files/forms
   ```
4. Launch app and tap "Fill Blank Form"

### Using Local JavaRosa for Debugging
1. Clone and build [JavaRosa](https://github.com/getodk/javarosa) locally:
   ```bash
   ./gradlew publishToMavenLocal
   ```
2. Change `Dependencies.kt`: `const val javarosa = javarosa_local`

### Debugging Robolectric Tests on macOS
If you get `build/intermediates/bundles/debug/AndroidManifest.xml (No such file or directory)`:
1. In Android Studio, go to **Run > Edit Configurations**
2. Select **Defaults > JUnit**
3. Change working directory to `$MODULE_DIR$`

### Fixing Gradle Issues
- **SDK location error**: Define `sdk.dir` in `local.properties` or set `ANDROID_HOME` environment variable
- **JVM Heap Size**: Override in user-level `gradle.properties`:
  ```
  org.gradle.jvmargs=-Xmx4096m
  ```
- **NullPointerException in gradlew**: Ensure Java 17 is configured (`File > Project Structure` or `org.gradle.java.home` in `~/.gradle/gradle.properties`)
- **Missing Robolectric JARs**: Run `./download-robolectric-deps.sh`

## Release Process

The release process includes:
1. Update translations from Transifex (automated before release)
2. Run `./gradlew releaseCheck` - performs Firebase Test Lab testing and assembles release APK
3. Manual verification: QR code scanning, form download, form fill, map opening, form submission
4. Run `./benchmark.sh` with real device for performance verification
5. Tag commit with `vX.X.X` (releases) or `vX.X.X-beta.X` (beta)
6. Create GitHub release with auto-generated notes and signed APK
7. Upload to Play Store

Release branches use format `vX.X.X.x` to allow point releases while `master` continues development. Hotfixes are backported to release branches.

## Key Development Notes

- **Emulator Setup**: See `.circleci/config.yml` for CI emulator configuration
- **Branch Strategy**: Work on `master` for current release features. Release branches exist as `v*.*.x` for point releases
- **PR Requirements**: Code must include tests, be comprehensively reviewed, and typically require manual testing before merge
- **Issue Triage**: Prioritized issues live in [GitHub project board](https://github.com/orgs/getodk/projects/9/views/8)
- **Static Analysis**: Code goes through CheckStyle, PMD, ktlint, and Android Lint - all must pass
- **Java Version**: Project uses Java 17 for compilation
- **Device Testing**: See README.md for list of test devices used by the core team

## Documentation References

- **STATE.md** - High-level overview of current state and direction
- **CODE-GUIDELINES.md** - Detailed code style, DI, and architecture guidance
- **TEST-GUIDELINES.md** - Testing strategies and practices
- **CONTRIBUTING.md** - Contribution workflow and PR process
- **WIDGETS.md** - QuestionWidget framework documentation
- **ANALYTICS-QUESTIONS.md** - Analytics event specifications
