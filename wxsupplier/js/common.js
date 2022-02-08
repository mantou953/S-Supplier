function request(paramName) {
	var url = location.href;
	var queryString = url.substring(url.indexOf("?") + 1, url.length).split("&");
	var map = {}
	for (i = 0; j = queryString[i]; i++) {
		map[j.substring(0, j.indexOf("="))] = j.substring(j.indexOf("=") + 1, j.length);
	}
	var returnValue = map[paramName];
	if (typeof (returnValue) == "undefined") {
		return "";
	} else {
		return returnValue;
	}
}

function fixMath(m, n, op) {
	var a = (m + "");
	var b = (n + "");
	var x = 1;
	var y = 1;
	var c = 1;
	if (a.indexOf(".") > 0) {
		x = Math.pow(10, a.length - a.indexOf(".") - 1);
	}
	if (b.indexOf(".") > 0) {
		y = Math.pow(10, b.length - b.indexOf(".") - 1);
	}
	switch (op) {
	case '+':
	case '-':
		c = Math.max(x, y);
		m = Math.round(m * c);
		n = Math.round(n * c);
		break;
	case '*':
		c = x * y
		m = Math.round(m * x);
		n = Math.round(n * y);
		break;
	case '/':
		c = Math.max(x, y);
		m = Math.round(m * c);
		n = Math.round(n * c);
		c = 1;
		break;
	}
	return eval("(" + m + op + n + ")/" + c);
}

function getString(value, defaultValue) {
	if (value) {
		return value;
	}
	if (defaultValue) {
		return defaultValue;
	} else {
		return "";
	}
}

function getNumber(value, defaultValue, fixed) {
	if (value) {
		value = window.parseFloat(value);
	} else {
		if (defaultValue) {
			value = window.parseFloat(defaultValue);
		} else {
			return "";
		}
	}
	if (fixed) {
		value = value.toFixed(fixed);
	}
	return value;
}

function getBoolean(value, defaultValue) {
	if (value) {
		if (value === true) {
			return true;
		} else {
			return false;
		}
	} else {
		return defaultValue;
	}
}

function openView(url, title, width, height) {
	layer.open({
		type : 2,
		title : '<font style="font-size:16px;font-style:italic;">' + title + '</font>',
		shade : 0.3,
		shadeClose : false,
		maxmin : false,
		area : [ width, height ],
		content : url
	});
	return false;
}

var layerIndex = null;

/*AJAX统一控制*/
$(function() {
	$.ajaxSetup({
		complete : function(xhr, status) {
			var win = (window.self == window.top ? window : window.top);
			if (xhr.status == 911) {
				layer.alert("\u7531\u4E8E\u60A8\u957F\u65F6\u95F4\u6CA1\u6709\u64CD\u4F5C,\u8BF7\u91CD\u65B0\u767B\u5F55\uFF01", function(index){
					layer.close(index);
					win.location.href = "/";
				});
			} else if(xhr.status == 401 || xhr.status == 403){
				layer.alert("\u60A8\u7684\u8BBF\u95EE\u672A\u88AB\u6388\u6743,\u8BF7\u8054\u7CFB\u7BA1\u7406\u5458\uFF01", function(index){
					layer.close(index);
				});
			}  else if(xhr.status == 404){
				layer.alert("\u60A8\u8BBF\u95EE\u7684\u9875\u9762\u4E0D\u5B58\u5728,\u8BF7\u8054\u7CFB\u7BA1\u7406\u5458\uFF01", function(index){
					layer.close(index);
				});
			} else if(xhr.status == 500 || status == "error"){
				layer.alert("\u7CFB\u7EDF\u9519\u8BEF\uFF0C\u8BF7\u68C0\u67E5\u7F51\u7EDC\u6216\u8054\u7CFB\u7BA1\u7406\u5458\uFF01", function(index){
					layer.close(index);
				});
			}
		}
	});
});

function ajaxLoad(url, param, type, async, callback, showLoading, loadingMsg) {
	// 请求类型
	type = getString(type, "POST");   
	
	// 是否同步请求
	async = getBoolean(async, false); 
	
	// 加载提示
	if (showLoading && showLoading == true) {
		loadingMsg = getString(loadingMsg, "\u6B63\u5728\u5904\u7406\uFF0C\u8BF7\u7A0D\u540E...");   
		layerIndex = layer.msg(loadingMsg, { icon : 16, time : 0, shade : 0.3 });
	}
	
	// 发起请求
	return $.ajax({
		type : type,
		url : url,
		async : async,
		dataType : "JSON",
		data : param,
		success : function(data) {
			if (data.code && data.code === 200) {
				layer.close(layerIndex);
				if (typeof callback === "function") {
					callback(data.data);
				} else {
					try {
						window[callback](data.data);
					} catch (e) {

					}
				}
			} else {
				layer.alert(data.message, function(index) {
					layer.close(index);
					layer.close(layerIndex);
				});
			}
		},
		failure : function(data) {
			layer.close(layerIndex);
		}
	});
}

function ajaxGet(url, param, callback, showLoading, loadingMsg){
	return ajaxLoad(url, param, "GET", false, callback, showLoading, loadingMsg);
}

function ajaxPost(url, param, callback, showLoading, loadingMsg){
	return ajaxLoad(url, param, "POST", false, callback, showLoading, loadingMsg);
}

