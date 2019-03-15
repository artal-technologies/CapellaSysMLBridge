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
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.capella.integration.util.CapellaUtil;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.ObjectFlow;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.eclipse.uml2.uml.Pin;
import org.polarsys.capella.core.data.cs.CsFactory;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
import org.polarsys.capella.core.data.fa.FunctionalExchange;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalActor;
import org.polarsys.capella.core.data.la.LogicalActorPkg;
import org.polarsys.capella.core.data.la.LogicalContext;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class FunctionalArchitectureMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * Map to find Part for an Actor
	 */
	Map<LogicalActor, Part> _mapLAtoPart = new HashMap<LogicalActor, Part>();

	/**
	 * Manage sub rules
	 */
	MappingRulesManager _manager = new MappingRulesManager();

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Sysml2CapellaAlgo} algo.
	 * @param source
	 *            the {@link Model} sysml model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public FunctionalArchitectureMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		List<Activity> activities = Sysml2CapellaUtils.getActivities(_source, "02 Behavior/02 Functional Architecture");
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalFunction logicalFunctionRoot = CapellaUtil.getLogicalFunctionRoot(targetScope.getProject());
		LogicalActorPkg logicalActorPkg = Sysml2CapellaUtils.getLogicalActorPkg(targetScope.getProject());
		LogicalContext logicalContext = Sysml2CapellaUtils.getLogicalContext(targetScope.getProject());

		for (Activity activity : activities) {
			LogicalFunction evironnement = LaFactory.eINSTANCE.createLogicalFunction();
			evironnement.setName("Environment");
			LogicalActor genericActor = LaFactory.eINSTANCE.createLogicalActor();
			genericActor.setName("Generic Actor");
			Part partGenActor = CsFactory.eINSTANCE.createPart();
			partGenActor.setName(genericActor.getName());
			partGenActor.setAbstractType(genericActor);

			logicalActorPkg.getOwnedLogicalActors().add(genericActor);
			logicalContext.getOwnedFeatures().add(partGenActor);
			logicalFunctionRoot.getOwnedFunctions().add(evironnement);

			ComponentFunctionalAllocation cfa = FaFactory.eINSTANCE.createComponentFunctionalAllocation();
			cfa.setTargetElement(evironnement);
			cfa.setSourceElement(genericActor);

			genericActor.getOwnedFunctionalAllocation().add(cfa);

			Sysml2CapellaUtils.trace(this, eResource, activity, evironnement, "EnvFunction_");
			Sysml2CapellaUtils.trace(this, eResource, activity, genericActor, "GenericActor_");
			Sysml2CapellaUtils.trace(this, eResource, activity, partGenActor, "GenericActorPart_");
			Sysml2CapellaUtils.trace(this, eResource, activity, cfa, "GenericActorPartFunctionalAlloc_");

			EList<ActivityNode> nodes2 = activity.getNodes();
			EList<ActivityNode> nodes = nodes2;
			for (ActivityNode activityNode : nodes) {

				if (activityNode instanceof ActivityParameterNode) {
					ActivityParameterNode paramNode = (ActivityParameterNode) activityNode;
					Parameter parameter = paramNode.getParameter();
					ParameterDirectionKind direction = parameter.getDirection();
					FunctionPort envPort = null;

					// The logical function Environment represents the external
					// functions at the system. The port direction are inverted.
					if (direction == ParameterDirectionKind.IN_LITERAL) {
						envPort = FaFactory.eINSTANCE.createFunctionOutputPort();
						evironnement.getOutputs().add((FunctionOutputPort) envPort);
					} else {
						envPort = FaFactory.eINSTANCE.createFunctionInputPort();
						evironnement.getInputs().add((FunctionInputPort) envPort);
					}
					envPort.setName(paramNode.getName());

					Sysml2CapellaUtils.trace(this, eResource, paramNode, envPort, "EnvironmentPort_");

				}
			}

			HashMap<Pin, ActivityParameterNode> mapPinToParam = new HashMap<>();
			transformCallBehavior(eResource, logicalFunctionRoot, activity, mapPinToParam);
			_manager.executeRules();

			transformObjectFlow(eResource, activity, logicalFunctionRoot, mapPinToParam, true);

		}
		// _manager.executeRules();
	}

	private void transformObjectFlow(Resource eResource, Activity activity, LogicalFunction functionContainer,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean firstLevel) {
		EList<ActivityNode> nodes = activity.getNodes();
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
				FunctionPort sourceCapPort = getCapellaFunctionPort(eResource, source, mapPinToParam, true, false,
						objecFlow);
				FunctionPort targetCapPort = getCapellaFunctionPort(eResource, target, mapPinToParam, false, false,
						objecFlow);

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
					Sysml2CapellaUtils.trace(this, eResource, objecFlow, exchange, "FunctionalExchange_");
				}

			}
		}
		AbstractMapping rule = MappingRulesManager.getRule(FunctionalArchitectureMapping.class.getName());
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

	private FunctionPort getCapellaFunctionPort(Resource eResource, ActivityNode source,
			Map<Pin, ActivityParameterNode> mapPinToParam, boolean isSource, boolean transformParam,
			ObjectFlow objecFlow) {

		if (source instanceof Pin) {
			ActivityParameterNode parameter = mapPinToParam.get(source);
			if (parameter != null) {
				return getCapellaFunctionPort(eResource, parameter, mapPinToParam, source instanceof InputPin, true,
						objecFlow);
			}
			EObject sourcePortParent = source.eContainer();
			AbstractMapping rule = MappingRulesManager.getRule(LogicalFunctionPortMapping.class.getName()
					+ Sysml2CapellaUtils.getSysMLID(eResource, sourcePortParent));
			if (rule == null) {
				return null;
			}
			return (FunctionPort) rule.getMapSourceToTarget().get(source);
		} else if (source instanceof ActivityParameterNode) {
			EObject sourcePortParent = source.eContainer();
			if (sourcePortParent instanceof Activity
					&& ((Activity) sourcePortParent).getName().equals("02 Functional Architecture")) {
				AbstractMapping rule = MappingRulesManager.getRule(FunctionalArchitectureMapping.class.getName());
				return (FunctionPort) rule.getMapSourceToTarget().get(source);
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
		}
		// the association is not linked at a port.
		else if (source instanceof CallBehaviorAction) {
			Object object = getMapSourceToTarget().get(source);
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
			return port;

		}

		// EObject sourcePortParent = source.eContainer();
		// if (source instanceof ActivityParameterNode) {
		// AbstractMapping rule =
		// MappingRulesManager.getRule(LogicalFunctionMapping.class.getName());
		// return (FunctionPort) rule.getMapSourceToTarget().get(source);
		// }
		// if (sourcePortParent instanceof CallBehaviorAction) {
		// AbstractMapping rule =
		// MappingRulesManager.getRule(LogicalFunctionPortMapping.class.getName()
		// + Sysml2CapellaUtils.getSysMLID(eResource, sourcePortParent));
		// if (rule == null) {
		// return null;
		// }
		// return (FunctionPort) rule.getMapSourceToTarget().get(source);
		//
		// } else if (sourcePortParent instanceof Activity
		// && ((Activity) sourcePortParent).getName().equals("02 Functional
		// Architecture")) {
		// AbstractMapping rule =
		// MappingRulesManager.getRule(LogicalFunctionMapping.class.getName());
		// return (FunctionPort) rule.getMapSourceToTarget().get(source);
		// }
		return null;
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
