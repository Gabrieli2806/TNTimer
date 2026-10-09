## [3.0.0] - 2026-10-09
### Added
- Port to Minecraft 1.14.4 (Fabric and Forge)
- Forge support (multiloader: Fabric and Forge)
- Redesigned settings screen with tooltips, sliders and per-mode sections
- HUD text scale option
- Config button in the Forge mod list; `K` keybind to open settings
- Automatic build and Modrinth/CurseForge publishing via GitHub Actions

### Changed
- Rebuilt for Minecraft 1.14.4
- HUD timers sorted by soonest explosion; 3D labels prefer the closest TNT
- 3D labels follow moving TNT smoothly and hide with F1

### Fixed
- Log spam from the world renderer (#3)
- Corrupt config file no longer crashes the game
- Keybind category showing an untranslated name
