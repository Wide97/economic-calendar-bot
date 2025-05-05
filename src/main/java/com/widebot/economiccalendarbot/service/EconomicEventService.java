package com.widebot.economiccalendarbot.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Predicate;

@Service
public class EconomicEventService {

    private static final String CALENDAR_URL = "https://www.investing.com/economic-calendar/";

    /**
     * Scarica e restituisce il documento HTML del calendario economico.
     */
    private Document fetchCalendarPage() throws IOException {
        return Jsoup.connect(CALENDAR_URL)
                .userAgent("Mozilla/5.0") // fondamentale per evitare blocchi da parte di Investing
                .get();
    }

    /**
     * Metodo generico che costruisce il calendario filtrando gli eventi con il predicato passato.
     */
    private String parseEventi(String intestazione, Predicate<Element> filtroEvento, boolean mostraValuta) {
        StringBuilder calendario = new StringBuilder();
        calendario.append(intestazione).append("\n\n");

        try {
            Document doc = fetchCalendarPage();
            Elements eventi = doc.select(".js-event-item");

            for (Element evento : eventi) {
                String ora = evento.select(".time").text();
                String titolo = evento.select(".event").text();
                String valuta = evento.select(".left.flagCur.noWrap").text().trim();
                int stelle = evento.select(".grayFullBullishIcon, .bullishIcon, .mediumImpactIcon").size();

                if (!ora.isBlank() && !titolo.isBlank() && filtroEvento.test(evento)) {
                    calendario.append("⏰ ").append(ora).append(" - ");
                    if (mostraValuta) {
                        calendario.append("[").append(valuta).append("] ");
                    }
                    calendario.append(titolo).append(" (").append(stelle).append("⭐)\n");
                }
            }

        } catch (IOException e) {
            System.out.println("❌ Errore Jsoup: " + e.getMessage());
            return "❌ Errore nel recupero degli eventi economici.";
        }

        return calendario.toString().isBlank()
                ? "⚠️ Nessun evento trovato."
                : calendario.toString();
    }

    // ✅ Eventi di oggi senza filtro
    public String getCalendarioDiOggi() {
        return parseEventi("🗓️ Calendario Economico di Oggi:",
                evento -> true,
                false);
    }

    // ✅ Eventi filtrati per valuta
    public String getEventiPerValuta(String codiceValuta) {
        return parseEventi("🗓️ Eventi economici per: " + codiceValuta.toUpperCase(),
                evento -> {
                    String valuta = evento.select(".left.flagCur.noWrap").text().trim();
                    return valuta.equalsIgnoreCase(codiceValuta);
                },
                false);
    }

    // ✅ Eventi ad alto impatto (almeno 3 stelle)
    public String getEventiAdAltoImpatto() {
        return parseEventi("🔥 Eventi ad alto impatto (⭐⭐⭐+)",
                evento -> {
                    int stelle = evento.select(".grayFullBullishIcon, .bullishIcon, .mediumImpactIcon").size();
                    return stelle >= 3;
                },
                true);
    }

    // ✅ Eventi per nome del Paese (es. United States)
    public String getEventiPerPaese(String nomePaese) {
        return parseEventi("📅 Eventi macro per " + nomePaese + ":",
                evento -> {
                    String paese = evento.select(".flagCur").attr("title");
                    return paese.equalsIgnoreCase(nomePaese);
                },
                true);
    }
}
