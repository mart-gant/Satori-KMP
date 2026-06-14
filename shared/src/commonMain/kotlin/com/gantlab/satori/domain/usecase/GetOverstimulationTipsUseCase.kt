package com.gantlab.satori.domain.usecase

import com.gantlab.satori.domain.model.Tip

class GetOverstimulationTipsUseCase {
    operator fun invoke(): List<Tip> = listOf(
        Tip("Głębokie oddychanie", "Zamknij oczy i weź 5 głębokich oddechów, skupiając się tylko na powietrzu.", "🧘"),
        Tip("Redukcja światła", "Przyciemnij ekran telefonu lub wyjdź do ciemniejszego pomieszczenia na 5 minut.", "💡"),
        Tip("Biały szum", "Włącz dźwięk deszczu lub biały szum, aby odciąć się od nagłych dźwięków otoczenia.", "🎧"),
        Tip("Zasada 20-20-20", "Co 20 minut spójrz na obiekt oddalony o 20 stóp (6m) przez 20 sekund.", "👀"),
        Tip("Zimna woda", "Przemyj nadgarstki lub twarz zimną wodą, aby pobudzić układ przywspółczulny.", "💧")
    )
}
