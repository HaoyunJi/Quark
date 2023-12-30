package vazkii.quark.base.module.config.type.inputtable;

import vazkii.quark.base.client.config.screen.inputtable.IInputtableConfigType;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.config.ConfigFlagManager;
import vazkii.quark.base.module.config.type.AbstractConfigType;

public abstract class AbstractInputtableType<T extends IInputtableConfigType<T>> extends AbstractConfigType implements IInputtableConfigType<T> {

	private boolean dirty = false;
	
	@Override
	public void markDirty(boolean dirty) {
		this.dirty = dirty;
		
		if(category != null) {
			category.refresh();
			category.updateDirty();
		}
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	@Override
	public void onReload(QuarkModule module, ConfigFlagManager flagManager) {
		super.onReload(module, flagManager);
		dirty = false;
	}
	

}
