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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Pin;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class FunctionalExchangeMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Activity _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	Map<ObjectFlow, List<FunctionPort>> managedMergedObjectFlow = new HashMap<>();

	public FunctionalExchangeMapping(Sysml2CapellaAlgo algo, Activity source, IMappingExecution mappingExecution) {
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

		LogicalFunctionMapping functionRule = (LogicalFunctionMapping) MappingRulesManager
				.getRule(LogicalFunctionMapping.class.getName());
		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalFunction logicalFunctionRoot = Sysml2CapellaUtils.getLogicalFunctionRoot(targetScope.getProject());
		transformObjectFlow(eResource, _source, logicalFunctionRoot, functionRule.getMapPinToParam(), true);
	}

	/**
	 * Transform {@link ObjectFlow} to {@link FunctionalExchange}. Browse all
	 * the {@link Activity} breakdown to transform all {@link ObjectFlow}
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param activity
	 *            the {@link Activity} containing the {@link ObjectFlow}
	 * @param functionContainer
	 *            the LogicalFunction parent where is added the
	 *            {@link FunctionalExchange}
	 * @param mapPinToParam
	 *            {@link Map} allowing to have the link between the {@link Pin}
	 *            and the {@link ActivityParameterNode}
	 * @param firstLevel
	 *            at the first level the {@link ActivityParameterNode} are
	 *            managed.
	 */
	private void transformObjectFlow(Resource eResource, Activity activity, LogicalFunction functionContainer,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean firstLevel) {

		transformEdges(eResource, activity, functionContainer, mapPinToParam, firstLevel);

		AbstractMapping rule = MappingRulesManager.getRule(LogicalFunctionMapping.class.getName());
		EList<ActivityNode> nodes = activity.getNodes();
		for (ActivityNode node : nodes) {
			if (node instanceof CallBehaviorAction) {
				Behavior behavior = ((CallBehaviorAction) node).getBehavior();
				if (behavior != null) {
					LogicalFunction lf = (LogicalFunction) rule.getMapSourceToTarget().get(node);
					transformObjectFlow(eResource, (Activity) behavior, lf, mapPinToParam, false);
				}
			}
		}
	}

	/**
	 * Transform {@link ObjectFlow} to {@link FunctionalExchange}.
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param activity
	 *            the {@link Activity} containing the {@link ObjectFlow}
	 * @param functionContainer
	 *            the LogicalFunction parent where is added the
	 *            {@link FunctionalExchange}
	 * @param mapPinToParam
	 *            {@link Map} allowing to have the link between the {@link Pin}
	 *            and the {@link ActivityParameterNode}
	 * @param firstLevel
	 *            at the first level the {@link ActivityParameterNode} are
	 *            managed.
	 */
	private void transformEdges(Resource eResource, Activity activity, LogicalFunction functionContainer,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean firstLevel) {
		EList<ActivityEdge> edges = activity.getEdges();
		for (ActivityEdge edge : edges) {

			if (edge instanceof ObjectFlow) {
				ObjectFlow objecFlow = (ObjectFlow) edge;
				ActivityNode source = objecFlow.getSource();
				ActivityNode target = objecFlow.getTarget();
				if ((source instanceof ActivityParameterNode || target instanceof ActivityParameterNode)
						&& !firstLevel) {
					continue;
				}
				if (target instanceof MergeNode) {
					continue;
				}
				List<FunctionPort> sourceCapPorts = getCapellaFunctionPort(eResource, source, mapPinToParam, true,
						false, objecFlow);

				List<FunctionPort> targetCapPorts = getCapellaFunctionPort(eResource, target, mapPinToParam, false,
						false, objecFlow);

				boolean hasManyFE = false;
				if (sourceCapPorts.size() > 1 || targetCapPorts.size() > 1) {
					hasManyFE = true;
				}

				for (FunctionPort targetCapPort : targetCapPorts) {
					for (FunctionPort sourceCapPort : sourceCapPorts) {
						String suffix = "";
						if (hasManyFE) {
							suffix = targetCapPort.getName() + "_" + sourceCapPort.getName();
						}
						if (targetCapPort != null && sourceCapPort != null) {
							FunctionalExchange exchange = FaFactory.eINSTANCE.createFunctionalExchange();
							if (objecFlow.getName() != null && !objecFlow.getName().isEmpty()) {
								exchange.setName(objecFlow.getName());
							} else if (sourceCapPort instanceof FunctionOutputPort) {
								if (sourceCapPort.getName() != null && !sourceCapPort.getName().isEmpty()) {
									exchange.setName(sourceCapPort.getName());
								} else {
									exchange.setName(targetCapPort.getName());
								}
							} else if (targetCapPort instanceof FunctionOutputPort) {
								if (targetCapPort.getName() != null && !targetCapPort.getName().isEmpty()) {
									exchange.setName(targetCapPort.getName());
								} else {
									exchange.setName(sourceCapPort.getName());
								}
							}
							functionContainer.getOwnedFunctionalExchanges().add(exchange);
							exchange.setSource((org.polarsys.capella.common.data.activity.ActivityNode) sourceCapPort);
							exchange.setTarget((org.polarsys.capella.common.data.activity.ActivityNode) targetCapPort);
							Sysml2CapellaUtils.trace(this, eResource, objecFlow, exchange,
									"FunctionalExchange_" + suffix);
						}
					}
				}
			}
		}
	}

	/**
	 * Get capella {@link FunctionPort}.
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param source
	 *            the {@link Activity} node to treat
	 * @param mapPinToParam
	 *            {@link Map} allowing to have the link between the {@link Pin}
	 *            and the {@link ActivityParameterNode}
	 * @param isSource
	 *            true if the port is source
	 * @param transformParam
	 * @param objecFlow
	 *            the source exchange
	 * @return {@link FunctionPort}
	 */
	private List<FunctionPort> getCapellaFunctionPort(Resource eResource, ActivityNode source,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean isSource, boolean transformParam,
			ObjectFlow objecFlow) {

		if (source instanceof Pin) {
			return manageExchangeWithPin(eResource, source, mapPinToParam, objecFlow);
		} else if (source instanceof ActivityParameterNode) {
			return manageExchangeWithParam(eResource, source, mapPinToParam, transformParam);
		}
		// the association is not linked at a port.
		else if (source instanceof CallBehaviorAction) {
			return manageExchangeWithCallBehavior(eResource, source, isSource, objecFlow);

		} else if (source instanceof MergeNode) {
			List<FunctionPort> mergeResults = manageMergeNode(eResource, source, mapPinToParam, isSource,
					transformParam, objecFlow);
			return mergeResults;

		} else if (source instanceof ForkNode) {
			List<FunctionPort> forkResults = manageForkNode(eResource, source, mapPinToParam, isSource, transformParam,
					objecFlow);
			return forkResults;

		}

		return Collections.emptyList();
	}

	/**
	 * Manage the fork node
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param source
	 *            the {@link Activity} node to treat
	 * @param mapPinToParam
	 *            {@link Map} allowing to have the link between the {@link Pin}
	 *            and the {@link ActivityParameterNode}
	 * @param isSource
	 *            true if the port is source
	 * @param transformParam
	 * @param objecFlow
	 *            the source exchange
	 */
	private List<FunctionPort> manageForkNode(Resource eResource, ActivityNode source,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean isSource, boolean transformParam,
			ObjectFlow objecFlow) {
		List<FunctionPort> forkResults = new ArrayList<>();
		if (isSource) {
			EList<ActivityEdge> incomings = ((ForkNode) source).getOutgoings();
			for (ActivityEdge activityEdge : incomings) {
				if (managedMergedObjectFlow.containsKey(activityEdge)) {
					forkResults.addAll(managedMergedObjectFlow.get(activityEdge));
					continue;
				}
				if (activityEdge.getSource().equals(source)) {
					ActivityNode target = activityEdge.getTarget();
					forkResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				} else if (activityEdge.getTarget().equals(source)) {
					ActivityNode target = activityEdge.getSource();
					forkResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				}
				managedMergedObjectFlow.put((ObjectFlow) activityEdge, forkResults);
			}

		} else {
			EList<ActivityEdge> outgoings = ((ForkNode) source).getIncomings();
			for (ActivityEdge activityEdge : outgoings) {
				if (managedMergedObjectFlow.containsKey(activityEdge)) {
					forkResults.addAll(managedMergedObjectFlow.get(activityEdge));
					continue;
				}
				if (activityEdge.getSource().equals(source)) {
					ActivityNode target = activityEdge.getTarget();
					forkResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				} else if (activityEdge.getTarget().equals(source)) {
					ActivityNode target = activityEdge.getSource();
					forkResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				}
				managedMergedObjectFlow.put((ObjectFlow) activityEdge, forkResults);
			}
		}
		return forkResults;
	}

	/**
	 * 
	 * Manage the merge node
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param source
	 *            the {@link Activity} node to treat
	 * @param mapPinToParam
	 *            {@link Map} allowing to have the link between the {@link Pin}
	 *            and the {@link ActivityParameterNode}
	 * @param isSource
	 *            true if the port is source
	 * @param transformParam
	 * @param objecFlow
	 *            the source exchange
	 * @return
	 */
	private List<FunctionPort> manageMergeNode(Resource eResource, ActivityNode source,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean isSource, boolean transformParam,
			ObjectFlow objecFlow) {
		List<FunctionPort> mergeResults = new ArrayList<>();
		if (isSource) {
			EList<ActivityEdge> incomings = ((MergeNode) source).getIncomings();
			for (ActivityEdge activityEdge : incomings) {
				if (managedMergedObjectFlow.containsKey(activityEdge)) {
					mergeResults.addAll(managedMergedObjectFlow.get(activityEdge));
					continue;
				}
				if (activityEdge.getSource().equals(source)) {
					ActivityNode target = activityEdge.getTarget();
					mergeResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				} else if (activityEdge.getTarget().equals(source)) {
					ActivityNode target = activityEdge.getSource();
					mergeResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				}
				managedMergedObjectFlow.put((ObjectFlow) activityEdge, mergeResults);
			}

		} else {
			EList<ActivityEdge> outgoings = ((MergeNode) source).getOutgoings();
			for (ActivityEdge activityEdge : outgoings) {
				if (managedMergedObjectFlow.containsKey(activityEdge)) {
					mergeResults.addAll(managedMergedObjectFlow.get(activityEdge));
					continue;
				}
				if (activityEdge.getSource().equals(source)) {
					ActivityNode target = activityEdge.getTarget();
					mergeResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				} else if (activityEdge.getTarget().equals(source)) {
					ActivityNode target = activityEdge.getSource();
					mergeResults.addAll(getCapellaFunctionPort(eResource, target, mapPinToParam, isSource,
							transformParam, objecFlow));
				}
				managedMergedObjectFlow.put((ObjectFlow) activityEdge, mergeResults);
			}
		}
		return mergeResults;
	}

	/**
	 * Manage the {@link ObjectFlow} port is a {@link ActivityParameterNode}
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param source
	 *            the {@link ActivityParameterNode}
	 * @param mapPinToParam
	 *            {@link Map} allowing to have the link between the {@link Pin}
	 *            and the {@link ActivityParameterNode}
	 * @param transformParam
	 * @return {@link FunctionPort}
	 */
	private List<FunctionPort> manageExchangeWithParam(Resource eResource, ActivityNode source,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean transformParam) {
		EObject sourcePortParent = source.eContainer();
		if (sourcePortParent instanceof Activity
				&& ((Activity) sourcePortParent).getName().equals("02 Functional Architecture")) {
			AbstractMapping rule = MappingRulesManager.getRule(FunctionalArchitectureMapping.class.getName());
			List<FunctionPort> ports = new ArrayList<>();
			ports.add((FunctionPort) rule.getMapSourceToTarget().get(source));
			return ports;
		}
		if (transformParam) {
			ActivityParameterNode an = (ActivityParameterNode) source;
			List<ActivityEdge> e = new ArrayList<ActivityEdge>();
			e.addAll(an.getIncomings());
			e.addAll(an.getOutgoings());
			for (ActivityEdge activityEdge : e) {
				if (activityEdge instanceof ObjectFlow) {
					ActivityNode s = activityEdge.getSource();
					ActivityNode t = activityEdge.getTarget();
					boolean inDir = an.getParameter().getDirection() == ParameterDirectionKind.IN_LITERAL;
					if (s.equals(an)) {

						return getCapellaFunctionPort(eResource, t, mapPinToParam, inDir, true,
								(ObjectFlow) activityEdge);
					}
					if (t.equals(an)) {
						return getCapellaFunctionPort(eResource, s, mapPinToParam, inDir, true,
								(ObjectFlow) activityEdge);
					}
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Manage the {@link ObjectFlow} port is a {@link Pin}
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param source
	 *            the {@link Pin} source
	 * @param mapPinToParam
	 *            {@link Map} allowing to have the link between the {@link Pin}
	 *            and the {@link ActivityParameterNode}
	 * @param objecFlow
	 *            the source exchange
	 * @return {@link FunctionPort}
	 */
	private List<FunctionPort> manageExchangeWithPin(Resource eResource, ActivityNode source,
			Map<Pin, ActivityParameterNode> mapPinToParam, ObjectFlow objecFlow) {
		ActivityParameterNode parameter = mapPinToParam.get(source);
		if (parameter != null) {
			return getCapellaFunctionPort(eResource, parameter, mapPinToParam, source instanceof InputPin, true,
					objecFlow);
		}
		EObject sourcePortParent = source.eContainer();
		AbstractMapping rule = MappingRulesManager.getRule(LogicalFunctionPortMapping.class.getName()
				+ Sysml2CapellaUtils.getSysMLID(eResource, sourcePortParent));
		if (rule == null) {
			return Collections.emptyList();
		}
		List<FunctionPort> ports = new ArrayList<>();
		ports.add((FunctionPort) rule.getMapSourceToTarget().get(source));
		return ports;
	}

	/**
	 * Manage the {@link ObjectFlow} has not port.
	 * 
	 * @param eResource
	 *            the Sysml resource
	 * @param source
	 *            the {@link CallBehaviorAction} source
	 * @param isSource
	 *            the port is source
	 * @param objecFlow
	 *            the exchange
	 * @return {@link FunctionPort}
	 */
	private List<FunctionPort> manageExchangeWithCallBehavior(Resource eResource, ActivityNode source, boolean isSource,
			ObjectFlow objecFlow) {
		Object object = MappingRulesManager.getRule(LogicalFunctionMapping.class.getName()).getMapSourceToTarget()
				.get(source);
		LogicalFunction lfParent = null;
		if (object instanceof LogicalFunction) {
			lfParent = (LogicalFunction) object;
		} else if (object instanceof List) {
			List<?> list = (List<?>) object;
			for (Object object2 : list) {
				if (object2 instanceof LogicalFunction) {
					lfParent = (LogicalFunction) object2;
					break;
				}
			}
		}

		FunctionPort port = null;
		if (isSource) {
			port = FaFactory.eINSTANCE.createFunctionInputPort();
			lfParent.getInputs().add((FunctionInputPort) port);

		} else {
			port = FaFactory.eINSTANCE.createFunctionOutputPort();
			lfParent.getOutputs().add((FunctionOutputPort) port);
		}
		port.setName(objecFlow.getName());
		Sysml2CapellaUtils.trace(this, eResource, objecFlow, port, "NewPort");
		List<FunctionPort> ports = new ArrayList<>();
		ports.add(port);
		return ports;
	}

}
