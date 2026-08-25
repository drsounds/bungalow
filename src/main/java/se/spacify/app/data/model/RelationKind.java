package se.spacify.app.data.model;

/** The two kinds of {@link DataRelation} between two {@link DataTable}s. */
public enum RelationKind {
    /** A single LINK {@link DataField} on the source table points at a row of the target table. */
    BELONGS_TO,
    /** An ordinary {@link DataTable} acts as junction, with one LINK field per side. */
    MANY_TO_MANY
}
