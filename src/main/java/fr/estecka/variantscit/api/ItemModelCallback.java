package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * Callback for overriding item models with custom logic.
 *
 * <p>External mods can register callbacks to provide custom item model
 * resolution. Callbacks are invoked before Variants-CIT's internal module
 * lookup, so they take priority over resource-pack-based modules.
 *
 * <p>Return a non-null {@link Identifier} to override the item's model, or
 * {@code null} to pass through to the next handler.
 *
 * <p><b>Important:</b> The returned {@link Identifier} must point to a model
 * that Minecraft has loaded. For vanilla items this works out of the box, but
 * for custom models you must ensure:
 * <ol>
 *   <li>The model JSON exists at {@code assets/<namespace>/items/<path>.json}
 *       (an <b>item model definition</b> file), <b>not</b> just a model in
 *       {@code models/item/}.</li>
 *   <li>Your mod's resource pack is loaded so Minecraft discovers the file
 *       during resource reload.</li>
 * </ol>
 *
 * <p>If the model identifier you return is not loaded, Minecraft will silently
 * fall back to the missing-model texture. Enable {@code TRACE}-level logging
 * for the {@code variants-cit} logger to see which identifiers are being
 * returned by callbacks.
 *
 * <p>Example usage:
 * <pre>{@code
 * ItemModelCallback.EVENT.register((stack) -> {
 *     if (stack.isOf(Items.DIAMOND_SWORD) && hasCustomKills(stack)) {
 *         return Identifier.of("mymod", "diamond_sword_special");
 *     }
 *     return null;
 * });
 * }</pre>
 *
 * <p>This requires an item model definition file at
 * {@code assets/mymod/items/diamond_sword_special.json}. See
 * {@code docs/API.md} for full details.
 */
@FunctionalInterface
public interface ItemModelCallback
{
	Event<ItemModelCallback> EVENT = EventFactory.createArrayBacked(ItemModelCallback.class,
		(listeners) -> (stack) -> {
			for (ItemModelCallback listener : listeners) {
				Identifier result = listener.resolveModel(stack);
				if (result != null)
					return result;
			}
			return null;
		}
	);

	/**
	 * Called when resolving the model for an item stack.
	 *
	 * @param stack The item stack being rendered.
	 * @return A model {@link Identifier} to use, or {@code null} to defer to
	 * the next handler or Variants-CIT's internal modules.
	 */
	@Nullable Identifier resolveModel(ItemStack stack);
}
