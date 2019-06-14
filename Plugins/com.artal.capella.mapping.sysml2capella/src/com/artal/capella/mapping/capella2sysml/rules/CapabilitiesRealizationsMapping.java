/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml.rules;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UseCase;
import org.polarsys.capella.core.data.capellacommon.AbstractCapabilityPkg;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.la.CapabilityRealization;
import org.polarsys.capella.core.data.la.CapabilityRealizationPkg;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class CapabilitiesRealizationsMapping extends AbstractMapping {

	Project _source;
	IMappingExecution _mappingExecution;

	public CapabilitiesRealizationsMapping(CapellaBridgeAlgo<?> algo, Project source_p,
			IMappingExecution mappingExecution) {
		super(algo);
		_source = source_p;
		_mappingExecution = mappingExecution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.rules.AbstractMapping#computeMapping()
	 */
	@Override
	public void computeMapping() {

		CapabilityRealizationPkg capabilityRealizationPkg = (CapabilityRealizationPkg) Sysml2CapellaUtils
				.getCapabilityRealizationPkg(_source);
		EList<CapabilityRealization> ownedCapabilityRealizations = capabilityRealizationPkg
				.getOwnedCapabilityRealizations();
		Package useCasePkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "USECASES");

		for (CapabilityRealization capabilityRealization : ownedCapabilityRealizations) {
			transformCapabilityRealization(capabilityRealization, useCasePkg);
		}

	}

	private void transformCapabilityRealization(CapabilityRealization capabilityRealization, Package useCasePkg) {
		UseCase useCase = UMLFactory.eINSTANCE.createUseCase();
		useCase.setName(capabilityRealization.getName());
		useCasePkg.getPackagedElements().add(useCase);
		Sysml2CapellaUtils.trace(this, _source.eResource(), capabilityRealization, useCase, "USECASE_");
	}

}
