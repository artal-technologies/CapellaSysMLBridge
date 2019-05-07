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
import org.polarsys.capella.core.data.fa.AbstractFunction;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionKind;
import org.polarsys.capella.core.data.fa.FunctionPort;
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
		Map<Abstraction, Map<Activity, List<Class>>> mapAbstractionToActivityToClasses = rule
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
			Map<Abstraction, Map<Activity, List<Class>>> mapAbstractionToActivityToClasses, String suffix) {
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
				// if activityNode has children, transform these.
				Behavior behavior = ((CallBehaviorAction) activityNode).getBehavior();
				String lfName = activityNode.getName();
				if (behavior == null) {
					
					createLogicalFunction(eResource, logicalFunctionRoot, activityNode, lfName, suffix);
				} else {
					List<Class> list = null;
					Abstraction abstraction = null;
					AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
					for (Entry<Abstraction, Map<Activity, List<Class>>> entry : mapAbstractionToActivityToClasses
							.entrySet()) {
						abstraction = entry.getKey();
						Map<Activity, List<Class>> mapActivityToClasses = entry.getValue();

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
								Map<Class, List<Class>> mapGeneralizations = ((ComponentMapping) rule)
										.getMapGeneralizations();
								List<Class> listGeneralizations = mapGeneralizations.get(class1);
								if (listGeneralizations != null && !listGeneralizations.isEmpty()) {
									for (Class class2 : listGeneralizations) {
										transformPort = manageLogicalFunction(eResource, logicalFunctionRoot,
												mapPinToParam, mapAbstractionToActivityToClasses, activityNode,
												transformPort, abstraction, behavior, lfName,
												rule.getMapSourceToTarget().get(class2), class2);

									}
									transformPort = manageLogicalFunction(eResource, logicalFunctionRoot,
											mapPinToParam, mapAbstractionToActivityToClasses, activityNode,
											transformPort, abstraction, behavior, lfName, object, class1);
								} else {
									transformPort = manageLogicalFunction(eResource, logicalFunctionRoot,
											mapPinToParam, mapAbstractionToActivityToClasses, activityNode,
											transformPort, abstraction, behavior, lfName, object, class1);
								}

							}
						}

					} else {
						transformPort = manageLogicalFunction(eResource, logicalFunctionRoot, mapPinToParam,
								mapAbstractionToActivityToClasses, activityNode, transformPort, abstraction, behavior,
								lfName, null, null);
					}
				}
				// transform the ports of the leaf activity.
				if (transformPort) {
					LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
							(CallBehaviorAction) activityNode, _mappingExecution);
					_manager.add(functionPortMapping.getClass().getName()
							+ Sysml2CapellaUtils.getSysMLID(eResource, activityNode), functionPortMapping);

				}
			}
			if (activityNode instanceof MergeNode) {
				if (hasObjectFlow(activityNode)) {
					FunctionKind functionKing = FunctionKind.GATHER;
					LogicalFunction lf = createNodeFunction(activityNode, functionKing, logicalFunctionRoot);

					Sysml2CapellaUtils.trace(this, eResource, activityNode, lf, "GATHER_");

					managePartitions(activityNode, eResource, lf, FunctionKind.GATHER, logicalFunctionRoot);

					LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
							(MergeNode) activityNode, _mappingExecution);
					_manager.add(functionPortMapping.getClass().getName()
							+ Sysml2CapellaUtils.getSysMLID(eResource, activityNode), functionPortMapping);
				}
			}
			if (activityNode instanceof ForkNode) {
				if (hasObjectFlow(activityNode)) {
					FunctionKind functionKing = FunctionKind.SPLIT;
					LogicalFunction lf = createNodeFunction(activityNode, functionKing, logicalFunctionRoot);

					Sysml2CapellaUtils.trace(this, eResource, activityNode, lf, "SPLIT_");

					managePartitions(activityNode, eResource, lf, FunctionKind.SPLIT, logicalFunctionRoot);

					LogicalFunctionPortMapping functionPortMapping = new LogicalFunctionPortMapping(getAlgo(),
							(ForkNode) activityNode, _mappingExecution);
					_manager.add(functionPortMapping.getClass().getName()
							+ Sysml2CapellaUtils.getSysMLID(eResource, activityNode), functionPortMapping);
				}
			}

		}
		return hasChild;

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
		lf.setName(activityNode.getName());
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
			Map<Abstraction, Map<Activity, List<Class>>> mapAbstractionToActivityToClasses, ActivityNode activityNode,
			boolean transformPort, Abstraction abstraction, Behavior behavior, String lfName, Object object,
			Class parentClass) {
		String sysMLID = "";
		if (parentClass != null) {
			sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, parentClass);
		}
		LogicalFunction lFunction = createLogicalFunction(eResource, logicalFunctionRoot, activityNode, lfName,
				sysMLID);

		_mapActivityNodeToClass.put(lFunction, parentClass);

		if (object != null) {
			createFunctionAllocation(activityNode, eResource, lFunction, "FunctionAllocation_" + sysMLID, abstraction,
					object);
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
		Sysml2CapellaUtils.trace(this, eResource,
				Sysml2CapellaUtils.getSysMLID(eResource, activityPartition)
						+ Sysml2CapellaUtils.getSysMLID(eResource, activityNode),
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
