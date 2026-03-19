# EzTutorial

A configurable tutorial system for Paper servers.
Versions: 1.21.X

## Features

- First-join tutorial prompt
- Supports GUI and chat-based tutorials
- Configurable task system (`MOVE_TO`, `COMMAND`)
- Multiple storage options: YAML, SQLite, MySQL
- Player progress tracking
- Placeholder support (`%player_name%`, `%eztutorial_progress%`)
- Optional PlaceholderAPI integration
- Configurable GUI

## Commands

### Player Commands
- `/tutorial` – Open or resume the tutorial
- `/tutorial start` – Start the tutorial
- `/tutorial resume` – Continue the tutorial
- `/tutorial status` – Show current progress
- `/tutorial skip` – Skip the tutorial (if enabled)

### Admin Commands
- `/eztutorial help`
- `/eztutorial reload`
- `/eztutorial start <player>`
- `/eztutorial reset <player>`
- `/eztutorial status <player>`

## Setup

1. Place the plugin `.jar` into your `/plugins` folder
2. Start your server
3. Configure the plugin in `/plugins/EzTutorial/`
4. Reload or restart the server

## Configuration

The plugin is fully configurable via multiple files:

- `config.yml` – General settings and display modes
- `messages.yml` – All messages
- `gui.yml` – GUI layout and items
- `tutorial.yml` – Tutorial flow and tasks
- `storage.yml` – Storage configuration

## 📄 Information

[![Author](https://img.shields.io/badge/Author-irgendein--mensch-blue)](https://github.com/irgendein-mensch)
[![Discord](https://img.shields.io/discord/1320723057664589925?label=Support%20Server)](https://discord.gg/soulshine)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)