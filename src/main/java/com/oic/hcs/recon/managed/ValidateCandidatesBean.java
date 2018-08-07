/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oic.hcs.recon.managed;

import java.io.Serializable;
//import java.math.Double;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import com.oic.hcs.recon.service.ReconServiceBeanLocal;
import com.oic.hcs.recon.util.Util;
import com.oic.hcs.recon.utility.AlreadyExistsException;
import com.oic.hcs.recon.utility.LocationErrors;
import com.oic.hcs.recon.utility.StatusErrors;

/**
 *
 * @author noaman000
 */
@ManagedBean
@ViewScoped
public class ValidateCandidatesBean implements Serializable{

    @EJB
    ReconServiceBeanLocal reconEJB;

    private String reconId;
    private List<StatusErrors> statusErrorsList;
    private List<Double> groupErrorsList;
    private List<LocationErrors> locationErrorsList;
    private String newLocationCd;
    private boolean nextFlag;

    public String getReconId() {
        return reconId;
    }

    public void setReconId(String reconId) {
        this.reconId = reconId;
    }

    public List<StatusErrors> getStatusErrorsList() {
        return statusErrorsList;
    }

    public void setStatusErrorsList(List<StatusErrors> statusErrorsList) {
        this.statusErrorsList = statusErrorsList;
    }

    public List<Double> getGroupErrorsList() {
        return groupErrorsList;
    }

    public void setGroupErrorsList(List<Double> groupErrorsList) {
        this.groupErrorsList = groupErrorsList;
    }

    public List<LocationErrors> getLocationErrorsList() {
        return locationErrorsList;
    }

    public void setLocationErrorsList(List<LocationErrors> locationErrorsList) {
        this.locationErrorsList = locationErrorsList;
    }

    public String getNewLocationCd() {
        return newLocationCd;
    }

    public void setNewLocationCd(String newLocationCd) {
        this.newLocationCd = newLocationCd;
    }

    public boolean isNextFlag() {
        return nextFlag;
    }

    public void setNextFlag(boolean nextFlag) {
        this.nextFlag = nextFlag;
    }
   
    public void validateCandidates() {
        if (reconId != null && !reconId.trim().equals("")) {
            List errList = reconEJB.candidateValidations(reconId);
            if (errList != null && !errList.isEmpty()) {
                statusErrorsList = (ArrayList<StatusErrors>) errList.get(0);
                groupErrorsList = (List) errList.get(1);
                locationErrorsList = (ArrayList<LocationErrors>) errList.get(2);
            }
        }
    }

    public void deleteAllStatusErrors() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            reconEJB.deleteStatusErrors(reconId, statusErrorsList);
            validateCandidates();
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "Status errors have been successfully deleted."));
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,"Information","Status errors have been successfully deleted."); 
        //RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void deleteAllGroupErrors() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            reconEJB.deleteGroupErrors(reconId, groupErrorsList);
            validateCandidates();
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "Group/Member errors have been successfully deleted."));
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }

        //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,"Information", "Group/Member errors have been successfully deleted."); 
        //RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void deleteAllLocationErrors() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            reconEJB.deleteLocationErrors(reconId, locationErrorsList);
            validateCandidates();
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "Location errors have been successfully deleted."));
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }

        //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,"Information", "Location errors have been successfully deleted."); 
        //RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void updateAllLocationErrors() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String)session.getAttribute("username");
        try {
            reconEJB.updateAllLocationErrors(reconId, newLocationCd, locationErrorsList,user);
            validateCandidates();
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "All location errors have been successfully updated."));
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,"Information", "All location errors have been successfully updated."); 
        //RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public void updateLocationError(String locationCode, String errorClaimId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String)session.getAttribute("username");
        try {
            reconEJB.updateLocationError(reconId, locationCode, errorClaimId,user);
        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), null));
        }
        validateCandidates();
        if (locationErrorsList == null || locationErrorsList.isEmpty()) {
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "All location errors have been successfully updated."));
            //FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,"Information", "All location errors have been successfully updated."); 
            //RequestContext.getCurrentInstance().showMessageInDialog(message);
        }
    }


    public void generateReconData() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            HttpSession session = Util.getSession();
            String user = (String) session.getAttribute("username");
            reconEJB.insertReconStagingData(reconId, user);
            setNextFlag(true);
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information: ", "Generation of recon data has been completed, please proceed with updates."));
        } catch (AlreadyExistsException a) {
            setNextFlag(true);
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Can not generate recon data: " + a.getMessage()));
        } catch (Exception e) {
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Can not generate recon data, please contact your administrator."));
        }
    }

}
