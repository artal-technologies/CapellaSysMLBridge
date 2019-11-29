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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.ConnectableElement;
import org.eclipse.uml2.uml.Connector;
import org.eclipse.uml2.uml.ConnectorEnd;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.polarsys.capella.core.data.cs.Interface;
import org.polarsys.capella.core.data.fa.ComponentPort;
import org.polarsys.capella.core.data.fa.ComponentPortKind;
import org.polarsys.capella.core.data.fa.FaFactory;
import org.polarsys.capella.core.data.fa.OrientationPortKind;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * {@link ComponentMapping} allows to manage the {@link Port} to
 * {@link ComponentPort} mapping.
 * 
 * @author YBI
 *
 */
public class ComponentPortMapping extends AbstractMapping {

	/**
	 * The {@link Class} containing the port to transform.
	 */
	private Class _source;

	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	Map<Port, Map<String, Port>> _mapPortToSubPort;

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
	public ComponentPortMapping(Sysml2CapellaAlgo algo, Class source, IMappingExecution mappingExecution) {
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
		_mapPortToSubPort = new HashMap<Port, Map<String, Port>>();

		// transform all port of source Class.
		transformPorts(eResource, _source);
	}

	/**
	 * Transform all leaf ports. Leaf port are the port of class doesn't have
	 * sub classes.
	 * 
	 * @param eResource
	 *            the sysml resource
	 * @param class1
	 *            the Class to browse to get the ports.
	 */
	private void transformPorts(Resource eResource, Class class1) {

		// browse all port of the class
		EList<Port> ownedPorts = class1.getOwnedPorts();
		for (Port port : ownedPorts) {

			// if the port is not associated at an sub port (internal connector
			// in the browsed class), then create the ComponentPort.
			if (!hasSubPortConnected(eResource, port, port, class1)) {

				OrientationPortKind directionPort = getDirectionPort(port);
				ComponentPort cport = FaFactory.eINSTANCE.createComponentPort();
				cport.setOrientation(directionPort);
				cport.setKind(ComponentPortKind.FLOW);
				cport.setName(port.getName());

				Type type = port.getType();
				Interface inter = (Interface) MappingRulesManager.getCapellaObjectFromAllRules(
						Sysml2CapellaUtils.getSysMLID(_source.eResource(), type) + "INTERFACE");

				if (inter != null) {
					if (directionPort == OrientationPortKind.IN) {
						cport.getRequiredInterfaces().add(inter);
					} else if (directionPort == OrientationPortKind.OUT) {
						cport.getProvidedInterfaces().add(inter);
					} else {
						cport.getRequiredInterfaces().add(inter);
						cport.getProvidedInterfaces().add(inter);
					}
				}

				Sysml2CapellaUtils.trace(this, eResource, port, cport, "Port_");

				AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
				LogicalComponent lComponent = (LogicalComponent) rule.getMapSourceToTarget().get(_source);
				lComponent.getOwnedFeatures().add(cport);
			}
		}
	}

