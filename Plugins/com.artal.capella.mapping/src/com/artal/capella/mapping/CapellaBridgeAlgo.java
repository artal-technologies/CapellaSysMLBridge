/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.diffmerge.bridge.mapping.api.IMappingExecution;
import org.eclipse.emf.ecore.EObject;

import com.artal.capella.mapping.cheat.TraceCheat;

/**
 * Abstract class to implement to manage the specific mapping.
 * 
 * @author YBI
 * 
 *         DO NOT FORGET TO CALL ADD/ATTACH METHOD on created objects
 */
public abstract class CapellaBridgeAlgo<SD> {
	private List<TraceCheat<? extends EObject>> _allItems = new ArrayList<>();

	private List<EObject> _transientItems = new ArrayList<>();

	/**
	 * Launch the execution of the algorithm
	 * 
	 * DO NOT FORGET TO CALL ADD/ATTACH METHOD on created objects
	 * 
	 * @param _capellaMappingExecution
	 */
	public abstract void launch(SD source_p, IMappingExecution _mappingExecution);

	/**
	 * Indicate that the given item was created during the algo
	 * 
	 * @param unique
	 *            identifier of the created item (must be stable throw calls)
	 * @param item
	 *            the created EObject
	 */
	public void add(String guid, EObject item) {
		_allItems.add(new TraceCheat<EObject>(guid, item));
	}

	/**
	 * Indicate that the given item was created during the algo and is not
	 * contained by any parent ("add" method automatically called)
	 * 
	 * @param unique
	 *            identifier of the created item (must be stable throw calls)
	 * @param item
	 *            the created EObject
	 */
	public void attach(String guid, EObject item) {
		add(guid, item);
		_transientItems.add(item);
	}

	/**
	 * Just "attach" the given item. The method "add" must also be called on
	 * this item.
	 * 
	 * @param item
	 *            EObject to attach
	 */
	protected void attachOnly(EObject item) {
		_transientItems.add(item);
	}

	/**
	 * Get ALL created objects
	 * 
	 * @return the list of ALL created objects
	 */
	public List<TraceCheat<? extends EObject>> getAllItems() {
		return _allItems;
	}

	/**
	 * Get all created objects that are not attached to any parent
	 * 
	 * @return a List of items
	 */
	public List<EObject> getTransientItems() {
		return _transientItems;
	}

	

}
