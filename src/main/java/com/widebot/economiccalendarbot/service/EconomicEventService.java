package com.widebot.economiccalendarbot.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EconomicEventService {

    public String getCalendarioDiOggi() {
        StringBuilder calendario = new StringBuilder();
        calendario.append("🗓️ Calendario Economico di Oggi:\n\n");

        try {
            // URL del calendario economico
            Document doc = Jsoup.connect("https://www.investing.com/economic-calendar/")
                    .userAgent("Mozilla/5.0")  // importante per evitare blocchi
                    .get();

            // Seleziona tutti gli eventi della giornata
            Elements eventi = doc.select(".js-event-item");

            for (Element evento : eventi) {
                String ora = evento.select(".time").text();
                String titolo = evento.select(".event").text();
                int stelle = evento.select(".grayFullBullishIcon, .bullishIcon, .mediumImpactIcon").size();

                // Solo eventi visibili con orario e titolo
                if (!ora.isBlank() && !titolo.isBlank()) {
                    calendario
                            .append("⏰ ").append(ora).append(" - ")
                            .append(titolo).append(" (")
                            .append(stelle).append("⭐)\n");
                }
            }

        } catch (IOException e) {
            return "❌ Errore nel recupero del calendario economico.";
        }

        return calendario.toString().isBlank() ? "Nessun evento trovato per oggi." : calendario.toString();
    }

    public String getEventiPerValuta(String codiceValuta) {
        StringBuilder calendario = new StringBuilder();
        calendario.append("🗓️ Eventi economici per: ").append(codiceValuta.toUpperCase()).append("\n\n");

        try {
            Document doc = Jsoup.connect("https://www.investing.com/economic-calendar/")
                    .userAgent("Mozilla/5.0")
                    .get();

            Elements eventi = doc.select(".js-event-item");

            for (Element evento : eventi) {
                String ora = evento.select(".time").text();
                String titolo = evento.select(".event").text();
                String valuta = evento.select(".left.flagCur.noWrap").text().trim();// es: "USD", "EUR"
                int stelle = evento.select(".grayFullBullishIcon, .bullishIcon, .mediumImpactIcon").size();

                if (!ora.isBlank() && !titolo.isBlank() && valuta.equalsIgnoreCase(codiceValuta)) {
                    calendario.append("⏰ ").append(ora).append(" - ").append(titolo)
                            .append(" (").append(stelle).append("⭐)\n");
                }
            }

        } catch (IOException e) {
            return "❌ Errore durante il recupero degli eventi economici per " + codiceValuta;
        }

        return calendario.toString().isBlank()
                ? "Nessun evento trovato per " + codiceValuta.toUpperCase()
                : calendario.toString();
    }

    public String getEventiPerPaese(String paese) {
        StringBuilder calendario = new StringBuilder();
        calendario.append("📅 Eventi macro per ").append(paese).append(":\n\n");

        try {
            Document doc = Jsoup.connect("https://www.investing.com/economic-calendar/")
                    .userAgent("Mozilla/5.0")
                    .get();

            Elements eventi = doc.select(".js-event-item");

            for (Element evento : eventi) {
                String country = evento.select(".flagCur").attr("title"); // es: United States
                if (!country.equalsIgnoreCase(paese)) continue;

                String ora = evento.select(".time").text();
                String titolo = evento.select(".event").text();
                int stelle = evento.select(".grayFullBullishIcon, .bullishIcon, .mediumImpactIcon").size();

                calendario.append("🇺🇸 ").append(ora).append(" - ")
                        .append(titolo).append(" (")
                        .append(stelle).append("⭐)\n");
            }

        } catch (IOException e) {
            return "❌ Errore nel recupero degli eventi USA.";
        }

        return calendario.toString().isBlank() ? "Nessun evento per gli Stati Uniti oggi." : calendario.toString();
    }

    public String getEventiAdAltoImpatto() {
        StringBuilder calendario = new StringBuilder();
        calendario.append("🔥 Eventi ad alto impatto (⭐⭐⭐+)\n\n");

        try {
            Document doc = Jsoup.connect("https://www.investing.com/economic-calendar/")
                    .userAgent("Mozilla/5.0")
                    .get();

            Elements eventi = doc.select(".js-event-item");

            for (Element evento : eventi) {
                String ora = evento.select(".time").text();
                String titolo = evento.select(".event").text();
                String valuta = evento.select(".left.flagCur.noWrap").text().trim();

                int stelle = evento.select(".grayFullBullishIcon, .bullishIcon, .mediumImpactIcon").size();

                // Eventi con almeno 3 stelle
                if (!ora.isBlank() && !titolo.isBlank() && stelle >= 3) {
                    calendario.append("⏰ ").append(ora).append(" - [")
                            .append(valuta).append("] ").append(titolo)
                            .append(" (").append(stelle).append("⭐)\n");
                }
            }

        } catch (IOException e) {
            return "❌ Errore durante il recupero degli eventi ad alto impatto.";
        }

        return calendario.toString().isBlank()
                ? "Nessun evento ad alto impatto trovato per oggi."
                : calendario.toString();
    }



}
