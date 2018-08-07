/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.oic.hcs.recon.managed;

import com.oic.hcs.recon.entity.ReconClaimsData;
import com.oic.hcs.recon.entity.ReconClaimsDataPK;
import java.io.Serializable;
//import java.math.Double;
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
import com.oic.hcs.recon.utility.DataAndSummary;
import com.oic.hcs.recon.utility.ReconDataValidationErrors;

/**
 *
 * @author noaman000
 */
@ManagedBean
@ViewScoped
public class ValidateReconDataBean implements Serializable{
    @EJB
    ReconServiceBeanLocal reconEJB;

    private String reconId;
    private ReconDataValidationErrors errors;
    List<ReconClaimsData> nullErrors;
    List<ReconClaimsData> zeroErrors;
    List<ReconClaimsData> amtErrors;
    DataAndSummary payload = new DataAndSummary();
    private boolean nextFlag;
    private String reconType;

    public String getReconId() {
        return reconId;
    }

    public void setReconId(String reconId) {
        this.reconId = reconId;
    }

    public ReconDataValidationErrors getErrors() {
        return errors;
    }

    public void setErrors(ReconDataValidationErrors errors) {
        this.errors = errors;
    }

    public List<ReconClaimsData> getNullErrors() {
        return nullErrors;
    }

    public void setNullErrors(List<ReconClaimsData> nullErrors) {
        this.nullErrors = nullErrors;
    }

    public List<ReconClaimsData> getZeroErrors() {
        return zeroErrors;
    }

    public void setZeroErrors(List<ReconClaimsData> zeroErrors) {
        this.zeroErrors = zeroErrors;
    }

    public List<ReconClaimsData> getAmtErrors() {
        return amtErrors;
    }

    public void setAmtErrors(List<ReconClaimsData> amtErrors) {
        this.amtErrors = amtErrors;
    }

    public DataAndSummary getPayload() {
        return payload;
    }

    public void setPayload(DataAndSummary payload) {
        this.payload = payload;
    }

    public boolean isNextFlag() {
        return nextFlag;
    }

    public void setNextFlag(boolean nextFlag) {
        this.nextFlag = nextFlag;
    }

    public String getReconType() {
        return reconType;
    }

    public void setReconType(String reconType) {
        this.reconType = reconType;
    }
    
    public void validateReconData(){
        if (reconId != null && !reconId.trim().equals("")) {
            errors = reconEJB.validateReconData(reconId);
            if (errors != null) {
                zeroErrors = errors.getZeroErrors();
                nullErrors = errors.getNullErrors();
                amtErrors = errors.getAmtErrors();
            }
            payload = reconEJB.getReconClaimsDataByReconId(reconId);
            if(payload!=null)
            {
                List<ReconClaimsData> ls  = payload.getDataList();
                reconType = ls.get(0).getReconType();
             }
        }
    }
    
    public void deleteErrors(List<ReconClaimsData> ls){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try{
            reconEJB.deleteReconDataErrors(ls);
            validateReconData();
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "Errors have been successfully deleted."));
        }catch(Exception e){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error","Unable to delete errors. "+e.getMessage()));
        }
    }
    
    public void updateReconClaimsData(ReconClaimsDataPK key, Double newAmount,String notes){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String)session.getAttribute("username");
        try{
            reconEJB.updateReconClaimsData(key,newAmount,notes,user);
            validateReconData();
        }catch(AlreadyExistsException e){
           facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error",e.getMessage())); 
        }catch(Exception ex){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error","Unable to update the record. "+ex.getMessage())); 
        }
    }
    
    public void generateNewReconClaims(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String)session.getAttribute("username");
        try{
            reconEJB.generateNewReconClaims(reconId, user);
            validateReconData();
            setNextFlag(true);
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "Successfully processed generation of new recon claims."));
        }catch(AlreadyExistsException e){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error",e.getMessage())); 
        }catch(Exception ex){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error","Unable to generate new recon claims. "+ex.getMessage())); 
        }
    }
    
    public void updatePendingProductionClaims(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = Util.getSession();
        String user = (String)session.getAttribute("username");
        try{
            for(ReconClaimsData claim: zeroErrors){
                reconEJB.validateNewPayableAmount(claim);
            }
            reconEJB.updatePendingReconClaimsInProduction(reconId, user);
            validateReconData();
            setNextFlag(true);
            facesContext.addMessage("errorDeletions", new FacesMessage(FacesMessage.SEVERITY_INFO, "Information", "Successfully updated pending recon claims."));
        }catch(AlreadyExistsException e){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error",e.getMessage())); 
        }catch(Exception ex){
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Error","Unable to update pending recon claims. "+ex.getMessage())); 
        }
    }
}
