@echo off
ECHO Nepal Tourism Management App Launcher
ECHO ===================================
ECHO.

REM Check if JavaFX SDK path is provided as argument
IF "%1"=="" (
    ECHO Please download JavaFX SDK from https://gluonhq.com/products/javafx/
    ECHO Then run this batch file with the path to the JavaFX SDK lib folder
    ECHO.
    ECHO Example: run_app.bat C:\path\to\javafx-sdk-21\lib
    ECHO.
    SET /P JAVAFX_PATH=Enter path to JavaFX SDK lib folder: 
) ELSE (
    SET JAVAFX_PATH=%1
)

ECHO Using JavaFX from: %JAVAFX_PATH%
ECHO.

REM Check if the JavaFX path exists
IF NOT EXIST "%JAVAFX_PATH%" (
    ECHO Error: JavaFX SDK lib folder not found at %JAVAFX_PATH%
    ECHO Please download JavaFX SDK from https://gluonhq.com/products/javafx/
    ECHO.
    PAUSE
    EXIT /B 1
)

ECHO Creating output directory...
IF NOT EXIST "target\classes" MKDIR target\classes

ECHO Compiling application...
javac -d target/classes ^
    --class-path "%JAVAFX_PATH%\javafx.base.jar;%JAVAFX_PATH%\javafx.controls.jar;%JAVAFX_PATH%\javafx.fxml.jar;%JAVAFX_PATH%\javafx.graphics.jar" ^
    src/main/java/com/nepaltourismmanagementapp/TourismApp.java ^
    src/main/java/com/nepaltourismmanagementapp/model/*.java ^
    src/main/java/com/nepaltourismmanagementapp/controller/*.java ^
    src/main/java/com/nepaltourismmanagementapp/utils/*.java

IF %ERRORLEVEL% NEQ 0 (
    ECHO.
    ECHO Compilation failed!
    PAUSE
    EXIT /B 1
)

ECHO.
ECHO Running application...
java --module-path "%JAVAFX_PATH%" ^
    --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
    -cp target/classes com.nepaltourismmanagementapp.TourismApp

PAUSE