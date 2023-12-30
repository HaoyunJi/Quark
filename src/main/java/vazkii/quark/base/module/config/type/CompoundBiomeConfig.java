package vazkii.quark.base.module.config.type;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.ConfigFlagManager;

public class CompoundBiomeConfig extends AbstractConfigType implements IBiomeConfig {

	@Config(description = "Biome tags for which this should spawn in. Must match both this and 'biomes' to spawn.")
	public BiomeTagConfig tags;

	@Config(description = "Biome names this should spawn in. Must match both this and 'types' to spawn.")
	public StrictBiomeConfig biomes;

	private CompoundBiomeConfig(BiomeTagConfig tags, StrictBiomeConfig biomes) {
		this.tags = tags;
		this.biomes = biomes;
	}

	@SafeVarargs
	public static CompoundBiomeConfig fromBiomeTags(boolean isBlacklist, TagKey<Biome>... typesIn) {
		return new CompoundBiomeConfig(new BiomeTagConfig(isBlacklist, typesIn), noSBC());
	}

	public static CompoundBiomeConfig fromBiomeTagStrings(boolean isBlacklist, String... typesIn) {
		return new CompoundBiomeConfig(BiomeTagConfig.fromStrings(isBlacklist, typesIn), noSBC());
	}

	public static CompoundBiomeConfig fromBiomeReslocs(boolean isBlacklist, String... typesIn) {
		return new CompoundBiomeConfig(noBTC(), new StrictBiomeConfig(isBlacklist, typesIn));
	}

	public static CompoundBiomeConfig all() {
		return new CompoundBiomeConfig(noBTC(), noSBC());
	}

	private static BiomeTagConfig noBTC() {
		return new BiomeTagConfig(true);
	}


	private static StrictBiomeConfig noSBC() {
		return new StrictBiomeConfig(true);
	}

	@Override
	public void onReload(QuarkModule module, ConfigFlagManager flagManager) {
		tags.onReload(module, flagManager);
		biomes.onReload(module, flagManager);
	}

	@Override
	public boolean canSpawn(Holder<Biome> b) {
		return b != null && tags.canSpawn(b) && biomes.canSpawn(b);
	}

}
