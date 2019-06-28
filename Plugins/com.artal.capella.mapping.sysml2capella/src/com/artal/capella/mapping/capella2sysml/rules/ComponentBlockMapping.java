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

	private Project _source;
	private IMappingExecution _mappingExecution;

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

		IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Stereotype ownedStereotype = profile.getNestedPackage("Blocks").getOwnedStereotype("Block");

		LogicalComponent logicalContext = Sysml2CapellaUtils.getLogicalSystemRoot(_source);
		Package structurePkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "STRUCTURE_PKG");
		Package partsPkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "PARTS");

		Class product = structurePkg.createOwnedClass("Product", false);
		Sysml2CapellaUtils.trace(this, _source.eResource(), logicalContext, product, "PRODUCT_BLOCK");

		EObject applyStereotype = product.applyStereotype(ownedStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), logicalContext + "PRODUCT_BLOCK", applyStereotype,
				"PRODUCT_BLOCK_STEREO");

		// getAlgo().getTransientItems().add(applyStereotype);
		transformComponents(logicalContext, partsPkg, ownedStereotype);
	}

	public void transformComponents(LogicalComponent parent, Element pkgParent, Stereotype stereotype) {
		IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Stereotype traceStereotype = profile.getNestedPackage("Allocations").getOwnedStereotype("Allocate");

		Package behaviorPkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "BEHAVIOR_PKG");
		EList<LogicalComponent> ownedLogicalComponents = parent.getOwnedLogicalComponents();
		for (LogicalComponent logicalComponent : ownedLogicalComponents) {
			Class class1 = UMLFactory.eINSTANCE.createClass();
			class1.setName(logicalComponent.getName());

			if (pkgParent instanceof Package) {
				((Package) pkgParent).getPackagedElements().add(class1);
			}
			EObject applyStereotype = class1.applyStereotype(stereotype);
			Sysml2CapellaUtils.trace(this, _source.eResource(), logicalComponent + "_BLOCK_STEREO", applyStereotype,
					"_BLOCK_STEREO");
			Sysml2CapellaUtils.trace(this, _source.eResource(), logicalComponent, class1, "_BLOCK");
			// getAlgo().getTransientItems().add(applyStereotype);

			EList<ComponentFunctionalAllocation> ownedFunctionalAllocation = logicalComponent
					.getOwnedFunctionalAllocation();

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
				abstraction.applyStereotype(traceStereotype);
				Sysml2CapellaUtils.trace(this, _source.eResource(), componentFunctionalAllocation, abstraction,
						"ABSTRACTION_");
			}

			transformComponents(logicalComponent, pkgParent, stereotype);

		}
	}

	@Override
	public Capella2SysmlAlgo getAlgo() {
		return (Capella2SysmlAlgo) super.getAlgo();
	}

}
