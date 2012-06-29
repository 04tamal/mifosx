/* not used
queryParam = (function(){
    			var query = {}, pair, search = location.search.substring(1).split("&"), i = search.length;
    			while (i--) {
        			pair = search[i].split("=");
        			query[pair[0]] = decodeURIComponent(pair[1]);
    			}
    			return query;
			})();
*/


formErrorFunction = function(jqXHR, textStatus, errorThrown) {
				    	handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
				};

function executeAjaxRequest(url, verbType, jsonData, basicAuthKey, successFunction, errorFunction) { 

	var jqxhr = $.ajax({ 
				url : url, 
				type : verbType, //POST, GET, PUT or DELETE 
				contentType : "application/json; charset=utf-8", 
				dataType : 'json', 
				data : jsonData, 
				cache : false, 
				beforeSend : function(xhr) { 
						xhr.setRequestHeader("Authorization", "Basic " + basicAuthKey); 
					}, 
				success : successFunction, 
				error : errorFunction 
			}); 
}


function showILLogon(apiUrl, logonDivName) {

	var htmlVar = '<form name = "logonform"><table><tr><td>User Name:</td><td><input type="text" name="username"></td></tr>';
	htmlVar += '<tr><td>Password: </td><td><input type="password" name="password"></td></tr>';
	htmlVar += '<tr><td><input type="button" value="Logon" name="Submit" ';
	htmlVar += 'onclick= "setBasicAuthKey(' + "'" + apiUrl + "', '" + logonDivName + "'" + ', document.logonform.username.value, document.logonform.password.value )"></td><td></td></tr></table></form>';

	$("#" + logonDivName).html(htmlVar);
}


function setBasicAuthKey(apiUrl, logonDivName, username, password) 
{ 
	base64 = "";
	var jqxhr = $.ajax({ 
		url : apiUrl +  "authentication?username=" + username + "&password=" + password, 
		type : 'POST', 
		contentType : "application/json; charset=utf-8", 
		dataType : 'json', 
		data : "{}", 
		cache : false, 
		success : function(data, textStatus, jqXHR) { 
				base64 = data.base64EncodedAuthenticationKey; 
				$("#" + logonDivName).hide();
				$("#container").show();
				showILClientListing(apiUrl);
				return false;
			},
		error : function(jqXHR, textStatus, errorThrown) {
				alert("Invalid Username/Password"); 
				return true;
		} 
	});
}




function setClientListingContent(divName) {
	var htmlVar = '<button id="addclient" style="clear: both;">Add a new client</button>';
	htmlVar += '<div id="tabs"><ul><li><a href="#searchtab" title="searchtab">Search</a></li></ul><div id="searchtab"></div></div>';

	$("#" + divName).html(htmlVar);
}

function setClientContent(divName) {

	var htmlVar = '<div id="newtabs">	<ul><li><a href="nothing"'; 
	htmlVar += ' title="clienttab" class="topleveltab"><span id="clienttabname">Loading...</span></a></li></ul><div id="clienttab"></div></div>';
	$("#" + divName).html(htmlVar);
}


function setAddLoanContent(divName) {

	var htmlVar = '<div id="inputarea"></div><div id="schedulearea"></div>'
	$("#" + divName).html(htmlVar);
}








