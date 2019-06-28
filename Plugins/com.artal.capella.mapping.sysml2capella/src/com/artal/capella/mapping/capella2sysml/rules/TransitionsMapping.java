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

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.Vertex;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacommon.AbstractState;
import org.polarsys.capella.core.data.capellacommon.CapellacommonPackage;
import org.polarsys.capella.core.data.capellacommon.StateMachine;
import org.polarsys.capella.core.data.capellacommon.StateTransition;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class TransitionsMapping extends AbstractMapping {

	/**
	 * The {@link StateMachine} source.
	 */
	StateMachine _source;

	/**
	 * The {@link IMappingExecution} allows to get mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * Constructor
	 * 
	 * @param algo
	 *            the {@link Capella2SysmlAlgo} algo.
	 * @param source
	 *            the {@link StateMachine} source.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public TransitionsMapping(CapellaBridgeAlgo<?> algo, StateMachine source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
		_mappingExecution = mappingExecution;
	}

	@Override
	public void computeMapping() {
		Set<EObject> all = EObjectExt.getAll(_source, CapellacommonPackage.Literals.STATE_TRANSITION);
		for (EObject eObject : all) {
			if (eObject instanceof StateTransition) {
				StateTransition stateTransition = (StateTransition) eObject;
				EObject eContainer = stateTransition.eContainer();
				Object umlContainer = MappingRulesManager.getCapellaObjectFromAllRules(eContainer);
				if (umlContainer instanceof Region) {
					Transition transition = ((Region) umlContainer).createTransition(stateTransition.getName());
					Sysml2CapellaUtils.trace(this, _source.eResource(), stateTransition, transition, "TRANSITION_");
					AbstractState source = stateTransition.getSource();
					Vertex vertexSource = (Vertex) MappingRulesManager.getCapellaObjectFromAllRules(source);
					AbstractState target = stateTransition.getTarget();
					Vertex vertexTarget = (Vertex) MappingRulesManager.getCapellaObjectFromAllRules(target);

					transition.setSource(vertexSource);
					transition.setTarget(vertexTarget);
				}

			}
		}

	}

}
