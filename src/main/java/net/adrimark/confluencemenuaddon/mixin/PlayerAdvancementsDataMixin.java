package net.adrimark.confluencemenuaddon.mixin;

import org.spongepowered.asm.mixin.Mixin;

// Empty on purpose - just exists so Mixin treats this as a transform target and calls
// ConfluenceMenuAddonMixinPlugin#preApply for it. Targeted by string since the record is
// package-private.
@Mixin(targets = "net.minecraft.server.PlayerAdvancements$Data")
public abstract class PlayerAdvancementsDataMixin {
}
