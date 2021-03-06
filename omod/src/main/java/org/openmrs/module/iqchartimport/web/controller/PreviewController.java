/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.iqchartimport.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.iqchartimport.EntityBuilder;
import org.openmrs.module.iqchartimport.IncompleteMappingException;
import org.openmrs.module.iqchartimport.iq.IQChartDatabase;
import org.openmrs.module.iqchartimport.iq.IQChartSession;
import org.openmrs.module.iqchartimport.util.Utils;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Patients page controller
 */
@Controller("iqChartImportPreviewController")
@RequestMapping("/module/iqchartimport/preview")
public class PreviewController {
	
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public String showPage(HttpServletRequest request, ModelMap model) throws IOException {
		Utils.checkSuperUser();
		
		IQChartDatabase database = IQChartDatabase.getInstance();
		if (database == null)
			return "redirect:upload.form";
		
		model.put("database", database);
		IQChartSession session = new IQChartSession(database);
			
		try {
			EntityBuilder builder = new EntityBuilder(session);		
			model.put("patients", builder.getPatients());
			return "/module/iqchartimport/preview";
		}
		catch (IncompleteMappingException ex) {
			String message = ex.getMessage() != null ? ex.getMessage() : "Incomplete entity mappings";
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, message);
			return "redirect:mappings.form";
		}
		finally {
			session.close();
		}
	}
}
