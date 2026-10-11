Built by Pragati Gurav, final-year project, 2025
# Jewelry AR: Virtual Try-On Android App

An Android jewelry shopping app where users can virtually try on earrings, necklaces and rings using their phone camera before buying. Built as my final-year project.

## Features
- User authentication: register, login, forgot password (reset), logout
- Product catalog loaded from a PHP/MySQL backend, with images
- Real-time AR try-on using the camera:
  - **Earrings**: placed on both earlobes, scaled to face width
  - **Necklace**: placed below the chin, scaled to face width
  - **Rings**: placed on a chosen finger of either hand (thumb, index, middle, ring, little), rotated to follow the finger
- Front and back camera switch
- Themed gold-and-dark UI with splash screen

<!-- Add screenshots later:
## Screenshots
| Login | Catalog | Ring try-on |
|---|---|---|
| ![Login](screenshots/login.png) | ![Catalog](screenshots/catalog.png) | ![Ring](screenshots/ring.png) |
-->

## Tech stack
| Part | Technology |
|---|---|
| Android app | Java, XML, Material Components |
| Camera | CameraX |
| Face and hand tracking | Google MediaPipe Tasks (Face Landmarker, Hand Landmarker) |
| Networking and images | Retrofit, Glide |
| Backend | PHP (PDO, prepared statements), MySQL |
| Local server | XAMPP |

## How the try-on works
1. CameraX streams frames from the phone camera.
2. MediaPipe detects landmarks: 478 face points or 21 hand points per hand.
3. Landmark positions are converted to screen coordinates.
4. A transparent jewelry image is drawn on an overlay view at those positions. Size comes from face width or finger length, and rings are rotated to the finger's angle.
5. This repeats for every frame so the jewelry follows the user.

## Project structure
```
app/        Android application (Java, XML)
backend/    PHP REST API (login, register, products, reset_password)
database.sql   MySQL tables and sample products
```

## Setup

### 1. Backend
1. Install [XAMPP](https://www.apachefriends.org) and start Apache and MySQL.
2. Copy the `backend` folder to `C:\xampp\htdocs\` and rename it `jewelry_api`.
3. Open phpMyAdmin, go to the SQL tab, and run `database.sql`.
4. Add your own transparent PNGs to `jewelry_api/images/` named `earring1.png`, `necklace1.png` and `ring1.png`. Jewelry images are not included in this repo.
5. Test `http://localhost/jewelry_api/products.php` in a browser.

### 2. Android app
1. Open the project in Android Studio.
2. In `ApiClient.java`, set `BASE_URL` to your computer's IP, for example `http://192.168.1.5/jewelry_api/`. Your phone and computer must be on the same Wi-Fi.
3. Connect a phone, or build an APK, and run.
4. Register an account, log in, and tap a product to try it on.

The MediaPipe model files are already in `app/src/main/assets/`.

## Limitations
- The try-on is a 2D overlay, so jewelry does not rotate in 3D with head turns.
- Password reset has no email or OTP verification (simplified for a college project).
- The backend runs on a local XAMPP server, so the app needs it running on the same network.
- The app sends the email and password over plain HTTP in local testing. A production app would use HTTPS.

## Future scope
- 3D jewelry models for realistic try-on
- Email or OTP verification for password reset
- Cart, orders and payment
- Cloud-hosted backend with HTTPS and token-based login

## Credits
- Face and hand tracking by [Google MediaPipe](https://developers.google.com/mediapipe) (Apache License 2.0). The model files `face_landmarker.task` and `hand_landmarker.task` come from MediaPipe.
- Jewelry images are not included; use your own images or ones you have the right to use.