	/**
	 * Check the port has connected sub port.
	 * 
	 * @param eResource
	 *            the sysml resource
	 * @param refPort
	 *            the reference port. The "root" port
	 * @param parent
	 *            the parent port.
	 * @param class1
	 *            the class
	 * @return true if has sub port.
	 */
	private boolean hasSubPortConnected(Resource eResource, Port refPort, Port parent, Class class1) {
		Class type = class1;
		// browse all connector.
		EList<Connector> ownedConnectors = type.getOwnedConnectors();
		for (Connector connector : ownedConnectors) {
			// get the ConnectorEnds
			EList<ConnectorEnd> ends = connector.getEnds();
			ConnectorEnd connectorEnd = ends.get(0);
			Property partWithPort = connectorEnd.getPartWithPort();
			ConnectorEnd connectorEnd2 = ends.get(1);
			Property partWithPort2 = connectorEnd2.getPartWithPort();

			// check the connector is connected at the parent port. In this case
			// this is a connector of interest.
			if (!connectorEnd.getRole().equals(parent) && !connectorEnd2.getRole().equals(parent)) {
				continue;
			}

			// check source end connector
			if (hasConnectedPort(partWithPort, connectorEnd, connectorEnd2, refPort, class1, eResource)) {
				return true;
			}

			// check target end connector
			if (hasConnectedPort(partWithPort2, connectorEnd2, connectorEnd, refPort, class1, eResource)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * 
	 * Check has connected port.
	 * 
	 * @param partWithPort
	 *            the part associated at the connected port.
	 * @param connectorEnd
	 *            the source end
	 * @param connectorEnd2
	 *            the target end
	 * @param refPort
	 *            the port reference
	 * @param class1
	 *            the browsed class
	 * @param eResource
	 *            the sysml resource
	 * @return true if has sub port.
	 */
	private boolean hasConnectedPort(Property partWithPort, ConnectorEnd connectorEnd, ConnectorEnd connectorEnd2,
			Port refPort, Class class1, Resource eResource) {
		if (partWithPort == null) {
			ConnectableElement role = connectorEnd.getRole();
			EObject eContainer = role.eContainer();
			if (eContainer.equals(class1)) {
				checkHasSubPortConnectedOrFillMap(connectorEnd2, refPort, eResource);
				return true;
			}
		} else {
			Type type1 = partWithPort.getType();
			if (type1.equals(class1)) {
				checkHasSubPortConnectedOrFillMap(connectorEnd2, refPort, eResource);
				return true;
			}
		}
		return false;
	}

	/**
	 * Check the has connected sub port. If has the connected port has not sub
	 * port connected, it's put in map to link the connected port at the
	 * reference
	 * 
	 * @param connectorEnd2
	 * @param refPort
	 * @param eResource
	 */
	private void checkHasSubPortConnectedOrFillMap(ConnectorEnd connectorEnd2, Port refPort, Resource eResource) {
		if (!hasSubPortConnected(eResource, refPort, (Port) connectorEnd2.getRole(),
				(Class) connectorEnd2.getRole().eContainer())) {
			Map<String, Port> partIDTOPort = new HashMap<>();
			partIDTOPort.put(Sysml2CapellaUtils.getSysMLID(eResource, (Class) connectorEnd2.getRole().eContainer()),
					(Port) connectorEnd2.getRole());
			_mapPortToSubPort.put(refPort, partIDTOPort);
		}
	}

	/**
	 * Get the direction port. If the FlowPort is nonatomic, and the
	 * FlowSpecification typing the port has flow properties with direction
	 * �in,� the FlowPort direction is �in� (or �out� if isConjugated=true). If
	 * the flow properties are all out, the FlowPort direction is out (or in if
	 * isConjugated=true). If flow properties are both in and out, the direction
	 * is inout.
	 * 
	 * @param port
	 *            the source {@link Port}
	 * @return the {@link OrientationPortKind} direction
	 */
	private OrientationPortKind getDirectionPort(Port port) {
		Type type = port.getType();
		boolean conjugated = port.isConjugated();
		boolean isIn = false;
		boolean isOut = false;

		if (type != null) {
			for (Property prop : ((Class) type).getOwnedAttributes()) {
				Stereotype flowProperty = prop.getAppliedStereotype("SysML::Ports&Flows::FlowProperty");
				if (flowProperty != null) {
					Object val = prop.getValue(flowProperty, "direction");

					if (val instanceof EnumerationLiteral) {
						String direction = ((EnumerationLiteral) val).getName();
						if (direction != null) {
							switch (direction) {
							case "in":
								isIn = true;
								break;
							case "inout":
								isIn = true;
								isOut = true;
								break;
							case "out":
								isOut = true;
								break;
							default:
								break;
							}
						}
					}
				}
				if (isIn) {
					if (isOut) {
						return OrientationPortKind.INOUT;
					} else {
						if (conjugated) {
							return OrientationPortKind.OUT;
						} else {
							return OrientationPortKind.IN;
						}
					}
				} else {
					if (isOut) {
						if (conjugated) {
							return OrientationPortKind.IN;
						} else {
							return OrientationPortKind.OUT;
						}
					} else {
						return OrientationPortKind.INOUT;
					}
				}
			}
		}
		return OrientationPortKind.INOUT;
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

	/**
	 * Get the {@link Map} Reference port to leaf sub port.
	 * 
	 * @return {@link Map}
	 */
	public Map<Port, Map<String, Port>> getMapPortToSubPort() {
		return _mapPortToSubPort;
	}
}