function jsViewsRegisterHelpers() {

	// these helpers are registered for the jsViews and jsRender functionality to fix bug with display zero!
	$.views.registerHelpers({
			
			money: function(monetaryObj) {
				
				Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
				
				var digits = monetaryObj.digitsAfterDecimal.toFixed(0);
				return Globalize.format(monetaryObj.amount, "n" + digits); 
			},
			moneyWithCurrency: function(monetaryObj) {
				
				Globalize.culture().numberFormat.currency.symbol = monetaryObj.displaySymbol;
				
				var digits = monetaryObj.digitsAfterDecimal.toFixed(0);
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
			    	  if (undefined != dateParts)
				  {
			    	  	var year = dateParts[0];
			    	  	var month = parseInt(dateParts[1]) - 1; // month is zero indexed
			    	  	var day = dateParts[2];
			    	  
			    	  	var d = new Date();
			    	  	d.setFullYear(year,month,day);
			    	  
			    	  	return Globalize.format(d,"dd MMMM yyyy");
				  }
				  else return "";
			      } catch(e) {
			        return "??";
			      }
			},
			globalDateAsISOString: function(localDateAsISOString) {
				
			      try {
			    	  var dateParts = localDateAsISOString.split("-")
			    	  var year = dateParts[0];
			    	  var month = parseInt(dateParts[1]) - 1; // month is zero indexed
			    	  var day = dateParts[2];
			    	  
			    	  var d = new Date();
			    	  d.setFullYear(year,month,day);
			    	  
			    	  return Globalize.format(d,"dd MMMM yyyy");
			      } catch(e) {
			        return "??";
			      }
			},
			globalDateTime: function(dateInMillis) {
			      try {
			    	  var d = new Date(dateInMillis);
			    	  
			    	  return Globalize.format(d,"F");
			      } catch(e) {
			        return "??";
			      }
			}
	});
}

		
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
		  	console.log(jsonErrors);
		  	var valErrors = jsonErrors.errors;
		  	console.log(valErrors);
		  	var errorArray = new Array();
		  	var arrayIndex = 0;
		  	$.each(valErrors, function() {
		  	  var fieldId = '#' + this.parameterName;
		  	  $(fieldId).addClass("ui-state-error");
		  	  
		  	  var errorObj = new Object();
		  	  errorObj.field = this.parameterName;
		  	  errorObj.code = this.userMessageGlobalisationCode;
		  	  
		  	  var argArray = new Array();
		  	  var argArrayIndex = 0;
		  	  $.each(this.args, function() {
		  		argArray[argArrayIndex] = this.value;
		  		argArrayIndex++;
		  	  });
		  	  // hardcoded support for six arguments
		  	  errorObj.message = jQuery.i18n.prop(this.userMessageGlobalisationCode, argArray[0], argArray[1], argArray[2], argArray[3], argArray[4], argArray[5]);
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
	
	$.fn.serializeObject = function()
	{
	    var o = {};
	    var a = this.serializeArray();
	    $.each(a, function() {
	        if (o[this.name] !== undefined) {
	            if (!o[this.name].push) {
	                o[this.name] = [o[this.name]];
	            }
	            o[this.name].push(this.value || '');
	        } else {
	        	
	        	if (this.name === 'selectedItems' || this.name === 'notSelectedItems') {
	        		o[this.name] = new Array();
	        		o[this.name].push(this.value || '');
	        	} else {
	        		o[this.name] = this.value || '';	
	        	}
	        }
	    });
	    return o;
	};
	
	function popupDialogWithFormView(getUrl, postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction) {

		var successFunction = function(data, textStatus, jqXHR) {
				console.log(data);
				popupDialogWithFormViewData(data, postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction);
		  	};
		
		if (getUrl == "") popupDialogWithFormViewData("", postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction)
		else executeAjaxRequest(getUrl, "GET", "", base64, successFunction, formErrorFunction);

	}
	function popupDialogWithFormViewData(data, postUrl, submitType, titleCode, templateSelector, width, height, saveSuccessFunction)  {
				var dialogDiv = $("<div id='dialog-form'></div>");

				var formHtml = $(templateSelector).render(data);
				
				dialogDiv.append(formHtml);
				
				var saveButton = jQuery.i18n.prop('dialog.button.save');
				var cancelButton = jQuery.i18n.prop('dialog.button.cancel');
				
				var buttonsOpts = {};
				buttonsOpts[saveButton] = function() {
					
					$('#notSelectedItems option').each(function(i) {  
				    	   $(this).attr("selected", "selected");  
				    	});
			    	
			    		$('#selectedItems option').each(function(i) {  
			    	   		$(this).attr("selected", "selected");  
			    		});
			    	
			    		var newFormData = JSON.stringify($('#entityform').serializeObject());
			    		console.log(newFormData);
			    	
					executeAjaxRequest(postUrl, submitType, newFormData, base64, saveSuccessFunction, formErrorFunction);

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
				  			
				  			$('#add').click(function() {  
				  			     return !$('#notSelectedItems option:selected').remove().appendTo('#selectedItems');  
				  			});
				  			
				  			$('#remove').click(function() {  
				  				return !$('#selectedItems option:selected').remove().appendTo('#notSelectedItems');  
				  			});
				  			
				  			$('.datepickerfield').datepicker({constrainInput: true, maxDate: 0, dateFormat: 'dd MM yy'});
				  			
				  			$("#entityform textarea").first().focus();
				  			$('#entityform input').first().focus();
				  		}
				  	}).dialog('open');

	}







