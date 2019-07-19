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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.common.data.behavior.AbstractEvent;
import org.polarsys.capella.core.data.capellacommon.State;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class LogicalFunctionActivityMapping extends AbstractMapping {

	/**
	 * The {@link CapellaElement} source.
	 */
	private CapellaElement _source;
	/**
	 * The {@link IMappingExecution} allows to get the mapping data.
	 */
	private IMappingExecution _mappingExecution;

	/**
	 * {@link MappingRulesManager} allows to manage sub rules.
	 */
	private MappingRulesManager _manager = new MappingRulesManager();

	public static List<LogicalFunction> _lfs = new ArrayList<>();

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Capella2SysmlAlgo} algo.
	 * @param source
	 *            the {@link CapellaElement} source.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public LogicalFunctionActivityMapping(CapellaBridgeAlgo<?> algo, CapellaElement source,
			IMappingExecution mappingExecution) {
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
		// the ACtivities can be store as do activities, do entries or do exits
		// of an uml State
		List<LogicalFunction> doActivities = new ArrayList<>();
		List<LogicalFunction> doEntries = new ArrayList<>();
		List<LogicalFunction> doExits = new ArrayList<>();
		// fill lists.
		fillLogicalFunctions(doActivities, doEntries, doExits);
		// transform LogicalFunctions.
		Object parent = MappingRulesManager.getCapellaObjectFromAllRules(_source);
		transformLogicalFunctions(doActivities, doEntries, doExits, parent);
		_manager.executeRules();
	}

	/**
	 * Transform {@link LogicalFunction} to {@link Activity}.
	 * 
	 * @param doActivities
	 *            the {@link LogicalFunction} from doActivities feature of
	 *            {@link State}
	 * @param doEntries
	 *            the {@link LogicalFunction} from doEntries feature of
	 *            {@link State}
	 * @param doExits
	 *            the {@link LogicalFunction} from doExits feature of
	 *            {@link State}
	 * @param parent
	 *            the parent. can be {@link Project} or {@link State}
	 */
	private void transformLogicalFunctions(List<LogicalFunction> doActivities, List<LogicalFunction> doEntries,
			List<LogicalFunction> doExits, Object parent) {
		transformLogicalFunctions(doActivities, parent, TypeActivity.DO_ACTIVITY);
		transformLogicalFunctions(doEntries, parent, TypeActivity.ENTRY);
		transformLogicalFunctions(doExits, parent, TypeActivity.EXIT);
	}

	private enum TypeActivity {
		DO_ACTIVITY, ENTRY, EXIT;
	}

	/**
	 * transform {@link LogicalFunction}.
	 * 
	 * @param activities
	 *            the {@link LogicalFunction} to transform
	 * @param parent
	 *            the {@link CapellaElement} parent.
	 * @param ta
	 *            the {@link TypeActivity}.
	 */
	private void transformLogicalFunctions(List<LogicalFunction> activities, Object parent, TypeActivity ta) {
		for (LogicalFunction lf : activities) {
			if (!_lfs.contains(lf)) {
				_lfs.add(lf);
				transformLogicalFunction(lf, parent, ta);
				LogicalFunctionCallBehaviorsMapping behaviorsMapping = new LogicalFunctionCallBehaviorsMapping(
						getAlgo(), lf, _mappingExecution);
				_manager.add(
						behaviorsMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(_source.eResource(), lf),
						behaviorsMapping);
			}
		}
	}

	/**
	 * Fille the {@link List}
	 * 
	 * @param listToFill
	 *            the list to fill
	 * @param toRead
	 *            the list to read.
	 */
	private void fillActivitiesList(List<LogicalFunction> listToFill, List<AbstractEvent> toRead) {
		for (AbstractEvent abstractEvent : toRead) {
			if (abstractEvent instanceof LogicalFunction) {
				listToFill.add((LogicalFunction) abstractEvent);
			}
		}
	}

	/**
	 * Fill list with respect for the {@link State} feature doActivities,
	 * doEntry and doExit.
	 * 
	 * @param doactivities
	 *            the list {@link LogicalFunction} from doActivities
	 * @param doentries
	 *            the list {@link LogicalFunction} from doentries
	 * @param doexits
	 *            the list {@link LogicalFunction} from doexits
	 */
	private void fillLogicalFunctions(List<LogicalFunction> doactivities, List<LogicalFunction> doentries,
			List<LogicalFunction> doexits) {
		if (_source instanceof State) {
			fillActivitiesList(doactivities, ((State) _source).getDoActivity());
			fillActivitiesList(doentries, ((State) _source).getEntry());
			fillActivitiesList(doexits, ((State) _source).getExit());
		} else if (_source instanceof Project) {
			LogicalFunction rootLogicalFunction = Sysml2CapellaUtils.getLogicalFunctionRoot(_source);
			fillFunctions(doactivities, rootLogicalFunction);

		}
	}

	private void fillFunctions(List<LogicalFunction> doactivities, LogicalFunction rootLogicalFunction) {
		List<AbstractFunction> ownedFunctions = rootLogicalFunction.getOwnedFunctions();
		for (AbstractFunction abstractFunction : ownedFunctions) {
			if (abstractFunction instanceof LogicalFunction) {
				doactivities.add((LogicalFunction) abstractFunction);
				fillFunctions(doactivities, (LogicalFunction) abstractFunction);
			}
		}
	}

	/**
	 * Transform {@link LogicalFunction} to {@link Activity}.
	 * 
	 * @param lf
	 *            the {@link LogicalFunction} to transform
	 * @param parent
	 *            the capella parent
	 * @param ta
	 *            the feature.
	 */
	private void transformLogicalFunction(LogicalFunction lf, Object parent, TypeActivity ta) {
		if (MappingRulesManager.getCapellaObjectFromAllRules(lf) == null) {
			if ((parent == null || parent instanceof Model) && _source instanceof Project) {
				Package behaviorPkg = (Package) MappingRulesManager
						.getCapellaObjectFromAllRules(_source + "BEHAVIOR_PKG");
				behaviorPkg.getPackagedElements().add(createActivity(lf));
			} else if (parent instanceof org.eclipse.uml2.uml.State) {
				if (ta == TypeActivity.DO_ACTIVITY) {
					((org.eclipse.uml2.uml.State) parent).setDoActivity(createActivity(lf));
				}
				if (ta == TypeActivity.ENTRY) {
					((org.eclipse.uml2.uml.State) parent).setEntry(createActivity(lf));
				}
				if (ta == TypeActivity.EXIT) {
					((org.eclipse.uml2.uml.State) parent).setExit(createActivity(lf));
				}
			}
		}
	}

	/**
	 * Create new {@link Activity}
	 * 
	 * @param lf
	 *            the {@link LogicalFunction} to transform.
	 * @return the transformed {@link Activity}.
	 */
	private Activity createActivity(LogicalFunction lf) {
		Activity activity = UMLFactory.eINSTANCE.createActivity();
		activity.setName(lf.getName());
		Sysml2CapellaUtils.trace(this, _source.eResource(), lf, activity, "ACTIVITY_");
		return activity;
	}
}
