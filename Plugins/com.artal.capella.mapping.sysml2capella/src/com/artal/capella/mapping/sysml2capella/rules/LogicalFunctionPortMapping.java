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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.CallBehaviorAction;
import org.eclipse.uml2.uml.InputPin;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.OutputPin;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.FunctionInputPort;
import org.polarsys.capella.core.data.fa.FunctionOutputPort;
import org.polarsys.capella.core.data.la.LogicalFunction;

import com.artal.capella.mapping.CapellaBridgeAlgo;
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
	CallBehaviorAction _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

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
	public LogicalFunctionPortMapping(Sysml2CapellaAlgo algo, CallBehaviorAction source,
			IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
		_mappingExecution = mappingExecution;
	}

	@Override
	public void computeMapping() {
		Resource eResource = _source.eResource();
		AbstractMapping rule = MappingRulesManager.getRule(LogicalFunctionMapping.class.getName());
		LogicalFunction lf = (LogicalFunction) rule.getMapSourceToTarget().get(_source);
		EList<InputPin> arguments = _source.getArguments();
		for (InputPin inputPin : arguments) {
			FunctionInputPort inPort = FaFactory.eINSTANCE.createFunctionInputPort();
			inPort.setName(inputPin.getName());

			lf.getInputs().add(inPort);
			Sysml2CapellaUtils.trace(this, eResource, inputPin, inPort, "InputPIN");

		}

		EList<OutputPin> results = _source.getResults();
		for (OutputPin outputPin : results) {

			FunctionOutputPort outPort = FaFactory.eINSTANCE.createFunctionOutputPort();
			outPort.setName(outputPin.getName());

			lf.getOutputs().add(outPort);
			Sysml2CapellaUtils.trace(this, eResource, outputPin, outPort, "OutputPIN");

		}

	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}

}
