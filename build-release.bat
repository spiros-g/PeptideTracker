@echo off
setlocal
if not exist keystore.properties (echo ERROR: Create keystore.properties from the example before a signed production release.& exit /b 1)
if exist gradlew.bat (call gradlew.bat bundleRelease) else (call gradle bundleRelease)
