package vazkii.quark.base.module.config.type;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.ConfigFlagManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BiomeTagConfig extends AbstractConfigType implements IBiomeConfig {

	private final Object mutex = new Object();

	@Config(name = "Biome Tags")
	private List<String> biomeTagStrings;

	@Config
	private boolean isBlacklist;

	private List<TagKey<Biome>> tags;

	@SafeVarargs
	protected BiomeTagConfig(boolean isBlacklist, TagKey<Biome>... tagsIn) {
		this.isBlacklist = isBlacklist;

		biomeTagStrings = new LinkedList<>();
		for(TagKey<Biome> t : tagsIn)
			biomeTagStrings.add(t.location().toString());
	}

	private BiomeTagConfig(boolean isBlacklist, String... types) {
		this.isBlacklist = isBlacklist;

		biomeTagStrings = new LinkedList<>();
		biomeTagStrings.addAll(Arrays.asList(types));
	}

	protected static BiomeTagConfig fromStrings(boolean isBlacklist, String... types) {
		return new BiomeTagConfig(isBlacklist, types);
	}

	@Override
	public boolean canSpawn(Holder<Biome> biome) {
		if(biome == null)
			return false;

		synchronized (mutex) {
			if(tags == null)
				updateTypes();

			for(TagKey<Biome> tag : tags) {
				if(biome.is(tag))
					return !isBlacklist;
			}

			return isBlacklist;
		}
	}

	@Override
	public void onReload(QuarkModule module, ConfigFlagManager flagManager) {
		synchronized (mutex) {
			updateTypes();
		}
	}

	public void updateTypes() {
		tags = new LinkedList<>();
		for (String s : biomeTagStrings) {
			TagKey<Biome> tag = TagKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(s));

			if(tag != null)
				tags.add(tag);
		}
	}

}
