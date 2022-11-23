package io.github.marinersfan824.seedfindersmod.mixin;

import io.github.marinersfan824.racemod.RNGStreamGenerator;
import io.github.marinersfan824.racemod.mixinterface.ILevelProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.AbstractCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.command.IncorrectUsageException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.CommandRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mixin(CommandManager.class)
public class CommandManagerMixin extends CommandRegistry {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void addRatesCommand(CallbackInfo ci) {
        this.registerCommand(new AbstractCommand() {
            @Override
            public String getCommandName() {
                return "rates";
            }

            @Override
            public boolean isAccessible(CommandSource commandSource) {
                return true;
            }

            @Override
            public String getUsageTranslationKey(CommandSource source) {
                return "/rates";
            }

            @Override
            public void execute(CommandSource source, String[] args) {
                World overWorld = ((ServerWorld)source.getWorld()).getServer().getWorld();
                if (args.length == 0) {
                    RNGStreamGenerator.tellPlayerCurrentRates(overWorld); // ensure that the world used is the overworld
                } else if (args.length == 2) {
                    RNGStreamGenerator rngStreamGenerator = ((ILevelProperties)overWorld.getLevelProperties()).getRngStreamGenerator();
                    String seedType = args[0];
                    if (!args[1].equalsIgnoreCase("reset")) {
                        throw new IncorrectUsageException("Usage: /rates OR /rates <seedName> reset", new Object[0]);
                    }
                    for (Map.Entry<String, Long> pair : rngStreamGenerator.entrySet()) {
                        if (pair.getKey().equalsIgnoreCase(seedType)) {
                            rngStreamGenerator.setSeed(pair.getKey(), (new Random()).nextLong());
                            ClientPlayerEntity player = MinecraftClient.getInstance().field_3805;
                            player.addMessage(new TranslatableText("RNG seed " + pair.getKey() + " changed."));
                            RNGStreamGenerator.tellPlayerCurrentRates(overWorld);
                            return;
                        }
                    }
                    throw new IncorrectUsageException("An RNG seed with name " + seedType + " does not exist.", new Object[0]);
                } else {
                    throw new IncorrectUsageException("Usage: /rates OR /rates <seedName> reset", new Object[0]);
                }
            }

            @Override
            public List method_3276(CommandSource source, String[] strings) {
                World overWorld = ((ServerWorld)source.getWorld()).getServer().getWorld();
                RNGStreamGenerator rngStreamGenerator = ((ILevelProperties)overWorld.getLevelProperties()).getRngStreamGenerator();
                if (strings.length == 1) {
                    return method_2894(strings, rngStreamGenerator.keySet().toArray(new String[0]));
                } else if (strings.length == 2) {
                    return method_2894(strings, new String[]{"reset"});
                }
                return null;
            }

            @Override
            public int compareTo(@NotNull Object o) {
                return 0;
            }
        });
    }
}
