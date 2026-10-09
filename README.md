# TNTimer Plugin

Server-side version of [TNTimer](https://modrinth.com/mod/tntimer): shows a countdown above every
lit TNT, and above primed sulfur cubes on Minecraft 26.2+. Players don't need to install anything.

This branch (`plugin`) holds the Paper/Spigot plugin. The client mod lives on the `mc/*` branches.

## Features

* Countdown above each lit TNT, white, then gold under 2s and red under 1s (same as the mod)
* Primed sulfur cubes (Minecraft 26.2+)
* Floating text display on 1.19.4+, which never touches the entity's name; players can hide it
  for themselves with `/tntimer toggle`
* Entity name tag on older servers (1.8.8 - 1.19.3)

## Compatibility

One jar for Paper, Spigot, Bukkit and Purpur, Minecraft 1.8.8 to 26.3. Newer features are
detected at runtime:

| Server version   | How the timer is shown | `/tntimer toggle` | Sulfur cubes |
|------------------|------------------------|-------------------|--------------|
| 1.8.8 - 1.19.3   | Entity name tag        | No                | -            |
| 1.19.4 - 26.1.x  | Text display           | Yes               | -            |
| 26.2+            | Text display           | Yes               | Yes          |

Folia isn't supported.

## Commands and permissions

| Command            | Permission       | Default | Description                          |
|--------------------|------------------|---------|--------------------------------------|
| `/tntimer toggle`  | `tntimer.toggle` | all     | Hide or show the timers for yourself |
| `/tntimer reload`  | `tntimer.reload` | op      | Reload `config.yml`                  |

## Configuration

`plugins/TNTimer/config.yml`:

```yaml
enabled: true
mode: auto            # auto | display | name
show-only-seconds: false
sulfur-cubes: true
display:
  background: true
  shadow: true
  see-through: true
  offset: 0.25
```

## Building

```
./gradlew build
```

The jar is written to `build/libs/`. It compiles against the newest Spigot API but targets Java 8,
so it loads on every supported server.
