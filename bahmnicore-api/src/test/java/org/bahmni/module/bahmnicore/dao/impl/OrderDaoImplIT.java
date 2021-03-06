package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.service.OrderService;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class OrderDaoImplIT  extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private OrderDaoImpl orderDao;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PatientService patientService;


    @Test
    public void getPrescribedDrugOrders_ShouldNotGetDiscontinueOrders() throws Exception {
        executeDataSet("patientWithDiscontinuedOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null);

        assertThat(drugOrdersInLastVisit.size(), is(3));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(15, 16, 18));
    }

    @Test
    public void getPrescribedDrugOrders_ShouldGetRevisedOrdersAloneIfRevisionIsInSameEncounter() throws Exception {
        executeDataSet("patientWithOrderRevisedInSameEncounter.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null);

        assertThat(drugOrdersInLastVisit.size(), is(1));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(16));
    }

    @Test
    public void getPrescribedDrugOrders_ShouldGetBothRevisedOrdersAndPreviousOrderIfRevisionIsInDifferentEncounter() throws Exception {
        executeDataSet("patientWithOrderRevisedInDifferentEncounter.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null);

        assertThat(drugOrdersInLastVisit.size(), is(2));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(15, 16));
    }

    @Test
    public void getPrescribedDrugOrders_ShouldFetchAllPrescribedDrugOrdersInPastVisits() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, false, 1);
        assertThat(drugOrdersInLastVisit.size(), is(equalTo(1)));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(17));

        List<DrugOrder> drugOrdersInLastTwoVisit = orderDao.getPrescribedDrugOrders(patient, false, 2);
        assertThat(drugOrdersInLastTwoVisit.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrdersInLastTwoVisit), hasItems(15, 16, 17));

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, false, null);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));
    }

    @Test
    public void getPrescribedDrugOrders_shouldFetchAllPrescribedDrugOrdersIncludingActiveVisit() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null);
        assertThat(drugOrders.size(), is(equalTo(4)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17, 19));


        drugOrders = orderDao.getPrescribedDrugOrders(patient, null, null);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));
    }

    @Test
    public void getVisitsWithOrders_ShouldFetchVisitsWithGivenOrderType() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<Visit> visits = orderDao.getVisitsWithOrders(patient, "TestOrder", true, 1);

        assertThat(visits.size(), is(equalTo(1)));
        assertThat(visits.get(0).getId(), is(equalTo(5)));
    }

    @Test
    public void getPrescribedDrugOrdersForConcepts_shouldFetchAllPrescribedDrugOrdersForGivenConceptsForGivenNoOfVisits() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = patientService.getPatient(2);

        List<Concept> concepts = new ArrayList<>();
        concepts.add(conceptService.getConcept(24));
        concepts.add(conceptService.getConcept(27));

        List<Visit> visits = orderService.getVisitsWithOrders(patient, "DrugOrder", true, 1);
        assertEquals(1, visits.size());

        List<DrugOrder> result = orderDao.getPrescribedDrugOrdersForConcepts(patient, true, visits, concepts);
        assertEquals(2, result.size());
        assertThat(getOrderIds(result), hasItems(55, 59));

    }

    @Test
    public void shouldRetrieveAllVisitsRequested() throws Exception {
        executeDataSet("patientWithOrders.xml");
        String visitUuid1 = "1e5d5d48-6b78-11e0-93c3-18a97ba044dc";
        String visitUuid2 = "1e5d5d48-6b78-11e0-93c3-18a97b8ca4dc";
        String[] visitUuids = {visitUuid1, visitUuid2};

        List<Visit> visits = orderDao.getVisitsForUUids(visitUuids);

        assertThat(visits.size(), is(equalTo(visitUuids.length)));
        assertTrue(visitWithUuidExists(visitUuid1, visits));
        assertTrue(visitWithUuidExists(visitUuid2, visits));
    }

    @Test
    public void shouldGetDrugOrdersByVisitUuid() throws Exception {
        executeDataSet("patientWithOrders.xml");
        String visitUuid1 = "1e5d5d48-6b78-11e0-93c3-18a97ba044dc";
        String visitUuid2 = "1e5d5d48-6b78-11e0-93c3-18a97b8ca4dc";

        List<DrugOrder> prescribedDrugOrders = orderDao.getPrescribedDrugOrders(Arrays.asList(visitUuid1, visitUuid2));

        assertEquals(6, prescribedDrugOrders.size());
    }

    @Test
    public void getDrugOrderForRegimen_shouldRetrieveDrugOrdersAssignedToTheRegimen() throws Exception {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json"))
                .thenReturn(new File(this.getClass().getClassLoader().getResource("templates.json").toURI()));
        orderDao.setApplicationDataDirectory(applicationDataDirectory);


        Collection<EncounterTransaction.DrugOrder> drugOrdersForCancerRegimen = orderDao.getDrugOrderForRegimen("Cancer Regimen, CAF");
        Collection<EncounterTransaction.DrugOrder> drugOrdersForBreastCancer = orderDao.getDrugOrderForRegimen("Breast Cancer - AC");

        assertEquals(1, drugOrdersForCancerRegimen.size());
        EncounterTransaction.DrugOrder drugOrder = drugOrdersForCancerRegimen.iterator().next();
        assertThat(drugOrder.getDrug().getName(), is(equalTo("DNS")));
        assertEquals(10, drugOrdersForBreastCancer.size());

    }

    @Test (expected = NullPointerException.class)
    public void getDrugOrderForRegimen_shouldFailWhenFileDoesNotExist() {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json")).thenThrow(NullPointerException.class);
        orderDao.setApplicationDataDirectory(applicationDataDirectory);

        orderDao.getDrugOrderForRegimen("Breast Cancer - AC");
    }

    @Test
    public void getDrugOrderForRegimen_shouldReturnEmptyListWhenRegimenNotFound() throws URISyntaxException {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json"))
                .thenReturn(new File(this.getClass().getClassLoader().getResource("templates.json").toURI()));
        orderDao.setApplicationDataDirectory(applicationDataDirectory);

        Collection<EncounterTransaction.DrugOrder> drugOrders = orderDao.getDrugOrderForRegimen("Non existing regimen");
        assertThat(drugOrders.size(), is(equalTo(0)));

    }

    private boolean visitWithUuidExists(String uuid, List<Visit> visits) {
        boolean exists = false;
        for (Visit visit : visits) {
            exists |= visit.getUuid().equals(uuid);
        }
        return exists;
    }

    private List<Integer> getOrderIds(List<DrugOrder> drugOrders) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (DrugOrder drugOrder : drugOrders) {
            ids.add(drugOrder.getOrderId());
        }
        return ids;
    }
}
