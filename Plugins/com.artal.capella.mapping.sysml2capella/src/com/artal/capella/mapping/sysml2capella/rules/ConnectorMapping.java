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
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.model.helpers.ComponentExchangeExt;
import org.polarsys.capella.core.model.helpers.PortExt;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * Allows to transform {@link Connector} to {@link ComponentExchange}.
 * 
 * @author YBI
 *
 */
public class ConnectorMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
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
	 *            the {@link Model} sysml model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public ConnectorMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		MappingRulesManager managerRules = getAlgo().getManagerRules();
		List<Class> classes = Sysml2CapellaUtils.getClasses(_source, "03 Structure/Product");
		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalComponent rootLogicalSystem = CapellaUtil.getLogicalSystemRoot(targetScope.getProject());
		for (Class class1 : classes) {
			EList<Connector> ownedConnectors = class1.getOwnedConnectors();
			for (Connector connector : ownedConnectors) {
				EList<ConnectorEnd> ends = connector.getEnds();

				// TODO Is it possible to have a number of ConnectorEnd upper to
				// 2 ?
				if (ends.size() == 2) {
					ComponentExchange ce = FaFactory.eINSTANCE.createComponentExchange();
					ce.setName(connector.getName());
					rootLogicalSystem.getOwnedComponentExchanges().add(ce);

					Sysml2CapellaUtils.trace(this, eResource, connector, ce, "Connector_");

					transformEnd(ce, ends.get(0), true, managerRules, eResource);
					transformEnd(ce, ends.get(1), false, managerRules, eResource);
				}
			}
		}
	}

	/**
	 * Transform the ConnectorEnd to {@link ComponentPort} capella
	 * 
	 * @param ce
	 *            the {@link ComponentExchange} to fill
	 * @param connectorEnd
	 *            the sysXML {@link ConnectorEnd} to tranform
	 * @param isSource
	 *            if the {@link ComponentPort} is the source of the
	 *            {@link ComponentExchange}
	 * @param managerRules
	 *            Rules manager
	 * @param eResource
	 *            Sysml resource
	 */
	private void transformEnd(ComponentExchange ce, ConnectorEnd connectorEnd, boolean isSource,
			MappingRulesManager managerRules, Resource eResource) {
		ConnectableElement role = connectorEnd.getRole();
		Type part = connectorEnd.getPartWithPort().getType();
		String partID = Sysml2CapellaUtils.getSysMLID(eResource, part);
		Object object = managerRules.getRule(ComponentPortMapping.class.getName() + partID).getMapSourceToTarget()
				.get(role);
		if (object instanceof ComponentPort) {
			if (isSource) {
				ce.setSource((ComponentPort) object);
			} else {
				ce.setTarget((ComponentPort) object);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.artal.capella.mapping.rules.AbstractMapping#getAlgo()
	 */
	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo) super.getAlgo();
	}
}
