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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Include;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UseCase;
import org.polarsys.capella.core.data.capellacore.InvolvedElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.cs.ActorCapabilityRealizationInvolvement;
import org.polarsys.capella.core.data.interaction.AbstractCapabilityInclude;
import org.polarsys.capella.core.data.la.CapabilityRealization;
import org.polarsys.capella.core.data.la.CapabilityRealizationPkg;
import org.polarsys.capella.core.data.la.LogicalActor;

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

		List<AbstractCapabilityInclude> includes = new ArrayList<>();
		List<ActorCapabilityRealizationInvolvement> acris = new ArrayList<>();
		for (CapabilityRealization capabilityRealization : ownedCapabilityRealizations) {
			includes.addAll(capabilityRealization.getIncludes());
			acris.addAll(capabilityRealization.getOwnedActorCapabilityRealizations());

			transformCapabilityRealization(capabilityRealization, useCasePkg);
		}
		// includes
		for (AbstractCapabilityInclude include : includes) {
			transformIncludes(include);
		}

		// actor Capability Realization involvements
		for (ActorCapabilityRealizationInvolvement acri : acris) {
			CapabilityRealization cr = (CapabilityRealization) acri.eContainer();
			InvolvedElement involved = acri.getInvolved();

			if (involved instanceof LogicalActor) {
				UseCase usecase = (UseCase) MappingRulesManager.getCapellaObjectFromAllRules(cr);
				Actor umlActor = (Actor) MappingRulesManager.getCapellaObjectFromAllRules(involved);

				Association association = UMLFactory.eINSTANCE.createAssociation();
				useCasePkg.getPackagedElements().add(association);
				Sysml2CapellaUtils.trace(this, _source.eResource(), acri, association, "_ASSOCIATION");

				Property propertySource = association.createOwnedEnd("", usecase);
				Property propertyTarget = association.createOwnedEnd("", umlActor);

				Sysml2CapellaUtils.trace(this, _source.eResource(), acri + "SOURCE", propertySource, "SOURCE_END_");
				Sysml2CapellaUtils.trace(this, _source.eResource(), acri + "TARGET", propertyTarget, "TARGET_END_");

			}
		}

	}

	/**
	 * @param include
	 */
	private void transformIncludes(AbstractCapabilityInclude include) {
		CapabilityRealization cr = (CapabilityRealization) include.eContainer();
		CapabilityRealization addition = (CapabilityRealization) include.getIncluded();

		UseCase object = (UseCase) getMapSourceToTarget().get(cr);
		UseCase add = (UseCase) getMapSourceToTarget().get(addition);

		Include umlInclude = object.createInclude("", add);
		Sysml2CapellaUtils.trace(this, _source.eResource(), include, umlInclude, "INCLUDE_");
	}

	private void transformCapabilityRealization(CapabilityRealization capabilityRealization, Package useCasePkg) {
		UseCase useCase = UMLFactory.eINSTANCE.createUseCase();
		useCase.setName(capabilityRealization.getName());
		useCasePkg.getPackagedElements().add(useCase);
		Sysml2CapellaUtils.trace(this, _source.eResource(), capabilityRealization, useCase, "USECASE_");
	}

}
