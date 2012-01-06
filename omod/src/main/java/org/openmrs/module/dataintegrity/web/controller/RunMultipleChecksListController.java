package org.openmrs.module.dataintegrity.web.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openmrs.api.context.Context;
import org.openmrs.module.dataintegrity.IntegrityCheckResults;
import org.openmrs.module.dataintegrity.IntegrityCheck;
import org.openmrs.module.dataintegrity.DataIntegrityConstants;
import org.openmrs.module.dataintegrity.DataIntegrityService;
import org.openmrs.web.WebConstants;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.web.servlet.view.RedirectView;

public class RunMultipleChecksListController extends SimpleFormController {
	private DataIntegrityService getDataIntegrityService() {
        return (DataIntegrityService)Context.getService(DataIntegrityService.class);
    }
	
	protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        List<IntegrityCheck> checks = new ArrayList<IntegrityCheck>();
        if (Context.isAuthenticated()) {
        	checks = getDataIntegrityService().getAllIntegrityChecks(); 
        }
        return checks;
    }
	
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object obj,
            BindException errors) throws Exception {
		HttpSession httpSession = request.getSession();
		
		//Clear the previously stored failed records in the session
		if (httpSession.getAttribute("failedRecords") != null) {
			httpSession.removeAttribute("failedRecords");
		}
		
		String view = getFormView();
		if (Context.isAuthenticated()) {
			
			MessageSourceAccessor msa = getMessageSourceAccessor();
			
			String success = "";
			String error = "";
			String stack = "";
			DataIntegrityService service = getDataIntegrityService();
			
			String[] checkList = request.getParameterValues("integrityCheckId");
			if (checkList != null) {
				int successCount = 0;
				int errorCount = 0;
				List<IntegrityCheckResults> results = new ArrayList<IntegrityCheckResults>();
				StringBuffer buffer = new StringBuffer();
				for (String checkId : checkList) {
					try {
						int id = Integer.valueOf(checkId);
						IntegrityCheck template = service.getIntegrityCheck(id);
						service.runIntegrityCheck(template);
						// results.add(resultTemplate);
						successCount++;
					} catch (Exception e) {
						errorCount++;
						Writer writer = new StringWriter();
						PrintWriter printWriter = new PrintWriter(writer);
						e.printStackTrace(printWriter);
						buffer.append("\r\n" + writer.toString());
						view = "runMultipleChecks.list";
					}
				}
				httpSession.setAttribute("multipleCheckResults", results);
				view = getSuccessView();
				success = msa.getMessage("dataintegrity.runMultipleChecks.totalCount") + " " + (successCount + errorCount) + "<br/>";
				success += msa.getMessage("dataintegrity.runMultipleChecks.successCount") + " " + successCount+ "<br/>";
				error = errorCount > 0 ? (msa.getMessage("dataintegrity.runMultipleChecks.errorCount") + " " + errorCount + "<br/>") : "";
				if (!buffer.toString().equals("")) {
					stack = buffer.toString();
				}
			} else { 
				error = msa.getMessage("dataintegrity.runMultipleChecks.error");
				view = "runMultipleChecks.list";
			}
		
			if (!success.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, success);
			if (!error.equals(""))
				httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, error);
			if (!stack.equals(""))
				httpSession.setAttribute(DataIntegrityConstants.DATA_INTEGRITY_ERROR_STACK_TRACE, stack);
		}
		
		return new ModelAndView(new RedirectView(view));
	}
}
