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
import java.util.stream.Collectors;

import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Model;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.InformationFactory;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ValueTypesMapping extends AbstractMapping {

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

	public ValueTypesMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		DataPkg dataPkgRoot = Sysml2CapellaUtils.getDataPkgRoot(targetScope.getProject());

		List<DataType> dataTypes = Sysml2CapellaUtils
				.getDatatTypes(_source, getAlgo().getConfiguration().getParametricPath()).stream()
				.map(DataType.class::cast).filter(dt -> dt.getAppliedStereotype("SysML::Blocks::ValueType") != null)
				.collect(Collectors.toList());

		for (DataType dataType : dataTypes) {
			transformValueType(_source.eResource(), dataPkgRoot, dataType);

		}

		_manager.executeRules();

	}

	private void transformValueType(Resource eResource, DataPkg dataPkgRoot, DataType dataType) {
		org.polarsys.capella.core.data.information.Class capellaClass = InformationFactory.eINSTANCE.createClass();
		capellaClass.setIsPrimitive(true);
		capellaClass.setName(dataType.getName());
		dataPkgRoot.getOwnedClasses().add(capellaClass);
		Sysml2CapellaUtils.trace(this, eResource, dataType, capellaClass, "ValueType_");

		PropertyMapping primitiveTypesMapping = new PropertyMapping(getAlgo(), dataType, _mappingExecution);
		_manager.add(primitiveTypesMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, dataType),
				primitiveTypesMapping);

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
