
# Compass Application

## Overview

The Compass Application is an Android application that provides a compass functionality, allowing users to determine their orientation relative to magnetic north. It utilizes sensors such as accelerometer, compass, and gyroscope to provide accurate readings.

## Features

-  Provides a real-time compass display indicating the user's orientation.
- Provides options to customize appearance and settings.
- Supports haptic feedback for better user experience.
- Indicates sensor accuracy and true north indication.
-  Customizable appearance with adjustable text sizes and colors.
-  Integration with sensors including accelerometer, compass, and gyroscope.
-  ViewModel architecture for managing compass data and functionality.
-  LiveData objects for observing azimuth angle, sensor accuracy, and more.

## Permissions

The application requires the following permissions:

- `ACCESS_COARSE_LOCATION`: Allows the app to access approximate location.
- `ACCESS_FINE_LOCATION`: Allows the app to access precise location.

## Installation

To install the Compass Application on your Android device:

1. Clone the repository to your local machine using the following command:
```
 git clone https://github.com/wenubey/compass-application.git
 ```
2. Open Android Studio and select "Open an existing Android Studio project".
3. Navigate to the location where you cloned the repository and select the `compass-application` directory.
4. Android Studio will import the project. Wait for the process to complete.
5. Connect your Android device to your computer and ensure USB debugging is enabled.
6. Build and run the application on your device by clicking the "Run" button in Android Studio.

## Usage

1. Upon launching the application, the main activity displays the compass interface.
2. The azimuth angle is updated in real-time as the user's orientation changes.
3. Customize appearance and settings as needed.
4. Interact with the compass view to set azimuth manually or enable/disable features.

## Dependencies

The Compass Application relies on the following dependencies:

- Kotlin: The programming language used for implementation.
- AndroidX: Jetpack libraries for modern Android development.
- Android Architecture Components: Provides LiveData and ViewModel for managing UI-related data.
- Google Play Services: Provides access to location services for accurate compass readings.

## Visualization

<div style="display:flex">
    <img src="https://github.com/wenubey/CompassApp/blob/readme-md/app/src/main/assets/Screenshot_1.png" alt="not found" width="240" height="480">
    <img src="https://github.com/wenubey/CompassApp/blob/readme-md/app/src/main/assets/Screenshot_2.png" alt="not found" width="240" height="480">
    <img src="https://github.com/wenubey/CompassApp/blob/readme-md/app/src/main/assets/Screenshot_3.png" alt="not found" width="240" height="480">
</div>