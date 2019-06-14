/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml;

import org.eclipse.emf.common.util.URI;
import org.polarsys.capella.core.data.capellamodeller.Project;

import com.artal.capella.mapping.uml.UMLBridgeJob;

/**
 * @author YBI
 *
 */
public class Capella2SysmlBridgeJob extends UMLBridgeJob<Project> {

	public Capella2SysmlBridgeJob(String jobName_p, Project sourceDataSet_p, URI targetURI_p) {
		super(sourceDataSet_p, targetURI_p, new Capella2SysmlAlgo());
	}

	@Override
	protected void setupLogger() {
	}

}
