package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.model.BahmniDrugOrder;

import java.util.Date;
import java.util.List;

public interface BahmniDrugOrderService {
    void add(String patientId, Date orderDate, List<BahmniDrugOrder> bahmniDrugOrders, String systemUserName);
}
