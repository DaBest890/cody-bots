# OSRS Autopilot

An experimental Java automation project built with the RuneMate Gradle plugin. The current implementation centers on a configurable woodcutting agent.

> **Development status:** Active work is maintained on the [`Development-Branch`](../../tree/Development-Branch). The default branch contains the current baseline implementation.

## Current Components

- `SimpleWoodcutter` agent entry point
- Configurable tree types and woodcutting settings
- State-based woodcutting workflow
- Gradle wrapper for repeatable builds
- RuneMate development-mode configuration

## Technology

- Java
- Gradle with Kotlin DSL
- RuneMate Gradle plugin

## Build

```bash
./gradlew build
```

On Windows:

```powershell
.\gradlew.bat build
```

The configured `runClient` task builds the project before opening the development client.

## Credential Safety

RuneMate submission tokens should be stored outside the repository in the user-level Gradle properties file. Do not commit authentication tokens or account credentials.

## Responsible Use

This repository is presented as an automation and state-machine programming project. Anyone using or extending it is responsible for following the rules and terms of any third-party game or platform.
