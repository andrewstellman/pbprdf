@echo off

rem Set the lib dir relative to the batch file's directory
set LIB_DIR=%~dp0\target\scala-2.11
rem echo LIB_DIR = %LIB_DIR%

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set CMD_LINE_ARGS=%1
if ""%1""=="""" goto setupArgsEnd
shift
:setupArgs
if ""%1""=="""" goto setupArgsEnd
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setupArgs

:setupArgsEnd

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
goto javaHome

:noJavaHome
set JAVA=java
goto javaHomeEnd

:javaHome
set JAVA=%JAVA_HOME%\bin\java

:javaHomeEnd

:checkJdk18
"%JAVA%" -version 2>&1 | findstr "1.8" >NUL
IF ERRORLEVEL 0 goto java8
echo Java 8 or newer required to run pbprdf
goto end

:java8
rem use java 6+ wildcard feature
rem echo Using wildcard to set classpath
"%JAVA%" -cp "%LIB_DIR%\*" com.stellmangreene.pbprdf.PbpRdfApp %CMD_LINE_ARGS%
goto end

:end

