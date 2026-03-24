@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.2.0
@REM ----------------------------------------------------------------------------

@REM [SEMINAR-19] Infra tooling: одинаковая сборка на Windows/Linux — меньше "магии" и больше фокуса на архитектуре.

@echo off
setlocal

if "%MAVEN_SKIP_RC%"=="" (
  if exist "%USERPROFILE%\.mavenrc" call "%USERPROFILE%\.mavenrc"
)

set WRAPPER_DIR=%~dp0\.mvn\wrapper
set WRAPPER_JAR=%WRAPPER_DIR%\maven-wrapper.jar
set WRAPPER_PROPERTIES=%WRAPPER_DIR%\maven-wrapper.properties

if not exist "%WRAPPER_JAR%" (
  for /f "usebackq delims== tokens=1,2" %%A in ("%WRAPPER_PROPERTIES%") do (
    if "%%A"=="wrapperUrl" set WRAPPER_URL=%%B
  )
  if "%WRAPPER_URL%"=="" set WRAPPER_URL=https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar

  if not exist "%WRAPPER_DIR%" mkdir "%WRAPPER_DIR%"

  powershell -Command "& { (New-Object Net.WebClient).DownloadFile('%WRAPPER_URL%', '%WRAPPER_JAR%') }"
  if errorlevel 1 (
    echo ERROR: Failed to download Maven Wrapper jar from %WRAPPER_URL%
    exit /b 1
  )
)

if "%JAVA_HOME%"=="" (
  set JAVA_EXE=java
) else (
  set JAVA_EXE=%JAVA_HOME%\bin\java
)

"%JAVA_EXE%" %MAVEN_OPTS% ^
  -classpath "%WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%~dp0." ^
  org.apache.maven.wrapper.MavenWrapperMain %*

endlocal
