package restdoc.web.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "restdoc_project")
data class Project(
        @Id
        val id: String,

        /**
         *
         */
        val name: String,

        /**
         *
         */
        val desc: String?,

        /**
         *
         */
        val createTime: Long?,

        /**
         *
         */
        val type: ProjectType = ProjectType.REST_WEB,

        /**
         * Is allow non password access
         */
        val allowAccess: Boolean = false,

        /**
         * Access password
         */
        val accessPassword: String? = null,

        /**
         *Access
         */
        val accessURL: String? = null
)

enum class ProjectType {

    /**
     * Default
     */
    REST_WEB,

    /**
     *
     */
    DUBBO,
    SPRINGCLOUD
}


@Document(collection = "restdoc_project_config")
data class ProjectConfig(
        @Id var id: String,
        val projectId: String,
        val testURIPrefix: String
)

