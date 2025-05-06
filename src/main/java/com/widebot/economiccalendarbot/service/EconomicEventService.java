package com.widebot.economiccalendarbot.service;

import com.widebot.economiccalendarbot.model.EconomicEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Service
public class EconomicEventService {

    private static final Logger log = LoggerFactory.getLogger(EconomicEventService.class);

    private static final String CALENDAR_URL = "https://www.investing.com/economic-calendar/";

    // Selettori CSS
    private static final String SELECTOR_EVENT_ITEM = ".js-event-item";
    private static final String SELECTOR_TIME = ".time";
    private static final String SELECTOR_TITLE = ".event";
    private static final String SELECTOR_CURRENCY = ".left.flagCur.noWrap";
    private static final String SELECTOR_STARS = ".grayFullBullishIcon, .bullishIcon, .mediumImpactIcon";
    private static final String SELECTOR_COUNTRY_TITLE = ".flagCur";

    private Document fetchCalendarPage() throws IOException {
        return Jsoup.connect(CALENDAR_URL)
                .userAgent("Mozilla/5.0") // evitare blocchi
                .get();
    }

    private String parseEventi(String intestazione, Predicate<EconomicEvent> filtro, boolean mostraValuta) {
        StringBuilder calendario = new StringBuilder();
        calendario.append(intestazione).append("\n\n");

        try {
            Document doc = fetchCalendarPage();
            Elements elementi = doc.select(SELECTOR_EVENT_ITEM);

            if (elementi.isEmpty()) {
                log.warn("⚠️ Nessun evento trovato. Possibile cambio struttura HTML.");
                return "⚠️ Nessun evento disponibile.";
            }

            List<EconomicEvent> eventi = new ArrayList<>();

            for (Element el : elementi) {
                String ora = el.select(SELECTOR_TIME).text().trim();
                String titolo = el.select(SELECTOR_TITLE).text().trim();
                String valuta = el.select(SELECTOR_CURRENCY).text().trim();
                int stelle = el.select(SELECTOR_STARS).size();
                String paese = el.select(SELECTOR_COUNTRY_TITLE).attr("title").trim();

                if (!ora.isBlank() && !titolo.isBlank()) {
                    EconomicEvent evento = new EconomicEvent(ora, titolo, valuta, stelle, paese);
                    if (filtro.test(evento)) {
                        eventi.add(evento);
                    }
                }
            }

            if (eventi.isEmpty()) {
                return "⚠️ Nessun evento trovato con i criteri richiesti.";
            }

            eventi.forEach(ev -> calendario.append(formatEvent(ev, mostraValuta)));

        } catch (IOException e) {
            log.error("❌ Errore Jsoup: {}", e.getMessage(), e);
            return "❌ Errore nel recupero degli eventi economici.";
        }

        return calendario.toString();
    }

    private String formatEvent(EconomicEvent ev, boolean mostraValuta) {
        StringBuilder sb = new StringBuilder();
        sb.append("⏰ ").append(ev.getOra()).append(" - ");
        if (mostraValuta && !ev.getValuta().isBlank()) {
            sb.append("[").append(ev.getValuta()).append("] ");
        }
        sb.append(ev.getTitolo()).append(" (").append(ev.getStelle()).append("⭐)").append("\n");
        return sb.toString();
    }

    public String getCalendarioDiOggi() {
        return parseEventi("🗓️ Calendario Economico di Oggi:", ev -> true, false);
    }

    public String getEventiPerValuta(String codiceValuta) {
        return parseEventi("🗓️ Eventi economici per: " + codiceValuta.toUpperCase(),
                ev -> ev.getValuta().equalsIgnoreCase(codiceValuta), false);
    }

    public String getEventiAdAltoImpatto() {
        return parseEventi("🔥 Eventi ad alto impatto (⭐⭐⭐+)",
                ev -> ev.getStelle() >= 3, true);
    }

    public String getEventiPerPaese(String nomePaese) {
        return parseEventi("📅 Eventi macro per " + nomePaese + ":",
                ev -> ev.getPaese().equalsIgnoreCase(nomePaese), true);
    }


}
