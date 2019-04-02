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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaIncomingRelation;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaModule;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaOutgoingRelation;
import org.polarsys.capella.vp.requirements.CapellaRequirements.CapellaRequirementsFactory;
import org.polarsys.kitalpha.vp.requirements.Requirements.Requirement;
import org.polarsys.kitalpha.vp.requirements.Requirements.RequirementsFactory;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * 
 * 
 * The sysml requirement from a Cameo project are transposed as requirement
 * Capella. WARNING: This class can be used only if requirement vp is activated.
 * 
 * @author YBI
 *
 */
public class RequirementsMapping extends AbstractMapping {

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
	public RequirementsMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalArchitecture logicalArchitecture = Sysml2CapellaUtils.getLogicalArchitecture(targetScope.getProject());

		List<Class> requirements = getSysMLRequirements(_source);

		CapellaModule capellaModule = CapellaRequirementsFactory.eINSTANCE.createCapellaModule();
		logicalArchitecture.getOwnedExtensions().add(capellaModule);
		Sysml2CapellaUtils.trace(this, eResource, "CAPELLA_MODULE", capellaModule, "");

		// sort the sysml constraint to be sure the order doesn't change at the
		// coevolution.
		Collections.sort(requirements, new Comparator<org.eclipse.uml2.uml.Class>() {
			@Override
			public int compare(org.eclipse.uml2.uml.Class o1, org.eclipse.uml2.uml.Class o2) {
				String sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, o1);
				String sysMLID2 = Sysml2CapellaUtils.getSysMLID(eResource, o2);
				return sysMLID.compareTo(sysMLID2);
			}
		});

		transformRequirements(eResource, requirements, capellaModule);

	}

	/**
	 * Transform the SysML requirements.
	 * 
	 * @param eResource
	 *            the SysML resource
	 * @param requirements
	 *            the SysML requirements
	 * @param capellaModule
	 *            the {@link CapellaModule} containing the capella
	 *            {@link Requirement}
	 */
	private void transformRequirements(Resource eResource, List<Class> requirements, CapellaModule capellaModule) {
		for (Class requirement : requirements) {
			Stereotype appliedStereotype = ((Class) requirement)
					.getAppliedStereotype("SysML::Requirements::Requirement");

			Requirement req = RequirementsFactory.eINSTANCE.createRequirement();
			req.setReqIFLongName(requirement.getName());
			capellaModule.getOwnedRequirements().add(req);
			Sysml2CapellaUtils.trace(this, eResource, requirement, req, "REQ_");

			// manage the satisfy element
			manageSatisfyElement(eResource, requirement, appliedStereotype, req);

			// manage the refine element.
			manageRefineElement(eResource, requirement, appliedStereotype, req);

			// set text
			Object text = requirement.getValue(appliedStereotype, "Text");
			if (text != null) {
				req.setReqIFText(text.toString());
			}
		}
	}

	/**
	 * Manage the refine element.
	 * 
	 * @param eResource
	 *            the SysML resource
	 * @param requirement
	 *            the sysml {@link Class}
	 * @param appliedStereotype
	 *            the sysml {@link Stereotype}
	 *            "SysML::Requirements::Requirement"
	 * @param req
	 *            the capella {@link Requirement}
	 */
	@SuppressWarnings("unchecked")
	private void manageRefineElement(Resource eResource, Class requirement, Stereotype appliedStereotype,
			Requirement req) {
		List<EObject> value2 = (List<EObject>) requirement.getValue(appliedStereotype, "RefinedBy");
		if (value2 != null) {
			for (EObject eObject : value2) {
				CapellaElement refineCapella = (CapellaElement) MappingRulesManager
						.getCapellaObjectFromAllRules(eObject);
				if (refineCapella != null) {
					CapellaOutgoingRelation outgoingRelation = CapellaRequirementsFactory.eINSTANCE
							.createCapellaOutgoingRelation();
					outgoingRelation.setSource(refineCapella);
					outgoingRelation.setTarget(req);
					refineCapella.getOwnedExtensions().add(outgoingRelation);
					Sysml2CapellaUtils
							.trace(this, eResource,
									Sysml2CapellaUtils.getSysMLID(eResource, appliedStereotype)
											+ Sysml2CapellaUtils.getSysMLID(eResource, eObject),
									outgoingRelation, "REFINE_");
				}
			}
		}
	}

	/**
	 * Manage the satisfy element.
	 * 
	 * @param eResource
	 *            the SysML resource
	 * @param requirement
	 *            the sysml {@link Class}
	 * @param appliedStereotype
	 *            the sysml {@link Stereotype}
	 *            "SysML::Requirements::Requirement"
	 * @param req
	 *            the capella {@link Requirement}
	 */
	@SuppressWarnings("unchecked")
	private void manageSatisfyElement(Resource eResource, Class requirement, Stereotype appliedStereotype,
			Requirement req) {
		List<EObject> value = (List<EObject>) requirement.getValue(appliedStereotype, "SatisfiedBy");
		if (value != null) {
			for (EObject eObject : value) {

				CapellaElement satisfyCapella = (CapellaElement) MappingRulesManager
						.getCapellaObjectFromAllRules(eObject);
				if (satisfyCapella != null) {
					CapellaIncomingRelation incomingRelation = CapellaRequirementsFactory.eINSTANCE
							.createCapellaIncomingRelation();
					incomingRelation.setSource(req);
					incomingRelation.setTarget(satisfyCapella);
					req.getOwnedRelations().add(incomingRelation);
					Sysml2CapellaUtils
							.trace(this, eResource,
									Sysml2CapellaUtils.getSysMLID(eResource, appliedStereotype)
											+ Sysml2CapellaUtils.getSysMLID(eResource, eObject),
									incomingRelation, "SATISFY_");
				}
			}
		}
	}

	/**
	 * Get all the Sysml {@link Class} requirement. The Class with
	 * "SysML::Requirements::Requirement" stereotype
	 * 
	 * @param source
	 *            the source Package.
	 * @return {@link List}	
	 */
	private List<Class> getSysMLRequirements(Package source) {
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		List<Class> requirements = new ArrayList<Class>();
		for (PackageableElement eObject : packagedElements) {
			if (eObject instanceof Package) {
				Stereotype appliedStereotype = ((Package) eObject)
						.getAppliedStereotype("MagicDraw Profile::auxiliaryResource");
				if (appliedStereotype == null) {
					Set<EObject> all = EObjectExt.getAll(eObject, UMLPackage.Literals.CLASS);
					for (EObject eObject2 : all) {
						if (eObject2 instanceof org.eclipse.uml2.uml.Class) {
							Stereotype appliedStereotype2 = ((Class) eObject2)
									.getAppliedStereotype("SysML::Requirements::Requirement");
							if (appliedStereotype2 != null) {
								requirements.add((Class) eObject2);
							}
						}
					}
				}
			}
		}
		return requirements;
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
