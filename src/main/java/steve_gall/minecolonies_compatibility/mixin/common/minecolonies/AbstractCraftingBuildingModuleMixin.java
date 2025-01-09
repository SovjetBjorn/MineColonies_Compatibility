package steve_gall.minecolonies_compatibility.mixin.common.minecolonies;

import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.buildings.modules.AbstractBuildingModule;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.colony.requestsystem.requestable.IDeliverable;
import com.minecolonies.api.colony.requestsystem.token.IToken;
import com.minecolonies.api.crafting.IRecipeStorage;
import com.minecolonies.api.crafting.ItemStorage;
import com.minecolonies.core.colony.buildings.modules.AbstractCraftingBuildingModule;
import com.minecolonies.core.colony.crafting.CustomRecipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import steve_gall.minecolonies_compatibility.core.common.crafting.RecipeTest;
import steve_gall.minecolonies_compatibility.core.common.crafting.RecipeTestRecipeStorage;
import steve_gall.minecolonies_compatibility.core.common.init.ModRecipes;
import steve_gall.minecolonies_tweaks.api.common.crafting.ICustomizableRecipeStorage;

@Mixin(value = AbstractCraftingBuildingModule.class, remap = false)
public abstract class AbstractCraftingBuildingModuleMixin extends AbstractBuildingModule
{
	@Shadow(remap = false)
	private JobEntry jobEntry;

	@Shadow(remap = false)
	private List<IToken<?>> recipes;

	@Shadow(remap = false)
	private List<IToken<?>> disabledRecipes;

	@Inject(method = "checkForWorkerSpecificRecipes", remap = false, at = @At(value = "HEAD"), cancellable = true)
	private void checkForWorkerSpecificRecipes(CallbackInfo ci)
	{
		if (this.jobEntry == null)
		{
			return;
		}

		var colony = this.building.getColony();
		var vanillaRecipeManager = colony.getWorld().getRecipeManager();
		var colonyRecipeManager = MinecoloniesAPIProxy.getInstance().getColonyManager().getRecipeManager();

		for (var test : vanillaRecipeManager.getAllRecipesFor(ModRecipes.TEST_TYPE.get()))
		{
			if (test.getJob() != this.jobEntry)
			{
				continue;
			}

			var result = RecipeTest.test(vanillaRecipeManager, test);
			var recipe = result.recipe();

			if (result.test() && recipe != null)
			{
				var resultItem = recipe.getResultItem();
				var recipeToken = colonyRecipeManager.checkOrAddRecipe(new RecipeTestRecipeStorage(test, //
						test.getIngredientItems().stream().map(i -> new ItemStorage(new ItemStack(i))).toList(), //
						resultItem).wrap());

				if (!this.recipes.contains(recipeToken))
				{
					this.recipes.add(recipeToken);
					this.disabledRecipes.add(recipeToken);
					colony.getRequestManager().onColonyUpdate(request -> request.getRequest() instanceof IDeliverable iDeliverable && iDeliverable.matches(resultItem));
					this.markDirty();
				}

			}

		}

	}

	@Inject(method = "isPreTaughtRecipe", remap = false, at = @At(value = "HEAD"), cancellable = true)
	private void isPreTaughtRecipe(IRecipeStorage storage, Map<ResourceLocation, CustomRecipe> crafterRecipes, CallbackInfoReturnable<Boolean> cir)
	{
		if (storage instanceof ICustomizableRecipeStorage parent && parent.getImpl() instanceof RecipeTestRecipeStorage recipeStorage)
		{
			var result = RecipeTest.test(this.building.getColony().getWorld().getRecipeManager(), recipeStorage.getRecipeId());
			cir.setReturnValue(result.matches(recipeStorage.getPrimaryOutput()));
		}

	}

}
