package com.motorph.inventory;

import java.util.List;

interface InventoryRepository {
    void insert(InventoryData d) throws Exception;
    InventoryData findByEngineNumber(String engineNumber) throws Exception;
    void update(InventoryData d) throws Exception;
    void deleteByEngineNumber(String engineNumber) throws Exception;
    List<InventoryData> findAllSortedByEngineNumber() throws Exception;
}