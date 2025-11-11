<p align="center">
  <img src="assets/icon.svg" alt="2fa icon" width="150"/>
</p>
<h1 align="center">2fa</h1>
<h4 align="center">Manage one-time passwords securely, offline and without tracking</h4>
<p align="center">
    <a href="https://github.com/979st/2fa-android/releases/tag/vc-21">
        <img alt="Static Badge" src="https://img.shields.io/badge/latest-Beta%2012-blue">
    </a>
    <img alt="GitHub release downloads" src="https://img.shields.io/github/downloads/979st/2fa-android/total">
    <a href="https://crowdin.com/project/979-2fa">
        <img alt="Contribute to translations" src="https://badges.crowdin.net/979-2fa/localized.svg">
    </a>
    <a href="https://eupl.eu/1.2/en/">
        <img alt="EUPL" src="https://img.shields.io/badge/license-EUPL-blue">
    </a>
    <a href="https://developer.android.com/about/versions/15">
        <img alt="Minimum SDK Version" src="https://img.shields.io/badge/api-34%2B-blue">
    </a>
</p>
<p align="center">
    <a href="https://discord.com/invite/zxgXNzhYJu">
        <img alt="Join us on Discord" src="https://img.shields.io/badge/discord-join%20server-7289DA">
    </a>
    <a href="https://bsky.app/profile/979.st">
        <img alt="Follow on Bluesky" src="https://img.shields.io/badge/bluesky-follow-1DA1F2">
    </a>
</p>
<p align="center">
    <a href="https://accrescent.app/app/app.ninesevennine.twofactorauthenticator.accrescent">
        <img alt="Get it on Accrescent" src="https://accrescent.app/badges/get-it-on.png" height="80">
    </a>
</p>
<p align="center">
    <img src="assets/light_portrait.png" alt="light_portrait" width="17%"/>
    <img src="assets/dark_portrait.png" alt="light_portrait" width="17%"/>
    <img src="assets/light_landscape.png" alt="light_landscape" width="60%"/>
</p>

## Features
### Time-based One-Time Password (TOTP)
- Customizable refresh intervals starting at 10 seconds  
- Supports 4–10 digit codes  
- Hash algorithms: SHA-1, SHA-256, SHA-512  

### HMAC-based One-Time Password (HOTP)
- Supports 4–10 digit codes  
- Hash algorithms: SHA-1, SHA-256, SHA-512  

### Secure Vault
- All 2fa secrets are stored locally on the device  
- Encrypted and authenticated using AES-GCM
- Encryption keys are stored in the Android Keystore, with StrongBox hardware protection when supported  
- No cloud or external servers involved

### Backup & Restore 2fa Codes
- Create encrypted backups to external storage (SD card, USB drive, or Files)  
- Protected with a password using Argon2id for key derivation  
- Backups are encrypted and authenticated with ChaCha20-Poly1305

### Transfering 2fa codes
- Export to Google Authenticator via QR code
- Import from Aegis Authenticator via json

### Honorable mentions
- Works fully without Google Play Services

## Verifying APKs
All official release APKs are signed with our release key. Signing certificate fingerprints:

- SHA-256: `e5b28ea1e051c232dea94c27c0592e2c002d17c5ca81788b51306d608afe52c5`
- SHA-256 (AppVerifier): `E5:B2:8E:A1:E0:51:C2:32:DE:A9:4C:27:C0:59:2E:2C:00:2D:17:C5:CA:81:78:8B:51:30:6D:60:8A:FE:52:C5`

We recommend verifying downloads using either [AppVerifier](https://github.com/soupslurpr/AppVerifier) or Android's official apksigner tool.

## Development Roadmap
- ~~Alpha: Add essential features~~
- Beta: Refine the user interface, improve usability, add animations, integrate user feedback, ~~and publish on [Accrescent](https://accrescent.app/)~~
- Release Candidate: Prepare for a full launch on the Google Play Store

## License
<a href="https://interoperable-europe.ec.europa.eu/collection/eupl/eupl-text-eupl-12">
    <img alt="License: EUPL" src="assets/EUPL.svg" height="80">
</a>

This project is licensed under the [European Union Public License (EUPL)](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12) -  see the LICENSE file for details.