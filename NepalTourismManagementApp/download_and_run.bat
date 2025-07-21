@echo off
ECHO Nepal Tourism Management App Setup and Launcher
ECHO =============================================
ECHO.

SET JAVAFX_VERSION=21.0.2
SET DOWNLOAD_DIR=%TEMP%\javafx-sdk
SET JAVAFX_SDK_DIR=%DOWNLOAD_DIR%\javafx-sdk-%JAVAFX_VERSION%
SET JAVAFX_LIB_DIR=%JAVAFX_SDK_DIR%\lib

REM Check if JavaFX SDK is already downloaded
IF EXIST "%JAVAFX_LIB_DIR%" (
    ECHO JavaFX SDK found at: %JAVAFX_LIB_DIR%
) ELSE (
    ECHO JavaFX SDK not found. Would you like to download it? (Y/N)
    SET /P DOWNLOAD_CHOICE=
    
    IF /I "%DOWNLOAD_CHOICE%"=="Y" (
        ECHO.
        ECHO Creating download directory...
        IF NOT EXIST "%DOWNLOAD_DIR%" MKDIR "%DOWNLOAD_DIR%"
        
        ECHO.
        ECHO Please download JavaFX SDK from: https://gluonhq.com/products/javafx/
        ECHO Download the JavaFX Windows SDK (version %JAVAFX_VERSION% or newer)
        ECHO.
        ECHO After downloading, extract the ZIP file to: %DOWNLOAD_DIR%
        ECHO.
        ECHO Press any key when you have completed this step...
        PAUSE > NUL
        
        IF NOT EXIST "%JAVAFX_LIB_DIR%" (
            ECHO.
            ECHO JavaFX SDK not found at expected location.
            ECHO Please enter the full path to the JavaFX lib directory:
            SET /P JAVAFX_LIB_DIR=
        )
    ) ELSE (
        ECHO.
        ECHO Please enter the full path to your JavaFX lib directory:
        SET /P JAVAFX_LIB_DIR=
    )
)

ECHO.
ECHO Using JavaFX from: %JAVAFX_LIB_DIR%
ECHO.

REM Check if the JavaFX path exists
IF NOT EXIST "%JAVAFX_LIB_DIR%" (
    ECHO Error: JavaFX SDK lib folder not found at %JAVAFX_LIB_DIR%
    ECHO.
    PAUSE
    EXIT /B 1
)

ECHO Creating output directory...
IF NOT EXIST "target\classes" MKDIR target\classes

ECHO.
ECHO Compiling application...
javac -d target/classes ^
    --class-path "%JAVAFX_LIB_DIR%\javafx.base.jar;%JAVAFX_LIB_DIR%\javafx.controls.jar;%JAVAFX_LIB_DIR%\javafx.fxml.jar;%JAVAFX_LIB_DIR%\javafx.graphics.jar" ^
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
java --module-path "%JAVAFX_LIB_DIR%" ^
    --add-modules javafx.controls,javafx.fxml,javafx.graphics ^
    -cp target/classes com.nepaltourismmanagementapp.TourismApp

PAUSE