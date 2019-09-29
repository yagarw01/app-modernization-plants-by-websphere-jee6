package com.ibm.websphere.samples.pbw.war;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.websphere.samples.pbw.ejb.CatalogMgr;

@ExtendWith(MockitoExtension.class)
public class TestImageServlet {

	@Test
	public void getImageForInventoryItem(@Mock HttpServletRequest req, @Mock HttpServletResponse resp)
			throws Exception {
		ImageServlet imageServlet = new ImageServlet();
		CatalogMgr catalogMgrMock = mock(CatalogMgr.class);
		setFieldMock("catalog", imageServlet, catalogMgrMock);
		String inventoryID = "ID";
		byte[] someBytes = new byte[] {};
		ServletOutputStream mockOutputStream = mock(ServletOutputStream.class);

		when(req.getParameter("action")).thenReturn("getimage");
		when(req.getParameter("inventoryID")).thenReturn(inventoryID);
		when(catalogMgrMock.getItemImageBytes(inventoryID)).thenReturn(new byte[] {});
		when(resp.getOutputStream()).thenReturn(mockOutputStream);

		imageServlet.doGet(req, resp);

		verify(resp).setContentType(eq("image/jpeg"));
		verify(mockOutputStream).write(eq(someBytes));
	}

	@Test
	public void noAction(@Mock HttpServletRequest req, @Mock HttpServletResponse resp) throws Exception {
		ImageServlet imageServlet = new ImageServlet();
		CatalogMgr catalogMgrMock = mock(CatalogMgr.class);
		setFieldMock("catalog", imageServlet, catalogMgrMock);

		when(req.getParameter("action")).thenReturn("");

		imageServlet.doGet(req, resp);
		verify(catalogMgrMock, times(0)).getItemImageBytes(anyString());
	}

	public void setFieldMock(String fieldName, Object classToBeInjected, Object mockValue)
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = classToBeInjected.getClass().getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(classToBeInjected, mockValue);
	}
}
