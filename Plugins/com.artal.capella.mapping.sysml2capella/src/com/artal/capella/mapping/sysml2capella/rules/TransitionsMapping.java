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

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Vertex;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacommon.AbstractState;
import org.polarsys.capella.core.data.capellacommon.CapellacommonFactory;
import org.polarsys.capella.core.data.capellacommon.StateTransition;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * Transform SysML/UML {@link Transition} to Capella {@link StateTransition}
 * 
 * @author YBI
 *
 */
public class TransitionsMapping extends AbstractMapping {
	/**
	 * The sysml root {@link StateMachine}.
	 */
	StateMachine _source;
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
	 *            the {@link StateMachine} sysml StateMachine.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public TransitionsMapping(Sysml2CapellaAlgo algo, StateMachine source, IMappingExecution mappingExecution) {
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
		Set<EObject> all = EObjectExt.getAll(_source, UMLPackage.Literals.TRANSITION);
		List<EObject> sortedList = new ArrayList<>(all);
		Collections.sort(sortedList, new Comparator<EObject>() {
			@Override
			public int compare(EObject o1, EObject o2) {
				String sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, o1);
				String sysMLID2 = Sysml2CapellaUtils.getSysMLID(eResource, o2);
				return sysMLID.compareTo(sysMLID2);
			}
		});
		transformTransitions(eResource, sortedList);

	}

	/**
	 * Transform {@link Transition} to {@link StateTransition}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param sortedList
	 *            the sorted {@link Transition} list to transform (sorted for
	 *            the coevolution)
	 */
	private void transformTransitions(Resource eResource, List<EObject> sortedList) {
		for (EObject eObject : sortedList) {
			if (eObject instanceof Transition) {
				Transition transition = (Transition) eObject;

				EObject eContainer = transition.eContainer();
				Object capellaObjectFromAllRules = MappingRulesManager.getCapellaObjectFromAllRules(eContainer);
				if (capellaObjectFromAllRules instanceof org.polarsys.capella.core.data.capellacommon.Region) {
					// create capella state transition
					StateTransition capellaTransition = CapellacommonFactory.eINSTANCE.createStateTransition();
					capellaTransition.setName(transition.getName());
					Sysml2CapellaUtils.trace(this, eResource, transition, capellaTransition, "TRANSITION_");

					// add in container
					((org.polarsys.capella.core.data.capellacommon.Region) capellaObjectFromAllRules)
							.getOwnedTransitions().add(capellaTransition);

					// get both source and target capella element
					Vertex source = transition.getSource();
					AbstractState capellaObjectFromAllRules2 = (AbstractState) MappingRulesManager
							.getCapellaObjectFromAllRules(source);

					Vertex target = transition.getTarget();
					AbstractState capellaObjectFromAllRules3 = (AbstractState) MappingRulesManager
							.getCapellaObjectFromAllRules(target);

					// get both source and target capella elements in the new
					// capella state transition
					capellaTransition.setSource(capellaObjectFromAllRules2);
					capellaTransition.setTarget(capellaObjectFromAllRules3);
				}
			}
		}
	}

}
