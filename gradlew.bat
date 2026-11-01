@rem Gradle startup script for Windows
@if "%DEBUG%"=="" @echo off
set GRADLE_OPTS=%GRADLE_OPTS% "-Xmx512m" "-Xms256m"
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%gradle\wrapper\gradle-wrapper.jar
java -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
