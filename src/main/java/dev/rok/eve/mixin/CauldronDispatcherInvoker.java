package dev.rok.eve.mixin;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the package-private CauldronInteraction.Dispatcher#put so we can register
 *  the upgraded shulker box wash interaction. */
@Mixin(CauldronInteraction.Dispatcher.class)
public interface CauldronDispatcherInvoker {
	@Invoker("put")
	void eve$put(Item item, CauldronInteraction interaction);
}