function setFieldValue(fieldElement, fieldValue) {
	$(fieldElement).text(fieldValue);
}

function getFieldValue(data, field, fieldType, fieldDefault, fieldFixed) {
	var value = null;
	if (typeof (data) == "object") {
		var fieldArray = field.split(".");
		for (var i = 0; i < fieldArray.length; i++) {
			data = data[fieldArray[i]];
			if (i == fieldArray.length - 1) {
				if (typeof (data) != "undefined") {
					value = data;
				}
			} else {
				if (typeof (data) == "object") {
					continue;
				} else {
					break;
				}
			}
		}
	}
	if ("number" == fieldType) {
		value = getNumber(value, fieldDefault, fieldFixed);
	} else {
		value = getString(value, fieldDefault);
	}
	return value;
}

function findField(element) {
	var fieldList = $(element).find("[field]");
	var fieldArray = [];
	fieldList.each(function() {
		var object = $(this);
		var field = object.attr("field");
		var fieldType = object.attr("fieldType");
		var fieldFixed = object.attr("fieldFixed");
		var fieldDefault = object.attr("fieldDefault");
		var callback = object.attr("callback");
		fieldArray.push({
			object : object,
			field : field,
			fieldType : fieldType,
			fieldFixed : fieldFixed,
			fieldDefault : fieldDefault,
			callback : callback
		});
	});
	return fieldArray;
}

function loadElementData(elementId, url, param, type, showLoading, loadingMsg) {
	// 请求类型
	type = getString(type, "POST");   
	
	// 加载提示
	if (showLoading && showLoading == true) {
		loadingMsg = getString(loadingMsg, "\u6B63\u5728\u5904\u7406\uFF0C\u8BF7\u7A0D\u540E...");   
		layerIndex = layer.msg(loadingMsg, { icon : 16, time : 0, shade : 0.3 });
	}
	
	// 发起请求
	return $.ajax({
		type : type,
		url : url,
		async : false,
		dataType : "JSON",
		data : param,
		success : function(data) {
			if (data.code && data.code === 200) {
				layer.close(layerIndex);
				var elementIdArray = elementId.split(",");
				for (var ei = 0; ei < elementIdArray.length; ei++) {
					var element = $("#" + elementIdArray[ei]);
					var fieldArray = findField(element);
					if (fieldArray && fieldArray.length && fieldArray.length > 0) {
						var returnData = data.data;
						for (var i = 0; i < fieldArray.length; i++) {
							var field = fieldArray[i];
							try {
								if (field.callback && field.callback.length > 0) {
									window[field.callback](returnData);
								} else {
									var fieldValue = getFieldValue(returnData, field.field, field.fieldType, field.fieldDefault, field.fieldFixed);
									setFieldValue(field.object, fieldValue);
								}
							} catch (e) {

							}
						}
					}
				}
			} else {
				layer.alert(data.message, function(index) {
					layer.close(index);
					layer.close(layerIndex);
				});
			}
		},
		failure : function(data) {
			layer.close(layerIndex);
		}
	});
}

function loadTableData(elementId, url, param, type, showLoading, loadingMsg) {
	// 请求类型
	type = getString(type, "POST");   
	
	// 加载提示
	if (showLoading && showLoading == true) {
		loadingMsg = getString(loadingMsg, "\u6B63\u5728\u5904\u7406\uFF0C\u8BF7\u7A0D\u540E...");   
		layerIndex = layer.msg(loadingMsg, { icon : 16, time : 0, shade : 0.3 });
	}
	
	// 发起请求
	return $.ajax({
		type : type,
		url : url,
		async : false,
		dataType : "JSON",
		data : param,
		success : function(data) {
			if (data.code && data.code === 200) {
				layer.close(layerIndex);
				var elementIdArray = elementId.split(",");
				for (var i = 0; i < elementIdArray.length; i++) {
					appendDataToTable(elementIdArray[i], data.data);
				}
			} else {
				layer.alert(data.message, function(index) {
					layer.close(index);
					layer.close(layerIndex);
				});
			}
		},
		failure : function(data) {
			layer.close(layerIndex);
		}
	});
}

function appendDataToTable(tableId, dataList) {
	var table = $("#" + tableId);
	if (!table) {
		return;
	}

	// 获取定义的属性字段
	var fieldArray = findField(table);

	// 先清空数据行
	table.find("tr.data").remove();

	// 没有数据
	if (!dataList || !dataList.length || dataList.length == 0) {
		table.append('<tr class="data"><td class="none" colspan="' + fieldArray.length + '">暂无数据</td></tr>');
		return;
	}

	// 显示数据
	if (fieldArray && fieldArray.length && fieldArray.length > 0) {
		var html = null;
		for (var i = 0; i < dataList.length; i++) {
			html = '<tr class="data">';
			for (var j = 0; j < fieldArray.length; j++) {
				var field = fieldArray[j];
				var value = '';
				try {
					if (field.callback && field.callback.length > 0) {
						value = window[field.callback](dataList[i], i, dataList);
					} else {
						value = getFieldValue(dataList[i], field.field, field.fieldType, field.fieldDefault, field.fieldFixed);
					}
				} catch (e) {

				}
				html += '<td>' + value + '</td>';
			}
			html += '</tr>';
			table.append(html);
		}
	}
}