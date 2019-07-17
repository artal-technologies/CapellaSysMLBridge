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
import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.common.data.modellingcore.TraceableElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile.UMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ComponentBlockMapping extends AbstractMapping {

	/**
	 * The Capella source element.
	 */
	private Project _source;
	/**
	 * The {@link IMappingExecution} allows to get the mapping data.
	 */
	private IMappingExecution _mappingExecution;

	Profile _sysMLProfile;

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Capella2SysmlAlgo} algo.
	 * @param source
	 *            the Capella {@link Project}
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public ComponentBlockMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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

		// get the sysml profile.
		Profile profile = getSysMLProfile();

		// get the Blocks::Blocks stereotype to apply on transformed class.
		Stereotype ownedStereotype = profile.getNestedPackage("Blocks").getOwnedStereotype("Block");

		// Get the capella root LogicalComponent.
		LogicalComponent logicalContext = Sysml2CapellaUtils.getLogicalSystemRoot(_source);
		// The package containing the structure
		Package structurePkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "STRUCTURE_PKG");
		// The package containg the parts.
		Package partsPkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "PARTS");

		// create the root UML Class and apply the Block stereotype.
		Class product = structurePkg.createOwnedClass("Product", false);
		Sysml2CapellaUtils.trace(this, _source.eResource(), logicalContext, product, "PRODUCT_BLOCK");
		EObject applyStereotype = product.applyStereotype(ownedStereotype);
		// Sysml2CapellaUtils.trace(this, _source.eResource(), logicalContext +
		// "PRODUCT_BLOCK", applyStereotype,
		// "PRODUCT_BLOCK_STEREO");
		getAlgo().getStereoApplications().add(applyStereotype);

		// getAlgo().getTransientItems().add(applyStereotype);
		transformComponents(logicalContext, partsPkg, ownedStereotype);
	}

	private Profile getSysMLProfile() {
		if (_sysMLProfile == null) {
			IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
			ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
			_sysMLProfile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		}
		return _sysMLProfile;
	}

	/**
	 * Transform Capella {@link LogicalComponent} to uml {@link Class} with
	 * Blocks::Block sysml stereotype.
	 * 
	 * @param parent
	 *            the LogicalComponent root containg all the
	 *            {@link LogicalComponent} to transform.
	 * @param pkgParent
	 *            the uml package to fill with the transformed {@link Class}.
	 * @param stereotype
	 *            the {@link Stereotype} to apply on the transformed
	 *            {@link Class}
	 */
	public void transformComponents(LogicalComponent parent, Element pkgParent, Stereotype stereotype) {
		Profile profile = getSysMLProfile();
		// get trace stereotype.
		Stereotype traceStereotype = profile.getNestedPackage("Allocations").getOwnedStereotype("Allocate");

		// behavior package allows to store the Abstraction representing the
		// Allocation LogicalFunction to LogicalComponent.
		Package behaviorPkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "BEHAVIOR_PKG");
		// for each sub LogicalComponent
		EList<LogicalComponent> ownedLogicalComponents = parent.getOwnedLogicalComponents();
		for (LogicalComponent logicalComponent : ownedLogicalComponents) {
			// create the class
			createClass(pkgParent, stereotype, logicalComponent);

			// get the allocation from a LogicalComponent.
			EList<ComponentFunctionalAllocation> ownedFunctionalAllocation = logicalComponent
					.getOwnedFunctionalAllocation();
			// transform this allocation to Abstraction
			transformAllocations(traceStereotype, behaviorPkg, ownedFunctionalAllocation);

			// transform sub components.
			transformComponents(logicalComponent, pkgParent, stereotype);

		}
	}

	/**
	 * Create the uml {@link Class}.
	 * 
	 * @param pkgParent
	 *            the {@link Element} parent where store the created class.
	 * @param stereotype
	 *            the {@link Stereotype} to apply on the created class.
	 * @param logicalComponent
	 *            the LogicalComponent to transform to class.
	 */
	private void createClass(Element pkgParent, Stereotype stereotype, LogicalComponent logicalComponent) {
		Class class1 = UMLFactory.eINSTANCE.createClass();
		class1.setName(logicalComponent.getName());

		if (pkgParent instanceof Package) {
			((Package) pkgParent).getPackagedElements().add(class1);
		}
		EObject applyStereotype = class1.applyStereotype(stereotype);
		// Sysml2CapellaUtils.trace(this, _source.eResource(), logicalComponent
		// + "_BLOCK_STEREO", applyStereotype,
		// "_BLOCK_STEREO");
		getAlgo().getStereoApplications().add(applyStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), logicalComponent, class1, "_BLOCK");
		// getAlgo().getTransientItems().add(applyStereotype);
	}

	/**
	 * Transform {@link ComponentFunctionalAllocation} to {@link Abstraction}.
	 * 
	 * @param traceStereotype
	 *            the stereotype to apply on created {@link Abstraction}.
	 * @param behaviorPkg
	 *            the package where store the created {@link Abstraction}
	 * @param ownedFunctionalAllocation
	 *            the {@link ComponentFunctionalAllocation} to transform.
	 */
	private void transformAllocations(Stereotype traceStereotype, Package behaviorPkg,
			EList<ComponentFunctionalAllocation> ownedFunctionalAllocation) {
		for (ComponentFunctionalAllocation componentFunctionalAllocation : ownedFunctionalAllocation) {
			TraceableElement sourceElement = componentFunctionalAllocation.getSourceElement();
			Class supplier = (Class) MappingRulesManager.getCapellaObjectFromAllRules(sourceElement);

			TraceableElement targetElement = componentFunctionalAllocation.getTargetElement();
			Activity client = (Activity) MappingRulesManager.getCapellaObjectFromAllRules(targetElement);
			if (client == null || supplier == null) {
				continue;
			}
			Abstraction abstraction = UMLFactory.eINSTANCE.createAbstraction();
			abstraction.getSuppliers().add(supplier);
			abstraction.getClients().add(client);
			behaviorPkg.getPackagedElements().add(abstraction);
			EObject applyStereotype = abstraction.applyStereotype(traceStereotype);
			getAlgo().getStereoApplications().add(applyStereotype);
			Sysml2CapellaUtils.trace(this, _source.eResource(), componentFunctionalAllocation, abstraction,
					"ABSTRACTION_");
		}
	}

	@Override
	public Capella2SysmlAlgo getAlgo() {
		return (Capella2SysmlAlgo) super.getAlgo();
	}

}
