package mcjty.efab.recipes;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.fluids.FluidStack;
import scala.actors.threadpool.Arrays;

import javax.annotation.Nonnull;
import java.util.*;

public class EFabShapelessRecipe extends ShapelessRecipes implements IEFabRecipe {

    @Nonnull private final Set<RecipeTier> requiredTiers = EnumSet.noneOf(RecipeTier.class);
    @Nonnull private final List<FluidStack> requiredFluids = new ArrayList<>();
    private int requiredRfPerTick = 0;
    private int craftTime = 0;

    public EFabShapelessRecipe(ItemStack[] ingredientsIn, ItemStack output) {
        super(output, Arrays.asList(ingredientsIn));
    }

    public EFabShapelessRecipe tier(RecipeTier tier) {
        requiredTiers.add(tier);
        return this;
    }

    public EFabShapelessRecipe fluid(FluidStack stack) {
        requiredFluids.add(stack);
        return this;
    }

    public EFabShapelessRecipe rfPerTick(int rf) {
        requiredRfPerTick = rf;
        return this;
    }

    public EFabShapelessRecipe time(int t) {
        craftTime = t;
        return this;
    }

    @Nonnull
    @Override
    public Collection<FluidStack> getRequiredFluids() {
        return requiredFluids;
    }

    @Override
    public int getRequiredRfPerTick() {
        return requiredRfPerTick;
    }

    @Override
    public int getCraftTime() {
        return craftTime;
    }

    @Nonnull
    @Override
    public Set<RecipeTier> getRequiredTiers() {
        return requiredTiers;
    }

    @Nonnull
    @Override
    public IRecipe cast() {
        return this;
    }
}
