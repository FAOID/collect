package org.openforis.collect.presenter {
	
	import flash.events.DataEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.MouseEvent;
	import flash.events.ProgressEvent;
	import flash.events.TimerEvent;
	import flash.net.FileReference;
	import flash.net.URLRequest;
	import flash.net.URLRequestMethod;
	import flash.net.URLVariables;
	import flash.utils.Timer;
	
	import mx.collections.IList;
	import mx.rpc.AsyncResponder;
	import mx.rpc.IResponder;
	import mx.rpc.events.ResultEvent;
	
	import org.openforis.collect.Application;
	import org.openforis.collect.client.ClientFactory;
	import org.openforis.collect.client.DataImportClient;
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.model.CollectRecord$Step;
	import org.openforis.collect.remoting.service.dataImport.DataImportConflict;
	import org.openforis.collect.remoting.service.dataImport.DataImportState;
	import org.openforis.collect.remoting.service.dataImport.DataImportState$Step;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.view.DataImportView;
	import org.openforis.collect.util.AlertUtil;
	import org.openforis.collect.util.ApplicationConstants;
	import org.openforis.collect.util.StringUtil;
	
	import spark.formatters.DateTimeFormatter;

	/**
	 * 
	 * @author S. Ricci
	 * 
	 * */
	public class DataImportPresenter extends AbstractPresenter {
		
		private static const PROGRESS_UPDATE_DELAY:int = 2000;
		
		private var _view:DataImportView;
		private var _fileReference:FileReference;
		private var _dataImportClient:DataImportClient;
		private var _progressTimer:Timer;
		private var _state:DataImportState;
		
		private var _getStateResponder:IResponder;
		private var _firstOpen:Boolean;
		
		public function DataImportPresenter(view:DataImportView) {
			this._view = view;
			_firstOpen = true;
			_fileReference = new FileReference();
			_dataImportClient = ClientFactory.dataImportClient;
			
			_getStateResponder = new AsyncResponder(getStateResultHandler, faultHandler);
			
			super();
			
			//try to see if there is a process still running
			updateState();
		}
		
		override internal function initEventListeners():void {
			_fileReference.addEventListener(Event.SELECT, fileReferenceSelectHandler);
			_fileReference.addEventListener(Event.COMPLETE, fileReferenceCompleteHandler);
			_fileReference.addEventListener(ProgressEvent.PROGRESS, fileReferenceProgressHandler);
			_fileReference.addEventListener(Event.OPEN, fileReferenceOpenHandler);
			_fileReference.addEventListener(IOErrorEvent.IO_ERROR, fileReferenceIoErrorHandler);
			_fileReference.addEventListener(DataEvent.UPLOAD_COMPLETE_DATA, fileReferenceUploadCompleteDataHandler);
			
			_view.uploadButton.addEventListener(MouseEvent.CLICK, uploadButtonClickHandler);
			_view.startImportButton.addEventListener(MouseEvent.CLICK, startImportClickHandler);
			_view.cancelButton.addEventListener(MouseEvent.CLICK, cancelButtonClickHandler);
		}
		
		protected function uploadButtonClickHandler(event:MouseEvent):void {
			_fileReference.browse();
		}
		
		protected function cancelButtonClickHandler(event:MouseEvent):void {
			if ( _view.currentState == DataImportView.STATE_UPLOADING ) {
				_fileReference.cancel();
			} else {
				var responder:AsyncResponder = new AsyncResponder(cancelImportResultHandler, faultHandler);
				_dataImportClient.cancel(responder);
			}
			_view.currentState = DataImportView.STATE_DEFAULT;
		}
		
		protected function fileReferenceSelectHandler(event:Event):void {
			_view.currentState = DataImportView.STATE_UPLOADING;
			
			var url:String = ApplicationConstants.DATA_IMPORT_UPLOAD_URL;
			//workaround for firefox/chrome flahplayer bug
			//url +=";jsessionid=" + Application.sessionId;
			
			var request:URLRequest = new URLRequest(url);
			//request paramters
			request.method = URLRequestMethod.POST;
			var parameters:URLVariables = new URLVariables();
			parameters.name = _fileReference.name;
			parameters.sessionId = Application.sessionId;
			request.data = parameters;
			_fileReference.upload(request, "fileData");
		}
		
		protected function fileReferenceOpenHandler(event:Event):void {
			
		}
		
		protected function fileReferenceCompleteHandler(event:Event):void {
			
		}
		
		protected function fileReferenceProgressHandler(event:ProgressEvent):void {
			_view.progressBar.setProgress(event.bytesLoaded, event.bytesTotal);
		}
		
		protected function fileReferenceUploadCompleteDataHandler(event:DataEvent):void {
			_view.currentState = DataImportView.STATE_LOADING;
			var responder:AsyncResponder = new AsyncResponder(initProcessResultHandler, faultHandler);
			_dataImportClient.initProcess(responder);
		}
		
		protected function fileReferenceIoErrorHandler(event:IOErrorEvent):void {
			_view.currentState = DataImportView.STATE_DEFAULT;
			AlertUtil.showError("dataImport.file.error", [event.text]);
		}
		
		protected function initProcessResultHandler(event:ResultEvent, token:Object = null):void {
			var state:DataImportState = event.result as DataImportState;
			if ( state.newSurvey ) {
				_view.currentState = DataImportView.STATE_UPLOAD_COMPLETE_NEW_SURVEY;
			} else {
				_view.currentState = DataImportView.STATE_UPLOAD_COMPLETE;
			}
			var entryTotalRecords:int = state.totalPerStep.get(CollectRecord$Step.ENTRY);
			var cleansingTotalRecords:int = state.totalPerStep.get(CollectRecord$Step.CLEANSING);
			var analysisTotalRecords:int = state.totalPerStep.get(CollectRecord$Step.ANALYSIS);
			var importSummary:String = Message.get("dataImport.importSummary", [entryTotalRecords, cleansingTotalRecords, analysisTotalRecords]);
			_view.importSummaryLabel.text = importSummary;
			//AlertUtil.showConfirm("dataImport.confirmStart", [entryTotalRecords, cleansingTotalRecords, analysisTotalRecords], null, initProcessConfirmHandler);
		}
		
		protected function startImportClickHandler(event:MouseEvent):void {
			if ( validateForm() ) {
				var responder:AsyncResponder = new AsyncResponder(startImportResultHandler, faultHandler);
				var surveyName:String = null;
				if ( _view.currentState == DataImportView.STATE_UPLOAD_COMPLETE_NEW_SURVEY ) {
					surveyName = _view.surveyNameTextInput.text;
				}
				_dataImportClient.startImport(responder, surveyName);
				_view.progressBar.setProgress(0, 0);
				_view.currentState = DataImportView.STATE_IMPORT_RUNNING;
			}
		}
		
		protected function validateForm():Boolean {
			if ( _view.currentState == DataImportView.STATE_UPLOAD_COMPLETE_NEW_SURVEY ) {
				var surveyName:String = _view.surveyNameTextInput.text;
				surveyName = StringUtil.trim(surveyName);
				_view.surveyNameTextInput.text = surveyName;
				if ( StringUtil.isBlank(surveyName) ) {
					AlertUtil.showError("dataImport.error.specifySurveyName");
					return false;
				}
			}
			return true;
		}
		
		protected function startImportResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as DataImportState;
			updateView();
		}
		
		protected function cancelImportResultHandler(event:ResultEvent, token:Object = null):void {
			resetView();
		}
		
		protected function updateState():void {
			_dataImportClient.getState(_getStateResponder);
		}
		
		protected function getStateResultHandler(event:ResultEvent, token:Object = null):void {
			_state = event.result as DataImportState;
			updateView();
		}
		
		protected function startProgressTimer():void {
			if ( _progressTimer == null ) {
				_progressTimer = new Timer(PROGRESS_UPDATE_DELAY);
				_progressTimer.addEventListener(TimerEvent.TIMER, progressTimerHandler);
			}
			_progressTimer.start();
		}
		
		protected function stopProgressTimer():void {
			if ( _progressTimer != null ) {
				_progressTimer.stop();
				_progressTimer = null;
			}
		}
		
		protected function progressTimerHandler(event:TimerEvent):void {
			updateState();
		}
		
		private function updateView():void {
			if(_state != null) {
				var step:DataImportState$Step = _state.step;
				switch ( step ) {
				case DataImportState$Step.IMPORTING:
					_view.currentState = DataImportView.STATE_IMPORT_RUNNING;
					updateViewForImporting();
					if ( _progressTimer == null ) {
						startProgressTimer();
					}
					break;
				case DataImportState$Step.COMPLETE:
					if ( _firstOpen ) {
						resetView();
					} else {
						_view.currentState = DataImportView.STATE_IMPORT_COMPLETE;
						updateViewForImporting();
						stopProgressTimer();
					}
					break;
				case DataImportState$Step.CONFLICT:
					_view.currentState = DataImportView.STATE_IMPORT_RUNNING;
					updateViewForImporting();
					stopProgressTimer();
					showConfirmForConflict();
					break;
				case DataImportState$Step.ERROR:
					AlertUtil.showError("dataImport.error");
					resetView();
					break;
				case DataImportState$Step.CANCELLED:
					AlertUtil.showError("dataImport.cancelled");
					resetView();
					break;
				default:
					resetView();
				}
			} else {
				resetView();
			}
			_firstOpen = false;
		}
		
		protected function showConfirmForConflict():void {
			var conflict:DataImportConflict = _state.conflict;
			var rootEntityKeys:Array = conflict.existingRecord.rootEntityKeys.toArray();
			var recordKey:String = StringUtil.concat("-", rootEntityKeys);
			var dateFormatter:DateTimeFormatter = new DateTimeFormatter();
			dateFormatter.dateTimePattern = RecordSummaryDataGrid.DATE_TIME_PATTERN;
			var existingRecordModifiedDate:String = dateFormatter.format(conflict.existingRecord.modifiedDate);
			var importRecordModifiedDate:String = dateFormatter.format(conflict.importRecord.modifiedDate);
			AlertUtil.showConfirm("dataImport.conflict", 
				[recordKey, existingRecordModifiedDate, importRecordModifiedDate], 
				"dataImport.conflict.title", overwriteExistingRecordInConflict, null, doNotOverwriteExistingRecordInConflict);
		}
		
		protected function overwriteExistingRecordInConflict(value:Boolean = true):void {
			var responder:AsyncResponder = new AsyncResponder(overwriteExistingRecordInConflictResultHandler, faultHandler);
			_dataImportClient.overwriteRecordInConflict(responder, value);
		}
		
		private function overwriteExistingRecordInConflictResultHandler(event:ResultEvent, token:Object = null):void {
			updateState();
		}
		
		private function doNotOverwriteExistingRecordInConflict():void {
			overwriteExistingRecordInConflict(false);
		}
		
		private function updateViewForImporting():void {
			_view.progressBar.setProgress(_state.count, _state.total);
			updateProgressText();
			updateErrorsTextArea()
		}
		
		protected function updateProgressText():void {
			var progressText:String;
			if ( _state.total == 0 ) {
				progressText = Message.get("dataImport.processing");
			} else {
				progressText = Message.get("dataImport.progressLabel", [_state.count, _state.total]);
			}
			_view.progressLabel.text = progressText;
		}
		
		protected function updateErrorsTextArea():void {
			var result:String = "";
			if ( _state != null && _state.errors != null ) {
				var files:IList = _state.errors.keySet;
				for each (var fileName:String in files ) {
					var errorMessage:String = _state.errors.get(fileName);
					result += Message.get('dataImport.errorInFile', [fileName, errorMessage]);
				}
			}
			_view.errorsTextArea.text = result;
		}
		
		protected function resetView():void {
			_state = null;
			_view.currentState = DataImportView.STATE_DEFAULT;
			stopProgressTimer();
		}
		
	}
}