package fr.estecka.variantscit.api;

/**
 * Entrypoint for other mods to hook into Variants-CIT and register custom
 * CIT module types.
 *
 * <p>To use this entrypoint, implement this interface and declare the
 * implementation in your mod's {@code fabric.mod.json}:
 * <pre>{@code
 * "entrypoints": {
 *     "variants-cit": [
 *         "com.example.mymod.MyVariantsCitPlugin"
 *     ]
 * }
 * }</pre>
 *
 * <p>The {@link #onInitializeVariantsCit} method will be called during
 * client initialization, after Variants-CIT's own modules have been
 * registered.
 */
public interface VariantsCitApiEntrypoint {
	/**
	 * Called during client initialization to allow other mods to register
	 * custom CIT module types via the {@link ModuleRegistrar}.
	 *
	 * @param registrar The registrar used to register custom module types.
	 */
	void onInitializeVariantsCit(ModuleRegistrar registrar);
}
