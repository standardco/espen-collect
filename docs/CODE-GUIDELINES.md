# Code style guidelines

## Kotlin style guidelines

Follow the [Kotlin code conventions](https://kotlinlang.org/docs/coding-conventions.html).

## Java style guidelines
Follow the [Android style rules](http://source.android.com/source/code-style.html) and the [Google Java style guide](https://google.github.io/styleguide/javaguide.html).

## Testing style guidelines
Favor [Hamcrest](http://hamcrest.org/JavaHamcrest/) asserts over JUnit asserts for readability.

Old JUnit style:
```java
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
...
assertEquals("expected", ClassToTest.methodToTest("input"));
assertNull(ClassToTest.methodReturnsNull());
```

Preferred style using Hamcrest:
```java
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
...
assertThat(ClassToTest.methodToTest("input"), equalTo("expected"));
assertThat(ClassToTest.methodReturnsNull(), equalTo(null));
```

### Backtick test naming

Tests written in Kotlin should use [backtick enclosed method names](https://kotlinlang.org/docs/coding-conventions.html#names-for-test-methods) and use `#` JavaDoc syntax to refer to an object member's in unit tests:

```kotlin
@Test
fun `#getHello returns hello string`() {
    assertThat(subject.getHello(), equalTo("hello"))
}
```

Test naming structure is hard to put strict rules around, but in general conditions ("given") should go after the action ("when") and assertion ("then") in this style of method naming:

```kotlin
@Test
fun `#getHello returns goodbye string when subject is angry`() {
    subject.setAngry(true)
    assertThat(subject.getHello(), equalTo("goodbye"))
}
```

Also, to keep test names short, it's best to avoid words like "should" or "will" (`#getHello should return hello string`) and instead use definitive statements with [third person singular verb conjugations](https://www.grammarly.com/blog/verb-forms/) ("returns", "sets", "fetches" etc).

## XML style guidelines

Follow these naming conventions in Android XML files:

* Attributes (`attr`): `shouldBeCamelCased`
* String, dimension and color names: `should_be_snake_cased`
* Themes and Styles: `ShouldBePascalCased` and should also be qualified in a similar manner to Java package names like `<Type>.<Package>.<Name>...`. For instance:

  ```xml
  Theme.Collect.Light
  TextAppearance.Collect.H1.Purple
  Widget.Collect.Button
  Widget.Collect.Button.BigRed
  ```

## UI components style guidelines

Instead of building custom components, UI should be constructed from standard [Material components](https://material.io/components?platform=android). If a custom or platform component is needed, make sure it is compatible with both light and dark themes by using theme attributes for coloring instead of directly using color values (eg. #000000 or R.color.color_name). In XML these should come from the Material Theme attributes documented [here](https://material.io/develop/android/theming/color/). If you need to set colors in Java code there is a `ThemeUtils` helper class that will let you fetch these attributes.

## Custom view guidelines

When creating or refactoring views, keep in mind our vision of an "ideal" view:

- Views should generally be as dumb and as stateless as possible
- Views shouldn't interact with other layers of your application (repositories, network etc). They should be able to take in and render data (via setters) and emit "meaningful" events via listeners.
- A view should have a single public `Listener` sub interface and `setListener` method. Methods on the interface should correspond to "meaningful" events. If the view is a button that could just be `onClick` but if it's a volume slider this might be something like `onVolumeChanged`.
- Views would ideally have just one setter for data but more complex views (often that have many subviews) that take in data at different times may have more - fewer setters is better as it's less changing state in the view.
- Views that render more than one kind of data (and that have more than one setter) might benefit from a `render` method that encapsulates all the logic around displaying the state of a view.

## Strings
Always use [string resources](https://developer.android.com/guide/topics/resources/string-resource.html) instead of literal strings. This ensures wording consistency across the project and also enables full translation of the app. Only make changes to the base `res/values/strings.xml` English file (in the `strings` module) and not to the other language files. The translated files are generated from [Transifex](https://www.transifex.com/getodk/collect/) where translations can be submitted by the community. Names of software packages or other untranslatable strings should be placed in `res/values/untranslated.xml`.

If a new string is added that is used as a date format (with `SimpleDateFormat`), it should be added to `DateFormatsTest` to ensure that translations do not cause crashes.

Strings that represent very rare failure cases or that are meant more for ODK developers to use for troubleshooting rather than directly for users may be written as literal strings. This reduces the burden on translators and makes it easier for developers to troubleshoot edge cases without having to look up translations.

## Icons

Icons (usually defined using Android XML drawables) should be placed in the `icons` module so they can be shared between other modules without being duplicated. Icons should usually come from the set of Material Icons which are easiest to acquire from Android Studio's "Vector Asset" tool.

## Dependency injection

As much as possible to facilitate simpler, more modular and more testable components you should follow the Dependency Inversion principle in Collect Code. An example tutorial on this concept can be found [here](https://www.seadowg.com/dip-lesson/).

Because many Android components (Activity and Fragment for instance) don't allow us control over their constructors Collect uses [Dagger](https://google.github.io/dagger/) to 'inject' dependencies. The configuration for Dagger can be found in [AppDepdendencyComponent](collect_app/src/main/java/org/odk/collect/android/injection/config/AppDependencyComponent.java). For any normal objects it is probably best to avoid Dagger and use normal Java constructors.

While it's important to read the Dagger [documentation](https://google.github.io/dagger/users-guide) we've provided some basic instructions on how to use Dagger within Collect below.

### Providing dependencies

To declare a new dependency that objects can inject add a `@Provides` method to the `AppDependencyModule`:

```java
@Provides
public MyDependency providesMyDependency() {
    return MyDependency();
}
```

You can also have Dagger return the same instance every time (i.e. a Singleton) by annotating the method with `@Singleton` as well.

### Injecting dependencies into Activity/Fragment objects

To inject a dependency into the Activity you first need to make Dagger aware it's injecting into that Activity by adding an `inject` to the `AppDependencyComponent` (if it's not already there):

```java
void inject(MyActivity activity);
```

Then define a field with the `@Inject` annotation in your Activity:

```java
@Inject
MyDependency dependency;
```

To have Dagger inject the dependency you need to hook the injection into the Activity's `onCreate` (as this is the first part of the lifecycle we have access to):

```java
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DaggerUtils.getComponent(this).inject(this);
}
```

For Fragment objects you should hook into the `onAttach` lifecycle method instead:

```java
@Override
public void onAttach(Context context) {
    super.onAttach(context);
    DaggerUtils.getComponent(context).inject(this);
}
```

### Swapping out dependencies in tests

To swap out depdendencies in a Robolectric test you can override the module the Application object uses to inject objects using provided helpers:

```java
@Before
public void setup() {
    MyDependency mocked = mock(MyDependency.class);
    RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
      @Override
      public MyDependency providesMyDependency() {
        return mocked;
      }
    });
}
```

## Code from external sources
ODK Collect is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0). Please make sure that any code you include is an OSI-approved [permissive license](https://opensource.org/faq#permissive). **Please note that if no license is specified for a piece of code or if it has an incompatible license such as GPL, using it puts the project at legal risk**.

Sites with compatible licenses (including [StackOverflow](http://stackoverflow.com/)) will sometimes provide exactly the code snippet needed to solve a problem. You are encouraged to use such snippets in ODK Collect as long as you attribute them by including a direct link to the source. In addition to complying with the content license, this provides useful context for anyone reading the code.

## Gradle sub modules

Collect is a multi module Gradle project. Modules should have a focused feature or utility (like "location" or "analytics") rather than represent an architectural "layer" (like "ui" or "backend"). Collect has an `androidshared` and `shared` module for simple utilities that might be useful in any Android or Java application respectively. Modules names should be lowercase and be dash seperated (like `test-forms` rather than `testforms` or `test_forms`).

### Adding a new module

There's no easy way to define exactly when a new module should be pulled out of an existing one or when new code calls for a new module - it's best to discuss that with the team before making any decisions. Once a structure has been agreed on, to add a new module:

1. Click `File > New > New module...` in Android Studio
2. Decide whether the new module should be an "Android Library" or "Java or Kotlin Library" - ideally as much code as possible could avoid relying on Android but a lot of features will require at least one Android Library module
3. Review the generated `build.gradle` and remove any unnecessary dependencies or setup
4. Add quality checks to the module's `build.gradle`:

  ```
  apply from: '../config/quality.gradle'
  ```

5. If the module will have tests, make sure they get run on CI by adding a line to `test_modules.txt` with `<module-name>` and if it's a non-Android module, registering the `testDebug` task in its `build.gradle` file:

  ```
  tasks.register("testDebug") {
    dependsOn("test")
  }
  ```