//all the code for the various functions

function showILClientListing(baseApiUrl) {

setClientListingContent("content");

//HOME list clients functionality
	$("#tabs").tabs({
	    select: function(event, ui) {
	    	console.log("selected..");
	    },
	    load: function(event, ui) {
	    	console.log("load..");
	    },
	    show: function(event, ui) {
	    	console.log("show..");
		var successFunction =  function(data) {
				  			var clientObject = new Object();
			        			clientObject.clients = data;
			        			console.log(clientObject);
			        	
				    			var tableHtml = $("#clientSearchTabTemplate").render(clientObject);
							$("#searchtab").html(tableHtml);	
				  		};

  		executeAjaxRequest(baseApiUrl + 'clients', 'GET', "", base64, successFunction, formErrorFunction);
	    }
	});
	


	var addClientSuccessFunction = function(data, textStatus, jqXHR) {
		  $('#dialog-form').dialog("close");
		  showILClient(baseApiUrl, data.entityId);
	}
	$("#addclient").button().click(function(e) {
		var getUrl = baseApiUrl + 'clients/template';
		var postUrl = baseApiUrl + 'clients';
		var templateSelector = "#clientFormTemplate";
		var width = 600; 
		var height = 350;
		
		popupDialogWithFormView(getUrl, postUrl, 'POST', 'dialog.title.add.client', templateSelector, width, height, addClientSuccessFunction);
		
	    e.preventDefault();
	});
	
}	





