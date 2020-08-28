package restdoc.util


/**
 * Method for object isNullable
 */
infix fun Any?.ifNull(block: () -> Unit) {
    if (this == null) block()
}


