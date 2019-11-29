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
import java.util.Iterator;
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
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
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
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class FunctionalExchangesMapping extends AbstractMapping {

	/**
	 * The Capella source element.
	 */
	private Project _source;

	/**
	 * Constructor
	 * 
	 * @param algo
	 *            the {@link Capella2SysmlAlgo} algo.
	 * @param source
	 *            the Project capella source
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public FunctionalExchangesMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;

	}

	@Override
	public void computeMapping() {
		LogicalFunction logicalFunctionRoot = Sysml2CapellaUtils.getLogicalFunctionRoot(_source);
		transformFunctionalExchange(logicalFunctionRoot);
	}

	/**
	 * Transform all {@link FunctionalExchange} from {@link LogicalFunction} to
	 * {@link ActivityEdge}.
	 * 
	 * @param logicalFunction
	 *            the container {@link LogicalFunction}.
	 */
	private void transformFunctionalExchange(LogicalFunction logicalFunction) {
		EList<FunctionalExchange> ownedFunctionalExchanges = logicalFunction.getOwnedFunctionalExchanges();
		for (FunctionalExchange functionalExchange : ownedFunctionalExchanges) {

			// Get the acitivy mapped with the LogicalFunction.
			Activity activity = (Activity) MappingRulesManager.getCapellaObjectFromAllRules(logicalFunction);
			if (activity == null) {
				return;
			}

			// get the capella source and target FunctionPort.
			FunctionPort source = (FunctionPort) functionalExchange.getSource();
			FunctionPort target = (FunctionPort) functionalExchange.getTarget();

			// get the capella source and target parent LogicalFunction.
			LogicalFunction sourceParent = (LogicalFunction) source.eContainer();
			LogicalFunction targetParent = (LogicalFunction) target.eContainer();

			// transform the ports and get the mapping between Pin and
			// ActivityParameterNode.
			Map<Pin, ActivityParameterNode> umlSourcePin = transformPort(source, sourceParent);
			Map<Pin, ActivityParameterNode> umlTargetPin = transformPort(target, targetParent);
			// if parents are same.
			if (sourceParent.eContainer().equals(targetParent.eContainer())) {
				transformFunctionalExchangeWithCommonParent(functionalExchange, activity, umlSourcePin, umlTargetPin);

			} else {
				transformFunctionalExchangeWithDiffParent(functionalExchange, source, target, sourceParent,
						targetParent, umlSourcePin, umlTargetPin);
			}
		}

		// transfurm sub Logical functions.
		EList<AbstractFunction> ownedFunctions = logicalFunction.getOwnedFunctions();
		for (AbstractFunction abstractFunction : ownedFunctions) {
			if (abstractFunction instanceof LogicalFunction) {
				transformFunctionalExchange((LogicalFunction) abstractFunction);
			}
		}

	}

	/**
	 * Transform {@link FunctionalExchange} where the ports have not the same
	 * parent.
	 * 
	 * @param functionalExchange
	 *            the {@link FunctionalExchange} to transform
	 * @param source
	 *            the {@link FunctionPort} source.
	 * @param target
	 *            the {@link FunctionPort} target.
	 * @param sourceParent
	 *            the parent {@link LogicalFunction} source
	 * @param targetParent
	 *            the parent {@link LogicalFunction} target
	 * @param umlSourcePin
	 *            the uml source {@link Pin}
	 * @param umlTargetPin
	 *            the uml target {@link Pin}
	 */
	private void transformFunctionalExchangeWithDiffParent(FunctionalExchange functionalExchange, FunctionPort source,
			FunctionPort target, LogicalFunction sourceParent, LogicalFunction targetParent,
			Map<Pin, ActivityParameterNode> umlSourcePin, Map<Pin, ActivityParameterNode> umlTargetPin) {
		// get the ancestors of sourceParent and targetParent LogicalFunctions.
		Queue<LogicalFunction> sourceAncestors = getAncestors(sourceParent);
		Queue<LogicalFunction> targetAnceestors = getAncestors(targetParent);

		// get the common parents.
		LogicalFunction common = getCommonParent(sourceAncestors, targetAnceestors);
		// create intermerdiate ports.
		Pin umlSource = createIntermediatePort(functionalExchange, source, umlSourcePin, common, sourceAncestors);
		Pin umlTarget = createIntermediatePort(functionalExchange, target, umlTargetPin, common, targetAnceestors);

		Activity umlParent = (Activity) MappingRulesManager.getCapellaObjectFromAllRules(common);

		// create ObjectFlow
		ActivityEdge objectFlow = umlParent.createEdge(functionalExchange.getName(), UMLPackage.Literals.OBJECT_FLOW);
		Sysml2CapellaUtils.trace(this, _source.eResource(), functionalExchange, objectFlow, "OBJECTFLOW_");
		LiteralUnlimitedNatural createWeight = (LiteralUnlimitedNatural) objectFlow.createWeight("", null,
				UMLPackage.Literals.LITERAL_UNLIMITED_NATURAL);
		createWeight.setValue(1);
		Sysml2CapellaUtils.trace(this, _source.eResource(),
				Sysml2CapellaUtils.getSysMLID(_source.eResource(), functionalExchange) + "WEIGHT", createWeight,
				"WEIGHT_");
		objectFlow.setSource(umlSource);
		objectFlow.setTarget(umlTarget);
	}

	/**
	 * Get the common parent {@link LogicalFunction}
	 * 
	 * @param sourceAncestors
	 *            all the ancestors {@link LogicalFunction} of the source port.
	 * @param targetAnceestors
	 *            all the ancestors {@link LogicalFunction} of the target port.
	 * @return
	 */
	private LogicalFunction getCommonParent(Queue<LogicalFunction> sourceAncestors,
			Queue<LogicalFunction> targetAnceestors) {
		LogicalFunction common = null;
		for (LogicalFunction targetAncestor : targetAnceestors) {
			if (sourceAncestors.contains(targetAncestor)) {
				common = targetAncestor;
				break;
			}
		}
		return common;
	}

	/**
	 * Transform {@link FunctionalExchange} if the ports have the same parent.
	 * 
	 * @param functionalExchange
	 *            the {@link FunctionalExchange} to transform
	 * @param activity
	 *            the uml {@link Activity} is mapped with the parent
	 *            LogicalFunction
	 * @param umlSourcePin
	 *            the uml source {@link Pin}
	 * @param umlTargetPin
	 *            the uml target {@link Pin}
	 */
	private void transformFunctionalExchangeWithCommonParent(FunctionalExchange functionalExchange, Activity activity,
			Map<Pin, ActivityParameterNode> umlSourcePin, Map<Pin, ActivityParameterNode> umlTargetPin) {
		ActivityEdge objectFlow = activity.createEdge(functionalExchange.getName(), UMLPackage.Literals.OBJECT_FLOW);

		objectFlow.setSource((Pin) getPort(umlSourcePin, Pin.class));
		objectFlow.setTarget((Pin) getPort(umlTargetPin, Pin.class));

		LiteralUnlimitedNatural createWeight = (LiteralUnlimitedNatural) objectFlow.createWeight("", null,
				UMLPackage.Literals.LITERAL_UNLIMITED_NATURAL);
		createWeight.setValue(1);
		Sysml2CapellaUtils.trace(this, _source.eResource(),
				Sysml2CapellaUtils.getSysMLID(_source.eResource(), functionalExchange) + "WEIGHT", createWeight,
				"WEIGHT_");
		Sysml2CapellaUtils.trace(this, _source.eResource(), functionalExchange, objectFlow, "OBJECT_FLOW_");
	}

	/**
	 * Create the intermediate port and ObjectFlow.
	 * 
	 * @param functionalExchange
	 * 
	 * @param capellaPort
	 *            the capella reference port.
	 * @param map
	 *            the {@link Map} {@link Pin}, {@link ActivityParameterNode}
	 * @param common
	 *            the common parent
	 * @param ancestors
	 *            the ancestor of the ref {@link FunctionPort}.
	 * @return the Intermediate {@link Pin}.
	 */
	private Pin createIntermediatePort(FunctionalExchange functionalExchange, FunctionPort capellaPort,
			Map<Pin, ActivityParameterNode> map, LogicalFunction common, Queue<LogicalFunction> ancestors) {
		LogicalFunction parent;
		boolean isFoundCommon = false;

		Pin umlPin = (Pin) getPort(map, Pin.class);

		// while the common parent is not found
		while ((parent = ancestors.poll()) != null && !isFoundCommon) {
			// if common parent found, exit of the while loop.
			if (common.equals(parent)) {
				isFoundCommon = true;
			}
			// get the callbehavior element mapped with the parent
			// LogicalFunction.
			CallBehaviorAction callParent = (CallBehaviorAction) MappingRulesManager
					.getCapellaObjectFromAllRules(Sysml2CapellaUtils.getSysMLID(_source.eResource(), parent));
			// if the common parent is not found.
			if (!isFoundCommon && callParent != null) {
				// get the activity from call behavior.
				Behavior behavior = callParent.getBehavior();
				if (behavior instanceof Activity) {
					// create intermediate ObjecFlow.
					ActivityEdge objectFlow = ((Activity) behavior).createEdge(functionalExchange.getName(),
							UMLPackage.Literals.OBJECT_FLOW);
					Sysml2CapellaUtils.trace(this, _source.eResource(),
							Sysml2CapellaUtils.getSysMLID(_source.eResource(), functionalExchange)
									+ Sysml2CapellaUtils.getSysMLID(_source.eResource(), parent),
							objectFlow, "OBJECT_FLOW_");

					// Create intermediate port.
					ActivityParameterNode mirrorApn = null;
					Pin mirrorPin = null;
					Parameter createOwnedParameter = null;
					if (capellaPort instanceof FunctionOutputPort) {
						mirrorPin = callParent.createResult(functionalExchange.getName(), null);
						mirrorApn = (ActivityParameterNode) ((Activity) behavior).createOwnedNode(
								functionalExchange.getName(), UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
						createOwnedParameter = ((Activity) behavior).createOwnedParameter(functionalExchange.getName(),
								null);
						createOwnedParameter.setDirection(ParameterDirectionKind.OUT_LITERAL);
						mirrorApn.setParameter(createOwnedParameter);
						objectFlow.setSource(umlPin);
						objectFlow.setTarget(mirrorApn);
					}
					if (capellaPort instanceof FunctionInputPort) {
						mirrorPin = callParent.createArgument(functionalExchange.getName(), null);
						mirrorApn = (ActivityParameterNode) ((Activity) behavior).createOwnedNode(
								functionalExchange.getName(), UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
						createOwnedParameter = ((Activity) behavior).createOwnedParameter(functionalExchange.getName(),
								null);
						mirrorApn.setParameter(createOwnedParameter);

						objectFlow.setSource(mirrorApn);
						objectFlow.setTarget(umlPin);
					}
					// connect intermediate port at the intermediate object
					// flow.

					Sysml2CapellaUtils.trace(this, _source.eResource(), capellaPort, mirrorApn, "APN_MIRROR_");
					Sysml2CapellaUtils.trace(this, _source.eResource(), capellaPort, mirrorPin, "PIN_MIRROR_");
					Sysml2CapellaUtils.trace(this, _source.eResource(), capellaPort, createOwnedParameter,
							"PARAM_MIRROR_");
					LiteralUnlimitedNatural createWeight = (LiteralUnlimitedNatural) objectFlow.createWeight("", null,
							UMLPackage.Literals.LITERAL_UNLIMITED_NATURAL);
					createWeight.setValue(1);
					Sysml2CapellaUtils.trace(this, _source.eResource(),
							Sysml2CapellaUtils.getSysMLID(_source.eResource(), functionalExchange) + "WEIGHT_MIRROR_",
							createWeight, "WEIGHT_");

					umlPin = mirrorPin;

				}
			}

		}
		return umlPin;
	}

	/**
	 * Transform {@link FunctionPort} to {@link ActivityParameterNode}and
	 * {@link Pin}.
	 * 
	 * @param functionPort
	 *            the {@link FunctionPort} to transform
	 * @param parent
	 *            the parent LogicalFunction.
	 * @return the {@link Map}< {@link Pin}, {@link ActivityParameterNode}>
	 */
	private Map<Pin, ActivityParameterNode> transformPort(FunctionPort functionPort, LogicalFunction parent) {
		CallBehaviorAction umlParent = (CallBehaviorAction) MappingRulesManager
				.getCapellaObjectFromAllRules(Sysml2CapellaUtils.getSysMLID(_source.eResource(), parent));
		Pin umlPort = null;
		ActivityParameterNode apn = null;
		Parameter createOwnedParameter = null;
		Map<Pin, ActivityParameterNode> results = new HashMap<>();
		if (umlParent == null) {
			return results;
		}
		if (functionPort instanceof FunctionInputPort) {
			umlPort = umlParent.createArgument(functionPort.getName(), null);
			Activity activity = (Activity) umlParent.getBehavior();
			if (activity != null) {
				apn = (ActivityParameterNode) activity.createOwnedNode(functionPort.getName(),
						UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
				createOwnedParameter = activity.createOwnedParameter(functionPort.getName(), null);
				apn.setParameter(createOwnedParameter);
			}
		} else if (functionPort instanceof FunctionOutputPort) {
			umlPort = umlParent.createResult(functionPort.getName(), null);
			Activity activity = (Activity) umlParent.getBehavior();
			if (activity != null) {
				apn = (ActivityParameterNode) activity.createOwnedNode(functionPort.getName(),
						UMLPackage.Literals.ACTIVITY_PARAMETER_NODE);
				createOwnedParameter = activity.createOwnedParameter(functionPort.getName(), null);
				createOwnedParameter.setDirection(ParameterDirectionKind.OUT_LITERAL);
				apn.setParameter(createOwnedParameter);
			}
		}
		Sysml2CapellaUtils.trace(this, _source.eResource(), functionPort, umlPort, "PIN_");
		Sysml2CapellaUtils.trace(this, _source.eResource(), functionPort, apn, "APN_");
		Sysml2CapellaUtils.trace(this, _source.eResource(), functionPort, createOwnedParameter, "PARAM_");

		results.put(umlPort, apn);
		return results;
	}

	/**
	 * get all ancestors {@link LogicalFunction} of a LogicalFunction.
	 * 
	 * @param lf
	 *            the child LogicalFunction
	 * @return {@link Queue} of LogicalFunction.
	 */
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

	/**
	 * Get the {@link Pin} or the {@link ActivityParameterNode} from the
	 * {@link Map}
	 * 
	 * @param map
	 *            {@link Map} of {@link Pin} and {@link ActivityParameterNode}.
	 * @param clazz
	 *            the type to get.
	 * @return {@link Pin} or {@link ActivityParameterNode}.
	 */
	private EObject getPort(Map<Pin, ActivityParameterNode> map, Class<?> clazz) {
		Set<Entry<Pin, ActivityParameterNode>> entrySet = map.entrySet();
		Iterator<Entry<Pin, ActivityParameterNode>> iterator = entrySet.iterator();
		if (iterator.hasNext()) {
			Entry<Pin, ActivityParameterNode> next = iterator.next();
			if (Pin.class.isAssignableFrom(clazz)) {
				return next.getKey();
			} else if (ActivityParameterNode.class.isAssignableFrom(clazz)) {
				return next.getValue();
			}
		}
		return null;
	}

}
