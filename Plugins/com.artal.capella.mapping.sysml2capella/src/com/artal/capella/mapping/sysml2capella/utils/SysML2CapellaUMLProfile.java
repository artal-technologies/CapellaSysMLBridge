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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.diffmerge.bridge.uml.util.UMLProfilesUtil;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * @author YBI
 *
 */
public class SysML2CapellaUMLProfile {

	public enum UMLProfile {

		UML_STANDARD_PROFILE_VALIDATION_PROFILE(
				"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/UML_Standard_Profile.Validation_Profile.profile.uml"), UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_TRACEABILITY_CUST_PROFILE(
						"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/UML_Standard_Profile.MagicDraw_Profile.Traceability_customization.profile.uml"), UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE(
								"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/UML_Standard_Profile.MagicDraw_Profile.profile.uml"), UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_FIND_TEXT_PROFILE(
										"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/UML_Standard_Profile.MagicDraw_Profile.Find_By_Text.profile.uml"), UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_DSL_CUST_PROFILE(
												"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/UML_Standard_Profile.MagicDraw_Profile.DSL_Customization.profile.uml"), UML_STANDARD_PROFILE_DEPENDENCY_MATRIX_PROFILE(
														"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/UML_Standard_Profile.Dependency_Matrix_Profile.profile.uml"), UI_PROTOTYPING_PROFILE(
																"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/UI_Prototyping_Profile.profile.uml"), SYSML_PROFILE(
																		"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/SysML.profile.uml"), SIMULATIONPROFILE_PROFILE(
																				"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/SimulationProfile.profile.uml"), PROFILE_PROFILE(
																						"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/profile.profile.uml"), MODEL_LIBRARY_DSL_PROFILE(
																								"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/Model_Library.DSL_profile.profile.uml"), MD_CUST_VVP_DOCBOOK_PROFILE(
																										"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/MD_Customization_for_ViewsViewpoints.DocBook_Profile.profile.uml"), MD_CUST_SYSML_CUST_TRACEABILITY_PROFILE(
																												"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/MD_Customization_for_SysML.customizations_for_traceability.profile.uml"), MD_CUST_SYSML_ADD_STEREO_PROFILE(
																														"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/MD_Customization_for_SysML.additional_stereotypes.profile.uml"), MD_CUST_REQ_ADD_STEREO_PROFILE(
																																"platform:/plugin/com.artal.capella.mapping.sysml2capella/resources/profiles/MD_Customization_for_Requirements.additional_stereotypes.profile.uml");

		String _path;

		private UMLProfile(String path) {
			_path = path;
		}

		public String getPath() {
			return _path;
		}

	}

	static public void initProfiles(ResourceSet rset) {
		loadProfile(UMLProfile.MD_CUST_REQ_ADD_STEREO_PROFILE, rset);
		loadProfile(UMLProfile.MD_CUST_SYSML_ADD_STEREO_PROFILE, rset);
		loadProfile(UMLProfile.MD_CUST_SYSML_CUST_TRACEABILITY_PROFILE, rset);
		loadProfile(UMLProfile.MD_CUST_VVP_DOCBOOK_PROFILE, rset);
		loadProfile(UMLProfile.MODEL_LIBRARY_DSL_PROFILE, rset);
		loadProfile(UMLProfile.PROFILE_PROFILE, rset);
		loadProfile(UMLProfile.SIMULATIONPROFILE_PROFILE, rset);
		loadProfile(UMLProfile.SYSML_PROFILE, rset);
		loadProfile(UMLProfile.UI_PROTOTYPING_PROFILE, rset);
		loadProfile(UMLProfile.UML_STANDARD_PROFILE_DEPENDENCY_MATRIX_PROFILE, rset);
		loadProfile(UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_DSL_CUST_PROFILE, rset);
		loadProfile(UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_FIND_TEXT_PROFILE, rset);
		loadProfile(UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE, rset);
		loadProfile(UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_TRACEABILITY_CUST_PROFILE, rset);
		loadProfile(UMLProfile.UML_STANDARD_PROFILE_VALIDATION_PROFILE, rset);

	}

	static public List<Profile> getProfiles(ResourceSet rset) {
		List<Profile> results = new ArrayList<>();

		results.add(getProfile(rset, UMLProfile.MD_CUST_REQ_ADD_STEREO_PROFILE));
		results.add(getProfile(rset, UMLProfile.MD_CUST_SYSML_ADD_STEREO_PROFILE));
		results.add(getProfile(rset, UMLProfile.MD_CUST_SYSML_CUST_TRACEABILITY_PROFILE));
		results.add(getProfile(rset, UMLProfile.MD_CUST_VVP_DOCBOOK_PROFILE));
		results.add(getProfile(rset, UMLProfile.MODEL_LIBRARY_DSL_PROFILE));
		results.add(getProfile(rset, UMLProfile.PROFILE_PROFILE));
		results.add(getProfile(rset, UMLProfile.SIMULATIONPROFILE_PROFILE));
		results.add(getProfile(rset, UMLProfile.SYSML_PROFILE));
		results.add(getProfile(rset, UMLProfile.UI_PROTOTYPING_PROFILE));
		results.add(getProfile(rset, UMLProfile.UML_STANDARD_PROFILE_DEPENDENCY_MATRIX_PROFILE));
		results.add(getProfile(rset, UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_DSL_CUST_PROFILE));
		results.add(getProfile(rset, UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_FIND_TEXT_PROFILE));
		results.add(getProfile(rset, UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE));
		results.add(getProfile(rset, UMLProfile.UML_STANDARD_PROFILE_MAGIC_DRAW_PROFILE_TRACEABILITY_CUST_PROFILE));
		results.add(getProfile(rset, UMLProfile.UML_STANDARD_PROFILE_VALIDATION_PROFILE));

		return results;
	}

	/**
	 * @param rset
	 * @return
	 */
	static public Profile getProfile(ResourceSet rset, UMLProfile umlProfile) {
		String uri = umlProfile.getPath();
		URI pURI = URI.createURI(uri, false);
		Resource resource = rset.getResource(pURI, false);
		Profile umlStdProfile = (Profile) resource.getContents().get(0);
		return umlStdProfile;
	}

	static public void loadProfile(UMLProfile umlProfile, ResourceSet rset) {
		String path = umlProfile.getPath();

		URI pURI = URI.createURI(path);

		Resource profile = rset.getResource(pURI, true);
		Profile umlStdProfile = (Profile) EcoreUtil.getObjectByType(profile.getContents(), UMLPackage.Literals.PACKAGE);
		UMLProfilesUtil.registerProfile(rset, umlStdProfile);
	}
}