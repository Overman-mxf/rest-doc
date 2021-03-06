package restdoc.web.controller.console.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.bind.annotation.*
import restdoc.web.base.auth.Verify
import restdoc.web.controller.console.model.CreateProjectDto
import restdoc.web.controller.console.model.UpdateProjectDto
import restdoc.web.core.Result
import restdoc.web.core.Status
import restdoc.web.core.ok
import restdoc.web.model.ANY_ROLE
import restdoc.web.model.SYS_ADMIN
import restdoc.web.model.Project
import restdoc.web.model.ProjectType
import restdoc.web.repository.ProjectRepository
import restdoc.web.util.IDUtil
import restdoc.web.util.MD5Util
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/project")
class ProjectController {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var projectRepository: ProjectRepository

    /**
     * Add Search
     */
    @GetMapping("/list")
    @Verify(role = [SYS_ADMIN])
    fun list(@RequestParam(required = false, defaultValue = "0") page: Int,
             @RequestParam(required = false, defaultValue = "12") size: Int,
             @RequestParam type: ProjectType
    ): Result {
        return ok(projectRepository.page(Query().addCriteria(Criteria("type").`is`(type)), PageRequest.of(page, size)))
    }


    @GetMapping("/{id}")
    @Verify(role = [ANY_ROLE])
    fun get(@PathVariable id: String): Result = ok(mongoTemplate.findById(id, Project::class.java))

    @DeleteMapping("/{id}")
    @Verify(role = [SYS_ADMIN])
    fun delete(@PathVariable id: String): Result {
        val deleteResult = projectRepository.delete(Query().addCriteria(Criteria("_id").`is`(id)))
        return ok(deleteResult)
    }

    @PostMapping("")
    @Verify(role = [SYS_ADMIN])
    fun create(@RequestBody dto: CreateProjectDto): Result {
        if (dto.type == ProjectType.SPRINGCLOUD) Status.BAD_REQUEST.error("暂不支持SpringCloud项目")
        val projectId = IDUtil.id()
        val project = Project(
                id = projectId,
                name = dto.name,
                createTime = Date().time,
                desc = dto.desc,
                type = dto.type,
                accessPassword = MD5Util.encryptPassword("restdoc", projectId, 1024)
        )
        mongoTemplate.save(project)
        return ok()
    }

    @PatchMapping("")
    @Verify(role = [SYS_ADMIN])
    fun update(@RequestBody @Valid dto: UpdateProjectDto): Result {
        projectRepository.update(Project(
                id = dto.id,
                name = dto.name,
                createTime = null,
                desc = dto.desc,
                type = dto.type
        ))
        return ok()
    }
}