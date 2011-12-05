/**
 * 
 */
package org.openforis.collect.idm.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.openforis.idm.metamodel.Check;
import org.openforis.idm.metamodel.ModelObjectDefinition;
import org.openforis.idm.model.ModelObject;

/**
 * @author M. Togna
 * 
 */
@MappedSuperclass
public class AbstractModelObject<D extends ModelObjectDefinition> implements ModelObject<D> {

	@Column(unique = true, name = "id")
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	private D definition;
	private boolean relevant;
	private List<Check> errors;
	private List<Check> warnings;
	private RecordImpl record;
	private String path;
	private String type;

	@Override
	public D getDefinition() {
		return this.definition;
	}

	@Override
	public Boolean isRelevant() {
		return this.relevant;
	}

	@Override
	public List<Check> getFailedChecks() {
		List<Check> failedChecks = new ArrayList<Check>();
		if (this.hasErrors()) {
			failedChecks.addAll(this.errors);
		}
		if (this.hasWarnings()) {
			failedChecks.addAll(this.warnings);
		}
		return Collections.unmodifiableList(failedChecks);
	}

	@Override
	public List<Check> getErrors() {
		List<Check> errors = this.errors != null ? this.errors : new ArrayList<Check>();
		return Collections.unmodifiableList(errors);
	}

	@Override
	public List<Check> getWarnings() {
		List<Check> warnings = this.warnings != null ? this.warnings : new ArrayList<Check>();
		return Collections.unmodifiableList(warnings);
	}

	@Override
	public boolean hasErrors() {
		return (this.errors != null) && !this.errors.isEmpty();
	}

	@Override
	public boolean hasWarnings() {
		return (this.warnings != null) && !this.warnings.isEmpty();
	}

	protected RecordImpl getRecord() {
		return this.record;
	}

	protected void setRecord(RecordImpl record) {
		this.record = record;
	}

	public String getPath() {
		return this.path;
	}

	void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public Long getId() {
		return id;
	}

}