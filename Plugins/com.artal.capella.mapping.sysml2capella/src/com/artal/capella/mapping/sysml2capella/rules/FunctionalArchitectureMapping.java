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

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.capella.integration.util.CapellaUtil;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.ActivityParameterNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Parameter;
import org.eclipse.uml2.uml.ParameterDirectionKind;
import org.polarsys.capella.core.data.cs.CsFactory;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.ComponentFunctionalAllocation;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.fa.FunctionPort;
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
		LogicalFunction logicalFunctionRoot = Sysml2CapellaUtils.getLogicalFunctionRoot(targetScope.getProject());
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

			LogicalFunctionMapping functionMapping = new LogicalFunctionMapping(getAlgo(), activity, _mappingExecution);
			_manager.add(functionMapping.getClass().getName(), functionMapping);

			_manager.executeRules();

			FunctionalExchangeMapping functionExMapping = new FunctionalExchangeMapping(getAlgo(), activity,
					_mappingExecution);
			_manager.add(functionExMapping.getClass().getName(), functionExMapping);

			_manager.executeRules();
		}
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
