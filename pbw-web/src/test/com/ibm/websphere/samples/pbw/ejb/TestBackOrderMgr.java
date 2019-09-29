package com.ibm.websphere.samples.pbw.ejb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.websphere.samples.pbw.jpa.BackOrder;
import com.ibm.websphere.samples.pbw.jpa.Inventory;
import com.ibm.websphere.samples.pbw.utils.Util;

@ExtendWith(MockitoExtension.class)
public class TestBackOrderMgr {

	@Test
	public void testSuccessfullyCreateBackOrder(@Mock EntityManager mockEntityManager) throws Exception {
		BackOrderMgr backOrderMgr = new BackOrderMgr();
		Query mockQuery = mock(Query.class);
		String inventoryId = "InvID1";
		int amountToOrder = 100;
		int maximumItems = 1000;
		Inventory inventory1 = createInventoryItem(inventoryId);
		BackOrder backOrder1 = createBackOrder("1", inventory1, Util.STATUS_ORDERSTOCK, 100);

		when(mockEntityManager.createNamedQuery("findByInventoryID")).thenReturn(mockQuery);
		when(mockQuery.getSingleResult()).thenReturn(backOrder1);
		setFieldMock("em", backOrderMgr, mockEntityManager);

		backOrderMgr.createBackOrder(inventoryId, amountToOrder, maximumItems);
		verify(mockQuery).setParameter(eq("id"), eq(inventoryId));
		assertThat(backOrder1.getQuantity()).isEqualTo(200);
	}

	@Test
	public void testNoResultCreateBackOrder(@Mock EntityManager mockEntityManager) throws Exception {
		BackOrderMgr backOrderMgr = new BackOrderMgr();
		Query mockQuery = mock(Query.class);
		final String inventoryId = "InvID1";
		final int amountToOrder = 100;
		final int maximumItems = 1000;
		Inventory inventory1 = createInventoryItem(inventoryId);
		
		
		when(mockEntityManager.createNamedQuery("findByInventoryID")).thenReturn(mockQuery);
		when(mockQuery.getSingleResult()).thenThrow(NoResultException.class);
		when(mockEntityManager.find(Inventory.class, inventoryId)).thenReturn(inventory1);
		
		setFieldMock("em", backOrderMgr, mockEntityManager);
		
		backOrderMgr.createBackOrder(inventoryId, amountToOrder, maximumItems);

		ArgumentCaptor<BackOrder> argument = ArgumentCaptor.forClass(BackOrder.class);
		verify(mockEntityManager).persist(argument.capture());
		assertThat(argument.getValue().getQuantity()).isEqualTo(maximumItems + amountToOrder);
	}
	
	public BackOrder createBackOrder(String backOrderId, Inventory inventory, String status, int quantity) {
		BackOrder backOrder = new BackOrder();
		backOrder.setBackOrderID("1");
		backOrder.setInventory(inventory);
		backOrder.setLowDate(10000);
		backOrder.setOrderDate(20000);
		backOrder.setQuantity(quantity);
		backOrder.setStatus(status);
		backOrder.setSupplierOrderID("Supplier1");
		return backOrder;
	}

	public Inventory createInventoryItem(String inventoryId) {
		Inventory inventory = new Inventory(inventoryId, "Inventory Item 1", "Heading Value 1", "Some Description",
				"Info about the package", "image.jpeg", (float) 100.50, (float) 50.25, 100, 1,
				"Some notes about the item", true);
		return inventory;
	}

	public void setFieldMock(String fieldName, Object classToBeInjected, Object mockValue)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = classToBeInjected.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(classToBeInjected, mockValue);
	}
}
