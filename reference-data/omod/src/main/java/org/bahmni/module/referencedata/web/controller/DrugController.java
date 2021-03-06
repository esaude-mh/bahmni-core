package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.Drug;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.bahmni.module.referencedata.labconcepts.mapper.DrugMapper;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/v1/reference-data/drug")
public class DrugController extends BaseRestController {
    private final DrugMapper drugMapper;
    private ConceptService conceptService;

    @Autowired
    public DrugController(ConceptService conceptService) {
        drugMapper = new DrugMapper();
        this.conceptService = conceptService;
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Drug getDrug(@PathVariable("uuid") String uuid) {
        final org.openmrs.Drug drug = conceptService.getDrugByUuid(uuid);
        if (drug == null) {
            throw new ConceptNotFoundException("No drug found with uuid " + uuid);
        }
        return drugMapper.map(drug);
    }
    
}
