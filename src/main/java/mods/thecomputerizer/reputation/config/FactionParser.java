package mods.thecomputerizer.reputation.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

import mods.thecomputerizer.reputation.api.Faction;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.Reputation;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.apache.commons.io.FileUtils;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class FactionParser {

	public static void readFactionsFromConfig() {
		File directory = FMLPaths.GAMEDIR.get().resolve("config").resolve("reputation").toFile();
		if (!directory.toPath().resolve("factions").toFile().exists()) {
			try {
				createDefaultFiles(directory);
			} catch (Exception e) {
				Reputation.logError("failed to write file", e);
			}
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

	private static void createDefaultFiles(File directory) throws Exception {
		ModFile mod = FMLLoader.getLoadingModList().getModFileById(ModDefinitions.MODID).getFile();
		copyFile(mod, directory, "factions/illager.json");
		copyFile(mod, directory, "factions/nether.json");
		copyFile(mod, directory, "factions/piglin.json");
		copyFile(mod, directory, "factions/skeleton.json");
		copyFile(mod, directory, "factions/villager.json");
		copyFile(mod, directory, "resources/pack.mcmeta");
		copyFile(mod, directory, "resources/assets/reputation/lang/en_us.json");
	}

	private static void copyFile(ModFile mod, File directory, String path) throws Exception {
		File output = new File(directory, path);
		File dir = output.getParentFile();
		if (dir!=null)dir.mkdirs();
		FileUtils.copyInputStreamToFile(Files.newInputStream(mod.findResource("config-defaults/"+path), StandardOpenOption.READ), new File(directory, path));
	}

}
