<%@ page session="true" %>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="pageTitle" scope="request" value="View Client Account Details"/>
<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="../common-head.jsp" />
	<c:url value="/" var="rootContext" />
	<c:url value="/portfolio/client/${clientId}" var="clientUrl" />
	<c:url value="/portfolio/client/${clientId}/note/all" var="allClientNotesUrl" />
	
<style>
div.notecontainer {wdith:300px;min-width:300px;max-width:400px;}
span.noteusername {font-weight:bold;font-style: italic;}
div.notespacer {margin-top: 5px; margin-bottom: 5px;}
</style>

<script>
$(document).ready(function() {
	
	$.views.registerHelpers({
		
		money: function(monetaryObj) {
			
			Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
			
			var digits = monetaryObj.currencyDigitsAfterDecimal.toFixed(0);
			return Globalize.format(monetaryObj.amount, "n" + digits); 
		},
		moneyWithCurrency: function(monetaryObj) {
			
			Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
			
			var digits = monetaryObj.currencyDigitsAfterDecimal.toFixed(0);
			return Globalize.format(monetaryObj.amount, "c" + digits); 
		},
		decimal: function(number, digits) {
		      try {
		    	return Globalize.format(number, "n" + digits); 
		      } catch(e) {
		        return number +"(NaN)";
		      }
		},
		number: function(number) {
		      try {
		    	  return Globalize.format(number, "n0"); 
		      } catch(e) {
		        return number +"(NaN)";
		      }
		},
	    numberGreaterThanZero: function(number) {
		      try {
		    	var num = number.toFixed(0);
		        return num > 0;
		      } catch(e) {
		        return false;
		      }
		},
		globalDate: function(dateParts) {
		      try {
		    	  if (dateParts === null) {
		    		  return "";
		    	  } else {
			    	  var year = dateParts[0];
			    	  var month = parseInt(dateParts[1]) - 1; // month is zero indexed
			    	  var day = dateParts[2];
			    	  
			    	  var d = new Date();
			    	  d.setFullYear(year,month,day);
			    	  
			    	  return Globalize.format(d,"dd MMMM yyyy");
		    	  }
		      } catch(e) {
		        return "??";
		      }
		},
		globalDateTime: function(dateParts) {
		      try {
		    	  if (dateParts === null) {
		    		  return "";
		    	  } else {
		    		  var d = new Date(dateParts);
			    	  
			    	  return Globalize.format(d,"F");
		    	  }
		      } catch(e) {
		        return "??";
		      }
		}
		
	});
	
	function removeErrors(placeholderDiv) {
		// remove error class from all input fields
		var $inputs = $('#entityform :input');
		
	    $inputs.each(function() {
	        $(this).removeClass("ui-state-error");
	    });
		
	  	$(placeholderDiv).html("");
	}
	
	function handleXhrError(jqXHR, textStatus, errorThrown, templateSelector, placeholderDiv) {
	  	if (jqXHR.status === 0) {
		    alert('No connection. Verify application is running.');
	  	} else if (jqXHR.status == 401) {
			alert('Unauthorized. [401]');
		} else if (jqXHR.status == 404) {
		    alert('Requested page not found. [404]');
		} else if (jqXHR.status == 405) {
			alert('HTTP verb not supported [405]: ' + errorThrown);
		} else if (jqXHR.status == 500) {
		    alert('Internal Server Error [500].');
		} else if (errorThrown === 'parsererror') {
		    alert('Requested JSON parse failed.');
		} else if (errorThrown === 'timeout') {
		    alert('Time out error.');
		} else if (errorThrown === 'abort') {
		    alert('Ajax request aborted.');
		} else {
			
			removeErrors(placeholderDiv);
			
		  	var jsonErrors = JSON.parse(jqXHR.responseText);
		  	
		  	var errorArray = new Array();
		  	var arrayIndex = 0;
		  	$.each(jsonErrors, function() {
		  	  var fieldId = '#' + this.field;
		  	  $(fieldId).addClass("ui-state-error");
		  	  
		  	  var errorObj = new Object();
		  	  errorObj.field = this.field;
		  	  errorObj.code = this.code;
		  	  errorObj.message = jQuery.i18n.prop(this.code, this.args);
		  	  errorObj.value = this.value;
		  	  
		  	  errorArray[arrayIndex] = errorObj;
		  	  arrayIndex++
		  	});
		  	
		  	var templateArray = new Array();
		  	var templateErrorObj = new Object();
		  	templateErrorObj.title = jQuery.i18n.prop('error.msg.header');
		  	templateErrorObj.errors = errorArray;
		  	
		  	templateArray[0] = templateErrorObj;
		  	
		  	var formErrorsHtml = $(templateSelector).render(templateArray);
		  	
		  	$(placeholderDiv).append(formErrorsHtml);
		}
	}	
	
	function showNotAvailableDialog(titleCode) {
		var dialogDiv = $("<div id='notavailable-dialog-form'></div>");
		
		dialogDiv.append("<p>" + jQuery.i18n.prop('dialog.messages.functionality.not.available') + "</p>");
		
		var okButton = jQuery.i18n.prop('dialog.button.ok');
		
		var buttonsOpts = {};
		buttonsOpts[okButton] = function() {$(this).dialog("close");};
		
		dialogDiv.dialog({
	  		title: jQuery.i18n.prop(titleCode), 
	  		width: 300, 
	  		height: 200, 
	  		modal: true,
	  		buttons: buttonsOpts,
	  		close: function() {
	  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
	  			$(this).remove();
			}
		 }).dialog('open');
	}
	
	function popupConfirmationDialogAndPost(url, titleCode, width, height, tabIndex, redirectUrl) {
		  var dialogDiv = $("<div id='dialog-form'><div id='formerrors'></div>" + jQuery.i18n.prop('text.confirmation.required') + "</div>");
		  
		  	var confirmButton = jQuery.i18n.prop('dialog.button.confirm');
			var cancelButton = jQuery.i18n.prop('dialog.button.cancel');
			
			var buttonsOpts = {};
			buttonsOpts[confirmButton] = function() {
				 var jqxhr = $.ajax({
					  url: url,
					  type: 'POST',
					  success: function(data, textStatus, jqXHR) {
						  dialogDiv.dialog("close");
						  if (tabIndex > 0) {
							  $newtabs.tabs('load', tabIndex);
						  } else {
							  window.location.href = redirectUrl;
						  }
					  }
				 });
				 
				 jqxhr.error(function(jqXHR, textStatus, errorThrown) {
						handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
				 });
			};
			
			buttonsOpts[cancelButton] = function() {$(this).dialog( "close" );};
		  
		  
		  dialogDiv.dialog({
		  		title: jQuery.i18n.prop(titleCode), 
		  		width: width, 
		  		height: height, 
		  		modal: true,
		  		buttons: buttonsOpts,
		  		close: function() {
		  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
		  			$(this).remove();
				},
		  		open: function (event, ui) {}
		  	}).dialog('open');
	 }
	
	function popupDialogWithPostOnlyFormView(url, titleCode, templateSelector, width, height, tabIndex, minOffset, defaultOffset, maxOffset) {
		var dialogDiv = $("<div id='dialog-form'></div>");
		
		var data = new Object();
		var formHtml = $(templateSelector).render(data);
		
		dialogDiv.append(formHtml);
		
		var saveButton = jQuery.i18n.prop('dialog.button.save');
		var cancelButton = jQuery.i18n.prop('dialog.button.cancel');
		
		var buttonsOpts = {};
		buttonsOpts[saveButton] = function() {
			$('.multiSelectedItems option').each(function(i) {  
		    	   $(this).attr("selected", "selected");  
		    });
  				
  			var form_data = $('#entityform').serialize();
  				 
			var jqxhr = $.ajax({
				  url: url,
				  type: 'POST',
				  data: form_data,
				  success: function(data, textStatus, jqXHR) {
					  dialogDiv.dialog("close");
					  $newtabs.tabs('load', tabIndex);
				  }
			});
			
			jqxhr.error(function(jqXHR, textStatus, errorThrown) {
				handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
			});
		};
		
		buttonsOpts[cancelButton] = function() {$(this).dialog( "close" );};
		
		dialogDiv.dialog({
		  		title: jQuery.i18n.prop(titleCode), 
		  		width: width, 
		  		height: height, 
		  		modal: true,
		  		buttons: buttonsOpts,
		  		close: function() {
		  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
		  			$(this).remove();
				},
		  		open: function (event, ui) {
		  			$('.multiadd').click(function() {  
		  			     return !$('.multiNotSelectedItems option:selected').remove().appendTo('#selectedItems');  
		  			});
		  			
		  			$('.multiremove').click(function() {  
		  				return !$('.multiSelectedItems option:selected').remove().appendTo('#notSelectedItems');  
		  			});
		  			
		  			$('.datepickerfield').datepicker({constrainInput: true, minDate: minOffset, defaultDate: defaultOffset, maxDate: maxOffset, dateFormat: 'dd MM yy'});
		  			
		  			$("#entityform textarea").first().focus();
		  			$('#entityform input').first().focus();
		  		}
		  }).dialog('open');
	}
	
	function popupDialogWithFormView(url, titleCode, templateSelector, width, height, tabIndex, minOffset, defaultOffset, maxOffset) {
		 var dialogDiv = $("<div id='dialog-form'></div>");
		 var jqxhr = $.ajax({
			url: url,
			type: 'GET',
			contentType: 'application/json',
			dataType: 'json',
			cache: false,
			success: function(data, textStatus, jqXHR) {
			
			var formHtml = $(templateSelector).render(data);
			
			dialogDiv.append(formHtml);
			
			var saveButton = jQuery.i18n.prop('dialog.button.save');
			var cancelButton = jQuery.i18n.prop('dialog.button.cancel');
			
			var buttonsOpts = {};
			buttonsOpts[saveButton] = function() {
				$('.multiSelectedItems option').each(function(i) {  
			    	   $(this).attr("selected", "selected");  
			    });
	  				
	  			var form_data = $('#entityform').serialize();
	  				 
				var jqxhr = $.ajax({
					  url: url,
					  type: 'POST',
					  data: form_data,
					  success: function(data, textStatus, jqXHR) {
						  dialogDiv.dialog("close");
						  $newtabs.tabs('load', tabIndex);
					  }
				});
				
				jqxhr.error(function(jqXHR, textStatus, errorThrown) {
					handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
				});
			};
			
			buttonsOpts[cancelButton] = function() {$(this).dialog( "close" );};
			
			dialogDiv.dialog({
			  		title: jQuery.i18n.prop(titleCode), 
			  		width: width, 
			  		height: height, 
			  		modal: true,
			  		buttons: buttonsOpts,
			  		close: function() {
			  			// if i dont do this, theres a problem with errors being appended to dialog view second time round
			  			$(this).remove();
					},
			  		open: function (event, ui) {
			  			$('.multiadd').click(function() {  
			  			     return !$('.multiNotSelectedItems option:selected').remove().appendTo('#selectedItems');  
			  			});
			  			
			  			$('.multiremove').click(function() {  
			  				return !$('.multiSelectedItems option:selected').remove().appendTo('#notSelectedItems');  
			  			});
			  			
			  			$('.datepickerfield').datepicker({constrainInput: true, minDate: minOffset, defaultDate: defaultOffset, maxDate: maxOffset, dateFormat: 'dd MM yy'});
			  			
			  			$("#entityform textarea").first().focus();
			  			$('#entityform input').first().focus();
			  			
			  			calculateAnnualPercentageRate();
			  		}
			  	}).dialog('open');
		  }
		 });
		 
		jqxhr.error(function(jqXHR, textStatus, errorThrown) {
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	} 
	
	// "tabTemplate": '<li><a href="<%= "#" %>{href}"><%= "#" %>{label}</a> <span class="ui-icon ui-icon-close">Remove Tab</span></li>',
	var tab_counter = 1;
	var $newtabs = $("#newtabs").tabs({
		
		"add": function( event, ui ) {
			$newtabs.tabs('select', '#' + ui.panel.id);
		},
		"ajaxOptions": {
			type: 'GET',
			dataType: 'json',
			contentType: 'application/json',
			cache: false,
	        error: function(jqXHR, status, errorThrown, index, anchor) {
	        	handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
	        	
	            $(anchor.hash).html("error occured while ajax loading.");
	        },
	        success: function(data, status, xhr) {
	        	var currentTabIndex = $newtabs.tabs('option', 'selected');
	            var currentTabAnchor = $newtabs.data('tabs').anchors[currentTabIndex];
	            //$(currentTabAnchor).data('cache.tabs', true)
				//alert('tab: ' + currentTabIndex + ' - id: ' + currentTabAnchor);
	            
	            var offsetToSubmittedDate = 0;
	            var offsetToApprovalDate = 0;
	            var offsetToDisbursalDate = 0;
				var maxOffset = 0; // today
				
	            if (currentTabIndex < 1) {
	        		var tableHtml = $("#clientDataTabTemplate").render(data);
					$("#clienttab").html(tableHtml);
					var extraDataParams = {
							url: '${rootContext}',
							datasetType: "portfolio_client",
							datasetPKValue: data.clientInfo.id,
							datasetTypeDiv: "clientadditionaldata", 
							headingPrefix: "", 
							headingClass: "", 
							labelClass: "longrowlabel",
							valueClass:	"rowvalue"					
					};
					jQuery.stretchyData.displayAllExtraData(extraDataParams);

					$('.casflowbtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("cashflowbtn", "");
						var url = '${rootContext}portfolio/client/' + clientId + '/cashflow/new';
						window.location.href = url;
					    e.preventDefault();
					});
					$('button.casflowbtn span').text(jQuery.i18n.prop('dialog.button.new.cashflow.analysis'));
					
					$('.newloanbtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("newloanbtn", "");
						var url = '${rootContext}portfolio/client/' + clientId + '/loan/new';
						window.location.href = url;
					    e.preventDefault();
					});
					$('button.newloanbtn span').text(jQuery.i18n.prop('dialog.button.new.loan.application'));
					
					$('.addnotebtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("addnotebtn", "");
						var url = '${rootContext}portfolio/client/' + clientId + '/note/new';
						
						var templateSelector = "#noteFormTemplate";
						var width = 600; 
						var height = 400;
						var defaultOffset = data.clientInfo.maxJoinedOnOffsetFromToday;
						popupDialogWithFormView(url, "dialog.title.add.note", templateSelector, width, height, currentTabIndex, defaultOffset, defaultOffset, maxOffset);
					    e.preventDefault();
					});
					$('button.addnotebtn span').text(jQuery.i18n.prop('dialog.button.add.note'));
					
		            $("a.openloanaccount").click( function(e) {
						var tab_title = $(this).attr('title');
					    var tab_href = this.href;
					    
						$newtabs.tabs( "add", tab_href, tab_title );
						tab_counter++;
						e.preventDefault();
					});
					
		            refreshNoteWidget();
					
	        	} else {
	        		
	        		var tableHtml = $("#loanDataTabTemplate").render(data);
	        		
	        		var curTab = $('#newtabs .ui-tabs-panel:not(.ui-tabs-hide)');
	        		curTab.html(tableHtml);
	        		
					var extraDataParams = {
							url: '${rootContext}',
							datasetType: "portfolio_loan",
							datasetPKValue: data.id,
							datasetTypeDiv: "loanadditionaldata" + data.id, 
							headingPrefix: "", 
							headingClass: "", 
							labelClass: "longrowlabel",
							valueClass:	"rowvalue"					
					};
					jQuery.stretchyData.displayAllExtraData(extraDataParams);
					
	        		var curTabID = curTab.prop("id")
	        		
	        		offsetToSubmittedDate = data.maxSubmittedOnOffsetFromToday;
	        		offsetToApprovalDate = data.maxApprovedOnOffsetFromToday;
	        		offsetToDisbursalDate = data.maxDisbursedOnOffsetFromToday;
	        		
	        		$('.rejectloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("rejectbtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/reject';
						var templateSelector = "#rejectLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(url, 'dialog.title.reject.loan', templateSelector, width, height, currentTabIndex, offsetToSubmittedDate, defaultOffset, maxOffset);
					    e.preventDefault();
					});
	        		$('button.rejectloan span').text(jQuery.i18n.prop('dialog.button.reject.loan'));
					
					$('.withdrawnbyapplicantloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("withdrawnbyapplicantloanbtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/withdraw';
						var templateSelector = "#withdrawnByClientLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(url, 'dialog.title.loan.withdrawn.by.client', templateSelector, width, height, currentTabIndex, offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.withdrawnbyapplicantloan span').text(jQuery.i18n.prop('dialog.button.withdrawn.by.client.loan'));
					
					$('.approveloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("approvebtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/approve';
						var templateSelector = "#approveLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(url, 'dialog.title.approve.loan', templateSelector, width, height, currentTabIndex, offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.approveloan span').text(jQuery.i18n.prop('dialog.button.approve.loan'));
					
					$('.deleteloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("deletebtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/delete';
						var width = 400; 
						var height = 225;
						
						var redirectUrl = '${clientUrl}';
						
						popupConfirmationDialogAndPost(url, 'dialog.title.confirmation.required', width, height, 0, redirectUrl);
					    e.preventDefault();
					});
					$('button.deleteloan span').text(jQuery.i18n.prop('dialog.button.delete.loan'));
					
					$('.undoapproveloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("undoapprovebtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/undoapproval';
						var width = 400; 
						var height = 225;
						popupConfirmationDialogAndPost(url, 'dialog.title.undo.loan.approval', width, height, currentTabIndex);
					    e.preventDefault();
					});
					$('button.undoapproveloan span').text(jQuery.i18n.prop('dialog.button.undo.loan.approval'));
					
					$('.undodisbursalloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("undodisbursalbtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/undodisbursal';
						var width = 400; 
						var height = 150;
						popupConfirmationDialogAndPost(url, 'dialog.title.undo.loan.disbursal', width, height, currentTabIndex);
					    e.preventDefault();
					});
					$('button.undodisbursalloan span').text(jQuery.i18n.prop('dialog.button.undo.loan.disbursal'));
					
					$('.disburseloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("disbursebtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/disburse';
						var templateSelector = "#disburseLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						popupDialogWithPostOnlyFormView(url, 'dialog.title.disburse.loan', templateSelector, width, height, currentTabIndex, offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.disburseloan span').text(jQuery.i18n.prop('dialog.button.disburse.loan'));
					
					$('.repaymentloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("repaymentbtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/repayment';
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						popupDialogWithFormView(url, 'dialog.title.loan.repayment', templateSelector, width, height, currentTabIndex, offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.repaymentloan span').text(jQuery.i18n.prop('dialog.button.loan.repayment'));
					
					$('.adjustloanrepayment').button().click(function(e) {
						
						var linkId = this.id;
						var loanAndRepaymentId = linkId.replace("adjustrepaymentbtn", "");
						
						var ids = loanAndRepaymentId.split("_");
						
						var url = '${rootContext}portfolio/loan/' + ids[0] + '/repayment/' + ids[1] + '/adjust';
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						popupDialogWithFormView(url, 'dialog.title.adjust.loan.repayment', templateSelector, width, height, currentTabIndex, offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.adjustloanrepayment span').text(jQuery.i18n.prop('dialog.button.adjust.loan.repayment'));
					
					$('.waiveloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("waivebtn", "");
						var url = '${rootContext}portfolio/loan/' + loanId + '/waive';
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						popupDialogWithFormView(url, 'dialog.title.waive.loan', templateSelector, width, height, currentTabIndex, offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.waiveloan span').text(jQuery.i18n.prop('dialog.button.loan.waive'));
					
					var $loantabs = $(".loantabs").tabs({
						"show": function(event, ui) {
							
							var curTab = $('#newtabs .ui-tabs-panel:not(.ui-tabs-hide)');
			        		var curTabID = curTab.prop("id")
						}
					});
	        	}
	        }
	    }
	});
	
	function refreshNoteWidget() {
		
		var noteArray = new Array();
	  	var arrayIndex = 0;
	  	
	  	var jqxhr = $.ajax({
			  url: '${allClientNotesUrl}',
			  type: 'GET',
			  contentType: 'application/json',
			  dataType: 'json',
			  cache: false,
			  success: function(data, textStatus, jqXHR) {
				  
				  var noteParent = new Object();
				  noteParent.title = jQuery.i18n.prop('widget.notes.heading');
				  noteParent.items = data;
				  
				  var tableHtml = $("#noteListViewTemplate").render(noteParent);
				  $("#clienttabrightpane").html(tableHtml);
				  
				  $('.editclientnote').click(function(e) {
						var linkId = this.id;
						var noteId = linkId.replace("editclientnotelink", "");
						var url = '${rootContext}portfolio/client/${clientId}/note/' + noteId;
						var templateSelector = "#noteFormTemplate";
						var width = 600;
						var height = 400;
						var defaultOffset = -400; // would be nice to return the max allowed offset for date for client note
						var maxOffset = 0;
						var currentTabIndex = 0;
						popupDialogWithFormView(url, "dialog.title.edit.note", templateSelector, width, height, currentTabIndex, defaultOffset, defaultOffset, maxOffset);
					    e.preventDefault();
			      });
			  }
		 });
	  	
	  	jqxhr.error(function(jqXHR, textStatus, errorThrown) {
			handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
		});
	}
	
	function calculateAnnualPercentageRate() {
	//	alert('calculating interest');
		var periodInterestRate = parseFloat($('#nominalInterestRate').val());
		if (isNaN(periodInterestRate)) {
			periodInterestRate = 0;
		}
		
		var periodsInYear = 12;
		var periodType = $('#selectedInterestFrequencyOption').val();
		if (periodType == 3) {
			periodsInYear = 1;
		} else if (periodType == 2) {
			periodsInYear = 12;
		} else if (periodType == 1) {
			periodsInYear = 52;
		}
		
		var apr = parseFloat(periodsInYear * periodInterestRate);
        $('#interestRatePerYear').val(Globalize.format(apr, "n4"));
	}
});
</script>
</head>
<body>

<div id="container">
	<jsp:include page="../top-navigation.jsp" />

	<div style="float:none; clear:both;">
		<div id="spacer" style="line-height: 15px;">&nbsp;</div>
		
		<div id="content">
		
			<div id="newtabs">
				<ul>
					<li><a href="${clientUrl}" title="clienttab">${clientDisplayName}</a></li>
				</ul>
				<div id="clienttab">
				</div>
			</div>
		</div>
	</div>
</div>
<jsp:include page="../view-templates.jsp" />

</body>
</html>