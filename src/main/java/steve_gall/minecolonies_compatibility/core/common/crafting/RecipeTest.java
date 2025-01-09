package steve_gall.minecolonies_compatibility.core.common.crafting;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import com.minecolonies.api.MinecoloniesAPIProxy;
import com.minecolonies.api.colony.jobs.registry.JobEntry;
import com.minecolonies.api.util.constant.Constants;

import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import steve_gall.minecolonies_compatibility.core.common.init.ModRecipes;
import steve_gall.minecolonies_tweaks.core.common.util.GsonHelper2;

public class RecipeTest implements Recipe<Container>
{
	public static record TestResult(boolean test, CraftingRecipe recipe)
	{
		public boolean matches(ItemStack item)
		{
			return this.test() && ItemStack.matches(recipe.getResultItem(), item);
		}

	}

	public static TestResult test(RecipeManager recipeManager, ResourceLocation recipeId)
	{
		if (recipeManager.byKey(recipeId).orElse(null) instanceof RecipeTest test)
		{
			return test(recipeManager, test);
		}
		else
		{
			return new TestResult(false, null);
		}

	}

	public static TestResult test(RecipeManager recipeManager, RecipeTest recipe)
	{
		if (recipe == null)
		{
			return new TestResult(false, null);
		}

		if (!(recipeManager.byKey(recipe.getTarget()).orElse(null) instanceof CraftingRecipe target))
		{
			return new TestResult(false, null);
		}

		var recipeIngredients = recipe.getIngredientItems();
		var targetIngredients = target.getIngredients();

		if (recipeIngredients.size() != targetIngredients.size())
		{
			return new TestResult(false, target);
		}

		var remains = new ArrayList<>(recipeIngredients);

		for (var ingredient : targetIngredients)
		{
			for (var i = 0; i < remains.size(); i++)
			{
				var remain = remains.get(i);

				if (ingredient.test(new ItemStack(remain)))
				{
					remains.remove(i);
					break;
				}

			}

		}

		return new TestResult(remains.size() == 0 && recipe.getResult() == target.getResultItem().getItem(), target);
	}

	@NotNull
	private final ResourceLocation id;
	@NotNull
	private final JobEntry job;
	@NotNull
	private final ResourceLocation target;
	@NotNull
	private final List<Item> ingredients;
	private final Item result;

	public RecipeTest(@NotNull ResourceLocation id, @NotNull JobEntry job, @NotNull ResourceLocation target, @NotNull List<Item> ingredients, Item result)
	{
		this.id = id;
		this.job = job;
		this.target = target;
		this.ingredients = ImmutableList.copyOf(ingredients);
		this.result = result;
	}

	@Override
	public boolean matches(Container container, Level level)
	{
		return false;
	}

	@Override
	public ItemStack assemble(Container container)
	{
		return this.getResultItem().copy();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return false;
	}

	@Override
	public ItemStack getResultItem()
	{
		return ItemStack.EMPTY;
	}

	@Override
	public @NotNull ResourceLocation getId()
	{
		return this.id;
	}

	public @NotNull JobEntry getJob()
	{
		return this.job;
	}

	public @NotNull ResourceLocation getTarget()
	{
		return this.target;
	}

	@Override
	public @NotNull NonNullList<Ingredient> getIngredients()
	{
		var ingredients = NonNullList.withSize(this.ingredients.size(), Ingredient.EMPTY);

		for (var i = 0; i < ingredients.size(); i++)
		{
			ingredients.set(0, Ingredient.of(this.ingredients.get(i)));
		}

		return ingredients;
	}

	public @NotNull NonNullList<Item> getIngredientItems()
	{
		var items = NonNullList.withSize(this.ingredients.size(), Items.AIR);

		for (var i = 0; i < items.size(); i++)
		{
			items.set(0, this.ingredients.get(i));
		}

		return items;
	}

	public @NotNull Item getResult()
	{
		return this.result;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return ModRecipes.TEST_SERIALIER.get();
	}

	@Override
	public RecipeType<?> getType()
	{
		return ModRecipes.TEST_TYPE.get();
	}

	public static class Serializer implements RecipeSerializer<RecipeTest>
	{
		@Override
		public RecipeTest fromJson(ResourceLocation id, JsonObject json)
		{
			var jobId = GsonHelper2.getAsResourceLocation(json, "job", Constants.MOD_ID);
			var job = MinecoloniesAPIProxy.getInstance().getJobRegistry().getValue(jobId);

			if (job == null)
			{
				throw new RuntimeException("Invalid job: " + jobId);
			}

			var target = GsonHelper2.getAsResourceLocation(json, "target");
			var ingredients = new ArrayList<Item>();

			for (var j : GsonHelper.getAsJsonArray(json, "ingredients"))
			{
				var itemId = new ResourceLocation(j.getAsString());
				var item = ForgeRegistries.ITEMS.getValue(itemId);

				if (item == null)
				{
					throw new RuntimeException("Invalid item: " + itemId);
				}

				ingredients.add(item);
			}

			var resultId = GsonHelper2.getAsResourceLocation(json, "result");
			var result = ForgeRegistries.ITEMS.getValue(resultId);

			if (result == null)
			{
				throw new RuntimeException("Invalid result: " + resultId);
			}

			return new RecipeTest(id, job, target, ingredients, result);
		}

		@Override
		public @Nullable RecipeTest fromNetwork(ResourceLocation id, FriendlyByteBuf buffer)
		{
			var job = buffer.readRegistryIdUnsafe(MinecoloniesAPIProxy.getInstance().getJobRegistry());
			var target = buffer.readResourceLocation();
			var ingredients = buffer.readList(b -> b.readRegistryIdUnsafe(ForgeRegistries.ITEMS));
			var result = buffer.readRegistryIdUnsafe(ForgeRegistries.ITEMS);
			return new RecipeTest(id, job, target, ingredients, result);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, RecipeTest recipe)
		{
			buffer.writeRegistryIdUnsafe(MinecoloniesAPIProxy.getInstance().getJobRegistry(), recipe.job);
			buffer.writeResourceLocation(recipe.target);
			buffer.writeCollection(recipe.ingredients, (b, i) -> b.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, i));
			buffer.writeRegistryIdUnsafe(ForgeRegistries.ITEMS, recipe.result);
		}

	}

}
