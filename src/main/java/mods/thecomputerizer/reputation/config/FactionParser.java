package mods.thecomputerizer.reputation.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FactionParser {

	/* Archived & marked for deletion

	public static void readFactionsFromConfig() {
		File directory = FMLPaths.GAMEDIR.get().resolve("config").resolve("reputation").toFile();
		try {
			copyFactionsFromData(directory);
		} catch (Exception e) {
			Reputation.logError("failed to write file", e);
		}
		for (File file : Objects.requireNonNull(directory.toPath().resolve("factions").toFile().listFiles())) {
			if (file.getName().endsWith(".json")) {
				try {
					String name = file.getName().replace(".json", "");
					StringBuilder builder = new StringBuilder();
					BufferedReader reader = new BufferedReader(new FileReader(file));
					reader.lines().forEach(builder::append);
					reader.close();
					Faction faction = Faction.fromJson(name, builder.toString());
					assert faction != null;
					ReputationHandler.registerFaction(faction);
				} catch (Exception e) {
					Reputation.logError("failed to load file " + file.getName(), e);
				}
			}
		}
	}

	private static void copyFactionsFromData(File directory) throws Exception {
		for(String faction : getFactionList(directory)) {
			copyFile(directory, "factions/"+faction+".json");
		}
		copyFile(directory, "resources/pack.mcmeta");
		copyFile(directory, "resources/assets/reputation/lang/en_us.json");
	}

	private static void copyFile(File directory, String path) throws Exception {
		File output = new File(directory, path);
		if(output.exists()) output.delete();
		File dir = output.getParentFile();
		if (dir!=null)dir.mkdirs();
		Resource resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(ModDefinitions.MODID, "faction_resources/"+path));
		FileUtils.copyInputStreamToFile(resource.getInputStream(), new File(directory, path));
	}

	private static List<String> getFactionList(File directory) throws Exception {
		File output = new File(directory, "factionsList.json");
		if(output.exists()) output.delete();
		File dir = output.getParentFile();
		if (dir!=null)dir.mkdirs();
		Resource resource = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(ModDefinitions.MODID, "faction_resources/"+ "factionsList.json"));
		FileUtils.copyInputStreamToFile(resource.getInputStream(), new File(directory, "factionsList.json"));
		return fromJson(output);
	}

	private static List<String> fromJson(File file) {
		List<String> ret = new ArrayList<>();
		try {
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(new FileReader(file));
			reader.lines().forEach(builder::append);
			reader.close();
			JsonObject json = (JsonObject) JsonParser.parseString(builder.toString());
			for(JsonElement jsone : json.getAsJsonArray("factions")) {
				ret.add(jsone.getAsString());
			}
		} catch (Exception e) {
			Reputation.logError("Could not parse faction list!", new Exception());
		}
		return ret;
	}

	 */
}
