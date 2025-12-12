# Bluemap-CMI Plugin Testing Checklist

## ✅ Code Review Complete - All Issues Fixed

### Critical Issues Fixed:
1. ✅ **CMI API Integration** - Now properly uses CMI's SpawnManager and WarpManager
2. ✅ **World Name Hardcoding** - Removed hardcoded "world", now uses first available world
3. ✅ **Null Safety** - Added null checks for world references
4. ✅ **MarkerSet Efficiency** - Fixed to create marker set once instead of per-map
5. ✅ **Java Version** - Updated to Java 21 for dependency compatibility
6. ✅ **GitHub Workflow** - Updated to use Java 21

---

## Plugin Structure Verification

### ✅ Source Files (4 total):
- [x] `BluemapCMIPlugin.java` - Main plugin class with proper lifecycle
- [x] `UpdateTask.java` - Background thread for marker updates
- [x] `BluemapIntegration.java` - BlueMap API integration
- [x] `CMIIntegration.java` - CMI API integration

### ✅ Resource Files:
- [x] `config.yml` - Complete configuration with all options
- [x] `plugin.yml` - Proper metadata and dependencies

### ✅ Build Configuration:
- [x] `pom.xml` - Maven config with Java 21, all dependencies
- [x] `.github/workflows/maven-publish.yml` - GitHub Actions with Java 21

---

## Functional Testing Plan

### Phase 1: Build & Compilation
- [ ] **Test:** Run `mvn clean package`
- [ ] **Expected:** JAR file created in `target/` folder
- [ ] **Expected:** No compilation errors
- [ ] **Expected:** File size ~50-100KB (shaded JAR)

### Phase 2: Plugin Loading
- [ ] **Test:** Place JAR in server `plugins/` folder
- [ ] **Prerequisite:** BlueMap plugin installed
- [ ] **Prerequisite:** CMI plugin installed
- [ ] **Test:** Start server
- [ ] **Expected:** Plugin enables successfully
- [ ] **Expected:** Log shows: "Bluemap CMI Integration Enabled"
- [ ] **Expected:** Log shows: "BluemapIntegration initialized successfully"
- [ ] **Expected:** Log shows: "CMIIntegration initialized successfully"
- [ ] **Expected:** Log shows: "All integrations initialized successfully!"

### Phase 3: Spawn Marker
- [ ] **Test:** Check BlueMap web interface
- [ ] **Expected:** "CMI Locations" marker set visible
- [ ] **Expected:** Spawn marker appears at server spawn
- [ ] **Expected:** Marker has label "Spawn" (configurable)
- [ ] **Expected:** Clicking marker shows description
- [ ] **Test:** Change spawn location with `/cmi setspawn`
- [ ] **Expected:** Marker updates after 5 minutes (or manual reload)

### Phase 4: First Spawn Marker
- [ ] **Test:** Set first spawn with `/cmi setfirstspawn`
- [ ] **Expected:** First spawn marker appears
- [ ] **Expected:** Marker has label "First Spawn"
- [ ] **Test:** Disable in config: `integrations.first-spawn: false`
- [ ] **Expected:** First spawn marker no longer appears after reload

### Phase 5: Warp Markers
- [ ] **Test:** Create warp with `/cmi setwarp TestWarp`
- [ ] **Expected:** Warp marker appears on BlueMap
- [ ] **Expected:** Marker labeled "Warp: TestWarp"
- [ ] **Test:** Create multiple warps
- [ ] **Expected:** All warps appear as markers
- [ ] **Test:** Set `max-warps: 5` in config
- [ ] **Expected:** Only first 5 warps shown
- [ ] **Test:** Delete warp with `/cmi delwarp TestWarp`
- [ ] **Expected:** Marker removed after update cycle

### Phase 6: Auto-Update Feature
- [ ] **Test:** Wait 5 minutes (default update interval)
- [ ] **Expected:** Markers refresh automatically
- [ ] **Expected:** Log shows "Markers updated successfully" (if debug enabled)
- [ ] **Test:** Change `update-interval: 60` in config
- [ ] **Expected:** Updates happen every 60 seconds
- [ ] **Test:** Set `update-interval: 0` in config
- [ ] **Expected:** Auto-updates disabled

### Phase 7: Configuration Testing
- [ ] **Test:** Set `debug: true` in config
- [ ] **Expected:** Detailed logging in console
- [ ] **Test:** Set `log-marker-additions: false`
- [ ] **Expected:** No marker addition logs
- [ ] **Test:** Disable spawn: `spawn-marker.enabled: false`
- [ ] **Expected:** Spawn marker no longer appears
- [ ] **Test:** Disable first spawn: `first-spawn-marker.enabled: false`
- [ ] **Expected:** First spawn marker no longer appears
- [ ] **Test:** Disable warps: `warps-marker.enabled: false`
- [ ] **Expected:** Warp markers no longer appear

