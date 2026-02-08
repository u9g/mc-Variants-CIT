package fr.estecka.variantscit.api;

import org.jetbrains.annotations.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * A functional interface for providing custom item models based on arbitrary
 * logic. External mods can implement this to override item or equipment models
 * without relying on resource pack modules.
 *
 * <p>For example, a mod could change a sword's texture based on how many kills
 * a player has, or any other complex condition that cannot be expressed through
 * resource pack JSON alone.
 *
 * @see ModelOverrideRegistry
 */
@FunctionalInterface
public interface ICustomModelProvider
{
	/**
	 * Determines the model to use for the given item stack.
	 *
	 * @param stack The item stack being rendered.
	 * @return The model identifier to use, or null to let other providers or
	 * the default module system handle the item.
	 */
	@Nullable Identifier getModel(ItemStack stack);
}
