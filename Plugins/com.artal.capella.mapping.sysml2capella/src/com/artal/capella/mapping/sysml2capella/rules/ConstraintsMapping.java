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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.ValueSpecification;
import org.polarsys.capella.common.data.modellingcore.AbstractConstraint;
import org.polarsys.capella.common.data.modellingcore.ModelElement;
import org.polarsys.capella.core.data.capellacore.CapellacoreFactory;
import org.polarsys.capella.core.data.capellacore.Constraint;
import org.polarsys.capella.core.data.information.datavalue.DatavalueFactory;
import org.polarsys.capella.core.data.la.LogicalActor;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * The sysml constraints contained in the uml/SysML {@link Model} from a Cameo
 * project are transposed as Capella Constraints. This class is abstract, in the
 * specific extended class the method getInput() shall be implemented.
 * 
 * @author YBI
 *
 */
abstract public class ConstraintsMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Element _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * A {@link MappingRulesManager} allowing to manage the sub rules.
	 */
	MappingRulesManager _manager = new MappingRulesManager();

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
	public ConstraintsMapping(Sysml2CapellaAlgo algo, Element source, IMappingExecution mappingExecution) {
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

		// get the constraints to transform
		List<org.eclipse.uml2.uml.Constraint> sysMLConstraints = getInput();

		// sort the sysml constraint to be sure the order doesn't change at the
		// coevolution.
		Collections.sort(sysMLConstraints, new Comparator<org.eclipse.uml2.uml.Constraint>() {
			@Override
			public int compare(org.eclipse.uml2.uml.Constraint o1, org.eclipse.uml2.uml.Constraint o2) {
				String sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, o1);
				String sysMLID2 = Sysml2CapellaUtils.getSysMLID(eResource, o2);
				return sysMLID.compareTo(sysMLID2);
			}
		});

		for (org.eclipse.uml2.uml.Constraint sysMLConstraint : sysMLConstraints) {
			// get the container capella element from the container sysml
			// element.
			Object capellaObject = getCapellaConstraintContainer(sysMLConstraint);

			// create the capella constraint
			Constraint constraintCapella = CapellacoreFactory.eINSTANCE.createConstraint();
			constraintCapella.setName(sysMLConstraint.getName());

			// get the transformed capella constrained elements and add them in
			// the new capella constraint.
			EList<Element> constrainedElements = sysMLConstraint.getConstrainedElements();
			fillConstrainedElements(constraintCapella, constrainedElements);
			// get the SysML OpaqueExpression and transform in Capella
			// OpaqueExpression.
			ValueSpecification specification = sysMLConstraint.getSpecification();
			if (specification instanceof OpaqueExpression) {
				transformValueSpecification(eResource, constraintCapella, specification);
			}
			// add the capella constraint to capella container.
			if (capellaObject instanceof ModelElement) {
				List<AbstractConstraint> ownedConstraints = ((ModelElement) capellaObject).getOwnedConstraints();
				ownedConstraints.add(constraintCapella);
				Sysml2CapellaUtils.trace(this, eResource, sysMLConstraint, constraintCapella, "Constraint_");
			}

		}
		_manager.executeRules();

	}

	/**
	 * Get the capella constraint container.
	 * 
	 * @param sysMLConstraint
	 *            the SysML constraint to transform
	 * @return Object parent.
	 */
	private Object getCapellaConstraintContainer(org.eclipse.uml2.uml.Constraint sysMLConstraint) {
		Object capellaObject = MappingRulesManager.getCapellaObjectFromAllRules(sysMLConstraint.eContainer());
		// if no transformed container, add the capella constraint in the
		// main dataPkg.
		if (capellaObject == null) {
			CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
			capellaObject = Sysml2CapellaUtils.getDataPkgRoot(targetScope.getProject());
		}
		return capellaObject;
	}

	/**
	 * Fill the constrained elements in the capella constraint.
	 * 
	 * @param constraintCapella
	 *            the constraint capella to fill
	 * @param constrainedElements
	 *            the SysML constrained element
	 */
	private void fillConstrainedElements(Constraint constraintCapella, EList<Element> constrainedElements) {
		for (Element element : constrainedElements) {
			Object capellaElement = MappingRulesManager.getCapellaObjectFromAllRules(element);
			// in some case a sysml element is transform in list.
			if (capellaElement instanceof List<?>) {
				for (Object o : new ArrayList<>(((List<?>) capellaElement))) {
					if (o instanceof LogicalActor || o instanceof LogicalFunction || o instanceof LogicalComponent) {
						capellaElement = o;
						break;
					}
				}
			}
			if (capellaElement != null) {
				constraintCapella.getConstrainedElements().add((ModelElement) capellaElement);
			}
		}
	}

	/**
	 * Transform the SysML {@link OpaqueExpression} to Capella
	 * {@link org.polarsys.capella.core.data.information.datavalue.OpaqueExpression}
	 * 
	 * @param eResource
	 *            the SysML resource
	 * @param constraintCapella
	 *            the capella {@link Constraint}
	 * @param specification
	 *            the ValueSpecification to transform
	 */
	private void transformValueSpecification(Resource eResource, Constraint constraintCapella,
			ValueSpecification specification) {
		org.polarsys.capella.core.data.information.datavalue.OpaqueExpression capellaExpr = DatavalueFactory.eINSTANCE
				.createOpaqueExpression();
		capellaExpr.setName(capellaExpr.getName());
		EList<String> bodies = ((OpaqueExpression) specification).getBodies();
		for (String string : bodies) {
			capellaExpr.getBodies().add(string);
		}
		EList<String> languages = ((OpaqueExpression) specification).getLanguages();
		for (String string : languages) {
			capellaExpr.getLanguages().add(string);
		}
		constraintCapella.setOwnedSpecification(capellaExpr);
		Sysml2CapellaUtils.trace(this, eResource, specification, capellaExpr, "ValueSpec_");
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

	/**
	 * Get the SysML Constraints to transform.
	 * 
	 * @return {@link List} {@link org.eclipse.uml2.uml.Constraint}
	 */
	public abstract List<org.eclipse.uml2.uml.Constraint> getInput();

}
