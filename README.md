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
  Quiet Light — selectable via a palette grid (same pattern as Aura
  Launcher's theme picker).
- Adjustable font size, line numbers toggle, word wrap toggle.

## Tech stack

- Kotlin + Jetpack Compose (Material 3)
- AndroidX DataStore for settings persistence
- DocumentFile / SAF for file system access (no broad storage permission
  required on Android 11+)

## Building

```sh
./gradlew assembleDebug
```

The debug APK is signed with the same key as release builds when
`keystore.properties` is present (see `keystore.properties.template`),
mirroring the Aura Launcher build setup.

## Roadmap

- Multi-window / split editor panes
- Search across files
- Git integration
- Extension-style plugin system
