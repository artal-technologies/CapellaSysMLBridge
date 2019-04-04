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
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Stereotype;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.InformationFactory;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * Transform the SysML parametric Class to Capella Class or ExchangeItem
 * 
 * @author YBI
 *
 */
public class ParametricClassesMapping extends AbstractMapping {
	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;
	/**
	 * A {@link MappingRulesManager} allowing to manage the sub rules.
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
	public ParametricClassesMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		List<Class> classes = Sysml2CapellaUtils.getClasses(_source, getAlgo().getConfiguration().getParametricPath());

		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		DataPkg dataPkgRoot = Sysml2CapellaUtils.getDataPkgRoot(targetScope.getProject());

		for (Class sysmlParamClass : classes) {
			Stereotype blockStereotype = sysmlParamClass.getApplicableStereotype("SysML::Blocks::Block");
			EList<Stereotype> appliedSubstereotypes = sysmlParamClass.getAppliedSubstereotypes(blockStereotype);
			// check is not InterfaceBlock or ConstraintBlock
			if (blockStereotype != null && appliedSubstereotypes.isEmpty()) {
				org.polarsys.capella.core.data.information.Class capellaClass = InformationFactory.eINSTANCE
						.createClass();
				capellaClass.setName(sysmlParamClass.getName());
				dataPkgRoot.getOwnedClasses().add(capellaClass);
				Sysml2CapellaUtils.trace(this, eResource, sysmlParamClass, capellaClass, "Class_");

				PropertyMapping primitiveTypesMapping = new PropertyMapping(getAlgo(), sysmlParamClass,
						_mappingExecution);
				_manager.add(primitiveTypesMapping.getClass().getName()
						+ Sysml2CapellaUtils.getSysMLID(eResource, sysmlParamClass), primitiveTypesMapping);

			}
		}
		_manager.executeRules();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.rules.AbstractMapping#getAlgo()
	 */
	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}
}
