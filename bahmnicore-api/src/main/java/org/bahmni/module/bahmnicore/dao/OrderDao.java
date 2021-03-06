package org.bahmni.module.bahmnicore.dao;

import java.util.Collection;

import org.openmrs.*;

import java.util.List;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public interface OrderDao {
    List<Order> getCompletedOrdersFrom(List<Order> orders);

    List<DrugOrder> getPrescribedDrugOrders(Patient patient, Boolean includeActiveVisit, Integer numberOfVisits);

    List<Visit> getVisitsWithOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits);

    List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids);

    List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits, List<Concept> conceptIds);

    Collection<EncounterTransaction.DrugOrder> getDrugOrderForRegimen(String regimenName);

    List<Visit> getVisitsForUUids(String[] visitUuids);

    List<Order> getAllOrders(Patient patient, List<OrderType> orderTypes, Integer offset, Integer limit);

    List<Order> getAllOrdersForVisits(Patient patient, OrderType orderType, List<Visit> visits);

    Order getOrderByUuid(String orderUuid);
}
