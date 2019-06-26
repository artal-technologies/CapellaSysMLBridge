/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml.actions;

import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Namespace;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.common.data.modellingcore.ModelElement;
import org.polarsys.capella.common.data.modellingcore.ValueSpecification;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.CapellacorePackage;
import org.polarsys.capella.core.data.capellacore.Constraint;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.information.datavalue.OpaqueExpression;
import org.polarsys.capella.core.data.la.LogicalArchitecture;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ConstraintsMapping extends AbstractMapping {

	Project _source;
	IMappingExecution _mappingExecution;

	public ConstraintsMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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
		LogicalArchitecture logicalArchitecture = Sysml2CapellaUtils.getLogicalArchitecture(_source);
		Set<EObject> all = EObjectExt.getAll(logicalArchitecture, CapellacorePackage.Literals.CONSTRAINT);

		for (EObject eObject : all) {
			if (eObject instanceof Constraint) {
				transformConstraint((Constraint) eObject);
			}
		}

	}

	private void transformConstraint(Constraint capellaConstraint) {
		EObject eContainer = capellaConstraint.eContainer();
		Object umlParent = MappingRulesManager.getCapellaObjectFromAllRules(eContainer);
		org.eclipse.uml2.uml.Constraint umlConstraint = UMLFactory.eINSTANCE.createConstraint();
		umlConstraint.setName(capellaConstraint.getName());
		Sysml2CapellaUtils.trace(this, _source.eResource(), capellaConstraint, umlConstraint, "CONSTRAINT_");

		EList<ModelElement> constrainedElements = capellaConstraint.getConstrainedElements();
		for (ModelElement modelElement : constrainedElements) {
			Object umlConstrainedElement = MappingRulesManager.getCapellaObjectFromAllRules(modelElement);
			if (umlConstrainedElement instanceof Element) {
				umlConstraint.getConstrainedElements().add((Element) umlConstrainedElement);
			}
		}

		ValueSpecification ownedSpecification = capellaConstraint.getOwnedSpecification();
		if (ownedSpecification instanceof OpaqueExpression) {
			org.eclipse.uml2.uml.OpaqueExpression vs = UMLFactory.eINSTANCE.createOpaqueExpression();
			vs.setName(ownedSpecification.getName());

			EList<String> bodies = ((OpaqueExpression) ownedSpecification).getBodies();
			for (String string : bodies) {
				vs.getBodies().add(string);
			}
			EList<String> languages = ((OpaqueExpression) ownedSpecification).getLanguages();
			for (String string2 : languages) {
				vs.getLanguages().add(string2);
			}
			umlConstraint.setSpecification(vs);
			Sysml2CapellaUtils.trace(this, _source.eResource(), ownedSpecification, vs, "VALUE_SPEC_");
		}

		if (umlParent == null) {
			umlParent = (org.eclipse.uml2.uml.Package) MappingRulesManager
					.getCapellaObjectFromAllRules(_source + "TEXTUALREQUIREMENT");
		}
		if (umlParent instanceof Namespace) {
			((Namespace) umlParent).getOwnedRules().add(umlConstraint);
		}

	}

}
