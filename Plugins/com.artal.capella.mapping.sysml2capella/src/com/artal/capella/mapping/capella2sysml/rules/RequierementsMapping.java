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
import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
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
	private ResourceSet _resourceSet;
	private Profile _reqProfile;

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

		Stereotype ownedStereotype = getRequirementNestedPkg().getOwnedStereotype("Requirement");
		umlRequirement.applyStereotype(ownedStereotype);

		umlRequirement.setValue(ownedStereotype, "Text", requirement.getReqIFText());
		umlRequirement.setValue(ownedStereotype, "Id", requirement.getReqIFIdentifier());

		Sysml2CapellaUtils.trace(this, _source.eResource(), requirement, umlRequirement, "REQUIREMENT_");

		transformSatisfyElement(requirement, umlRequirement, ownedStereotype);
		transformRefineElement(requirement, umlRequirement, ownedStereotype);

	}

	/**
	 * @param profile
	 * @return
	 */
	private Package getRequirementNestedPkg() {
		if (_reqProfile == null) {
			_reqProfile = SysML2CapellaUMLProfile.getProfile(getResourceSet(), UMLProfile.SYSML_PROFILE);
		}
		return _reqProfile.getNestedPackage("Requirements");
	}

	/**
	 * @param rset
	 * @return
	 */
	private ResourceSet getResourceSet() {
		if (_resourceSet == null) {
			IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
			_resourceSet = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		}
		return _resourceSet;
	}

	/**
	 * Tarnsform Refine element.
	 * 
	 * @param requirement
	 *            the {@link Requirement} to transform
	 * @param umlRequirement
	 *            the transformed uml Class. (with Requirement stereotype)
	 * @param ownedStereotype
	 *            the refined by stereotype.
	 */
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
					Package firstPackageParent = getFirstPackageParent((EObject) umlSource);
					Abstraction refineAbs = UMLFactory.eINSTANCE.createAbstraction();
					firstPackageParent.getPackagedElements().add(refineAbs);
					Stereotype refineStereoType = getRequirementNestedPkg().getOwnedStereotype("Refine");
					refineAbs.applyStereotype(refineStereoType);
					refineAbs.getSuppliers().add(umlRequirement);
					refineAbs.getClients().add((NamedElement) umlSource);
					// refineAbs.setValue(refineStereoType,
					// "base_DirectedRelationship", refineAbs);
					Sysml2CapellaUtils.trace(this, _source.eResource(), eObject, refineAbs, "REFINE_");

				}
			}

		}

	}

	/**
	 * Tarnsform Satisfy element.
	 * 
	 * @param requirement
	 *            the {@link Requirement} to transform
	 * @param umlRequirement
	 *            the transformed uml Class. (with Requirement stereotype)
	 * @param ownedStereotype
	 *            the satisfy stereotype.
	 */
	private void transformSatisfyElement(Requirement requirement, Class umlRequirement, Stereotype ownedStereotype) {
		EList<AbstractRelation> ownedRelations = requirement.getOwnedRelations();
		for (AbstractRelation abstractRelation : ownedRelations) {
			if (abstractRelation instanceof CapellaIncomingRelation) {
				CapellaElement target = ((CapellaIncomingRelation) abstractRelation).getTarget();

				Object umlTarget = MappingRulesManager.getCapellaObjectFromAllRules(target);
				List<EObject> value = (List<EObject>) umlRequirement.getValue(ownedStereotype, "SatisfiedBy");
				if (value == null) {
					value = new ArrayList<EObject>();
					umlRequirement.setValue(ownedStereotype, "SatisfiedBy", value);
				}
				value.add((EObject) umlTarget);
				Package firstPackageParent = getFirstPackageParent((EObject) umlTarget);
				Abstraction satisfyAbs = UMLFactory.eINSTANCE.createAbstraction();
				firstPackageParent.getPackagedElements().add(satisfyAbs);
				Stereotype satisfyStereoType = getRequirementNestedPkg().getOwnedStereotype("Satisfy");
				satisfyAbs.applyStereotype(satisfyStereoType);
				satisfyAbs.getSuppliers().add(umlRequirement);
				satisfyAbs.getClients().add((NamedElement) umlTarget);
				// satisfyAbs.setValue(satisfyStereoType,
				// "base_DirectedRelationship", satisfyAbs);
				Sysml2CapellaUtils.trace(this, _source.eResource(), abstractRelation, satisfyAbs, "SATISFY_");

			}
		}
	}

	/**
	 * Get the first package parent.
	 * 
	 * @param
	 * @return
	 */
	private Package getFirstPackageParent(EObject element) {
		if (element instanceof Package) {
			return (Package) element;
		} else {
			EObject eContainer = element.eContainer();
			if (eContainer != null) {
				return getFirstPackageParent(eContainer);
			}
		}
		return null;

	}

	/**
	 * Get the {@link CapellaModule}
	 * 
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
