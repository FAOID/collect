package org.openforis.collect.designer.form.validator;

import org.zkoss.bind.ValidationContext;

/**
 * 
 * @author S. Ricci
 *
 */
public class SurveyMainInfoFormValidator extends FormValidator {
	
	protected static final String NAME_FIELD = "name";
	protected static final String URI_FIELD = "uri";
	protected static final String PROJECT_FIELD = "projectName";
	protected static final String DESCRIPTION_FIELD = "description";

	@Override
	protected void internalValidate(ValidationContext ctx) {
		validateName(ctx);
		validateUri(ctx);
		validateRequired(ctx, DESCRIPTION_FIELD);
		validateRequired(ctx, PROJECT_FIELD);
	}

	protected void validateName(ValidationContext ctx) {
		validateRequired(ctx, NAME_FIELD);
	}

	protected void validateUri(ValidationContext ctx) {
		String field = URI_FIELD;
		if ( validateRequired(ctx, field) ) {
			validateUri(ctx, field);
		}
	}
	
}
