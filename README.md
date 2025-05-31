# TimerApp

A lightweight, customizable desktop timer application built with Java Swing. TimerApp allows users to set countdown timers with hours, minutes, and seconds, featuring a sleek, modern interface with system tray integration, customizable alerts, and optional system shutdown on timer completion.

![Screenshot](/other_imgs/image.png)

## Features

- **Customizable Timer**: Set timers using intuitive spinners for hours (0-99), minutes (0-59), and seconds (0-59).
- **Progress Bar**: Visual countdown progress with a percentage display.
- **Alerts**: Visual (window blinking) and audio (beep) alerts when the timer completes, with a toggle to enable/disable sound.
- **System Tray Integration**: Minimize to system tray with options to restore or exit the application.
- **Drag-and-Drop Window**: Move the undecorated window by dragging anywhere on the background.
- **Double-Click to Stop Alerts**: Quickly stop alerts by double-clicking the window.
- **Always On Top**: Option to keep the window on top of other applications.
- **Shutdown on Complete**: Schedule system shutdown when the timer finishes, with a 20-second countdown dialog to cancel.
- **Idle Transparency**: Window becomes semi-transparent after 5 seconds of inactivity, returning to full opacity on interaction.
- **Startup Shortcut**: Automatically creates a Windows startup shortcut for convenience.
- **Modern UI**: Powered by FlatLaf for a clean, rounded, and shadowed interface.

## Prerequisites

- **Java 17 or later**: Ensure you have a compatible JDK installed (e.g., [Amazon Corretto 17](https://aws.amazon.com/corretto/)).
- **Maven**: Required for building the project and managing dependencies.

## Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/TimerApp.git
   cd TimerApp
   ```

2. **Build the Project**:
   Use Maven to compile and package the application:
   ```bash
   mvn clean package
   ```

3. **Run the Application**:
   Execute the JAR file generated in the `target` directory:
   ```bash
   java -jar target/TimerApp-1.0-SNAPSHOT-jar-with-dependencies.jar
   ```

   Alternatively, run directly via Maven:
   ```bash
   mvn exec:java -Dexec.mainClass="org.TimerApp.TimerApp"
   ```

## Usage

1. **Set the Timer**:
   - Use the spinners to set hours, minutes, and seconds.
   - Click **START** to begin the countdown.

2. **Control the Timer**:
   - **PAUSE**: Temporarily stop the timer.
   - **RESET**: Clear the timer and reset to zero.

3. **Context Menu**:
   Right-click the window to access options:
   - **Minimize to Tray**: Hide the window to the system tray.
   - **Toggle Always On Top**: Keep the window above others.
   - **Toggle Sound**: Enable/disable beep alerts.
   - **Shutdown on Timer Complete**: Schedule a system shutdown when the timer finishes.

4. **System Tray**:
   - Double-click the tray icon to restore the window.
   - Right-click the tray icon for **Show** or **Exit** options.

5. **Stop Alerts**:
   Double-click the window background to stop blinking and beeping when the timer completes.

6. **Shutdown Dialog**:
   If "Shutdown on Timer Complete" is enabled, a dialog appears with a 20-second countdown when the timer finishes. Click **Cancel Shutdown** to abort.

## Project Structure

- `src/main/java/org/TimerApp/`:
  - `TimerApp.java`: Entry point for the application.
  - `TimerUI.java`: Handles the GUI, mouse events, and context menu.
  - `TimerLogic.java`: Manages timer countdown, progress, and shutdown logic.
  - `AlertManager.java`: Controls visual and audio alerts.
  - `SystemTrayManager.java`: Manages system tray integration.
  - `AppUtilities.java`: Creates a Windows startup shortcut.
- `src/main/resources/icon.png`: Application icon (ensure this file exists).
- `pom.xml`: Maven configuration with dependencies (FlatLaf, etc.).

## Dependencies

- [FlatLaf 3.0](https://www.formdev.com/flatlaf/): Modern look and feel for Swing.
- [FlatLaf Extras 3.0](https://www.formdev.com/flatlaf/): Additional FlatLaf components.
- Java 17 (Maven compiler source/target).



