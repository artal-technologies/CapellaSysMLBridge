/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.rules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Include;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.UseCase;
import org.polarsys.capella.core.data.interaction.AbstractCapabilityInclude;
import org.polarsys.capella.core.data.interaction.InteractionFactory;
import org.polarsys.capella.core.data.la.CapabilityRealization;
import org.polarsys.capella.core.data.la.CapabilityRealizationPkg;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalActor;
import org.polarsys.capella.core.data.la.LogicalActorPkg;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class UseCaseMapping extends AbstractMapping {
	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Sysml2CapellaAlgo} algo.
	 * @param source
	 *            the {@link Model} sysml model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public UseCaseMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
		_mappingExecution = mappingExecution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.rules.AbstractMapping#computeMapping()
	 */
	@Override
	public void computeMapping() {
		List<UseCase> useCases = Sysml2CapellaUtils.getUseCases(_source, "02 Behavior/02 Use Cases");
		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		CapabilityRealizationPkg capabilityRealPkg = (CapabilityRealizationPkg) Sysml2CapellaUtils
				.getCapabilityRealizationPkg(targetScope.getProject());

		List<Include> includes = new ArrayList<Include>();
		for (UseCase useCase : useCases) {
			CapabilityRealization cr = LaFactory.eINSTANCE.createCapabilityRealization();
			cr.setName(useCase.getName());

			capabilityRealPkg.getOwnedCapabilityRealizations().add(cr);
			Sysml2CapellaUtils.trace(this, eResource, useCase, cr, "CapabilityRealization_");

			includes.addAll(useCase.getIncludes());
		}
		
		for (Include include : includes) {
			UseCase useCase = (UseCase) include.eContainer();
			UseCase addition = include.getAddition();
			CapabilityRealization add = (CapabilityRealization) getMapSourceToTarget().get(addition);

			AbstractCapabilityInclude cinclude = InteractionFactory.eINSTANCE.createAbstractCapabilityInclude();
			cinclude.setIncluded(add);
			CapabilityRealization object = (CapabilityRealization) getMapSourceToTarget().get(useCase);
			object.getIncludes().add(cinclude);

			Sysml2CapellaUtils.trace(this, eResource, include, cinclude, "AbstractCapabilityInclude_");

		}

	}

}
