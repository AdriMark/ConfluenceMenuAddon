# Confluence Menu Addon

Puts [Confluence: Otherworld](https://github.com/Magic-team-jvav/confluence)'s animated
achievement-screen background on the main menu and a few other menu
screens.

Available on Curseforge and Modrinth:
https://www.curseforge.com/minecraft/mc-mods/confluence-main-menu/preview
https://modrinth.com/mod/confluence-main-menu

Requires **Confluence: Otherworld** (obviously). 
Currently 1.21.1 neoforge only, will be ported to 1.20.1 forge alongside Confluence.

## Config

Hit the **Config** button on the mods list, or edit `config/confluencemenuaddon-client.toml`
directly:

```toml
[menus]
enableMainMenu = true
enableWorldSelectScreen = true
enableMultiplayerScreen = true
enableOptionsScreen = true
enableCreateWorldScreen = true
enableModListScreen = true
enableConfigScreen = true
enableAccessibilityScreen = true
enableLanguageScreen = true
additionalScreens = []

[animation]
backgroundAnimationSpeed = 1.0
environmentScrollSpeed = 4.0
environmentScrollDirection = "LEFT_TO_RIGHT"

[extra]
enableSunClickLogo = true
enableConfluenceSplashText = true
```

## License
LGPL-3.0.
