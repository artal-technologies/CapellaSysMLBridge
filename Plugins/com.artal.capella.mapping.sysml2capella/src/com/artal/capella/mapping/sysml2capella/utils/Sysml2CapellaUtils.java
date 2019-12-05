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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.api.scopes.IModelScope;
import org.eclipse.emf.diffmerge.bridge.incremental.IntermediateModelScope;
import org.eclipse.emf.diffmerge.impl.scopes.FragmentedModelScope;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.ECrossReferenceAdapter;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.uml2.uml.Abstraction;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Class;
import org.eclipse.uml2.uml.Classifier;
import org.eclipse.uml2.uml.DataType;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.PackageableElement;
import org.eclipse.uml2.uml.PrimitiveType;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UseCase;
import org.polarsys.capella.common.data.modellingcore.AbstractType;
import org.polarsys.capella.common.helpers.EObjectExt;
import org.polarsys.capella.core.data.capellacommon.AbstractCapabilityPkg;
import org.polarsys.capella.core.data.capellacore.ModellingArchitecture;
import org.polarsys.capella.core.data.capellamodeller.ModelRoot;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.capellamodeller.SystemEngineering;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.cs.InterfacePkg;
import org.polarsys.capella.core.data.cs.Part;
import org.polarsys.capella.core.data.ctx.SystemAnalysis;
import org.polarsys.capella.core.data.fa.ComponentExchange;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.fa.FunctionPkg;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.la.LogicalActorPkg;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.la.LogicalContext;
import org.polarsys.capella.core.data.la.LogicalFunction;
import org.polarsys.capella.core.data.la.LogicalFunctionPkg;

