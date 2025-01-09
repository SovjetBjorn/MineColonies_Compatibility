package steve_gall.minecolonies_compatibility.core.common.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import steve_gall.minecolonies_compatibility.core.common.MineColoniesCompatibility;
import steve_gall.minecolonies_compatibility.core.common.crafting.RecipeTest;

public class ModRecipes
{
	public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, MineColoniesCompatibility.MOD_ID);
	public static final RegistryObject<RecipeType<RecipeTest>> TEST_TYPE = registerType("test");

	public static final DeferredRegister<RecipeSerializer<?>> SERIALIERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MineColoniesCompatibility.MOD_ID);
	public static final RegistryObject<RecipeSerializer<RecipeTest>> TEST_SERIALIER = SERIALIERS.register("test", RecipeTest.Serializer::new);

	private static <RECIPE extends Recipe<?>> RegistryObject<RecipeType<RECIPE>> registerType(String name)
	{
		return TYPES.register("test", () -> RecipeType.simple(new ResourceLocation(MineColoniesCompatibility.MOD_ID, name)));
	}

}
