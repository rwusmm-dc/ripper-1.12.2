package com.rayferric.havook.feature.mod;

public class ModAttributeInteger extends ModAttribute {
	public int value;
	private final transient int nativeValue;

	public ModAttributeInteger(String name, int nativeValue) {
		super(name);
		this.nativeValue = nativeValue;
		value = nativeValue;
	}

	public int getNativeValue() {
		return nativeValue;
	}
}