function showILClient(baseApiUrl, clientId) {
	var clientUrl = baseApiUrl + 'clients/' + clientId
	setClientContent("content");
	$newtabs = $("#newtabs").tabs({
		
		"add": function( event, ui ) {
			$newtabs.tabs('select', '#' + ui.panel.id);
		}

	});

	var errorFunction = function(jqXHR, status, errorThrown, index, anchor) {
	        	handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
	            $(anchor.hash).html("error occured while ajax loading.");
	        };

	var successFunction = function(data, status, xhr) {
	        	
	        		var currentTabIndex = $newtabs.tabs('option', 'selected');
	            	var currentTabAnchor = $newtabs.data('tabs').anchors[currentTabIndex];
	            
	        		var tableHtml = $("#clientDataTabTemplate").render(data);
					$("#clienttab").html(tableHtml);
					
					$("#clienttabname").html(data.displayName);
					
					// retrieve accounts summary info
					refreshLoanSummaryInfo(clientUrl);
					
					// bind click listeners to buttons.

					/* cashflow not in the general app
					$('.casflowbtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("cashflowbtn", "");
						var url = '${rootContext}portfolio/client/' + clientId + '/cashflow/new';
						window.location.href = url;
					    e.preventDefault();
					});
					
					$('button.casflowbtn span').text(jQuery.i18n.prop('dialog.button.new.cashflow.analysis'));
					*/

					$('.newloanbtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("newloanbtn", "");
						addILLoan(baseApiUrl, clientId);
					    e.preventDefault();
					});
					$('button.newloanbtn span').text(jQuery.i18n.prop('dialog.button.new.loan.application'));
					
					$('.addnotebtn').button().click(function(e) {
						var linkId = this.id;
						var clientId = linkId.replace("addnotebtn", "");
						var postUrl = baseApiUrl + 'clients/' + clientId + '/notes';
						var templateSelector = "#noteFormTemplate";
						var width = 600; 
						var height = 400;
						
						var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  	$("#dialog-form").dialog("close");
						  	refreshNoteWidget(baseApiUrl + 'clients/' + clientId);
						}
						
						popupDialogWithFormView("", postUrl, 'POST', "dialog.title.add.note", templateSelector, width, height,  saveSuccessFunction);
					    e.preventDefault();
					});
					$('button.addnotebtn span').text(jQuery.i18n.prop('dialog.button.add.note'));

					refreshNoteWidget(clientUrl);
					
					// retrieve additional info
					var additionalFieldsParams = {
							url: baseApiUrl,
							basicAuthKey: base64,
							datasetType: "portfolio_client",
							datasetPKValue: data.id,
							datasetTypeDiv: "clientadditionaldata", 
							headingPrefix: "", 
							headingClass: "", 
							labelClass: "longrowlabel",
							valueClass:	"rowvalue"					
					};
					jQuery.stretchyData.displayAllExtraData(additionalFieldsParams);
	        };
	    
		executeAjaxRequest(clientUrl, 'GET', "", base64, successFunction, errorFunction);	  

}
	


	// function to retrieve and display loan summary information in it placeholder
	function refreshLoanSummaryInfo(clientUrl) {

		var successFunction =  function(data, textStatus, jqXHR) {
				  			var tableHtml = $("#clientAccountSummariesTemplate").render(data);
				  			$("#clientaccountssummary").html(tableHtml);
				  
				  			//$("a.openloanaccount").click( function(e) {
							//	var tab_title = $(this).attr('title');
					    		//	var tab_href = this.href;
							//	$newtabs.tabs( "add", "", tab_title );
							//	showILLoan(baseApiUrl, $(this).attr('id'), tab_title );
							//	e.preventDefault();
							//});
			  			}

  		executeAjaxRequest(clientUrl + '/loans', 'GET', "", base64, successFunction, formErrorFunction);	  	
	}
	


	function refreshNoteWidget(clientUrl) {
			  	
		eval(genRefreshNoteWidgetSuccessVar(clientUrl));
  		executeAjaxRequest(clientUrl + '/notes', 'GET', "", base64, successFunction, formErrorFunction);	  
	}
	function genRefreshNoteWidgetSuccessVar(clientUrl) {

		return 'var successFunction = function(data, textStatus, jqXHR) {	' +
				  ' var noteParent = new Object();' + 
				  ' noteParent.title = jQuery.i18n.prop("widget.notes.heading");' +
				  ' noteParent.notes = data;' +
				  ' var tableHtml = $("#noteListViewTemplate").render(noteParent);' +
				  ' $("#clienttabrightpane").html(tableHtml);' + 
				  ' $(".editclientnote").click(function(e) { ' +
						' var linkId = this.id;' +
						' var noteId = linkId.replace("editclientnotelink", "");' +
						' var getAndPutUrl = "' + clientUrl + '/notes/" + noteId;' +
						' var templateSelector = "#noteFormTemplate";' +
						' var width = 600;' +
						' var height = 400;' +
						' var saveSuccessFunction = function(data, textStatus, jqXHR) {' +
						  	' $("#dialog-form").dialog("close");' +
						  	' refreshNoteWidget("' + clientUrl + '");' +
						' };' +
						' popupDialogWithFormView(getAndPutUrl, getAndPutUrl, "PUT", "dialog.title.edit.note", templateSelector, width, height,  saveSuccessFunction);' +
					    ' e.preventDefault();' +
			      ' });' +
			  ' };'
	}

	
	function addILLoan(baseApiUrl, clientId) {
		setAddLoanContent("content");

		eval(genAddLoanSuccessVar(clientId));

  		executeAjaxRequest(baseApiUrl + 'loans/template?clientId=' + clientId, 'GET', "", base64, successFunction, formErrorFunction);	  
	}
	function genAddLoanSuccessVar(clientId) {

		return 'var successFunction = function(data, textStatus, jqXHR) { ' +
				' var formHtml = $("#newLoanFormTemplateMin").render(data);' +
				' $("#inputarea").html(formHtml);' +
				' $("#productId").change(function() {' +
					' var productId = $("#productId").val();' +
					' repopulateFullForm(' + clientId + ', productId);' +
				' });' +
			' };'

	}

	function repopulateFullForm(clientId, productId) {
				
		successFunction =  function(data, textStatus, jqXHR) {
			
				var formHtml = $("#newLoanFormTemplate").render(data);
			
				$("#inputarea").html(formHtml);

				$('#productId').change(function() {
					var productId = $('#productId').val();
					repopulateFullForm(clientId, productId);
				});
				
				$('.datepickerfield').datepicker({constrainInput: true, defaultDate: 0, maxDate: 0, dateFormat: 'dd MM yy'});
				
				calculateAnnualPercentageRate();
				
				calculateLoanSchedule();
				
				// change detection
				$('#principal').change(function() {
					calculateLoanSchedule();
				});
				
				$('#repaymentEvery').change(function() {
					calculateLoanSchedule();
				});
				
				$('#repaymentFrequencyType').change(function() {
					calculateLoanSchedule();
				});
				
				$('#numberOfRepayments').change(function() {
					calculateLoanSchedule();
				});
				
				$('#expectedDisbursementDate').change(function() {
					calculateLoanSchedule();
				});
				
				$('#repaymentsStartingFromDate').change(function() {
					calculateLoanSchedule();
				});
				
				$('#interestRatePerPeriod').change(function() {
					calculateAnnualPercentageRate();
					calculateLoanSchedule();
				});
				
				$('#interestRateFrequencyType').change(function() {
					calculateAnnualPercentageRate();
					calculateLoanSchedule();
				});
				
				$('#amortizationType').change(function() {
					calculateLoanSchedule();
				});
				
				$('#interestType').change(function() {
					calculateLoanSchedule();
				});
				
				$('#interestCalculationPeriodType').change(function() {
					calculateLoanSchedule();
				});
				
				$('#interestChargedFromDate').change(function() {
					calculateLoanSchedule();
				});
				
				$('#submitloanapp').button().click(function(e) {
					submitLoanApplication(clientId);
				    e.preventDefault();
				});
				$('button#submitloanapp span').text(jQuery.i18n.prop('dialog.button.submit'));
				
				$('#cancelloanapp').button().click(function(e) {
		  			showILClient(baseApiUrl, clientId);
				    e.preventDefault();
				});
				$('button#cancelloanapp span').text(jQuery.i18n.prop('dialog.button.cancel'));
			};
			  		
		executeAjaxRequest(baseApiUrl + 'loans/template?clientId=' + clientId + '&productId=' + productId, 'GET', "", base64, successFunction, formErrorFunction);	  

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

	function calculateLoanSchedule() {
		
		var newFormData = JSON.stringify($('#entityform').serializeObject());
    	
		var successFunction = function(data, textStatus, jqXHR) {
				  		removeErrors("#formerrors");
				  		var loanScheduleHtml = $("#newLoanScheduleTemplate").render(data);
				  		$("#schedulearea").html(loanScheduleHtml);
			  		};
		
		var errorFunction = function(jqXHR, textStatus, errorThrown) {
						 $("#schedulearea").html("");
						handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
					};
		executeAjaxRequest(baseApiUrl + 'loans?command=calculateLoanSchedule', "POST", newFormData, base64, successFunction, errorFunction);	  

	}


	function submitLoanApplication(clientId) {
		
		var newFormData = JSON.stringify($('#entityform').serializeObject());
    	
		var successFunction =  function(data, textStatus, jqXHR) {
		  				showILClient(baseApiUrl, clientId);
			  };
		
		executeAjaxRequest(baseApiUrl + 'loans', "POST", newFormData, base64, successFunction, formErrorFunction);	  

	}


function showILLoan(baseApiUrl, loanId, product) {
	var title = product + ": #" + loanId ;			    
	$newtabs.tabs( "add", "no url", title);
	var loanUrl = baseApiUrl + 'loans/' + loanId + "?associations=ALL";

	var errorFunction = function(jqXHR, status, errorThrown, index, anchor) {
	        	handleXhrError(jqXHR, textStatus, errorThrown, "#formErrorsTemplate", "#formerrors");
	            $(anchor.hash).html("error occured while ajax loading.");
	        };

	var successFunction = function(data, status, xhr) {
	        	
	        		var currentTabIndex = $newtabs.tabs('option', 'selected');
	            	var currentTabAnchor = $newtabs.data('tabs').anchors[currentTabIndex];
	            
	            	var offsetToSubmittedDate = 0;
	            	var offsetToApprovalDate = 0;
	            	var offsetToDisbursalDate = 0;
				var maxOffset = 0; // today


	        		var tableHtml = $("#loanDataTabTemplate").render(data);
	        		
	        		var currentTab = $("#newtabs").children(".ui-tabs-panel").not(".ui-tabs-hide");
	        		currentTab.html(tableHtml);

	        		var curTabID = currentTab.prop("id")
	        		
	        		offsetToSubmittedDate = data.convenienceData.maxSubmittedOnOffsetFromToday;
	        		offsetToApprovalDate = data.convenienceData.maxApprovedOnOffsetFromToday;
	        		offsetToDisbursalDate = data.convenienceData.maxDisbursedOnOffsetFromToday;
	        		
	        		var $loantabs = $(".loantabs").tabs({
						"show": function(event, ui) {
							
							var curTab = $('#newtabs .ui-tabs-panel:not(.ui-tabs-hide)');
			        		var curTabID = curTab.prop("id")
						}
					});
	        		
	        		$('.rejectloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("rejectbtn", "");
						var postUrl = baseApiUrl + 'loans/' + loanId + '?command=reject';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(postUrl, 'dialog.title.reject.loan', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset);
					    e.preventDefault();
					});
	        		$('button.rejectloan span').text(jQuery.i18n.prop('dialog.button.reject.loan'));
					
					$('.withdrawnbyapplicantloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("withdrawnbyapplicantloanbtn", "");
						var postUrl = baseApiUrl + 'loans/' + loanId + '?command=withdrewbyclient';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(postUrl, 'dialog.title.loan.withdrawn.by.client', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.withdrawnbyapplicantloan span').text(jQuery.i18n.prop('dialog.button.withdrawn.by.client.loan'));
					
					$('.approveloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("approvebtn", "");
						var postUrl = baseApiUrl + 'loans/' + loanId + '?command=approve';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(postUrl, 'dialog.title.approve.loan', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.approveloan span').text(jQuery.i18n.prop('dialog.button.approve.loan'));
					
					$('.undoapproveloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("undoapprovebtn", "");
						var postUrl = baseApiUrl + 'loans/' + loanId + '?command=undoapproval';
						var templateSelector = "#undoStateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToSubmittedDate;
						popupDialogWithPostOnlyFormView(postUrl, 'dialog.title.undo.loan.approval', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.undoapproveloan span').text(jQuery.i18n.prop('dialog.button.undo.loan.approval'));
					
					$('.deleteloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("deletebtn", "");
						var url = baseApiUrl + 'loans/' + loanId;
						var width = 400; 
						var height = 225;
						
						var redirectUrl = '${clientUrl}';
						
						popupConfirmationDialogAndPost(url, 'DELETE', 'dialog.title.confirmation.required', width, height, 0, redirectUrl);
					    e.preventDefault();
					});
					$('button.deleteloan span').text(jQuery.i18n.prop('dialog.button.delete.loan'));
					
					$('.disburseloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("disbursebtn", "");
						var postUrl = baseApiUrl + 'loans/' + loanId + '?command=disburse';
						var templateSelector = "#stateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						popupDialogWithPostOnlyFormView(postUrl, 'dialog.title.disburse.loan', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.disburseloan span').text(jQuery.i18n.prop('dialog.button.disburse.loan'));
					
					$('.undodisbursalloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("undodisbursalbtn", "");
						var postUrl = baseApiUrl + 'loans/' + loanId + '?command=undodisbursal';
						var templateSelector = "#undoStateTransitionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						popupDialogWithPostOnlyFormView(postUrl, 'dialog.title.undo.loan.disbursal', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.undodisbursalloan span').text(jQuery.i18n.prop('dialog.button.undo.loan.disbursal'));
					
					$('.repaymentloan').button().click(function(e) {
						
						var linkId = this.id;
						var loanId = linkId.replace("repaymentbtn", "");
						var getUrl = baseApiUrl + 'loans/' + loanId + '/transactions/template?command=repayment';
						var postUrl = baseApiUrl + 'loans/' + loanId + '/transactions?command=repayment';
						
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						
						var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  	$("#dialog-form").dialog("close");
						  	$newtabs.tabs('load', currentTabIndex);
						}
						
						popupDialogWithFormView(getUrl, postUrl, 'POST', "dialog.title.loan.repayment", templateSelector, width, height,  saveSuccessFunction);
						//popupDialogWithFormView(getUrl, postUrl, 'POST', 'dialog.title.loan.repayment', templateSelector, width, height, currentTabIndex,  offsetToSubmittedDate, defaultOffset, maxOffset)
					    e.preventDefault();
					});
					$('button.repaymentloan span').text(jQuery.i18n.prop('dialog.button.loan.repayment'));
					
					$('.waiveloan').button().click(function(e) {
						var linkId = this.id;
						var loanId = linkId.replace("waivebtn", "");
						
						var getUrl = baseApiUrl + 'loans/' + loanId + '/transactions/template?command=waiver';
						var postUrl = baseApiUrl + 'loans/' + loanId + '/transactions?command=waiver';
						
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						
						var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  	$("#dialog-form").dialog("close");
						  	$newtabs.tabs('load', currentTabIndex);
						}
						
						popupDialogWithFormView(getUrl, postUrl, 'POST', "dialog.title.waive.loan", templateSelector, width, height, saveSuccessFunction);
					    e.preventDefault();
					});
					$('button.waiveloan span').text(jQuery.i18n.prop('dialog.button.loan.waive'));
					
					$('.adjustloanrepayment').button().click(function(e) {
						
						var linkId = this.id;
						var loanAndRepaymentId = linkId.replace("adjustrepaymentbtn", "");
						var ids = loanAndRepaymentId.split("_");
						var loanId = ids[0];
						var transactionId = ids[1];
						var getAndPostUrl = baseApiUrl + 'loans/' + loanId + '/transactions/' + transactionId;
						
						var templateSelector = "#transactionLoanFormTemplate";
						var width = 500; 
						var height = 350;
						var defaultOffset = offsetToApprovalDate;
						
						var saveSuccessFunction = function(data, textStatus, jqXHR) {
						  	$("#dialog-form").dialog("close");
						  	$newtabs.tabs('load', currentTabIndex);
						}
						
						popupDialogWithFormView(getAndPostUrl, getAndPostUrl, 'POST', "dialog.title.adjust.loan.repayment", templateSelector, width,  height, saveSuccessFunction);
					    e.preventDefault();
					});
					$('button.adjustloanrepayment span').text(jQuery.i18n.prop('dialog.button.adjust.loan.repayment'));
					
					// additional data
					var additionalFieldsParams = {
							url: baseApiUrl,
							basicAuthKey: base64,
							datasetType: "portfolio_loan",
							datasetPKValue: data.id,
							datasetTypeDiv: "loanadditionaldata" + data.id, 
							headingPrefix: "", 
							headingClass: "", 
							labelClass: "longrowlabel",
							valueClass:	"rowvalue"					
					};
					jQuery.stretchyData.displayAllExtraData(additionalFieldsParams)
	        };
	    
		executeAjaxRequest(loanUrl, 'GET', "", base64, successFunction, errorFunction);	  

}
