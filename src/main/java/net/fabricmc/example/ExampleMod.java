package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");

	@Override
	public void onInitialize()
	{
		AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) ->
		{
			BlockState state = world.getBlockState(pos);
            /* Manual spectator check is necessary because AttackBlockCallbacks
               fire before the spectator check */
			if (state.isToolRequired() && !player.isSpectator() &&
					player.getMainHandStack().isEmpty())
			{
				player.damage(DamageSource.GENERIC, 20.0F);
			}
			return ActionResult.PASS;
		});

		LOGGER.info("Hello Fabric world!");
	}
}
