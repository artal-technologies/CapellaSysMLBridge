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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.ActivityPartition;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.Pin;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionKind;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalComponent;
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

	Map<LogicalFunction, Class> _mapActivityNodeToClass = new HashMap<>();

	Map<Signal, List<ActivityNode>> _mapSignalActivityNode = new HashMap<>();

	/**
	 * Manage sub rules
	 */
	MappingRulesManager _manager = new MappingRulesManager();
	private Map<Pin, ActivityParameterNode> _mapPinToParam;

	static public Map<Activity, List<CallBehaviorAction>> _mapActivityToCallBehaviors = new HashMap<Activity, List<CallBehaviorAction>>();

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
		LogicalFunction logicalFunctionRoot = Sysml2CapellaUtils.getLogicalFunctionRoot(targetScope.getProject());
		FunctionalArchitectureMapping rule = (FunctionalArchitectureMapping) MappingRulesManager
				.getRule(FunctionalArchitectureMapping.class.getName());
		Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses = rule
				.getMapAbstractionToActivityToClasses();
		_mapPinToParam = new HashMap<>();
		transformCallBehavior(eResource, logicalFunctionRoot, _source, _mapPinToParam,
				mapAbstractionToActivityToClasses, "");
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
	 * @param mapActivityToClasses
	 * @return a {@link Boolean} true if the source <code>activity</code> are
	 *         children.
	 */
	private boolean transformCallBehavior(Resource eResource, LogicalFunction logicalFunctionRoot, Activity activity,
			Map<Pin, ActivityParameterNode> mapPinToParam,
			Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses, String suffix) {
		EList<ActivityNode> nodes = activity.getNodes();

		List<CallBehaviorAction> subCallBehav = getSubBehavior(nodes);

		boolean hasChild = checkThereIsSubCallBehavior(nodes, subCallBehav);

		fillMapPinToParam(mapPinToParam, nodes, hasChild);

		// browse all the CallBehavior nodes
		for (ActivityNode activityNode : nodes) {
			if (activityNode instanceof CallBehaviorAction) {
				manageCallBehavior(eResource, logicalFunctionRoot, mapPinToParam, mapAbstractionToActivityToClasses,
						suffix, activityNode);
			}
			if (activityNode instanceof AcceptEventAction) {
				manageAcceptEventAction(eResource, logicalFunctionRoot, suffix, activityNode);
			}
			if (activityNode instanceof SendSignalAction) {
				manageSendEventAction(eResource, logicalFunctionRoot, suffix, activityNode);
			}
			if (activityNode instanceof MergeNode) {
				manageMergeNode(eResource, logicalFunctionRoot, activityNode, mapAbstractionToActivityToClasses);
			}
			if (activityNode instanceof ForkNode) {
				manageForkNode(eResource, logicalFunctionRoot, activityNode, mapAbstractionToActivityToClasses);
			}

		}
		return hasChild;

	}

	/**
	 * @param mapPinToParam
	 * @param nodes
	 * @param hasChild
	 */
	private void fillMapPinToParam(Map<Pin, ActivityParameterNode> mapPinToParam, EList<ActivityNode> nodes,
			boolean hasChild) {
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
	}

	/**
	 * @param nodes
	 * @return
	 */
	private List<CallBehaviorAction> getSubBehavior(EList<ActivityNode> nodes) {
		List<CallBehaviorAction> subCallBehav = new ArrayList<>();
		for (ActivityNode activityNode : nodes) {
			if (activityNode instanceof CallBehaviorAction) {
				subCallBehav.add((CallBehaviorAction) activityNode);
			}
		}
		return subCallBehav;
	}

	/**
	 * @param nodes
	 * @param hasChild
	 * @param subCallBehav
	 * @return
	 */
	private boolean checkThereIsSubCallBehavior(EList<ActivityNode> nodes, List<CallBehaviorAction> subCallBehav) {
		boolean hasChild = false;
		for (ActivityNode activityNode : nodes) {

			if (activityNode instanceof ActivityParameterNode) {
				ActivityParameterNode an = (ActivityParameterNode) activityNode;
				List<ActivityEdge> e = new ArrayList<ActivityEdge>();
				e.addAll(an.getIncomings());
				e.addAll(an.getOutgoings());
				for (ActivityEdge activityEdge : e) {
					if (activityEdge instanceof ObjectFlow) {
						ActivityNode s = activityEdge.getSource();
						ActivityNode t = activityEdge.getTarget();
						if (s.equals(an)) {
							if (subCallBehav.contains(t) || subCallBehav.contains(t.eContainer())) {
								hasChild = true;
								break;
							}
						}
						if (t.equals(an)) {
							if (subCallBehav.contains(s) || subCallBehav.contains(s.eContainer())) {
								hasChild = true;
								break;
							}
						}
					}
				}
			}

		}
		return hasChild;
	}

	/**
	 * @param eResource
	 * @param logicalFunctionRoot
	 * @param activityNode
	 */
	private void manageForkNode(Resource eResource, LogicalFunction logicalFunctionRoot, ActivityNode activityNode,
			Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses) {
		if (hasObjectFlow(activityNode)) {
			FunctionKind functionKing = FunctionKind.SPLIT;
			LogicalFunction lf = createNodeFunction(activityNode, functionKing, logicalFunctionRoot);

			Sysml2CapellaUtils.trace(this, eResource, activityNode, lf, "SPLIT_");

			// managePartitions(activityNode, eResource, lf, FunctionKind.SPLIT,
			// logicalFunctionRoot);
			manageGeneralization(eResource, activityNode, mapAbstractionToActivityToClasses, lf, "SplitAllocation_");
			LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
					(ForkNode) activityNode, _mappingExecution);
			_manager.add(
					functionPortMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, activityNode),
					functionPortMapping);
		}
	}

	/**
	 * @param eResource
	 * @param logicalFunctionRoot
	 * @param activityNode
	 * @param mapAbstractionToActivityToClasses
	 */
	private void manageMergeNode(Resource eResource, LogicalFunction logicalFunctionRoot, ActivityNode activityNode,
			Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses) {
		if (hasObjectFlow(activityNode)) {
			FunctionKind functionKing = FunctionKind.GATHER;
			LogicalFunction lf = createNodeFunction(activityNode, functionKing, logicalFunctionRoot);

			Sysml2CapellaUtils.trace(this, eResource, activityNode, lf, "GATHER_");

			// managePartitions(activityNode, eResource, lf,
			// FunctionKind.GATHER, logicalFunctionRoot);
			manageGeneralization(eResource, activityNode, mapAbstractionToActivityToClasses, lf, "GatherAllocation_");

			LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
					(MergeNode) activityNode, _mappingExecution);
			_manager.add(
					functionPortMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, activityNode),
					functionPortMapping);
		}
	}

	/**
	 * @param eResource
	 * @param activityNode
	 * @param mapAbstractionToActivityToClasses
	 * @param lf
	 */
	private void manageGeneralization(Resource eResource, ActivityNode activityNode,
			Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses, LogicalFunction lf,
			String prefix) {
		List<Class> list = null;
		Abstraction abstraction = null;
		AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
		for (Entry<Abstraction, Map<Element, List<Class>>> entry : mapAbstractionToActivityToClasses.entrySet()) {
			abstraction = entry.getKey();
			Map<Element, List<Class>> mapActivityToClasses = entry.getValue();

			list = mapActivityToClasses.get(activityNode);
			if (list != null) {
				break;
			}

		}
		if (list != null) {
			for (Class class1 : list) {

				Object object = rule.getMapSourceToTarget().get(class1);
				if (object instanceof LogicalComponent) {
					Map<Class, List<Class>> mapGeneralizations = ((ComponentMapping) rule).getMapGeneralizations();
					List<Class> listGeneralizations = mapGeneralizations.get(class1);
					if (listGeneralizations != null && !listGeneralizations.isEmpty()) {
						for (Class class2 : listGeneralizations) {
							createFunctionAllocation(activityNode, eResource, lf, prefix, abstraction,
									rule.getMapSourceToTarget().get(class2));

						}
						createFunctionAllocation(activityNode, eResource, lf, prefix, abstraction, object);
					} else {
						createFunctionAllocation(activityNode, eResource, lf, prefix, abstraction, object);
					}

				}
			}

		}
	}

	/**
	 * @param eResource
	 * @param logicalFunctionRoot
	 * @param suffix
	 * @param activityNode
	 */
	private void manageSendEventAction(Resource eResource, LogicalFunction logicalFunctionRoot, String suffix,
			ActivityNode activityNode) {
		Signal signal = Sysml2CapellaUtils.getSignal(activityNode);
		String lfName = "";
		if (signal != null) {
			lfName = signal.getName();
		}
		fillSignalActivityNodeMap(activityNode, signal);
		if (getAlgo().isEventOption()) {
			LogicalFunction lf = createLogicalFunction(eResource, logicalFunctionRoot, activityNode, lfName, suffix);
			managePartitions(activityNode, eResource, lf, null, logicalFunctionRoot);

			FunctionOutputPort input = FaFactory.eINSTANCE.createFunctionOutputPort();
			lf.getOutputs().add(input);
			Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, activityNode)
					+ Sysml2CapellaUtils.getSysMLID(eResource, signal), input, "_ApplyEvent");

			FunctionInputPort out = FaFactory.eINSTANCE.createFunctionInputPort();
			FunctionalArchitectureMapping rule = (FunctionalArchitectureMapping) MappingRulesManager
					.getRule(FunctionalArchitectureMapping.class.getName());
			Model source = rule.getSource();
			List<?> objects = (List<?>) rule.getMapSourceToTarget().get(source);
			LogicalFunction env = null;
			for (Object object : objects) {
				if (object instanceof LogicalFunction) {
					env = ((LogicalFunction) object);
					env.getInputs().add(out);
					break;
				}
			}
			Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, activityNode)
					+ Sysml2CapellaUtils.getSysMLID(eResource, signal), out, "_SendEvent");

			FunctionalExchange fe = manageFunctionExchange(eResource, activityNode, signal, input, out, env);

			manageSignal(eResource, activityNode, signal, fe);

			LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
					(Action) activityNode, _mappingExecution);
			_manager.add(
					functionPortMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, activityNode),
					functionPortMapping);
		}
	}

	/**
	 * @param activityNode
	 * @param signal
	 */
	private void fillSignalActivityNodeMap(ActivityNode activityNode, Signal signal) {
		List<ActivityNode> list = _mapSignalActivityNode.get(signal);
		if (list == null) {
			list = new ArrayList<>();
			_mapSignalActivityNode.put(signal, list);
		}
		list.add(activityNode);
	}

	public Map<Signal, List<ActivityNode>> getMapSignalActivityNode() {
		return _mapSignalActivityNode;
	}

	/**
	 * @param signal
	 * @param input
	 * @param out
	 * @param env
	 * @return
	 */
	private FunctionalExchange manageFunctionExchange(Resource eResource, ActivityNode activityNode, Signal signal,
			FunctionOutputPort input, FunctionInputPort out, LogicalFunction env) {
		FunctionalExchange fe = FaFactory.eINSTANCE.createFunctionalExchange();
		fe.setName(signal.getName());
		fe.setSource(input);
		fe.setTarget(out);
		LogicalFunction root = (LogicalFunction) env.eContainer();
		root.getOwnedFunctionalExchanges().add(fe);

		Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, activityNode)
				+ Sysml2CapellaUtils.getSysMLID(eResource, signal), fe, "_ExchangeEvent");

		return fe;
	}

	/**
	 * @param eResource
	 * @param activityNode
	 * @param signal
	 * @param fe
	 */
	private void manageSignal(Resource eResource, ActivityNode activityNode, Signal signal, FunctionalExchange fe) {
		SignalMapping signalRule = (SignalMapping) MappingRulesManager.getRule(SignalMapping.class.getName());
		ExchangeItem ei = (ExchangeItem) signalRule.getMapSourceToTarget().get(signal);
		if (ei != null) {
			fe.getExchangedItems().add(ei);
		}
	}

	/**
	 * @param eResource
	 * @param logicalFunctionRoot
	 * @param suffix
	 * @param activityNode
	 */
	private void manageAcceptEventAction(Resource eResource, LogicalFunction logicalFunctionRoot, String suffix,
			ActivityNode activityNode) {
		Signal signal = Sysml2CapellaUtils.getSignal(activityNode);
		String lfName = "";
		if (signal != null) {
			lfName = signal.getName();
		}
		fillSignalActivityNodeMap(activityNode, signal);
		if (signal != null) {
			if (getAlgo().isEventOption()) {
				LogicalFunction lf = createLogicalFunction(eResource, logicalFunctionRoot, activityNode, lfName,
						suffix);
				FunctionInputPort input = FaFactory.eINSTANCE.createFunctionInputPort();
				lf.getInputs().add(input);
				Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, activityNode)
						+ Sysml2CapellaUtils.getSysMLID(eResource, signal), input, "_ApplyEvent");

				FunctionOutputPort output = FaFactory.eINSTANCE.createFunctionOutputPort();
				FunctionalArchitectureMapping rule = (FunctionalArchitectureMapping) MappingRulesManager
						.getRule(FunctionalArchitectureMapping.class.getName());
				Model source = rule.getSource();
				List<?> objects = (List<?>) rule.getMapSourceToTarget().get(source);
				LogicalFunction env = null;
				for (Object object : objects) {
					if (object instanceof LogicalFunction) {
						env = ((LogicalFunction) object);
						env.getOutputs().add(output);
						break;
					}
				}
				Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, activityNode)
						+ Sysml2CapellaUtils.getSysMLID(eResource, signal), output, "_SendEvent");

				FunctionalExchange fe = manageFunctionExchange(eResource, activityNode, signal, output, input, env);
				manageSignal(eResource, activityNode, signal, fe);

				LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
						(Action) activityNode, _mappingExecution);
				_manager.add(functionPortMapping.getClass().getName()
						+ Sysml2CapellaUtils.getSysMLID(eResource, activityNode), functionPortMapping);
			}
		}
	}

	/**
	 * @param eResource
	 * @param logicalFunctionRoot
	 * @param mapPinToParam
	 * @param mapAbstractionToActivityToClasses
	 * @param suffix
	 * @param activityNode
	 */
	private void manageCallBehavior(Resource eResource, LogicalFunction logicalFunctionRoot,
			Map<Pin, ActivityParameterNode> mapPinToParam,
			Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses, String suffix,
			ActivityNode activityNode) {
		// boolean should be true for transformed activity ports.
		boolean transformPort = true;
		// if activityNode has children, transform these.
		Behavior behavior = ((CallBehaviorAction) activityNode).getBehavior();
		String lfName = activityNode.getName();
		if (behavior == null) {

			createLogicalFunction(eResource, logicalFunctionRoot, activityNode, lfName, suffix);
		} else {
			if (lfName == null || lfName.isEmpty()) {
				lfName = behavior.getName();
			}
			List<Class> list = null;
			Abstraction abstraction = null;
			AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
			for (Entry<Abstraction, Map<Element, List<Class>>> entry : mapAbstractionToActivityToClasses.entrySet()) {
				abstraction = entry.getKey();
				Map<Element, List<Class>> mapActivityToClasses = entry.getValue();

				if (behavior != null) {
					list = mapActivityToClasses.get(behavior);
					lfName = behavior.getName();
					if (list != null) {
						break;
					}

				}
			}
			if (list != null) {
				for (Class class1 : list) {

					Object object = rule.getMapSourceToTarget().get(class1);
					if (object instanceof LogicalComponent) {
						Map<Class, List<Class>> mapGeneralizations = ((ComponentMapping) rule).getMapGeneralizations();
						List<Class> listGeneralizations = mapGeneralizations.get(class1);
						if (listGeneralizations != null && !listGeneralizations.isEmpty()) {
							for (Class class2 : listGeneralizations) {
								transformPort = manageLogicalFunction(eResource, logicalFunctionRoot, mapPinToParam,
										mapAbstractionToActivityToClasses, activityNode, transformPort, abstraction,
										behavior, lfName, rule.getMapSourceToTarget().get(class2), class2, false);

							}
							transformPort = manageLogicalFunction(eResource, logicalFunctionRoot, mapPinToParam,
									mapAbstractionToActivityToClasses, activityNode, transformPort, abstraction,
									behavior, lfName, object, class1, false);
						} else {
							transformPort = manageLogicalFunction(eResource, logicalFunctionRoot, mapPinToParam,
									mapAbstractionToActivityToClasses, activityNode, transformPort, abstraction,
									behavior, lfName, object, class1, false);
						}

					}
				}

			} else {
				transformPort = manageLogicalFunction(eResource, logicalFunctionRoot, mapPinToParam,
						mapAbstractionToActivityToClasses, activityNode, transformPort, abstraction, behavior, lfName,
						null, null, true);
			}
		}
		// transform the ports of the leaf activity.
		if (transformPort) {
			LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
					(CallBehaviorAction) activityNode, _mappingExecution);
			_manager.add(
					functionPortMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, activityNode),
					functionPortMapping);

		}
	}

	private boolean hasObjectFlow(ActivityNode activityNode) {

		List<ActivityEdge> activityEdges = new ArrayList<>();

		activityEdges.addAll(activityNode.getIncomings());
		activityEdges.addAll(activityNode.getOutgoings());
		for (ActivityEdge activityEdge : activityEdges) {
			if (activityEdge instanceof ObjectFlow) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param activityNode
	 * @param functionKing
	 * @return
	 */
	private LogicalFunction createNodeFunction(ActivityNode activityNode, FunctionKind functionKing,
			LogicalFunction logicalFunctionRoot) {
		LogicalFunction lf = LaFactory.eINSTANCE.createLogicalFunction();
		lf.setKind(functionKing);
		String name = activityNode.getName();
		if (name == null || name.isEmpty()) {
			System.out.println();
		}
		lf.setName(name);
		EList<AbstractFunction> ownedFunctions = logicalFunctionRoot.getOwnedFunctions();
		ownedFunctions.add(lf);
		return lf;
	}

	/**
	 * @param eResource
	 * @param logicalFunctionRoot
	 * @param mapPinToParam
	 * @param mapAbstractionToActivityToClasses
	 * @param activityNode
	 * @param transformPort
	 * @param abstraction
	 * @param behavior
	 * @param lfName
	 * @param object
	 * @return
	 */
	private boolean manageLogicalFunction(Resource eResource, LogicalFunction logicalFunctionRoot,
			Map<Pin, ActivityParameterNode> mapPinToParam,
			Map<Abstraction, Map<Element, List<Class>>> mapAbstractionToActivityToClasses, ActivityNode activityNode,
			boolean transformPort, Abstraction abstraction, Behavior behavior, String lfName, Object object,
			Class parentClass, boolean checkPartition) {
		String sysMLID = "";
		if (parentClass != null) {
			sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, parentClass);
		}
		LogicalFunction lFunction = createLogicalFunction(eResource, logicalFunctionRoot, activityNode, lfName,
				sysMLID);

		_mapActivityNodeToClass.put(lFunction, parentClass);

		if (object != null && !checkPartition) {
			createFunctionAllocation(activityNode, eResource, lFunction, "FunctionAllocation_" + sysMLID, abstraction,
					object);
		} else {
			managePartitions(activityNode, eResource, lFunction, null, logicalFunctionRoot);
		}
		// put activity and this linked
		// callBehaviorAction
		putActivityToCallBehaviorActions(activityNode, behavior);

		if (behavior.getName() != null && !behavior.getName().isEmpty()) {
			lfName = behavior.getName();
			// if behavior has not children, the
			// ports from
			// activity
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
			transformPort = !transformCallBehavior(eResource, lFunction, (Activity) behavior, map,
					mapAbstractionToActivityToClasses, sysMLID);

			mapPinToParam.putAll(map);
		}
		return transformPort;
	}

	/**
	 * @param eResource
	 * @param logicalFunctionRoot
	 * @param activityNode
	 * @param lfName
	 */
	private LogicalFunction createLogicalFunction(Resource eResource, LogicalFunction logicalFunctionRoot,
			ActivityNode activityNode, String lfName, String prefix) {
		LogicalFunction lFunction = LaFactory.eINSTANCE.createLogicalFunction();
		logicalFunctionRoot.getOwnedFunctions().add(lFunction);
		Sysml2CapellaUtils.trace(this, eResource, activityNode, lFunction, "LogicalFunction_" + prefix);
		lFunction.setName(lfName);
		return lFunction;
	}

	private void managePartitions(ActivityNode activityNode, Resource eResource, LogicalFunction nodeFunction,
			FunctionKind functionKing, LogicalFunction logicalFunctionRoot) {
		String prefix = "";
		if (functionKing == FunctionKind.GATHER) {
			prefix = "GatherAllocation_";
		} else if (functionKing == FunctionKind.SPLIT) {
			prefix = "SplitAllocation_";
		}
		EList<ActivityPartition> inPartitions = activityNode.getInPartitions();
		for (ActivityPartition activityPartition : inPartitions) {
			Element represents = activityPartition.getRepresents();
			AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
			Object object = rule.getMapSourceToTarget().get(represents);
			if (object instanceof LogicalComponent) {
				createFunctionAllocation(activityNode, eResource, nodeFunction, prefix, activityPartition, object);
				if (represents instanceof Class) {
					Map<Class, List<Class>> mapGeneralizations = ((ComponentMapping) rule).getMapGeneralizations();
					List<Class> listGeneralizations = mapGeneralizations.get(represents);
					if (listGeneralizations != null && !listGeneralizations.isEmpty()) {
						for (Class class1 : listGeneralizations) {
							LogicalFunction createNodeFunction = createNodeFunction(activityNode, functionKing,
									logicalFunctionRoot);
							Object object2 = rule.getMapSourceToTarget().get(class1);
							createFunctionAllocation(activityNode, eResource, createNodeFunction, prefix,
									activityPartition, object2);
						}
					}

				}
			}
		}
	}

	/**
	 * @param activityNode
	 * @param eResource
	 * @param nodeFunction
	 * @param prefix
	 * @param activityPartition
	 * @param object
	 */
	private void createFunctionAllocation(ActivityNode activityNode, Resource eResource, LogicalFunction nodeFunction,
			String prefix, Element activityPartition, Object object) {
		ComponentFunctionalAllocation createComponentFunctionalAllocation = FaFactory.eINSTANCE
				.createComponentFunctionalAllocation();
		createComponentFunctionalAllocation.setTargetElement(nodeFunction);
		createComponentFunctionalAllocation.setSourceElement((LogicalComponent) object);
		((LogicalComponent) object).getOwnedFunctionalAllocation().add(createComponentFunctionalAllocation);
		Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, activityNode),
				createComponentFunctionalAllocation, prefix);
	}

	/**
	 * Put an {@link Activity} and this linked {@link CallBehaviorAction}
	 * 
	 * @param activityNode
	 *            the {@link CallBehaviorAction}
	 * @param behavior
	 *            the {@link Activity}
	 */
	private void putActivityToCallBehaviorActions(ActivityNode activityNode, Behavior behavior) {
		List<CallBehaviorAction> listCallBehaviors = _mapActivityToCallBehaviors.get(behavior);
		if (listCallBehaviors == null) {
			listCallBehaviors = new ArrayList<CallBehaviorAction>();
			_mapActivityToCallBehaviors.put((Activity) behavior, listCallBehaviors);
		}
		listCallBehaviors.add((CallBehaviorAction) activityNode);
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

	public static Map<Activity, List<CallBehaviorAction>> getMapActivityToCallBehaviors() {
		return _mapActivityToCallBehaviors;
	}

	public Map<LogicalFunction, Class> getMapActivityNodeToClass() {
		return _mapActivityNodeToClass;
	}

}
