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
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InformationFlow;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Pin;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.fa.AbstractFunctionalBlock;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.ComponentExchangeFunctionalExchangeAllocation;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.data.information.InformationFactory;
import org.polarsys.capella.core.data.information.Port;
import org.polarsys.capella.core.data.information.PortAllocation;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalFunction;
import org.polarsys.capella.core.model.helpers.ComponentExchangeExt;

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
	private Map<LogicalFunction, Class> _mapActivityNodeToClass;

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
		_mapActivityNodeToClass = functionRule.getMapActivityNodeToClass();
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
					Object object = rule.getMapSourceToTarget().get(node);
					if (object instanceof List<?>) {
						List<?> list = (List<?>) object;
						for (Object object2 : list) {
							LogicalFunction lf = (LogicalFunction) object2;
							transformObjectFlow(eResource, (Activity) behavior, lf, mapPinToParam, false);
						}
					} else {
						LogicalFunction lf = (LogicalFunction) object;
						transformObjectFlow(eResource, (Activity) behavior, lf, mapPinToParam, false);
					}
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
				List<FunctionPort> sourceCapPorts = getCapellaFunctionPort(eResource, source, mapPinToParam, true,
						false, objecFlow);

				List<FunctionPort> targetCapPorts = getCapellaFunctionPort(eResource, target, mapPinToParam, false,
						false, objecFlow);

				boolean hasManyFE = false;
				if (sourceCapPorts.size() > 1 || targetCapPorts.size() > 1) {
					hasManyFE = true;
				}

				Signal signal = null;
				if (!getAlgo().isEventOption()) {
					signal = Sysml2CapellaUtils.getSignal(source.eContainer());
					if (signal == null) {
						signal = Sysml2CapellaUtils.getSignal(target.eContainer());
					}
				}

				for (FunctionPort targetCapPort : targetCapPorts) {
					for (FunctionPort sourceCapPort : sourceCapPorts) {
						String suffix = "";
						if (hasManyFE) {
							suffix = targetCapPort.getName() + "_" + sourceCapPort.getName();
						}
						if (targetCapPort != null && sourceCapPort != null
								&& isLinkable(source, target, sourceCapPort, targetCapPort)) {
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

							if (signal != null) {
								ExchangeItem ei = (ExchangeItem) MappingRulesManager
										.getCapellaObjectFromAllRules(signal);
								exchange.getExchangedItems().add(ei);

								ConnectorMapping connectorMapping = (ConnectorMapping) MappingRulesManager
										.getRule(ConnectorMapping.class.getName());
								Map<Connector, InformationFlow> map = connectorMapping.getMapSignalConnector()
										.get(signal);
								if (map != null) {

									Set<Entry<Connector, InformationFlow>> entrySet = map.entrySet();
									for (Entry<Connector, InformationFlow> entry : entrySet) {

										Connector connector = entry.getKey();
										InformationFlow informationFlow = entry.getValue();

										ComponentExchange ce = (ComponentExchange) MappingRulesManager
												.getCapellaObjectFromAllRules(connector);
										if (ce != null) {
											EList<ComponentExchangeFunctionalExchangeAllocation> ceAllocation = ce
													.getOwnedComponentExchangeFunctionalExchangeAllocations();

											ComponentExchangeFunctionalExchangeAllocation allocation = FaFactory.eINSTANCE
													.createComponentExchangeFunctionalExchangeAllocation();

											allocation.setSourceElement(ce);
											allocation.setTargetElement(exchange);

											Port sourcePort = ComponentExchangeExt.getSourcePort(ce);
											allocatePorts(sourcePort, sourceCapPort, targetCapPort, eResource,
													informationFlow, edge, signal, "_PORTSOURCEALLOCATION");

											Port targetPort = ComponentExchangeExt.getTargetPort(ce);

											allocatePorts(targetPort, sourceCapPort, targetCapPort, eResource,
													informationFlow, edge, signal, "_PORTTARGETALLOCATION");

											ceAllocation.add(allocation);

											Sysml2CapellaUtils.trace(this, eResource,
													Sysml2CapellaUtils.getSysMLID(eResource, informationFlow)
															+ Sysml2CapellaUtils.getSysMLID(eResource, edge)
															+ Sysml2CapellaUtils.getSysMLID(eResource, signal),
													allocation, "_ALLOCATION");
										}
									}
								}
							}

							String prefix = "";
							Class class1 = _mapActivityNodeToClass.get(sourceCapPort.eContainer());
							if (class1 != null) {
								prefix += Sysml2CapellaUtils.getSysMLID(eResource, class1);
							}
							Class class2 = _mapActivityNodeToClass.get(targetCapPort.eContainer());
							if (class2 != null) {
								prefix += Sysml2CapellaUtils.getSysMLID(eResource, class2);
							}
							Sysml2CapellaUtils.trace(this, eResource, objecFlow, exchange,
									"FunctionalExchange_" + suffix + prefix);
						}
					}
				}
			}
		}
	}

	private void allocatePorts(Port port, FunctionPort sourceCapPort, FunctionPort targetCapPort, Resource eResource,
			InformationFlow informationFlow, ActivityEdge edge, Signal signal, String prefix) {

		PortAllocation portAllocSource = InformationFactory.eINSTANCE.createPortAllocation();
		portAllocSource.setSourceElement(port);
		LogicalComponent block = (LogicalComponent) port.eContainer();
		if (containsFunctionalExchange(block, (LogicalFunction) sourceCapPort.eContainer())) {
			portAllocSource.setTargetElement(sourceCapPort);
		} else if (containsFunctionalExchange(block, (LogicalFunction) targetCapPort.eContainer())) {
			portAllocSource.setTargetElement(targetCapPort);
		}

		port.getOwnedPortAllocations().add(portAllocSource);
		Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, informationFlow)
				+ Sysml2CapellaUtils.getSysMLID(eResource, edge) + Sysml2CapellaUtils.getSysMLID(eResource, signal),
				portAllocSource, prefix);
	}

	private boolean containsFunctionalExchange(Component component, LogicalFunction function) {
		EList<ComponentFunctionalAllocation> ownedFunctionalAllocation = component.getOwnedFunctionalAllocation();
		for (ComponentFunctionalAllocation componentFunctionalAllocation : ownedFunctionalAllocation) {
			if (componentFunctionalAllocation.getFunction().equals(function)) {
				return true;
			}
		}
		return false;
	}

	private boolean isLinkable(ActivityNode source, ActivityNode target, FunctionPort sourceCapella,
			FunctionPort targetCapella) {
		FunctionalArchitectureMapping ruleFunctArch = (FunctionalArchitectureMapping) MappingRulesManager
				.getRule(FunctionalArchitectureMapping.class.getName());
		Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses = ruleFunctArch
				.getMapAbstractionToActivityToClasses();

		ActivityNode fSource = null;
		Class sourceParent = null;
		if (source instanceof CallBehaviorAction) {
			fSource = (CallBehaviorAction) source;
		} else if (source instanceof Pin) {
			fSource = (ActivityNode) source.eContainer();
		}
		if (fSource != null) {
			Behavior sourceBehavior = null;
			if (fSource instanceof CallBehaviorAction) {
				sourceBehavior = ((CallBehaviorAction) fSource).getBehavior();
			}
			List<Class> listSource = null;
			for (Entry<Abstraction, Map<Element, List<Class>>> entry : mapAbstractionToActivityToClasses.entrySet()) {
				Map<Element, List<Class>> mapActivityToClasses = entry.getValue();

				if (sourceBehavior != null) {
					listSource = mapActivityToClasses.get(sourceBehavior);
					if (listSource != null) {
						break;
					}

				}
			}

			if (listSource != null && !listSource.isEmpty()) {
				sourceParent = listSource.get(0);
			}
		}
		ActivityNode fTarget = null;
		Class targetParent = null;
		if (target instanceof CallBehaviorAction) {
			fTarget = (CallBehaviorAction) source;
		} else if (target instanceof Pin) {
			fTarget = (ActivityNode) target.eContainer();
		}
		if (fTarget != null) {
			Behavior targetBehavior = null;
			if (fTarget instanceof CallBehaviorAction) {
				targetBehavior = ((CallBehaviorAction) fTarget).getBehavior();
			}
			List<Class> listTarget = null;
			for (Entry<Abstraction, Map<Element, List<Class>>> entry : mapAbstractionToActivityToClasses.entrySet()) {
				Map<Element, List<Class>> mapActivityToClasses = entry.getValue();

				if (targetBehavior != null) {
					listTarget = mapActivityToClasses.get(targetBehavior);
					if (listTarget != null) {
						break;
					}

				}
			}
			if (listTarget != null && !listTarget.isEmpty()) {
				targetParent = listTarget.get(0);
			}
		}
		if (sourceParent != null && targetParent != null && sourceParent.equals(targetParent)) {
			AbstractFunctionalBlock blockSource = null;
			LogicalFunction lfSource = (LogicalFunction) sourceCapella.eContainer();
			EList<ComponentFunctionalAllocation> componentFunctionalAllocationsSource = lfSource
					.getComponentFunctionalAllocations();
			for (ComponentFunctionalAllocation componentFunctionalAllocation : componentFunctionalAllocationsSource) {
				blockSource = componentFunctionalAllocation.getBlock();
			}

			AbstractFunctionalBlock blockTarget = null;
			LogicalFunction lfTarget = (LogicalFunction) targetCapella.eContainer();
			EList<ComponentFunctionalAllocation> componentFunctionalAllocationsTarget = lfTarget
					.getComponentFunctionalAllocations();
			for (ComponentFunctionalAllocation componentFunctionalAllocation : componentFunctionalAllocationsTarget) {
				blockTarget = componentFunctionalAllocation.getBlock();
			}
			if (!blockSource.equals(blockTarget)) {
				return false;
			}

		}
		return true;
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
			List<FunctionPort> mergeResults = manageMergeOrForkNode(objecFlow);
			return mergeResults;

		} else if (source instanceof ForkNode) {
			List<FunctionPort> forkResults = manageMergeOrForkNode(objecFlow);
			return forkResults;

		}

		return Collections.emptyList();
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
	private List<FunctionPort> manageMergeOrForkNode(ObjectFlow objecFlow) {
		List<FunctionPort> mergeResults = new ArrayList<>();
		Object capellaObject = MappingRulesManager.getCapellaObjectFromAllRules(objecFlow);
		if (capellaObject instanceof FunctionPort) {
			mergeResults.add((FunctionPort) capellaObject);
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
			List<FunctionPort> capellaFunctionPort = getCapellaFunctionPort(eResource, parameter, mapPinToParam,
					source instanceof InputPin, true, objecFlow);
			if (capellaFunctionPort != null && !capellaFunctionPort.isEmpty()) {
				return capellaFunctionPort;
			}
		}

		EObject sourcePortParent = source.eContainer();
		if (!getAlgo().isEventOption()) {
			LogicalFunctionMapping functionRule = (LogicalFunctionMapping) MappingRulesManager
					.getRule(LogicalFunctionMapping.class.getName());
			Map<Signal, List<ActivityNode>> mapSignalActivityNode = functionRule.getMapSignalActivityNode();
			if (sourcePortParent instanceof SendSignalAction) {
				Signal signal = Sysml2CapellaUtils.getSignal((SendSignalAction) sourcePortParent);
				List<ActivityNode> list = mapSignalActivityNode.get(signal);
				for (ActivityNode activityNode : list) {
					if (activityNode instanceof AcceptEventAction) {
						EList<OutputPin> results = ((AcceptEventAction) activityNode).getResults();
						List<FunctionPort> ports = new ArrayList<>();
						for (OutputPin outputPin : results) {
							EList<ActivityEdge> incomings = outputPin.getOutgoings();
							for (ActivityEdge activityEdge : incomings) {
								if (activityEdge instanceof ObjectFlow) {
									ActivityNode source2 = activityEdge.getSource();
									ActivityNode target2 = activityEdge.getTarget();
									if (!source2.equals(outputPin)) {
										ports.addAll(getCapellaFunctionPort(eResource, source2, mapPinToParam, true,
												true, objecFlow));
									}
									if (!target2.equals(outputPin)) {
										ports.addAll(getCapellaFunctionPort(eResource, target2, mapPinToParam, true,
												true, objecFlow));
									}

								}
							}
						}
						return ports;
					}
				}
			}
		}
		AbstractMapping rule = MappingRulesManager.getRule(LogicalFunctionPortMapping.class.getName()
				+ Sysml2CapellaUtils.getSysMLID(eResource, sourcePortParent));
		if (rule == null) {
			return Collections.emptyList();
		}
		List<FunctionPort> ports = new ArrayList<>();
		Object object = rule.getMapSourceToTarget().get(source);
		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			for (Object object2 : list) {
				ports.add((FunctionPort) object2);
			}
		} else {
			ports.add((FunctionPort) object);
		}

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

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
