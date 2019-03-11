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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.bridge.api.IBridge;
import org.eclipse.emf.diffmerge.bridge.mapping.impl.emf.EMFMappingBridge;
import org.eclipse.uml2.uml.Model;

import com.artal.capella.mapping.CapellaExtensionBridgeJob;

/**
 * @author binot
 *
 */
public class Sysml2CapellaBridgeJob extends CapellaExtensionBridgeJob<Model> {

	public Sysml2CapellaBridgeJob(String jobName_p, Model sourceDataSet_p, URI targetURI_p) {
		// super(jobName_p, sourceDataSet_p, targetURI_p);
		super(sourceDataSet_p, targetURI_p, new Sysml2CapellaAlgo());
	}

	@Override
	protected IBridge<Model, IEditableModelScope> createMappingBridge() {
		return new EMFMappingBridge<>();
	}

	@Override
	protected void setupLogger() {
		// TODO Auto-generated method stub
	}

}