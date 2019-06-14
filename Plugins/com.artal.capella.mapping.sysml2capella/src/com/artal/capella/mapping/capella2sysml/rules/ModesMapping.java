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
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.UMLFactory;
import org.eclipse.uml2.uml.Vertex;
import org.polarsys.capella.core.data.capellacommon.AbstractState;
import org.polarsys.capella.core.data.capellacommon.ChoicePseudoState;
import org.polarsys.capella.core.data.capellacommon.DeepHistoryPseudoState;
import org.polarsys.capella.core.data.capellacommon.EntryPointPseudoState;
import org.polarsys.capella.core.data.capellacommon.ExitPointPseudoState;
import org.polarsys.capella.core.data.capellacommon.FinalState;
import org.polarsys.capella.core.data.capellacommon.ForkPseudoState;
import org.polarsys.capella.core.data.capellacommon.InitialPseudoState;
import org.polarsys.capella.core.data.capellacommon.JoinPseudoState;
import org.polarsys.capella.core.data.capellacommon.Mode;
import org.polarsys.capella.core.data.capellacommon.Pseudostate;
import org.polarsys.capella.core.data.capellacommon.Region;
import org.polarsys.capella.core.data.capellacommon.ShallowHistoryPseudoState;
import org.polarsys.capella.core.data.capellacommon.State;
import org.polarsys.capella.core.data.capellacommon.TerminatePseudoState;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ModesMapping extends AbstractMapping {

	private Region _source;
	private IMappingExecution _mappingExecution;
	/**
	 * A {@link MappingRulesManager} allowing to manage the sub rules.
	 */
	MappingRulesManager _manager = new MappingRulesManager();

	public ModesMapping(CapellaBridgeAlgo<?> algo, Region source, IMappingExecution mappingExecution) {
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
		EList<AbstractState> ownedStates = _source.getOwnedStates();
		for (AbstractState abstractState : ownedStates) {
			transformAbstractState(abstractState);
		}
		_manager.executeRules();
	}

	private void transformAbstractState(AbstractState abstractState) {
		Vertex vertex = null;
		if (abstractState instanceof Pseudostate) {
			vertex = transformPseudostate((Pseudostate) abstractState);

		} else if (abstractState instanceof State) {
			vertex = transformState(abstractState);

			// Do activity
			LogicalFunctionActivityMapping functionActivityMapping = new LogicalFunctionActivityMapping(getAlgo(),
					(State) abstractState, _mappingExecution);
			_manager.add(
					functionActivityMapping.getClass().getName()
							+ Sysml2CapellaUtils.getSysMLID(_source.eResource(), abstractState),
					functionActivityMapping);

			RegionsMapping regionsMapping = new RegionsMapping(getAlgo(), abstractState, _mappingExecution);
			_manager.add(regionsMapping.getClass().getName()
					+ Sysml2CapellaUtils.getSysMLID(_source.eResource(), abstractState), regionsMapping);
		}
		org.eclipse.uml2.uml.Region umlRegion = (org.eclipse.uml2.uml.Region) MappingRulesManager
				.getCapellaObjectFromAllRules(_source);
		umlRegion.getSubvertices().add(vertex);
		Sysml2CapellaUtils.trace(this, _source.eResource(), abstractState, vertex, "VERTEX_");

	}

	/**
	 * @param abstractState
	 */
	private org.eclipse.uml2.uml.State transformState(AbstractState abstractState) {
		org.eclipse.uml2.uml.State state = null;
		if (abstractState instanceof FinalState) {
			state = UMLFactory.eINSTANCE.createFinalState();
		}
		if (abstractState instanceof Mode) {
			state = UMLFactory.eINSTANCE.createState();
		}
		state.setName(abstractState.getName());
		return state;
	}

	/**
	 * @param abstractState
	 */
	private org.eclipse.uml2.uml.Pseudostate transformPseudostate(Pseudostate abstractState) {
		org.eclipse.uml2.uml.Pseudostate umlPseudoState = UMLFactory.eINSTANCE.createPseudostate();
		umlPseudoState.setName(abstractState.getName());
		PseudostateKind kind = getPseudoKind(abstractState);
		umlPseudoState.setKind(kind);
		return umlPseudoState;
	}

	/**
	 * @param abstractState
	 * @return
	 */
	private PseudostateKind getPseudoKind(Pseudostate abstractState) {
		PseudostateKind kind = null;
		if (abstractState instanceof ChoicePseudoState) {
			kind = PseudostateKind.CHOICE_LITERAL;
		}
		if (abstractState instanceof DeepHistoryPseudoState) {
			kind = PseudostateKind.DEEP_HISTORY_LITERAL;
		}
		if (abstractState instanceof EntryPointPseudoState) {
			kind = PseudostateKind.ENTRY_POINT_LITERAL;
		}
		if (abstractState instanceof ExitPointPseudoState) {
			kind = PseudostateKind.EXIT_POINT_LITERAL;
		}
		if (abstractState instanceof ForkPseudoState) {
			kind = PseudostateKind.FORK_LITERAL;
		}
		if (abstractState instanceof InitialPseudoState) {
			kind = PseudostateKind.INITIAL_LITERAL;
		}
		if (abstractState instanceof JoinPseudoState) {
			kind = PseudostateKind.JOIN_LITERAL;
		}
		if (abstractState instanceof ShallowHistoryPseudoState) {
			kind = PseudostateKind.SHALLOW_HISTORY_LITERAL;
		}
		if (abstractState instanceof TerminatePseudoState) {
			kind = PseudostateKind.TERMINATE_LITERAL;
		}
		return kind;
	}

}
