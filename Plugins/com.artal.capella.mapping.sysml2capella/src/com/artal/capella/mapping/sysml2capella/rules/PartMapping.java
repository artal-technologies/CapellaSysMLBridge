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
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Type;
import org.polarsys.capella.common.data.modellingcore.AbstractType;
import org.polarsys.capella.core.data.cs.CsFactory;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.la.LogicalComponent;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * {@link PartMapping} allows to manage the {@link Class} from "Structure
 * 03/Product" to {@link Part} mapping.
 * 
 * @author YBI
 *
 */
public class PartMapping extends AbstractMapping {

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
	public PartMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
		super(algo);
		_source = source;
		_mappingExecution = mappingExecution;
	}

	public void computeMapping() {
		MappingRulesManager managerRules = getAlgo().getManagerRules();

		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalComponent rootLogicalSystem = CapellaUtil.getLogicalSystemRoot(targetScope.getProject());

		List<Class> classes = Sysml2CapellaUtils.getClasses(_source, "03 Structure/Product");
		for (Class class1 : classes) {
			EList<Property> ownedAttributes = class1.getOwnedAttributes();
			for (Property property : ownedAttributes) {
				Type type = property.getType();
				Object object = managerRules.getRule(ComponentMapping.class.getName()).getMapSourceToTarget().get(type);
				if (object == null) {
					continue;
				}

				Part part = CsFactory.eINSTANCE.createPart();
				if (object instanceof AbstractType) {
					part.setAbstractType((AbstractType) object);
				}
				part.setName(property.getName());
				rootLogicalSystem.getOwnedFeatures().add(part);

				Sysml2CapellaUtils.trace(this, eResource, property, part, "Part_");
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
