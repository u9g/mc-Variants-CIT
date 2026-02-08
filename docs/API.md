# Variants-CIT API

This document explains how to depend on Variants-CIT from your own Fabric mod and register custom CIT module types.

## Adding Variants-CIT as a Dependency

### 1. Add the Maven repository

In your `build.gradle`, add the repository where Variants-CIT is published (e.g. a local maven or a hosting service):

```groovy
repositories {
    maven {
        name = "VariantsCIT"
        url = "https://maven.example.com" // Replace with the actual maven URL
    }
}
```

If you are building Variants-CIT locally, publish it to your local Maven repository:

```bash
# In the Variants-CIT project directory
./gradlew publishToMavenLocal
```

Then reference your local Maven in the dependent mod's `build.gradle`:

```groovy
repositories {
    mavenLocal()
}
```

### 2. Add the dependency

In your `build.gradle` dependencies block:

```groovy
dependencies {
    // ... existing dependencies ...
    modImplementation "fr.estecka.variantscit:variants-cit:<version>+<mc_version>"
}
```

Replace `<version>` and `<mc_version>` with the version of Variants-CIT and Minecraft you are targeting. For example:

```groovy
modImplementation "fr.estecka.variantscit:variants-cit:3.16.1+1.21.4"
```

### 3. Declare the dependency in `fabric.mod.json`

Add Variants-CIT as a dependency in your mod's `fabric.mod.json`:

```json
{
    "depends": {
        "variants-cit": ">=3.16.1"
    }
}
```

## Registering Custom Modules

### 1. Implement the entrypoint interface

Create a class that implements `VariantsCitApiEntrypoint`:

```java
package com.example.mymod;

import fr.estecka.variantscit.api.ModuleRegistrar;
import fr.estecka.variantscit.api.VariantsCitApiEntrypoint;
import net.minecraft.util.Identifier;

public class MyVariantsCitPlugin implements VariantsCitApiEntrypoint {
    @Override
    public void onInitializeVariantsCit(ModuleRegistrar registrar) {
        // Register a simple module (stateless, no parameters)
        registrar.register(
            Identifier.of("mymod", "my_module"),
            new MyCustomModule()
        );
    }
}
```

### 2. Declare the entrypoint in your `fabric.mod.json`

Add a `variants-cit` entrypoint pointing to your implementation class:

```json
{
    "entrypoints": {
        "client": [
            "com.example.mymod.MyMod"
        ],
        "variants-cit": [
            "com.example.mymod.MyVariantsCitPlugin"
        ]
    }
}
```

### 3. Implement a custom module

There are two interfaces you can implement, depending on your needs:

#### `ISimpleCitModule` — Variant-based model selection

Use this when your module identifies a variant ID from an item stack, and the model is determined by matching the variant ID to a model path.

```java
package com.example.mymod;

import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MyCustomModule implements ISimpleCitModule {
    @Override
    public @Nullable Identifier GetItemVariant(ItemStack stack) {
        // Return a variant identifier based on the item stack,
        // or null if this item has no variant.
        // The returned ID is matched against model paths in the module's modelPrefix directory.
        return null;
    }
}
```

#### `ICitModule` — Full model control

Use this when you need complete control over which model to use, including access to special models and the variant library.

```java
package com.example.mymod;

import fr.estecka.variantscit.api.ICitModule;
import fr.estecka.variantscit.api.IVariantManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class MyAdvancedModule implements ICitModule {
    @Override
    public @Nullable Identifier GetItemModel(ItemStack stack, IVariantManager modelProvider) {
        // Use modelProvider.GetVariantModel() or modelProvider.GetSpecialModel()
        // to look up models, or return a model ID directly.
        return null;
    }
}
```

#### Parameterized modules (codec-based)

If your module needs parameters from the JSON definition, provide a `MapCodec`:

```java
package com.example.mymod;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.estecka.variantscit.api.ISimpleCitModule;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public record MyParameterizedModule(String someOption) implements ISimpleCitModule {
    public static final MapCodec<MyParameterizedModule> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            MapCodec.assumeMapUnsafe(
                com.mojang.serialization.Codec.STRING.fieldOf("someOption")
            ).forGetter(MyParameterizedModule::someOption)
        ).apply(instance, MyParameterizedModule::new)
    );

    @Override
    public @Nullable Identifier GetItemVariant(ItemStack stack) {
        // Use this.someOption in your logic
        return null;
    }
}
```

Register it with the codec overload:

```java
registrar.register(
    Identifier.of("mymod", "my_parameterized_module"),
    MyParameterizedModule.CODEC
);
```

## Resource Pack Format

Once your module type is registered, resource pack authors can use it in their module definitions:

```json
{
    "type": "mymod:my_module",
    "items": "minecraft:diamond_sword",
    "modelPrefix": "item/my_custom_variants/"
}
```

For parameterized modules:

```json
{
    "type": "mymod:my_parameterized_module",
    "items": "minecraft:diamond_sword",
    "modelPrefix": "item/my_custom_variants/",
    "parameters": {
        "someOption": "example_value"
    }
}
```

## Building from Source

To build Variants-CIT from source and publish to your local Maven:

```bash
git clone https://github.com/Estecka/mc-Variants-CIT.git
cd mc-Variants-CIT
./gradlew build
./gradlew publishToMavenLocal
```

The built jar will be in `build/libs/` and the artifact will be available in your local Maven repository (`~/.m2/repository/`).
