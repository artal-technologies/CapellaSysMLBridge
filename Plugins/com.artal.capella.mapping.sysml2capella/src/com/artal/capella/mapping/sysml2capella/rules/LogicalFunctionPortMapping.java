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
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.AcceptEventAction;
import org.eclipse.uml2.uml.Action;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ForkNode;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.MergeNode;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OutputPin;
import org.eclipse.uml2.uml.SendSignalAction;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class LogicalFunctionPortMapping extends AbstractMapping {
	/**
	 * The sysml root {@link Model}.
	 */
	ActivityNode _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;
	private Map<LogicalFunction, Class> _mapActivityNodeToClass;

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Sysml2CapellaAlgo} algo.
	 * @param source
	 *            the {@link CallBehaviorAction} sysml model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public LogicalFunctionPortMapping(Sysml2CapellaAlgo algo, ActivityNode source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
		_mappingExecution = mappingExecution;
	}

	@Override
	public void computeMapping() {
		Resource eResource = _source.eResource();
		AbstractMapping rule = MappingRulesManager.getRule(LogicalFunctionMapping.class.getName());
		_mapActivityNodeToClass = ((LogicalFunctionMapping) rule).getMapActivityNodeToClass();
		Object object = rule.getMapSourceToTarget().get(_source);
		if (object instanceof List<?>) {
			List<?> list = (List<?>) object;
			for (Object object2 : list) {
				if (object2 instanceof LogicalFunction) {
					Class class1 = _mapActivityNodeToClass.get(object2);
					String sysMLID = "";
					if (class1 != null) {
						sysMLID = Sysml2CapellaUtils.getSysMLID(eResource, class1);
					}

					manageLogicalFunctionPort(eResource, (LogicalFunction) object2, sysMLID);
				}
			}
		} else if (object instanceof LogicalFunction) {
			manageLogicalFunctionPort(eResource, (LogicalFunction) object, "");
		}

	}

	/**
	 * @param eResource
	 * @param lf
	 */
	private void manageLogicalFunctionPort(Resource eResource, LogicalFunction lf, String prefix) {
		if (_source instanceof AcceptEventAction) {
			EList<OutputPin> results = ((AcceptEventAction) _source).getResults();
			for (OutputPin outputPin : results) {
				FunctionOutputPort outPort = FaFactory.eINSTANCE.createFunctionOutputPort();
				outPort.setName(outputPin.getName());

				lf.getOutputs().add(outPort);
				Sysml2CapellaUtils.trace(this, eResource, outputPin, outPort, "OutputPIN" + prefix);
			}
		}
		if (_source instanceof SendSignalAction) {
			EList<InputPin> arguments = ((SendSignalAction) _source).getArguments();
			for (InputPin inputPin : arguments) {
				FunctionInputPort inPort = FaFactory.eINSTANCE.createFunctionInputPort();
				inPort.setName(inputPin.getName());

				lf.getInputs().add(inPort);
				Sysml2CapellaUtils.trace(this, eResource, inputPin, inPort, "InputPIN" + prefix);

			}
		}
		if (_source instanceof CallBehaviorAction) {
			EList<InputPin> arguments = ((CallBehaviorAction) _source).getArguments();
			for (InputPin inputPin : arguments) {
				FunctionInputPort inPort = FaFactory.eINSTANCE.createFunctionInputPort();
				inPort.setName(inputPin.getName());

				lf.getInputs().add(inPort);
				Sysml2CapellaUtils.trace(this, eResource, inputPin, inPort, "InputPIN" + prefix);

			}

			EList<OutputPin> results = ((CallBehaviorAction) _source).getResults();
			for (OutputPin outputPin : results) {

				FunctionOutputPort outPort = FaFactory.eINSTANCE.createFunctionOutputPort();
				outPort.setName(outputPin.getName());

				lf.getOutputs().add(outPort);
				Sysml2CapellaUtils.trace(this, eResource, outputPin, outPort, "OutputPIN" + prefix);

			}
		} else if (_source instanceof MergeNode) {
			EList<ActivityEdge> incomings = _source.getIncomings();
			for (ActivityEdge activityEdge : incomings) {

				FunctionInputPort inPort = FaFactory.eINSTANCE.createFunctionInputPort();
				inPort.setName(activityEdge.getName());

				lf.getInputs().add(inPort);
				Sysml2CapellaUtils.trace(this, eResource, activityEdge, inPort, "InputComing" + prefix);

			}
			EList<ActivityEdge> outgoings = _source.getOutgoings();
			for (ActivityEdge activityEdge : outgoings) {

				FunctionOutputPort outPort = FaFactory.eINSTANCE.createFunctionOutputPort();
				outPort.setName(activityEdge.getName());

				lf.getOutputs().add(outPort);
				Sysml2CapellaUtils.trace(this, eResource, activityEdge, outPort, "Outputgoing" + prefix);

			}
		} else if (_source instanceof ForkNode) {
			EList<ActivityEdge> outgoings = _source.getOutgoings();
			for (ActivityEdge activityEdge : outgoings) {

				FunctionOutputPort outPort = FaFactory.eINSTANCE.createFunctionOutputPort();
				outPort.setName(activityEdge.getName());

				lf.getOutputs().add(outPort);
				Sysml2CapellaUtils.trace(this, eResource, activityEdge, outPort, "Outputgoing" + prefix);

			}
		}
	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
