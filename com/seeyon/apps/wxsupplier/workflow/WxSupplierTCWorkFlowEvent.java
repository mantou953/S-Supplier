package com.seeyon.apps.wxsupplier.workflow;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.collaboration.po.ColSummary;
import com.seeyon.apps.wxsupplier.manager.WxSupplierManager;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.workflow.event.AbstractWorkflowEvent;
import com.seeyon.ctp.workflow.event.WorkflowEventData;
import com.seeyon.ctp.workflow.event.WorkflowEventResult;

/**
 * 退仓单变更审批表
 */
public class WxSupplierTCWorkFlowEvent extends AbstractWorkflowEvent {

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
		return AppContext.getSystemProperty("wxsupplier.tcWorkFlowEventId");
	}

	public String getLabel() {
		return AppContext.getSystemProperty("wxsupplier.tcWorkFlowEventLabel");
	}
	
	// 消息推送
	private void sendMessage(WorkflowEventData data) {
		try {
			// 流程ID
			long summaryId = data.getSummaryId();
			if (summaryId == 0) {
				if (data.getSummaryObj() != null) {
					ColSummary summary = (ColSummary) data.getSummaryObj();
					summaryId = summary.getId();
				}
			}

			// 表单数据
			Map<String, Object> formmainData = data.getBusinessData();

			// 表单类型
			String formmainType = "tc";

			wxSupplierManager.sendMessage(summaryId, formmainData, formmainType);
		} catch (Throwable e) {
			logger.error("退仓单变更审批表签名确认的消息推送失败", e);
		}
	}
	
	// 发起事件
	public void onStart(WorkflowEventData data) {
		sendMessage(data);
	}

	// 处理前事件
	public WorkflowEventResult onBeforeFinishWorkitem(WorkflowEventData data) {
		sendMessage(data);
		return null;
	}
	
	// 处理事件
	public void onFinishWorkitem(WorkflowEventData data) {
		sendMessage(data);
	}

	// 结束事件
	public void onProcessFinished(WorkflowEventData data) {
		sendMessage(data);
	}

}
