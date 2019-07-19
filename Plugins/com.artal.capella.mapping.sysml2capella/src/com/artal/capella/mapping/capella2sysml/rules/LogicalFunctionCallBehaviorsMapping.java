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
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.UMLFactory;
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
public class LogicalFunctionCallBehaviorsMapping extends AbstractMapping {

	/**
	 * The {@link CapellaElement} source
	 */
	private CapellaElement _source;

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Capella2SysmlAlgo} algo.
	 * @param source
	 *            the CapellaElement source.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public LogicalFunctionCallBehaviorsMapping(CapellaBridgeAlgo<?> algo, CapellaElement source,
			IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
	}

	@Override
	public void computeMapping() {
		LogicalFunction logicalFunctionRoot = null;
		Activity functionArchAct = null;
		if (_source instanceof Project) {
			logicalFunctionRoot = Sysml2CapellaUtils.getLogicalFunctionRoot(_source);
			org.eclipse.uml2.uml.Package behaviorPkg = (org.eclipse.uml2.uml.Package) MappingRulesManager
					.getCapellaObjectFromAllRules(_source + "BEHAVIOR_PKG");

			functionArchAct = UMLFactory.eINSTANCE.createActivity();
			functionArchAct.setName("02 Functional Architecture");
			behaviorPkg.getPackagedElements().add(functionArchAct);
			Sysml2CapellaUtils.trace(this, _source.eResource(), logicalFunctionRoot, functionArchAct, "FUNCTARCH_");
		} else if (_source instanceof LogicalFunction) {
			logicalFunctionRoot = (LogicalFunction) _source;
			functionArchAct = (Activity) MappingRulesManager.getCapellaObjectFromAllRules(logicalFunctionRoot);
		}
		if (functionArchAct == null) {
			return;
		}

		EList<AbstractFunction> ownedFunctions = logicalFunctionRoot.getOwnedFunctions();
		for (AbstractFunction abstractFunction : ownedFunctions) {
			if (abstractFunction instanceof LogicalFunction) {
				transformCallBehaviors((LogicalFunction) abstractFunction, functionArchAct);
			}
		}
	}

	/**
	 * Transform {@link LogicalFunction} to {@link CallBehaviorAction}.
	 * 
	 * @param lf
	 *            the {@link LogicalFunction} to transform
	 * @param functionArchAct
	 *            the parent {@link Activity}.
	 */
	private void transformCallBehaviors(LogicalFunction lf, Activity functionArchAct) {
		CallBehaviorAction callBehavior = UMLFactory.eINSTANCE.createCallBehaviorAction();
		Activity activity = (Activity) MappingRulesManager.getCapellaObjectFromAllRules(lf);
		if (activity == null) {
			callBehavior.setName(lf.getName());
			callBehavior.setIsLeaf(true);
		} else {
			callBehavior.setBehavior(activity);
		}
		functionArchAct.getOwnedNodes().add(callBehavior);
		Sysml2CapellaUtils.trace(this, _source.eResource(), Sysml2CapellaUtils.getSysMLID(_source.eResource(), lf),
				callBehavior, "CALLBEHAVIOR_");

	}

}
