package restdoc.web.obj

import restdoc.model.BodyFieldDescriptor
import restdoc.model.FieldType
import restdoc.model.HeaderFieldDescriptor
import restdoc.model.URIVarDescriptor

data class CreateProjectDto(val name: String, val desc: String?)

data class UpdateProjectDto(val id: String, val name: String, val desc: String)


data class RequestDto(
        var documentId: String?,
        var url: String,
        val name: String?,
        val description: String?,
        val resource: String,
        val method: String,
        val headers: List<HeaderDto>?,
        val requestFields: List<RequestFieldsDto>?,
        val responseFields: List<ResponseFieldDto>?,
        val uriFields: List<UriVarFieldDto>?,
        val executeResult: Map<String, Any>? = null) {

    fun autocomplete() {
        if (!this.url.startsWith("http") &&
                !this.url.startsWith("https")) {
            this.url = String.format("%s%s", "http://", this.url)
        }
    }

    fun mapToHeaderDescriptor(): List<HeaderFieldDescriptor> {

        if (!(headers == null || this.headers.isEmpty())) {
            return headers
                    .filter { it.headerKey.isNotBlank() }
                    .map {
                        HeaderFieldDescriptor(
                                field = it.headerKey,
                                value = it.headerValue.split(","),
                                description = it.headerDescription,
                                optional = it.headerConstraint)
                    }
        }
        return mutableListOf()
    }

    fun mapToRequestDescriptor(): List<BodyFieldDescriptor> {
        if (!(requestFields == null || this.requestFields.isEmpty())) {
            return requestFields
                    .filter { it.requestFieldPath.isNotBlank() }
                    .map {
                        BodyFieldDescriptor(
                                path = it.requestFieldPath,
                                value = it.requestFieldValue,
                                description = it.requestFieldDescription,
                                type = FieldType.valueOf(it.requestFieldType.toUpperCase()),
                                optional = it.requestFieldConstraint,
                                defaultValue = null
                        )
                    }
        }
        return mutableListOf()
    }

    fun mapToResponseDescriptor(): List<BodyFieldDescriptor> {
        if (!(responseFields == null || this.responseFields.isEmpty())) {
            return responseFields
                    .filter { it.responseFieldPath.isNotBlank() }
                    .map {
                        BodyFieldDescriptor(
                                path = it.responseFieldPath,
                                value = it.responseFieldValue,
                                description = it.responseFieldDescription,
                                type = FieldType.valueOf(it.responseFieldType.toUpperCase()),
                                optional = it.responseFieldConstraint,
                                defaultValue = null
                        )
                    }
        }
        return mutableListOf()
    }

    fun mapToURIVarDescriptor(): List<URIVarDescriptor> {
        if (this.uriFields != null && !uriFields.isEmpty()) {
            return uriFields.filter { it.field != null && it.field.isNotEmpty() }
                    .map {
                        URIVarDescriptor(
                                field = it.field.toString(),
                                value = it.value.toString(),
                                description = it.desc)
                    }
        }
        return mutableListOf()
    }
}


data class UriVarFieldDto(
        val field: String?,
        val value: Any?,
        val desc: String?
)

data class HeaderDto(
        val headerKey: String,
        val headerValue: String,
        val headerDescription: String,
        val headerConstraint: Boolean
)

data class RequestFieldsDto(
        val requestFieldPath: String,
        val requestFieldValue: Any,
        val requestFieldType: String,
        val requestFieldDescription: String,
        val requestFieldConstraint: Boolean
)

data class ResponseFieldDto(
        val responseFieldPath: String,
        val responseFieldValue: Any,
        val responseFieldType: String,
        val responseFieldDescription: String,
        val responseFieldConstraint: Boolean
)

data class CreateResourceDto(
        val name: String,
        val tag: String,
        var pid: String
)