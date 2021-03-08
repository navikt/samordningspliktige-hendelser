package no.nav.samordning.hendelser.consumer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class Consumer(
    // Eksempel "0192:991825827"
    @JsonProperty("ID") val consumerOrgno: String
) {
    fun getOrgno() = this.consumerOrgno.substringAfter(":")
}