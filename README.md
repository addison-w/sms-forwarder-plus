# SMS Forwarder Plus

SMS Forwarder Plus is an Android application that automatically forwards incoming SMS messages to a specified email address. It's built using Kotlin and follows a clean, minimal design approach.

## Features

- Forward SMS messages to email automatically
- Configure SMTP settings (host, port, SSL, username, password)
- Test SMTP connection before saving settings
- Run as a background service
- Start/stop service with a simple toggle
- Auto-start on device boot
- Minimal and intuitive UI

## Requirements

- Android 7.0 (API level 24) or higher
- SMS permissions
- Internet connection

## Setup

1. Install the app on your Android device
2. Grant SMS permissions when prompted
3. Configure your SMTP settings:
   - SMTP Host (e.g., smtp.gmail.com)
   - SMTP Port (e.g., 587 for TLS, 465 for SSL)
   - Username (your email username)
   - Password (your email password or app password)
   - Sender Email (the email address messages will be sent from)
   - Recipient Email (the email address messages will be sent to)
   - SSL toggle (enable for SSL connections)
4. Test the connection to verify your settings
5. Save your settings
6. Toggle the service ON to start forwarding SMS messages

## Gmail Users

If you're using Gmail, you'll need to:
1. Enable 2-Step Verification for your Google account
2. Generate an App Password specifically for this app
3. Use that App Password instead of your regular Gmail password

## Troubleshooting

If the test connection fails, check:
- Your internet connection
- SMTP host and port are correct
- Username and password are correct
- If using Gmail, ensure you're using an App Password
- SSL/TLS settings match your email provider's requirements

## Privacy

This app does not collect or transmit any data other than the SMS messages you choose to forward. All settings are stored locally on your device.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 