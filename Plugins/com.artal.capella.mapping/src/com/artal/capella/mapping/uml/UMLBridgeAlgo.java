/**
 * 
 */
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
