/*******************************************************************************
 * Copyright (c) 2019 - 2022 Artal Technologies.
 * This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 *     Artal Technologies - initial API and implementation
 *******************************************************************************/
package com.artal.capella.mapping.sysml2capella.preferences;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * {@link SysmlCapellaPreferenceUtils} provides the SysML preferences
 * implementations.
 * 
 * @author YBI
 *
 */
public class SysmlCapellaPreferenceUtils {

	public static final String SYSML_STRUCTURE_TEMPLATE = "SysML Structure Template";

	/**
	 * Create the composite allowing the set a configuration file.
	 * 
	 * @param parent
	 *            the parent container
	 * @param defaultMode
	 *            if true the composite allows to modify the default xml file.
	 * @return The field containing the configuration file path.
	 */
	static public Text createPreferenceComposite(Composite parent, boolean defaultMode) {

		String defaultpath = PlatformUI.getPreferenceStore()
				.getString(SysmlCapellaPreferenceUtils.SYSML_STRUCTURE_TEMPLATE);

		Composite main = new Composite(parent, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		main.setLayout(new GridLayout(3, false));

		Label label = new Label(main, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(defaultMode ? "Default configuration file" : "Configuration file");

		Text text = new Text(main, SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (defaultpath != null && !defaultpath.isEmpty()) {
			text.setText(defaultpath);
		}

		Button button = new Button(main, SWT.NONE);
		button.setLayoutData(new GridData());
		button.setText("Browse..");

		button.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(Display.getCurrent().getActiveShell());
				dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
				String path = dialog.open();
				if (path != null) {
					text.setText(path);
					if (defaultMode) {
						PlatformUI.getPreferenceStore().setValue(SysmlCapellaPreferenceUtils.SYSML_STRUCTURE_TEMPLATE,
								path);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});

		return text;
	}
}
