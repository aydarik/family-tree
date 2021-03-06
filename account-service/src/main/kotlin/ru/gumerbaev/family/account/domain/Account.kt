package ru.gumerbaev.family.account.domain

import org.codehaus.jackson.annotate.JsonIgnoreProperties
import org.hibernate.validator.constraints.Length
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.util.*

@Document(collection = "accounts")
@JsonIgnoreProperties(ignoreUnknown = true)
class Account {

    @Id
    var name: String? = null

    var ethAddress: String? = null

    var lastSeen: Date? = null

    @Length(min = 0, max = 20000)
    var note: String? = null

    override fun toString(): String {
        return "Account {" +
                "name='" + name + "', " +
                "ethAddress='" + ethAddress + "', " +
                "lastSeen='" + lastSeen + "', " +
                "note='" + note + "'}"
    }
}
