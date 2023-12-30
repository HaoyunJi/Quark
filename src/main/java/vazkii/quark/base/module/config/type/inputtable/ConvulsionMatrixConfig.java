package vazkii.quark.base.module.config.type.inputtable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vazkii.quark.api.config.IConfigElement;
import vazkii.quark.base.client.config.screen.CategoryScreen;
import vazkii.quark.base.client.config.screen.WidgetWrapper;
import vazkii.quark.base.client.config.screen.inputtable.ConvulsionMatrixInputScreen;
import vazkii.quark.base.client.config.screen.inputtable.IInputtableConfigType;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.Config;
import vazkii.quark.base.module.config.ConfigFlagManager;
import vazkii.quark.content.client.module.GreenerGrassModule;

public class ConvulsionMatrixConfig extends AbstractInputtableType<ConvulsionMatrixConfig> {

	@Config public List<Double> r;
	@Config public List<Double> g;
	@Config public List<Double> b;

	public final Params params;
	
	public double[] colorMatrix;

	public ConvulsionMatrixConfig(Params params) {
		this.params = params;
		
		double[] defaultMatrix = params.defaultMatrix;
		this.colorMatrix = Arrays.copyOf(defaultMatrix, defaultMatrix.length);

		updateRGB();
	}

	@Override
	public void onReload(QuarkModule module, ConfigFlagManager flagManager) {
		super.onReload(module, flagManager);
		
		try {
			colorMatrix = new double[] {
					r.get(0), r.get(1), r.get(2),
					g.get(0), g.get(1), g.get(2),
					b.get(0), b.get(1), b.get(2)
			};
		} catch(NumberFormatException | ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			colorMatrix = Arrays.copyOf(params.defaultMatrix, params.defaultMatrix.length);
		}
	}

	@Override
	public void inherit(ConvulsionMatrixConfig other, boolean committing) {
		colorMatrix = Arrays.copyOf(other.colorMatrix, other.colorMatrix.length);
		updateRGB();
	}
	
	private void updateRGB() {
		r = Arrays.asList(colorMatrix[0], colorMatrix[1], colorMatrix[2]);
		g = Arrays.asList(colorMatrix[3], colorMatrix[4], colorMatrix[5]);
		b = Arrays.asList(colorMatrix[6], colorMatrix[7], colorMatrix[8]);
	}

	@Override
	public void inheritDefaults(ConvulsionMatrixConfig other) {
		colorMatrix = Arrays.copyOf(other.params.defaultMatrix, other.params.defaultMatrix.length);
	}

	@Override
	public ConvulsionMatrixConfig copy() {
		ConvulsionMatrixConfig newMatrix = new ConvulsionMatrixConfig(params.cloneWithNewDefault(colorMatrix));
		newMatrix.inherit(this, false);
		return newMatrix;
	}

	public int convolve(int color) {
		int r = color >> 16 & 0xFF;
		int g = color >> 8 & 0xFF;
		int b = color & 0xFF;

		int outR = clamp((int) ((double) r * colorMatrix[0] + (double) g * colorMatrix[1] + (double) b * colorMatrix[2]));
		int outG = clamp((int) ((double) r * colorMatrix[3] + (double) g * colorMatrix[4] + (double) b * colorMatrix[5]));
		int outB = clamp((int) ((double) r * colorMatrix[6] + (double) g * colorMatrix[7] + (double) b * colorMatrix[8]));

		return 0xFF000000 | (((outR & 0xFF) << 16) + ((outG & 0xFF) << 8) + (outB & 0xFF));
	}

	private int clamp(int val) {
		return Math.min(0xFF, Math.max(0, val));
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj instanceof ConvulsionMatrixConfig other && Arrays.equals(other.colorMatrix, colorMatrix));
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(colorMatrix);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addWidgets(CategoryScreen parent, IConfigElement element, List<WidgetWrapper> widgets) {
		IInputtableConfigType.addPencil(parent, element, widgets, () -> new ConvulsionMatrixInputScreen(parent, this, element, parent.category));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public String getSubtitle() {
		return "[" + Arrays.stream(colorMatrix).boxed().map(d -> String.format("%.1f", d)).collect(Collectors.joining(", ")) + "]";
	}
	
	public static class Params {
		
		private static final String IDENTITY_NAME = "Vanilla";
		public static final double[] IDENTITY = {
				1, 0, 0,
				0, 1, 0,
				0, 0, 1
			};
		
		public final String name;
		public final String[] biomeNames;
		public final double[] defaultMatrix;
		public final int[] testColors;
		@Nullable public final int[] folliageTestColors;
		
		private final String[] presetNames;
		private final double[][] presets;
		
		public final Map<String, double[]> presetMap;

		public Params(String name, double[] defaultMatrix,  String[] biomeNames, int[] testColors, @Nullable int[] folliageTestColors, String[] presetNames, double[][] presets) {
			Preconditions.checkArgument(defaultMatrix.length == 9);
			Preconditions.checkArgument(biomeNames.length == 6);
			Preconditions.checkArgument(testColors.length == 6);
			Preconditions.checkArgument(folliageTestColors == null || folliageTestColors.length == 6);
			Preconditions.checkArgument(presetNames.length == presets.length);
			
			this.name = name;
			this.defaultMatrix = defaultMatrix;
			this.biomeNames = biomeNames;
			this.testColors = testColors;
			this.folliageTestColors = folliageTestColors;
			
			this.presetNames = presetNames;
			this.presets = presets;
			
			presetMap = new LinkedHashMap<>();
			presetMap.put(IDENTITY_NAME, IDENTITY);
			for(int i = 0; i < presetNames.length; i++)
				presetMap.put(presetNames[i], presets[i]);
		}
		
		public Params cloneWithNewDefault(double[] newDefault) {
			return new Params(name, newDefault, biomeNames, testColors, folliageTestColors, presetNames, presets);
		}
		
		public boolean shouldDisplayFolliage() {
			return folliageTestColors != null && GreenerGrassModule.affectLeaves;
		}
		
	}
	
}
