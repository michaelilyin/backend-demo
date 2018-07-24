package com.example.demo.controller

import com.example.demo.dto.UserDTO
import com.example.demo.exception.NotFoundException
import net.andreinc.mockneat.MockNeat
import net.andreinc.mockneat.types.enums.NameType
import net.andreinc.mockneat.types.enums.StringType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.lang.IllegalArgumentException
import java.util.*

val mock = MockNeat.threadLocal()

val users = (0..100).map {
    UserDTO(
            UUID.randomUUID().toString(),
            mock.names().type(NameType.FIRST_NAME).valStr(),
            mock.names().type(NameType.LAST_NAME).valStr(),
            mock.emails().valStr(),
            mock.strings().size(10).type(StringType.NUMBERS).valStr(),
            mock.cities().capitals().valStr()
    )
}.toMutableList()

@RestController
@RequestMapping("api")
class UsersController {

    @GetMapping("users")
    fun users(@RequestParam("filter", required = false) filter: String?,
              @RequestParam("offset", required = false) offset: Int?,
              @RequestParam("limit", required = false) limit: Int?): List<UserDTO> {
        val filtered = filter(filter)
        val factOffset = offset ?: 0
        val factLimit = limit ?: filtered.size
        return filtered
                .sortedBy { it.id }
                .drop(factOffset)
                .take(factLimit)
    }

    @GetMapping("users/{id}")
    fun user(@PathVariable("id") id: String): UserDTO {
        return users.find { it.id == id } ?: throw NotFoundException()
    }

    @PutMapping("users/{id}")
    fun user(@PathVariable("id") id: String,
             @RequestBody() user: UserDTO): UserDTO {
        if (user.id != id) {
            throw IllegalArgumentException()
        }
        val index = users.indexOfFirst { it.id == id }
        if (index == -1) {
            throw IllegalArgumentException()
        }
        users[index] = user
        return user
    }

    @PostMapping("users")
    @ResponseStatus(HttpStatus.CREATED)
    fun users(@RequestBody user: UserDTO): UserDTO {
        user.id = UUID.randomUUID().toString()
        users.add(user)
        return user
    }

    private fun filter(filter: String?): List<UserDTO> {
        return if (filter != null) {
            val normalized = filter.toLowerCase()
            users.filter {
                it.firstName.toLowerCase().contains(normalized)
                        || it.lastName.toLowerCase().contains(normalized)
                        || it.email.toLowerCase().contains(normalized)
            }
        } else {
            users
        }
    }
}