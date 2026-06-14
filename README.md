# VSCodium Mobile

A native Android code editor inspired by [VSCodium](https://vscodium.com),
built with Jetpack Compose and reusing the design system and palette
architecture from [Aura Launcher](https://github.com/BorgorNinja/aura-launcher).

This is **not** an Electron port — VSCodium's desktop runtime can't run on
Android. Instead, this project recreates the core editing experience as a
lightweight native app, with VS Code-inspired color themes and UI patterns.

## Features

- **Folder-based editing** via Storage Access Framework — open any directory
  and browse its file tree.
- **Tabbed editor** with dirty-state indicators and per-tab save.
- **Syntax highlighting** (regex-based) for Kotlin, Java, JavaScript,
  TypeScript, Python, C/C++, JSON, XML, HTML, CSS, Markdown, Shell, YAML.
- **9 built-in color themes**, ported from popular VS Code themes:
  Dark+, Light+, Monokai, Dracula, Solarized Dark, Nord, One Dark, Night Owl,
  Quiet Light - selectable via a palette grid (same pattern as Aura
  Launcher's theme picker).
- **Integrated terminal** powered by Termux - a persistent shell session
  embedded in the editor UI, with clean monospace output and a `clear`
  command that works as expected.
- Adjustable font size, line numbers toggle, word wrap toggle.

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- AndroidX DataStore for settings persistence
- DocumentFile / SAF for file system access (no broad storage permission
  required on Android 11+)
- Termux + ncat for the integrated terminal bridge

## Building

```sh
./gradlew assembleDebug
```

The debug APK is signed with the same key as release builds when
`keystore.properties` is present (see `keystore.properties.template`),
mirroring the Aura Launcher build setup.

## Integrated Terminal Setup (Termux)

The terminal panel connects to a live Termux shell session via a local TCP
bridge. Termux must be installed and configured before the terminal feature
will work.

### Step 1 - Install Termux

Install from **F-Droid or GitHub Releases** only. The Play Store version is
outdated and will not work correctly.

- F-Droid: https://f-droid.org/packages/com.termux/
- GitHub: https://github.com/termux/termux-app/releases

### Step 2 - Enable external app access

Open Termux and run:

```bash
mkdir -p ~/.termux
echo "allow-external-apps = true" >> ~/.termux/termux.properties
termux-reload-settings
```

This allows VSCodium Mobile to send `RUN_COMMAND` intents to Termux and is
the only configuration Termux requires. No ADB or root is needed.

### Step 3 - Install ncat

The terminal bridge uses ncat (part of nmap) to relay the shell session:

```bash
pkg install nmap
```

That's it. Open VSCodium Mobile, tap the terminal icon, and press the
connect button.

### How the bridge works

Android enforces per-UID loopback isolation: TCP connections between two
different app UIDs on `127.0.0.1` are silently dropped at the kernel level
on many devices and ROMs (confirmed on Infinix X688B / Android 11 and others).

The bridge works around this by:

1. Sending a `RUN_COMMAND` intent to Termux, which starts:
   ```
   ncat -l 0.0.0.0 12399 -e bash
   ```
2. VSCodium Mobile then connects to port `12399` via the device's own local
   network IP address (WiFi/cellular) rather than `127.0.0.1`, routing
   through `wlan0`/`rmnet0` instead of `lo` and bypassing the UID filter.

> **Note:** Port `12399` is open on all local network interfaces while a
> terminal session is active. Avoid using the terminal on untrusted public
> networks.

### Troubleshooting

| Symptom | Likely cause | Fix |
|---------|--------------|-----|
| Status immediately shows **Error** | `allow-external-apps` not set | Step 2 |
| Commands sent but no output appears | `ncat`/`nmap` not installed | Step 3 |
| Port already in use on reconnect | Previous ncat session still running | Run `pkill ncat` in Termux |
| Terminal works on WiFi but not mobile data | Device IP changed | Disconnect and reconnect |

## Roadmap

- Multi-window / split editor panes
- Search across files
- Git integration
- Extension-style plugin system
- ANSI colour rendering in the terminal panel
