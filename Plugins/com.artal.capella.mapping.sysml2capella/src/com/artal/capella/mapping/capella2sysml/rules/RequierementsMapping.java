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
import org.eclipse.emf.diffmerge.impl.scopes.FragmentedModelScope;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaModule;
import org.polarsys.kitalpha.emde.model.ElementExtension;
import org.polarsys.kitalpha.vp.requirements.Requirements.Requirement;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile.UMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class RequierementsMapping extends AbstractMapping {

	private Project _source;
	private IMappingExecution _mappingExecution;

	public RequierementsMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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
		org.eclipse.uml2.uml.Package requierementsPkg = (Package) MappingRulesManager
				.getCapellaObjectFromAllRules(_source + "TEXTUALREQUIREMENT");

		CapellaModule capellaModule = getCapellaModule();

		EList<Requirement> ownedRequirements = capellaModule.getOwnedRequirements();
		for (Requirement requirement : ownedRequirements) {
			transformRequirements(requirement, requierementsPkg);
		}
	}

	private void transformRequirements(Requirement requirement, Package requierementsPkg) {
		Class umlRequirement = requierementsPkg.createOwnedClass(requirement.getReqIFLongName(), false);
		ResourceSet rset = null;
		Object targetDataSet = _mappingExecution.getTargetDataSet();
		if (targetDataSet instanceof FragmentedModelScope) {
			rset = ((FragmentedModelScope) targetDataSet).getResources().get(0).getResourceSet();
		}
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Stereotype ownedStereotype = profile.getNestedPackage("Requirements").getOwnedStereotype("Requirement");
		umlRequirement.applyStereotype(ownedStereotype);

		umlRequirement.setValue(ownedStereotype, "Text", requirement.getReqIFText());
		umlRequirement.setValue(ownedStereotype, "Id", requirement.getReqIFIdentifier());

		Sysml2CapellaUtils.trace(this, _source.eResource(), requirement, umlRequirement, "REQUIREMENT_");

	}

	/**
	 * @return
	 */
	private CapellaModule getCapellaModule() {
		LogicalArchitecture logicalArchitecture = Sysml2CapellaUtils.getLogicalArchitecture(_source);
		EList<ElementExtension> ownedExtensions = logicalArchitecture.getOwnedExtensions();
		CapellaModule capellaModule = null;
		for (ElementExtension elementExtension : ownedExtensions) {
			if (elementExtension instanceof CapellaModule) {
				capellaModule = (CapellaModule) elementExtension;
				break;
			}
		}
		return capellaModule;
	}

}
