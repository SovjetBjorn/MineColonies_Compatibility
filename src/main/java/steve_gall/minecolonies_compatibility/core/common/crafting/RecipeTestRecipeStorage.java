package steve_gall.minecolonies_compatibility.core.common.crafting;

import java.util.List;

import com.minecolonies.api.crafting.ItemStorage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import steve_gall.minecolonies_compatibility.api.common.crafting.SimpleGenericRecipe;
import steve_gall.minecolonies_compatibility.api.common.crafting.SimpleRecipeStorage;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;

public class RecipeTestRecipeStorage extends SimpleRecipeStorage<SimpleGenericRecipe>
{
	public static final ResourceLocation ID = MineColoniesCompatibility.rl("recipe_test");

	public RecipeTestRecipeStorage(CompoundTag tag)
	{
		super(tag);
	}

	public RecipeTestRecipeStorage(RecipeTest recipe, List<ItemStorage> ingredients, ItemStack output)
	{
		super(recipe.getId(), ingredients, output);
	}

	@Override
	public ResourceLocation getId()
	{
		return ID;
	}

	@Override
	protected GenericRecipeFactory<SimpleGenericRecipe> getGenericRecipeFactory()
	{
		return SimpleGenericRecipe::new;
	}

}
