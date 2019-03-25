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
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Port;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
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
		EList<Port> ownedPorts = _source.getOwnedPorts();
		for (Port port : ownedPorts) {
			OrientationPortKind directionPort = getDirectionPort(port);

			ComponentPort cport = FaFactory.eINSTANCE.createComponentPort();
			cport.setOrientation(directionPort);
			cport.setKind(ComponentPortKind.FLOW);
			cport.setName(port.getName());

			Sysml2CapellaUtils.trace(this, eResource, port, cport, "Port_");

			AbstractMapping rule = MappingRulesManager.getRule(ComponentMapping.class.getName());
			LogicalComponent lComponent = (LogicalComponent) rule.getMapSourceToTarget().get(_source);
			lComponent.getOwnedFeatures().add(cport);

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
}
