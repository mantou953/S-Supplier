package com.seeyon.apps.wxsupplier.workflow;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.wxsupplier.manager.WxSupplierManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.workflow.event.AbstractWorkflowEvent;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import com.seeyon.ctp.workflow.event.WorkflowEventResult;

/**
 * 微信供应商签名验证
 */
public class WxSupplierSignCheckWorkFlowEvent extends AbstractWorkflowEvent {

	protected Log logger = LogFactory.getLog(this.getClass());
	
	private WxSupplierManager wxSupplierManager;

	public WxSupplierManager getWxSupplierManager() {
		return wxSupplierManager;
	}

	public void setWxSupplierManager(WxSupplierManager wxSupplierManager) {
		this.wxSupplierManager = wxSupplierManager;
	}
	
	public ApplicationCategoryEnum getAppName() {
		return ApplicationCategoryEnum.form;
	}

	public String getId() {
		return AppContext.getSystemProperty("wxsupplier.signCheckWorkFlowEventId");
	}

	public String getLabel() {
		return AppContext.getSystemProperty("wxsupplier.signCheckWorkFlowEventLabel");
	}
	
	// 处理前事件
	public WorkflowEventResult onBeforeFinishWorkitem(WorkflowEventData data) {
		WorkflowEventResult workflowEventResult = null;
		try {
			// 流程ID
			Long summaryId = data.getSummaryId();
			logger.info("节点处理前事件：供应商签名验证， summaryId=" + summaryId);
			
			// 表单数据
			Map<String, Object> formmainData = data.getBusinessData();

			// 如果没有签名
			if (!wxSupplierManager.checkSignName(summaryId, formmainData)) {
				workflowEventResult = new WorkflowEventResult("供应商未签名");
			}
		} catch (Throwable e) {
			logger.error("供应商签名验证失败", e);
			workflowEventResult = new WorkflowEventResult("供应商签名验证失败");
		}
		return workflowEventResult;
	}

}
