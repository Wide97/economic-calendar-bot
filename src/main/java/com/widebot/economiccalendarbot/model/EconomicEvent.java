package com.widebot.economiccalendarbot.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class EconomicEvent {
    private final String ora;
    private final String titolo;
    private final String valuta;
    private final int stelle;
    private final String paese;
}
