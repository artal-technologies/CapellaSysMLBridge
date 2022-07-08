/*******************************************************************************
 * Copyright (c) 2019 - 2022 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.uml;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.artal.capella.mapping.CapellaBridgeAlgo;

/**
 * @author binot
 *
 */
abstract public class UMLBridgeAlgo<SD> extends CapellaBridgeAlgo<SD> {

	List<EObject> _stereoApplication = new ArrayList<>();

	List<EObject> _profileApplication = new ArrayList<>();

	public List<EObject> getStereoApplications() {
		return _stereoApplication;
	}

	public List<EObject> getProfileApplication() {
		return _profileApplication;
	}

}
