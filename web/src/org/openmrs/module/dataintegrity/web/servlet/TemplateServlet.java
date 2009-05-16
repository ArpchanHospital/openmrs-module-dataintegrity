package org.openmrs.module.dataintegrity.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.dataintegrity.DataIntegrityService;
import org.openmrs.module.dataintegrity.DataIntegrityTemplate;

public class TemplateServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		writer.write("Not valid");
		
	}
		
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter writer = response.getWriter();
		try {
			DataIntegrityService service = (DataIntegrityService) Context.getService(DataIntegrityService.class);
			DataIntegrityTemplate temp = new DataIntegrityTemplate();
			temp.setDataIntegrityName(request.getParameter("tempName"));
			service.saveDataIntegrityTemplate(temp);
			response.sendRedirect("../../module/dataintegrity/displayTemplate.form");
		}
		catch (APIException apiexception) {
			apiexception.printStackTrace(writer);
		}	
	}

}
