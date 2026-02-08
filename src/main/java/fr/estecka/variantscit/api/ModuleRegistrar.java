package fr.estecka.variantscit.api;

import fr.estecka.variantscit.VCitRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.Identifier;

public final class ModuleRegistrar
{
	static public void Register(Identifier moduleId, MapCodec<? extends ICitModule> moduleCodec){
		VCitRegistries.RegisterSimpleModule(moduleId, moduleCodec);
	}

	static public void Register(Identifier moduleId, ISimpleCitModule module){
		VCitRegistries.RegisterSimpleModule(moduleId, module);
	}

	static public void Register(Identifier moduleId, ICitModule module){
		VCitRegistries.RegisterSimpleModule(moduleId, module);
	}

	/**
	 * Registers a module type with a codec for parameterized deserialization.
	 * @param moduleId The unique identifier for this module type.
	 * @param moduleCodec The codec used to deserialize module parameters from JSON.
	 */
	public void register(Identifier moduleId, MapCodec<? extends ICitModule> moduleCodec){
		Register(moduleId, moduleCodec);
	}

	/**
	 * Registers a simple module instance (no parameters needed).
	 * @param moduleId The unique identifier for this module type.
	 * @param module The module instance implementing variant identification.
	 */
	public void register(Identifier moduleId, ISimpleCitModule module){
		Register(moduleId, module);
	}

	/**
	 * Registers a module instance (no parameters needed).
	 * @param moduleId The unique identifier for this module type.
	 * @param module The module instance implementing model resolution.
	 */
	public void register(Identifier moduleId, ICitModule module){
		Register(moduleId, module);
	}
}
