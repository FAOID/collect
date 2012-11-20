/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.0.1"},
                            comments = "This class is generated by jOOQ")
public class OfcRecord extends org.jooq.impl.UpdatableTableImpl<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord> {

	private static final long serialVersionUID = -1281076309;

	/**
	 * The singleton instance of ofc_record
	 */
	public static final org.openforis.collect.persistence.jooq.tables.OfcRecord OFC_RECORD = new org.openforis.collect.persistence.jooq.tables.OfcRecord();

	/**
	 * The class holding records for this type
	 */
	private static final java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord> __RECORD_TYPE = org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord.class;

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord> getRecordType() {
		return __RECORD_TYPE;
	}

	/**
	 * An uncommented item
	 * 
	 * PRIMARY KEY
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.sql.Timestamp> DATE_CREATED = createField("date_created", org.jooq.impl.SQLDataType.TIMESTAMP, this);

	/**
	 * An uncommented item
	 * <p>
	 * <code><pre>
	 * FOREIGN KEY [collect.ofc_record.created_by_id]
	 * REFERENCES ofc_user [collect.ofc_user.id]
	 * </pre></code>
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> CREATED_BY_ID = createField("created_by_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.sql.Timestamp> DATE_MODIFIED = createField("date_modified", org.jooq.impl.SQLDataType.TIMESTAMP, this);

	/**
	 * An uncommented item
	 * <p>
	 * <code><pre>
	 * FOREIGN KEY [collect.ofc_record.modified_by_id]
	 * REFERENCES ofc_user [collect.ofc_user.id]
	 * </pre></code>
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> MODIFIED_BY_ID = createField("modified_by_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> MODEL_VERSION = createField("model_version", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> STEP = createField("step", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> STATE = createField("state", org.jooq.impl.SQLDataType.CHAR, this);

	/**
	 * An uncommented item
	 * <p>
	 * <code><pre>
	 * FOREIGN KEY [collect.ofc_record.locked_by_id]
	 * REFERENCES ofc_user [collect.ofc_user.id]
	 * </pre></code>
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> LOCKED_BY_ID = createField("locked_by_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> SKIPPED = createField("skipped", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> MISSING = createField("missing", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> ERRORS = createField("errors", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> WARNINGS = createField("warnings", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> KEY1 = createField("key1", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> KEY2 = createField("key2", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> KEY3 = createField("key3", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> COUNT1 = createField("count1", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> COUNT2 = createField("count2", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> COUNT3 = createField("count3", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> COUNT4 = createField("count4", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> COUNT5 = createField("count5", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, byte[]> DATA1 = createField("data1", org.jooq.impl.SQLDataType.BLOB, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, byte[]> DATA2 = createField("data2", org.jooq.impl.SQLDataType.BLOB, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> ROOT_ENTITY_DEFINITION_ID = createField("root_entity_definition_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> LOCK_ID = createField("lock_id", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * An uncommented item
	 * <p>
	 * <code><pre>
	 * FOREIGN KEY [collect.ofc_record.survey_id]
	 * REFERENCES ofc_survey [collect.ofc_survey.id]
	 * </pre></code>
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.Integer> SURVEY_ID = createField("survey_id", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> KEY4 = createField("key4", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, java.lang.String> KEY5 = createField("key5", org.jooq.impl.SQLDataType.VARCHAR, this);

	/**
	 * No further instances allowed
	 */
	private OfcRecord() {
		super("ofc_record", org.openforis.collect.persistence.jooq.Collect.COLLECT);
	}

	/**
	 * No further instances allowed
	 */
	private OfcRecord(java.lang.String alias) {
		super(alias, org.openforis.collect.persistence.jooq.Collect.COLLECT, org.openforis.collect.persistence.jooq.tables.OfcRecord.OFC_RECORD);
	}

	@Override
	public org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord> getMainKey() {
		return org.openforis.collect.persistence.jooq.Keys.ofc_record_pkey;
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord>>asList(org.openforis.collect.persistence.jooq.Keys.ofc_record_pkey);
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.ForeignKey<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, ?>> getReferences() {
		return java.util.Arrays.<org.jooq.ForeignKey<org.openforis.collect.persistence.jooq.tables.records.OfcRecordRecord, ?>>asList(org.openforis.collect.persistence.jooq.Keys.ofc_record__ofc_record_created_by_user_fkey, org.openforis.collect.persistence.jooq.Keys.ofc_record__ofc_record_modified_by_user_fkey, org.openforis.collect.persistence.jooq.Keys.ofc_record__ofc_record_locked_by_user_fkey, org.openforis.collect.persistence.jooq.Keys.ofc_record__ofc_record_survey_fkey);
	}

	@Override
	public org.openforis.collect.persistence.jooq.tables.OfcRecord as(java.lang.String alias) {
		return new org.openforis.collect.persistence.jooq.tables.OfcRecord(alias);
	}
}
