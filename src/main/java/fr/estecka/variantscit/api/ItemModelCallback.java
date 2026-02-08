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
 * <p>Example usage:
 * <pre>{@code
 * ItemModelCallback.EVENT.register((stack) -> {
 *     if (stack.isOf(Items.DIAMOND_SWORD) && hasCustomKills(stack)) {
 *         return Identifier.of("mymod", "item/diamond_sword_special");
 *     }
 *     return null;
 * });
 * }</pre>
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
