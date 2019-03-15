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

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.uml2.uml.Model;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.rules.ActorMapping;
import com.artal.capella.mapping.sysml2capella.rules.AssociationActorUseCaseMapping;
import com.artal.capella.mapping.sysml2capella.rules.ComponentMapping;
import com.artal.capella.mapping.sysml2capella.rules.ConnectorMapping;
import com.artal.capella.mapping.sysml2capella.rules.LogicalFunctionMapping;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.artal.capella.mapping.CapellaBridgeAlgo#launch()
	 */
	@Override
	public void launch(Model source, IMappingExecution mappingExecution_p) {

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

		LogicalFunctionMapping functionMapping = new LogicalFunctionMapping(this, source, mappingExecution_p);
		_managerRules.add(functionMapping.getClass().getName(), functionMapping);

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

}
