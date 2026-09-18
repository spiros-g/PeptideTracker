@echo off
setlocal

where java >nul 2>&1 || (
  echo ERROR: Java/JDK 17 is required.
  exit /b 1
)

if not defined ANDROID_HOME if not defined ANDROID_SDK_ROOT (
  echo ERROR: Set ANDROID_HOME or ANDROID_SDK_ROOT.
  exit /b 1
)

if not exist keystore.properties (
  echo ERROR: Create keystore.properties from keystore.properties.example before a signed production release.
  exit /b 1
)

if exist gradlew.bat (
  set "GRADLE_CMD=gradlew.bat"
) else (
  where gradle >nul 2>&1 || (
    echo ERROR: Gradle 8.11.1 is required and no Gradle wrapper is committed.
    exit /b 1
  )
  set "GRADLE_CMD=gradle"
)

echo [1/3] Running unit tests...
call %GRADLE_CMD% testDebugUnitTest --stacktrace
if errorlevel 1 exit /b 1

echo [2/3] Running release lint...
call %GRADLE_CMD% lintRelease --stacktrace
if errorlevel 1 exit /b 1

echo [3/3] Building signed release AAB...
call %GRADLE_CMD% bundleRelease --stacktrace
if errorlevel 1 exit /b 1

if not exist app\build\outputs\bundle\release\app-release.aab (
  echo ERROR: Release build completed without the expected AAB.
  exit /b 1
)

echo.
echo RELEASE AAB: app\build\outputs\bundle\release\app-release.aab
exit /b 0
