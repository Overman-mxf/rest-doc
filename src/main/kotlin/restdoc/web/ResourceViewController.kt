package restdoc.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@Controller
class ResourceViewController {

    @GetMapping("/{projectId}/resource/view/list")
    fun list(): String {
        return "resource/list"
    }

    @GetMapping("/{projectId}/resource/view/add")
    fun add(@PathVariable projectId: String): String {
        return "resource/add"
    }
}