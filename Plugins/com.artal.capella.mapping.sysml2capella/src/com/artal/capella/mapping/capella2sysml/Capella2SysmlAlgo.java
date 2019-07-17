/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml;

import org.eclipse.emf.diffmerge.api.scopes.IEditableModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.polarsys.capella.core.data.capellamodeller.Project;

import com.artal.capella.mapping.capella2sysml.rules.ActorsMapping;
import com.artal.capella.mapping.capella2sysml.rules.CapabilitiesRealizationsMapping;
import com.artal.capella.mapping.capella2sysml.rules.ClassesMapping;
import com.artal.capella.mapping.capella2sysml.rules.ComponentBlockMapping;
import com.artal.capella.mapping.capella2sysml.rules.ComponentExchangesMapping;
import com.artal.capella.mapping.capella2sysml.rules.ConstraintsMapping;
import com.artal.capella.mapping.capella2sysml.rules.ExchangeItemMapping;
import com.artal.capella.mapping.capella2sysml.rules.FunctionalExchangesMapping;
import com.artal.capella.mapping.capella2sysml.rules.LogicalFunctionActivityMapping;
import com.artal.capella.mapping.capella2sysml.rules.LogicalFunctionCallBehaviorsMapping;
import com.artal.capella.mapping.capella2sysml.rules.ModelAndStateMapping;
import com.artal.capella.mapping.capella2sysml.rules.PartsMapping;
import com.artal.capella.mapping.capella2sysml.rules.RequierementsMapping;
import com.artal.capella.mapping.capella2sysml.rules.RootInitialMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;
import com.artal.capella.mapping.uml.UMLBridge;
import com.artal.capella.mapping.uml.UMLBridgeAlgo;

/**
 * @author YBI
 *
 */
public class Capella2SysmlAlgo extends UMLBridgeAlgo<Project> {

	MappingRulesManager _managerRules = new MappingRulesManager();
	private String _targetParentFolder;
	private UMLBridge<Project, IEditableModelScope> _mappingBridge;

	public Capella2SysmlAlgo() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.CapellaBridgeAlgo#launch(java.lang.Object,
	 * org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution)
	 */
	@Override
	public void launch(Project source_p, IMappingExecution _mappingExecution) {

		IEditableModelScope targetDataSet = (IEditableModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		SysML2CapellaUMLProfile.initProfiles(rset, getTargetParentFolder());
		RootInitialMapping componentMapping = new RootInitialMapping(this, source_p, _mappingExecution);
		LogicalFunctionActivityMapping._lfs.clear();
		_managerRules.add(componentMapping.getClass().getName(), componentMapping);

		ClassesMapping classesMapping = new ClassesMapping(this, source_p, _mappingExecution);
		_managerRules.add(classesMapping.getClass().getName(), classesMapping);

		ExchangeItemMapping exchangeItemMapping = new ExchangeItemMapping(this, source_p, _mappingExecution);
		_managerRules.add(exchangeItemMapping.getClass().getName(), exchangeItemMapping);

		ActorsMapping actorsMapping = new ActorsMapping(this, source_p, _mappingExecution);
		_managerRules.add(actorsMapping.getClass().getName(), actorsMapping);

		CapabilitiesRealizationsMapping capabilitiesRealizationsMapping = new CapabilitiesRealizationsMapping(this,
				source_p, _mappingExecution);
		_managerRules.add(capabilitiesRealizationsMapping.getClass().getName(), capabilitiesRealizationsMapping);

		ModelAndStateMapping modelAndStateMapping = new ModelAndStateMapping(this, source_p, _mappingExecution);
		_managerRules.add(modelAndStateMapping.getClass().getName(), modelAndStateMapping);

		LogicalFunctionActivityMapping functionActivityMapping = new LogicalFunctionActivityMapping(this, source_p,
				_mappingExecution);
		_managerRules.add(functionActivityMapping.getClass().getName(), functionActivityMapping);

		LogicalFunctionCallBehaviorsMapping callBehaviorsMapping = new LogicalFunctionCallBehaviorsMapping(this,
				source_p, _mappingExecution);
		_managerRules.add(LogicalFunctionCallBehaviorsMapping.class.getName(), callBehaviorsMapping);

		FunctionalExchangesMapping functionalExchangesMapping = new FunctionalExchangesMapping(this, source_p,
				_mappingExecution);
		_managerRules.add(functionalExchangesMapping.getClass().getName(), functionalExchangesMapping);

		ComponentBlockMapping blockMapping = new ComponentBlockMapping(this, source_p, _mappingExecution);
		_managerRules.add(blockMapping.getClass().getName(), blockMapping);

		PartsMapping partsMapping = new PartsMapping(this, source_p, _mappingExecution);
		_managerRules.add(partsMapping.getClass().getName(), partsMapping);

		ComponentExchangesMapping exchangesMapping = new ComponentExchangesMapping(this, source_p, _mappingExecution);
		_managerRules.add(exchangesMapping.getClass().getName(), exchangesMapping);

		EPackage ePackage = Registry.INSTANCE.getEPackage("http://www.polarsys.org/capella/requirements");
		if (ePackage != null) {
			RequierementsMapping requierementsMapping = new RequierementsMapping(this, source_p, _mappingExecution);
			_managerRules.add(requierementsMapping.getClass().getName(), requierementsMapping);
		}

		ConstraintsMapping constraintsMapping = new ConstraintsMapping(this, source_p, _mappingExecution);
		_managerRules.add(constraintsMapping.getClass().getName(), constraintsMapping);

		// execute rules
		_managerRules.executeRules();

	}

	public void setTargetParentFolder(String folder) {
		_targetParentFolder = folder;

	}

	public String getTargetParentFolder() {
		return _targetParentFolder;
	}

	public void setBridge(UMLBridge<Project, IEditableModelScope> mappingBridge) {
		_mappingBridge = mappingBridge;

	}

	public UMLBridge<Project, IEditableModelScope> getMappingBridge() {
		return _mappingBridge;
	}

}
