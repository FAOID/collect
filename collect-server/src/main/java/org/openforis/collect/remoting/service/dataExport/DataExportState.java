package org.openforis.collect.remoting.service.dataExport;

import org.openforis.collect.remoting.service.dataProcessing.DataProcessingState;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataExportState extends DataProcessingState {

	private static final long serialVersionUID = 1L;

	public enum Format {
		XML, CSV
	}
	
	private Format format;

	public DataExportState(Format format) {
		super();
		this.format = format;
	}
	
	public Format getFormat() {
		return format;
	}

}
