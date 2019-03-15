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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.capella.integration.util.CapellaUtil;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Pin;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class LogicalFunctionMapping extends AbstractMapping {
	/**
	 * The sysml root {@link Model}.
	 */
	Activity _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * Manage sub rules
	 */
	MappingRulesManager _manager = new MappingRulesManager();
	private Map<Pin, ActivityParameterNode> _mapPinToParam;

	public LogicalFunctionMapping(Sysml2CapellaAlgo algo, Activity source, IMappingExecution mappingExecution) {
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
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalFunction logicalFunctionRoot = CapellaUtil.getLogicalFunctionRoot(targetScope.getProject());
		_mapPinToParam = new HashMap<>();
		transformCallBehavior(eResource, logicalFunctionRoot, _source, _mapPinToParam);
		_manager.executeRules();

	}

	public Map<Pin, ActivityParameterNode> getMapPinToParam() {
		return _mapPinToParam;
	}

	/**
	 * 
	 * Transform all the {@link CallBehaviorAction} under the
	 * <code>activity</code> parameter. All activities are transformed in
	 * {@link LogicalFunction}. The leaf activity "ports" are transformed to
	 * {@link FunctionPort}.
	 * 
	 * @param eResource
	 *            the Sysml resource. Allows to get the sysml element ID.
	 * @param logicalFunctionRoot
	 *            the logical function containing the transformed
	 *            {@link LogicalFunction}
	 * @param activity
	 *            the source activity to browse.
	 * @return a {@link Boolean} true if the source <code>activity</code> are
	 *         children.
	 */
	private boolean transformCallBehavior(Resource eResource, LogicalFunction logicalFunctionRoot, Activity activity,
			Map<Pin, ActivityParameterNode> mapPinToParam) {
		EList<ActivityNode> nodes = activity.getNodes();
		boolean hasChild = false;
		for (ActivityNode activityNode : nodes) {
			if (activityNode instanceof CallBehaviorAction) {
				hasChild = true;
				break;
			}
		}
		for (ActivityNode activityNode : nodes) {
			if (activityNode instanceof ActivityParameterNode && hasChild) {
				Parameter parameter = ((ActivityParameterNode) activityNode).getParameter();
				Set<Pin> keySet = mapPinToParam.keySet();
				for (Pin pin : keySet) {
					if (pin.getName().equals(parameter.getName())) {
						mapPinToParam.put(pin, (ActivityParameterNode) activityNode);
						break;
					}
				}
			}
		}
		// browse all the CallBehavior nodes
		for (ActivityNode activityNode : nodes) {
			if (activityNode instanceof CallBehaviorAction) {

				// boolean should be true for transformed activity ports.
				boolean transformPort = true;

				String lfName = activityNode.getName();
				LogicalFunction lFunction = LaFactory.eINSTANCE.createLogicalFunction();
				logicalFunctionRoot.getOwnedFunctions().add(lFunction);
				Sysml2CapellaUtils.trace(this, eResource, activityNode, lFunction, "LogicalFunction_");

				// if activityNode has children, transform these.
				Behavior behavior = ((CallBehaviorAction) activityNode).getBehavior();
				if (behavior != null) {
					if (behavior.getName() != null && !behavior.getName().isEmpty()) {
						lfName = behavior.getName();
						// if behavior has not children, the ports from activity
						// are transformed.
						Map<Pin, ActivityParameterNode> map = new HashMap<Pin, ActivityParameterNode>();

						EList<InputPin> arguments = ((CallBehaviorAction) activityNode).getArguments();
						for (InputPin inputPin : arguments) {
							map.put(inputPin, null);
						}
						EList<OutputPin> results = ((CallBehaviorAction) activityNode).getResults();
						for (OutputPin outputPin : results) {
							map.put(outputPin, null);
						}
						transformPort = !transformCallBehavior(eResource, lFunction, (Activity) behavior, map);
						mapPinToParam.putAll(map);
					}
				}
				lFunction.setName(lfName);

				// transform the ports of the leaf activity.
				if (transformPort) {
					LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
							(CallBehaviorAction) activityNode, _mappingExecution);
					_manager.add(functionPortMapping.getClass().getName()
							+ Sysml2CapellaUtils.getSysMLID(eResource, activityNode), functionPortMapping);

				}
			}

		}
		return hasChild;
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
