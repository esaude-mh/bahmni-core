package org.bahmni.module.referencedata.labconcepts.mapper;

import ca.uhn.hl7v2.Test;
import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.openmrs.Concept;

public class PanelMapper extends ResourceMapper {
    public PanelMapper() {
        super(AllTestsAndPanels.ALL_TESTS_AND_PANELS);
    }

    @Override
    public Panel map(Concept panelConcept) {
        Panel panel = new Panel();
        panel = mapResource(panel, panelConcept);
        panel.setTests(MapperUtils.getMinimalResources(panelConcept.getSetMembers(), LabTest.LAB_TEST_CONCEPT_CLASS));
        panel.setSortOrder(getSortWeight(panelConcept));
        panel.setDescription(MapperUtils.getDescriptionOrName(panelConcept));
        return panel;
    }
}
