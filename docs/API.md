# Variants-CIT API

This document explains how to depend on Variants-CIT from your own Fabric mod and use its API to add custom item/equipment model logic.

## Adding Variants-CIT as a Dependency

### 1. Build Variants-CIT locally

Since Variants-CIT is not published to a public Maven repository, you need to build it and publish it to your local Maven repository:

```bash
git clone https://github.com/Estecka/mc-Variants-CIT.git
cd mc-Variants-CIT
./gradlew publishToMavenLocal
```

This publishes the mod to `~/.m2/repository/`.

### 2. Configure your mod's `build.gradle`

Add `mavenLocal()` to your repositories and declare Variants-CIT as a dependency:

```groovy
repositories {
    mavenLocal()
}

dependencies {
    // ... your other dependencies (minecraft, fabric-loader, fabric-api) ...

    // Variants-CIT API dependency
    modImplementation "fr.estecka.variantscit:variants-cit:3.16.1+1.21.4"
}
```

### 3. Configure `fabric.mod.json`

Add Variants-CIT as a dependency in your mod's `fabric.mod.json`:

```json
{
    "depends": {
        "variants-cit": ">=3.16.1"
    }
}
```

If Variants-CIT is optional (your mod works without it), use `"suggests"` instead of `"depends"` and guard your API calls with a mod-loaded check.

## Using the API

### Custom Model Providers

The primary API for external mods is `ModelOverrideRegistry`, which lets you register callbacks that provide custom item or equipment models based on arbitrary logic.

Custom providers are checked **before** the resource-pack module system, so your code takes priority over JSON-defined modules. Providers are called in registration order; the first non-null result wins.

#### Overriding Item Models

Register an `ICustomModelProvider` during your client initialization:

```java
import fr.estecka.variantscit.api.ICustomModelProvider;
import fr.estecka.variantscit.api.ModelOverrideRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class MyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModelOverrideRegistry.registerItemModelProvider(stack -> {
            // Example: change diamond sword model based on custom NBT data
            if (stack.isOf(Items.DIAMOND_SWORD)) {
                // Your arbitrary logic here — check NBT, game state, etc.
                int kills = getKillCount(stack); // your own method
                if (kills >= 100) {
                    return Identifier.of("mymod", "item/legendary_sword");
                } else if (kills >= 10) {
                    return Identifier.of("mymod", "item/battle_worn_sword");
                }
            }
            // Return null to let other providers or the default system handle it
            return null;
        });
    }
}
```

#### Overriding Equipment Models

For armor, elytra, capes, and other equipment rendering:

```java
ModelOverrideRegistry.registerEquipmentModelProvider(stack -> {
    if (stack.isOf(Items.DIAMOND_CHESTPLATE) && isEnchanted(stack)) {
        return Identifier.of("mymod", "enchanted_diamond_chestplate");
    }
    return null;
});
```

### Registering Custom Module Types

If you want to add a new module type that can be used in resource pack JSON (like the built-in `enchantment`, `potion_type`, etc.), use `ModuleRegistrar`:

```java
import fr.estecka.variantscit.api.ModuleRegistrar;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.util.Identifier;

// Register a simple module type that resource packs can reference
ModuleRegistrar.Register(
    Identifier.of("mymod", "kill_count"),
    (ISimpleCitModule) stack -> {
        int kills = getKillCount(stack);
        if (kills > 0) {
            return Identifier.of("mymod", "kills_" + kills);
        }
        return null;
    }
);
```

Resource packs can then use your module type:
```json
{
    "type": "mymod:kill_count",
    "items": "minecraft:diamond_sword",
    "modelPrefix": "item/sword_kills/"
}
```

## API Reference

### `ICustomModelProvider`

A functional interface for providing custom item models:

```java
@FunctionalInterface
public interface ICustomModelProvider {
    /**
     * @param stack The item stack being rendered.
     * @return The model identifier, or null to pass to the next provider.
     */
    @Nullable Identifier getModel(ItemStack stack);
}
```

### `ModelOverrideRegistry`

Static registry for custom model providers:

| Method | Description |
|--------|-------------|
| `registerItemModelProvider(ICustomModelProvider)` | Register a provider for item model overrides |
| `registerEquipmentModelProvider(ICustomModelProvider)` | Register a provider for equipment model overrides |

### `ModuleRegistrar`

Static registration for resource-pack-driven module types:

| Method | Description |
|--------|-------------|
| `Register(Identifier, MapCodec<? extends ICitModule>)` | Register a codec-based module type |
| `Register(Identifier, ISimpleCitModule)` | Register a simple module instance as a type |
| `Register(Identifier, ICitModule)` | Register a module instance as a type |
