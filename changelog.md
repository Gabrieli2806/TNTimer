## [3.0.2] - 2026-10-09
### Fixed
- 3D timer above TNT was never drawn; it now uses the vanilla nametag renderer

## [3.0.1] - 2026-10-09
### Added
- 3D display mode: the timer floats above each TNT, like a nametag
- Forge support (multiloader: Fabric and Forge)
- Redesigned settings screen with tooltips, sliders and per-mode sections
- HUD text scale option
- Config button in the Forge mod list; `K` keybind to open settings

### Changed
- First release for Minecraft 1.19.3, on the shared 3.0 code base
- HUD timers sorted by soonest explosion; 3D labels prefer the closest TNT
- 3D labels follow moving TNT smoothly and hide with F1

### Fixed
- Corrupt config file no longer crashes the game
