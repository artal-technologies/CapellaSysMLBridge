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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.EnumerationLiteral;
import org.eclipse.uml2.uml.LiteralBoolean;
import org.eclipse.uml2.uml.LiteralInteger;
import org.eclipse.uml2.uml.LiteralReal;
import org.eclipse.uml2.uml.LiteralSpecification;
import org.eclipse.uml2.uml.LiteralString;
import org.eclipse.uml2.uml.LiteralUnlimitedNatural;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.ValueSpecification;
import org.polarsys.capella.common.data.modellingcore.AbstractType;
import org.polarsys.capella.common.data.modellingcore.AbstractTypedElement;
import org.polarsys.capella.core.data.capellacore.Classifier;
import org.polarsys.capella.core.data.capellacore.Feature;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.information.ExchangeItem;
import org.polarsys.capella.core.data.information.ExchangeItemElement;
import org.polarsys.capella.core.data.information.InformationFactory;
import org.polarsys.capella.core.data.information.MultiplicityElement;
import org.polarsys.capella.core.data.information.ParameterDirection;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.DatavalueFactory;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.la.LogicalComponent;

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
	org.eclipse.uml2.uml.Classifier _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;
	/**
	 * A {@link MappingRulesManager} allowing to manage the sub rules.
	 */
	MappingRulesManager _manager = new MappingRulesManager();

	public static List<Property> contraintsProperties = new ArrayList<>();

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
	public PropertyMapping(Sysml2CapellaAlgo algo, org.eclipse.uml2.uml.Classifier source,
			IMappingExecution mappingExecution) {
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
		CapellaUpdateScope scope = _mappingExecution.getTargetDataSet();
		for (Property property : allAttributes) {
			Stereotype constraintPropStereotype = property
					.getAppliedStereotype("additional_stereotypes::deprecated elements::ConstraintProperty");
			if (constraintPropStereotype != null) {
				contraintsProperties.add(property);
				continue;
			}

			Stereotype valuePropStereotype = property.getAppliedStereotype("additional_stereotypes::ValueProperty");
			Stereotype flowStereotype = property.getAppliedStereotype("SysML::Ports&Flows::FlowProperty");
			if (valuePropStereotype != null || flowStereotype != null) {
				NamedElement capellaObjectFromAllRules = (NamedElement) MappingRulesManager
						.getCapellaObjectFromAllRules(_source);
				NamedElement capellaProp = null;
				if (capellaObjectFromAllRules instanceof org.polarsys.capella.core.data.information.Class) {
					capellaProp = InformationFactory.eINSTANCE.createProperty();
				} else if (capellaObjectFromAllRules instanceof ExchangeItem) {
					capellaProp = InformationFactory.eINSTANCE.createExchangeItemElement();
				} else if (capellaObjectFromAllRules instanceof LogicalComponent) {
					capellaProp = InformationFactory.eINSTANCE.createProperty();
				}

				if (capellaProp != null) {
					capellaProp.setName(property.getName());
					Sysml2CapellaUtils.trace(this, eResource, property, capellaProp, "PROPERTY_");

					if (capellaProp instanceof MultiplicityElement) {
						ValueSpecification lowerValue = property.getLowerValue();
						transformMinCard(eResource, property, (MultiplicityElement) capellaProp, lowerValue);

						ValueSpecification upperValue = property.getUpperValue();
						transformMaxCard(eResource, property, (MultiplicityElement) capellaProp, upperValue);

						ValueSpecification defaultValue = property.getDefaultValue();
						if (defaultValue != null) {
							transformDefault(eResource, property, (MultiplicityElement) capellaProp, defaultValue);
						}
					}
				}

				if (capellaProp instanceof Feature && capellaObjectFromAllRules instanceof Classifier) {
					((Classifier) capellaObjectFromAllRules).getOwnedFeatures().add((Feature) capellaProp);
				} else if (capellaProp instanceof ExchangeItemElement
						&& capellaObjectFromAllRules instanceof ExchangeItem) {
					((ExchangeItem) capellaObjectFromAllRules).getOwnedElements()
							.add((ExchangeItemElement) capellaProp);
					Stereotype flowPropertyStereotype = property
							.getAppliedStereotype("SysML::Ports&Flows::FlowProperty");
					if (flowPropertyStereotype != null) {
						Object value = property.getValue(flowPropertyStereotype, "direction");
						if (value instanceof EnumerationLiteral) {
							// TODO this implementation seems not valid in case
							// of Flow ExchangeItem. It's valid for Operation
							// ExchangeItem
							String direction = ((EnumerationLiteral) value).getName();
							if (direction != null) {
								switch (direction) {
								case "in":
									((ExchangeItemElement) capellaProp).setDirection(ParameterDirection.IN);
									break;
								case "inout":
									((ExchangeItemElement) capellaProp).setDirection(ParameterDirection.INOUT);
									break;
								case "out":
									((ExchangeItemElement) capellaProp).setDirection(ParameterDirection.OUT);
									break;
								default:
									break;
								}
							}
						}
					}
				}

				if (capellaProp instanceof AbstractTypedElement) {

					transformType(scope, property, (AbstractTypedElement) capellaProp);
				}
			} else if (_source instanceof DataType) {
				NamedElement capellaObjectFromAllRules = (NamedElement) MappingRulesManager
						.getCapellaObjectFromAllRules(_source);
				NamedElement capellaProp = null;

				capellaProp = InformationFactory.eINSTANCE.createProperty();

				capellaProp.setName(property.getName());
				Sysml2CapellaUtils.trace(this, eResource, property, capellaProp, "PROPERTY_");

				if (capellaProp instanceof MultiplicityElement) {
					ValueSpecification lowerValue = property.getLowerValue();
					transformMinCard(eResource, property, (MultiplicityElement) capellaProp, lowerValue);

					ValueSpecification upperValue = property.getUpperValue();
					transformMaxCard(eResource, property, (MultiplicityElement) capellaProp, upperValue);

					ValueSpecification defaultValue = property.getDefaultValue();
					if (defaultValue != null) {
						transformDefault(eResource, property, (MultiplicityElement) capellaProp, defaultValue);
					}
				}
				((Classifier) capellaObjectFromAllRules).getOwnedFeatures().add((Feature) capellaProp);
				if (capellaProp instanceof AbstractTypedElement) {
					transformType(scope, property, (AbstractTypedElement) capellaProp);
				}
			}
		}
	}

	/**
	 * Transform the {@link Property} {@link Type} to
	 * {@link AbstractTypedElement} {@link AbstractType}
	 * 
	 * @param scope
	 *            the mapping scope
	 * @param property
	 *            the SysML/UML {@link Property}
	 * @param capellaProp
	 *            the Capella {@link AbstractTypedElement}
	 */
	private void transformType(CapellaUpdateScope scope, Property property, AbstractTypedElement capellaProp) {
		Type type = property.getType();
		if (type != null) {
			// Stereotype appliedStereotype =
			// type.getAppliedStereotype("SysML::Blocks::ValueType");
			Object capellaObjectFromAllRules2 = null;
			if (type instanceof PrimitiveType) {
				capellaObjectFromAllRules2 = Sysml2CapellaUtils.getPrimitiveType((PrimitiveType) type,
						scope.getProject());
			} else {
				capellaObjectFromAllRules2 = MappingRulesManager.getCapellaObjectFromAllRules(type);
			}
			if (capellaObjectFromAllRules2 instanceof AbstractType) {
				((AbstractTypedElement) capellaProp).setAbstractType((AbstractType) capellaObjectFromAllRules2);
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
	private void transformMaxCard(Resource eResource, Property property, MultiplicityElement capellaProp,
			ValueSpecification upperValue) {
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
	 * Transform the default value.
	 * 
	 * @param eResource
	 *            the sysml resource
	 * @param property
	 *            the sysml property
	 * @param capellaProp
	 *            the capella property
	 * @param defaultValue
	 *            the default value to transform
	 */
	private void transformDefault(Resource eResource, Property property, MultiplicityElement capellaProp,
			ValueSpecification defaultValue) {
		if (defaultValue != null) {
			if (defaultValue instanceof LiteralSpecification) {
				DataValue capellaDefault = createCapellaDataValue((LiteralSpecification) defaultValue, capellaProp,
						defaultValue);
				Sysml2CapellaUtils.trace(this, eResource, defaultValue, capellaDefault, "DEFAULTCARD_");
			}
		}
	}

	/**
	 * Create new Capella {@link DataValue}
	 * 
	 * @param ls
	 * @param capellaProp
	 * @param defaultValue
	 * @return
	 */
	private DataValue createCapellaDataValue(LiteralSpecification ls, MultiplicityElement capellaProp,
			ValueSpecification defaultValue) {
		if (ls instanceof LiteralUnlimitedNatural) {
			LiteralNumericValue capellaDefault = DatavalueFactory.eINSTANCE.createLiteralNumericValue();
			capellaDefault.setName(defaultValue.getName());
			capellaDefault.setValue(((LiteralUnlimitedNatural) ls).getValue() + "");
			capellaProp.setOwnedDefaultValue(capellaDefault);

			return capellaDefault;
		}
		if (ls instanceof LiteralBoolean) {

			LiteralBooleanValue capellaDefault = DatavalueFactory.eINSTANCE.createLiteralBooleanValue();
			capellaDefault.setName(defaultValue.getName());
			capellaDefault.setValue(((LiteralBoolean) ls).isValue());
			capellaProp.setOwnedDefaultValue(capellaDefault);

			return capellaDefault;

		}
		if (ls instanceof LiteralInteger) {
			LiteralNumericValue capellaDefault = DatavalueFactory.eINSTANCE.createLiteralNumericValue();
			capellaDefault.setName(defaultValue.getName());
			capellaDefault.setValue(((LiteralInteger) ls).getValue() + "");
			capellaProp.setOwnedDefaultValue(capellaDefault);
			return capellaDefault;
		}
		if (ls instanceof LiteralReal) {
			LiteralNumericValue capellaDefault = DatavalueFactory.eINSTANCE.createLiteralNumericValue();
			capellaDefault.setName(defaultValue.getName());
			capellaDefault.setValue(((LiteralReal) ls).getValue() + "");
			capellaProp.setOwnedDefaultValue(capellaDefault);
			return capellaDefault;
		}
		if (ls instanceof LiteralString) {
			LiteralStringValue capellaDefault = DatavalueFactory.eINSTANCE.createLiteralStringValue();
			capellaDefault.setName(defaultValue.getName());
			capellaDefault.setValue(((LiteralString) ls).getValue() + "");
			capellaProp.setOwnedDefaultValue(capellaDefault);
			return capellaDefault;
		}
		return null;
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
	private void transformMinCard(Resource eResource, Property property, MultiplicityElement capellaProp,
			ValueSpecification lowerValue) {
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
