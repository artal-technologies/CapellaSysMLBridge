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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Pin;
import org.eclipse.uml2.uml.UMLPackage;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class FunctionalExchangesMapping extends AbstractMapping {

	private Project _source;

	public FunctionalExchangesMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;

	}

	@Override
	public void computeMapping() {
		LogicalFunction logicalFunctionRoot = Sysml2CapellaUtils.getLogicalFunctionRoot(_source);

		transformFunctionalExchange(logicalFunctionRoot);
	}

	private void transformFunctionalExchange(LogicalFunction logicalFunction) {
		EList<FunctionalExchange> ownedFunctionalExchanges = logicalFunction.getOwnedFunctionalExchanges();
		for (FunctionalExchange functionalExchange : ownedFunctionalExchanges) {

			Activity activity = (Activity) MappingRulesManager.getCapellaObjectFromAllRules(logicalFunction);
			if (activity == null) {
				return;
			}
			FunctionPort source = (FunctionPort) functionalExchange.getSource();
			FunctionPort target = (FunctionPort) functionalExchange.getTarget();

			LogicalFunction sourceParent = (LogicalFunction) source.eContainer();
			LogicalFunction targetParent = (LogicalFunction) target.eContainer();
			Map<Pin, ActivityParameterNode> umlSourcePin = transformPort(source, sourceParent);
			Map<Pin, ActivityParameterNode> umlTargetPin = transformPort(target, targetParent);
			if (sourceParent.eContainer().equals(targetParent.eContainer())) {
				ActivityEdge objectFlow = activity.createEdge(functionalExchange.getName(),
						UMLPackage.Literals.OBJECT_FLOW);

				objectFlow.setSource((Pin) getPort(umlSourcePin, Pin.class));
				objectFlow.setTarget((Pin) getPort(umlTargetPin, Pin.class));

				LiteralUnlimitedNatural createWeight = (LiteralUnlimitedNatural) objectFlow.createWeight("", null,
						UMLPackage.Literals.LITERAL_UNLIMITED_NATURAL);
				createWeight.setValue(1);
				Sysml2CapellaUtils.trace(this, _source.eResource(),
						Sysml2CapellaUtils.getSysMLID(_source.eResource(), functionalExchange) + "WEIGHT", objectFlow,
						"WEIGHT_");
				Sysml2CapellaUtils.trace(this, _source.eResource(), functionalExchange, objectFlow, "OBJECT_FLOW_");

			} else {
				Queue<LogicalFunction> sourceAncestors = getAncestors(sourceParent);
				Queue<LogicalFunction> targetAnceestors = getAncestors(targetParent);
				LogicalFunction common = null;
				for (LogicalFunction targetAncestor : targetAnceestors) {
					if (sourceAncestors.contains(targetAncestor)) {
						common = targetAncestor;
						break;
					}
				}
				Pin umlSource = createIntermediatePort(source, umlSourcePin, common, sourceAncestors);
				Pin umlTarget = createIntermediatePort(target, umlTargetPin, common, targetAnceestors);

				Activity umlParent = (Activity) MappingRulesManager.getCapellaObjectFromAllRules(common);
				ActivityEdge objectFlow = umlParent.createEdge(functionalExchange.getName(),
						UMLPackage.Literals.OBJECT_FLOW);
				Sysml2CapellaUtils.trace(this, _source.eResource(), functionalExchange, objectFlow, "OBJECTFLOW_");
				LiteralUnlimitedNatural createWeight = (LiteralUnlimitedNatural) objectFlow.createWeight("", null,
						UMLPackage.Literals.LITERAL_UNLIMITED_NATURAL);
				createWeight.setValue(1);
				Sysml2CapellaUtils.trace(this, _source.eResource(),
						Sysml2CapellaUtils.getSysMLID(_source.eResource(), functionalExchange) + "WEIGHT", objectFlow,
						"WEIGHT_");
				objectFlow.setSource(umlSource);
				objectFlow.setTarget(umlTarget);
			}
		}
		EList<AbstractFunction> ownedFunctions = logicalFunction.getOwnedFunctions();

		for (AbstractFunction abstractFunction : ownedFunctions) {
			if (abstractFunction instanceof LogicalFunction) {
				transformFunctionalExchange((LogicalFunction) abstractFunction);
			}
		}

	}

	private Pin createIntermediatePort(FunctionPort capellaPort, Map<Pin, ActivityParameterNode> map,
			LogicalFunction common, Queue<LogicalFunction> ancestors) {
		LogicalFunction parent;
		boolean isFoundCommon = false;

		Pin umlPin = (Pin) getPort(map, Pin.class);

		while ((parent = ancestors.poll()) != null && !isFoundCommon) {
			if (common.equals(parent)) {
				isFoundCommon = true;
			}
			CallBehaviorAction callParent = (CallBehaviorAction) MappingRulesManager
					.getCapellaObjectFromAllRules(Sysml2CapellaUtils.getSysMLID(_source.eResource(), parent));
			if (!isFoundCommon) {
				Behavior behavior = callParent.getBehavior();
				if (behavior instanceof Activity) {
					ActivityEdge objectFlow = ((Activity) behavior).createEdge("", UMLPackage.Literals.OBJECT_FLOW);
					// Sysml2CapellaUtils.trace(this, _source.eResource(),
					// functionalExchange, objectFlow, "OBJECT_FLOW_");

					objectFlow.setSource(umlPin);

					ActivityParameterNode mirrorApn = null;
					Pin mirrorPin = null;
					if (capellaPort instanceof FunctionOutputPort) {
						mirrorPin = callParent.createResult("", null);
						mirrorApn = (ActivityParameterNode) ((Activity) behavior).createOwnedNode("",
								UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
					}
					if (capellaPort instanceof FunctionInputPort) {
						mirrorPin = callParent.createArgument("", null);
						mirrorApn = (ActivityParameterNode) ((Activity) behavior).createOwnedNode("",
								UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
					}
					objectFlow.setTarget(mirrorApn);

					LiteralUnlimitedNatural createWeight = (LiteralUnlimitedNatural) objectFlow.createWeight("", null,
							UMLPackage.Literals.LITERAL_UNLIMITED_NATURAL);
					createWeight.setValue(1);
					Sysml2CapellaUtils.trace(this, _source.eResource(),
							Sysml2CapellaUtils.getSysMLID(_source.eResource(), behavior) + "WEIGHT", objectFlow,
							"WEIGHT_");

					umlPin = mirrorPin;

				}
			}

		}
		return umlPin;
	}

	private Map<Pin, ActivityParameterNode> transformPort(FunctionPort functionPort, LogicalFunction parent) {
		CallBehaviorAction umlParent = (CallBehaviorAction) MappingRulesManager
				.getCapellaObjectFromAllRules(Sysml2CapellaUtils.getSysMLID(_source.eResource(), parent));
		Pin umlPort = null;
		ActivityParameterNode apn = null;
		if (functionPort instanceof FunctionInputPort) {
			umlPort = umlParent.createArgument(functionPort.getName(), null);
			Activity activity = (Activity) umlParent.getBehavior();
			if (activity != null) {
				apn = (ActivityParameterNode) activity.createOwnedNode(functionPort.getName(),
						UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
			}
		} else if (functionPort instanceof FunctionOutputPort) {
			umlPort = umlParent.createResult(functionPort.getName(), null);
			Activity activity = (Activity) umlParent.getBehavior();
			if (activity != null) {
				apn = (ActivityParameterNode) activity.createOwnedNode(functionPort.getName(),
						UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
			}
		}
		Sysml2CapellaUtils.trace(this, _source.eResource(), functionPort, umlPort, "PIN_");
		Sysml2CapellaUtils.trace(this, _source.eResource(), functionPort, umlPort, "APN_");
		Map<Pin, ActivityParameterNode> results = new HashMap<>();
		results.put(umlPort, apn);
		return results;
	}

	private Queue<LogicalFunction> getAncestors(LogicalFunction lf) {
		Queue<LogicalFunction> queue = new LinkedList<>();
		EObject parent = lf;
		while (parent instanceof LogicalFunction) {
			parent = parent.eContainer();
			if (parent instanceof LogicalFunction) {
				queue.add((LogicalFunction) parent);
			}
		}

		return queue;

	}

	private EObject getPort(Map<Pin, ActivityParameterNode> map, Class<?> clazz) {
		Set<Entry<Pin, ActivityParameterNode>> entrySet = map.entrySet();
		Entry<Pin, ActivityParameterNode> next = entrySet.iterator().next();
		if (Pin.class.isAssignableFrom(clazz)) {
			return next.getKey();
		} else if (ActivityParameterNode.class.isAssignableFrom(clazz)) {
			return next.getValue();
		}
		return null;
	}

}
