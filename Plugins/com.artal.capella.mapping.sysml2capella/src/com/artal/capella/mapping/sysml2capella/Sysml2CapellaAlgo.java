/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package com.artal.capella.mapping.sysml2capella;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.Stereotype;
import org.eclipse.uml2.uml.UMLPackage;
import org.polarsys.capella.common.helpers.EObjectExt;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.preferences.SysMLConfiguration;
import com.artal.capella.mapping.sysml2capella.rules.ActorMapping;
import com.artal.capella.mapping.sysml2capella.rules.AssociationActorUseCaseMapping;
import com.artal.capella.mapping.sysml2capella.rules.ComponentMapping;
import com.artal.capella.mapping.sysml2capella.rules.ConnectorMapping;
import com.artal.capella.mapping.sysml2capella.rules.ConstraintsMapping;
import com.artal.capella.mapping.sysml2capella.rules.FunctionalArchitectureMapping;
import com.artal.capella.mapping.sysml2capella.rules.PartMapping;
import com.artal.capella.mapping.sysml2capella.rules.UseCaseMapping;

/**
 * 
 * {@link Sysml2CapellaAlgo} implements the {@link CapellaBridgeAlgo}. This
 * implementation allows to manage the Sysml to Capella transformation.
 * 
 * @author YBI
 *
 */
public class Sysml2CapellaAlgo extends CapellaBridgeAlgo<Model> {

	MappingRulesManager _managerRules = new MappingRulesManager();

	/**
	 * configuration provides the package where get the sysml data.
	 */
	private SysMLConfiguration _configuration;

	/**
	 * Default constructor
	 */
	public Sysml2CapellaAlgo() {

	}

	/**
	 * Constructor.
	 * 
	 * @param configuration
	 *            provides the package where get the sysml data.
	 */
	public Sysml2CapellaAlgo(SysMLConfiguration configuration) {
		_configuration = configuration;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.CapellaBridgeAlgo#launch()
	 */
	@Override
	public void launch(Model source, IMappingExecution mappingExecution_p) {

		if (_configuration == null) {
			// default value
			_configuration = new SysMLConfiguration();
		}

		// manage components mapping.
		ComponentMapping componentMapping = new ComponentMapping(this, source, mappingExecution_p);
		_managerRules.add(componentMapping.getClass().getName(), componentMapping);

		PartMapping partMapping = new PartMapping(this, source, mappingExecution_p);
		_managerRules.add(partMapping.getClass().getName(), partMapping);

		ConnectorMapping connectorMapping = new ConnectorMapping(this, source, mappingExecution_p);
		_managerRules.add(connectorMapping.getClass().getName(), connectorMapping);

		ActorMapping actorMapping = new ActorMapping(this, source, mappingExecution_p);
		_managerRules.add(actorMapping.getClass().getName(), actorMapping);

		UseCaseMapping useCaseMapping = new UseCaseMapping(this, source, mappingExecution_p);
		_managerRules.add(useCaseMapping.getClass().getName(), useCaseMapping);

		AssociationActorUseCaseMapping associationActorUseCaseMapping = new AssociationActorUseCaseMapping(this, source,
				mappingExecution_p);
		_managerRules.add(associationActorUseCaseMapping.getClass().getName(), associationActorUseCaseMapping);

		FunctionalArchitectureMapping functionMapping = new FunctionalArchitectureMapping(this, source,
				mappingExecution_p);
		_managerRules.add(functionMapping.getClass().getName(), functionMapping);

		// add constraints rule for all rules in the SysML model
		ConstraintsMapping constraintsMapping = new ConstraintsMapping(this, source, mappingExecution_p) {
			@Override
			public List<Constraint> getInput() {
				EList<PackageableElement> packagedElements = source.getPackagedElements();
				List<Constraint> constraintClassses = new ArrayList<Constraint>();
				for (PackageableElement eObject : packagedElements) {
					if (eObject instanceof Package) {
						Stereotype appliedStereotype = ((Package) eObject)
								.getAppliedStereotype("MagicDraw Profile::auxiliaryResource");
						if (appliedStereotype == null) {
							Set<EObject> all = EObjectExt.getAll(eObject, UMLPackage.Literals.CONSTRAINT);
							for (EObject eObject2 : all) {
								if (eObject2 instanceof Constraint) {
									constraintClassses.add((Constraint) eObject2);
								}
							}
						}
					}
				}
				EList<Constraint> ownedRules = source.getOwnedRules();
				constraintClassses.addAll(ownedRules);
				return constraintClassses;
			}
		};
		_managerRules.add(constraintsMapping.getClass().getName(), constraintsMapping);
		// execute rules
		_managerRules.executeRules();

	}

	/**
	 * Get the manager allowing to manage the rules.
	 * 
	 * @return the {@link MappingRulesManager} manager
	 */
	public MappingRulesManager getManagerRules() {
		return _managerRules;
	}

	/**
	 * Get the {@link SysMLConfiguration} configuration.
	 * 
	 * @return <code>_configuration</code>
	 */
	public SysMLConfiguration getConfiguration() {
		return _configuration;
	}

}
