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
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.impl.scopes.FragmentedModelScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaIncomingRelation;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaModule;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaOutgoingRelation;
import org.polarsys.kitalpha.emde.model.ElementExtension;
import org.polarsys.kitalpha.vp.requirements.Requirements.AbstractRelation;
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

		transformSatisfyElement(requirement, umlRequirement, ownedStereotype);
		transformRefineElement(requirement, umlRequirement, ownedStereotype);

	}

	private void transformRefineElement(Requirement requirement, Class umlRequirement, Stereotype ownedStereotype) {

		Collection<Setting> referencingInverse = Sysml2CapellaUtils.getReferencingInverse(requirement);
		for (Setting setting : referencingInverse) {
			EObject eObject = setting.getEObject();
			if (eObject instanceof CapellaOutgoingRelation) {
				CapellaElement source = ((CapellaOutgoingRelation) eObject).getSource();
				Object umlSource = MappingRulesManager.getCapellaObjectFromAllRules(source);
				if (umlSource instanceof EObject) {
					List<EObject> value = (List<EObject>) umlRequirement.getValue(ownedStereotype, "RefinedBy");
					if (value == null) {
						value = new ArrayList<EObject>();
						umlRequirement.setValue(ownedStereotype, "RefinedBy", value);
					}
					value.add((EObject) umlSource);
				}
			}

		}

	}

	private void transformSatisfyElement(Requirement requirement, Class umlRequirement, Stereotype ownedStereotype) {
		EList<AbstractRelation> ownedRelations = requirement.getOwnedRelations();
		for (AbstractRelation abstractRelation : ownedRelations) {
			if (abstractRelation instanceof CapellaIncomingRelation) {
				Requirement source = ((CapellaIncomingRelation) abstractRelation).getSource();
				CapellaElement target = ((CapellaIncomingRelation) abstractRelation).getTarget();

				Object umlTarget = MappingRulesManager.getCapellaObjectFromAllRules(target);
				List<EObject> value = (List<EObject>) umlRequirement.getValue(ownedStereotype, "SatisfiedBy");
				if (value == null) {
					value = new ArrayList<EObject>();
					umlRequirement.setValue(ownedStereotype, "SatisfiedBy", value);
				}
				value.add((EObject) umlTarget);

			}
		}
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
