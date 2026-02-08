# Variants-CIT API

Variants-CIT exposes an API that allows other Fabric mods to hook into its item model resolution pipeline with custom logic. This is useful when you need complex, programmatic control over item textures — for example, changing a sword's appearance based on game state that cannot be expressed through resource-pack-based CIT modules.

## Adding Variants-CIT as a Dependency

### 1. Add the Maven Repository

In your `build.gradle`, add the repository where Variants-CIT is published (or use a local maven for development):

```groovy
repositories {
    // If published to a Maven repository, add it here:
    // maven { url = 'https://maven.example.com/' }

    // For local development, you can publish Variants-CIT to your local Maven:
    mavenLocal()
}
```

### 2. Add the Dependency

In your `build.gradle` `dependencies` block, add Variants-CIT as a `modCompileOnly` + `modLocalRuntime` dependency (or `modImplementation` if you want it bundled):

```groovy
dependencies {
    // Compile against Variants-CIT API (required at compile time, provided at runtime by the user)
    modCompileOnly "fr.estecka.variantscit:variants-cit:<version>"

    // Include in your dev environment for testing
    modLocalRuntime "fr.estecka.variantscit:variants-cit:<version>"
}
```

Replace `<version>` with the desired version (e.g., `4.12.0+1.21.9`).

> **Tip:** To publish Variants-CIT to your local Maven for development, clone the Variants-CIT repository and run `./gradlew publishToMavenLocal`.

### 3. Declare the Dependency in `fabric.mod.json`

Add a dependency (or optional dependency) on Variants-CIT in your mod's `fabric.mod.json`:

```json
{
    "depends": {
        "variants-cit": ">=4.12"
    }
}
```

Or, if your mod should work without Variants-CIT installed:

```json
{
    "suggests": {
        "variants-cit": ">=4.12"
    }
}
```

## Using the API

### ItemModelCallback — Custom Item Model Resolution

The primary extension point is `ItemModelCallback.EVENT`, a Fabric event that lets you override any item's model with arbitrary logic.

**How it works:**
1. When Minecraft resolves an item's model, Variants-CIT invokes all registered `ItemModelCallback` listeners **before** its own internal (resource-pack-based) module lookup.
2. If any listener returns a non-null `Identifier`, that model is used immediately.
3. If all listeners return `null`, Variants-CIT falls back to its internal modules, then to vanilla.

**Example: Override a sword model based on custom logic**

```java
import fr.estecka.variantscit.api.ItemModelCallback;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class MyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ItemModelCallback.EVENT.register((stack) -> {
            // Example: change diamond sword texture based on custom kill count
            if (stack.isOf(Items.DIAMOND_SWORD)) {
                int kills = getKillCount(stack); // your custom logic
                if (kills >= 100) {
                    return Identifier.of("mymod", "diamond_sword_legendary");
                } else if (kills >= 10) {
                    return Identifier.of("mymod", "diamond_sword_veteran");
                }
            }
            return null; // defer to Variants-CIT modules or vanilla
        });
    }
}
```

The returned `Identifier` must point to an **item model definition** that Minecraft has loaded. This is an `items/` file, **not** a `models/item/` file. Specifically, you need:

```
assets/mymod/items/diamond_sword_legendary.json
```

This item model definition file tells Minecraft how to render the item. For example, a simple model reference:

```json
{
    "model": {
        "type": "minecraft:model",
        "model": "mymod:item/diamond_sword_legendary"
    }
}
```

And the actual model file at `assets/mymod/models/item/diamond_sword_legendary.json`:

```json
{
    "parent": "minecraft:item/handheld",
    "textures": {
        "layer0": "mymod:item/diamond_sword_legendary"
    }
}
```

With your texture at `assets/mymod/textures/item/diamond_sword_legendary.png`.

> **Common mistake:** Using `Identifier.of("mymod", "item/diamond_sword_legendary")` — note the path should be just `"diamond_sword_legendary"`, not `"item/diamond_sword_legendary"`. The identifier maps to `assets/mymod/items/<path>.json`, so including `item/` in the path would look for `assets/mymod/items/item/diamond_sword_legendary.json`.

### ModuleRegistrar — Custom CIT Module Types

If you want to add new module types that work with Variants-CIT's resource-pack format, use `ModuleRegistrar`:

```java
import fr.estecka.variantscit.api.ModuleRegistrar;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.util.Identifier;

// Register a module that can be used in resource pack JSON files
ModuleRegistrar.Register(
    Identifier.of("mymod", "custom_variant"),
    (stack) -> {
        // Return a variant identifier based on item data
        return computeVariant(stack);
    }
);
```

This lets resource pack authors use your module type:
```json
{
    "type": "mymod:custom_variant",
    "items": "minecraft:diamond_sword",
    "modelPrefix": "item/sword_cit/"
}
```

## Building Variants-CIT from Source

1. Clone the repository:
   ```sh
   git clone https://github.com/Estecka/mc-Variants-CIT.git
   cd mc-Variants-CIT
   ```

