![Tntimerlogo](https://cdn.modrinth.com/data/cached_images/f2e8250eb3dcefe943cb829d0971629f10811672.png)

![SeparatorBar](https://cdn.modrinth.com/data/cached_images/cd452159c8ee74da3578ffd088abab56e5fe1c46.png)

**is a client-side mod that displays a precise countdown timer for TNT explosions, helping on explosive timing in Minecraft. For Minigames gameplay, TNT jumps, or just show when a TNT explode.**

![SeparatorBar2](https://cdn.modrinth.com/data/cached_images/68771686d87d838b08514067a7fe490087de83d8.png)

**Features**

*Dual Display Modes*
* HUD Timer - On-screen list with 7 positions (top-left, top-right, bottom-left, bottom-right, top-center, bottom-center, under cursor) and adjustable text scale
* 3D World Timer - Floating countdown directly above each TNT

*Visual Info*
* Color-coded warnings - White (2s or more), orange (under 2s), red (under 1s)
* Clean, minimal design - Shows precise time in "3.5s" format
* Optional HUD background for better readability

*Customization*
* Track up to 20 timers at once; the most urgent (HUD) or closest (3D) are shown first
* Settings screen with tooltips, via Mod Menu (Fabric) or the keybind (default: `K`)
* Translated into English, Spanish, German, French, Italian, Japanese, Polish, Russian and Chinese

*Made For*
* Minigames - Time TNT jumps perfectly in Bedwars, SkyWars, etc
* Parkour - TNT jumps
* Redstone - Coordinate timed explosions

**Requirements**
* Minecraft 1.17 - 1.17.1, Java 16
* Fabric (Fabric API; Mod Menu optional) or Forge 37+

Client-Side - Works on any server without requiring server-side installation. Your timers are visible only to you.

**Configuration**

Saved in `config/tntimer.json` (edit in-game with `K` or the mod list):

| Option | Default | Description |
|--------|---------|-------------|
| `enabled` | `true` | Turn all timers on or off |
| `displayMode` | `WORLD` | `HUD` (on-screen list) or `WORLD` (label above each TNT) |
| `maxTntDisplay` | `5` | Timers shown at once (1-20) |
| `showOnlySeconds` | `true` | `3.4s` instead of `TNT: 3.4s` |
| `position` | `TOP_LEFT` | HUD anchor (7 positions) |
| `showBackground` | `false` | Dark box behind HUD timers |
| `hudScale` | `1.0` | HUD text scale (0.5-3.0) |
