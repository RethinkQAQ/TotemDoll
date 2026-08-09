<div align="center">
  <img src="common/src/main/resources/itemdoll.png" width="96" alt="Totem Doll icon">
  <h1>Totem Doll</h1>
  <p>
    Totem Doll is a client-side Minecraft mod based on my
    <a href="https://www.bilibili.com/video/BV1Zw411S7DS/">original Totem Doll resource pack</a>,
    turning the Totem of Undying into customizable Minecraft-style dolls and animated models.
  </p>
</div>

[![GitHub License](https://img.shields.io/github/license/RethinkQAQ/TotemDoll)](http://www.gnu.org/licenses/lgpl-3.0.html)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/CcfJqGL2?label=Modrith%20Download&color=green)](https://modrinth.com/mod/item-tool)
[![CurseForge](https://cf.way2muchnoise.eu/full_1641634_downloads.svg)](https://legacy.curseforge.com/minecraft/mc-mods/totemdoll)
[![MC Versions](https://cf.way2muchnoise.eu/versions/For%20MC_1641634_all.svg)](https://legacy.curseforge.com/minecraft/mc-mods/totemdoll)

[English](README.md) | [中文](README_CN.md)

## Overview

Totem Doll lets you replace the appearance of the Totem of Undying with selectable Minecraft-style dolls and other custom models. Built-in styles, custom skins, dynamic textures, and bone animations can all be used without changing the Totem's original survival effect.

### Idle animation in hand

![Idle animation while holding the Totem](docs/img/onHand.gif)

The doll can play an idle animation while held by the player.

### Totem activation animation

![Animation when the Totem activates](docs/img/onDie.gif)

The doll can play a special animation when the Totem of Undying is activated.

### Configuration screen animation

![Animation when opening the configuration screen](docs/img/onScreen.gif)

Some styles can play an animation when the F9 configuration screen is opened.

## Features

- Built-in Alex, Steve, and other Minecraft-style doll templates
- Dynamic textures, such as blinking.
- Bone-based models and animations
- Rendering in first person, third person, the inventory, dropped items, and item frames
- Custom player skins and locally saved personal styles
- ZIP style-pack import
- Client-side only; the server does not need to install Totem Doll
- The Totem of Undying keeps its original survival behavior

## Usage

1. Install the Fabric or NeoForge version matching your Minecraft version.
2. Launch the game and press `F9` to open the Totem Doll configuration screen.
3. Choose a built-in template or one of your personal styles.
4. Click **Use** to apply the selected style immediately.

To create a personal style, choose a template that supports custom skins, select a standard 64×64 Minecraft skin PNG, enter a name, and save it. Personal styles are stored locally and remain available after restarting the game.

To import a style pack, use **Import Style Pack** in the configuration screen and select a ZIP file. Imported styles can be used directly or used as templates for personal styles.

## Compatibility

| Platform | Supported versions |
| --- | --- |
| Fabric | Minecraft 1.21–1.21.11 |
| NeoForge | Minecraft 1.21–1.21.11 |

## Documentation

- [English documentation](docs/en/README.md)
- [中文文档](docs/zh/README.md)
- [Style pack template](style-template/README.md)
