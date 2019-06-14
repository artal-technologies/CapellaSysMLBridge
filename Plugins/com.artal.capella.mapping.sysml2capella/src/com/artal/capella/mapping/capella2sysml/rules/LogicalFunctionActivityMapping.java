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

import org.eclipse.emf.common.util.EList;
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
		List<LogicalFunction> logicalFunctions = getLogicalFunctions();
		Object parent = MappingRulesManager.getCapellaObjectFromAllRules(_source);
		for (LogicalFunction lf : logicalFunctions) {
			transformLogicalFunction(lf, parent);
			LogicalFunctionCallBehaviorsMapping behaviorsMapping = new LogicalFunctionCallBehaviorsMapping(getAlgo(),
					lf, _mappingExecution);
			_manager.add(behaviorsMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(_source.eResource(), lf),
					behaviorsMapping);
		}
		_manager.executeRules();
	}

	private List<LogicalFunction> getLogicalFunctions() {
		List<LogicalFunction> results = new ArrayList<LogicalFunction>();
		if (_source instanceof State) {
			EList<AbstractEvent> doActivity = ((State) _source).getDoActivity();
			for (AbstractEvent abstractEvent : doActivity) {
				if (abstractEvent instanceof LogicalFunction) {
					results.add((LogicalFunction) abstractEvent);
				}
			}
		} else if (_source instanceof Project) {
			LogicalFunction rootLogicalFunction = Sysml2CapellaUtils.getLogicalFunctionRoot(_source);
			List<AbstractFunction> ownedFunctions = rootLogicalFunction.getOwnedFunctions();
			for (AbstractFunction abstractFunction : ownedFunctions) {
				if (abstractFunction instanceof LogicalFunction) {
					results.add((LogicalFunction) abstractFunction);
				}
			}

		}

		return results;
	}

	private void transformLogicalFunction(LogicalFunction lf, Object parent) {
		if (MappingRulesManager.getCapellaObjectFromAllRules(lf) == null) {
			if (parent == null && _source instanceof Project) {
				Package behaviorPkg = (Package) MappingRulesManager
						.getCapellaObjectFromAllRules(_source + "BEHAVIOR_PKG");
				behaviorPkg.getPackagedElements().add(createActivity(lf));
			} else if (parent instanceof org.eclipse.uml2.uml.State) {
				((org.eclipse.uml2.uml.State) parent).setDoActivity(createActivity(lf));
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
