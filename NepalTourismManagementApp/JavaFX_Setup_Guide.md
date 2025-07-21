# JavaFX Setup Guide

## Option 1: Quick Start (Recommended)

1. Install Java JDK 21 or later
2. Double-click the `download_and_run.bat` file
3. Follow the prompts to download and set up JavaFX
4. The application will compile and run automatically

## Option 2: Manual Setup with JavaFX SDK

1. Download JavaFX SDK from https://gluonhq.com/products/javafx/
2. Extract the ZIP file to a location on your computer (e.g., C:\javafx-sdk-21)
3. Run the application using the command:
   ```
   run_app.bat C:\path\to\javafx-sdk-21\lib
   ```
   (Replace the path with the actual path to your JavaFX lib folder)

## Option 3: Using an IDE (IntelliJ IDEA or Eclipse)

### IntelliJ IDEA

1. Open the project in IntelliJ IDEA
2. Go to File > Project Structure > Libraries
3. Click + and select "From Maven"
4. Search for "org.openjfx:javafx-controls:21" and add it
5. Add other JavaFX modules as needed (javafx-fxml, javafx-web, etc.)
6. Go to Run > Edit Configurations
7. Create a new Application configuration
8. Set the main class to "com.nepaltourismmanagementapp.TourismApp"
9. Add VM options: `--module-path "PATH_TO_FX" --add-modules javafx.controls,javafx.fxml`
   (Replace PATH_TO_FX with the path to your JavaFX lib folder)
10. Apply and run the configuration

### Eclipse

1. Open the project in Eclipse
2. Right-click on the project > Properties > Java Build Path > Libraries
3. Click "Add External JARs" and add all the JAR files from the JavaFX lib folder
4. Go to Run > Run Configurations
5. Create a new Java Application configuration
6. Set the main class to "com.nepaltourismmanagementapp.TourismApp"
7. Go to the Arguments tab
8. Add VM arguments: `--module-path "PATH_TO_FX" --add-modules javafx.controls,javafx.fxml`
   (Replace PATH_TO_FX with the path to your JavaFX lib folder)
9. Apply and run the configuration

## Troubleshooting

If you encounter errors about missing JavaFX classes:

1. Make sure you're using the correct version of JavaFX that matches your JDK version
2. Verify that the module-path points to the correct location of your JavaFX lib folder
3. Check that you've included all the necessary modules (javafx.controls, javafx.fxml, etc.)
4. Ensure your module-info.java file has the correct requires statements
