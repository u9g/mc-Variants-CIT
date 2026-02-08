package fr.estecka.variantscit.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Registry for custom model providers that allow external mods to override item
 * and equipment models with arbitrary logic.
 *
 * <p>Providers registered here are checked <b>before</b> the resource-pack
 * module system. The first provider that returns a non-null {@link Identifier}
 * wins.
 *
 * <p>Registration should be done during your mod's client initialization
 * (in your {@code ClientModInitializer}).
 *
 * <p>Example usage:
 * <pre>{@code
 * public class MyMod implements ClientModInitializer {
 *     @Override
 *     public void onInitializeClient() {
 *         ModelOverrideRegistry.registerItemModelProvider(stack -> {
 *             if (stack.isOf(Items.DIAMOND_SWORD) && hasSpecialCondition(stack)) {
 *                 return Identifier.of("mymod", "item/special_sword");
 *             }
 *             return null;
 *         });
 *     }
 * }
 * }</pre>
 *
 * @see ICustomModelProvider
 */
public final class ModelOverrideRegistry
{
	private static final List<ICustomModelProvider> ITEM_MODEL_PROVIDERS = new CopyOnWriteArrayList<>();
	private static final List<ICustomModelProvider> EQUIPMENT_MODEL_PROVIDERS = new CopyOnWriteArrayList<>();

	private ModelOverrideRegistry() {}

	/**
	 * Registers a provider for overriding item models.
	 * Providers are called in registration order; the first non-null result is
	 * used.
	 *
	 * @param provider The custom model provider.
	 */
	public static void registerItemModelProvider(ICustomModelProvider provider) {
		if (provider == null)
			throw new IllegalArgumentException("provider must not be null");
		ITEM_MODEL_PROVIDERS.add(provider);
	}

	/**
	 * Registers a provider for overriding equipment (armor, elytra, etc.)
	 * models.
	 * Providers are called in registration order; the first non-null result is
	 * used.
	 *
	 * @param provider The custom model provider.
	 */
	public static void registerEquipmentModelProvider(ICustomModelProvider provider) {
		if (provider == null)
			throw new IllegalArgumentException("provider must not be null");
		EQUIPMENT_MODEL_PROVIDERS.add(provider);
	}

	/**
	 * Queries all registered item model providers for the given stack.
	 * @return The first non-null model identifier, or null if no provider
	 * matched.
	 */
	public static @Nullable Identifier getItemModel(ItemStack stack) {
		for (var provider : ITEM_MODEL_PROVIDERS) {
			Identifier model = provider.getModel(stack);
			if (model != null)
				return model;
		}
		return null;
	}

	/**
	 * Queries all registered equipment model providers for the given stack.
	 * @return The first non-null model identifier, or null if no provider
	 * matched.
	 */
	public static @Nullable Identifier getEquipmentModel(ItemStack stack) {
		for (var provider : EQUIPMENT_MODEL_PROVIDERS) {
			Identifier model = provider.getModel(stack);
			if (model != null)
				return model;
		}
		return null;
	}
}
