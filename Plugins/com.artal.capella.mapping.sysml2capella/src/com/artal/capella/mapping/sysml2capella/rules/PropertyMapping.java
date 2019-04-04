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
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.ValueSpecification;
import org.polarsys.capella.core.data.information.InformationFactory;
import org.polarsys.capella.core.data.information.datavalue.DatavalueFactory;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * Transform the Sysml properties to Capella properties.
 * 
 * @author YBI
 *
 */
public class PropertyMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Class}.
	 */
	Class _source;
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
	 *            the {@link Class} sysml model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public PropertyMapping(Sysml2CapellaAlgo algo, Class source, IMappingExecution mappingExecution) {
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
		EList<Property> allAttributes = _source.getAllAttributes();
		transformProperties(eResource, allAttributes);

	}

	/**
	 * 
	 * Transform the SysML properties to Capella Properties.
	 * 
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param allAttributes
	 *            all the SysML properties
	 */
	private void transformProperties(Resource eResource, EList<Property> allAttributes) {
		for (Property property : allAttributes) {
			Stereotype valuePropStereotype = property.getApplicableStereotype("additional_stereotypes::ValueProperty");
			if (valuePropStereotype != null) {
				org.polarsys.capella.core.data.information.Property capellaProp = InformationFactory.eINSTANCE
						.createProperty();
				capellaProp.setName(property.getName());
				Sysml2CapellaUtils.trace(this, eResource, property, capellaProp, "PROPERTY_");

				ValueSpecification lowerValue = property.getLowerValue();
				transformMinCard(eResource, property, capellaProp, lowerValue);

				ValueSpecification upperValue = property.getUpperValue();
				transformMaxCard(eResource, property, capellaProp, upperValue);

				org.polarsys.capella.core.data.information.Class capellaObjectFromAllRules = (org.polarsys.capella.core.data.information.Class) MappingRulesManager
						.getCapellaObjectFromAllRules(_source);
				capellaObjectFromAllRules.getOwnedFeatures().add(capellaProp);
			}

		}
	}

	/**
	 * Transform the sysml max card {@link LiteralUnlimitedNatural} value to
	 * Capella max card {@link LiteralNumericValue}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param property
	 *            the sysml property
	 * @param capellaProp
	 *            the capella property
	 * @param upperValue
	 *            the {@link LiteralUnlimitedNatural} upper value
	 */
	private void transformMaxCard(Resource eResource, Property property,
			org.polarsys.capella.core.data.information.Property capellaProp, ValueSpecification upperValue) {
		if (upperValue != null) {
			if (upperValue instanceof LiteralUnlimitedNatural) {
				int value = ((LiteralUnlimitedNatural) upperValue).getValue();
				LiteralNumericValue capellaMaxCard = DatavalueFactory.eINSTANCE.createLiteralNumericValue();
				capellaMaxCard.setName(upperValue.getName());
				capellaMaxCard.setValue(value == -1 ? "*" : value + "");
				capellaProp.setOwnedMaxCard(capellaMaxCard);
				Sysml2CapellaUtils.trace(this, eResource, upperValue, capellaMaxCard, "MAXCARD_");
			}
		} else {
			LiteralNumericValue capellaMaxCard = DatavalueFactory.eINSTANCE.createLiteralNumericValue();
			capellaMaxCard.setValue(property.getUpper() == -1 ? "*" : property.getUpper() + "");
			capellaProp.setOwnedMaxCard(capellaMaxCard);
			Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, property) + "MAXCARD",
					capellaMaxCard, "MAXCARD_");
		}
	}

	/**
	 * Transform the sysml min card {@link LiteralInteger} value to Capella min
	 * card {@link LiteralNumericValue}
	 * 
	 * @param eResource
	 *            the sysml model
	 * @param property
	 *            the sysml property
	 * @param capellaProp
	 *            the capella property
	 * @param upperValue
	 *            the {@link LiteralUnlimitedNatural} lower value
	 */
	private void transformMinCard(Resource eResource, Property property,
			org.polarsys.capella.core.data.information.Property capellaProp, ValueSpecification lowerValue) {
		if (lowerValue != null) {
			if (lowerValue instanceof LiteralInteger) {
				int value = ((LiteralInteger) lowerValue).getValue();
				LiteralNumericValue capellaMinCard = DatavalueFactory.eINSTANCE.createLiteralNumericValue();
				capellaMinCard.setName(lowerValue.getName());
				capellaMinCard.setValue(value + "");
				capellaProp.setOwnedMinCard(capellaMinCard);
				Sysml2CapellaUtils.trace(this, eResource, lowerValue, capellaMinCard, "MINCARD_");
			}
		} else {
			LiteralNumericValue capellaMinCard = DatavalueFactory.eINSTANCE.createLiteralNumericValue();
			capellaMinCard.setValue(property.getLower() + "");
			capellaProp.setOwnedMinCard(capellaMinCard);
			Sysml2CapellaUtils.trace(this, eResource, Sysml2CapellaUtils.getSysMLID(eResource, property) + "MINCARD",
					capellaMinCard, "MINCARD_");
		}
	}

}
