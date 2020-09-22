package restdoc.web.controller.obj

import org.thymeleaf.spring5.context.SpringContextUtils
import restdoc.web.base.getBean
import restdoc.web.core.code.CURLCodeSampleGenerator
import restdoc.web.model.BodyFieldDescriptor
import restdoc.web.model.HeaderFieldDescriptor
import restdoc.web.model.RestWebDocument
import restdoc.web.model.URIVarDescriptor

val ROOT_NAV: NavNode = NavNode(
        id = "root",
        title = "一级目录",
        field = "title",
        children = mutableListOf(),
        href = null,
        pid = "0",
        checked = true)


fun findChild(parentNode: NavNode, navNodes: List<NavNode>) {
    val children: MutableList<NavNode> = navNodes.filter { it.pid == parentNode.id }.toMutableList()
    parentNode.children = children
    for (child in children) {
        findChild(child, navNodes)
    }
}

enum class NodeType {
    RESOURCE, WIKI, API
}

data class NavNode(var id: String,
                   var title: String,
                   var field: String?,
                   var children: MutableList<NavNode>?,
                   var href: String? = null,
                   var pid: String,
                   var spread: Boolean = true,
                   var checked: Boolean = false,
                   var disabled: Boolean = false,
                   var type: NodeType = NodeType.RESOURCE

)


/**
 * Layui data format
 *
 * {
"code": 0,
"msg": "",
"count": 1000,
"data": [{}, {}]
}
 *
 */
fun layuiTableOK(data: Any, count: Int): LayuiTable = LayuiTable(code = 0, count = count, data = data, msg = null)

data class LayuiTable(val code: Int, var msg: String?, val count: Int = 0, val data: Any? = null)

data class HeaderFieldDescriptorVO(val field: String, val value: String, val optional: String = "是", val description: String = "")

data class BodyFieldDescriptorVO(val path: String, val value: Any = "",
                                 val optional: String = "是", val description: String = "",
                                 val type: String = "Object")

data class URIVarDescriptorVO(val field: String, val value: String, val description: String)


data class RestWebDocumentVO(

        val id: String,


        val method: String,

        /**
         *
         */
        val projectId: String,


        /**
         *
         */
        val name: String,


        /**
         *
         */
        val resource: String,

        /**
         * No ip or domain
         * and no port,net protocol
         *
         * This field example:/{contextPath}/...
         */
        val url: String,

        /**
         *
         */
        val description: String = "",

        /**
         *
         */
        val requestHeaderDescriptor: MutableList<HeaderFieldDescriptorVO> = mutableListOf(),

        /**
         *
         */
        val requestBodyDescriptor: MutableList<BodyFieldDescriptorVO> = mutableListOf(),

        /**
         *
         */
        val responseBodyDescriptors: MutableList<BodyFieldDescriptorVO> = mutableListOf(),


        var uriVarDescriptors: MutableList<URIVarDescriptorVO> = mutableListOf(),

        /**
         * Code sample
         */
        val codeSample: String = ""
)

fun transformHeaderToVO(headers: List<HeaderFieldDescriptor>) =
        headers.map {
            HeaderFieldDescriptorVO(field = it.field, value = it.value.joinToString(separator = ","), description = it.description
                    ?: "", optional = if (it.optional) "是" else "否")
        }.toMutableList()

fun transformNormalParamToVO(params: List<BodyFieldDescriptor>) =
        params.map {
            BodyFieldDescriptorVO(path = it.path, value = it.value
                    ?: "", optional = if (it.optional) "是" else "否", type = it.type.name.toLowerCase(), description = it.description
                    ?: "")
        }.toMutableList()


fun transformURIFieldToVO(uriVars: List<URIVarDescriptor>) =
        uriVars.map {
            URIVarDescriptorVO(
                    field = it.field,
                    value = it.value.toString(),
                    description = it.description ?: ""
            )
        }.toMutableList()


fun transformRestDocumentToVO(doc: RestWebDocument) = RestWebDocumentVO(
        id = doc.id!!,
        method = doc.method.name,
        projectId = doc.projectId!!,
        name = doc.name ?: "",
        resource = doc.resource,
        url = doc.url,
        description = doc.description ?: "",
        requestHeaderDescriptor = transformHeaderToVO(doc.requestHeaderDescriptor ?: mutableListOf()),
        responseBodyDescriptors = transformNormalParamToVO(doc.responseBodyDescriptors ?: mutableListOf()),
        requestBodyDescriptor = transformNormalParamToVO(doc.requestBodyDescriptor ?: mutableListOf()),
        uriVarDescriptors = transformURIFieldToVO(doc.uriVarDescriptors ?: mutableListOf()),
        codeSample = getBean(CURLCodeSampleGenerator::class.java).invoke(doc)
)