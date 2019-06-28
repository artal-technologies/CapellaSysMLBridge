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
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.common.data.behavior.AbstractEvent;
import org.polarsys.capella.core.data.capellacommon.State;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class LogicalFunctionActivityMapping extends AbstractMapping {

	private CapellaElement _source;
	private IMappingExecution _mappingExecution;
	private MappingRulesManager _manager = new MappingRulesManager();

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
		List<LogicalFunction> doActivities = new ArrayList<>();
		List<LogicalFunction> doEntries = new ArrayList<>();
		List<LogicalFunction> doExits = new ArrayList<>();
		fillLogicalFunctions(doActivities, doEntries, doExits);
		Object parent = MappingRulesManager.getCapellaObjectFromAllRules(_source);
		transformLogicalFunctions(doActivities, parent, TypeActivity.DO_ACTIVITY);
		transformLogicalFunctions(doEntries, parent, TypeActivity.ENTRY);
		transformLogicalFunctions(doExits, parent, TypeActivity.EXIT);
		_manager.executeRules();
	}

	private enum TypeActivity {
		DO_ACTIVITY, ENTRY, EXIT;
	}

	/**
	 * @param activities
	 * @param parent
	 */
	private void transformLogicalFunctions(List<LogicalFunction> activities, Object parent, TypeActivity ta) {
		for (LogicalFunction lf : activities) {
			transformLogicalFunction(lf, parent, ta);
			LogicalFunctionCallBehaviorsMapping behaviorsMapping = new LogicalFunctionCallBehaviorsMapping(getAlgo(),
					lf, _mappingExecution);
			_manager.add(behaviorsMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(_source.eResource(), lf),
					behaviorsMapping);
		}
	}

	private void fillActivitiesList(List<LogicalFunction> listToFill, List<AbstractEvent> toRead) {
		for (AbstractEvent abstractEvent : toRead) {
			if (abstractEvent instanceof LogicalFunction) {
				listToFill.add((LogicalFunction) abstractEvent);
			}
		}
	}

	private void fillLogicalFunctions(List<LogicalFunction> doactivities, List<LogicalFunction> doentries,
			List<LogicalFunction> doexits) {
		if (_source instanceof State) {
			fillActivitiesList(doactivities, ((State) _source).getDoActivity());
			fillActivitiesList(doentries, ((State) _source).getEntry());
			fillActivitiesList(doexits, ((State) _source).getExit());
		} else if (_source instanceof Project) {
			LogicalFunction rootLogicalFunction = Sysml2CapellaUtils.getLogicalFunctionRoot(_source);
			List<AbstractFunction> ownedFunctions = rootLogicalFunction.getOwnedFunctions();
			for (AbstractFunction abstractFunction : ownedFunctions) {
				if (abstractFunction instanceof LogicalFunction) {
					doactivities.add((LogicalFunction) abstractFunction);
				}
			}

		}
	}

	private void transformLogicalFunction(LogicalFunction lf, Object parent, TypeActivity ta) {
		if (MappingRulesManager.getCapellaObjectFromAllRules(lf) == null) {
			if (parent == null && _source instanceof Project) {
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
	 * @param lf
	 * @return
	 */
	private Activity createActivity(LogicalFunction lf) {
		Activity activity = UMLFactory.eINSTANCE.createActivity();
		activity.setName(lf.getName());
		Sysml2CapellaUtils.trace(this, _source.eResource(), lf, activity, "ACTIVITY_");
		return activity;
	}
}
