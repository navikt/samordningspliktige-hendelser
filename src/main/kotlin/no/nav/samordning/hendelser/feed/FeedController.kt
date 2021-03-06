package no.nav.samordning.hendelser.feed

import io.micrometer.core.annotation.Timed
import no.nav.samordning.hendelser.database.Database
import no.nav.samordning.hendelser.metrics.AppMetrics
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import javax.validation.constraints.Digits
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.PositiveOrZero

@RestController
@Validated
class FeedController {

    @Autowired
    private lateinit var database: Database

    @Autowired
    private lateinit var metrics: AppMetrics

    @Value("\${NEXT_BASE_URL}")
    private lateinit var nextBaseUrl: String

    @Timed
    @GetMapping(path = ["/hendelser"])
    fun hendelser(
            @RequestParam(value = "tpnr") @Digits(integer = 4, fraction = 0) tpnr: String,
            @RequestParam(value = "side", required = false, defaultValue = "0") @PositiveOrZero side: Int,
            @RequestParam(value = "antall", required = false, defaultValue = "10000") @Min(0) @Max(10000) antall: Int,
            @RequestParam(value = "sekvensnummer", required = false, defaultValue = "1") @Min(1) sekvensnummer: Int): Feed {

        val hendelseMap = database.fetchSeqAndHendelser(tpnr, sekvensnummer, side, antall)
        val latestReadSNR = hendelseMap.keys.lastOrNull() ?: 1

        metrics.incHendelserLest(tpnr, hendelseMap.size.toDouble())

        return Feed(hendelseMap.values.toList(), database.latestSekvensnummer(tpnr), latestReadSNR, nextUrl(tpnr, sekvensnummer, antall, side))
    }

    private fun nextUrl(tpnr: String, sekvensnummer: Int, antall: Int, side: Int) =
            if (database.getNumberOfPages(tpnr, sekvensnummer, antall) > side + 1) "$nextBaseUrl/hendelser?tpnr=$tpnr&sekvensnummer=$sekvensnummer&antall=$antall&side=${side + 1}"
            else null
}
