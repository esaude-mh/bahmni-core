package org.bahmni.module.elisatomfeedclient.api.domain;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.elisatomfeedclient.api.worker.ProviderHelper;
import org.joda.time.DateTime;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Provider;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class OpenElisAccession {
    private String accessionUuid;
    private String patientUuid;
    private String patientFirstName;
    private String patientLastName;
    private String dateTime;
    private String patientIdentifier;
    private List<OpenElisAccessionNote> accessionNotes;
    private Set<OpenElisTestDetail> testDetails = new HashSet<>();

    public void addTestDetail(OpenElisTestDetail testDetail) {
        getTestDetails().add(testDetail);
    }

    public AccessionDiff getDiff(Encounter previousEncounter) {
        AccessionDiff accessionDiff = new AccessionDiff();
        for (OpenElisTestDetail testDetail : testDetails) {
            String orderableUuid = StringUtils.isBlank(testDetail.getPanelUuid()) ? testDetail.getTestUuid() : testDetail.getPanelUuid();
            if (testDetail.isCancelled()) {
                if (hasOrderByUuid(previousEncounter.getOrders(), orderableUuid)) {
                    accessionDiff.addRemovedTestDetails(testDetail);
                }
            } else {
                Order order = getOrderByTestUuid(previousEncounter.getOrders(),orderableUuid);
                if (order==null) {
                    accessionDiff.addAddedTestDetail(testDetail);
                }
            }
        }

        return accessionDiff;
    }

    private boolean hasOrderByUuid(Set<Order> orders, String testUuid) {
        for (Order order : orders) {
            if (!order.getVoided() && order.getConcept() != null && order.getConcept().getUuid().equals(testUuid))
                return true;
        }
        return false;
    }

    private Order getOrderByTestUuid(Set<Order> orders, String testUuid){
        for (Order order : orders) {
            if (order.getConcept() != null && order.getConcept().getUuid().equals(testUuid))
                return order;
        }
        return null;
    }

    public Date fetchDate() {
        return dateTime == null ? null : DateTime.parse(dateTime).toDate();
    }

    public String getHealthCenter() {
        return patientIdentifier.substring(0, 3);
    }

    public AccessionDiff getAccessionNoteDiff(Set<Encounter> encounters, Concept labManagerNoteConcept,Provider defaultLabManagerProvider) {
        AccessionDiff accessionNotesDiff = new AccessionDiff();
        if (accessionNotes != null) {
            ArrayList<OpenElisAccessionNote> accessionNotesCopy = new ArrayList<>(accessionNotes);
            if(encounters != null){
                for(Encounter labManagerEncounter : encounters){
                    filterOutAlreadyAddedAccessionNotes(labManagerEncounter, labManagerNoteConcept, accessionNotesCopy,defaultLabManagerProvider);
                }
            }
            accessionNotesDiff.setAccessionNotesToBeAdded(accessionNotesCopy);
        }
        return accessionNotesDiff;
    }

    private void filterOutAlreadyAddedAccessionNotes(Encounter encounter, Concept labManagerNoteConcept, ArrayList<OpenElisAccessionNote> accessionNotesCopy, Provider defaultLabManagerProvider) {
        Set<Obs> encObs = encounter.getObs();
        for (Obs obs : encObs) {
            if (obs.getConcept().equals(labManagerNoteConcept)) {
                for (OpenElisAccessionNote accessionNote : accessionNotes) {
                    if (shouldMatchNoteInEncounter(encounter, defaultLabManagerProvider, obs, accessionNote)) {
                        accessionNotesCopy.remove(accessionNote);
                    }
                }
            }
        }
    }

    private boolean shouldMatchNoteInEncounter(Encounter encounter, Provider defaultLabManagerProvider, Obs obs, OpenElisAccessionNote accessionNote) {
        return accessionNote.getNote().equals(obs.getValueText()) &&
                (accessionNote.isProviderInEncounter(encounter) || ProviderHelper.getProviderFrom(encounter).equals(defaultLabManagerProvider));
    }


}
