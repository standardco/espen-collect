# Dependency Upgrade Summary — 16 KB Memory Page Size Fix

## Background

Android 15 introduced support for devices with 16 KB memory page size kernels.
Apps that include native libraries (.so files) not aligned to 16 KB boundaries
will show the error: "Your app does not support 16 KB memory page sizes."

## Manifest & Build Config Fixes

| File | Change |
|------|--------|
| `collect_app/src/main/AndroidManifest.xml` | Added `android:extractNativeLibs="false"` |
| `collect_app/build.gradle` | Added `jniLibs.useLegacyPackaging = false` |

These two settings make Android load `.so` files directly from the APK
(uncompressed, page-aligned) instead of extracting them to disk, which is
required for 16 KB page size compatibility.

## Library Updates (`buildSrc/src/main/java/dependencies/`)

### Versions.kt

| Constant | Old | New | Reason |
|----------|-----|-----|--------|
| `camerax` | 1.3.4 | 1.5.3 | 16 KB page support added in 1.4.0+ |
| `androidx_fragment` | 1.8.1 | 1.8.9 | Latest patch |
| `dagger` | 2.51.1 | 2.59.2 | Latest stable |
| `robolectric` | 4.12.2 | 4.16.1 | Latest stable |
| `work` | 2.9.0 | 2.11.1 | Latest stable |
| `lifecycle` | 2.8.3 | 2.10.0 | Latest stable |
| `navigation` | 2.7.7 | 2.9.7 | Latest stable |

### Dependencies.kt

| Constant | Old | New | Reason |
|----------|-----|-----|--------|
| `desugar_jdk_libs` | 2.0.4 | 2.1.5 | Latest stable |
| `core-ktx` | 1.13.1 | 1.17.0 | Latest stable |
| `recyclerview` | 1.3.2 | 1.4.0 | Latest stable |
| `play-services-maps` | 19.0.0 | 20.0.0 | Latest stable |
| `play-services-location` | 20.0.0 | 21.3.0 | Latest stable |
| `osmdroid` | 6.1.18 | 6.1.20 | Latest stable |
| `opencsv` | 5.9 | 5.12.0 | Latest stable |
| `javarosa` | 5.0.0 | 5.1.0 | Latest stable |
| `kotlinx-coroutines` | 1.8.1 | 1.10.2 | Latest stable |
| `gson` | 2.11.0 | 2.13.2 | Latest stable |
| `firebase-analytics` | 22.0.2 | 23.0.0 | Latest stable |
| `firebase-crashlytics` | 19.0.2 | 19.4.4 | Latest stable |
| `splashscreen` | 1.0.1 | 1.2.0 | Latest stable |
| `jsoup` | 1.17.2 | 1.22.1 | Latest stable |
| `test core-ktx` | 1.6.1 | 1.7.0 | Latest stable |
| `test rules` | 1.6.1 | 1.7.0 | Latest stable |
| `test-ext-junit` | 1.2.1 | 1.3.0 | Latest stable |

### build.gradle (root)

| Plugin | Old | New | Reason |
|--------|-----|-----|--------|
| `navigation-safe-args-gradle-plugin` | 2.7.7 | 2.9.7 | Must match navigation library version |

## Intentionally Skipped

| Library | Reason |
|---------|--------|
| `mapbox` (10.16.4) | Known crash issue — see comment in Dependencies.kt before upgrading to 11.x |
| `commons-io` (2.5) | 2.6+ requires `java.nio` which needs minSdk ≥ 26; current minSdk is 21 |
| `myanmar-calendar` | Known issue — see comment in Dependencies.kt |
| `mp4parser-muxer` | Known issue — see comment in Dependencies.kt |
| `okhttp` (4.x) | OkHttp 5.x has breaking API changes; separate migration needed |
| `glide` (4.x) | Glide 5.x has breaking API changes; separate migration needed |

## Next Steps

- Build and test on an Android 15 device or emulator with a 16 KB page size kernel
- Verify map screens still work after `play-services-location` and `play-services-maps` upgrades
- Check the Mapbox issue linked in Dependencies.kt before considering a Mapbox 11.x upgrade
