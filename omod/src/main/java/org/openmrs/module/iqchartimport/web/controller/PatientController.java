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
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.iqchartimport.EntityBuilder;
import org.openmrs.module.iqchartimport.IncompleteMappingException;
import org.openmrs.module.iqchartimport.iq.IQChartDatabase;
import org.openmrs.module.iqchartimport.iq.IQChartSession;
import org.openmrs.module.iqchartimport.iq.code.ExitCode;
import org.openmrs.module.iqchartimport.util.MappingUtils;
import org.openmrs.module.iqchartimport.util.Utils;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Patients page controller
 */
@Controller("iqChartImportPatientController")
@RequestMapping("/module/iqchartimport/patient")
public class PatientController {
	
	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET)
	public String showPage(HttpServletRequest request, @RequestParam("tracnetID") Integer tracnetID, ModelMap model) throws IOException {
		Utils.checkSuperUser();
		
		IQChartDatabase database = IQChartDatabase.getInstance();
		if (database == null)
			return "redirect:upload.form";
		
		model.put("database", database);
		IQChartSession session = new IQChartSession(database);
			
		try {
			EntityBuilder builder = new EntityBuilder(session);
			Patient patient = builder.getPatient(tracnetID);
			List<PatientProgram> patientPrograms = builder.getPatientPrograms(patient, tracnetID);
			List<Encounter> encounters = builder.getPatientEncounters(patient, tracnetID);
			List<DrugOrder> drugOrders = builder.getPatientDrugOrders(patient, tracnetID);
			
			// Find exit reason obs
			List<Obs> exitObss = Utils.findObs(encounters, MappingUtils.getConcept(ExitCode.mappedQuestion));
			Obs patientExitObs = exitObss.size() > 0 ? exitObss.get(0) : null;
			
			// Find civil status
			PersonAttribute civilAttr = patient.getAttribute(Context.getPersonService().getPersonAttributeType(5));
			if (civilAttr != null)
				model.put("civilStatus", Context.getConceptService().getConcept(civilAttr.getValue()));
			
			model.put("patient", patient);
			model.put("patientPrograms", patientPrograms);
			model.put("patientExitObs", patientExitObs);
			model.put("encounters", encounters);
			model.put("drugOrders", drugOrders);
			
			return "/module/iqchartimport/patient";
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