### Phase 8: World Blacklist Testing
- [ ] **Test:** Add world to blacklist: `world-blacklist: ["world_nether"]`
- [ ] **Expected:** Markers from world_nether don't appear
- [ ] **Test:** Create warp in blacklisted world
- [ ] **Expected:** Warp marker skipped
- [ ] **Expected:** Log shows "X skipped from blacklisted worlds"
- [ ] **Test:** Set spawn in blacklisted world
- [ ] **Expected:** Spawn marker not added
- [ ] **Test:** Enable debug and check blacklisted world
- [ ] **Expected:** Debug log shows "Skipping... world X is blacklisted"
- [ ] **Test:** Remove world from blacklist
- [ ] **Expected:** Markers from that world now appear

### Phase 9: Error Handling
- [ ] **Test:** Start server WITHOUT BlueMap
- [ ] **Expected:** Plugin logs "BlueMap plugin not found! Disabling..."
- [ ] **Expected:** Plugin disables gracefully
- [ ] **Test:** Start server WITHOUT CMI
- [ ] **Expected:** Plugin logs "CMI plugin not found! Disabling..."
- [ ] **Expected:** Plugin disables gracefully
- [ ] **Test:** Delete a world that has markers
- [ ] **Expected:** Plugin handles gracefully, no crashes
- [ ] **Test:** Add non-existent world to blacklist
- [ ] **Expected:** No errors, plugin works normally

### Phase 10: Reload & Restart
- [ ] **Test:** Run `/reload` command
- [ ] **Expected:** Plugin reloads successfully
- [ ] **Expected:** Markers persist
- [ ] **Test:** Stop server
- [ ] **Expected:** Log shows "Bluemap CMI Integration Disabled"
- [ ] **Expected:** Update task stops gracefully
- [ ] **Test:** Restart server
- [ ] **Expected:** All markers reload correctly
- [ ] **Expected:** Blacklist settings preserved

### Phase 11: Multi-World Support
- [ ] **Test:** Create spawn in different world (e.g., world_nether)
- [ ] **Expected:** Marker appears on correct BlueMap world
- [ ] **Test:** Create warps in multiple worlds
- [ ] **Expected:** Each marker appears on its respective world map

---

## Performance Testing

### Memory & CPU:
- [ ] **Test:** Monitor with `/timings` or profiler
- [ ] **Expected:** Minimal CPU usage (<1%)
- [ ] **Expected:** Update task doesn't lag server
- [ ] **Expected:** Memory usage stable (no leaks)

### Large Warp Count:
- [ ] **Test:** Create 50+ warps
- [ ] **Expected:** All markers load without lag
- [ ] **Test:** Use `max-warps: 20` limit
- [ ] **Expected:** Only 20 markers shown, improves performance

---

## Edge Cases

- [ ] **Test:** Warp with special characters in name
- [ ] **Test:** Warp at extreme coordinates (y=320, y=-64)
- [ ] **Test:** Warp in unloaded chunk
- [ ] **Test:** Multiple spawn locations across worlds
- [ ] **Test:** Config file with invalid values
- [ ] **Test:** BlueMap API temporarily unavailable

---

## Known Limitations

1. **CMI Dependency:** Requires CMI premium plugin
2. **BlueMap Dependency:** Requires BlueMap installed and configured
3. **Java 21:** Server must run Java 21+
4. **Marker Icons:** Uses default BlueMap icons (customization requires BlueMap web config)

---

## Quick Test Commands

```bash
# Check if plugins loaded
/plugins

# Check plugin version
/bluemapcmi (if command added) or /plugins

# Test CMI spawn
/cmi setspawn
/spawn

# Test CMI warps
/cmi setwarp test1
/cmi warp test1
/cmi listwarps

# Test world blacklist
# 1. Create warp in nether: /cmi setwarp nethertest
# 2. Check BlueMap - should not appear (nether is blacklisted by default)
# 3. Remove from blacklist in config.yml
# 4. /reload confirm
# 5. Check BlueMap - should now appear

# Check BlueMap
/bluemap
# Then visit web interface: http://yourserver:8100

# Reload plugin
/reload confirm

# Enable debug mode
# Edit config.yml: debug: true
/reload confirm
```

---

## Success Criteria

✅ **Plugin compiles with no errors**  
✅ **Plugin loads successfully with both dependencies**  
✅ **Spawn marker appears correctly**  
✅ **Warp markers appear for all CMI warps**  
✅ **Markers update automatically every 5 minutes**  
✅ **Configuration changes take effect**  
✅ **No console errors or warnings**  
✅ **No performance impact on server**  
✅ **Plugin disables gracefully when dependencies missing**

---

## Post-Testing Actions

- [ ] Document any bugs found
- [ ] Test on production server (if applicable)
- [ ] Create release notes
- [ ] Tag release version
- [ ] Publish to GitHub releases
