package fr.estecka.variantscit.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import fr.estecka.variantscit.VariantsCitMod;
import fr.estecka.variantscit.assetgen.FilledTemplate;
import fr.estecka.variantscit.assetgen.GeneratedResourcePack;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.concurrent.CompletableFuture;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;

public class AssetGenCommands
{
	static public final Identifier ID = Identifier.of(VariantsCitMod.MODID, "assetgen");

	static public final String ASSET_ARG = "asset id";
	
	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, AssetGenCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
		var peek = literal("peek")
			.then(argument(ASSET_ARG, identifier())
				.suggests(AssetGenCommands::AssetAutofill)
				.executes(AssetGenCommands::AssetDump)
			);

		var root = literal(VariantsCitMod.MODID)
			.then(literal("assetgen")
				.then(peek)
			);

		dispatcher.register(root);
	}


/******************************************************************************/
/* # Autofill                                                                 */
/******************************************************************************/

	static private CompletableFuture<Suggestions> AssetAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		CommandSource.suggestIdentifiers(GeneratedResourcePack.INSTANCE.GetAll().keySet(), builder);
		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	static private int AssetDump(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		Identifier id = context.getArgument(ASSET_ARG, Identifier.class);
		FilledTemplate resource = (FilledTemplate)GeneratedResourcePack.INSTANCE.GetAll().get(id);

		if (resource == null){
			context.getSource().sendError(Text.literal("No such asset: "+id.toString()));
			return -1;
		}

		VariantsCitMod.LOGGER.info("{}:\n{}", id, resource.getString());

		context.getSource().sendFeedback(Text.literal("Asset content was printed into the game's log."));
		return 0;
	}

}

