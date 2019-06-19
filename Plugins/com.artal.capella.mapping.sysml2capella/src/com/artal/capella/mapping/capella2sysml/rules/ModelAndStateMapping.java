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

import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.UMLPackage;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacommon.AbstractState;
import org.polarsys.capella.core.data.capellacommon.CapellacommonFactory;
import org.polarsys.capella.core.data.capellacommon.CapellacommonPackage;
import org.polarsys.capella.core.data.capellacommon.StateMachine;
import org.polarsys.capella.core.data.capellacommon.StateTransition;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ModelAndStateMapping extends AbstractMapping {

	private Project _source;
	private IMappingExecution _mappingExecution;
	private MappingRulesManager _manager = new MappingRulesManager();

	public ModelAndStateMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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
		LogicalComponent logicalSystemRoot = Sysml2CapellaUtils.getLogicalSystemRoot(_source);
		Set<EObject> stateMachines = EObjectExt.getAll(logicalSystemRoot, CapellacommonPackage.Literals.STATE_MACHINE);

		Package useCasesPkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "USECASES");

		for (EObject eObject : stateMachines) {
			StateMachine stateMachine = (StateMachine) eObject;
			transformStateMachine(stateMachine, useCasesPkg);
			RegionsMapping regionsMapping = new RegionsMapping(getAlgo(), stateMachine, _mappingExecution);
			_manager.add(regionsMapping.getClass().getName()
					+ Sysml2CapellaUtils.getSysMLID(_source.eResource(), stateMachine), regionsMapping);
			TransitionsMapping transitionsMapping = new TransitionsMapping(getAlgo(), stateMachine, _mappingExecution);
			_manager.add(transitionsMapping.getClass().getName()
					+ Sysml2CapellaUtils.getSysMLID(_source.eResource(), stateMachine), transitionsMapping);
		}

		_manager.executeRules();

	}

	private void transformStateMachine(StateMachine stateMachine, Package useCasesPkg) {
		org.eclipse.uml2.uml.StateMachine umlSM = UMLFactory.eINSTANCE.createStateMachine();
		umlSM.setName(stateMachine.getName());
		useCasesPkg.getPackagedElements().add(umlSM);
		Sysml2CapellaUtils.trace(this, _source.eResource(), stateMachine, umlSM, "STATEMACHINE_");
	}

}
