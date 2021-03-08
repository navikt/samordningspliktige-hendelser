package no.nav.samordning.hendelser.consumer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

@JsonIgnoreProperties(ignoreUnknown = true)
class Consumer(
    // Example "0192:991825827"
    @JsonProperty("ID") val consumerOrgno: String
) {
    fun getOrgno() = this.consumerOrgno.substringAfter(":")

    companion object {
        fun parse(consumerObject: String) = jacksonObjectMapper().readValue<Consumer>(consumerObject)
    }
}