2. Build the mod:
   ```sh
   ./gradlew build
   ```
   The built JAR will be in `build/libs/`.

3. Publish to local Maven (for use as a dependency in other projects):
   ```sh
   ./gradlew publishToMavenLocal
   ```
   This makes the mod available at `mavenLocal()` in other Gradle projects.

## Troubleshooting

### My modded item model isn't showing up / falls back to missing texture

The most common issue when using `ItemModelCallback` with a custom namespace (e.g., `Identifier.of("mymod", "my_sword")`) is that Minecraft hasn't loaded the model. The identifier you return must correspond to an **item model definition** file that Minecraft discovers during resource loading.

**Checklist:**

1. **Item model definition file exists** — You need a file at `assets/<namespace>/items/<path>.json`.
   For `Identifier.of("mymod", "diamond_sword_legendary")`, the file must be at:
   ```
   assets/mymod/items/diamond_sword_legendary.json
   ```

2. **The path is correct** — A common mistake is returning `Identifier.of("mymod", "item/diamond_sword_legendary")` which would look for `assets/mymod/items/item/diamond_sword_legendary.json` (note the extra `item/` segment). The path in the identifier maps directly to `assets/<namespace>/items/<path>.json`.

3. **The item model definition references a valid model** — Your `items/*.json` file must point to an actual model:
   ```json
   {
       "model": {
           "type": "minecraft:model",
           "model": "mymod:item/diamond_sword_legendary"
       }
   }
   ```

4. **The referenced model JSON exists** — At `assets/mymod/models/item/diamond_sword_legendary.json`.

5. **The texture exists** — At `assets/mymod/textures/item/diamond_sword_legendary.png`.

6. **Your mod's resources are in the correct directory** — In a Fabric mod, resources go in `src/main/resources/assets/<your_mod_id>/`.

### Why does it work with vanilla identifiers but not my own?

Vanilla item models (e.g., `Identifier.of("minecraft", "iron_sword")`) work because Minecraft always loads its own item model definitions during startup. Your mod's item model definitions are only loaded if they exist in a resource pack that Minecraft knows about — which for a Fabric mod means they must be in `src/main/resources/assets/`.

### Example file structure for a working custom model

```
src/main/resources/
└── assets/
    └── mymod/
        ├── items/
        │   └── diamond_sword_legendary.json    ← item model definition
        ├── models/
        │   └── item/
        │       └── diamond_sword_legendary.json ← the actual model
        └── textures/
            └── item/
                └── diamond_sword_legendary.png  ← the texture
```

## Debugging

Variants-CIT logs callback results at `TRACE` level. To see these messages, set the logging level for `variants-cit` to `TRACE` in your logging configuration.

### Enabling TRACE logging (Log4j)

Add the following to your `log4j2.xml` (or create one in your run directory):

```xml
<Configuration>
    <Loggers>
        <Logger name="variants-cit" level="TRACE"/>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

Or add a system property to your Fabric run configuration (in `build.gradle`):

```groovy
loom {
    runs {
        client {
            property "log4j.configurationFile", "log4j2-debug.xml"
        }
    }
}
```

### What to look for in the logs

When TRACE logging is enabled, you will see messages like:

```
[TRACE] [variants-cit] ItemModelCallback resolved model for diamond_sword: mymod:diamond_sword_legendary
```

If you see the correct identifier being returned but the model still doesn't show up, the issue is that the item model definition file is missing or in the wrong location. Double-check the file structure in the [Troubleshooting](#troubleshooting) section above.

If you don't see any log output for your item, your callback may not be matching the item. Add temporary logging inside your callback to verify it is being invoked.

## API Reference

### `fr.estecka.variantscit.api.ItemModelCallback`

| Member | Description |
|--------|-------------|
| `EVENT` | Fabric `Event` instance. Register callbacks via `EVENT.register(callback)`. |
| `resolveModel(ItemStack stack)` | Return a model `Identifier` to override the item's model, or `null` to defer. |

### `fr.estecka.variantscit.api.ModuleRegistrar`

| Method | Description |
|--------|-------------|
| `Register(Identifier id, ICitModule module)` | Register a singleton module instance. |
| `Register(Identifier id, ISimpleCitModule module)` | Register a simple module (variant-based). |
| `Register(Identifier id, MapCodec<? extends ICitModule> codec)` | Register a module type with configurable parameters via codec. |

### `fr.estecka.variantscit.api.ICitModule`

Full-control module interface. Implement `GetItemModel(ItemStack, IVariantManager)` for complete control over model selection.

### `fr.estecka.variantscit.api.ISimpleCitModule`

Simplified module interface. Implement `GetItemVariant(ItemStack)` to return a variant identifier; the framework handles model lookup.

### `fr.estecka.variantscit.api.IVariantManager`

Provides model lookup by variant ID. Available in `ICitModule.GetItemModel()`:
- `HasVariantModel(Identifier)` — Check if a variant has a model.
- `GetVariantModel(Identifier)` — Get the model for a variant (with fallback).
- `GetSpecialModel(String)` — Get a special model by key.
