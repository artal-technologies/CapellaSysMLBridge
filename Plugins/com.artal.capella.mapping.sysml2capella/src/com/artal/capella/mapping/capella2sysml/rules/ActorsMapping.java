/*******************************************************************************
 * Copyright (c) 2019 - 2022 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.capella2sysml.rules;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.UMLFactory;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalComponentPkg;

import com.artal.capella.mapping.CapellaBridgeAlgo;
import com.artal.capella.mapping.capella2sysml.Capella2SysmlAlgo;
import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.rules.MappingRulesManager;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;;

/**
 * @author YBI
 *
 */
public class ActorsMapping extends AbstractMapping {

	/**
	 * The Capella element source.
	 */
	Project _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * @param algo
	 *            the {@link Capella2SysmlAlgo}
	 * @param source_p
	 *            the capella {@link Project} model.
	 * @param mappingExecution
	 *            the {@link IMappingExecution} allows to get the mapping data.
	 */
	public ActorsMapping(CapellaBridgeAlgo<?> algo, Project source_p, IMappingExecution mappingExecution) {
		super(algo);
		_source = source_p;
		_mappingExecution = mappingExecution;
	}

	/* (non-Javadoc)
	 * @see com.artal.capella.mapping.rules.AbstractMapping#computeMapping()
	 */
	@Override
	public void computeMapping() {

		LogicalComponentPkg logicalActorPkg = Sysml2CapellaUtils.getLogicalActorPkg(_source);
		List<LogicalComponent> ownedLogicalActors = logicalActorPkg.getOwnedLogicalComponents().stream().map(sel->(LogicalComponent) sel).filter(sel-> sel.isActor()).collect((Collectors.toList()));

		Package useCasePkg = (Package) MappingRulesManager.getCapellaObjectFromAllRules(_source + "USECASES");

		for (LogicalComponent logicalActor : ownedLogicalActors) {
			if (!logicalActor.getName().equals("Generic Actor")) {
				Actor actor = UMLFactory.eINSTANCE.createActor();
				actor.setName(logicalActor.getName());

				useCasePkg.getPackagedElements().add(actor);
				Sysml2CapellaUtils.trace(this, _source.eResource(), logicalActor, actor, "ACTOR_");
			}
		}
	}

}
