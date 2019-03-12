/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.cheat;

import org.eclipse.emf.ecore.EObject;

/**
 * @author YBI
 *
 * @param <T>
 */
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
