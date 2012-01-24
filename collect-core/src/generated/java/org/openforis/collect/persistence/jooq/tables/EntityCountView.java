/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.0.1"},
                            comments = "This class is generated by jOOQ")
public class EntityCountView extends org.jooq.impl.TableImpl<org.openforis.collect.persistence.jooq.tables.records.EntityCountViewRecord> {

	private static final long serialVersionUID = -1416711520;

	/**
	 * The singleton instance of entity_count_view
	 */
	public static final org.openforis.collect.persistence.jooq.tables.EntityCountView ENTITY_COUNT_VIEW = new org.openforis.collect.persistence.jooq.tables.EntityCountView();

	/**
	 * The class holding records for this type
	 */
	private static final java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.EntityCountViewRecord> __RECORD_TYPE = org.openforis.collect.persistence.jooq.tables.records.EntityCountViewRecord.class;

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.EntityCountViewRecord> getRecordType() {
		return __RECORD_TYPE;
	}

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.EntityCountViewRecord, java.lang.Integer> RECORD_ID = createField("record_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.EntityCountViewRecord, java.lang.Integer> DEFINITION_ID = createField("definition_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.EntityCountViewRecord, java.lang.Long> COUNT = createField("count", org.jooq.impl.SQLDataType.BIGINT, this);

	/**
	 * No further instances allowed
	 */
	private EntityCountView() {
		super("entity_count_view", org.openforis.collect.persistence.jooq.Collect.COLLECT);
	}

	/**
	 * No further instances allowed
	 */
	private EntityCountView(java.lang.String alias) {
		super(alias, org.openforis.collect.persistence.jooq.Collect.COLLECT, org.openforis.collect.persistence.jooq.tables.EntityCountView.ENTITY_COUNT_VIEW);
	}

	@Override
	public org.openforis.collect.persistence.jooq.tables.EntityCountView as(java.lang.String alias) {
		return new org.openforis.collect.persistence.jooq.tables.EntityCountView(alias);
	}
}