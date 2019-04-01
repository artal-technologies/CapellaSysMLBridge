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
import java.util.List;
import java.util.Map;

import org.eclipse.emf.diffmerge.bridge.capella.integration.scopes.CapellaUpdateScope;
import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Model;
import org.polarsys.capella.core.data.cs.CsFactory;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.la.LaFactory;
import org.polarsys.capella.core.data.la.LogicalActor;
import org.polarsys.capella.core.data.la.LogicalActorPkg;
import org.polarsys.capella.core.data.la.LogicalContext;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.sysml2capella.Sysml2CapellaAlgo;
import com.artal.capella.mapping.sysml2capella.utils.Sysml2CapellaUtils;

/**
 * @author YBI
 *
 */
public class ActorMapping extends AbstractMapping {

	/**
	 * The sysml root {@link Model}.
	 */
	Model _source;
	/**
	 * the {@link IMappingExecution} allows to get the mapping data.
	 */
	IMappingExecution _mappingExecution;

	/**
	 * Map to find Part for an Actor
	 */
	Map<LogicalActor, Part> _mapLAtoPart = new HashMap<LogicalActor, Part>();

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
	public ActorMapping(Sysml2CapellaAlgo algo, Model source, IMappingExecution mappingExecution) {
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
		List<Actor> actors = Sysml2CapellaUtils.getActors(_source, getAlgo().getConfiguration().getUseCasesPath());
		Resource eResource = _source.eResource();
		CapellaUpdateScope targetScope = _mappingExecution.getTargetDataSet();
		LogicalActorPkg logicalActorPkg = Sysml2CapellaUtils.getLogicalActorPkg(targetScope.getProject());
		LogicalContext logicalContext = Sysml2CapellaUtils.getLogicalContext(targetScope.getProject());

		for (Actor actor : actors) {
			LogicalActor lActor = LaFactory.eINSTANCE.createLogicalActor();
			lActor.setName(actor.getName());

			logicalActorPkg.getOwnedLogicalActors().add(lActor);
			Sysml2CapellaUtils.trace(this, eResource, actor, lActor, "LogicalActor_");

			Part partActor = CsFactory.eINSTANCE.createPart();
			partActor.setAbstractType(lActor);
			partActor.setName(lActor.getName());
			logicalContext.getOwnedFeatures().add(partActor);
			Sysml2CapellaUtils.trace(this, eResource, actor, partActor, "LogicalActor_Part_");

			_mapLAtoPart.put(lActor, partActor);

		}

	}

	@Override
	public Sysml2CapellaAlgo getAlgo() {
		return (Sysml2CapellaAlgo)super.getAlgo();
	}
	
}
