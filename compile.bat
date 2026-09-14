@echo off
setlocal
where java >nul 2>&1 || (echo ERROR: Java/JDK 17 is required.& exit /b 1)
if not defined ANDROID_HOME if not defined ANDROID_SDK_ROOT (echo ERROR: Set ANDROID_HOME or ANDROID_SDK_ROOT.& exit /b 1)
if exist gradlew.bat (set "GRADLE_CMD=gradlew.bat") else (where gradle >nul 2>&1 || (echo ERROR: Gradle is missing.& exit /b 1) & set "GRADLE_CMD=gradle")
call %GRADLE_CMD% testDebugUnitTest || exit /b 1
call %GRADLE_CMD% lintDebug || exit /b 1
call %GRADLE_CMD% assembleDebug || exit /b 1
echo APK: app\build\outputs\apk\debug\app-debug.apk
