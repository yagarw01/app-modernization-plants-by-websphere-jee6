package com.ibm.websphere.samples.pbw.war;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.websphere.samples.pbw.ejb.BackOrderMgr;
import com.ibm.websphere.samples.pbw.ejb.CatalogMgr;
import com.ibm.websphere.samples.pbw.jpa.BackOrder;
import com.ibm.websphere.samples.pbw.jpa.Inventory;
import com.ibm.websphere.samples.pbw.utils.Util;

@ExtendWith(MockitoExtension.class)
public class TestAdminServlet {

	@Test
	public void performBackOrderActionNull(@Mock HttpServletRequest req, @Mock HttpServletResponse resp)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
			ServletException, IOException {
		//GIVEN - STATE SETUP
		AdminServlet adminServlet = new AdminServlet();
		HttpSession mockSession = mock(HttpSession.class);

		BackOrderMgr backOrderMgrMock = mock(BackOrderMgr.class);
		CatalogMgr catalogMgrMock = mock(CatalogMgr.class);

		Inventory inventory1 = createInventoryItem("KEY1");
		Inventory inventory2 = createInventoryItem("KEY2");
		BackOrder backOrder1 = createBackOrder("1", inventory1, "Ready");
		BackOrder backOrder2 = createBackOrder("2", inventory2, Util.STATUS_ADDEDSTOCK);
		
		List<BackOrder> backOrders = new ArrayList<BackOrder>();
		backOrders.add(backOrder1);
		backOrders.add(backOrder2);

		setFieldMock("backOrderStock", adminServlet, backOrderMgrMock);
		setFieldMock("catalog", adminServlet, catalogMgrMock);

		ServletConfig mockConfig = mock(ServletConfig.class);
		ServletContext mockContext = mock(ServletContext.class);

		Field configField = adminServlet.getClass().getSuperclass().getSuperclass().getDeclaredField("config");
		configField.setAccessible(true);
		configField.set(adminServlet, mockConfig);

		
		//WHEN - BEHAVIORS THAT WILL OCCUR DURING TEST
		when(req.getParameter(Util.ATTR_ACTION)).thenReturn(null);
		when(req.getSession(true)).thenReturn(mockSession);
		when(mockConfig.getServletContext()).thenReturn(mockContext);
		when(mockContext.getRequestDispatcher(Util.PAGE_BACKADMIN)).thenReturn(mock(RequestDispatcher.class));
		when(backOrderMgrMock.findBackOrders()).thenReturn(backOrders);
		when(catalogMgrMock.getItemInventory("KEY1")).thenReturn(inventory1);
		when(catalogMgrMock.getItemInventory("KEY2")).thenReturn(inventory2);
		adminServlet.performBackOrder(req, resp);

		//THEN - VERIFICATION OF BEHAVIOR
		ArgumentCaptor<ArrayList<BackOrderItem>> argument = ArgumentCaptor.forClass(ArrayList.class);

		verify(mockSession).setAttribute(eq("backorderitems"), argument.capture());
		assertEquals(2, argument.getValue().size());
	}
	

	@Test
	public void performBackOrderActionUpdateStocks(@Mock HttpServletRequest req, @Mock HttpServletResponse resp)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException,
			ServletException, IOException {
		//GIVEN - STATE SETUP
		AdminServlet adminServlet = new AdminServlet();
		String backOrderToBeUpdated= "1";
		String inventoryItemToBeUpdated = "KEY1";
		HttpSession mockSession = mock(HttpSession.class);

		BackOrderMgr backOrderMgrMock = mock(BackOrderMgr.class);
		CatalogMgr catalogMgrMock = mock(CatalogMgr.class);

		Inventory inventory1 = createInventoryItem(inventoryItemToBeUpdated);
		Inventory inventory2 = createInventoryItem("KEY2");
		BackOrder backOrder1 = createBackOrder(backOrderToBeUpdated, inventory1, "Ready");
		BackOrder backOrder2 = createBackOrder("2", inventory2, Util.STATUS_ADDEDSTOCK);
		
		List<BackOrder> backOrders = new ArrayList<BackOrder>();
		backOrders.add(backOrder1);
		backOrders.add(backOrder2);

		setFieldMock("backOrderStock", adminServlet, backOrderMgrMock);
		setFieldMock("catalog", adminServlet, catalogMgrMock);

		ServletConfig mockConfig = mock(ServletConfig.class);
		ServletContext mockContext = mock(ServletContext.class);

		Field configField = adminServlet.getClass().getSuperclass().getSuperclass().getDeclaredField("config");
		configField.setAccessible(true);
		configField.set(adminServlet, mockConfig);
		

		
		//WHEN - BEHAVIORS THAT WILL OCCUR DURING TEST
		when(req.getParameter(Util.ATTR_ACTION)).thenReturn(Util.ACTION_UPDATESTOCK);
		when(req.getParameterValues("selectedObjectIds")).thenReturn(new String[] {backOrderToBeUpdated});
		when(req.getSession(true)).thenReturn(mockSession);
		when(mockConfig.getServletContext()).thenReturn(mockContext);
		when(mockContext.getRequestDispatcher(Util.PAGE_BACKADMIN)).thenReturn(mock(RequestDispatcher.class));
		when(backOrderMgrMock.findBackOrders()).thenReturn(backOrders);
		when(backOrderMgrMock.getBackOrderInventoryID(backOrderToBeUpdated)).thenReturn(inventoryItemToBeUpdated);
		when(backOrderMgrMock.getBackOrderQuantity(backOrderToBeUpdated)).thenReturn(inventory1.getQuantity());
		when(catalogMgrMock.getItemInventory("KEY1")).thenReturn(inventory1);
		when(catalogMgrMock.getItemInventory("KEY2")).thenReturn(inventory2);
		adminServlet.performBackOrder(req, resp);

		//THEN - VERIFICATION OF BEHAVIOR
		ArgumentCaptor<ArrayList<BackOrderItem>> argument = ArgumentCaptor.forClass(ArrayList.class);

		verify(catalogMgrMock).setItemQuantity(eq(inventoryItemToBeUpdated), eq(inventory1.getQuantity()));
		verify(backOrderMgrMock).updateStock(eq(backOrderToBeUpdated), eq(inventory1.getQuantity()));
		verify(mockSession).setAttribute(eq("backorderitems"), argument.capture());
		assertEquals(2, argument.getValue().size());
	}

	public BackOrder createBackOrder(String backOrderId, Inventory inventory, String status) {
		BackOrder backOrder = new BackOrder();
		backOrder.setBackOrderID("1");
		backOrder.setInventory(inventory);
		backOrder.setLowDate(10000);
		backOrder.setOrderDate(20000);
		backOrder.setQuantity(1000);
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
	
	public void setFieldMock(String fieldName, Object classToBeInjected, Object mockValue) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = classToBeInjected.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(classToBeInjected, mockValue);
	}
}
