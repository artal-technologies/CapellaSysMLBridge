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
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacommon.CapellacommonFactory;
import org.polarsys.capella.core.data.capellacommon.State;
import org.polarsys.capella.core.data.cs.Block;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * Transform SysML/UML {@link StateMachine} to
 * {@link org.polarsys.capella.core.data.capellacommon.StateMachine}
 * 
 * @author YBI
 *
 */
public class StateMachineMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
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
	public StateMachineMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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

		EList<PackageableElement> packagedElements = _source.getPackagedElements();
		List<StateMachine> stateMachines = new ArrayList<StateMachine>();
		for (PackageableElement eObject : packagedElements) {
			if (eObject instanceof Package) {
				Stereotype appliedStereotype = ((Package) eObject)
						.getAppliedStereotype("MagicDraw Profile::auxiliaryResource");
				if (appliedStereotype == null) {
					Set<EObject> all = EObjectExt.getAll(eObject, UMLPackage.Literals.STATE_MACHINE);
					for (EObject eObject2 : all) {
						if (eObject2 instanceof StateMachine) {
							stateMachines.add((StateMachine) eObject2);
						}
					}
				}
			}
		}
		// transform stateMachines
		transformStateMachines(eResource, stateMachines);
		_manager.executeRules();

	}

	/**
	 * Transform {@link StateMachine} to
	 * {@link org.polarsys.capella.core.data.capellacommon.StateMachine}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param stateMachines
	 *            the {@link StateMachine} to transform
	 */
	private void transformStateMachines(Resource eResource, List<StateMachine> stateMachines) {
		for (StateMachine sysmlStateMachine : stateMachines) {
			// create capella statemachine
			org.polarsys.capella.core.data.capellacommon.StateMachine capellaStateMachine = CapellacommonFactory.eINSTANCE
					.createStateMachine();
			capellaStateMachine.setName(sysmlStateMachine.getName());
			Sysml2CapellaUtils.trace(this, eResource, sysmlStateMachine, capellaStateMachine, "StateMachine_");

			// get parent statemachine
			EObject eContainer = sysmlStateMachine.eContainer();
			Object capellaObjectFromAllRules = MappingRulesManager.getCapellaObjectFromAllRules(eContainer);
			if (capellaObjectFromAllRules != null && capellaObjectFromAllRules instanceof Block) {
				((Block) capellaObjectFromAllRules).getOwnedStateMachines().add(capellaStateMachine);
			} else {
				CapellaUpdateScope targetDataSet = _mappingExecution.getTargetDataSet();
				LogicalComponent logicalSystemRoot = Sysml2CapellaUtils
						.getLogicalSystemRoot(targetDataSet.getProject());
				logicalSystemRoot.getOwnedStateMachines().add(capellaStateMachine);
			}

			// transform regions under statemachine
			RegionsMapping regionsMapping = new RegionsMapping(getAlgo(), sysmlStateMachine, _mappingExecution);
			_manager.add(
					regionsMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, sysmlStateMachine),
					regionsMapping);
			// transform transitions under statemachine
			TransitionsMapping transitionsMapping = new TransitionsMapping(getAlgo(), sysmlStateMachine,
					_mappingExecution);
			_manager.add(transitionsMapping.getClass().getName()
					+ Sysml2CapellaUtils.getSysMLID(eResource, sysmlStateMachine), transitionsMapping);
		}
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
