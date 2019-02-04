# Padfoot
Padfoot is a activity tracking application for four-legged friends, developed for my [Ada Developers Academy](https://www.adadevelopersacademy.org/) capstone project.

You can find a copy of my presentation, including a demo, [here](https://docs.google.com/presentation/d/194CYs_QVgdxTuEOwFOGOsLW1hw3vMLnqcob7OLrou4g/edit?usp=sharing).  

This repository contains the source code for the Padfoot Android application, which can be run and installed through Android Studio. Its [sister repository](https://github.com/jacquelynoelle/padfoot_tracker) contains the code and instructions for building the accompanying pedometer.

## Running Padfoot
Please note that you will not be able to observe Padfoot's step counting features without the physical pedometer. 

1. [Download Android Studio](https://developer.android.com/studio/)
2. Clone this repository with the following command 'git clone https://github.com/jacquelynoelle/padfoot.git'
3. Open Android Studio and select Import Project, then choose the folder into which you cloned this repo.
4. Connect an Android phone running Android Nougat (APK 23) or higher to your computer. (You can also use an emulator to run Padfoot on a virtual device, however, you will not be able to use any of the features that require Bluetooth LE).
5. Click the green Run button in the menu bar and select the device you plugged in to build the project and install the app on your phone. The app should open automatically. 

Note that running this application requires the following dependencies, which should be included in the app's gradle file and will be installed the first time you build the project:
- appcompat-v7:28.0.0
- cardview-v7:28.0.0
- recyclerview-v7:28.0.0
- firebase-core:16.0.6
- firebase-database:16.0.5
- firebase-auth:16.1.0
- firebase-ui-auth:4.3.0
- firebase-ui-database:4.3.1
- play-services-auth:16.0.1
- [MPAndroidChart:v3.1.0-alpha](https://github.com/PhilJay/MPAndroidChart)

## Additional Links
- Padfoot's original [product plan](https://gist.github.com/jacquelynoelle/4735b04382520b79c34f728fb57be00e)
- [Trello board](https://trello.com/b/kCfnk6eU/jackie-c-capstone) used for sprint planning during development
