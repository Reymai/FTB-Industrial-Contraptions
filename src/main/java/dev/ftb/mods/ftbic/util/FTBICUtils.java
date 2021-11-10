package dev.ftb.mods.ftbic.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftbic.FTBICConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FTBICUtils {
	public static final Tag.Named<Item> UNCANNABLE_FOOD = ItemTags.createOptional(new ResourceLocation("ftbic:uncannable_food"));
	public static final Tag.Named<Item> NO_AUTO_RECIPE = ItemTags.createOptional(new ResourceLocation("ftbic:no_auto_recipe"));

	public static void init() {
	}

	public static String formatEnergyValue(double energy) {
		return String.format("%,d", (long) energy);
	}

	public static TextComponent formatEnergy(double energy) {
		return (TextComponent) new TextComponent("").append(formatEnergyValue(energy) + " ").append(FTBICConfig.ENERGY_FORMAT);
	}

	public static TextComponent formatEnergy(double energy, double cap) {
		return (TextComponent) new TextComponent("").append(formatEnergyValue(energy) + " / " + formatEnergyValue(cap) + " ").append(FTBICConfig.ENERGY_FORMAT);
	}

	public static <T> List<T> listFromJson(JsonObject json, String key, Function<JsonElement, T> function) {
		if (json.has(key)) {
			JsonArray array = json.get(key).getAsJsonArray();

			if (array.size() == 0) {
				return Collections.emptyList();
			} else if (array.size() == 1) {
				return Collections.singletonList(function.apply(array.get(0)));
			}

			List<T> list = new ArrayList<>(array.size());

			for (JsonElement e : array) {
				list.add(function.apply(e));
			}

			return list;
		}

		return Collections.emptyList();
	}

	public static <T> void listToJson(List<T> list, JsonObject json, String key, Function<T, JsonElement> function) {
		JsonArray array = new JsonArray();

		for (T obj : list) {
			array.add(function.apply(obj));
		}

		if (array.size() > 0) {
			json.add(key, array);
		}
	}

	public static <T> List<T> listFromNet(FriendlyByteBuf buf, Function<FriendlyByteBuf, T> function) {
		int size = buf.readVarInt();

		if (size == 0) {
			return Collections.emptyList();
		} else if (size == 1) {
			return Collections.singletonList(function.apply(buf));
		}

		List<T> list = new ArrayList<>(size);

		for (int i = 0; i < size; i++) {
			list.add(function.apply(buf));
		}

		return list;
	}

	public static <T> void listToNet(List<T> list, FriendlyByteBuf buf, BiConsumer<T, FriendlyByteBuf> function) {
		buf.writeVarInt(list.size());

		for (T obj : list) {
			function.accept(obj, buf);
		}
	}

	public static FluidStack fluidFromJson(JsonElement element) {
		if (element == null || element.isJsonNull()) {
			return FluidStack.EMPTY;
		} else if (element.isJsonObject()) {
			JsonObject o = element.getAsJsonObject();
			Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(o.get("fluid").getAsString()));
			int amount = o.has("amount") ? o.get("amount").getAsInt() : FluidAttributes.BUCKET_VOLUME;
			return new FluidStack(fluid, amount);
		}

		Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(element.getAsString()));
		return new FluidStack(fluid, FluidAttributes.BUCKET_VOLUME);
	}

	public static JsonElement fluidToJson(FluidStack stack) {
		JsonObject json = new JsonObject();
		json.addProperty("fluid", stack.getFluid().getRegistryName().toString());
		json.addProperty("amount", stack.getAmount());
		return json;
	}
}