import com.artal.capella.mapping.rules.AbstractMapping;
import com.artal.capella.mapping.sysml2capella.utils.SysML2CapellaUMLProfile.UMLProfile;

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

	/*
	 * Get the datatype from target uml package
	 * 
	 * @param source the container package
	 * 
	 * @param paths the path to the package target. the format is
	 * "Pkg1/Pkg2/../Pkg2" or "Pkg1/Pkg2/../Pkg2/TargetClass" and represents the
	 * path from source (source/<code>paths</code>).
	 * 
	 * @return the Class {@link List}
	 */
	static public List<DataType> getDatatTypes(Package source, String paths) {
		List<DataType> results = new ArrayList<>();

		String[] pathsArray = paths.split("/");
		paths = paths.substring(paths.indexOf("/") + 1, paths.length());
		String packageName = pathsArray[0];
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals(packageName)) {
				if (pathsArray.length > 1) {
					results.addAll(getDatatTypes((Package) structurePkg, paths));
				} else {
					EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();
					for (PackageableElement packageableElement : packagedElements2) {
						if (packageableElement instanceof DataType) {
							results.add((DataType) packageableElement);
						}
					}
				}
			} else if (structurePkg instanceof DataType && structurePkg.getName().equals(packageName)) {
				results.add((DataType) structurePkg);
			}
		}
		return results;
	}

	static public List<Abstraction> getAllAbstractions(Package source) {
		List<Abstraction> results = new ArrayList<>();

		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement packageableElement : packagedElements) {
			if (packageableElement instanceof Abstraction) {
				results.add((Abstraction) packageableElement);
			}
			if (packageableElement instanceof Package) {
				results.addAll(getAllAbstractions((Package) packageableElement));
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
	 * Get the Activity from target uml package
	 * 
	 * @param source
	 *            the container package
	 * @param paths
	 *            the path to the package target. the format is
	 *            "Pkg1/Pkg2/../Pkg2" or "Pkg1/Pkg2/../Pkg2/TargetActor" and
	 *            represents the path from source (source/<code>paths</code>).
	 * @return the Class {@link List}
	 */
	static public List<Activity> getActivities(Package source, String paths) {
		List<Activity> results = new ArrayList<>();

		String[] pathsArray = paths.split("/");
		paths = paths.substring(paths.indexOf("/") + 1, paths.length());
		String packageName = pathsArray[0];
		EList<PackageableElement> packagedElements = source.getPackagedElements();
		for (PackageableElement structurePkg : packagedElements) {
			if (structurePkg instanceof Package && structurePkg.getName().equals(packageName)) {
				if (pathsArray.length > 1) {
					results.addAll(getActivities((Package) structurePkg, paths));
				} else {
					EList<PackageableElement> packagedElements2 = ((Package) structurePkg).getPackagedElements();
					for (PackageableElement packageableElement : packagedElements2) {
						if (packageableElement instanceof Activity) {
							results.add((Activity) packageableElement);
						}
					}
				}
			} else if (structurePkg instanceof Activity && structurePkg.getName().equals(packageName)) {
				results.add((Activity) structurePkg);
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
	static public void trace(AbstractMapping rule, Resource eResource, Object sourceElement, EObject targetElement,
			String prefix) {
		String id = "";
		if (sourceElement instanceof EObject) {
			id = Sysml2CapellaUtils.getSysMLID(eResource, (EObject) sourceElement);
		} else {
			id = sourceElement.toString();
		}
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof LogicalArchitecture)
								return ((LogicalArchitecture) arch).getOwnedLogicalActorPkg();
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof LogicalArchitecture)
								return ((LogicalArchitecture) arch).getOwnedLogicalContext();
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof LogicalArchitecture)
								return ((LogicalArchitecture) arch).getOwnedAbstractCapabilityPkg();
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
	public static LogicalComponent getLogicalSystemRoot(EObject source_p) {
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof LogicalArchitecture)
								return ((LogicalArchitecture) arch).getOwnedLogicalComponent();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the logical architecture system root given a semantic object
	 * 
	 * @param source_p
	 *            the semantic object
	 * @return the logical component root
	 */
	public static LogicalArchitecture getLogicalArchitecture(EObject source_p) {
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof LogicalArchitecture)
								return ((LogicalArchitecture) arch);
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the data package given a semantic object
	 * 
	 * @param source_p
	 *            the semantic object
	 * @return the data pkg root
	 */
	public static DataPkg getDataPkgRoot(EObject source_p) {
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof LogicalArchitecture)
								return ((LogicalArchitecture) arch).getOwnedDataPkg();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the interface package given a semantic object
	 * 
	 * @param source_p
	 *            the semantic object
	 * @return the data pkg root
	 */
	public static InterfacePkg getInterfacePkgRoot(EObject source_p) {
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof LogicalArchitecture)
								return ((LogicalArchitecture) arch).getOwnedInterfacePkg();
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns the logical function root of a Capella model
	 * 
	 * @param source_p
	 *            a (non-null) Capella semantic object
	 * @return the logical function root given an eObject.
	 */
	public static LogicalFunction getLogicalFunctionRoot(EObject source_p) {
		LogicalFunctionPkg functionPackage = getLogicalFunctionPackage(source_p);
		return functionPackage.getOwnedLogicalFunctions().get(0);
	}

	/**
	 * Returns the physical function package of a Capella model
	 * 
	 * @param source_p
	 *            a (non-null) Capella semantic object
	 * @return the physical function package.
	 */
	public static LogicalFunctionPkg getLogicalFunctionPackage(EObject source_p) {
		ResourceSet resourceSet = source_p.eResource().getResourceSet();
		URI semanticResourceURI = source_p.eResource().getURI().trimFileExtension()
				.appendFileExtension("melodymodeller");
		Resource semanticResource = resourceSet.getResource(semanticResourceURI, false);
		LogicalFunctionPkg logicalFunctionPkgTmp = null;
		if (semanticResource != null) {
			EObject root = semanticResource.getContents().get(0);
			if (root instanceof Project) {
				EList<ModelRoot> ownedModelRoots = ((Project) root).getOwnedModelRoots();
				for (ModelRoot modelRoot : ownedModelRoots) {
					if (modelRoot instanceof SystemEngineering) {
						EList<ModellingArchitecture> containedPhysicalArchitectures = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedPhysicalArchitectures) {
							if (arch instanceof LogicalArchitecture) {
								FunctionPkg ownedFunctionPkg = ((LogicalArchitecture) arch).getOwnedFunctionPkg();
								if (ownedFunctionPkg instanceof LogicalFunctionPkg) {
									logicalFunctionPkgTmp = (LogicalFunctionPkg) ownedFunctionPkg;
									break;
								}
							}
						}
					}
				}
			}
		}
		return logicalFunctionPkgTmp;
	}

	/**
	 * Returns the data package given a semantic object
	 * 
	 * @param source_p
	 *            the semantic object
	 * @return the data pkg root
	 */
	public static DataPkg getDataPkgPredefinedTypeRoot(EObject source_p) {
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
						EList<ModellingArchitecture> containedLogicalArchitecture = ((SystemEngineering) modelRoot)
								.getOwnedArchitectures();
						for (ModellingArchitecture arch : containedLogicalArchitecture) {
							if (arch instanceof SystemAnalysis) {
								DataPkg ownedDataPkg = ((SystemAnalysis) arch).getOwnedDataPkg();
								EList<DataPkg> ownedDataPkgs = ownedDataPkg.getOwnedDataPkgs();
								for (DataPkg dataPkg : ownedDataPkgs) {
									if (dataPkg.getName().equals("Predefined Types")) {
										return dataPkg;
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Get all sub {@link Class}.
	 * 
	 * @param parent
	 *            the parent {@link Class}
	 * @return list of sub {@link Class}
	 */
	static public List<Class> getSubClasses(Class parent) {
		List<Class> results = new ArrayList<Class>();
		EList<Classifier> nestedClassifiers = parent.getNestedClassifiers();
		for (Classifier classifier : nestedClassifiers) {
			if (classifier instanceof Class) {
				results.add((Class) classifier);
			}
		}
		return results;
	}

	static public boolean isPrimiriveType(AbstractType type) {
		DataPkg dataPkgPredefinedTypeRoot = getDataPkgPredefinedTypeRoot(type);
		if (dataPkgPredefinedTypeRoot != null) {
			EList<org.polarsys.capella.core.data.information.datatype.DataType> ownedDataTypes = dataPkgPredefinedTypeRoot
					.getOwnedDataTypes();
			for (org.polarsys.capella.core.data.information.datatype.DataType dataType : ownedDataTypes) {
				if (dataType.equals(type)) {
					return true;
				}
			}
		}
		return false;

	}

	static public Type getPrimitiveType(AbstractType type, ResourceSet targetResourceSet) {
		// PrimitiveType ptype = UMLFactory.eINSTANCE.createPrimitiveType();

		String name = type.getName();
		String pName = "";
		switch (name) {
		case "Boolean":
			pName = "Boolean";
			break;
		case "Integer":
			pName = "Integer";
			break;
		case "Float":
			pName = "Real";
			break;
		case "String":
			pName = "String";
			break;
		default:
			break;
		}
		Profile profile = SysML2CapellaUMLProfile.getProfile(targetResourceSet, UMLProfile.SYSML_PROFILE);
		Type ownedType = profile.getNestedPackage("Libraries").getNestedPackage("PrimitiveValueTypes")
				.getOwnedType(pName);
		return ownedType;
	}

	static public org.polarsys.capella.core.data.information.datatype.DataType getPrimitiveType(
			PrimitiveType primitiveType, Project capella) {
		String name = primitiveType.getName();
		String capellaDataTypeName = "";
		switch (name) {
		case "Boolean":
			capellaDataTypeName = "Boolean";
			break;
		case "Complex":
			capellaDataTypeName = "";
			break;
		case "Integer":
			capellaDataTypeName = "Integer";
			break;
		case "Number":
			capellaDataTypeName = "Float";
			break;
		case "Real":
			capellaDataTypeName = "Float";
			break;
		case "String":
			capellaDataTypeName = "String";
			break;
		default:
			break;
		}

		DataPkg dataPkgPredefinedTypeRoot = getDataPkgPredefinedTypeRoot(capella);
		EList<org.polarsys.capella.core.data.information.datatype.DataType> ownedDataTypes = dataPkgPredefinedTypeRoot
				.getOwnedDataTypes();
		for (org.polarsys.capella.core.data.information.datatype.DataType dataType : ownedDataTypes) {
			if (dataType.getName().equals(capellaDataTypeName)) {
				return dataType;
			}
		}

		return null;
	}

	static public List<ComponentExchange> getAllLogicalComponentExchange(Project project) {
		List<ComponentExchange> results = new ArrayList<>();
		LogicalArchitecture logicalArchitecture = getLogicalArchitecture(project);
		Set<EObject> all = EObjectExt.getAll(logicalArchitecture, FaPackage.Literals.COMPONENT_EXCHANGE);
		for (EObject eObject : all) {
			results.add((ComponentExchange) eObject);
		}
		return results;
	}

	static public Part getInversePart(Component component) {
		Collection<Setting> referencingInverse = getReferencingInverse(component);
		for (Setting setting : referencingInverse) {
			EObject eObject = setting.getEObject();
			if (eObject instanceof Part) {
				return (Part) eObject;
			}
		}
		return null;
	}

	/**
	 * Check the {@link Component} <code>component</code> is {@link Component}
	 * <code>parent</code> descendant
	 * 
	 * @param component
	 *            the {@link Component} to check.
	 * @param parent
	 *            the {@link Component} parent.
	 * @return true if <code>component</code> is <code>parent</code> descendant.
	 */
	static public boolean isDescendantComponent(Component component, Component parent) {
		if (parent instanceof LogicalComponent) {
			EList<LogicalComponent> ownedLogicalComponents = ((LogicalComponent) parent).getOwnedLogicalComponents();
			for (LogicalComponent logicalComponent : ownedLogicalComponents) {
				if (logicalComponent.equals(component)) {
					return true;
				} else {
					boolean isDesc = isDescendantComponent(component, logicalComponent);
					if (isDesc) {
						return true;
					}
				}
			}
		}
		return false;
	}

	static public Collection<Setting> getReferencingInverse(EObject referenceTarget) {
		Resource res = referenceTarget.eResource();
		ResourceSet rs = res.getResourceSet();
		ECrossReferenceAdapter crossReferencer = null;
		List<Adapter> adapters = rs.eAdapters();
		for (Adapter adapter : adapters) {
			if (adapter instanceof ECrossReferenceAdapter) {
				crossReferencer = (ECrossReferenceAdapter) adapter;
				break;
			}
		}
		if (crossReferencer == null) {
			crossReferencer = new ECrossReferenceAdapter();
			rs.eAdapters().add(crossReferencer);
		}
		Collection<Setting> referencers = crossReferencer.getInverseReferences(referenceTarget, true);

		return referencers;

	}

	public static ResourceSet getTargetResourceSet(IModelScope scope) {

		if (scope instanceof FragmentedModelScope) {
			List<Resource> resources = ((FragmentedModelScope) scope).getResources();
			if (resources != null && !resources.isEmpty()) {
				return resources.get(0).getResourceSet();
			}

		} else if (scope instanceof IntermediateModelScope) {
			return getTargetResourceSet(((IntermediateModelScope) scope).getTargetDataSet());
		}

		return null;
	}

}
