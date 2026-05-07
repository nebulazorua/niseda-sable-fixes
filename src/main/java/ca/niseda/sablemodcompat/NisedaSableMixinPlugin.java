package ca.niseda.sablemodcompat;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NisedaSableMixinPlugin implements IMixinConfigPlugin {
    private static boolean classExists(String cls) {
        try {
            Class.forName(cls, false, NisedaSableMixinPlugin.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean hasOritech = false;
    private boolean hasCreateFluid = false;
    private boolean hasFigura = false;

    @Override
    public void onLoad(String mixinPackage) {
        hasOritech = classExists("rearth.oritech.Oritech");
        hasCreateFluid = classExists("com.adonis.fluid.CreateFluid");
        hasFigura = classExists("org.figuramc.figura.FiguraMod");

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(mixinClassName.startsWith("ca.niseda.sablemodcompat.mixin.oritech")){
            return hasOritech;
        }else if(mixinClassName.startsWith("ca.niseda.sablemodcompat.mixin.fluid")){
            return hasCreateFluid;
        }else if(mixinClassName.startsWith("ca.niseda.sablemodcompat.mixin.figura")){
            return hasFigura;
        }

        return true; // Likely a fix to Sable itself, always allow
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

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
