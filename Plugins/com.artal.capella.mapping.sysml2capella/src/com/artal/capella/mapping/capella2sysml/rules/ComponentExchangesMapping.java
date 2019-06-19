/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml.rules;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.diffmerge.impl.scopes.FragmentedModelScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile.UMLProfile;

/**
 * @author YBI
 *
 */
public class ComponentExchangesMapping extends AbstractMapping {

	Project _source;
	IMappingExecution _mappingExecution;

	public ComponentExchangesMapping(CapellaBridgeAlgo<?> algo, Project source, IMappingExecution mappingExecution) {
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
		List<ComponentExchange> ces = Sysml2CapellaUtils.getAllLogicalComponentExchange(_source);

		for (ComponentExchange componentExchange : ces) {
			transformComponentExchanges(componentExchange);
		}

	}

	private void transformComponentExchanges(ComponentExchange ce) {
		org.polarsys.capella.core.data.information.Port sourcePort = ce.getSourcePort();

		org.polarsys.capella.core.data.information.Port targetPort = ce.getTargetPort();

		if (sourcePort == null || targetPort == null) {
			return;
		}

		ResourceSet rset = null;
		Object targetDataSet = _mappingExecution.getTargetDataSet();
		if (targetDataSet instanceof FragmentedModelScope) {
			rset = ((FragmentedModelScope) targetDataSet).getResources().get(0).getResourceSet();
		}
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Stereotype ownedStereotype = profile.getNestedPackage("Ports&Flows").getOwnedStereotype("ProxyPort");
		Port umlSourcePort = transformPort(sourcePort, ownedStereotype);
		Port umlTargetPort = transformPort(targetPort, ownedStereotype);

		// LogicalComponent sourceParent = (LogicalComponent)
		// sourcePort.eContainer().eContainer();
		// LogicalComponent targetParent = (LogicalComponent)
		// targetPort.eContainer().eContainer();

		Queue<Component> sourceAncestorQueue = getAncestorQueue((LogicalComponent) sourcePort.eContainer());
		Queue<Component> targetAncestorQueue = getAncestorQueue((LogicalComponent) targetPort.eContainer());

		Component sourceParent = sourceAncestorQueue.peek();
		Component targetParent = targetAncestorQueue.peek();
		if (sourceParent.equals(targetParent)) {
			Class umlParent = (Class) MappingRulesManager.getCapellaObjectFromAllRules(targetParent);
			Connector connector = UMLFactory.eINSTANCE.createConnector();
			connector.setName(ce.getName());
			umlParent.getOwnedConnectors().add(connector);
			Sysml2CapellaUtils.trace(this, _source.eResource(), ce, connector, "CONNECTOR_");

			ConnectorEnd targetEnd = connector.createEnd();
			// Sysml2CapellaUtils.trace(this, _source.eResource(), ,
			// targetElement, prefix);
			ConnectorEnd sourceEnd = connector.createEnd();
			// Sysml2CapellaUtils.trace(this, _source.eResource(), ,
			// targetElement, prefix);

			sourceEnd.setRole(umlSourcePort);
			targetEnd.setRole(umlTargetPort);

		} else {
			Component commonParent = null;
			for (Component component : targetAncestorQueue) {
				if (sourceAncestorQueue.contains(component)) {
					commonParent = component;
					break;
				}
			}
			Port umlSourceIntermediatePort = createIntermediatePort((ComponentPort) sourcePort, umlSourcePort,
					commonParent, sourceAncestorQueue);
			Port umlTargetIntermediatePort = createIntermediatePort((ComponentPort) targetPort, umlTargetPort,
					commonParent, targetAncestorQueue);

			Class umlParent = (Class) MappingRulesManager.getCapellaObjectFromAllRules(commonParent);
			Connector connector = UMLFactory.eINSTANCE.createConnector();
			connector.setName(ce.getName());
			umlParent.getOwnedConnectors().add(connector);
			Sysml2CapellaUtils.trace(this, _source.eResource(), ce, connector, "CONNECTOR_");

			ConnectorEnd targetEnd = connector.createEnd();
			// Sysml2CapellaUtils.trace(this, _source.eResource(), ,
			// targetElement, prefix);
			ConnectorEnd sourceEnd = connector.createEnd();
			// Sysml2CapellaUtils.trace(this, _source.eResource(), ,
			// targetElement, prefix);

			sourceEnd.setRole(umlSourceIntermediatePort);
			targetEnd.setRole(umlTargetIntermediatePort);

		}

	}

	private Port createIntermediatePort(ComponentPort capellaPort, Port umlPort, Component common,
			Queue<Component> acestors) {
		Component parent;
		Component child = (Component) capellaPort.eContainer();
		boolean isFoundCommon = false;
		while ((parent = acestors.poll()) != null && !isFoundCommon) {
			if (common.equals(parent)) {
				isFoundCommon = true;

			}
			Class umlChild = (Class) MappingRulesManager.getCapellaObjectFromAllRules(child);
			Class umlParent = (Class) MappingRulesManager.getCapellaObjectFromAllRules(parent);
			if (!isFoundCommon) {
				Connector connector = UMLFactory.eINSTANCE.createConnector();
				umlParent.getOwnedConnectors().add(connector);

				ConnectorEnd sourceEnd = connector.createEnd();
				sourceEnd.setRole(umlPort);
				ConnectorEnd targetEnd = connector.createEnd();

				Port mirrorPort = umlParent.createOwnedPort(capellaPort.getName(), null);
				targetEnd.setRole(mirrorPort);
				umlPort = mirrorPort;

			}

		}
		return umlPort;
	}

	private Port transformPort(org.polarsys.capella.core.data.information.Port sourcePort, Stereotype ownedStereotype) {
		EObject parent = sourcePort.eContainer();
		Class blockComp = (Class) MappingRulesManager.getCapellaObjectFromAllRules(parent);

		Port umlPort = blockComp.createOwnedPort(sourcePort.getName(), null);
		umlPort.applyStereotype(ownedStereotype);
		Sysml2CapellaUtils.trace(this, _source.eResource(), sourcePort, umlPort, "PORT_");
		return umlPort;
	}

	protected Queue<Component> getAncestorQueue(Component component) {
		Queue<Component> queue = new LinkedList<>();
		EObject parent = component;
		Component current = component;
		boolean parentContainerIsNull = false;
		while (!parentContainerIsNull) {
			parent = parent.eContainer();
			if (parent instanceof Component) {
				Part inversePart = Sysml2CapellaUtils.getInversePart(current);
				Component compoParentPart = (Component) inversePart.eContainer();
				if (Sysml2CapellaUtils.isDescendantComponent(compoParentPart, (Component) parent)) {
					parent = compoParentPart;
				}
				queue.add((Component) parent);
				current = (Component) parent;
			}
			if (parent.eContainer() == null) {
				parentContainerIsNull = true;
			}

		}
		queue.add(Sysml2CapellaUtils.getLogicalContext(_source));
		return queue;

	}

}
