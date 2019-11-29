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
import java.util.stream.Collectors;

import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Stereotype;
import org.polarsys.capella.core.data.cs.CsFactory;
import org.polarsys.capella.core.data.cs.ExchangeItemAllocation;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.cs.InterfacePkg;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.data.information.ExchangeMechanism;
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
		List<Class> classes = getAllSubClass(
				Sysml2CapellaUtils.getClasses(_source, getAlgo().getConfiguration().getParametricPath()));

		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		DataPkg dataPkgRoot = Sysml2CapellaUtils.getDataPkgRoot(targetScope.getProject());

		Map<String, AbstractMapping> blocks = new HashMap<String, AbstractMapping>();
		Map<String, AbstractMapping> interfaces = new HashMap<String, AbstractMapping>();
		Map<String, AbstractMapping> constraints = new HashMap<String, AbstractMapping>();

		for (Class sysmlParamClass : classes) {
			Stereotype blockStereotype = sysmlParamClass.getApplicableStereotype("SysML::Blocks::Block");
			if (blockStereotype != null) {
				Stereotype interfaceBlock = sysmlParamClass.getAppliedSubstereotype(blockStereotype,
						"SysML::Ports&Flows::InterfaceBlock");
				Stereotype constraintBlock = sysmlParamClass.getAppliedSubstereotype(blockStereotype,
						"SysML::ConstraintBlocks::ConstraintBlock");
				// check is not InterfaceBlock or ConstraintBlock
				if (interfaceBlock != null) {
					InterfacePkg interfacePkgRoot = Sysml2CapellaUtils.getInterfacePkgRoot(targetScope.getProject());
					transformExchangeItem(eResource, dataPkgRoot, interfacePkgRoot, interfaces, sysmlParamClass);
				} else if (constraintBlock != null) {

					transformConstraintClass(eResource, dataPkgRoot, constraints, sysmlParamClass);
				} else {
					transformClass(eResource, dataPkgRoot, blocks, sysmlParamClass);
				}

			}
		}

		// fill the block properties rule in first
		for (Entry<String, AbstractMapping> entryBlock : blocks.entrySet()) {
			_manager.add(entryBlock.getKey(), entryBlock.getValue());
		}
		// and the InterfaceBlock rule
		for (Entry<String, AbstractMapping> entryInterfaces : interfaces.entrySet()) {
			_manager.add(entryInterfaces.getKey(), entryInterfaces.getValue());
		}
		for (Entry<String, AbstractMapping> entryConstraintes : constraints.entrySet()) {
			_manager.add(entryConstraintes.getKey(), entryConstraintes.getValue());
		}

		_manager.executeRules();

	}

	private List<Class> getAllSubClass(List<Class> classes) {
		List<Class> results = new ArrayList<Class>();
		for (Classifier clazz : classes) {
			results.add((Class) clazz);
			List<Class> nestedClassifiers = ((Class) clazz).getNestedClassifiers().stream()
					.filter(cl -> (cl instanceof Class)).map(Class.class::cast).collect(Collectors.toList());
			results.addAll(getAllSubClass(nestedClassifiers));
		}
		return results;
	}

	private void transformConstraintClass(Resource eResource, DataPkg dataPkgRoot,
			Map<String, AbstractMapping> constraints, Class sysmlParamClass) {

		// nothing to do, the BlockConstraint aren't transformed in Capella. The
		// constraint under the Block constraint is move under the Block
		// constraint container.

	}

	/**
	 * Transform the SysML {@link Class} to Capella
	 * {@link org.polarsys.capella.core.data.information.Class}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param dataPkgRoot
	 *            the {@link DataPkg} root
	 * @param blocks
	 *            the {@link Map} to fill with the properties rule
	 * @param sysmlParamClass
	 *            the SysML {@link Class} to transform
	 */
	private void transformClass(Resource eResource, DataPkg dataPkgRoot, Map<String, AbstractMapping> blocks,
			Class sysmlParamClass) {
		org.polarsys.capella.core.data.information.Class capellaClass = InformationFactory.eINSTANCE.createClass();
		capellaClass.setName(sysmlParamClass.getName());
		dataPkgRoot.getOwnedClasses().add(capellaClass);
		Sysml2CapellaUtils.trace(this, eResource, sysmlParamClass, capellaClass, "Class_");

		PropertyMapping primitiveTypesMapping = new PropertyMapping(getAlgo(), sysmlParamClass, _mappingExecution);
		blocks.put(
				primitiveTypesMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, sysmlParamClass),
				primitiveTypesMapping);
	}

	/**
	 * Transform the SysML {@link Class} to Capella {@link ExchangeItem}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param dataPkgRoot
	 *            the {@link DataPkg} root
	 * @param interfacePkgRoot
	 * @param interfaces
	 *            the {@link Map} to fill with the properties rule
	 * @param sysmlParamClass
	 *            the SysML {@link Class} to transform
	 */
	private void transformExchangeItem(Resource eResource, DataPkg dataPkgRoot, InterfacePkg interfacePkgRoot,
			Map<String, AbstractMapping> interfaces, Class sysmlParamClass) {

		Interface inter = CsFactory.eINSTANCE.createInterface();
		inter.setName(sysmlParamClass.getName());
		interfacePkgRoot.getOwnedInterfaces().add(inter);
		Sysml2CapellaUtils.trace(this, eResource,
				Sysml2CapellaUtils.getSysMLID(_source.eResource(), sysmlParamClass) + "INTERFACE", inter, "INTERFACE");

		ExchangeItem ei = InformationFactory.eINSTANCE.createExchangeItem();
		ei.setName(sysmlParamClass.getName());
		ei.setExchangeMechanism(ExchangeMechanism.FLOW);
		dataPkgRoot.getOwnedExchangeItems().add(ei);
		Sysml2CapellaUtils.trace(this, eResource, sysmlParamClass, ei, "EXCHANGEITEM");

		ExchangeItemAllocation eia = CsFactory.eINSTANCE.createExchangeItemAllocation();
		eia.setAllocatedItem(ei);
		inter.getOwnedExchangeItemAllocations().add(eia);
		Sysml2CapellaUtils.trace(this, eResource,
				Sysml2CapellaUtils.getSysMLID(_source.eResource(), sysmlParamClass) + "EXCHANGEITEMALLOCATION", eia,
				"EXCHANGEITEMALLOCATION");

		PropertyMapping propertyMapping = new PropertyMapping(getAlgo(), sysmlParamClass, _mappingExecution);
		interfaces.put(propertyMapping.getClass().getName() + Sysml2CapellaUtils.getSysMLID(eResource, sysmlParamClass),
				propertyMapping);
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
