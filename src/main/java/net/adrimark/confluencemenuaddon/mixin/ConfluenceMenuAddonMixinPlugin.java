package net.adrimark.confluencemenuaddon.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

// Makes PlayerAdvancements.Data's record constructor public by editing its access flags
// directly instead of going through an AT entry. Can't use an AT here - Confluence ships a
// malformed one for this same constructor, and since it loads first, its broken entry poisons
// the merge map before ours ever gets a chance to apply. The Mixin plugin hook runs in a
// separate pipeline, so it's unaffected.
public final class ConfluenceMenuAddonMixinPlugin implements IMixinConfigPlugin {

    private static final String TARGET_CLASS = "net.minecraft.server.PlayerAdvancements$Data";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (!TARGET_CLASS.equals(targetClassName)) {
            return;
        }
        for (MethodNode method : targetClass.methods) {
            if ("<init>".equals(method.name)) {
                method.access = (method.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
            }
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
