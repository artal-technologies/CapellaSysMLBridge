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
