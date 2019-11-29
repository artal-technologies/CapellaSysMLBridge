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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.common.data.modellingcore.AbstractExchangeItem;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.OrientationPortKind;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile.UMLProfile;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ComponentExchangesMapping extends AbstractMapping {

	/**
	 * The capella source element.
	 */
	Project _source;
	/**
	 * The {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * Constructor.
	 * 
	 * @param algo
	 *            the {@link Capella2SysmlAlgo} algo.
	 * @param source
	 *            the Capella source {@link Project}.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
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

	/**
	 * Transform {@link ComponentExchange} to {@link Connector}
	 * 
	 * @param ce
	 *            the {@link ComponentExchange} to transform
	 */
	private void transformComponentExchanges(ComponentExchange ce) {

		org.polarsys.capella.core.data.information.Port sourcePort = ce.getSourcePort();
		org.polarsys.capella.core.data.information.Port targetPort = ce.getTargetPort();

		// the ComponentExchange is connected at both source and target ports.
		if (sourcePort == null || targetPort == null || !(sourcePort.eContainer() instanceof LogicalComponent)
				|| !(targetPort.eContainer() instanceof LogicalComponent)) {
			return;
		}

		// get stereotype ProxyPort
		IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Package portAndFlow = profile.getNestedPackage("Ports&Flows");
		Stereotype ownedStereotype = portAndFlow.getOwnedStereotype("ProxyPort");

		// transform the both source and target ports.
		Port umlSourcePort = transformPort(sourcePort, ownedStereotype);
		Port umlTargetPort = transformPort(targetPort, ownedStereotype);

		// get the both source and target Component parent.
		LogicalComponent sourceComponent = (LogicalComponent) sourcePort.eContainer();
		LogicalComponent targetComponent = (LogicalComponent) targetPort.eContainer();

		// get all the ancestors for source and target component parent.
		Queue<Component> sourceAncestorQueue = getAncestorQueue(sourceComponent);
		Queue<Component> targetAncestorQueue = getAncestorQueue(targetComponent);

		// get both source and target Part.
		Part sourcePart = Sysml2CapellaUtils.getInversePart(sourceComponent);
		Part targetPart = Sysml2CapellaUtils.getInversePart(targetComponent);

		// get source and target properties.
		Property sourceProp = (Property) MappingRulesManager.getCapellaObjectFromAllRules(sourcePart);
		Property targetProp = (Property) MappingRulesManager.getCapellaObjectFromAllRules(targetPart);

		Component sourceParent = sourceAncestorQueue.peek();
		Component targetParent = targetAncestorQueue.peek();
		if (sourceParent.equals(targetParent)) {
			transformConnectorWithCommonParent(ce, (ComponentPort) sourcePort, (ComponentPort) targetPort, profile,
					umlSourcePort, umlTargetPort, sourceProp, targetProp, targetParent);

		} else {
			transformConnectorWithDiffParent(ce, sourcePort, targetPort, umlSourcePort, umlTargetPort,
					sourceAncestorQueue, targetAncestorQueue, sourceProp, targetProp);

		}

	}

	/**
	 * Transform {@link ComponentExchange} with port parents are not in same
	 * parent Component.
	 * 
	 * @param ce
	 *            The {@link ComponentExchange} to transform
	 * @param sourcePort
	 *            the capella source
	 *            {@link org.polarsys.capella.core.data.information.Port}
	 * @param targetPort
	 *            the capella target
	 *            {@link org.polarsys.capella.core.data.information.Port}
	 * @param umlSourcePort
	 *            the uml source {@link Port}
	 * @param umlTargetPort
	 *            the uml target {@link Port}
	 * @param sourceAncestorQueue
	 *            the source port ancestors
	 * @param targetAncestorQueue
	 *            the target port ancestors
	 * @param sourceProp
	 *            the source {@link Property}
	 * @param targetPropthe
	 *            target Property
	 */
	private void transformConnectorWithDiffParent(ComponentExchange ce,
			org.polarsys.capella.core.data.information.Port sourcePort,
			org.polarsys.capella.core.data.information.Port targetPort, Port umlSourcePort, Port umlTargetPort,
			Queue<Component> sourceAncestorQueue, Queue<Component> targetAncestorQueue, Property sourceProp,
			Property targetProp) {

		// get Common parent
		Component commonParent = getCommonParent(sourceAncestorQueue, targetAncestorQueue);

		// create all the intermdiate Port (with respect to the difference of
		// level between component source and component target).
		Entry<Port, Property> portPropSource = createIntermediatePort((ComponentPort) sourcePort, umlSourcePort,
				commonParent, sourceAncestorQueue).entrySet().iterator().next();
		Port umlSourceIntermediatePort = portPropSource.getKey();
		Entry<Port, Property> portPropTarget = createIntermediatePort((ComponentPort) targetPort, umlTargetPort,
				commonParent, targetAncestorQueue).entrySet().iterator().next();
		Port umlTargetIntermediatePort = portPropTarget.getKey();

		// get the uml Class mapped with the capella common parent.
		Class umlParent = (Class) MappingRulesManager.getCapellaObjectFromAllRules(commonParent);

		// create new connector.
		createConnector(ce, portPropSource.getValue(), portPropTarget.getValue(), umlSourceIntermediatePort,
				umlTargetIntermediatePort, umlParent);
	}

	/**
	 * Create a new {@link Connector}
	 * 
	 * @param ce
	 *            the {@link ComponentExchange} to transfom
	 * @param sourceProp
	 *            the source {@link Property}
	 * @param targetProp
	 *            the target {@link Property}
	 * @param umlSourceIntermediatePort
	 *            the source intermediate {@link Port}
	 * @param umlTargetIntermediatePort
	 *            the target intermediate {@link Port}
	 * @param umlParent
	 *            the uml parent
	 */
	private void createConnector(ComponentExchange ce, Property sourceProp, Property targetProp,
			Port umlSourceIntermediatePort, Port umlTargetIntermediatePort, Class umlParent) {
		Connector connector = UMLFactory.eINSTANCE.createConnector();
		connector.setName(ce.getName());
		umlParent.getOwnedConnectors().add(connector);
		Sysml2CapellaUtils.trace(this, _source.eResource(), ce, connector, "CONNECTOR_");
		
		
		// manage binding connector stereotype
		IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
		ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
		Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
		Package blockProfile = profile.getNestedPackage("Blocks");
		Stereotype bindingConnectorStereo = blockProfile.getOwnedStereotype("BindingConnector");
		EObject applyStereotype = connector.applyStereotype(bindingConnectorStereo);
		getAlgo().getStereoApplications().add(applyStereotype);
		
		ConnectorEnd targetEnd = connector.createEnd();
		// Sysml2CapellaUtils.trace(this, _source.eResource(), ,
		// targetElement, prefix);
		ConnectorEnd sourceEnd = connector.createEnd();
		// Sysml2CapellaUtils.trace(this, _source.eResource(), ,
		// targetElement, prefix);

		sourceEnd.setRole(umlSourceIntermediatePort);
		sourceEnd.setPartWithPort(sourceProp);
		targetEnd.setRole(umlTargetIntermediatePort);
		targetEnd.setPartWithPort(targetProp);

		// manage nested connector ends stereotype.
		Stereotype nestedConnectorEndStereo = blockProfile.getOwnedStereotype("NestedConnectorEnd");
		if (nestedConnectorEndStereo != null) {
			List<Property> srcList = new ArrayList<>();
			srcList.add(sourceProp);
			List<Property> trgList = new ArrayList<>();
			trgList.add(targetProp);

			EObject applyStereotype3 = sourceEnd.applyStereotype(nestedConnectorEndStereo);
			sourceEnd.setValue(nestedConnectorEndStereo, "propertyPath", srcList);

			EObject applyStereotype2 = targetEnd.applyStereotype(nestedConnectorEndStereo);
			targetEnd.setValue(nestedConnectorEndStereo, "propertyPath", trgList);

			getAlgo().getStereoApplications().add(applyStereotype3);
			getAlgo().getStereoApplications().add(applyStereotype2);
		}

	}

	/**
	 * Get the common parent
	 * 
	 * @param sourceAncestorQueue
	 *            all the source ancestors
	 * @param targetAncestorQueue
	 *            all the target ancestors
	 * @return the first common component.
	 */
	private Component getCommonParent(Queue<Component> sourceAncestorQueue, Queue<Component> targetAncestorQueue) {
		Component commonParent = null;
		for (Component component : targetAncestorQueue) {
			if (sourceAncestorQueue.contains(component)) {
				commonParent = component;
				break;
			}
		}
		return commonParent;
	}

	/**
	 * Transform {@link ComponentExchange} with ports have the same parent
	 * 
	 * @param ce
	 *            the {@link ComponentExchange} to tranform
	 * @param targetPort
	 * @param sourcePort
	 * @param profile
	 *            the SysML profile
	 * @param umlSourcePort
	 *            the uml source {@link Port}
	 * @param umlTargetPort
	 *            the uml target {@link Port}
	 * @param sourceProp
	 *            the source {@link Property}
	 * @param targetProp
	 *            the target {@link Property}
	 * @param commonParent
	 *            the parent.
	 */
	private void transformConnectorWithCommonParent(ComponentExchange ce, ComponentPort sourcePort,
			ComponentPort targetPort, Profile profile, Port umlSourcePort, Port umlTargetPort, Property sourceProp,
			Property targetProp, Component commonParent) {
		Class umlParent = (Class) MappingRulesManager.getCapellaObjectFromAllRules(commonParent);
		Connector connector = UMLFactory.eINSTANCE.createConnector();
		connector.setName(ce.getName());
		umlParent.getOwnedConnectors().add(connector);
		Sysml2CapellaUtils.trace(this, _source.eResource(), ce, connector, "CONNECTOR_");
		Package blockProfile = profile.getNestedPackage("Blocks");
		Stereotype bindingConnectorStereo = blockProfile.getOwnedStereotype("BindingConnector");
		EObject applyStereotype = connector.applyStereotype(bindingConnectorStereo);
		getAlgo().getStereoApplications().add(applyStereotype);

		ConnectorEnd targetEnd = connector.createEnd();
		Sysml2CapellaUtils.trace(this, _source.eResource(),
				Sysml2CapellaUtils.getSysMLID(_source.eResource(), ce) + "CONNECTORENDTARGET", targetEnd, "TARGETEND_");
		ConnectorEnd sourceEnd = connector.createEnd();
		Sysml2CapellaUtils.trace(this, _source.eResource(),
				Sysml2CapellaUtils.getSysMLID(_source.eResource(), ce) + "CONNECTORENDSOURCE", sourceEnd, "SOURCEEND_");

		EList<AbstractExchangeItem> convoyedInformations = ce.getConvoyedInformations();
		for (AbstractExchangeItem abstractExchangeItem : convoyedInformations) {
			transformConvoyedInformation(sourcePort, targetPort, umlSourcePort, umlTargetPort, sourceProp, targetProp,
					blockProfile, targetEnd, sourceEnd, abstractExchangeItem, profile);

		}

		sourceEnd.setRole(umlSourcePort);
		sourceEnd.setPartWithPort(sourceProp);
		targetEnd.setRole(umlTargetPort);
		targetEnd.setPartWithPort(targetProp);
	}

	/**
	 * Transform the Convoyed information
	 * 
	 * @param targetPort
	 * @param sourcePort
	 * 
	 * @param umlSourcePort
	 *            the uml source {@link Port}
	 * @param umlTargetPort
	 *            the uml target {@link Port}
	 * @param sourceProp
	 *            the source {@link Property}
	 * @param targetProp
	 *            the target {@link Property}
	 * @param blockProfile
	 *            the block profile;
	 * @param targetEnd
	 *            the target {@link ConnectorEnd}
	 * @param sourceEnd
	 *            the source ConnectorEnd
	 * @param abstractExchangeItem
	 *            the convoyed information
	 * @param profile
	 */
	private void transformConvoyedInformation(ComponentPort sourcePort, ComponentPort targetPort, Port umlSourcePort,
			Port umlTargetPort, Property sourceProp, Property targetProp, Package blockProfile, ConnectorEnd targetEnd,
			ConnectorEnd sourceEnd, AbstractExchangeItem abstractExchangeItem, Profile profile) {
		Class umlType = (Class) MappingRulesManager.getCapellaObjectFromAllRules(abstractExchangeItem);
		if (umlType != null) {
			umlSourcePort.setType(umlType);
			umlTargetPort.setType(umlType);

			// manage direction
			manageOrientation(sourcePort, umlSourcePort);
			manageOrientation(targetPort, umlTargetPort);

			Stereotype nestedConnectorEndStereo = blockProfile.getOwnedStereotype("NestedConnectorEnd");
			if (nestedConnectorEndStereo != null) {
				List<Property> srcList = new ArrayList<>();
				srcList.add(sourceProp);
				List<Property> trgList = new ArrayList<>();
				trgList.add(targetProp);

				EObject applyStereotype = sourceEnd.applyStereotype(nestedConnectorEndStereo);
				sourceEnd.setValue(nestedConnectorEndStereo, "propertyPath", srcList);

				EObject applyStereotype2 = targetEnd.applyStereotype(nestedConnectorEndStereo);
				targetEnd.setValue(nestedConnectorEndStereo, "propertyPath", trgList);

				getAlgo().getStereoApplications().add(applyStereotype);
				getAlgo().getStereoApplications().add(applyStereotype2);
			}
			return;
		}
	}

	/**
	 * Create intermediate port and connector.
	 * 
	 * @param capellaPort
	 *            the source capella port
	 * @param umlPort
	 *            the uml port
	 * @param common
	 *            the common parent
	 * @param ancestors
	 *            the ancestor
	 * @return the intermediage port
	 */
	private Map<Port, Property> createIntermediatePort(ComponentPort capellaPort, Port umlPort, Component common,
			Queue<Component> ancestors) {
		// reach the ancestor while the common parent is not found.
		Component parent;
		boolean isFoundCommon = false;
		Component container = (Component) capellaPort.eContainer();
		Property targetProp = null;
		Part partInitial = Sysml2CapellaUtils.getInversePart(container);

		// get source and target properties.
		targetProp = (Property) MappingRulesManager.getCapellaObjectFromAllRules(partInitial);
		// reach ancestors
		while ((parent = ancestors.poll()) != null && !isFoundCommon) {
			// if common parent found, exit the while loop.
			if (common.equals(parent)) {
				isFoundCommon = true;

			}
			// get uml parent
			Class umlParent = (Class) MappingRulesManager.getCapellaObjectFromAllRules(parent);
			// if umlParent it's not the common parent, create intermediate
			// connector and intermediate port. Connect this connector at the
			// intermediate port.
			if (!isFoundCommon) {
				Connector connector = UMLFactory.eINSTANCE.createConnector();
				umlParent.getOwnedConnectors().add(connector);

				// Manage binding connector stereotype
				IModelScope targetDataSet = (IModelScope) _mappingExecution.getTargetDataSet();
				ResourceSet rset = Sysml2CapellaUtils.getTargetResourceSet(targetDataSet);
				Profile profile = SysML2CapellaUMLProfile.getProfile(rset, UMLProfile.SYSML_PROFILE);
				Package blockProfile = profile.getNestedPackage("Blocks");
				Stereotype bindingConnectorStereo = blockProfile.getOwnedStereotype("BindingConnector");
				EObject applyStereotype = connector.applyStereotype(bindingConnectorStereo);
				getAlgo().getStereoApplications().add(applyStereotype);

				ConnectorEnd sourceEnd = connector.createEnd();
				sourceEnd.setRole(umlPort);

				ConnectorEnd targetEnd = connector.createEnd();

				Port mirrorPort = umlParent.createOwnedPort(capellaPort.getName(), null);
				targetEnd.setRole(mirrorPort);
				umlPort = mirrorPort;
				
				Part partSource = Sysml2CapellaUtils.getInversePart(container);
				Property sourceProp = (Property) MappingRulesManager.getCapellaObjectFromAllRules(partSource);
				container = parent;
				Part partTarget = Sysml2CapellaUtils.getInversePart(container);

				// get source and target properties.
				targetProp = (Property) MappingRulesManager.getCapellaObjectFromAllRules(partTarget);

				sourceEnd.setPartWithPort(sourceProp);

				Stereotype nestedConnectorEndStereo = blockProfile.getOwnedStereotype("NestedConnectorEnd");
				if (nestedConnectorEndStereo != null) {
					List<Property> srcList = new ArrayList<>();
					srcList.add(sourceProp);
//					List<Property> trgList = new ArrayList<>();
//					trgList.add(targetProp);

					EObject applyStereotype3 = sourceEnd.applyStereotype(nestedConnectorEndStereo);
					sourceEnd.setValue(nestedConnectorEndStereo, "propertyPath", srcList);

//					EObject applyStereotype2 = targetEnd.applyStereotype(nestedConnectorEndStereo);
//					targetEnd.setValue(nestedConnectorEndStereo, "propertyPath", trgList);

					getAlgo().getStereoApplications().add(applyStereotype3);
//					getAlgo().getStereoApplications().add(applyStereotype2);
				}

			}

		}

		// return the port under the common parent.
		Map<Port, Property> map = new HashMap<>();
		map.put(umlPort, targetProp);
		return map;
	}

	private void manageOrientation(ComponentPort port, Port umlPort) {
		OrientationPortKind orientationSrc = port.getOrientation();
		if (orientationSrc == OrientationPortKind.IN) {
			umlPort.setIsConjugated(true);
		} else if (orientationSrc == OrientationPortKind.OUT) {
			umlPort.setIsConjugated(false);
		}
	}

	/**
	 * Transform Capella {@link org.polarsys.capella.core.data.information.Port}
	 * to uml {@link Port}.
	 * 
	 * @param capellaPort
	 *            the capella {@link Port} to transform.
	 * @param ownedStereotype
	 *            the stereotype to apply on created port.
	 * @return the uml {@link Port}.
	 */
	private Port transformPort(org.polarsys.capella.core.data.information.Port capellaPort,
			Stereotype ownedStereotype) {
		EObject parent = capellaPort.eContainer();
		if (parent instanceof LogicalComponent) {
			Class blockComp = (Class) MappingRulesManager.getCapellaObjectFromAllRules(parent);

			Port umlPort = blockComp.createOwnedPort(capellaPort.getName(), null);
			EObject applyStereotype = umlPort.applyStereotype(ownedStereotype);
			getAlgo().getStereoApplications().add(applyStereotype);
			Sysml2CapellaUtils.trace(this, _source.eResource(), capellaPort, umlPort, "PORT_");
			return umlPort;
		}
		return null;
	}

	/**
	 * Get the ancestors {@link Queue}.
	 * 
	 * @param component
	 *            the child component.
	 * @return the {@link Queue}.
	 */
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

	@Override
	public Capella2SysmlAlgo getAlgo() {
		return (Capella2SysmlAlgo) super.getAlgo();
	}

}
