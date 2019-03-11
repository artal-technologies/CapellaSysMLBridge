/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.artal.capella.mapping.sysml2capella;

import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.emf.EMFMappingBridge;
import org.eclipse.uml2.uml.Model;

/**
 * @author YBI
 *
 */
public class Sysml2CapellaBridge extends EMFMappingBridge<Model, IEditableModelScope> {

	public Sysml2CapellaBridge() {

		ComponentRule componentRule = new ComponentRule(this);

	}

}
