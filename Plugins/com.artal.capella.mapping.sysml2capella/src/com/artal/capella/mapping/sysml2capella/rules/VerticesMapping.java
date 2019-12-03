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

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.FinalState;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Vertex;
import org.polarsys.capella.core.data.capellacommon.AbstractState;
import org.polarsys.capella.core.data.capellacommon.CapellacommonFactory;
import org.polarsys.capella.core.data.capellacommon.Mode;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * Transform the SysML/UML {@link Vertex} to Capella {@link AbstractState}
 * 
 * @author YBI
 *
 */
public class VerticesMapping extends AbstractMapping {
	/**
	 * The sysml root {@link Region}.
	 */
	Region _source;
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
	 *            the {@link Region} sysml Region.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public VerticesMapping(Sysml2CapellaAlgo algo, Region source, IMappingExecution mappingExecution) {
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

		EList<Vertex> subvertices = _source.getSubvertices();
		transformVertices(eResource, subvertices);

		_manager.executeRules();
	}

	/**
	 * Transform {@link Vertex} to {@link AbstractState}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param subvertices
	 *            the vertices list to transform
	 */
	private void transformVertices(Resource eResource, EList<Vertex> subvertices) {
		for (Vertex vertex : subvertices) {
			AbstractState capellaState = null;
			if (vertex instanceof State) {
				capellaState = transformState(eResource, (State) vertex);
			} else if (vertex instanceof Pseudostate) {
				PseudostateKind kind = ((Pseudostate) vertex).getKind();
				switch (kind) {
				case CHOICE_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createChoicePseudoState();
					break;
				case DEEP_HISTORY_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createDeepHistoryPseudoState();
					break;
				case ENTRY_POINT_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createEntryPointPseudoState();
					break;
				case EXIT_POINT_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createExitPointPseudoState();
					break;
				case FORK_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createForkPseudoState();
					break;
				case INITIAL_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createInitialPseudoState();
					break;
				case JOIN_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createJoinPseudoState();
					break;
				case SHALLOW_HISTORY_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createShallowHistoryPseudoState();
					break;
				case TERMINATE_LITERAL:
					capellaState = CapellacommonFactory.eINSTANCE.createTerminatePseudoState();
					break;
				case JUNCTION_LITERAL:
				default:

				}
			}

			capellaState.setName(vertex.getName());
			Sysml2CapellaUtils.trace(this, eResource, vertex, capellaState, "State_");

			org.polarsys.capella.core.data.capellacommon.Region region = (org.polarsys.capella.core.data.capellacommon.Region) MappingRulesManager
					.getCapellaObjectFromAllRules(_source);
			region.getOwnedStates().add(capellaState);

		}
	}

	/**
	 * Transform {@link State} to
	 * {@link org.polarsys.capella.core.data.capellacommon.FinalState} or
	 * {@link Mode}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param state
	 *            the {@link State} to transform
	 * @return the {@link AbstractState}
	 */
	private AbstractState transformState(Resource eResource, State state) {
		AbstractState capellaState;
		// state can be FinalState or State
		if (state instanceof FinalState) {
			// create a Capella FinalState
			capellaState = CapellacommonFactory.eINSTANCE.createFinalState();
		} else {
			// create a Capella Mode.
			capellaState = CapellacommonFactory.eINSTANCE.createMode();

			// set the capella doActivity from sysml doActivity
			Behavior doActivity = ((State) state).getDoActivity();
			manageDoActivity(capellaState, doActivity);

			Behavior entry = state.getEntry();
			manageEntry(capellaState, entry);

			Behavior exit = state.getExit();
			manageExit(capellaState, exit);

		}
		if (((State) state).getRegions() == null || ((State) state).getRegions().isEmpty()) {
			createDefaultRegion(eResource, state, capellaState);
		} else {
			RegionsMapping regionsMapping = new RegionsMapping(getAlgo(), state, _mappingExecution);
			_manager.add(regionsMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, state),
					regionsMapping);
		}
		return capellaState;
	}

	/**
	 * @param capellaState
	 * @param exit
	 */
	private void manageExit(AbstractState capellaState, Behavior exit) {
		if (exit != null) {
			List<CallBehaviorAction> callBehaviors = LogicalFunctionMapping.getMapActivityToCallBehaviors().get(exit);
			if (callBehaviors != null) {
				for (CallBehaviorAction callBehaviorAction : callBehaviors) {

					LogicalFunction lf = (LogicalFunction) MappingRulesManager
							.getCapellaObjectFromAllRules(callBehaviorAction);
					if (lf != null) {
						((Mode) capellaState).getExit().add(lf);
					}
				}
			}
		}
	}

	/**
	 * @param capellaState
	 * @param entry
	 */
	private void manageEntry(AbstractState capellaState, Behavior entry) {
		if (entry != null) {
			List<CallBehaviorAction> callBehaviors = LogicalFunctionMapping.getMapActivityToCallBehaviors().get(entry);
			if (callBehaviors != null) {
				for (CallBehaviorAction callBehaviorAction : callBehaviors) {

					LogicalFunction lf = (LogicalFunction) MappingRulesManager
							.getCapellaObjectFromAllRules(callBehaviorAction);
					if (lf != null) {
						((Mode) capellaState).getEntry().add(lf);
					}
				}
			}
		}
	}

	/**
	 * @param capellaState
	 * @param doActivity
	 */
	private void manageDoActivity(AbstractState capellaState, Behavior doActivity) {
		if (doActivity != null) {
			List<CallBehaviorAction> callBehaviors = LogicalFunctionMapping.getMapActivityToCallBehaviors()
					.get(doActivity);
			if (callBehaviors != null) {
				for (CallBehaviorAction callBehaviorAction : callBehaviors) {

					LogicalFunction lf = (LogicalFunction) MappingRulesManager
							.getCapellaObjectFromAllRules(callBehaviorAction);
					if (lf != null) {
						((Mode) capellaState).getDoActivity().add(lf);
					}
				}
			}
		}
	}

	/**
	 * Create the default
	 * {@link org.polarsys.capella.core.data.capellacommon.Region}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param state
	 *            the sysml state
	 * @param capellaState
	 *            the capella state
	 */
	private void createDefaultRegion(Resource eResource, State state, AbstractState capellaState) {
		org.polarsys.capella.core.data.capellacommon.Region defaultRegion = CapellacommonFactory.eINSTANCE
				.createRegion();
		((org.polarsys.capella.core.data.capellacommon.State) capellaState).getOwnedRegions().add(defaultRegion);
		Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, state) + "DEFAULT_REGIONS",
				defaultRegion, "DEFAULT_REGION");
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}
}
