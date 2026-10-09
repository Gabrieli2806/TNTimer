## [3.0.2] - 2026-10-09
### Fixed
- 3D timer above TNT was never drawn; it now uses the vanilla nametag renderer

## [3.0.1] - 2026-10-09
### Fixed
- Forge: settings, keybind and display names showed raw translation keys (the mod's resources were missing pack metadata)

## [3.0.0] - 2026-10-08
### Added
- Forge support (multiloader: Fabric and Forge)
- Redesigned settings screen with tooltips, sliders and per-mode sections
- HUD text scale option
- Config button in the Forge mod list; `K` keybind to open settings
- Automatic build and Modrinth/CurseForge publishing via GitHub Actions

### Changed
- Rebuilt for Minecraft 1.19.4
- HUD timers sorted by soonest explosion; 3D labels prefer the closest TNT
- 3D labels follow moving TNT smoothly and hide with F1

### Fixed
- Log spam from the world renderer (#3)
- Corrupt config file no longer crashes the game
- Keybind category showing an untranslated name
