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
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.UseCase;
import org.polarsys.capella.core.data.capellacommon.AbstractCapabilityPkg;
import org.polarsys.capella.core.data.capellamodeller.ModelRoot;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.capellamodeller.SystemEngineering;
import org.polarsys.capella.core.data.la.CapabilityRealizationPkg;
import org.polarsys.capella.core.data.la.LogicalActorPkg;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.core.data.la.LogicalContext;

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
	 * Get the Actors from target uml package
	 * 
	 * @param source
	 *            the container package
	 * @param paths
	 *            the path to the package target. the format is
	 *            "Pkg1/Pkg2/../Pkg2" or "Pkg1/Pkg2/../Pkg2/TargetActor" and
	 *            represents the path from source (source/<code>paths</code>).
	 * @return the Class {@link List}
	 */
	static public List<Actor> getActors(Package source, String paths) {
		List<Actor> results = new ArrayList<>();

		String[] pathsArray = paths.split("/");
		paths = paths.substring(paths.indexOf("/") + 1, paths.length());
		String packageName = pathsArray[0];
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals(packageName)) {
				if (pathsArray.length > 1) {
					results.addAll(getActors((Package) structurePkg, paths));
				} else {
					EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();
					for (PackageableElement packageableElement : packagedElements2) {
						if (packageableElement instanceof Actor) {
							results.add((Actor) packageableElement);
						}
					}
				}
			} else if (structurePkg instanceof Actor && structurePkg.getName().equals(packageName)) {
				results.add((Actor) structurePkg);
			}
		}
		return results;
	}

	/**
	 * Get the UseCase from target uml package
	 * 
	 * @param source
	 *            the container package
	 * @param paths
	 *            the path to the package target. the format is
	 *            "Pkg1/Pkg2/../Pkg2" or "Pkg1/Pkg2/../Pkg2/TargetActor" and
	 *            represents the path from source (source/<code>paths</code>).
	 * @return the Class {@link List}
	 */
	static public List<UseCase> getUseCases(Package source, String paths) {
		List<UseCase> results = new ArrayList<>();

		String[] pathsArray = paths.split("/");
		paths = paths.substring(paths.indexOf("/") + 1, paths.length());
		String packageName = pathsArray[0];
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals(packageName)) {
				if (pathsArray.length > 1) {
					results.addAll(getUseCases((Package) structurePkg, paths));
				} else {
					EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();
					for (PackageableElement packageableElement : packagedElements2) {
						if (packageableElement instanceof UseCase) {
							results.add((UseCase) packageableElement);
						}
					}
				}
			} else if (structurePkg instanceof UseCase && structurePkg.getName().equals(packageName)) {
				results.add((UseCase) structurePkg);
			}
		}
		return results;
	}

	/**
	 * Get the Association from target uml package
	 * 
	 * @param source
	 *            the container package
	 * @param paths
	 *            the path to the package target. the format is
	 *            "Pkg1/Pkg2/../Pkg2" or "Pkg1/Pkg2/../Pkg2/TargetActor" and
	 *            represents the path from source (source/<code>paths</code>).
	 * @return the Class {@link List}
	 */
	static public List<Association> getAssociations(Package source, String paths) {
		List<Association> results = new ArrayList<>();

		String[] pathsArray = paths.split("/");
		paths = paths.substring(paths.indexOf("/") + 1, paths.length());
		String packageName = pathsArray[0];
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals(packageName)) {
				if (pathsArray.length > 1) {
					results.addAll(getAssociations((Package) structurePkg, paths));
				} else {
					EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();
					for (PackageableElement packageableElement : packagedElements2) {
						if (packageableElement instanceof Association) {
							results.add((Association) packageableElement);
						}
					}
				}
			} else if (structurePkg instanceof Association && structurePkg.getName().equals(packageName)) {
				results.add((Association) structurePkg);
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
		if (rule.getMapSourceToTarget().containsKey(sourceElement)) {
			Object object = rule.getMapSourceToTarget().get(sourceElement);
			if (object instanceof List<?>) {
				List<Object> list = (List<Object>) object;
				list.add(targetElement);
			} else {
				List<Object> list = new ArrayList<>();
				list.add(object);
				list.add(targetElement);
				rule.getMapSourceToTarget().put(sourceElement, list);
			}
		} else {
			rule.getMapSourceToTarget().put(sourceElement, targetElement);
		}
	}

	/**
	 * Returns the logical component system root given a semantic object
	 * 
	 * @param source_p
	 *            the semantic object
	 * @return the logical component root
	 */
	public static LogicalActorPkg getLogicalActorPkg(EObject source_p) {
		ResourceSet resourceSet = source_p.eResource().getResourceSet();
		URI semanticResourceURI = source_p.eResource().getURI().trimFileExtension()
				.appendFileExtension("melodymodeller");
		Resource semanticResource = resourceSet.getResource(semanticResourceURI, false);
		if (semanticResource != null) {
			EObject root = semanticResource.getContents().get(0);
			if (root instanceof Project) {
				EList<ModelRoot> ownedModelRoots = ((Project) root).getOwnedModelRoots();
				for (ModelRoot modelRoot : ownedModelRoots) {
					if (modelRoot instanceof SystemEngineering) {
						EList<LogicalArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getContainedLogicalArchitectures();
						for (LogicalArchitecture arch : containedLogicalArchitecture) {
							return arch.getOwnedLogicalActorPkg();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the logical component system root given a semantic object
	 * 
	 * @param source_p
	 *            the semantic object
	 * @return the logical component root
	 */
	public static LogicalContext getLogicalContext(EObject source_p) {
		ResourceSet resourceSet = source_p.eResource().getResourceSet();
		URI semanticResourceURI = source_p.eResource().getURI().trimFileExtension()
				.appendFileExtension("melodymodeller");
		Resource semanticResource = resourceSet.getResource(semanticResourceURI, false);
		if (semanticResource != null) {
			EObject root = semanticResource.getContents().get(0);
			if (root instanceof Project) {
				EList<ModelRoot> ownedModelRoots = ((Project) root).getOwnedModelRoots();
				for (ModelRoot modelRoot : ownedModelRoots) {
					if (modelRoot instanceof SystemEngineering) {
						EList<LogicalArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getContainedLogicalArchitectures();
						for (LogicalArchitecture arch : containedLogicalArchitecture) {
							return arch.getOwnedLogicalContext();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the logical component system root given a semantic object
	 * 
	 * @param source_p
	 *            the semantic object
	 * @return the logical component root
	 */
	public static AbstractCapabilityPkg getCapabilityRealizationPkg(EObject source_p) {
		ResourceSet resourceSet = source_p.eResource().getResourceSet();
		URI semanticResourceURI = source_p.eResource().getURI().trimFileExtension()
				.appendFileExtension("melodymodeller");
		Resource semanticResource = resourceSet.getResource(semanticResourceURI, false);
		if (semanticResource != null) {
			EObject root = semanticResource.getContents().get(0);
			if (root instanceof Project) {
				EList<ModelRoot> ownedModelRoots = ((Project) root).getOwnedModelRoots();
				for (ModelRoot modelRoot : ownedModelRoots) {
					if (modelRoot instanceof SystemEngineering) {
						EList<LogicalArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getContainedLogicalArchitectures();
						for (LogicalArchitecture arch : containedLogicalArchitecture) {
							return arch.getOwnedAbstractCapabilityPkg();
						}
					}
				}
			}
		}
		return null;
	}

}
