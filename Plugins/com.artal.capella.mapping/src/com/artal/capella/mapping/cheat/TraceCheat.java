package com.artal.capella.mapping.cheat;

import org.eclipse.emf.ecore.EObject;

public class TraceCheat<T extends EObject> {
	private String _causeValue;
	private T _target;

	public TraceCheat(String causeValue, T target) {
		_causeValue = causeValue;
		_target = target;
	}

	public String getCause() {
		return _causeValue;
	}

	public T getTarget() {
		return _target;
	}
}
