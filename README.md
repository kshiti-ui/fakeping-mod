# FakePing - Network Latency Simulator for Minecraft 1.21+

A client-side Fabric mod that simulates network latency in singleplayer Minecraft for PvP practice. Perfect for players who want to practice combat mechanics under realistic network conditions.

## ✅ Verified for Minecraft 1.21.4

This mod has been updated and tested for Minecraft 1.21.4 with the latest Fabric API.

## Features

- ✅ **Client-side only** - No server mod required
- ✅ **Singleplayer only** - Automatically disabled on real multiplayer servers
- ✅ **Configurable delay** - Set any delay from 0-1000ms
- ✅ **Ping variance/jitter** - Add realistic ±20ms (or custom) variation
- ✅ **Selective packet delay** - Toggle delay for specific packet types
- ✅ **HUD overlay** - Visual indicator of current fake ping
- ✅ **Command interface** - Easy in-game configuration
- ✅ **Persistent config** - Settings saved between sessions

## Installation

### Requirements
- **Minecraft 1.21.4** (or 1.21+)
- **Fabric Loader 0.16.9+**
- **Fabric API 0.111.0+**
- **Java 21+**

## Usage

### Basic Commands

All commands are client-side and start with `/ping`:

```
/ping set <milliseconds>    # Set fake ping (0-1000ms) and enable
/ping on                    # Enable fake ping with current settings
/ping off                   # Disable fake ping
/ping status                # Show current configuration
/ping jitter <milliseconds> # Set ping variance (0-100ms)
```

### Advanced Commands

Toggle delay for specific packet types:

```
/ping toggle attacks       # Toggle attack packet delay
/ping toggle movement      # Toggle movement packet delay
/ping toggle interactions  # Toggle hand swing/interaction delay
/ping toggle blocks        # Toggle block breaking delay
/ping toggle items         # Toggle item use delay
```

### Example Usage

**Practice with 150ms ping:**
```
/ping set 150
```

**Simulate unstable connection (100ms ±30ms):**
```
/ping set 100
/ping jitter 30
```

**Practice movement only (no attack delay):**
```
/ping set 150
/ping toggle attacks
```

**Check current settings:**
```
/ping status
```

## How It Works

### Technical Overview

1. **Packet Interception**: Uses Mixin to hook into `ClientConnection.send()` method
2. **Singleplayer Detection**: Only activates when connected to integrated server
3. **Queue System**: Stores packets in a thread-safe `ConcurrentLinkedQueue`
4. **Delay Calculation**: Applies base delay + random jitter
5. **Tick Processing**: Sends packets when their scheduled time arrives

### Supported Packet Types

- Attack packets (`PlayerInteractEntityC2SPacket`)
- Movement packets (`PlayerMoveC2SPacket`)
- Hand swing (`HandSwingC2SPacket`)
- Block interactions (`PlayerInteractBlockC2SPacket`)
- Block breaking (`PlayerActionC2SPacket`)
- Item usage (`PlayerInteractItemC2SPacket`)

## Configuration File

Location: `.minecraft/config/fakeping.json`

```json
{
  "enabled": false,
  "baseDelayMs": 150,
  "jitterMs": 20,
  "delayAttacks": true,
  "delayMovement": true,
  "delayInteractions": true,
  "delayBlockBreaking": true,
  "delayItemUse": true,
  "showHud": true
}
```

## FAQ

**Q: Does this work on multiplayer servers?**  
A: No, the mod is automatically disabled when connected to real multiplayer servers.

**Q: Will this get me banned?**  
A: No, it only works in singleplayer and doesn't provide any advantage.

**Q: Why do my hits still look instant?**  
A: The mod delays server-side processing, not client-side visuals. This is how real lag works.

**Q: Can I use this with other mods?**  
A: Yes, FakePing is compatible with most Fabric mods.

## Troubleshooting

**Mod not loading:**
- Ensure you have Minecraft 1.21.4
- Verify Fabric Loader 0.16.9+ is installed
- Check Fabric API 0.111.0+ is in mods folder
- Make sure you're using Java 21+

**Commands not working:**
- Ensure you're in singleplayer mode
- Enable cheats in world settings
- Check mod loaded with `/ping status`

## Contributing

Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Test thoroughly
4. Submit a pull request

## License

MIT License - See LICENSE file

## Credits

Built with:
- [Fabric Loader](https://fabricmc.net/)
- [Fabric API](https://github.com/FabricMC/fabric)
- [Mixin](https://github.com/SpongePowered/Mixin)

---

**For Practice Only** - This mod simulates lag to help improve PvP skills under realistic conditions!
