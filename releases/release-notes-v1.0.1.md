# SMS Forwarder Plus v1.0.1

## Bugfix Release

SMS Forwarder Plus is an Android application that automatically forwards incoming SMS messages to a specified email address.

### What's Fixed

- **Navigation Bug**: Fixed an issue where users couldn't navigate back to the Home screen from the Settings page after testing connection or saving settings
- **Large SMS Handling**: Fixed issue with large SMS messages being split into multiple emails

### Features

- Forward SMS messages to email automatically
- Configure SMTP settings (host, port, SSL, username, password)
- Test SMTP connection before saving settings
- Run as a background service
- Start/stop service with a simple toggle
- Auto-start on device boot
- Modern, minimalist UI design

### Installation

1. Download the APK file
2. Install it on your Android device (you may need to enable "Install from unknown sources" in your device settings)
3. Grant the necessary permissions when prompted
4. Configure your SMTP settings
5. Start the service

### Requirements

- Android 7.0 (API level 24) or higher
- SMS permissions
- Internet connection 