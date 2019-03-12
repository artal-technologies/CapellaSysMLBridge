/*******************************************************************************
 * Copyright (c) 2019 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;

import com.artal.capella.mapping.rules.AbstractMapping;

/**
 * {@link Sysml2CapellaUtils} provides methods to manage sysml to capella
 * mapping common behaviors.
 * 
 * @author YBI
 *
 */
public class Sysml2CapellaUtils {

	/**
	 * Get the classes from target uml package
	 * 
	 * @param source
	 *            the container package
	 * @param paths
	 *            the path to the package target. the format is
	 *            "Pkg1/Pkg2/../Pkg2" or "Pkg1/Pkg2/../Pkg2/TargetClass" and
	 *            represents the path from source (source/<code>paths</code>).
	 * @return the Class {@link List}
	 */
	static public List<Class> getClasses(Package source, String paths) {
		List<Class> results = new ArrayList<>();

		String[] pathsArray = paths.split("/");
		paths = paths.substring(paths.indexOf("/") + 1, paths.length());
		String packageName = pathsArray[0];
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals(packageName)) {
				if (pathsArray.length > 1) {
					results.addAll(getClasses((Package) structurePkg, paths));
				} else {
					EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();
					for (PackageableElement packageableElement : packagedElements2) {
						if (packageableElement instanceof Class) {
							results.add((Class) packageableElement);
						}
					}
				}
			} else if (structurePkg instanceof Class && structurePkg.getName().equals(packageName)) {
				results.add((Class) structurePkg);
			}
		}
		return results;

	}

	/**
	 * Get the xmi id for a {@link EObject} <code>object</code>> element.
	 * 
	 * @param resource
	 *            The Sysml {@link Resource}.
	 * @param object
	 *            the element to identify
	 * @return the String id
	 */
	static public String getSysMLID(Resource resource, EObject object) {
		if (resource instanceof XMIResource) {
			return ((XMIResource) resource).getID(object);
		}
		return "";
	}

	/**
	 * Allows to trace the created target element.
	 * 
	 * @param rule
	 *            the creation rule.
	 * @param eResource
	 *            the {@link Resource}
	 * @param sourceElement
	 *            the sysml element.
	 * @param targetElement
	 *            the capella element
	 * @param prefix
	 *            a prefix to add for identification.
	 */
	static public void trace(AbstractMapping rule, Resource eResource, EObject sourceElement, EObject targetElement,
			String prefix) {
		String id = Sysml2CapellaUtils.getSysMLID(eResource, sourceElement);
		rule.getAlgo().add(prefix + id, targetElement);
		rule.getMapSourceToTarget().put(sourceElement, targetElement);
	}

}
