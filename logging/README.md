This library project contains code related to logging on Android. It currently uses the [Timber](https://github.com/JakeWharton/timber) library to create logging trees for various build environments.


## Trees ##

[LogcatLoggingTree](src/main/java/com/quizlet/android/logging/LogcatLoggingTree.java)

Forwards all logging calls to `android.util.Log.printLn()`. Rethrows exceptions above Log.WARN priority. Only install in debug builds, so we don't crash in production.


[ConsoleLoggingTree](src/main/java/com/quizlet/android/logging/ConsoleLoggingTree.java)

Forwards all logging calls to `System.out.println()`. Also prints stacktrace for any passed exceptions. Installed in unit tests where printing to logcat doesn't work.


[CrashlyticsLoggingTree](src/main/java/com/quizlet/android/logging/CrashlyticsLoggingTree.java)

Forwards all logging calls to `Crashlytics.log()`. Also forwards exceptions to `Crashlytics.logException()`. Only logs statements at Log.INFO priority or higher. Installed in release builds.


## Usage ##

Install LogcatLoggingTree in debug builds, and CrashlyticsLoggingTree in release builds.
Install ConsoleLoggingTree in unit tests to have all logs and exceptions printed to the console.
Pass exceptions to Timber.e() to crash in debug builds, for issues that should be fixed during testing. Logging to Timber.e() without an exception will not cause a crash. Log to Timber.w() or lower for errors that should not crash in debug builds.
Use Timber.i() for important information to be included in Crashlytics logs.
Use Timber.d() or Timber.v() for normal debug logging.
