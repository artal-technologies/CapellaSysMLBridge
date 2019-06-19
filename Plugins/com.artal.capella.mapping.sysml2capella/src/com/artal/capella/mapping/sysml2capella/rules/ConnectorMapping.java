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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.data.la.LogicalComponent;

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
		// manage product block and all sub block.
		List<Class> classes = Sysml2CapellaUtils.getClasses(_source, getAlgo().getConfiguration().getProductPath());
		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalComponent rootLogicalSystem = Sysml2CapellaUtils.getLogicalSystemRoot(targetScope.getProject());
		transfomConnectors(classes, eResource, rootLogicalSystem);

		// manage all block of Parts.
		List<Class> subClasses = Sysml2CapellaUtils.getClasses(_source, getAlgo().getConfiguration().getPartPath());
		for (Class class1 : subClasses) {
			ArrayList<Class> classes2 = new ArrayList<Class>();
			classes2.add(class1);
			AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
			LogicalComponent lc = (LogicalComponent) rule.getMapSourceToTarget().get(class1);
			transfomConnectors(classes2, eResource, lc);
		}

	}

	/**
	 * Transform all connectors. It's a recursive method.
	 * 
	 * @param classes
	 *            the class to browse for get the connectors
	 * @param eResource
	 *            the sysml resource
	 * @param rootLogicalSystem
	 *            the {@link LogicalComponent} containing all the created
	 *            {@link ComponentExchange}
	 */
	private void transfomConnectors(List<Class> classes, Resource eResource, LogicalComponent lcContainer) {
		for (Class class1 : classes) {
			EList<Connector> ownedConnectors = class1.getOwnedConnectors();
			for (Connector connector : ownedConnectors) {
				EList<ConnectorEnd> ends = connector.getEnds();

				// TODO Is it possible to have a number of ConnectorEnd upper to
				// 2 ?
				if (ends.size() == 2) {
					ComponentExchange ce = FaFactory.eINSTANCE.createComponentExchange();
					ce.setName(connector.getName());
					lcContainer.getOwnedComponentExchanges().add(ce);

					Sysml2CapellaUtils.trace(this, eResource, connector, ce, "Connector_");

					transformEnd(ce, ends.get(0), true, eResource);
					transformEnd(ce, ends.get(1), false, eResource);
				}
			}

			List<Class> subClasses = Sysml2CapellaUtils.getSubClasses(class1);
			AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
			LogicalComponent lc = (LogicalComponent) rule.getMapSourceToTarget().get(class1);
			transfomConnectors(subClasses, eResource, lc);
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
	 * @param eResource
	 *            Sysml resource
	 */
	private void transformEnd(ComponentExchange ce, ConnectorEnd connectorEnd, boolean isSource, Resource eResource) {
		ConnectableElement role = connectorEnd.getRole();
		Property partWithPort = connectorEnd.getPartWithPort();
		if (partWithPort == null) {
			return;
		}
		Type part = partWithPort.getType();
		String partID = Sysml2CapellaUtils.getSysMLID(eResource, part);
		ComponentPortMapping rule = (ComponentPortMapping) MappingRulesManager
				.getRule(ComponentPortMapping.class.getName() + partID);
		Object object = null;

		if (rule == null) {
			return;
		}

		Map<Port, Map<String, Port>> mapPortToSubPort = rule.getMapPortToSubPort();
		Map<String, Port> map = mapPortToSubPort.get(role);

		if (map != null) {
			Entry<String, Port> next = map.entrySet().iterator().next();
			String subPartID = next.getKey();
			Port subPort = next.getValue();

			Type type = subPort.getType();
			Object capellaObjectFromAllRules = MappingRulesManager.getCapellaObjectFromAllRules(type);
			if (capellaObjectFromAllRules instanceof ExchangeItem) {
				ce.getConvoyedInformations().add((ExchangeItem) capellaObjectFromAllRules);
			}

			ComponentPortMapping subRule = (ComponentPortMapping) MappingRulesManager
					.getRule(ComponentPortMapping.class.getName() + subPartID);
			object = subRule.getMapSourceToTarget().get(subPort);
		} else {
			Object capellaObjectFromAllRules = MappingRulesManager.getCapellaObjectFromAllRules(role.getType());
			if (capellaObjectFromAllRules instanceof ExchangeItem) {
				ce.getConvoyedInformations().add((ExchangeItem) capellaObjectFromAllRules);
			}
			object = rule.getMapSourceToTarget().get(role);
		}

		if (object instanceof ComponentPort) {
			if (isSource) {
				ce.setSource((ComponentPort) object);
			} else {
				ce.setTarget((ComponentPort) object);
			}
		}
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
