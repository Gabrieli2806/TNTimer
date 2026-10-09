## [3.0.0] - 2026-10-09
### Added
- 3D display mode: the timer floats above each TNT, like a nametag
- Redesigned settings screen with tooltips, sliders and per-mode sections
- HUD text scale option (replaces Small/Medium/Large)
- Up to 20 timers at once
- Config button in the Forge mod list; `K` keybind to open settings

### Changed
- Rebuilt for Minecraft 1.13.2 on the shared 3.0 code base
- Settings moved from `config/tntimer.cfg` to `config/tntimer.json` (old settings are not carried over)
- HUD timers sorted by soonest explosion; 3D labels prefer the closest TNT
- Timer colours: white, orange under 2s, red under 1s

### Removed
- Donate button in the settings screen
