package ca.niseda.sablemodcompat.mixin.oritech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import rearth.oritech.block.blocks.reactor.NukeBlock;

@Mixin(NukeBlock.class)
public interface NukeBlockAccessor {
    @Accessor("small")
    abstract boolean getSmall();

}
